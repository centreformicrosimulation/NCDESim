package NCDESim.model;

import NCDESim.data.enums.UtilityFunctions;
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
    private int parameter_numberOfPersonsInitial, parameter_perYearNumberOfPersons, parameter_numberOfFirmsInitial, parameter_perYearNumberOfFirms, parameter_person_removal_age, parameter_person_maximum_potential_age;
    private double parameter_shareOfNewFirmsCloned, parameter_endTime, parameter_amenityCostMultiplier, parameter_healthDecay, parameter_searchIntensityEmployed, parameter_lambda,
            parameter_SearchIntensityUnemployed, parameter_desired_firm_size, parameter_firm_minimum_size, parameter_firm_minimum_profit, parameter_cobb_douglas_TFP, parameter_cobb_douglas_alpha, parameter_noise_amount;
    private boolean parameter_onTheJobSearch, parameter_heterogenousSearchIntensity, parameter_clone_firms_with_noise, parameter_on_the_job_search_destroy_jobs, parameter_amenity_cost_floor_at_zero, parameter_zero_health_death;
    private UtilityFunctions parameter_utility_function;

    // Aggregate model statistics
    private int outcome_numberOfPersons, outcome_numberOfFirms, outcome_numberOfAdvertisedJobs;

    private double outcome_employmentRate, outcome_jobChangingRate;
    private double outcome_person_age_mean, outcome_person_age_median, outcome_person_age_min, outcome_person_age_max, outcome_person_age_sd, outcome_person_age_kurtosis, outcome_person_age_skewness;
    private double outcome_person_health_mean, outcome_person_health_median, outcome_person_health_min, outcome_person_health_max, outcome_person_health_sd, outcome_person_health_kurtosis, outcome_person_health_skewness;
    private double outcome_person_productivity_mean, outcome_person_productivity_median, outcome_person_productivity_min, outcome_person_productivity_max, outcome_person_productivity_sd, outcome_person_productivity_kurtosis, outcome_person_productivity_skewness;
    private double outcome_person_utility_mean, outcome_person_utility_median, outcome_person_utility_min, outcome_person_utility_max, outcome_person_utility_sd, outcome_person_utility_kurtosis, outcome_person_utility_skewness;
    private double outcome_person_amenities_mean, outcome_person_amenities_median, outcome_person_amenities_min, outcome_person_amenities_max, outcome_person_amenities_sd, outcome_person_amenities_kurtosis, outcome_person_amenities_skewness;
    private double outcome_person_wage_mean, outcome_person_wage_median, outcome_person_wage_min, outcome_person_wage_max, outcome_person_wage_sd, outcome_person_wage_kurtosis, outcome_person_wage_skewness;

    private double outcome_firm_age_mean, outcome_firm_age_median, outcome_firm_age_min, outcome_firm_age_max, outcome_firm_age_sd, outcome_firm_age_kurtosis, outcome_firm_age_skewness;
    private double outcome_firm_jobs_posted_mean, outcome_firm_jobs_posted_median, outcome_firm_jobs_posted_min, outcome_firm_jobs_posted_max, outcome_firm_jobs_posted_sd, outcome_firm_jobs_posted_kurtosis, outcome_firm_jobs_posted_skewness;
    private double outcome_firm_profit_mean, outcome_firm_profit_median, outcome_firm_profit_min, outcome_firm_profit_max, outcome_firm_profit_sd, outcome_firm_profit_kurtosis, outcome_firm_profit_skewness;
    private double outcome_firm_size_mean, outcome_firm_size_median, outcome_firm_size_min, outcome_firm_size_max, outcome_firm_size_sd, outcome_firm_size_kurtosis, outcome_firm_size_skewness;

    // ---------------------------------------------------------------------
    // Constructor
    // ---------------------------------------------------------------------

    public SimulationStatistics() {
        key = new PanelEntityKey(0L);
    }

}
