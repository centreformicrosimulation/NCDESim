package NCDESim.data.filters;

import org.apache.commons.collections4.Predicate;
import NCDESim.model.AbstractFirm;


public class FirmRemovalFilter<T extends AbstractFirm> implements Predicate<T> {

    // Firms will be removed when the filter evaluates to true.
    @Override
    public boolean evaluate(T t) {
        return ((t.getEmployeesSet().size() == 0) || // Firms with no employees are removed
                (t.getProfit() <= -.5) // Firms with no profits are removed
        );
    }
}
