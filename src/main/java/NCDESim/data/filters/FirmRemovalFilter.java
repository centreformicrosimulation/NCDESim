package NCDESim.data.filters;

import NCDESim.model.AbstractFirm;
import org.apache.commons.collections4.Predicate;

/**
 * Class for removing firms based on certain criteria.
 *
 * @param <T> The type of firm to filter, must extend the AbstractFirm class.
 */
public class FirmRemovalFilter<T extends AbstractFirm> implements Predicate<T> {

    private final int firmMinimumSize;
    private final double firmMinimumProfit;

    public FirmRemovalFilter(int firmMinimumSize, double firmMinimumProfit) {
        this.firmMinimumSize = firmMinimumSize;
        this.firmMinimumProfit = firmMinimumProfit;
    }

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

        return ((firm.getEmployeesSet().size() <= firmMinimumSize)  // Firms with no employees are removed
                || (firm.getProfit() <= firmMinimumProfit) // Firms with no profits are removed
        );
    }
}

