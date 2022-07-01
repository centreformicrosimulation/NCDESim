package NCDESim.model;

import NCDESim.model.objects.Amenity;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Transient;

import java.util.TreeSet;

@Getter
@Setter
@ToString
@RequiredArgsConstructor
public abstract class AbstractFirm extends Agent {
    // This class defines all variables that all types of firms have in common

    @Transient
    private TreeSet<Person> employeesSet; // Set of employees of a firm

    private TreeSet<Amenity> amenitiesSet; // Set of amenities of a firm

    // ---------------------------------------------------------------------
    // Constructor
    // --------------------------------------------------------------------

    public AbstractFirm(boolean newFirm) {
        super();
        // Define initial variables common to all types of firms
        this.employeesSet = new TreeSet<Person>();
        this.amenitiesSet = new TreeSet<Amenity>();
    }

    // ---------------------------------------------------------------------
    // Own methods
    // --------------------------------------------------------------------
    public void addEmployee(Person employee) {
        getEmployeesSet().add(employee); // Add employee (Person) to a set of employees of a firm
    }

    public void addAmenity(Amenity amenity) {
        getAmenitiesSet().add(amenity); // Add amenity to a set of amenities of a firm
    }

}
