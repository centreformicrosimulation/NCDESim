package NCDESim.data;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import microsim.event.Order;

@Getter
@Setter
@ToString
public class Parameters {

    // Scheduling
    public static final int MODEL_ORDERING = 0;
    public static final int COLLECTOR_ORDERING = Order.AFTER_ALL.getOrdering()-1;
    public static final int OBSERVER_ORDERING = Order.AFTER_ALL.getOrdering();

    // Graphs
    public static final int SHOW_INDIVIDUAL_GRAPHS_NUMBER_OBSERVATIONS = 100; // Individual-level graphs will be show if simulated number of observations is smaller or equal to this value

    //Values of parameters used by other simulated objects
    public static final double COST_OF_AMENITY_MULTIPLIER = 0.01; // Cost of providing amenity is per worker and depends on the level of amenity multiplied by this multiplier.
    public static final int MAXIMUM_NUMBER_OF_JOBS_SAMPLED_BY_PERSON = 10; // Search intensity. Defines the maximum number of jobs a person can sample.

    public static double evaluateUtilityFunction(double health, double wage) { // This utility function is used to calculate person's well-being. Job offers are evaluated according to the level of well-being they generate.
        double utility = wage + health;
        return utility;
    }

}
