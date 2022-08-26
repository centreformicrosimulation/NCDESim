package NCDESim.algorithms;

import NCDESim.model.objects.Job;

import java.util.*;

public class Helpers {

    // Method picking n random values from original list of jobs
    public static List<Job> pickNRandom(List<Job> lst, int n) {
        List<Job> copy = new ArrayList<Job>(lst);
        Collections.shuffle(copy);
        return n > copy.size() ? copy.subList(0, copy.size()) : copy.subList(0, n);
    }

}
