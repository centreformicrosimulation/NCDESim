package NCDESim.model;

import lombok.*;
import microsim.data.db.PanelEntityKey;
import microsim.engine.SimulationEngine;
import microsim.event.EventListener;
import microsim.statistics.IDoubleSource;
import microsim.statistics.regression.RegressionUtils;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Transient;

@Entity
@Getter
@Setter
@ToString
@RequiredArgsConstructor
public class Agent implements EventListener, IDoubleSource {

	@EmbeddedId
	private PanelEntityKey key = new PanelEntityKey(idCounter++);

	@Transient
	private static long idCounter = 1000000;

	private double wealth = 100 * SimulationEngine.getRnd().nextDouble();		//Each agent has a random endowment of wealth


	// ---------------------------------------------------------------------
	// EventListener
	// ---------------------------------------------------------------------

	public enum Processes {
		AgentProcess;
	}

	public void onEvent(Enum<?> type) {
		switch ((Processes) type) {
		case AgentProcess:
			//TODO: code to manage process
			if(RegressionUtils.event(0.1)) {	//Lose 90% of wealth with a 10% probability
				wealth /= 10.;
			}
			else {
				wealth *= 1.1;			//Else wealth grows steadily by 10% per time-step
			}

			break;
		}
	}



	// ---------------------------------------------------------------------
	// IDoubleSource
	// ---------------------------------------------------------------------

	public enum Variables{
		Wealth;
	}

	@Override
	public double getDoubleValue(Enum<?> variable) {
		switch((Variables) variable){
			case Wealth:
				return wealth;

			default: 
				throw new IllegalArgumentException("Unsupported variable");
		}
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Agent agent = (Agent) o;
		return key != null && key.equals(agent.key);
	}

	@Override
	public int hashCode() {
		return getClass().hashCode();
	}
}