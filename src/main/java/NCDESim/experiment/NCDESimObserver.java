package NCDESim.experiment;

import microsim.annotation.GUIparameter;
import microsim.engine.AbstractSimulationObserverManager;
import microsim.engine.SimulationCollectorManager;
import microsim.engine.SimulationManager;
import microsim.event.CommonEventType;
import microsim.event.EventGroup;
import microsim.event.EventListener;
import microsim.event.Order;
import microsim.event.SingleTargetEvent;
import microsim.gui.GuiUtils;
import microsim.gui.plot.TimeSeriesSimulationPlotter;
import microsim.statistics.CrossSection;
import microsim.statistics.IDoubleSource;
import microsim.statistics.functions.MeanArrayFunction;
import microsim.statistics.functions.MultiTraceFunction;
import NCDESim.model.Agent;
import NCDESim.model.NCDESimModel;

import org.apache.log4j.Logger;

public class NCDESimObserver extends AbstractSimulationObserverManager implements EventListener {

	private final static Logger log = Logger.getLogger(NCDESimObserver.class);

	@GUIparameter(description = "Toggle to display charts in the GUI")
	private boolean showGraphs = true;

	@GUIparameter(description = "Set a regular time for any charts to update")
	private Double chartUpdatePeriod = 1.;

	private TimeSeriesSimulationPlotter csWealthPlotter, averagePlotter;

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

			final NCDESimModel model = (NCDESimModel) getManager();

			csWealthPlotter = new TimeSeriesSimulationPlotter("Agents' wealth", "wealth");
			for(Agent agent : model.getAgentsCreated()){
				csWealthPlotter.addSeries("Agent " + agent.getKey().getId(), (IDoubleSource) new MultiTraceFunction.Double(agent, Agent.Variables.Wealth));
			}
			GuiUtils.addWindow(csWealthPlotter, 50, 120, 700, 450);

//			CrossSection.Double wealthCS = new CrossSection.Double(model.getAgentsCreated(), Agent.Variables.Wealth);				//Obtain wealth values by IDoubleSource interface...
			CrossSection.Double wealthCS = new CrossSection.Double(model.getAgentsCreated(), Agent.class, "getWealth", true);		//... or obtain wealth values by Java reflection
			averagePlotter = new TimeSeriesSimulationPlotter("Average wealth", "wealth");
			averagePlotter.addSeries("Average", new MeanArrayFunction(wealthCS));
			GuiUtils.addWindow(averagePlotter, 750, 120, 700, 450);
		}

			log.debug("Observer objects created");
	}

	public void buildSchedule() {

		if(showGraphs) {
			EventGroup chartingEvents = new EventGroup();

			chartingEvents.addEvent(new SingleTargetEvent(csWealthPlotter, CommonEventType.Update));
			chartingEvents.addEvent(new SingleTargetEvent(averagePlotter, CommonEventType.Update));
			getEngine().getEventQueue().scheduleRepeat(chartingEvents, 0., Order.AFTER_ALL.getOrdering()-1, chartUpdatePeriod);
		}

		log.debug("Observer schedule created");

	}

	// ---------------------------------------------------------------------
	// Own methods
	// ---------------------------------------------------------------------



	// ---------------------------------------------------------------------
	// Access methods
	// ---------------------------------------------------------------------

	public boolean isShowGraphs() {
		return showGraphs;
	}

	public void setShowGraphs(boolean showGraphs) {
		this.showGraphs = showGraphs;
	}

	public Double getChartUpdatePeriod() {
		return chartUpdatePeriod;
	}

	public void setChartUpdatePeriod(Double chartUpdatePeriod) {
		this.chartUpdatePeriod = chartUpdatePeriod;
	}

}
