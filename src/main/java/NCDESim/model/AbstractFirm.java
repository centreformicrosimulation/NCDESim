package NCDESim.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import javax.persistence.Transient;
import java.util.Set;
import java.util.TreeSet;

@Getter
@Setter
@ToString
@RequiredArgsConstructor
public abstract class AbstractFirm extends Agent {
    // This class defines all variables that all types of firms have in common

    @Transient
    Set<Person> employees; // List of employees of a firm

    // ---------------------------------------------------------------------
    // Constructor
    // --------------------------------------------------------------------

    public AbstractFirm(boolean newFirm) {
        super();
        // Define initial variables common to all types of firms
        this.employees = new TreeSet<Person>();
    }

    // ---------------------------------------------------------------------
    // Own methods
    // --------------------------------------------------------------------
    public void addEmployee(Person employee) {
        getEmployees().add(employee); // Add employee (Person) to a set of employees of a firm
    }

}
