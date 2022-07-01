package NCDESim.model;

import NCDESim.experiment.NCDESimCollector;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import microsim.engine.SimulationEngine;
import microsim.event.EventListener;

/**
 * Agent class contains fields common to all simulated objects:
 *  Person extends Agent
 *  AbstractFirm extends Agent
 *      FirmTypeA extends AbstractFirm (Multi-level inheritance)
 */

@Getter
@Setter
@ToString
public abstract class Agent implements EventListener {

    protected NCDESimModel model;
    protected NCDESimCollector collector;

    /*
    EventListener
     */

    public enum Processes {

    }

    public void onEvent(Enum<?> type) {

    }

    /*
    Constructors
     */

    public Agent() {
        this.model = (NCDESimModel) SimulationEngine.getInstance().getManager(NCDESimModel.class.getCanonicalName());
        this.collector = model.getCollector();
    }

    /*
    Destructor
     */

    public void kill() {
        this.model = null;
        this.collector = null;
    }

    /*
    Access methods are handled by Lombok
     */
}
