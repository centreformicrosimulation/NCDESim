package NCDESim.model;

import lombok.*;
import microsim.data.db.PanelEntityKey;
import microsim.event.EventListener;
import microsim.statistics.IDoubleSource;
import jakarta.persistence.Entity;
import jakarta.persistence.Transient;
import jakarta.persistence.EmbeddedId;

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
        Update,
        Exit
    }

    public void onEvent(Enum<?> type) {
        switch ((Processes) type) {
            case PostJobOffers -> postJobOffers();
            case Update -> update();
        }
    }

    // ---------------------------------------------------------------------
    // Constructors
    // ---------------------------------------------------------------------
    public FirmTypeA(boolean newFirm) {
        super(true);
        //Extend the AbstractFirm constructor
    }

    public FirmTypeA(AbstractFirm originalFirm) {
        super(originalFirm);
    }

    public FirmTypeA(AbstractFirm originalFirm, boolean withNoise) {
        super(originalFirm, withNoise);
    }

    // ---------------------------------------------------------------------
    // Own methods
    // ---------------------------------------------------------------------

}
