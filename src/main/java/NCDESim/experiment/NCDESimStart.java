package NCDESim.experiment;

import microsim.engine.ExperimentBuilder;
import microsim.engine.SimulationEngine;
import microsim.gui.shell.MicrosimShell;
import NCDESim.model.NCDESimModel;
import NCDESim.experiment.NCDESimCollector;
import NCDESim.experiment.NCDESimObserver;

public class NCDESimStart implements ExperimentBuilder {

	public static void main(String[] args) {
		boolean showGui = true;

		SimulationEngine engine = SimulationEngine.getInstance();

		/* If turnOffDatabaseConnection is set to true, the simulation run will not be connected to an
		 * input or output database. This may speed up the building and execution of the simulation,
		* however the relational database management features provided by JAS-mine cannot then be used for
		* the duration of the simulation and data will not be persisted to the output database.  Any data
		* should be exported to CSV files instead.  If an attempt is made to import any data from an input
		* database during the simulation, an exception will be thrown.  */
		engine.setTurnOffDatabaseConnection(false);

		MicrosimShell gui = null;
		if (showGui) {
			gui = new MicrosimShell(engine);
			gui.setVisible(true);
		}

		NCDESimStart experimentBuilder = new NCDESimStart();
		engine.setExperimentBuilder(experimentBuilder);

		engine.setup();
	}

	public void buildExperiment(SimulationEngine engine) {
		NCDESimModel model = new NCDESimModel();
		NCDESimCollector collector = new NCDESimCollector(model);
		NCDESimObserver observer = new NCDESimObserver(model, collector);

		engine.addSimulationManager(model);
		engine.addSimulationManager(collector);
		engine.addSimulationManager(observer);	
	}
}
