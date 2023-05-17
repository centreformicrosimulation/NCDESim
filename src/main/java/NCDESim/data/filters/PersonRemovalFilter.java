package NCDESim.data.filters;

import NCDESim.model.Person;
import org.apache.commons.collections4.Predicate;


public class PersonRemovalFilter<T extends Person> implements Predicate<T> {

    private final int personRemovalAge;
    private final boolean zeroHealthDeath;

    public PersonRemovalFilter(int personRemovalAge, boolean zeroHealthDeath) {
        this.personRemovalAge = personRemovalAge;
        this.zeroHealthDeath = zeroHealthDeath;
    }

    // Individuals will be removed when the filter evaluates to true.
    @Override
    public boolean evaluate(T t) {

        return ((t.getAge() >= personRemovalAge) // Individuals above given age are removed from the simulation
                | (t.getHealth() <= 0. && zeroHealthDeath) // Individuals who get to 0 health are removed
        );
    }
}
