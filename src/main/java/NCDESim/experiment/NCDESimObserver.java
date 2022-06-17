package NCDESim.experiment;

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
import microsim.event.SingleTargetEvent;
import microsim.gui.GuiUtils;
import microsim.gui.plot.TimeSeriesSimulationPlotter;
import microsim.statistics.CrossSection;
import microsim.statistics.IDoubleSource;
import microsim.statistics.functions.MeanArrayFunction;
import microsim.statistics.functions.MultiTraceFunction;
import NCDESim.model.Person;
import NCDESim.model.NCDESimModel;

import org.apache.log4j.Logger;

@Getter
@Setter
@ToString
public class NCDESimObserver extends AbstractSimulationObserverManager implements EventListener {

	private final static Logger log = Logger.getLogger(NCDESimObserver.class);

	@GUIparameter(description = "Toggle to display charts in the GUI")
	private boolean showGraphs = true;

	@GUIparameter(description = "Set a regular time for any charts to update")
	private Double chartUpdatePeriod = 1.;

	private TimeSeriesSimulationPlotter csAgePlotter, averagePlotter;

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

			csAgePlotter = new TimeSeriesSimulationPlotter("Agents' age", "Age");
			for(Person person : model.getIndividuals()){
				csAgePlotter.addSeries("Person " + person.getKey().getId(), (IDoubleSource) new MultiTraceFunction.Double(person, Person.Variables.Age));
			}
			GuiUtils.addWindow(csAgePlotter, 50, 120, 700, 450);

			CrossSection.Double wealthCS = new CrossSection.Double(model.getIndividuals(), Person.Variables.Age);				//Obtain wealth values by IDoubleSource interface...
			averagePlotter = new TimeSeriesSimulationPlotter("Average age", "Age");
			averagePlotter.addSeries("Average", new MeanArrayFunction(wealthCS));
			GuiUtils.addWindow(averagePlotter, 750, 120, 700, 450);
		}

			log.debug("Observer objects created");
	}

	public void buildSchedule() {

		if(showGraphs) {
			EventGroup chartingEvents = new EventGroup();

			chartingEvents.addEvent(new SingleTargetEvent(csAgePlotter, CommonEventType.Update));
			chartingEvents.addEvent(new SingleTargetEvent(averagePlotter, CommonEventType.Update));
			getEngine().getEventQueue().scheduleRepeat(chartingEvents, 0., Order.AFTER_ALL.getOrdering()-1, chartUpdatePeriod);
		}

		log.debug("Observer schedule created");

	}

	// ---------------------------------------------------------------------
	// Own methods
	// ---------------------------------------------------------------------



	// ---------------------------------------------------------------------
	// Access methods are handled by Lombok
	// ---------------------------------------------------------------------

}
