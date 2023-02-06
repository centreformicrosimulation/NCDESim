package NCDESim.data.filters;

import org.apache.commons.collections4.Predicate;
import NCDESim.model.AbstractFirm;

/**
 * Class for removing firms based on certain criteria.
 *
 * @param <T> The type of firm to filter, must extend the AbstractFirm class.
 */
public class FirmRemovalFilter<T extends AbstractFirm> implements Predicate<T> {

    /**
     * Evaluates a firm to determine if it should be removed.
     * Firms will be removed if either of the following conditions is true:
     *  - The firm has no employees.
     *  - The firm's profit is less than or equal to zero.
     *
     * @param firm The firm to evaluate.
     * @return True if the firm should be removed, false otherwise.
     * @throws IllegalArgumentException If the firm argument is null.
     */
    @Override
    public boolean evaluate(T firm) {
        if (firm == null) {
            throw new IllegalArgumentException("The firm argument cannot be null.");
        }

        return ((firm.getEmployeesSet().size() == 0)  // Firms with no employees are removed
                || (firm.getProfit() <= 0) // Firms with no profits are removed
        );
    }
}

