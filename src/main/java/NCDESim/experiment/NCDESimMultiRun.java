package NCDESim.experiment;

import microsim.engine.SimulationEngine;
import microsim.engine.MultiRun;
import microsim.gui.shell.MultiRunFrame;

import NCDESim.model.NCDESimModel;

public class NCDESimMultiRun extends MultiRun {

	// Experimental design usually involves one of the following:
	// (a) Running the simulation a given number of times, without changing the values of the parameters (but changing the random number seed)
	// (b) Spanning over the values of the parameters, keeping the random number seed fixed
	// In the code below, the simulation is repeated a number of times equal to numberOfRepeatedRuns for each population size of agents, 	// specified by the parameter maxNumberOfAgents.

	public static boolean executeWithGui = true;

	// Define the parameters that specify the experiment, and assign an initial value (used in the first simulation)
	private Long counter = 1L;
	private Integer numberOfPersons = 10;

	private double shareOfNewFirmsCloned = 0;

	// Define maximum values for the experiment (used in the last simulation)
	private static Integer numberOfRepeatedRuns = 5;		//Set default number of repeated runs
	private static Integer maxNumberOfAgents = 100000;		//Set default maximum number of agents

	//Set the absolute maximum number of runs when using the MultiRun GUI.  The series of simulations will stop when this
	//value is reached when using the MultiRun GUI.  Ensure that this is large enough to cover all necessary simulation runs
	//to prevent premature termination of experiment.
	private static final Integer maxNumberOfRuns = (int) (numberOfRepeatedRuns * Math.log10(maxNumberOfAgents));


	public static void main(String[] args) {

		batchModeArgumentParsing(args);		//Used to pass arguments to the main class via the command line if the user wants to run in 'batch mode' outside of Eclipse IDE

		SimulationEngine engine = SimulationEngine.getInstance();

		NCDESimMultiRun experimentBuilder = new NCDESimMultiRun();
		engine.setExperimentBuilder(experimentBuilder);
		engine.setup();

		if (executeWithGui)
			new MultiRunFrame(experimentBuilder, "NCDESim MultiRun", maxNumberOfRuns).setResizable(true);
		else
			experimentBuilder.start();
	}

	@Override
	public void buildExperiment(SimulationEngine engine) {

		NCDESimModel model = new NCDESimModel();
		engine.addSimulationManager(model);

		NCDESimCollector collector = new NCDESimCollector(model);
		engine.addSimulationManager(collector);

		//No need to add observer if running in batch mode

		// Overwrite the default values of the parameters of the simulation
		collector.setExportToCSV(true); // Write outputs to CSV
		model.setInitialNumberOfPersons(numberOfPersons);
		model.setShareOfNewFirmsCloned(shareOfNewFirmsCloned);
	}

	@Override
	public boolean nextModel() {
		// Update the values of the parameters for the next experiment
		counter++;
		// Increase the number of persons and reset parameters and counter
		if(counter > numberOfRepeatedRuns) {
			numberOfPersons *= 10;			// Increase the number of agents by a factor of 10 for the next experiment
			shareOfNewFirmsCloned += 0.2;	// Increase the share of cloned firms among new entrants
			counter = 1L;					// Reset counter
		}

		// Define the continuation condition
		//Stop when the numberOfAgents goes above maxNumberOfAgents
		return numberOfPersons < maxNumberOfAgents;
	}

	@Override
	public String setupRunLabel() {
		return numberOfPersons.toString() + " persons, share of new firms cloned: " + shareOfNewFirmsCloned + " run count: " + counter.toString();
	}


	//MultiRun is designed for batch mode, so can overwrite default values by passing the values
	//to the main class as command line arguments
	//E.g. To specify the number of repeated runs to equal 10, add the following string in
	//the command line when executing the java program:
	// -n 10
	//if, for example, the model is to be run 10 times.
	public static void batchModeArgumentParsing(String[] args) {

		for (int i = 0; i < args.length; i++) {

			switch (args[i]) {
				case "-n" -> {            //Set the number of repeated runs in the experiment as a command line argument

					try {
						numberOfRepeatedRuns = Integer.parseInt(args[i + 1]);
					} catch (NumberFormatException e) {
						System.err.println("Argument " + args[i + 1] + " must be an integer reflecting the number of repeated simulations to run.");
						System.exit(1);
					}
					i++;
				}
				case "-a" -> {            //Set the maximum number of agents in the experiment as a command line argument

					try {
						maxNumberOfAgents = Integer.parseInt(args[i + 1]);
					} catch (NumberFormatException e) {
						System.err.println("Argument " + args[i + 1] + " must be an integer reflecting the maximum number of agents.");
						System.exit(1);
					}
					i++;
				}
				case "-g" -> {            //Toggle the MultiRun Gui on / off by passing the string '-g true' (on) or '-g false' (off) as a command line argument
					executeWithGui = Boolean.parseBoolean(args[i + 1]);
					i++;
				}
			}
		}
	}

}
