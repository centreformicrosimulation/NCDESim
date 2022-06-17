package NCDESim.model;

import NCDESim.experiment.NCDESimCollector;
import lombok.Data;
import microsim.data.db.DatabaseUtils;
import microsim.engine.AbstractSimulationManager;
import microsim.annotation.GUIparameter;
import microsim.event.EventGroup;
import microsim.event.EventListener;
import microsim.event.Order;
import microsim.event.SingleTargetEvent;

import java.util.*;

import org.apache.log4j.Logger;

import javax.persistence.Transient;

@Data
public class NCDESimModel extends AbstractSimulationManager implements EventListener {

	private final static Logger log = Logger.getLogger(NCDESimModel.class);

	@Transient
	NCDESimCollector collector;

	@GUIparameter(description = "Set the number of agents to create")
	Integer numberOfAgents = 10;

	@GUIparameter(description = "Set the number of firms to create")
	Integer numberOfFirms = 5;

	@GUIparameter(description = "Set the time at which the simulation will terminate")
	Double endTime = 20.;

	private List<Person> individuals;
	private Set<FirmTypeA> firms;


	// ---------------------------------------------------------------------
	// Manager methods
	// ---------------------------------------------------------------------

	public void buildObjects() {

		createAgents();
//		loadAgentsFromDatabase(); //Can be used instead of createAgents() to load agents from h2 database

		log.debug("Model objects created");

	}

	public void buildSchedule() {

		EventGroup modelEvents = new EventGroup();

//		modelEvents.addCollectionEvent(agentsLoadedFromInputDatabase, Person.Processes.AgentProcess);
		modelEvents.addCollectionEvent(individuals, Person.Processes.Ageing);

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
		individuals = new ArrayList<Person>();
		for(int i=0; i < numberOfAgents; i++) {
			individuals.add(new Person());
		}

		/*
		Create a collection of firms to simulate
		 */
		firms = new LinkedHashSet<FirmTypeA>();
		for (int i=0; i < numberOfFirms; i++) {
			firms.add(new FirmTypeA(true));
		}
	}

	protected void loadAgentsFromDatabase() {
		//Load agents from an input/input.h2.db database containing the Person table (if there is one).
		individuals = (List<Person>) DatabaseUtils.loadTable(Person.class);
	}



}