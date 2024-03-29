package NCDESim.model;

import NCDESim.model.objects.Job;
import jakarta.persistence.Transient;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import microsim.engine.SimulationEngine;
import microsim.event.EventListener;
import microsim.statistics.IDoubleSource;
import microsim.statistics.IIntSource;

import java.util.TreeSet;

@Getter
@Setter
@ToString
@RequiredArgsConstructor
public abstract class AbstractFirm extends Agent implements EventListener, IDoubleSource, IIntSource {
    // This class defines all variables that all types of firms have in common

    @Transient
    private TreeSet<Person> employeesSet; // Set of employees of a firm
    private double amenity; // Level of amenities provided by firm, between <0;1>
    private double costOfAmenity; // Cost of providing amenity per employee
    private double wage; // Randomly drawn hourly wage offered by a firm
    private double profit; // Sum of (productivity per worker - wage) - max(0, cost * amenity)
    private int desiredSize; // Size (number of employees) that firm wants to achieve.
    private int numberOfOffersToPost; // Number of jobs offers firm posts
    private int age; // Firm age. For how many periods firm has existed.


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
    // IIntSource
    // ---------------------------------------------------------------------

    public enum IntegerVariables {
        Age,
        JobsPosted,
    }

    @Override
    public int getIntValue(Enum<?> variable) {
        return switch ((AbstractFirm.IntegerVariables) variable) {
            case Age -> getAge();
            case JobsPosted -> numberOfOffersToPost;
        };
    }

    // ---------------------------------------------------------------------
    // IDoubleSource
    // ---------------------------------------------------------------------
    public enum Variables{
        Age,
        AmenitiesLevel,
        Count,
        JobsPosted,
        Profit,
        Size,
        Wages,
    }
    @Override
    public double getDoubleValue(Enum<?> variable) {
        return switch ((Variables) variable) {
            case Age -> getAge();
            case AmenitiesLevel -> getAmenity();
            case Count -> 1.;
            case JobsPosted -> numberOfOffersToPost;
            case Profit -> profit;
            case Size -> getEmployeesSet().size();
            case Wages -> wage;
        };
    }
    // ---------------------------------------------------------------------
    // Constructor
    // --------------------------------------------------------------------

    public AbstractFirm(boolean newFirm) {
        super();
        // Define initial variables common to all types of firms
        this.employeesSet = new TreeSet<>();
        this.amenity = SimulationEngine.getRnd().nextDouble() * 2 - 1;
        this.wage = SimulationEngine.getRnd().nextDouble();
        this.costOfAmenity = calculateCostOfAmenity();
        this.desiredSize = model.getFirmDesiredSize();
        this.age = 0;
    }

    // Constructor to clone firms. Note that firms' characteristics are cloned, but not relationships to employees etc.
    public AbstractFirm(AbstractFirm originalFirm) {
        super();
        this.employeesSet = new TreeSet<>(); // originalFirm is cloned, but cannot clone the employees
        this.amenity = originalFirm.amenity;
        this.wage = originalFirm.wage;
        this.costOfAmenity = originalFirm.costOfAmenity;
        this.desiredSize = originalFirm.desiredSize;
        this.age = 0;
    }

    public AbstractFirm(AbstractFirm originalFirm, boolean withNoise) {
        super();
        if (withNoise) {
            this.amenity = originalFirm.amenity * SimulationEngine.getRnd().nextDouble(1-model.getNoiseAmount(), 1+model.getNoiseAmount());
            this.wage = originalFirm.wage * SimulationEngine.getRnd().nextDouble(1-model.getNoiseAmount(), 1+model.getNoiseAmount());
            this.costOfAmenity = originalFirm.costOfAmenity * SimulationEngine.getRnd().nextDouble(1-model.getNoiseAmount(), 1+model.getNoiseAmount());;
        } else {
            this.amenity = originalFirm.amenity;
            this.wage = originalFirm.wage;
            this.costOfAmenity = originalFirm.costOfAmenity;
        }
        this.desiredSize = originalFirm.desiredSize;
        this.employeesSet = new TreeSet<>(); // originalFirm is cloned, but cannot clone the employees
        this.age = 0;
    }

    // ---------------------------------------------------------------------
    // Own methods
    // --------------------------------------------------------------------

    /**
     * Update method refreshes values of firm variables.
     */
    public void update() {
        age++; // Increment age by 1
        profit = calculateProfit();
    }

    public void hireEmployee(Person employee, Job job) {
        employee.setJob(job);
        getEmployeesSet().add(employee); // Add employee (Person) to a set of employees of a firm
    }

    public void removeEmployee(Person employee) {
        getEmployeesSet().remove(employee);
    }

    public void removeAllEmployees() {
        // Iterate through all employees instead of simply clearing the employees set because person values should be updated
        for (Person employee : employeesSet) {
            employee.removeJob();
        }
        getEmployeesSet().clear();
    }

    public double calculateProfit() {
        double profit = 0;
        double unitCostOfAmenity = amenity * costOfAmenity; // Note that this is per employee
        for (Person employee : employeesSet) {
            profit += employee.getProductivity() - employee.getJob().getWage() - unitCostOfAmenity;
        }
        return profit;
    }
    public double calculateCostOfAmenity() {
        if (model.amenityCostFloorAtZero) {
            return Math.max(0, amenity * model.getAmenityUnitCost()); // Calculate cost of providing the amenity with a floor at zero
        } else return amenity * model.getAmenityUnitCost(); // Calculate unrestricted cost of providing amenity. This implies that providing a dis-amenity increases firm's profit.
    }

    public void postJobOffers() {
        numberOfOffersToPost = desiredSize - employeesSet.size(); // Firms post job offers to reach their desired size
        for (int i = 0; i < numberOfOffersToPost; i++) {
            Job jobToPost = new Job(this, this.amenity, this.wage); // Create a job offer
            model.getJobList().add(jobToPost); // Add the job offer to the list of available offers
        }
    }

    public void prepareForRemoval() {
        removeAllEmployees();
    }

}
