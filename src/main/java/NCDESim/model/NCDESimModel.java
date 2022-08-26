package NCDESim.model;

import NCDESim.model.objects.Job;
import lombok.Data;
import microsim.data.db.DatabaseUtils;
import microsim.engine.AbstractSimulationManager;
import microsim.engine.SimulationEngine;
import microsim.annotation.GUIparameter;
import microsim.event.EventGroup;
import microsim.event.EventListener;
import microsim.event.Order;
import microsim.event.SingleTargetEvent;
import org.apache.log4j.Logger;

import java.util.*;

@Data
public class NCDESimModel extends AbstractSimulationManager implements EventListener {

	//Parameters of the model
	private final static Logger log = Logger.getLogger(NCDESimModel.class);
	@GUIparameter(description = "Use a fixed random seed to start (pseudo) random number generator")
	boolean fixRandomSeed 				= true;

	@GUIparameter(description = "Seed of the (pseudo) random number generator if fixed")
	Long seedIfFixed 					= 1166517026l;

	@GUIparameter(description = "Set the number of agents to create")
	Integer numberOfAgents = 10;

	@GUIparameter(description = "Set the number of firms to create")
	Integer numberOfFirms = 5;

	@GUIparameter(description = "Set the time at which the simulation will terminate")
	Double endTime = 20.;

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

		modelEvents.addCollectionEvent(individuals, Person.Processes.Ageing);
		modelEvents.addCollectionEvent(firms, FirmTypeA.Processes.PostJobOffers);

		getEngine().getEventQueue().scheduleRepeat(modelEvents, 0., 0, 1.);
		getEngine().getEventQueue().scheduleOnce(new SingleTargetEvent(this, Processes.End), endTime, Order.AFTER_ALL.getOrdering());

		log.debug("Model schedule created");

	}


	// ---------------------------------------------------------------------
	// EventListener
	// ---------------------------------------------------------------------

	public enum Processes {
		End;
	}

	public void onEvent(Enum<?> type) {
		switch ((Processes) type) {

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
		for (int i=0; i < numberOfFirms; i++) {
			firms.add(new FirmTypeA(true));
		}
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