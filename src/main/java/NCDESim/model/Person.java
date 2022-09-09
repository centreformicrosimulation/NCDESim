package NCDESim.model;

import NCDESim.algorithms.Helpers;
import NCDESim.data.Parameters;
import NCDESim.model.objects.Job;
import lombok.*;
import microsim.data.db.PanelEntityKey;
import microsim.engine.SimulationEngine;
import microsim.statistics.IDoubleSource;
import jakarta.persistence.Entity;
import jakarta.persistence.Transient;
import jakarta.persistence.EmbeddedId;
import java.util.*;

@Entity
@Getter
@Setter
@ToString
public class Person extends Agent implements IDoubleSource, Comparable<Person> {

	@EmbeddedId
	private PanelEntityKey key;
	@Transient
	private static long idCounter = 1;
	private double alpha;
	private int age;
	private double health, health_L1;
	private double productivity;
	private double wage;

	private double utility;

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
		this.age = SimulationEngine.getRnd().nextInt(100); // Each person has a random age between 0 and 100
		this.health = SimulationEngine.getRnd().nextDouble() * 2 - 1; // Each person has a random health level between -1 and 1
		this.productivity = SimulationEngine.getRnd().nextDouble(); // Each person has a random productivity between 0 and 1
		this.job = new Job(null, 0., 0.); // Job of the person
		this.wage = job.getWage(); // Wage of the person
		this.searchIntensity = SimulationEngine.getRnd().nextInt(Parameters.MAXIMUM_NUMBER_OF_JOBS_SAMPLED_BY_PERSON)+1;

		// Initialise lagged values
		this.health_L1 = health; // In the first period, lagged value of health is equal to the value of health
	}

	// ---------------------------------------------------------------------
	// EventListener
	// ---------------------------------------------------------------------

	public enum Processes {
		Ageing,
		BeginNewYear,
		SearchForJob,
		UpdateHealth,
		UpdateUtility;
	}

	public void onEvent(Enum<?> type) {
		switch ((Processes) type) {
			case Ageing:
				age();
				break;
			case BeginNewYear:
				beginNewYear();
				break;
			case SearchForJob:
				searchForJob();
				break;
			case UpdateHealth:
				updateHealth();
				break;
			case UpdateUtility:
				updateUtility();
				break;
		}
	}


	// ---------------------------------------------------------------------
	// IDoubleSource
	// ---------------------------------------------------------------------

	public enum Variables{
		Age,
		Health,
		Wage;
	}

	@Override
	public double getDoubleValue(Enum<?> variable) {
		switch((Variables) variable){
			case Age:
				return age;
			case Health:
				return health;
			case Wage:
				return wage;
			default: 
				throw new IllegalArgumentException("Unsupported variable");
		}
	}


	// ---------------------------------------------------------------------
	// Own methods
	// ---------------------------------------------------------------------

	/**
	 * Methods related to basic characteristics below
	 */

	// Method to update lagged values at the beginning of each new year
	public void beginNewYear() {
		// Update lagged values
		this.health_L1 = health;
	}

	public void age() {
		age++;
	}

	// Method to calculate the level of health
	public void updateHealth() {
		health = health_L1 + alpha * job.getAmenity(); // Level of health = previous level of health + alpha * level of amenity in current job
	}

	public void updateUtility() {
		utility = calculateUtility();
	}

	public double calculateUtility() { // Utility is also referred to as well-being in the model
		return Parameters.evaluateUtilityFunction(health, wage);
	}

	/**
	 * Methods related to employment below
	 */

	// Called when a person is hired by a firm to update employment-related information
	public void updateEmploymentVariables(Job job) {
		this.job = job; // Set person's job
		this.wage = job.getWage();
	}

	// Method to allow person to search through the list of jobs and accept one
	public void searchForJob() {
		List<Job> sampledJobList = Helpers.pickNRandomJobs(model.getJobList(), searchIntensity); // Sample n = searchIntensity jobs from all available. This produces a list of jobs available to this person.
		Map<Job, Double> utilityOfSampledJobsMap = calculateUtilityOfListOfJobs(sampledJobList); // A map of job - utility combinations for jobs sampled in the previous step
		Job selectedJob = findJobWithHighestUtility(utilityOfSampledJobsMap); // Choose the job providing maximum utility to the person, from the list of sampled jobs.
		this.updateEmploymentVariables(selectedJob); // Set person's job and wage
		selectedJob.getEmployer().hireEmployee(this);
		model.getJobList().remove(selectedJob); //Remove accepted job offer from the list of available offers
	}

	// Method to calculate a utility of each job on the list. Returns a job - utility mapping.
	public Map<Job, Double> calculateUtilityOfListOfJobs(List<Job> listOfJobs) {
		Map<Job, Double> jobUtilityMapToReturn = new LinkedHashMap<>();
		for (Job j : listOfJobs) {
			double health = this.health;
			double wage = j.getWage();
			double utility = Parameters.evaluateUtilityFunction(health, wage);
			jobUtilityMapToReturn.put(j, utility);
		}
		return jobUtilityMapToReturn;
	}

	public Job findJobWithHighestUtility(Map<Job, Double> mapOfJobsAndUtilities) {
		Job jobToReturn;
		double maxUtilityInTheList = Collections.max(mapOfJobsAndUtilities.values()); // Find the highest utility in the map of job-utility pairs
		List<Job> keys = new ArrayList<>(); // List of jobs with maximum utility. If more than one, pick the first one.
		for (Map.Entry<Job, Double> entry : mapOfJobsAndUtilities.entrySet()) {
			if (entry.getValue()==maxUtilityInTheList) { // If utility of a job is equal to the maximum observed in the list...
				keys.add(entry.getKey()); // ...add to a list of jobs with the highest utility
			}
		}
		jobToReturn = keys.get(0);
		return jobToReturn;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Person person = (Person) o;
		return key != null && key.equals(person.key);
	}

	@Override
	public int compareTo(Person o) {
		Person p = o;
		return (int) (this.getKey().getId() - p.getKey().getId());
	}

	@Override
	public int hashCode() {
		return getClass().hashCode();
	}
}