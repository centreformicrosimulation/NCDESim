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
	private double alpha;
	private double age;
	private double health;
	private double productivity;
	private double wage;
	private double wellbeing;

	// ---------------------------------------------------------------------
	// Constructors and Initialization
	// ---------------------------------------------------------------------
	public Person() {
		super();

		this.key = new PanelEntityKey(idCounter++);
		this.alpha = SimulationEngine.getRnd().nextDouble(); // Value of parameter alpha
		this.age = 100 * SimulationEngine.getRnd().nextDouble(); // Each person has a random age between 0 and 100
		this.health = SimulationEngine.getRnd().nextDouble() * 2 - 1; // Each person has a random health level between -1 and 1
		this.productivity = SimulationEngine.getRnd().nextDouble(); // Each person has a random productivity between 0 and 1
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

	// ---------------------------------------------------------------------
	// Own methods
	// ---------------------------------------------------------------------

	public double calculateWellbeing() {
		double wage = this.wage;
		double health = this.health;

		double wellbeing = wage + health;
		
		return wellbeing;
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