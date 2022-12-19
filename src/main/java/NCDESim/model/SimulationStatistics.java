package NCDESim.model;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import microsim.data.db.PanelEntityKey;

/**
 * SimulationStatistics defines variables storing aggregate statistics, which are output to a .csv file by the collector at the end of each year
 */

@Entity
@Getter
@Setter
@ToString
public class SimulationStatistics {

    @EmbeddedId
    private PanelEntityKey key;

    // Model parameters
    private int parameter_numberOfPersonsInitial, parameter_perYearNumberOfPersons, parameter_numberOfFirmsInitial, parameter_perYearNumberOfFirms;
    private double parameter_shareOfNewFirmsCloned, parameter_endTime, parameter_amenityCostMultiplier;

    // Aggregate model statistics
    private int outcome_numberOfPersons, outcome_numberOfFirms, outcome_numberOfAdvertisedJobs;
    private double outcome_employmentRate, outcome_jobChangingRate;

    // ---------------------------------------------------------------------
    // Constructor
    // ---------------------------------------------------------------------

    public SimulationStatistics() {
        key = new PanelEntityKey(0L);
    }

}
