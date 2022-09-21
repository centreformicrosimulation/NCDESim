package NCDESim.experiment;

import NCDESim.model.AbstractFirm;
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
import microsim.gui.plot.TimeSeriesSimulationPlotter;
import microsim.statistics.CrossSection;
import microsim.statistics.functions.MeanArrayFunction;
import microsim.statistics.functions.MultiTraceFunction;
import NCDESim.model.Person;
import NCDESim.model.NCDESimModel;

import microsim.statistics.functions.SumArrayFunction;
import org.apache.log4j.Logger;

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

	@GUIparameter(description = "Set a regular time for any charts to update")
	private Double chartUpdatePeriod = 1.;

	Set<JInternalFrame> updateChartSet; // Charts added to this set will be refreshed automatically
	Set<JComponent> tabSet; // Charts are added to tabs in this set

	private TimeSeriesSimulationPlotter csAgePlotter, csHealthPlotter, csWagePlotter, csUtilityPlotter, averageAgePlotter,
			averageWagePlotter, averageHealthPlotter, averageAmenitiesPlotter, averageUtilityPlotter, averageProfitPlotter,
			averageSizePlotter, populationPlotter;

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

		if(showGraphs) {

			updateChartSet = new LinkedHashSet<>(); // Set of all charts needed to be scheduled for updating
			tabSet = new LinkedHashSet<JComponent>();		//Set of all JInternalFrames each having a tab.  Each tab frame will potentially contain more than one cha
			final NCDESimModel model = (NCDESimModel) getManager();

			/**
			 * AGE
			 */
			csAgePlotter = new TimeSeriesSimulationPlotter("Agents' age", "Age");
			for(Person person : model.getIndividuals()){
				csAgePlotter.addSeries("Person " + person.getKey().getId(), new MultiTraceFunction.Double(person, Person.Variables.Age));
			}
			addChart(csAgePlotter, "Age");

			CrossSection.Double ageCS = new CrossSection.Double(model.getIndividuals(), Person.Variables.Age);				//Obtain age values by IDoubleSource interface...
			averageAgePlotter = new TimeSeriesSimulationPlotter("Average age", "Age");
			averageAgePlotter.addSeries("Average", new MeanArrayFunction(ageCS));
			addChart(averageAgePlotter, "Avg. age");

			/**
			 * HEALTH
			 */
			csHealthPlotter = new TimeSeriesSimulationPlotter("Agents' health", "Health");
			for(Person person : model.getIndividuals()){
				csHealthPlotter.addSeries("Person " + person.getKey().getId(), new MultiTraceFunction.Double(person, Person.Variables.Health));
			}
			addChart(csHealthPlotter, "Health");

			CrossSection.Double healthCS = new CrossSection.Double(model.getIndividuals(), Person.Variables.Health);
			averageHealthPlotter = new TimeSeriesSimulationPlotter("Average health", "Health");
			averageHealthPlotter.addSeries("Average", new MeanArrayFunction(healthCS));
			addChart(averageHealthPlotter, "Avg. health");

			/**
			 * WAGE
			 */
			csWagePlotter = new TimeSeriesSimulationPlotter("Agents' wage", "Wage");
			for(Person person : model.getIndividuals()){
				csWagePlotter.addSeries("Person " + person.getKey().getId(), new MultiTraceFunction.Double(person, Person.Variables.Wage));
			}
			addChart(csWagePlotter, "Wage");

			CrossSection.Double wageCS = new CrossSection.Double(model.getIndividuals(), Person.Variables.Wage);
			averageWagePlotter = new TimeSeriesSimulationPlotter("Average wage", "Wage");
			averageWagePlotter.addSeries("Average", new MeanArrayFunction(wageCS));
			addChart(averageWagePlotter, "Avg. wage");

			/**
			 * UTILITY
			 */
			csUtilityPlotter = new TimeSeriesSimulationPlotter("Agents' utility", "Utility");
			for(Person person : model.getIndividuals()){
				csUtilityPlotter.addSeries("Person " + person.getKey().getId(), new MultiTraceFunction.Double(person, Person.Variables.Utility));
			}
			addChart(csUtilityPlotter, "Utility");

			CrossSection.Double utilityCS = new CrossSection.Double(model.getIndividuals(), Person.Variables.Utility);
			averageUtilityPlotter = new TimeSeriesSimulationPlotter("Average utility", "Utility");
			averageUtilityPlotter.addSeries("Average", new MeanArrayFunction(utilityCS));
			addChart(averageUtilityPlotter, "Avg. utility");

			/**
			 * AMENITIES (FIRM)
			 */
			CrossSection.Double amenitiesCS = new CrossSection.Double(model.getFirms(), AbstractFirm.Variables.AmenitiesLevel);
			averageAmenitiesPlotter = new TimeSeriesSimulationPlotter("Average amenities", "Amenities");
			averageAmenitiesPlotter.addSeries("Average", new MeanArrayFunction(wageCS));
			addChart(averageAmenitiesPlotter, "Avg. amenities");

			/**
			 * PROFITS (FIRM)
			 */
			CrossSection.Double profitCS = new CrossSection.Double(model.getFirms(), AbstractFirm.Variables.Profit);
			averageProfitPlotter = new TimeSeriesSimulationPlotter("Average profit", "Profit");
			averageProfitPlotter.addSeries("Average", new MeanArrayFunction(profitCS));
			addChart(averageProfitPlotter, "Avg. profit");

			/**
			 * SIZE (FIRM)
			 */
			CrossSection.Double sizeCS = new CrossSection.Double(model.getFirms(), AbstractFirm.Variables.Size);
			averageSizePlotter = new TimeSeriesSimulationPlotter("Average size", "Size");
			averageSizePlotter.addSeries("Average", new MeanArrayFunction(sizeCS));
			addChart(averageSizePlotter, "Avg. size");

			/**
			 * NUMBER OF INDIVIDUALS AND FIRMS
			 */
			CrossSection.Double populationIndividualsCS = new CrossSection.Double(model.getIndividuals(), Person.Variables.Count);
			CrossSection.Double populationFirmsCS = new CrossSection.Double(model.getFirms(), AbstractFirm.Variables.Count);
			populationPlotter = new TimeSeriesSimulationPlotter("Number of individuals and firms", "Number");
			populationPlotter.addSeries("Individuals", new SumArrayFunction.Double(populationIndividualsCS));
			populationPlotter.addSeries("Firms", new SumArrayFunction.Double(populationFirmsCS));
			addChart(populationPlotter, "Population");

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
			GuiUtils.addWindow(chartsFrame, 300, 0, 1560, 660);
		}

			log.debug("Observer objects created");
	}

	public void buildSchedule() {

		if(showGraphs) {
			EventGroup chartingEvents = new EventGroup();

			for(JInternalFrame plot: updateChartSet) {
				chartingEvents.addEvent(plot, CommonEventType.Update);
			}
			/*
			chartingEvents.addEvent(new SingleTargetEvent(csAgePlotter, CommonEventType.Update));
			chartingEvents.addEvent(new SingleTargetEvent(csHealthPlotter, CommonEventType.Update));
			chartingEvents.addEvent(new SingleTargetEvent(csWagePlotter, CommonEventType.Update));
			chartingEvents.addEvent(new SingleTargetEvent(averagePlotter, CommonEventType.Update));
			 */
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
