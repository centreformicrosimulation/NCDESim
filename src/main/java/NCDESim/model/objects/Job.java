package NCDESim.model.objects;

import NCDESim.model.AbstractFirm;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Job implements Comparable<Job> {

    private AbstractFirm employer; //Firm which offers the job

    private double amenity; //Level of amenity

    private double wage; //Level of wage

    /*
    The comparator allows jobs to be compared one to another. This allows a list of jobs to be ordered.
    Jobs are ordered by the wages they offer. TODO: order jobs by well-being they offer?
     */
    @Override
    public int compareTo(Job j) {
        if (this.wage== j.wage)
            return 0;
        else if (this.wage>j.wage)
            return 1;
        else
            return -1;
    }
}
