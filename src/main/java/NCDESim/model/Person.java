package NCDESim.model;

import lombok.*;
import microsim.data.db.PanelEntityKey;
import microsim.engine.SimulationEngine;
import microsim.statistics.IDoubleSource;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Transient;

@Entity
@Getter
@Setter
@ToString
@RequiredArgsConstructor
public class Person extends Agent implements IDoubleSource {

	@EmbeddedId
	private PanelEntityKey key = new PanelEntityKey(idCounter++);

	@Transient
	private static long idCounter = 1;

	private double age = 100 * SimulationEngine.getRnd().nextDouble();		//Each agent has a random age between 0 and 100


	// ---------------------------------------------------------------------
	// EventListener
	// ---------------------------------------------------------------------

	public enum Processes {
		Ageing;
	}

	public void onEvent(Enum<?> type) {
		switch ((Processes) type) {
		case Ageing:
		}
	}



	// ---------------------------------------------------------------------
	// IDoubleSource
	// ---------------------------------------------------------------------

	public enum Variables{
		Age;
	}

	@Override
	public double getDoubleValue(Enum<?> variable) {
		switch((Variables) variable){
			case Age:
				return age;

			default: 
				throw new IllegalArgumentException("Unsupported variable");
		}
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Person person = (Person) o;
		return key != null && key.equals(person.key);
	}

	@Override
	public int hashCode() {
		return getClass().hashCode();
	}
}