package NCDESim.model;

import NCDESim.algorithms.Helpers;
import NCDESim.data.Parameters;
import NCDESim.model.objects.Job;
import lombok.*;
import microsim.annotation.GUIparameter;
import microsim.data.db.PanelEntityKey;
import microsim.engine.SimulationEngine;
import microsim.statistics.IDoubleSource;
import jakarta.persistence.Entity;
import jakarta.persistence.Transient;
import jakarta.persistence.EmbeddedId;
import microsim.statistics.IIntSource;

import java.util.*;

@Entity
@Getter
@Setter
@ToString
public class Person extends Agent implements IDoubleSource, IIntSource, Comparable<Person> {

	@EmbeddedId
	private PanelEntityKey key;
	@Transient
	private static long idCounter = 1;
	private int age;
	private double health, health_L1;
	private double productivity;
	private double utility;
	private int searchIntensity;
	@Transient
	private Job job;
	private boolean flagChangedJobs;

	// ---------------------------------------------------------------------
	// Constructors and Initialization
	// ---------------------------------------------------------------------
	public Person() {
		super();

		this.key = new PanelEntityKey(idCounter++);
		this.age = SimulationEngine.getRnd().nextInt(100); // Each person has a random age between 0 and 100
		this.health = SimulationEngine.getRnd().nextDouble(); // Each person has a random health level between 0 and 1
		this.productivity = SimulationEngine.getRnd().nextDouble(); // Each person has a random productivity between 0 and 1
		this.job = new Job(null, 0., 0.); // Job of the person
		this.searchIntensity = SimulationEngine.getRnd().nextInt(Parameters.MAXIMUM_NUMBER_OF_JOBS_SAMPLED_BY_PERSON)+1; // Only used if turned on in Parameters

		// Initialise flag variables
		this.flagChangedJobs = false; // Indicates if individual who was employed changed jobs

		// Initialise lagged values
		this.health_L1 = health; // In the first period, lagged value of health is equal to the value of health
	}

	// ---------------------------------------------------------------------
	// EventListener
	// ---------------------------------------------------------------------

	public enum Processes {
		Ageing,
		BeginNewYear,
		Update
	}

	public void onEvent(Enum<?> type) {
		switch ((Processes) type) {
			case Ageing -> age();
			case BeginNewYear -> beginNewYear();
			case Update -> {
				updateHealth();
				updateUtility();
			}
		}
	}

	// ---------------------------------------------------------------------
	// IIntSource
	// ---------------------------------------------------------------------

	public enum IntegerVariables {
		ChangedJobs,
		IsEmployed
	}

	@Override
	public int getIntValue(Enum<?> variable) {
		return switch ((IntegerVariables) variable) {
			case ChangedJobs -> (flagChangedJobs) ? 1 : 0;
			case IsEmployed -> (job.getEmployer() != null) ? 1 : 0;
		};
	}

	// ---------------------------------------------------------------------
	// IDoubleSource
	// ---------------------------------------------------------------------

	public enum DoubleVariables {
		Age,
		Amenities,
		Count,
		Health,
		Utility,
		Wage
	}

	@Override
	public double getDoubleValue(Enum<?> variable) {
		return switch ((DoubleVariables) variable) {
			case Age -> age;
			case Amenities -> job.getAmenity();
			case Count -> 1.;
			case Health -> health;
			case Wage -> job.getWage();
			case Utility -> utility;
		};
	}


	// ---------------------------------------------------------------------
	// Own methods
	// ---------------------------------------------------------------------

	/**
	 * Methods related to basic characteristics below
	 */

	// Method to update lagged values at the beginning of each new year
	public void beginNewYear() {
		// Reset flag variables
		this.flagChangedJobs = false; // Reset at the beginning of the time period. Set to true if individual changes jobs in searchForJob() method.

		// Update lagged values
		this.health_L1 = health;
	}

	public void age() {
		age++;
	}

	// Method to calculate the level of health
	public void updateHealth() {
		health = health_L1 + model.getLambda() + job.getAmenity(); // Level of health = previous level of health + alpha * level of amenity in current job
	}

	public void updateUtility() {
		utility = calculateUtility();
	}

	public double calculateUtility() { // Utility is also referred to as well-being in the model
		return Parameters.evaluateUtilityFunction(health, job.getWage());
	}

	/**
	 * Methods related to employment below
	 */

	// Called when a person is hired by a firm to update employment-related information
	public void updateEmployment(Job job) {
		if (this.job.getEmployer() != null) { // Check if person is currently employed
			leaveJob(); // Make person leave their current job
		}
		job.getEmployer().hireEmployee(this, job); // Make employer associated with the selected job to hire the person (sets job field and adds person to the employer's set of employees).
	}

	// Method to allow person to search through the list of jobs and accept one. If on the job search is turned on, currently employed individuals can move to different jobs.
	public void searchForJob() {
		List<Job> sampledJobList;
		if (Parameters.SEARCH_INTENSITY) {
			sampledJobList = Helpers.pickNRandomJobs(model.getJobList(), searchIntensity); // Sample n = searchIntensity jobs from all available. This produces a list of jobs available to this person.
		} else {
			sampledJobList = model.getJobList();
		}
		if (sampledJobList.size() > 0) {
			Map<Job, Double> utilityOfSampledJobsMap = calculateUtilityOfListOfJobs(sampledJobList); // A map of job - utility combinations for jobs sampled in the previous step.
			Job selectedJob = findJobWithHighestUtility(utilityOfSampledJobsMap); // Choose the job providing maximum utility to the person, from the list of sampled jobs.
			if (job.getEmployer() != null && Parameters.ON_THE_JOB_SEARCH) {
				if (Parameters.evaluateUtilityFunction(health, selectedJob.getWage()) > Parameters.evaluateUtilityFunction(health, job.getWage())) { // Only change jobs if utility of the new job is higher than of the current job.
					updateEmployment(selectedJob); // Set person's job.
					model.getJobList().remove(selectedJob); //Remove accepted job offer from the list of available offers.
					setFlagChangedJobs(true); // Record the fact that employed individual changed jobs by setting flagChangedJobs to true.
				}
			} else {
				updateEmployment(selectedJob); // Set person's job
				model.getJobList().remove(selectedJob); //Remove accepted job offer from the list of available offers
			}

		}
	}

	public void leaveJob() {
		try {
			this.job.getEmployer().removeEmployee(this);
			removeJob();
		} catch (NullPointerException nullPointerException) {
			System.out.println("Null pointer exception. Individual " + this.getKey().getId() + " tried to leave a job, but was already unemployed." );
		}

	}

	public void removeJob() {
		Job noJob = new Job(null, 0., 0.);
		this.job = noJob;
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
	public int compareTo(Person p) {
		return (int) (this.getKey().getId() - p.getKey().getId());
	}

	@Override
	public int hashCode() {
		return getClass().hashCode();
	}
}