package NCDESim.algorithms;

import NCDESim.model.AbstractFirm;
import NCDESim.model.objects.Job;

import java.util.*;

public class Helpers {

    // Method picking n random values from original list of jobs
    public static List<Job> pickNRandomJobs(List<Job> lst, int n) {
        List<Job> copy = new ArrayList<Job>(lst);
        Collections.shuffle(copy);
        return n > copy.size() ? copy.subList(0, copy.size()) : copy.subList(0, n);
    }

    public static List<AbstractFirm> pickNRandomFirms(List<AbstractFirm> lst, int n) {
        List<AbstractFirm> copy = new ArrayList<AbstractFirm>(lst);
        Collections.shuffle(copy);
        return n > copy.size() ? copy.subList(0, copy.size()) : copy.subList(0, n);
    }

    public static double findHighestProfitFromListOfFirms(List<AbstractFirm> lst) {
        double highestProfit = -Double.MAX_VALUE;
        for (AbstractFirm f : lst) {
            if (f.getProfit() > highestProfit) highestProfit = f.getProfit();
        }
        return highestProfit;
    }

    /**
     * Calculate asinh(a) (Inverse Hyperbolic Sine Transformation)
     * @param a
     * @return
     */
    public static double asinh(double a) {
        final double sign;
        // check the sign bit of the raw representation to handle -0
        if (Double.doubleToRawLongBits(a) < 0) {
            a = Math.abs(a);
            sign = -1.0d;
        } else {
            sign = 1.0d;
        }

        return sign * Math.log(Math.sqrt(a * a + 1.0d) + a);
    }
}
