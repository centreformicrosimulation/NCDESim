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
}
