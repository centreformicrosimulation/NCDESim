package NCDESim.data.filters;

import NCDESim.model.Person;
import org.apache.commons.collections4.Predicate;

public class IndividualCanLookForJobFilter<T extends Person> implements Predicate<T> {
    @Override
    public boolean evaluate(T t) {
        return (t.getJob().getEmployer() == null);
    }
}
