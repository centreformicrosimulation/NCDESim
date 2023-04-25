package NCDESim.experiment;

import NCDESim.algorithms.ScatterplotTest;
import NCDESim.data.Parameters;
import NCDESim.model.AbstractFirm;
import NCDESim.model.NCDESimModel;
import NCDESim.model.Person;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import microsim.annotation.GUIparameter;
import microsim.engine.AbstractSimulationObserverManager;
import microsim.engine.SimulationCollectorManager;
import microsim.engine.SimulationManager;
import microsim.event.CommonEventType;
import microsim.event.EventGroup;
import microsim.event.EventListener;
import microsim.event.Order;
import microsim.gui.GuiUtils;
import microsim.gui.plot.HistogramSimulationPlotter;
import microsim.gui.plot.ScatterplotSimulationPlotter;
import microsim.gui.plot.TimeSeriesSimulationPlotter;
import microsim.statistics.CrossSection;
import microsim.statistics.IIntSource;
import microsim.statistics.functions.MeanArrayFunction;
import microsim.statistics.functions.MultiTraceFunction;
import microsim.statistics.functions.SumArrayFunction;
import org.apache.log4j.Logger;
import org.jfree.data.statistics.HistogramType;

import javax.swing.*;
import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@ToString
public class NCDESimObserver extends AbstractSimulationObserverManager implements EventListener {

	private final static Logger log = Logger.getLogger(NCDESimObserver.class);

	@GUIparameter(description = "Toggle to display charts in the GUI")
	private boolean showGraphs = true;

	private boolean showIndividualGraphs = true;

	@GUIparameter(description = "Set a regular time for any charts to update")
	private Double chartUpdatePeriod = 1.;

	Set<JInternalFrame> updateChartSet; // Charts added to this set will be refreshed automatically
	Set<JComponent> tabSet; // Charts are added to tabs in this set

	private TimeSeriesSimulationPlotter csAgePlotter, csHealthPlotter, csWagePlotter, csUtilityPlotter, csAmenitiesPlotter, averageAgePlotter,
			averageWagePlotter, averageEmployedPlotter, averageHealthPlotter, averageFirmAmenitiesPlotter, averageIndividualAmenitiesPlotter, averageUtilityPlotter, averageProfitPlotter,
			averageSizePlotter, populationPlotter, numberOfJobsPlotter, numberOfFirmsCreatedAndDestroyedPlotter;

	private ScatterplotSimulationPlotter scatterIndividualHealthUtility, scatterIndividualAmenityHealth, scatterIndividualHealthWages, scatterIndividualWagesAmenities, scatterIndividualWagesAmenitiesCS;
	private ScatterplotTest csScatterIndividualHealthUtility, csScatterIndividualAmenityHealth, csScatterIndividualHealthWages, csScatterIndividualWagesAmenities, csScatterIndividualWagesAmenitiesCS;

	private HistogramSimulationPlotter amenitiesHist, wagesHist, utilityHist, profitsHist, sizeHist, firmAgeHist;

	public NCDESimObserver(SimulationManager manager, SimulationCollectorManager collectorManager) {
		super(manager, collectorManager);
	}


	// ---------------------------------------------------------------------
	// EventListener
	// ---------------------------------------------------------------------

	public enum Processes {
//		Update;
	}
	public void onEvent(Enum<?> type) {
		switch ((Processes) type) {

//		case Update:
//
//			break;
		}
	}

	// ---------------------------------------------------------------------
	// Manager methods
	// ---------------------------------------------------------------------

	public void buildObjects() {

		final NCDESimModel model = ((NCDESimModel) getManager());

		showIndividualGraphs = (model.getIndividuals().size() <= Parameters.SHOW_INDIVIDUAL_GRAPHS_NUMBER_OBSERVATIONS);

		if(showGraphs) {

			updateChartSet = new LinkedHashSet<>(); // Set of all charts needed to be scheduled for updating
			tabSet = new LinkedHashSet<>();		//Set of all JInternalFrames each having a tab.  Each tab frame will potentially contain more than one chart.

			/*
			 * NUMBER OF INDIVIDUALS, FIRMS
			 */
			CrossSection.Double populationIndividualsCS = new CrossSection.Double(model.getIndividuals(), Person.DoubleVariables.Count);
			CrossSection.Double populationFirmsCS = new CrossSection.Double(model.getFirms(), AbstractFirm.Variables.Count);
			populationPlotter = new TimeSeriesSimulationPlotter("Number of individuals and firms", "Number");
			populationPlotter.addSeries("Individuals", new SumArrayFunction.Double(populationIndividualsCS));
			populationPlotter.addSeries("Firms", new SumArrayFunction.Double(populationFirmsCS));
			addChart(populationPlotter, "Population");

			/*
			 * FIRM CREATION AND DESTRUCTION RATES
			 */
			numberOfFirmsCreatedAndDestroyedPlotter = new TimeSeriesSimulationPlotter("Number of created and destroyed firms", "Number");
			numberOfFirmsCreatedAndDestroyedPlotter.addSeries("Firms created", (IIntSource) new MultiTraceFunction.Integer(model, NCDESimModel.IntVariables.NumberOfFirmsCreated));
			numberOfFirmsCreatedAndDestroyedPlotter.addSeries("Firms destroyed", (IIntSource) new MultiTraceFunction.Integer(model, NCDESimModel.IntVariables.NumberOfFirmsDestroyed));
			addChart(numberOfFirmsCreatedAndDestroyedPlotter, "Firm entry and exit");


			/*
			 * HISTOGRAM OF AGE
			 */
			CrossSection.Double populationIndividualAgeCS = new CrossSection.Double(model.getIndividuals(), Person.DoubleVariables.Age);
			HistogramSimulationPlotter populationAgeHistogram = new HistogramSimulationPlotter("Age histogram", "Age", HistogramType.FREQUENCY, 100);
			populationAgeHistogram.addCollectionSource("test", populationIndividualAgeCS);
			addChart(populationAgeHistogram, "HIST Age");


			/*
			 * AGE (INDIVIDUALS)
			 */
			csAgePlotter = new TimeSeriesSimulationPlotter("Agents' age", "Age");
			for(Person person : model.getIndividuals()) {
				csAgePlotter.addSeries("Person " + person.getKey().getId(), new MultiTraceFunction.Double(person, Person.DoubleVariables.Age));
			}

			CrossSection.Double ageCS = new CrossSection.Double(model.getIndividuals(), Person.DoubleVariables.Age);				//Obtain age values by IDoubleSource interface...
			averageAgePlotter = new TimeSeriesSimulationPlotter("Average age", "Age");
			averageAgePlotter.addSeries("Average", new MeanArrayFunction(ageCS));
			addChart(averageAgePlotter, "IND Avg. age");

			/*
			 * EMPLOYMENT (INDIVIDUALS)
			 */
			CrossSection.Integer employedCS = new CrossSection.Integer(model.getIndividuals(), Person.IntegerVariables.IsEmployed);
			CrossSection.Integer changedJobsCS = new CrossSection.Integer(model.getIndividuals(), Person.IntegerVariables.ChangedJobs);
			averageEmployedPlotter = new TimeSeriesSimulationPlotter("Employment statistics", "Share");
			averageEmployedPlotter.addSeries("Employed", new MeanArrayFunction(employedCS));
			averageEmployedPlotter.addSeries("Changed jobs", new MeanArrayFunction(changedJobsCS));
			addChart(averageEmployedPlotter, "IND Employment stats");

			/*
			NUMBER OF JOBS ADVERTISED
			 */
			CrossSection.Integer jobOffersCS = new CrossSection.Integer(model.getFirms(), AbstractFirm.IntegerVariables.JobsPosted);
			numberOfJobsPlotter = new TimeSeriesSimulationPlotter("Number of advertised jobs", "Jobs");
			numberOfJobsPlotter.addSeries("Jobs", new MeanArrayFunction(jobOffersCS));
			addChart(numberOfJobsPlotter, "MODEL Number of jobs");

			/*
			 * HEALTH (INDIVIDUALS)
			 */
			csHealthPlotter = new TimeSeriesSimulationPlotter("Agents' health", "Health");
			for(Person person : model.getIndividuals()) {
				csHealthPlotter.addSeries("Person " + person.getKey().getId(), new MultiTraceFunction.Double(person, Person.DoubleVariables.Health));
			}

			CrossSection.Double healthCS = new CrossSection.Double(model.getIndividuals(), Person.DoubleVariables.Health);
			averageHealthPlotter = new TimeSeriesSimulationPlotter("Average health", "Health");
			averageHealthPlotter.addSeries("Average", new MeanArrayFunction(healthCS));
			addChart(averageHealthPlotter, "IND Avg. health");

			/*
			 * HISTOGRAM OF UTILITY
			 */
			utilityHist = new HistogramSimulationPlotter("Health histogram", "Health", HistogramType.FREQUENCY, 100);
			utilityHist.addCollectionSource("Health", healthCS);
			addChart(utilityHist, "HIST Health");

			/*
			 * AMENITIES (INDIVIDUALS)
			 */
			csAmenitiesPlotter = new TimeSeriesSimulationPlotter("Agents' amenities", "Amenities");
			for (Person person : model.getIndividuals()) {
				csAmenitiesPlotter.addSeries("Person " + person.getKey().getId(), new MultiTraceFunction.Double(person, Person.DoubleVariables.Amenities));
			}

			CrossSection.Double amenitiesCS = new CrossSection.Double(model.getIndividuals(), Person.DoubleVariables.Amenities);
			averageIndividualAmenitiesPlotter = new TimeSeriesSimulationPlotter("Average amenities", "Amenities");
			averageIndividualAmenitiesPlotter.addSeries("Average", new MeanArrayFunction(amenitiesCS));
			addChart(averageIndividualAmenitiesPlotter, "IND Avg. amenities");

			/*
			 * HISTOGRAM OF AMENITIES
			 */
			amenitiesHist = new HistogramSimulationPlotter("Amenities histogram", "Amenities", HistogramType.FREQUENCY, 100);
			amenitiesHist.addCollectionSource("Amenities", amenitiesCS);
			addChart(amenitiesHist, "HIST Amenities");

			/*
			 * WAGE (INDIVIDUALS)
			 */
			csWagePlotter = new TimeSeriesSimulationPlotter("Agents' wage", "Wage");
			for(Person person : model.getIndividuals()) {
				csWagePlotter.addSeries("Person " + person.getKey().getId(), new MultiTraceFunction.Double(person, Person.DoubleVariables.Wage));
			}

			CrossSection.Double wageCS = new CrossSection.Double(model.getIndividuals(), Person.DoubleVariables.Wage);
			averageWagePlotter = new TimeSeriesSimulationPlotter("Average wage", "Wage");
			averageWagePlotter.addSeries("Average", new MeanArrayFunction(wageCS));
			addChart(averageWagePlotter, "IND Avg. wage");

			/*
			 * HISTOGRAM OF WAGES
			 */
			wagesHist = new HistogramSimulationPlotter("Wages histogram", "Wages", HistogramType.FREQUENCY, 100);
			wagesHist.addCollectionSource("Wages", wageCS);
			addChart(wagesHist, "HIST Wages");

			/*
			 * UTILITY (INDIVIDUALS)
			 */
			csUtilityPlotter = new TimeSeriesSimulationPlotter("Agents' utility", "Utility");
			for(Person person : model.getIndividuals()) {
				csUtilityPlotter.addSeries("Person " + person.getKey().getId(), new MultiTraceFunction.Double(person, Person.DoubleVariables.Utility));
			}

			CrossSection.Double utilityCS = new CrossSection.Double(model.getIndividuals(), Person.DoubleVariables.Utility);
			averageUtilityPlotter = new TimeSeriesSimulationPlotter("Average utility", "Utility");
			averageUtilityPlotter.addSeries("Average", new MeanArrayFunction(utilityCS));
			addChart(averageUtilityPlotter, "IND Avg. utility");

			/*
			 * HISTOGRAM OF UTILITY
			 */
			utilityHist = new HistogramSimulationPlotter("Utility histogram", "Utility", HistogramType.FREQUENCY, 100);
			utilityHist.addCollectionSource("Utility", utilityCS);
			addChart(utilityHist, "HIST Utility");

			/*
			 * AMENITIES (FIRM)
			 */
			CrossSection.Double amenitiesFirmCS = new CrossSection.Double(model.getFirms(), AbstractFirm.Variables.AmenitiesLevel);
			averageFirmAmenitiesPlotter = new TimeSeriesSimulationPlotter("Average amenities", "Amenities");
			averageFirmAmenitiesPlotter.addSeries("Average", new MeanArrayFunction(amenitiesFirmCS));
			addChart(averageFirmAmenitiesPlotter, "FIRM Avg. amenities");

			/*
			 * PROFITS (FIRM)
			 */
			CrossSection.Double profitCS = new CrossSection.Double(model.getFirms(), AbstractFirm.Variables.Profit);
			averageProfitPlotter = new TimeSeriesSimulationPlotter("Average profit", "Profit");
			averageProfitPlotter.addSeries("Average", new MeanArrayFunction(profitCS));
			addChart(averageProfitPlotter, "FIRM Avg. profit");

			/*
			 * HISTOGRAM OF FIRM PROFITS
			 */
			profitsHist = new HistogramSimulationPlotter("Profits histogram", "Profits", HistogramType.FREQUENCY, 100);
			profitsHist.addCollectionSource("Profits", profitCS);
			addChart(profitsHist, "HIST Profits");

			/*
			 * SIZE (FIRM)
			 */
			CrossSection.Double sizeCS = new CrossSection.Double(model.getFirms(), AbstractFirm.Variables.Size);
			averageSizePlotter = new TimeSeriesSimulationPlotter("Average size", "Size");
			averageSizePlotter.addSeries("Average", new MeanArrayFunction(sizeCS));
			addChart(averageSizePlotter, "FIRM Avg. size");

			/*
			 * HISTOGRAM OF FIRM SIZE
			 */
			sizeHist = new HistogramSimulationPlotter("Firm size histogram", "Size", HistogramType.FREQUENCY, 100);
			sizeHist.addCollectionSource("Size", sizeCS);
			addChart(sizeHist, "HIST Firm size");

			/*
			 * HISTOGRAM OF FIRM AGE
			 */
			CrossSection.Integer firmAgeCS = new CrossSection.Integer(model.getFirms(), AbstractFirm.IntegerVariables.Age);
			firmAgeHist = new HistogramSimulationPlotter("Firm age histogram", "Age", HistogramType.FREQUENCY, 100);
			firmAgeHist.addCollectionSource("Age", firmAgeCS);
			addChart(firmAgeHist, "HIST Firm age");

			// Add individual-level graphs
			if (showIndividualGraphs) {
				addChart(csAgePlotter, "IND Age");
				addChart(csHealthPlotter, "IND Health");
				addChart(csWagePlotter, "IND Wage");
				addChart(csUtilityPlotter, "IND Utility");
				addChart(csAmenitiesPlotter, "IND Amenities");
			}

			/*
			 * SCATTER PLOTS - CORRELATIONS BASED ON MEAN VALUES OVER TIME
			 */
			scatterIndividualHealthUtility = new ScatterplotSimulationPlotter("Health and utility", "Health", "Utility");
			scatterIndividualHealthUtility.addSeries("Health and utility", new MeanArrayFunction(healthCS), new MeanArrayFunction(utilityCS));
			addChart(scatterIndividualHealthUtility, "IND CORR Health / Utility");

			scatterIndividualAmenityHealth = new ScatterplotSimulationPlotter("Health and amenity", "Amenity", "Health");
			scatterIndividualAmenityHealth.addSeries("Health and amenity", new MeanArrayFunction(amenitiesCS), new MeanArrayFunction(healthCS));
			addChart(scatterIndividualAmenityHealth, "IND CORR Health / Amenity");

			scatterIndividualHealthWages = new ScatterplotSimulationPlotter("Health and wages", "Health", "Wages");
			scatterIndividualHealthWages.addSeries("Health and wages", new MeanArrayFunction(healthCS), new MeanArrayFunction(wageCS));
			addChart(scatterIndividualHealthWages, "IND CORR Health / Wages");

			scatterIndividualWagesAmenities = new ScatterplotSimulationPlotter("Wages and amenity", "Wages", "Amenity");
			scatterIndividualWagesAmenities.addSeries("Wages and amenity", new MeanArrayFunction(wageCS), new MeanArrayFunction(amenitiesCS));
			addChart(scatterIndividualWagesAmenities, "IND CORR Wages / Amenity");

			/*
			 * SCATTER PLOTS - CORRELATIONS BASED ON CROSS-SECTIONAL VALUES
			 */

			csScatterIndividualHealthUtility = new ScatterplotTest("Health and utility", "Health", "Utility");
			csScatterIndividualHealthUtility.addSeries("Health and utility", healthCS, utilityCS);
			addChart(csScatterIndividualHealthUtility, "IND CS CORR Health / Utility");

			csScatterIndividualAmenityHealth = new ScatterplotTest("Health and amenity", "Amenity", "Health");
			csScatterIndividualAmenityHealth.addSeries("Health and amenity", amenitiesCS, healthCS);
			addChart(csScatterIndividualAmenityHealth, "IND CS CORR Health / Amenity");

			csScatterIndividualHealthWages = new ScatterplotTest("Health and wages", "Health", "Wages");
			csScatterIndividualHealthWages.addSeries("Health and wages", healthCS, wageCS);
			addChart(csScatterIndividualHealthWages, "IND CS CORR Health / Wages");

			CrossSection.Double wagesFirmCS = new CrossSection.Double(model.getFirms(), AbstractFirm.Variables.Wages);
			csScatterIndividualWagesAmenities = new ScatterplotTest("Wages and amenity", "Wages", "Amenity");
			csScatterIndividualWagesAmenities.addSeries("Wages and amenity", amenitiesFirmCS, wagesFirmCS);
			addChart(csScatterIndividualWagesAmenities, "FIRM CS CORR Wages / Amenity");

			csScatterIndividualWagesAmenities = new ScatterplotTest("Profits and amenity", "Profit", "Amenity");
			csScatterIndividualWagesAmenities.addSeries("Profits and amenity", amenitiesFirmCS, profitCS);
			addChart(csScatterIndividualWagesAmenities, "FIRM CS CORR Profits / Amenity");


			//-------------------------------------------------------------------------------------------------------
			//
			//	BUILD A TABBED PANE HOLDING ALL THE CHARTS THAT ONLY UPDATE AT EACH TIME-STEP
			//
			//-------------------------------------------------------------------------------------------------------

			JInternalFrame chartsFrame = new JInternalFrame("Charts");
			JTabbedPane tabbedPane = new JTabbedPane();
			chartsFrame.add(tabbedPane);

			for(JComponent plot: tabSet) {
				tabbedPane.addTab(plot.getName(), plot);
			}
			tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
			chartsFrame.setResizable(true);
			chartsFrame.setMaximizable(true);
			GuiUtils.addWindow(chartsFrame, 321, 133, 800, 600);
		}

			log.debug("Observer objects created");
	}

	public void buildSchedule() {

		if(showGraphs) {
			EventGroup chartingEvents = new EventGroup();

			for(JInternalFrame plot: updateChartSet) {
				chartingEvents.addEvent(plot, CommonEventType.Update);
			}

			getEngine().getEventQueue().scheduleRepeat(chartingEvents, 0., Order.AFTER_ALL.getOrdering()-1, chartUpdatePeriod);
		}

		log.debug("Observer schedule created");

	}

	// ---------------------------------------------------------------------
	// Own methods
	// ---------------------------------------------------------------------

	private void addChart(JInternalFrame chartToAdd, String displayName) {
		chartToAdd.setName(displayName);
		updateChartSet.add(chartToAdd);
		tabSet.add(chartToAdd);
	}

	// ---------------------------------------------------------------------
	// Access methods are handled by Lombok
	// ---------------------------------------------------------------------

}
