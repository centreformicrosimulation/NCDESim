package NCDESim.experiment;

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

	@GUIparameter(description = "Set the time at which to start exporting snaphots to the database and/or .csv files")
	Double timeOfFirstSnapshot = 0.;

	@GUIparameter(description = "Set the time between snapshots to be exported to the database and/or .csv files")
	Double timeStepsBetweenSnapshots = 1.;

	//DataExport objects to handle exporting data to database and/or .csv files
	private DataExport exportIndividuals;
	private DataExport exportFirmsTypeA;

	//Other variables
	private NCDESimModel model;

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

		exportIndividuals = new DataExport(model.getIndividuals(), exportToDatabase, exportToCSV);
		exportFirmsTypeA = new DataExport(model.getFirms(), exportToDatabase, exportToCSV);

		log.debug("Collector objects created");	}

	public void buildSchedule() {

			EventGroup collectorEvents = new EventGroup();

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
		DumpFirms;
	}

	public void onEvent(Enum<?> type) {
		switch ((Processes) type) {

			case DumpIndividuals:
				exportIndividuals.export();

			case DumpFirms:
				exportFirmsTypeA.export();
		}


	}


	// ---------------------------------------------------------------------
	// Own methods
	// ---------------------------------------------------------------------



	// ---------------------------------------------------------------------
	// Access methods are handled by Lombok
	// ---------------------------------------------------------------------

}