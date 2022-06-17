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

}
