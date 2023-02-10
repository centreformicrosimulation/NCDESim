package NCDESim.data;

import NCDESim.data.enums.UtilityFunctions;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import microsim.event.Order;

@Getter
@Setter
@ToString
public class Parameters {

    public static final boolean ZERO_HEALTH_DEATH = true; // If true, individuals whose health equals zero will be removed from the simulation

    // Scheduling
    public static final int MODEL_ORDERING = 0;
    public static final int COLLECTOR_ORDERING = Order.AFTER_ALL.getOrdering()-1;
    public static final int OBSERVER_ORDERING = Order.AFTER_ALL.getOrdering();

    // Parameters
    public static final int PERSON_REMOVAL_AGE = 60; // Remove persons from the simulation when they reach this age
    public static final int PERSON_MAXIMUM_POTENTIAL_AGE = 80; // Maximum potential age, used in normalisation of health score
    public static final double FIRM_MINIMUM_PROFIT = 0.; // Remove firms with profits smaller or equal to this value
    public static final int FIRM_MINIMUM_SIZE = 0; // Remove firms with number of employees smaller or equal to this value
    public static final UtilityFunctions UTILITY_FUNCTION = UtilityFunctions.CobbDouglas;
    public static final double CobbDouglasUtilityTFP = 1; // Total Factor Productivity for the CB Utility
    public static final double CobbDouglasUtilityAlpha = 0.5; // Parameter Alpha for the CB Utility
    public static final double CobbDouglasUtilityBeta = 1 - CobbDouglasUtilityAlpha; // Parameter Beta for the CB Utility

    // Graphs
    public static final int SHOW_INDIVIDUAL_GRAPHS_NUMBER_OBSERVATIONS = 100; // Individual-level graphs will be show if simulated number of observations is smaller or equal to this value
    public static final boolean AMENITY_COST_FLOOR_AT_ZERO = false; // Set to true to restrict the firm's cost of providing amenity from the bottom at zero. If false, firms providing negative amenity (dis-amenity) increase their profits.
    public static final int MAXIMUM_NUMBER_OF_JOBS_SAMPLED_BY_PERSON = 10; // Search intensity. Defines the maximum number of jobs a person can sample.

    // Utility function and its parameters

    public static double evaluateUtilityFunction(double health, double wage) { // This utility function is used to calculate person's well-being. Job offers are evaluated according to the level of well-being they generate.

        switch (UTILITY_FUNCTION) {
            case CobbDouglas -> {
                return CobbDouglasUtilityTFP * Math.pow(health, CobbDouglasUtilityAlpha) * Math.pow(wage, CobbDouglasUtilityBeta);
            }
            default -> {
                return 0;
            }
        }
    }
}
