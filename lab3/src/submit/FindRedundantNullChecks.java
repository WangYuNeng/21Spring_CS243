package submit;

import joeq.Class.jq_Class;
import joeq.Main.Helper;
import submit.NullChecked;
import submit.MySolver;

public class FindRedundantNullChecks {

    /*
     * args is an array of class names
     * method should print out a list of quad ids of redundant null checks
     * for each function as described on the course webpage
     */
    public static void main(String[] args) {
        //fill me in

        // get an instance of the solver class.
        MySolver solver = new MySolver();

        // get an instance of the analysis class.
        NullChecked analysis = new NullChecked();

        // get the classes we will be visiting.
        jq_Class[] classes = new jq_Class[args.length];
        for (int i=0; i < classes.length; i++)
            classes[i] = (jq_Class)Helper.load(args[i]);

        // register the analysis with the solver.
        solver.registerAnalysis(analysis);

        // visit each of the specified classes with the solver.
        for (int i=0; i < classes.length; i++) {
            Helper.runPass(classes[i], solver);
        }
    }
}
