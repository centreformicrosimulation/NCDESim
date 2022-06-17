package NCDESim.model;

import microsim.data.db.PanelEntityKey;
import microsim.event.EventListener;

import javax.persistence.EmbeddedId;
import javax.persistence.Transient;

public class FirmTypeA extends AbstractFirm implements EventListener {

    @EmbeddedId
    private PanelEntityKey key = new PanelEntityKey(idCounter++);

    @Transient
    private static long idCounter = 1;

    // ---------------------------------------------------------------------
    // EventListener
    // ---------------------------------------------------------------------

    public enum Processes {

    }

    public void onEvent(Enum<?> type) {
        switch ((Processes) type) {

        }
    }

    // ---------------------------------------------------------------------
    // Constructors
    // ---------------------------------------------------------------------
    public FirmTypeA(boolean newFirm) {
        super(true);
        //Extend the AbstractFirm constructor

    }


}
