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
	private Integer numberOfPersons = 100;
	private double healthDecay = 0.1;
	private double shareOfNewFirmsCloned = 0.9;
	private double amenityUnitCost = 0.01;
	private double lambda = 1.;
	private double personRemovalAge = 60;
	private double firmMinimumSize = 0.;
	private double firmMinimumProfit = 0.;
	private double cobbDouglasUtilityAlpha = 0.5;
	private double noiseAmount = 0.1;
	private boolean zeroHealthDeath = true;
	private boolean amenityCostFloorAtZero = false;
	private boolean destroyJobs = false;
	private boolean cloneWithNoise = true;

	// Define maximum values for the experiment (used in the last simulation)
	private static Integer numberOfRepeatedRuns = 1;		//Set default number of repeated runs
	private static Integer maxNumberOfAgents = 100000;		//Set default maximum number of agents

	//Set the absolute maximum number of runs when using the MultiRun GUI.  The series of simulations will stop when this
	//value is reached when using the MultiRun GUI.  Ensure that this is large enough to cover all necessary simulation runs
	//to prevent premature termination of experiment.
	//private static final Integer maxNumberOfRuns = (int) (numberOfRepeatedRuns * Math.log10(maxNumberOfAgents));
	private static final Integer maxNumberOfRuns = Integer.MAX_VALUE;

	private int parameterCombinationNumber = 0; // Counter to retrieve combinations of parameters
	static double[][] combinations = new double[24576][14];

	public static void main(String[] args) {

		batchModeArgumentParsing(args);		//Used to pass arguments to the main class via the command line if the user wants to run in 'batch mode' outside of Eclipse IDE

		SimulationEngine engine = SimulationEngine.getInstance();

		NCDESimMultiRun experimentBuilder = new NCDESimMultiRun();
		engine.setExperimentBuilder(experimentBuilder);
		engine.setup();

		prepareGrid();

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
		collector.setExportMicrodataToCSV(false); // Write microdata outputs to CSV
		collector.setExportAggregateStatisticsToCSV(true); // Write aggregate statistics output to CSV
		model.setInitialNumberOfPersons(numberOfPersons);
		model.setShareOfNewFirmsCloned(shareOfNewFirmsCloned);
		model.setHealthDecay(healthDecay);
		model.setEndTime(100.);
		model.setAmenityUnitCost(amenityUnitCost);
		model.setLambda(lambda);
		model.setPersonRemovalAge((int) personRemovalAge);
		model.setFirmMinimumSize((int) firmMinimumSize);
		model.setFirmMinimumProfit(firmMinimumProfit);
		model.setCobbDouglasAlpha(cobbDouglasUtilityAlpha);
		model.setNoiseAmount(noiseAmount);
		model.setZeroHealthDeath(zeroHealthDeath);
		model.setAmenityCostFloorAtZero(amenityCostFloorAtZero);
		model.setDestroyJobs(destroyJobs);
		model.setCloneWithNoise(cloneWithNoise);

	}

	public static void prepareGrid() {
		produceValuesToIterateOver();
		for (int i = 0; i < 100; i++) {
			System.out.println("Combination " + i + " is " + combinations[i][0] + " , " + combinations[i][1] + " , " + combinations[i][2] + " , " + combinations[i][3] + " , " + combinations[i][4]
					+ " , " + combinations[i][5] + " , " + combinations[i][6] + " , " + combinations[i][7] + " , " + combinations[i][8] + " , " + combinations[i][9] + " , " + combinations[i][10]
					+ " , " + combinations[i][11] + " , " + combinations[i][12] + " , " + combinations[i][13]);
		}
	}

	@Override
	public boolean nextModel() {
		// Update the values of the parameters for the next experiment
		counter++;
		// Increase the number of persons and reset parameters and counter
		if(counter > numberOfRepeatedRuns) {
			numberOfPersons = (int) combinations[parameterCombinationNumber][0];			// Increase the number of agents
			shareOfNewFirmsCloned = combinations[parameterCombinationNumber][1];	// Increase the share of cloned firms among new entrants
			healthDecay = combinations[parameterCombinationNumber][2]; // Increase the health decay parameter
			amenityUnitCost = combinations[parameterCombinationNumber][3];
			lambda = combinations[parameterCombinationNumber][4];
			personRemovalAge = combinations[parameterCombinationNumber][5];
			firmMinimumProfit = combinations[parameterCombinationNumber][6];
			firmMinimumSize = combinations[parameterCombinationNumber][7];
			cobbDouglasUtilityAlpha = combinations[parameterCombinationNumber][8];
			noiseAmount = combinations[parameterCombinationNumber][9];
			zeroHealthDeath = combinations[parameterCombinationNumber][10] == 1.;
			amenityCostFloorAtZero = combinations[parameterCombinationNumber][11] == 1.;
			destroyJobs = combinations[parameterCombinationNumber][12] == 1.;
			cloneWithNoise = combinations[parameterCombinationNumber][13] == 1.;

			counter = 1L;					// Reset counter
			parameterCombinationNumber++;
		}

	//	System.out.println("Evaluating combination " + parameterCombinationNumber + ". Number of persons " + numberOfPersons + " share of firms cloned " + shareOfNewFirmsCloned + "destroy jobs " + destroyJobs + " clone with noise " + cloneWithNoise);

		// Define the continuation condition
		//Stop when all combinations have been tested
		return parameterCombinationNumber < combinations.length;
	}

	/*
	This method produces combinations of parameters to pass to the nextModel() method
	 */
	private static void produceValuesToIterateOver() {
		int combination = 0;
		for (double a = 100; a <= 500; a+=400) { // Initial number of persons
			for (double b = 0.1; b <= 1; b+=0.5) { // Share of new firms cloned
				for (double c = 0.1; c <= 1; c+=0.5) { // Health decay
					for (double d = 0.01; d <= 1; d+=0.5) { // Amenity unit cost
						for (double e = 0.1; e <= 1; e+=0.5) { // Lambda (effect of productivity on health)
							for (double f = 20; f <= 100; f+=50) { // Person removal age
								for (double g = 0; g < 1; g+=0.5) { // Firm minimum profit
									for (double h = 1; h <= 100; h+=50) { // Firm minimum size
										for (double i = 0.1; i <= 1; i+=0.5) { // Cobb-Douglas utility alpha parameter
											for (double j = 0.1; j <= 1; j+=0.4) { // Noise amount
												for (double k = 0; k <= 1; k+=1) { // Zero health death boolean
													for (double l = 0; l <= 1; l+=1) { // Amenity cost floor at zero boolean
														for (double m = 0; m <= 1; m+=1) { // Destroy jobs boolean
															for (double n = 0; n <= 1; n+=1) { // Clone with noise boolean

																combinations[combination][0] = a;
																combinations[combination][1] = b;
																combinations[combination][2] = c;
																combinations[combination][3] = d;
																combinations[combination][4] = e;
																combinations[combination][5] = f;
																combinations[combination][6] = g;
																combinations[combination][7] = h;
																combinations[combination][8] = i;
																combinations[combination][9] = j;
																combinations[combination][10] = k;
																combinations[combination][11] = l;
																combinations[combination][12] = m;
																combinations[combination][13] = n;

																combination++;
															}
														}
													}
												}
											}
										}
									}
								}
							}
						}
					}
				}
			}
		}
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
