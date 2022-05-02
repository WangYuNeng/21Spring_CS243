package submit;

import java.util.List;
import joeq.Class.jq_Class;
import joeq.Main.Helper;

public class Optimize {
    /*
     * optimizeFiles is a list of names of class that should be optimized
     * if nullCheckOnly is true, disable all optimizations except "remove redundant NULL_CHECKs."
     */
    public static void optimize(List<String> optimizeFiles, boolean nullCheckOnly) {
        
        // get an instance of the solver class.
        MySolver solver = new MySolver();

        // get an instance of the analysis class.
        NullChecked analysis = new NullChecked();
        solver.registerAnalysis(analysis);

        for (int i = 0; i < optimizeFiles.size(); i++) {
            jq_Class classes = (jq_Class)Helper.load(optimizeFiles.get(i));
            // Run your optimization on each classes.

            // register the analysis with the solver.
            Helper.runPass(classes, solver);
        }

        if (!nullCheckOnly) {
            // get an instance of the solver class.
            solver = new MySolver();

            // get an instance of the analysis class.
            Liveness analysis2 = new Liveness();
            solver.registerAnalysis(analysis2);

            for (int i = 0; i < optimizeFiles.size(); i++) {
                jq_Class classes = (jq_Class)Helper.load(optimizeFiles.get(i));
                // Run your optimization on each classes.

                // register the analysis with the solver.
                Helper.runPass(classes, solver);
            }
        }

    }
}
