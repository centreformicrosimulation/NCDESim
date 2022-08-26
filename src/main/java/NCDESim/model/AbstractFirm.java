package NCDESim.model;

import NCDESim.data.Parameters;
import NCDESim.model.objects.Job;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import microsim.engine.SimulationEngine;
import microsim.event.EventListener;
import microsim.statistics.IDoubleSource;

import javax.persistence.Entity;
import javax.persistence.Transient;

import java.util.List;
import java.util.TreeSet;

@Getter
@Setter
@ToString
@RequiredArgsConstructor
public abstract class AbstractFirm extends Agent implements EventListener, IDoubleSource {
    // This class defines all variables that all types of firms have in common

    @Transient
    private TreeSet<Person> employeesSet; // Set of employees of a firm
    private double amenity; // Level of amenities provided by firm, between <-1;1>
    private double costOfAmenity; // Cost of providing amenity
    private double wage; // Randomly drawn hourly wage offered by a firm
    private double profit; // Sum of (productivity per worker - wage) - max(0, cost * amenity)
    private int desiredSize; // Size (number of employees) that firm wants to achieve.

    // ---------------------------------------------------------------------
    // EventListener
    // ---------------------------------------------------------------------

    public enum Processes {

    }

    public void onEvent(Enum<?> type) {
        switch ((Processes) type) {

        }
    }

    // ---------------------------------------------------------------------
    // IDoubleSource
    // ---------------------------------------------------------------------
    public enum Variables{

    }
    @Override
    public double getDoubleValue(Enum<?> anEnum) {
        return 0;
    }
    // ---------------------------------------------------------------------
    // Constructor
    // --------------------------------------------------------------------

    public AbstractFirm(boolean newFirm) {
        super();
        // Define initial variables common to all types of firms
        this.employeesSet = new TreeSet<Person>();
        this.amenity = SimulationEngine.getRnd().nextDouble() * 2 - 1;
        this.wage = 2500 * SimulationEngine.getRnd().nextDouble();
        this.costOfAmenity = calculateCostOfAmenity();
        this.desiredSize = 5;
    }

    // ---------------------------------------------------------------------
    // Own methods
    // --------------------------------------------------------------------
    public void hireEmployee(Person employee) {
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
    public double calculateCostOfAmenity() {
        return amenity* Parameters.COST_OF_AMENITY_MULTIPLIER; // Calculate unrestricted cost of providing amenity. This implies that providing a disamenity increases firm's profit.
    //    return Math.max(0, amenity* Parameters.COST_OF_AMENITY_MULTIPLIER); // Calculate cost of providing the amenity with a floor at zero
    }

    public void postJobOffers() {
        int numberOfOffersToPost = desiredSize - employeesSet.size(); // Firms post job offers to reach their desired size
        for (int i = 0; i < numberOfOffersToPost; i++) {
            Job jobToPost = new Job(this, this.amenity, this.wage); // Create a job offer
            model.getJobList().add(jobToPost); // Add the job offer to the list of available offers
        }
    }

}
