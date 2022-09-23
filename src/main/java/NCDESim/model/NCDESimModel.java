package NCDESim.model;

import NCDESim.algorithms.Helpers;
import NCDESim.data.filters.FirmRemovalFilter;
import NCDESim.model.objects.Job;
import lombok.Data;
import microsim.annotation.GUIparameter;
import microsim.data.db.DatabaseUtils;
import microsim.engine.AbstractSimulationManager;
import microsim.engine.SimulationEngine;
import microsim.event.EventGroup;
import microsim.event.EventListener;
import microsim.event.Order;
import microsim.event.SingleTargetEvent;
import microsim.statistics.CrossSection;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.log4j.Logger;

import java.util.*;

@Data
public class NCDESimModel extends AbstractSimulationManager implements EventListener {

	//Parameters of the model
	private final static Logger log = Logger.getLogger(NCDESimModel.class);
	@GUIparameter(description = "Use a fixed random seed to start (pseudo) random number generator")
	boolean fixRandomSeed = true;
	@GUIparameter(description = "Seed of the (pseudo) random number generator if fixed")
	Long seedIfFixed = 1166517026l;
	@GUIparameter(description = "Set the number of agents to create at launch")
	Integer numberOfAgents = 100;
	@GUIparameter(description = "Set the number of firms to create at launch")
	Integer initialNumberOfFirms = 100;

	@GUIparameter(description = "Set the number of firms to create each year")
	Integer perYearNumberOfFirms = 10;

	@GUIparameter(description = "Set the number of firms to create each year")
	Double shareOfNewFirmsCloned = 0.75;
	@GUIparameter(description = "Set the time at which the simulation will terminate")
	Double endTime = 100.;

	//Objects
	private List<Person> individuals;
	private Set<AbstractFirm> firms;
	private List<Job> jobList; //List of job offers made by firms, characterised by wage and amenity

	// ---------------------------------------------------------------------
	// Manager methods
	// ---------------------------------------------------------------------

	public void buildObjects() {

		if(fixRandomSeed)										// If fixed, the model will follow the same trajectory as other executions withe same random number seed.
			SimulationEngine.getRnd().setSeed(seedIfFixed);

		createAgents();
//		loadAgentsFromDatabase(); //Can be used instead of createAgents() to load agents from h2 database

		createAuxiliaryObjects(); // Initialize jobList

	}

	public void buildSchedule() {

		EventGroup modelEvents = new EventGroup();

		modelEvents.addCollectionEvent(individuals, Person.Processes.BeginNewYear); // Update values of lagged variables

		modelEvents.addEvent(this, Processes.AddNewFirms);

		modelEvents.addCollectionEvent(individuals, Person.Processes.Ageing);
		modelEvents.addCollectionEvent(firms, FirmTypeA.Processes.PostJobOffers);
		modelEvents.addCollectionEvent(individuals, Person.Processes.SearchForJob);

		modelEvents.addCollectionEvent(individuals, Person.Processes.Update); // Update persons' state variables
		modelEvents.addCollectionEvent(firms, FirmTypeA.Processes.Update); // Update firms' state variables

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
		RemoveFirms,
		End;
	}

	public void onEvent(Enum<?> type) {
		switch ((Processes) type) {
		case AddNewFirms:
			addNewFirms();
			break;
		case RemoveFirms:
			removeFirms();
			break;
		case End:
			getEngine().end();
			break;

		}
	}

	// ---------------------------------------------------------------------
	// Own methods
	// ---------------------------------------------------------------------

	protected void createAgents() {
		/*
		Create a collection of individuals to simulate
		 */
		individuals = new LinkedList<>();
		for(int i=0; i < numberOfAgents; i++) {
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
		List<AbstractFirm> listOfFirmsToClone = new ArrayList<>();
		double highestProfit = Helpers.findHighestProfitFromListOfFirms(listOfFirmsInTheSimulation); // Highest profit observed in the simulated period

		for (int i = 0; i < numberOfNewClonedFirmsToAdd; i++) { // Sample existing firms with probability corresponding to their profits / maximum profit observed in the simulated year, until the desired number of firms is met
			for (AbstractFirm firm : listOfFirmsInTheSimulation) {
				double weight = firm.getProfit() / highestProfit;
				if (Double.isNaN(weight)) weight = 0.5;
				boolean add = SimulationEngine.getRnd().nextDouble() <= weight;
				if (add) {
					listOfFirmsToClone.add(new FirmTypeA(firm));
					i++;
					if (i >= numberOfNewClonedFirmsToAdd) {
						break;
					}
				}
			}
		}
		firms.addAll(listOfFirmsToClone); // Done here because otherwise would sample cloned firms when cloning
	}

	private void removeFirms() {
		List<AbstractFirm> firmsToRemove = new ArrayList<>();
		CollectionUtils.select(getFirms(), new FirmRemovalFilter<AbstractFirm>(), firmsToRemove);
		for (AbstractFirm firm : firmsToRemove) {
			firm.prepareForRemoval();
		}
		firms.removeAll(firmsToRemove); // Remove all firms which meet criteria specified in the FirmRemovalFilter
	}

	protected void createAuxiliaryObjects() {
		jobList = new LinkedList<>(); // Initialize list of jobs available to workers
	}

	protected void loadAgentsFromDatabase() {
		//Load agents from an input/input.h2.db database containing the Person table (if there is one).
		individuals = (List<Person>) DatabaseUtils.loadTable(Person.class);
	}

	// ---------------------------------------------------------------------
	// Access methods are handled by Lombok by default
	// ---------------------------------------------------------------------


}