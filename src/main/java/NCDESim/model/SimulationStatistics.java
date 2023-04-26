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
    private double parameter_shareOfNewFirmsCloned, parameter_endTime, parameter_amenityCostMultiplier, parameter_healthDecayLambda, parameter_searchIntensityEmployed,
            parameterSearchIntensityUnemployed;
    private boolean parameter_onTheJobSearch, parameter_heterogenousSearchIntensity;

    // Aggregate model statistics
    private int outcome_numberOfPersons, outcome_numberOfFirms, outcome_numberOfAdvertisedJobs;
    private double outcome_employmentRate, outcome_jobChangingRate;
    private double outcome_person_age_mean, outcome_person_age_median, outcome_person_age_min, outcome_person_age_max, outcome_person_age_sd, outcome_person_age_kurtosis, outcome_person_age_skewness;
    private double outcome_person_health_mean, outcome_person_health_median, outcome_person_health_min, outcome_person_health_max, outcome_person_health_sd, outcome_person_health_kurtosis, outcome_person_health_skewness;
    private double outcome_person_productivity_mean, outcome_person_productivity_median, outcome_person_productivity_min, outcome_person_productivity_max, outcome_person_productivity_sd, outcome_person_productivity_kurtosis, outcome_person_productivity_skewness;
    private double outcome_person_utility_mean, outcome_person_utility_median, outcome_person_utility_min, outcome_person_utility_max, outcome_person_utility_sd, outcome_person_utility_kurtosis, outcome_person_utility_skewness;
    private double outcome_person_search_intensity_mean, outcome_person_search_intensity_median, outcome_person_search_intensity_min, outcome_person_search_intensity_max, outcome_person_search_intensity_sd, outcome_person_search_intensity_kurtosis, outcome_person_search_intensity_skewness;
    private double outcome_person_amenities_mean, outcome_person_amenities_median, outcome_person_amenities_min, outcome_person_amenities_max, outcome_person_amenities_sd, outcome_person_amenities_kurtosis, outcome_person_amenities_skewness;
    private double outcome_person_wage_mean, outcome_person_wage_median, outcome_person_wage_min, outcome_person_wage_max, outcome_person_wage_sd, outcome_person_wage_kurtosis, outcome_person_wage_skewness;

    // ---------------------------------------------------------------------
    // Constructor
    // ---------------------------------------------------------------------

    public SimulationStatistics() {
        key = new PanelEntityKey(0L);
    }

}
