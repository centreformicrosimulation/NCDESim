package NCDESim.experiment;

import NCDESim.model.AbstractFirm;
import NCDESim.model.NCDESimModel;
import NCDESim.model.Person;
import NCDESim.model.SimulationStatistics;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import microsim.annotation.GUIparameter;
import microsim.data.DataExport;
import microsim.engine.AbstractSimulationCollectorManager;
import microsim.engine.SimulationManager;
import microsim.event.EventGroup;
import microsim.event.EventListener;
import microsim.event.Order;
import microsim.statistics.CrossSection;
import microsim.statistics.IDoubleSource;
import microsim.statistics.functions.MeanArrayFunction;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.log4j.Logger;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@ToString
public class NCDESimCollector extends AbstractSimulationCollectorManager implements EventListener {

	private final static Logger log = Logger.getLogger(NCDESimCollector.class);

	@GUIparameter(description = "Toggle to export snapshot of microdata to .csv files")
	boolean exportMicrodataToCSV = false;				//If true, microdata will be recorded to .csv files in the output directory
	@GUIparameter(description = "Toggle to export snapshot of aggregate statistics to .csv files")
	boolean exportAggregateStatisticsToCSV = true;				//If true, aggregate data will be recorded to .csv files in the output directory
	@GUIparameter(description = "Toggle to export snapshot to output database")
	boolean exportToDatabase = false;		//If true, data will be recorded in the output database in the output directory
	@GUIparameter(description = "Set the time at which to start exporting snapshots to the database and/or .csv files")
	Double timeOfFirstSnapshot = 0.;
	@GUIparameter(description = "Set the time between snapshots to be exported to the database and/or .csv files")
	Double timeStepsBetweenSnapshots = 1.;

	// DataExport objects to handle exporting data to database and/or .csv files
	private DataExport exportIndividuals;
	private DataExport exportFirmsTypeA;
	private DataExport exportStatistics; // Exports aggregate statistics to .csv file

	private SimulationStatistics statistics; // Object which defines and stores aggregate statistics to be exported to a .csv file by the exportStatistics
	//Other variables
	private NCDESimModel model;

	// Variables defined below are more complicated aggregate statistics, which are first calculated by the collector and then recorded in the Statistics .csv file
	private CrossSection.Integer employedCS, jobChangingCS;
	private CrossSection.Double personAgeCS, personHealthCS, personProductivityCS, personUtilityCS, personAmenitiesCS, personWageCS;
	private CrossSection.Double firmAgeCS, firmJobsPostedCS, firmProfitCS, firmSizeCS;
	private MeanArrayFunction employmentRateMAF, jobChangingRateMAF;
	private double outcome_employmentRate, outcome_jobChangingRate; // Employment rate in the model, calculated as share of individuals in employment among all individuals (employed and unemployed: there are two states in the model)

	private double outcome_person_age_mean, outcome_person_age_median, outcome_person_age_min, outcome_person_age_max, outcome_person_age_sd, outcome_person_age_kurtosis, outcome_person_age_skewness;
	private double outcome_person_health_mean, outcome_person_health_median, outcome_person_health_min, outcome_person_health_max, outcome_person_health_sd, outcome_person_health_kurtosis, outcome_person_health_skewness;
	private double outcome_person_productivity_mean, outcome_person_productivity_median, outcome_person_productivity_min, outcome_person_productivity_max, outcome_person_productivity_sd, outcome_person_productivity_kurtosis, outcome_person_productivity_skewness;
	private double outcome_person_utility_mean, outcome_person_utility_median, outcome_person_utility_min, outcome_person_utility_max, outcome_person_utility_sd, outcome_person_utility_kurtosis, outcome_person_utility_skewness;
	private double outcome_person_amenity_mean, outcome_person_amenity_median, outcome_person_amenity_min, outcome_person_amenity_max, outcome_person_amenity_sd, outcome_person_amenity_kurtosis, outcome_person_amenity_skewness;
	private double outcome_person_wage_mean, outcome_person_wage_median, outcome_person_wage_min, outcome_person_wage_max, outcome_person_wage_sd, outcome_person_wage_kurtosis, outcome_person_wage_skewness;

	private double outcome_firm_age_mean, outcome_firm_age_median, outcome_firm_age_min, outcome_firm_age_max, outcome_firm_age_sd, outcome_firm_age_kurtosis, outcome_firm_age_skewness;
	private double outcome_firm_jobs_posted_mean, outcome_firm_jobs_posted_median, outcome_firm_jobs_posted_min, outcome_firm_jobs_posted_max, outcome_firm_jobs_posted_sd, outcome_firm_jobs_posted_kurtosis, outcome_firm_jobs_posted_skewness;
	private double outcome_firm_profit_mean, outcome_firm_profit_median, outcome_firm_profit_min, outcome_firm_profit_max, outcome_firm_profit_sd, outcome_firm_profit_kurtosis, outcome_firm_profit_skewness;
	private double outcome_firm_size_mean, outcome_firm_size_median, outcome_firm_size_min, outcome_firm_size_max, outcome_firm_size_sd, outcome_firm_size_kurtosis, outcome_firm_size_skewness;


	// ---------------------------------------------------------------------
	// Constructor
	// ---------------------------------------------------------------------

	public NCDESimCollector(SimulationManager manager) {
		super(manager);
	}

	// ---------------------------------------------------------------------
	// Manager methods
	// ---------------------------------------------------------------------

	public void buildObjects() {

		model = (NCDESimModel) getManager();

		statistics = new SimulationStatistics();

		exportIndividuals = new DataExport(model.getIndividuals(), exportToDatabase, exportMicrodataToCSV);
		exportFirmsTypeA = new DataExport(model.getFirms(), exportToDatabase, exportMicrodataToCSV);
		exportStatistics = new DataExport(statistics, exportToDatabase, exportAggregateStatisticsToCSV);

		log.debug("Collector objects created");	}

	public void buildSchedule() {

			EventGroup collectorEvents = new EventGroup();

			collectorEvents.addEvent(this, Processes.CalculateStatistics);
			collectorEvents.addEvent(this, Processes.RecordStatistics);
			collectorEvents.addEvent(this, Processes.DumpStatistics);
			collectorEvents.addEvent(this, Processes.DumpIndividuals);
			collectorEvents.addEvent(this, Processes.DumpFirms);


			getEngine().getEventQueue().scheduleRepeat(collectorEvents, timeOfFirstSnapshot, Order.AFTER_ALL.getOrdering()-1, timeStepsBetweenSnapshots);

		log.debug("Collector schedule created");
	}


	// ---------------------------------------------------------------------
	// EventListener
	// ---------------------------------------------------------------------

	public enum Processes {
		DumpIndividuals,
		DumpFirms,
		DumpStatistics,
		CalculateStatistics,
		RecordStatistics,
	}

	public void onEvent(Enum<?> type) {
		switch ((Processes) type) {

			case DumpIndividuals:
				exportIndividuals.export();
				break;

			case DumpFirms:
				exportFirmsTypeA.export();
				break;

			case DumpStatistics:
				try {
					exportStatistics.export();
				} catch (Exception e) {
					log.error(e.getMessage());
				}
				break;

			case CalculateStatistics:
				calculateStatistics();
				break;

			case RecordStatistics:
				recordStatistics();
				break;
		}


	}


	// ---------------------------------------------------------------------
	// Own methods
	// ---------------------------------------------------------------------

	/**
	calculateStatistics() calculates values of more complicated aggregate statistics which cannot be obtained from other classes directly. They are then recorded in the .csv file through recordStatistics() and exportStatistics
	 */
	private void calculateStatistics() {

		// Employment rate
		employedCS = new CrossSection.Integer(model.getIndividuals(), Person.IntegerVariables.IsEmployed);
		outcome_employmentRate = calculateRateIntCS(employedCS);

		// Job changing rate
		jobChangingCS = new CrossSection.Integer(model.getIndividuals(), Person.IntegerVariables.ChangedJobs);
		outcome_jobChangingRate = calculateRateIntCS(jobChangingCS);

		/*
		For distributional statistics below, this method prepares cross-sections with data. Statistics are calculated and stored in a single step in recordStatistics() method.
		 */

		// Individual age distribution
		personAgeCS = new CrossSection.Double(model.getIndividuals(), Person.DoubleVariables.Age);

		// Individual health distribution
		personHealthCS = new CrossSection.Double(model.getIndividuals(), Person.DoubleVariables.Health);

		// Individual productivity distribution
		personProductivityCS = new CrossSection.Double(model.getIndividuals(), Person.DoubleVariables.Productivity);

		// Individual utility distribution
		personUtilityCS = new CrossSection.Double(model.getIndividuals(), Person.DoubleVariables.Utility);

		// Individual job amenity distribution
		personAmenitiesCS = new CrossSection.Double(model.getIndividuals(), Person.DoubleVariables.Amenities);

		// Individual wage distribution
		personWageCS = new CrossSection.Double(model.getIndividuals(), Person.DoubleVariables.Wage);

		// Firm age distribution
		firmAgeCS = new CrossSection.Double(model.getFirms(), AbstractFirm.Variables.Age);

		// Firm jobs posted distribution
		firmJobsPostedCS = new CrossSection.Double(model.getFirms(), AbstractFirm.Variables.JobsPosted);

		// Firm profit distribution
		firmProfitCS = new CrossSection.Double(model.getFirms(), AbstractFirm.Variables.Profit);

		// Firm size distribution
		firmSizeCS = new CrossSection.Double(model.getFirms(), AbstractFirm.Variables.Size);

	}

	/**
	 * Calculates the statistics for the given CrossSection object and prefix.
	 *
	 * @param cs     the CrossSection object for which statistics are to be calculated
	 * @param prefix the prefix to use for the outcome variables
	 * @return a map of the calculated statistics, with keys being the variable names and values being the corresponding statistics
	 */
	private HashMap<String, Double> calculateDistributionalStats(CrossSection.Double cs, String prefix) {
		cs.updateSource();
		DescriptiveStatistics ds = new DescriptiveStatistics(cs.getDoubleArray());
		HashMap<String, Double> stats = new HashMap<>();
		stats.put(prefix + "_mean", ds.getMean());
		stats.put(prefix + "_median", ds.getPercentile(50));
		stats.put(prefix + "_min", ds.getMin());
		stats.put(prefix + "_max", ds.getMax());
		stats.put(prefix + "_sd", ds.getStandardDeviation());
		stats.put(prefix + "_kurtosis", ds.getKurtosis());
		stats.put(prefix + "_skewness", ds.getSkewness());
		return stats;
	}

	/**
	 * Sets the given statistics object with the calculated statistics for the given CrossSection object and prefix.
	 *
	 * @param statistics the statistics object to set with calculated statistics
	 * @param cs         the CrossSection object for which statistics are to be calculated
	 * @param prefix     the prefix to use for the outcome variables in the statistics object
	 */
	private void setDistributionalStats(SimulationStatistics statistics, CrossSection.Double cs, String prefix) {
		HashMap<String, Double> stats = calculateDistributionalStats(cs, prefix.toLowerCase());
		for (Map.Entry<String, Double> entry : stats.entrySet()) {
			try {
				Method[] methods = statistics.getClass().getMethods();
				for (Method method : methods) {
					if (method.getName().equalsIgnoreCase("set" + entry.getKey())) {
						method.invoke(statistics, entry.getValue());
						break;
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}


	/**
	 * This method calculates a rate (e.g. rate of employment) on the provided Integer Cross-section of data
	 * @param crossSectionOfData
	 * @return
	 */
	private double calculateRateIntCS(CrossSection.Integer crossSectionOfData) {
		crossSectionOfData.updateSource();
		MeanArrayFunction MAFonCS = new MeanArrayFunction(crossSectionOfData);
		MAFonCS.applyFunction();
		return MAFonCS.getDoubleValue(IDoubleSource.Variables.Default);
	}


	/**
	recordStatistics() sets values of fields defined in SimulationStatistics class. These are then output to Statistics Excel file.
	 */
	private void recordStatistics() {
		// Model parameters
		statistics.setParameter_numberOfPersonsInitial(model.getInitialNumberOfPersons());
		statistics.setParameter_perYearNumberOfPersons(model.getPerYearNumberOfPersons());
		statistics.setParameter_numberOfFirmsInitial(model.getInitialNumberOfFirms());
		statistics.setParameter_shareOfNewFirmsCloned(model.getShareOfNewFirmsCloned());
		statistics.setParameter_clone_firms_with_noise(model.isCloneWithNoise());
		statistics.setParameter_noise_amount(model.getNoiseAmount());
		statistics.setParameter_endTime(model.getEndTime());
		statistics.setParameter_amenityCostMultiplier(model.getAmenityUnitCost());
		statistics.setParameter_healthDecay(model.getHealthDecay());
		statistics.setParameter_onTheJobSearch(model.isOnTheJobSearch());
		statistics.setParameter_on_the_job_search_destroy_jobs(model.isDestroyJobs());
		statistics.setParameter_searchIntensityEmployed(model.getSearchIntensityEmployed());
		statistics.setParameter_SearchIntensityUnemployed(model.getSearchIntensityUnemployed());
		statistics.setParameter_desired_firm_size(model.getFirmDesiredSize());
		statistics.setParameter_person_removal_age(model.getPersonMaximumAge());
		statistics.setParameter_person_maximum_potential_age(model.getPersonMaximumPotentialAge());
		statistics.setParameter_firm_minimum_size(model.getFirmMinimumSize());
		statistics.setParameter_firm_minimum_profit(model.getFirmMinimumProfit());
		statistics.setParameter_utility_function(model.getUtilityFunction());
		statistics.setParameter_cobb_douglas_TFP(model.getCobbDouglasTFP());
		statistics.setParameter_cobb_douglas_alpha(model.getCobbDouglasAlpha());
		statistics.setParameter_amenity_cost_floor_at_zero(model.isAmenityCostFloorAtZero());
		statistics.setParameter_zero_health_death(model.isZeroHealthDeath());
		statistics.setParameter_lambda(model.getLambda());


		// Model outcomes
		statistics.setOutcome_numberOfPersons(model.getIntValue(NCDESimModel.IntVariables.NumberOfPersons));
		statistics.setOutcome_numberOfFirms(model.getIntValue(NCDESimModel.IntVariables.NumberOfFirms));
		statistics.setOutcome_employmentRate(outcome_employmentRate); // This is first calculated by the collector and stored in outcome_employment_rate variable
		statistics.setOutcome_jobChangingRate(outcome_jobChangingRate); // Job changing rate is calculated by the collector, similarly to employment rate

		// About distribution of individual age
		setDistributionalStats(statistics, personAgeCS, "outcome_person_age");

		// About distribution of individual health
		setDistributionalStats(statistics, personHealthCS, "outcome_person_health");

		// About distribution of individual productivity
		setDistributionalStats(statistics, personProductivityCS, "outcome_person_productivity");

		// About distribution of individual utility
		setDistributionalStats(statistics, personUtilityCS, "outcome_person_utility");

		// About distribution of individual job amenities
		setDistributionalStats(statistics, personAmenitiesCS, "outcome_person_amenities");

		// About distribution of individual wage
		setDistributionalStats(statistics, personWageCS, "outcome_person_wage");

		// About distribution of firm age
		setDistributionalStats(statistics, firmAgeCS, "outcome_firm_age");

		// About distribution of number of jobs posted by firms
		setDistributionalStats(statistics, firmJobsPostedCS, "outcome_firm_jobs_posted");

		// About distribution of firm profit
		setDistributionalStats(statistics, firmProfitCS, "outcome_firm_profit");

		// About distribution of firm size
		setDistributionalStats(statistics, firmSizeCS, "outcome_firm_size");
	}


	// ---------------------------------------------------------------------
	// Access methods are handled by Lombok
	// ---------------------------------------------------------------------

}