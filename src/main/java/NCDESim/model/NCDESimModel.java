package NCDESim.model;

import NCDESim.algorithms.Helpers;
import NCDESim.data.enums.UtilityFunctions;
import NCDESim.data.filters.FirmRemovalFilter;
import NCDESim.data.filters.IndividualCanLookForJobFilter;
import NCDESim.data.filters.PersonRemovalFilter;
import NCDESim.model.objects.Job;
import lombok.Data;
import lombok.EqualsAndHashCode;
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
@EqualsAndHashCode(callSuper = false)
public class NCDESimModel extends AbstractSimulationManager implements EventListener, IDoubleSource, IIntSource {

    //Parameters of the model
    private final static Logger log = Logger.getLogger(NCDESimModel.class);
    @GUIparameter(description = "Use a fixed random seed to start (pseudo) random number generator")
    boolean fixRandomSeed = true;
    @GUIparameter(description = "Seed of the (pseudo) random number generator if fixed")
    Long seedIfFixed = 1166517026L;
    @GUIparameter(description = "Set the number of persons to create at launch")
    Integer initialNumberOfPersons = 101;
    @GUIparameter(description = "Set the number of persons to create each year")
    Integer perYearNumberOfPersons = 100;
    @GUIparameter(description = "Set the number of firms to create at launch")
    Integer initialNumberOfFirms = 10;
    @GUIparameter(description = "Set the number of firms to create each year")
    Integer perYearNumberOfFirms = 10;
    @GUIparameter(description = "Set the equilibrium number of workers each firm wants to achieve")
    Integer firmDesiredSize = 5;
    @GUIparameter(description = "Set the share of firms cloned each year")
    Double shareOfNewFirmsCloned = 0.9;
    @GUIparameter(description = "Toggle to add random variation (noise) to cloned firms")
    boolean cloneWithNoise = true;
    @GUIparameter(description = "Set the time at which the simulation will terminate")
    Double endTime = 10000.;
    @GUIparameter(description = "Unit cost of amenity provided by firms")
    Double amenityUnitCost = 0.01;
    @GUIparameter(description = "Health decay")
    Double healthDecay = 0.1;
    @GUIparameter(description = "Health effect on productivity parameter")
    Double lambda = 1.;
    @GUIparameter(description = "Toggle to switch on the job search on / off")
    boolean onTheJobSearch = true; // If true, currently employed individuals will also look for jobs each period
    @GUIparameter(description = "Toggle to destroy jobs left during on the job search")
    boolean destroyJobs = false;
    @GUIparameter(description = "Search intensity unemployed")
    Integer searchIntensityUnemployed = 5;
    @GUIparameter(description = "Search intensity employed")
    Integer searchIntensityEmployed = 1;
    double desiredFirmSize = initialNumberOfPersons/initialNumberOfFirms;
    @GUIparameter(description = "Age of youngest persons created in the simulation")
    Integer personMinimumAge = 20;
    @GUIparameter(description = "Remove persons from the simulation when they reach this age")
    Integer personMaximumAge = 60;
    @GUIparameter(description = "Maximum potential age, used in normalisation of health score")
    Integer personMaximumPotentialAge = 80;
    @GUIparameter(description = "Remove firms with profits smaller or equal to this value")
    Double firmMinimumProfit = 0.;
    @GUIparameter(description = "Remove firms with number of employees smaller or equal to this value")
    Integer firmMinimumSize = 0;
    @GUIparameter(description = "Utility function used to calculate person's well-being.")
    UtilityFunctions utilityFunction = UtilityFunctions.CobbDouglas;
    @GUIparameter(description = "Total Factor Productivity for the CB Utility")
    Double cobbDouglasTFP = 1.;
    @GUIparameter(description = "Parameter Alpha for the CB Utility")
    Double cobbDouglasAlpha = 0.5;
    Double CobbDouglasUtilityBeta = 1 - cobbDouglasAlpha; // Parameter Beta for the CB Utility
    @GUIparameter(description = "Set to true to restrict the firm's cost of providing amenity from the bottom at zero. If false, firms providing negative amenity (dis-amenity) increase their profits.")
    boolean amenityCostFloorAtZero = false;
    @GUIparameter(description = "If true, individuals whose health equals zero will be removed from the simulation")
    boolean zeroHealthDeath = true;
    @GUIparameter(description = "Amount of noise +- 1 used when creating new firms.")
    Double noiseAmount = 0.1;

    private int time;
    private List<Person> individuals;
    private Set<AbstractFirm> firms;
    private List<Job> jobList; //List of job offers made by firms, characterised by wage and amenity
    private int numberOfFirmsCreated, numberOfFirmsDestroyed;

    // ---------------------------------------------------------------------
    // Manager methods
    // ---------------------------------------------------------------------

    public void buildObjects() {

        if (fixRandomSeed)                                        // If fixed, the model will follow the same trajectory as other executions withe same random number seed.
            SimulationEngine.getRnd().setSeed(seedIfFixed);

        checkParameters(); // Check that specified parameter values meet the conditions we impose
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

        modelEvents.addCollectionEvent(individuals, Person.Processes.Ageing);
        modelEvents.addCollectionEvent(firms, FirmTypeA.Processes.PostJobOffers);

        modelEvents.addEvent(this, Processes.JobSearch);

        modelEvents.addCollectionEvent(individuals, Person.Processes.Update); // Update persons' state variables
        modelEvents.addCollectionEvent(firms, FirmTypeA.Processes.Update); // Update firms' state variables

        modelEvents.addEvent(this, Processes.RemovePersons); // Remove persons who meet criteria specified in PersonRemovalFilter from the simulation. This should occur before firm removal, because it modifies the set of employees of a firm, and firm size = 0 is a condition for firm removal.
        modelEvents.addEvent(this, Processes.RemoveFirms); // Remove firms which meet criteria specified in FirmRemovalFilter from the simulation
        

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
                numberOfFirmsCreated = 0;
            }
        }
    }
    // ---------------------------------------------------------------------
    // IIntSource
    // ---------------------------------------------------------------------
    public enum IntVariables {
        NumberOfFirms,
        NumberOfPersons,
        NumberOfFirmsCreated,
        NumberOfFirmsDestroyed,
    }

    @Override
    public int getIntValue(Enum<?> variable) {
        return switch ((IntVariables) variable) {

            case NumberOfFirms -> getFirms().size();
            case NumberOfPersons -> getIndividuals().size();
            case NumberOfFirmsCreated -> numberOfFirmsCreated;
            case NumberOfFirmsDestroyed -> numberOfFirmsDestroyed;
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
		Create a collection of individuals to simulate.
		A fixed number of individuals of each age is created to obtain cohorts.
		 */
        individuals = new LinkedList<>();
        int numberOfPersonsToAddInEachAgeGroup = initialNumberOfPersons / (personMaximumAge - personMinimumAge + 1);

        for (int age = personMinimumAge; age <= personMaximumAge; age++) {
            for (int person = 0; person < numberOfPersonsToAddInEachAgeGroup; person++) {
                individuals.add(new Person(age));
            }
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

        int flowOfFirms = perYearNumberOfFirms;
        numberOfFirmsCreated += flowOfFirms;

        int numberOfNewClonedFirmsToAdd = (int) (flowOfFirms * shareOfNewFirmsCloned);
        int numberOfNewRandomFirmsToAdd = flowOfFirms - numberOfNewClonedFirmsToAdd;
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
                    if (cloneWithNoise) {
                        listOfClonedFirms.add(new FirmTypeA(firm, true)); // Clone firm with some amount of noise added
                    } else {
                        listOfClonedFirms.add(new FirmTypeA(firm));
                    }
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
        List<Person> individualsLookingForJobs = new ArrayList<>(); // According to the documentation it is not possible to have filters in the yearly schedule directly. Job search is therefore all handled here, instead of splitting the updating of the filtered list and job search into two parts.

        if (!onTheJobSearch) {
            CollectionUtils.select(individuals, new IndividualCanLookForJobFilter<>(), individualsLookingForJobs);
        } else {
            individualsLookingForJobs = new ArrayList<>(individuals);
        }
        Collections.shuffle(individualsLookingForJobs, SimulationEngine.getRnd()); // Shuffle individuals so the order in which they look for jobs is random
        individualsLookingForJobs.forEach(Person::searchForJob); // Call searchForJob method on each person on the list of individuals lookingForJobs
    }

    private void clearJobList() {
        jobList.clear();
    }

    private void removeFirms() {
        List<AbstractFirm> firmsToRemove = new ArrayList<>();
        CollectionUtils.select(getFirms(), new FirmRemovalFilter<>(firmMinimumSize, firmMinimumProfit), firmsToRemove);
        numberOfFirmsDestroyed = firmsToRemove.size();
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
        CollectionUtils.select(getIndividuals(), new PersonRemovalFilter<>(personMaximumAge, zeroHealthDeath), personsToRemove);
        Iterator<Person> itr = personsToRemove.iterator();
        while (itr.hasNext()) {
            Person person = itr.next();
            if (person.getJob().getEmployer() != null) {
                AbstractFirm firm = person.getJob().getEmployer();
                firm.removeEmployee(person);
            }
            individuals.remove(person);
            itr.remove();
        }
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

    /**
     * checkParameters() method verifies that parameters meet requirements specified below, such as:
     * 1) Number of firms cannot exceed the number of individuals
     */
    private void checkParameters() throws IllegalArgumentException {
        if (initialNumberOfFirms > initialNumberOfPersons) throw new IllegalArgumentException("Initial number of firms must not exceed the initial number of persons. Increase the number of persons or lower the number of firms.");
    }

    public double evaluateUtilityFunction(double health, double wage) { // This utility function is used to calculate person's well-being. Job offers are evaluated according to the level of well-being they generate.

        switch (utilityFunction) {
            case CobbDouglas -> {
                return cobbDouglasTFP * Math.pow(health, cobbDouglasAlpha) * Math.pow(wage, CobbDouglasUtilityBeta);
            }
            default -> {
                return 0;
            }
        }
    }

    // ---------------------------------------------------------------------
    // Access methods are handled by Lombok by default
    // ---------------------------------------------------------------------


}