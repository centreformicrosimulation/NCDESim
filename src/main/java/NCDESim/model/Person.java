package NCDESim.model;

import NCDESim.algorithms.Helpers;
import NCDESim.data.Parameters;
import NCDESim.model.objects.Job;
import lombok.*;
import microsim.data.db.PanelEntityKey;
import microsim.engine.SimulationEngine;
import microsim.statistics.IDoubleSource;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Transient;
import java.util.*;

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

	private int searchIntensity;
	@Transient
	private Job job;

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
		this.job = null; // Job of the person
		this.searchIntensity = SimulationEngine.getRnd().nextInt(Parameters.MAXIMUM_NUMBER_OF_JOBS_SAMPLED_BY_PERSON);
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
			age();
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

	public void age() {
		age++;
	}

	public double calculateWellbeing() {
		double wage = this.wage;
		double health = this.health;

		double wellbeing = wage + health;
		
		return wellbeing;
	}

	// Called when a person is hired by a firm to update employment-related information
	public void updateEmploymentVariables(Job job) {
		this.job = job; // Set person's job
		this.wage = job.getWage();
	}

	// Method to allow person to search through the list of jobs and accept one
	public void searchForJob() {
		List<Job> sampledJobList = Helpers.pickNRandom(model.getJobList(), searchIntensity); // Sample n = searchIntensity jobs from all available. This produces a list of jobs available to this person.
		Job selectedJob = Collections.max(sampledJobList); // Choose the "maximum" job from the list of sampled jobs. Note that the definition of the "maximum" depends on the comparator of the Job class.
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