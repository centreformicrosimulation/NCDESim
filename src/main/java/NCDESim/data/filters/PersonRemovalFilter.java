package NCDESim.data.filters;

import NCDESim.data.Parameters;
import NCDESim.model.NCDESimModel;
import NCDESim.model.Person;
import microsim.engine.SimulationEngine;
import org.apache.commons.collections4.Predicate;


public class PersonRemovalFilter<T extends Person> implements Predicate<T> {

    // Individuals will be removed when the filter evaluates to true.
    @Override
    public boolean evaluate(T t) {
        return ((t.getAge() >= Parameters.PERSON_REMOVAL_AGE) // Individuals above given age are removed from the simulation
                | (t.getHealth() <= 0. && Parameters.ZERO_HEALTH_DEATH) // Individuals who get to 0 health are removed
        );
    }
}
