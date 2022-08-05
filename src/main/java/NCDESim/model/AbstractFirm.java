package NCDESim.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import microsim.engine.SimulationEngine;

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
    private double amenity; // Level of amenities provided by firm, between <-1;1>
    private double costOfAmenity; // Cost of providing amenity
    private double wage; // Randomly drawn hourly wage offered by a firm
    private double profit; // Sum of (productivity per worker - wage) - max(0, cost * amenity)


    // ---------------------------------------------------------------------
    // Constructor
    // --------------------------------------------------------------------

    public AbstractFirm(boolean newFirm) {
        super();
        // Define initial variables common to all types of firms
        this.employeesSet = new TreeSet<Person>();
        this.amenity = SimulationEngine.getRnd().nextDouble() * 2 - 1;
        this.wage = 2500 * SimulationEngine.getRnd().nextDouble();
    }

    // ---------------------------------------------------------------------
    // Own methods
    // --------------------------------------------------------------------
    public void addEmployee(Person employee) {
        getEmployeesSet().add(employee); // Add employee (Person) to a set of employees of a firm
    }

    public double calculateProfit() {
        double profit = 0;

        for (Person employee : employeesSet) {
            profit += employee.getProductivity() - employee.getWage();
        }

        profit += amenity*costOfAmenity;

        return profit;
    }

}
