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
public class Person extends Agent implements IDoubleSource {

	@EmbeddedId
	private PanelEntityKey key;

	@Transient
	private static long idCounter = 1;

	private double age;

	private double chronicPainLevel;

	private double psychologicalDistressLevel;

	private double hourlyReservationWage;

	// ---------------------------------------------------------------------
	// Constructors and Initialization
	// ---------------------------------------------------------------------
	public Person() {
		super();

		this.key = new PanelEntityKey(idCounter++);
		this.age = 100 * SimulationEngine.getRnd().nextDouble(); // Each person has a random age between 0 and 100
		this.chronicPainLevel = 10 * SimulationEngine.getRnd().nextDouble(); // Each person has a random chronic pain level between 0 and 10
		this.psychologicalDistressLevel = 10 * SimulationEngine.getRnd().nextDouble(); // Each person has a random psychological distress level between 0 and 10
		this.hourlyReservationWage = 1000 * SimulationEngine.getRnd().nextDouble(); // Each person has an hourly wage between 0 and 1000
	}

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