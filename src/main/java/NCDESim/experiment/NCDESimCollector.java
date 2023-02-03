package NCDESim.experiment;

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
import org.apache.log4j.Logger;

import NCDESim.model.NCDESimModel;

@Getter
@Setter
@ToString
public class NCDESimCollector extends AbstractSimulationCollectorManager implements EventListener {

	private final static Logger log = Logger.getLogger(NCDESimCollector.class);

	@GUIparameter(description = "Toggle to export snapshot to .csv files")
	boolean exportToCSV = false;				//If true, data will be recorded to .csv files in the output directory

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
	private MeanArrayFunction employmentRateMAF, jobChangingRateMAF;
	private double outcome_employmentRate, outcome_jobChangingRate; // Employment rate in the model, calculated as share of individuals in employment among all individuals (employed and unemployed: there are two states in the model)

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

		exportIndividuals = new DataExport(model.getIndividuals(), exportToDatabase, exportToCSV);
		exportFirmsTypeA = new DataExport(model.getFirms(), exportToDatabase, exportToCSV);
		exportStatistics = new DataExport(statistics, exportToDatabase, exportToCSV);

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
	/*
	calculateStatistics() calculates values of more complicated aggregate statistics which cannot be obtained from other classes directly. They are then recorded in the .csv file through recordStatistics() and exportStatistics
	 */
	private void calculateStatistics() {

		// Employment rate
		employedCS = new CrossSection.Integer(model.getIndividuals(), Person.IntegerVariables.IsEmployed);
		employedCS.updateSource();
		employmentRateMAF = new MeanArrayFunction(employedCS);
		employmentRateMAF.applyFunction();
		outcome_employmentRate = employmentRateMAF.getDoubleValue(IDoubleSource.Variables.Default);

		// Job changing rate
		jobChangingCS = new CrossSection.Integer(model.getIndividuals(), Person.IntegerVariables.ChangedJobs);
		jobChangingCS.updateSource();
		jobChangingRateMAF = new MeanArrayFunction(jobChangingCS);
		jobChangingRateMAF.applyFunction();
		outcome_jobChangingRate = jobChangingRateMAF.getDoubleValue(IDoubleSource.Variables.Default);


	}


	/*
	recordStatistics() sets values of fields defined in SimulationStatistics class. These are then output to Statistics Excel file.
	 */
	private void recordStatistics() {
		// Model parameters
		statistics.setParameter_numberOfPersonsInitial(model.getInitialNumberOfPersons());
		statistics.setParameter_perYearNumberOfPersons(model.getPerYearNumberOfPersons());
		statistics.setParameter_numberOfFirmsInitial(model.getInitialNumberOfFirms());
		statistics.setParameter_shareOfNewFirmsCloned(model.getShareOfNewFirmsCloned());
		statistics.setParameter_endTime(model.getEndTime());
		statistics.setParameter_amenityCostMultiplier(model.getAmenityUnitCost());
		statistics.setParameter_healthDecayLambda(model.getHealthDecay());
		statistics.setParameter_onTheJobSearch(model.isOnTheJobSearch());
		statistics.setParameter_searchIntensityEmployed(model.getSearchIntensityEmployed());
		statistics.setParameterSearchIntensityUnemployed(model.getSearchIntensityUnemployed());

		// Model outcomes
		statistics.setOutcome_numberOfPersons(model.getIntValue(NCDESimModel.IntVariables.NumberOfPersons));
		statistics.setOutcome_numberOfFirms(model.getIntValue(NCDESimModel.IntVariables.NumberOfFirms));
		statistics.setOutcome_employmentRate(outcome_employmentRate); // This is first calculated by the collector and stored in outcome_employment_rate variable
		statistics.setOutcome_jobChangingRate(outcome_jobChangingRate); // Job changing rate is calculated by the collector, similarly to employment rate
	}


	// ---------------------------------------------------------------------
	// Access methods are handled by Lombok
	// ---------------------------------------------------------------------

}