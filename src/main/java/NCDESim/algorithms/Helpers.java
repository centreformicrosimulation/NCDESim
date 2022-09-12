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
}
