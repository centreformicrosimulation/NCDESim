package NCDESim.model;

import lombok.*;
import microsim.data.db.PanelEntityKey;
import microsim.event.EventListener;
import microsim.statistics.IDoubleSource;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Transient;

@Entity
@Getter
@Setter
@ToString
@RequiredArgsConstructor
public class FirmTypeA extends AbstractFirm implements EventListener, IDoubleSource {

    @EmbeddedId
    private PanelEntityKey key = new PanelEntityKey(idCounter++);

    @Transient
    private static long idCounter = 1;


    // ---------------------------------------------------------------------
    // EventListener
    // ---------------------------------------------------------------------

    public enum Processes {
        PostJobOffers,
    }

    public void onEvent(Enum<?> type) {
        switch ((Processes) type) {
            case PostJobOffers:
                postJobOffers();
                break;
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
