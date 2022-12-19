package NCDESim.model;

import NCDESim.algorithms.Helpers;
import NCDESim.data.Parameters;
import NCDESim.data.filters.FirmRemovalFilter;
import NCDESim.data.filters.IndividualCanLookForJobFilter;
import NCDESim.data.filters.PersonRemovalFilter;
import NCDESim.experiment.NCDESimCollector;
import NCDESim.model.objects.Job;
import jakarta.persistence.Transient;
import lombok.Data;
import microsim.annotation.GUIparameter;
import microsim.data.db.DatabaseUtils;
import microsim.engine.AbstractSimulationManager;
import microsim.engine.SimulationEngine;
import microsim.event.EventGroup;
import microsim.event.EventListener;
import microsim.event.Order;
import microsim.event.SingleTargetEvent;
import microsim.statistics.IDoubleSource;
import microsim.statistics.IIntSource;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.log4j.Logger;

import java.util.*;

@Data
public class NCDESimModel extends AbstractSimulationManager implements EventListener, IDoubleSource, IIntSource {

    //Parameters of the model
    private final static Logger log = Logger.getLogger(NCDESimModel.class);
    @GUIparameter(description = "Use a fixed random seed to start (pseudo) random number generator")
    boolean fixRandomSeed = true;
    @GUIparameter(description = "Seed of the (pseudo) random number generator if fixed")
    Long seedIfFixed = 1166517026L;
    @GUIparameter(description = "Set the number of persons to create at launch")
    Integer numberOfPersons = 100;
    @GUIparameter(description = "Set the number of persons to create each year")
    Integer perYearNumberOfPersons = 10;
    @GUIparameter(description = "Set the number of firms to create at launch")
    Integer initialNumberOfFirms = 100;
    @GUIparameter(description = "Set the number of firms to create each year")
    Integer perYearNumberOfFirms = 10;
    @GUIparameter(description = "Set the number of firms to create each year")
    Double shareOfNewFirmsCloned = 0.75;
    @GUIparameter(description = "Set the time at which the simulation will terminate")
    Double endTime = 100.;
    @GUIparameter(description = "Multiplier on the cost of amenity provided by firms")
    Double amenityCostMultiplier = 0.01;
    @GUIparameter(description = "Health decay parameter lambda")
    Double lambda = 0.1;
    private int time;
    private List<Person> individuals;
    private Set<AbstractFirm> firms;
    private List<Job> jobList; //List of job offers made by firms, characterised by wage and amenity

    // ---------------------------------------------------------------------
    // Manager methods
    // ---------------------------------------------------------------------

    public void buildObjects() {

        if (fixRandomSeed)                                        // If fixed, the model will follow the same trajectory as other executions withe same random number seed.
            SimulationEngine.getRnd().setSeed(seedIfFixed);

        createAgents();
//		loadAgentsFromDatabase(); //Can be used instead of createAgents() to load agents from h2 database

        createAuxiliaryObjects(); // Initialize jobList

    }

    public void buildSchedule() {

        EventGroup modelEvents = new EventGroup();

        modelEvents.addEvent(this, Processes.BeginNewYear); // Increment model time variable by 1
        modelEvents.addCollectionEvent(individuals, Person.Processes.BeginNewYear); // Update values of individuals' lagged variables

        modelEvents.addEvent(this, Processes.AddNewPersons);
        modelEvents.addEvent(this, Processes.AddNewFirms);

        //	modelEvents.addCollectionEvent(individuals, Person.Processes.Ageing);
        modelEvents.addCollectionEvent(firms, FirmTypeA.Processes.PostJobOffers);

        modelEvents.addEvent(this, Processes.JobSearch);

        modelEvents.addCollectionEvent(individuals, Person.Processes.Update); // Update persons' state variables
        modelEvents.addCollectionEvent(firms, FirmTypeA.Processes.Update); // Update firms' state variables

        modelEvents.addEvent(this, Processes.RemoveFirms); // Remove firms which meet criteria specified in FirmRemovalFilter from the simulation
        modelEvents.addEvent(this, Processes.RemovePersons); // Remove persons who meet criteria specified in PersonRemovalFilter from the simulation

        getEngine().getEventQueue().scheduleRepeat(modelEvents, 0., 0, 1.);
        getEngine().getEventQueue().scheduleOnce(new SingleTargetEvent(this, Processes.End), endTime, Order.AFTER_ALL.getOrdering());

        log.debug("Model schedule created");

    }
    


    // ---------------------------------------------------------------------
    // EventListener
    // ---------------------------------------------------------------------

    public enum Processes {
        AddNewFirms,
        AddNewPersons,
        JobSearch,
        RemoveFirms,
        RemovePersons,
        End,
        BeginNewYear
    }

    public void onEvent(Enum<?> type) {
        switch ((Processes) type) {
            case AddNewFirms -> addNewFirms();
            case AddNewPersons -> addNewPersons();
            case JobSearch -> jobSearch();
            case RemoveFirms -> removeFirms();
            case RemovePersons -> removePersons();
            case End -> getEngine().end();
            case BeginNewYear -> {
                time++;
                clearJobList();
            }
        }
    }
    // ---------------------------------------------------------------------
    // IIntSource
    // ---------------------------------------------------------------------
    public enum IntVariables {
        NumberOfFirms,
        NumberOfPersons,
    }

    @Override
    public int getIntValue(Enum<?> variable) {
        return switch ((IntVariables) variable) {

            case NumberOfFirms -> getFirms().size();
            case NumberOfPersons -> getIndividuals().size();
        };
    }
    // ---------------------------------------------------------------------
    // IDoubleSource
    // ---------------------------------------------------------------------
    public enum DoubleVariables {
        NumberOfJobs,

    }
    
    @Override
    public double getDoubleValue(Enum<?> variable) {
        return switch ((DoubleVariables) variable) {

            case NumberOfJobs -> getJobList().size();
        };
    }

    // ---------------------------------------------------------------------
    // Own methods
    // ---------------------------------------------------------------------

    protected void createAgents() {
		/*
		Create a collection of individuals to simulate
		 */
        individuals = new LinkedList<>();
        for (int i = 0; i < numberOfPersons; i++) {
            individuals.add(new Person());
        }

		/*
		Create a collection of firms to simulate
		 */
        firms = new LinkedHashSet<>();
        for (int i = 0; i < initialNumberOfFirms; i++) {
            firms.add(new FirmTypeA(true));
        }
    }

    /**
     * Method to create new firms each simulated year.
     * perYearNumberOfFirms is created, split between clones of existing firms and new firms, with random level of
     * wages and amenities. The split is determined by shareOfNewFirmsCloned.
     */
    protected void addNewFirms() {
        int numberOfNewClonedFirmsToAdd = (int) (perYearNumberOfFirms * shareOfNewFirmsCloned);
        int numberOfNewRandomFirmsToAdd = perYearNumberOfFirms - numberOfNewClonedFirmsToAdd;
        List<AbstractFirm> listOfFirmsInTheSimulation = new ArrayList<>(firms);
        List<AbstractFirm> listOfClonedFirms = new ArrayList<>(numberOfNewClonedFirmsToAdd);
        List<AbstractFirm> listOfRandomFirms = new ArrayList<>(numberOfNewRandomFirmsToAdd);
        double highestProfit = Helpers.findHighestProfitFromListOfFirms(listOfFirmsInTheSimulation); // Highest profit observed in the simulated period

        for (int i = 0; i < numberOfNewClonedFirmsToAdd; i++) { // Sample existing firms with probability corresponding to their profits / maximum profit observed in the simulated year, until the desired number of firms is met
            for (AbstractFirm firm : listOfFirmsInTheSimulation) {
                double weight = firm.getProfit() / highestProfit;
                if (Double.isNaN(weight)) weight = 0.5;
                boolean add = SimulationEngine.getRnd().nextDouble() <= weight;
                if (add) {
                    listOfClonedFirms.add(new FirmTypeA(firm));
                    i++;
                    if (i >= numberOfNewClonedFirmsToAdd) {
                        break;
                    }
                }
            }
        }

        for (int i = 0; i < numberOfNewRandomFirmsToAdd; i++) { // Add the required number of new firms with random characteristics
            listOfRandomFirms.add(new FirmTypeA(true));
        }

        firms.addAll(listOfClonedFirms); // Done here because otherwise would sample cloned firms when cloning
        firms.addAll(listOfRandomFirms);
    }

    protected void addNewPersons() {
        int numberOfNewIndividualsToAdd = perYearNumberOfPersons;
        for (int i = 0; i < numberOfNewIndividualsToAdd; i++) {
            individuals.add(new Person());
        }
    }

    private void jobSearch() {
        List<Person> individualsLookingForJobs; // According to the documentation it is not possible to have filters in the yearly schedule directly. Job search is therefore all handled here, instead of splitting the updating of the filtered list and job search into two parts.

        if (!Parameters.ON_THE_JOB_SEARCH) {
            CollectionUtils.select(individuals, new IndividualCanLookForJobFilter<>(), individualsLookingForJobs);
        } else {
            individualsLookingForJobs = new ArrayList<>(individuals);
        }
        individualsLookingForJobs.forEach(Person::searchForJob); // Call searchForJob method on each person on the list of individuals lookingForJobs
    }

    private void clearJobList() {
        jobList.clear();
    }

    private void removeFirms() {
        List<AbstractFirm> firmsToRemove = new ArrayList<>();
        CollectionUtils.select(getFirms(), new FirmRemovalFilter<>(), firmsToRemove);
        Iterator<AbstractFirm> itr = firmsToRemove.iterator();
        while (itr.hasNext()) {
            AbstractFirm firm = itr.next();
            firm.prepareForRemoval();
            firms.remove(firm);
            itr.remove();
        }
    }

    private void removePersons() {
        ArrayList<Person> personsToRemove = new ArrayList<>();
        CollectionUtils.select(getIndividuals(), new PersonRemovalFilter<>(), personsToRemove);
        individuals.removeAll(personsToRemove);
    }

    protected void createAuxiliaryObjects() {
        jobList = new LinkedList<>(); // Initialize list of jobs available to workers
    }

    @SuppressWarnings("unchecked")
    // Suppress type check warning; should the type be checked when loading persons from the database?
    protected void loadAgentsFromDatabase() {
        //Load agents from an input/input.h2.db database containing the Person table (if there is one).
        individuals = (List<Person>) DatabaseUtils.loadTable(Person.class);
    }

    // ---------------------------------------------------------------------
    // Access methods are handled by Lombok by default
    // ---------------------------------------------------------------------


}