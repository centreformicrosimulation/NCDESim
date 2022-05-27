package NCDESim.model;

import microsim.engine.AbstractSimulationManager;
import microsim.annotation.GUIparameter;
import microsim.data.db.DatabaseUtils;
import microsim.event.EventGroup;
import microsim.event.EventListener;
import microsim.event.Order;
import microsim.event.SingleTargetEvent;

import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;

public class NCDESimModel extends AbstractSimulationManager implements EventListener {

	private final static Logger log = Logger.getLogger(NCDESimModel.class);

	@GUIparameter(description = "Set the number of agents to create")
	Integer numberOfAgents = 10;

	@GUIparameter(description = "Set the time at which the simulation will terminate")
	Double endTime = 20.;

	private List<Agent> agentsCreated;

//	private List<Agent> agentsLoadedFromInputDatabase;

	// ---------------------------------------------------------------------
	// Manager methods
	// ---------------------------------------------------------------------

	public void buildObjects() {

		//Load agents from an input/input.h2.db database containing the Agent table (if there is one).
//		agentsLoadedFromInputDatabase = (List<Agent>) DatabaseUtils.loadTable(Agent.class);

		//Alternatively, create a collection of agents here
		agentsCreated = new ArrayList<Agent>();
		for(int i=0; i < numberOfAgents; i++) {
			agentsCreated.add(new Agent());
		}

		log.debug("Model objects created");

	}

	public void buildSchedule() {

		EventGroup modelEvents = new EventGroup();

//		modelEvents.addCollectionEvent(agentsLoadedFromInputDatabase, Agent.Processes.AgentProcess);
		modelEvents.addCollectionEvent(agentsCreated, Agent.Processes.AgentProcess);

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



	// ---------------------------------------------------------------------
	// Access methods
	// ---------------------------------------------------------------------

	public Double getEndTime() {
		return endTime;
	}

	public void setEndTime(Double endTime) {
		this.endTime = endTime;
	}

	public Integer getNumberOfAgents() {
		return numberOfAgents;
	}

	public void setNumberOfAgents(Integer numberOfAgents) {
		this.numberOfAgents = numberOfAgents;
	}

	public List<Agent> getAgentsCreated() {
		return agentsCreated;
	}

//	public List<Agent> getAgentsLoadedFromInputDatabase() {
//		return agentsLoadedFromInputDatabase;
//	}

}