package NCDESim.model;

import NCDESim.algorithms.Helpers;
import NCDESim.model.objects.Job;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Transient;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import microsim.data.db.PanelEntityKey;
import microsim.engine.SimulationEngine;
import microsim.statistics.IDoubleSource;
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
	private double productivity, productivity_L1;
	private double utility;
	@Transient
	private Job job;
	private boolean flagChangedJobs;
	private double testVar1 = SimulationEngine.getRnd().nextDouble();
	private double testVar2 = Math.pow(testVar1,2);

	// ---------------------------------------------------------------------
	// Constructors and Initialization
	// ---------------------------------------------------------------------
	public Person() {
		super();

		this.key = new PanelEntityKey(idCounter++);
		this.age = SimulationEngine.getRnd().nextInt(20, 60); // Each person has a random age between 20 and 60
		this.health = SimulationEngine.getRnd().nextDouble(); // Each person has a random health level between 0 and 1
	//	this.productivity = SimulationEngine.getRnd().nextDouble(); // Each person has a random productivity between 0 and 1
		this.productivity = 1; // Homogenous productivity
		this.job = new Job(null, 0., 0.); // Job of the person

		// Initialise flag variables
		this.flagChangedJobs = false; // Indicates if individual who was employed changed jobs

		// Initialise lagged values
		this.health_L1 = health; // In the first period, lagged value of health is equal to the value of health
		this.productivity_L1 = productivity; // In the first period, lagged value of productivity is equal to the value of productivity
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
				updateProductivity();
				updateUtility();
			}
		}
	}

	// ---------------------------------------------------------------------
	// IIntSource
	// ---------------------------------------------------------------------

	public enum IntegerVariables {
		Age,
		ChangedJobs,
		IsEmployed,
	}

	@Override
	public int getIntValue(Enum<?> variable) {
		return switch ((IntegerVariables) variable) {
			case Age -> getAge();
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
		Productivity,
		Utility,
		Wage,
		TestVar1,
		TestVar2,
	}

	@Override
	public double getDoubleValue(Enum<?> variable) {
		return switch ((DoubleVariables) variable) {
			case Age -> age;
			case Amenities -> job.getAmenity();
			case Count -> 1.;
			case Health -> health;
			case Productivity -> productivity;
			case Wage -> job.getWage();
			case Utility -> utility;
			case TestVar1 -> testVar1;
			case TestVar2 -> testVar2;
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
		this.productivity_L1 = productivity;
	}

	public void age() {
		age++;
	}

	// Method to calculate the level of health
	public void updateHealth() {
	//	health = health_L1 - (model.getLambda() * age) + job.getAmenity(); // Level of health = previous level of health + alpha * level of amenity in current job
	//	double healthScore = Math.min((health_L1 - (Math.pow(1 + model.getLambda(), age) - 1) + job.getAmenity()), 1); // Health score

	//	double currentHealthScore = Math.min((health_L1 - (Math.pow(1 + model.getLambda(), age) - 1) + job.getAmenity()), 1);
	//	double maximumPotentialAgeHealthScore = Math.min((health_L1 - (Math.pow(1 + model.getLambda(), 80) - 1) + job.getAmenity()), 1);

		double maximumPotentialHealthDecay = (Math.pow(1 + model.getHealthDecay(), model.getPersonMaximumPotentialAge()) - 1); // Normalisation factor based on maximum possible health decay
		double currentHealthDecay = (Math.pow(1 + model.getHealthDecay(), age) - 1);
		double normalisedHealthDecay = currentHealthDecay/maximumPotentialHealthDecay;
		health = Math.min((health_L1 - normalisedHealthDecay + job.getAmenity()), 1); // Health score with normalised health decay
	}

	public void updateProductivity() {
		productivity = productivity_L1 * ((1 - model.getLambda() * Math.sqrt((1 - health))));
	}

	public void updateUtility() {
		utility = calculateUtility();
	}

	public double calculateUtility() { // Utility is also referred to as well-being in the model
		return model.evaluateUtilityFunction(health, job.getWage());
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
		if (job.getEmployer() != null) {
			sampledJobList = Helpers.pickNRandomJobs(model.getJobList(), model.getSearchIntensityEmployed()); // Sample n = searchIntensityEmployed jobs from all available. This produces a list of jobs available to this person.
		} else {
			sampledJobList = Helpers.pickNRandomJobs(model.getJobList(), model.getSearchIntensityUnemployed());
		}
		if (sampledJobList.size() > 0) {
			Map<Job, Double> utilityOfSampledJobsMap = calculateUtilityOfListOfJobs(sampledJobList); // A map of job - utility combinations for jobs sampled in the previous step.
			Job selectedJob = findJobWithHighestUtility(utilityOfSampledJobsMap); // Choose the job providing maximum utility to the person, from the list of sampled jobs.
			Job currentJob = this.job;
			if (job.getEmployer() != null && model.onTheJobSearch) {
				if (model.evaluateUtilityFunction(health, selectedJob.getWage()) > model.evaluateUtilityFunction(health, currentJob.getWage())) { // Only change jobs if utility of the new job is higher than of the current job.
					updateEmployment(selectedJob); // Set person's job.

					if (!model.destroyJobs) { // If destroyJobs parameter is set to false, the job that individual leaves is added to the list from which other individuals can sample jobs
						model.getJobList().add(currentJob);
					}

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
			double utility = model.evaluateUtilityFunction(health, wage);
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