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

    //Values of parameters used by other simulated objects
    public static final double COST_OF_AMENITY_MULTIPLIER = 1000;
    public static final int MAXIMUM_NUMBER_OF_JOBS_SAMPLED_BY_PERSON = 10; // Search intensity. Defines the maximum number of jobs a person can sample.

}
