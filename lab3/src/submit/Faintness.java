package submit;

// some useful things to import. add any additional imports you need.
import java.util.*;
import joeq.Compiler.Quad.*;
import joeq.Compiler.Quad.Operand.RegisterOperand;
import flow.Flow;
import joeq.Main.Helper;

/**
 * Skeleton class for implementing a faint variable analysis
 * using the Flow.Analysis interface.
 */
public class Faintness implements Flow.Analysis {

    /**
     * Class for the dataflow objects in the Faintness analysis.
     * You are free to change this class or move it to another file.
     */
    public static class VarSet implements Flow.DataflowObject {
        private Set<String> set;
        public static Set<String> universalSet;
        public VarSet() { set = new TreeSet<String>(universalSet); }
        /**
         * Methods from the Flow.DataflowObject interface.
         * See Flow.java for the meaning of these methods.
         * These need to be filled in.
         */
        public void setToTop() { set = new TreeSet<String>(universalSet); }
        public void setToBottom() { set = new TreeSet<String>(); }

        public void meetWith(Flow.DataflowObject o) 
        {
            VarSet a = (VarSet)o;
            set.retainAll(a.set);
        }

        public void copy(Flow.DataflowObject o) 
        {
            VarSet a = (VarSet) o;
            set = new TreeSet<String>(a.set);
        }

        @Override
        public boolean equals(Object o) 
        {
            if (o instanceof VarSet) 
            {
                VarSet a = (VarSet) o;
                return set.equals(a.set);
            }
            return false;
        }

        /**
         * toString() method for the dataflow objects which is used
         * by postprocess() below.  The format of this method must
         * be of the form "[REG0, REG1, REG2, ...]", where each REG is
         * the identifier of a register, and the list of REGs must be sorted.
         * See src/test/TestFaintness.out for example output of the analysis.
         * The output format of your reaching definitions analysis must
         * match this exactly.
         */
        @Override
        public int hashCode() {
            return set.hashCode();
        }
        @Override
        public String toString() 
        {
            return set.toString();
        }

        public void genVar(String v) {set.add(v);}
        public void killVar(String v) {set.remove(v);}
        public boolean hasVar(String v) {return set.contains(v);}
    }

    /**
     * Dataflow objects for the interior and entry/exit points
     * of the CFG. in[ID] and out[ID] store the entry and exit
     * state for the input and output of the quad with identifier ID.
     *
     * You are free to modify these fields, just make sure to
     * preserve the data printed by postprocess(), which relies on these.
     */
    private VarSet[] in, out;
    private VarSet entry, exit;

    /**
     * This method initializes the datflow framework.
     *
     * @param cfg  The control flow graph we are going to process.
     */
    public void preprocess(ControlFlowGraph cfg) {
        // this line must come first.
        // System.out.println("Method: "+cfg.getMethod().getName().toString());

        // get the amount of space we need to allocate for the in/out arrays.
        QuadIterator qit = new QuadIterator(cfg);
        int max = 0;
        while (qit.hasNext()) {
            int id = qit.next().getID();
            if (id > max) max = id;
        }
        max += 1;

        // allocate the in and out arrays.
        in = new VarSet[max];
        out = new VarSet[max];

        // initialize the contents of in and out.
        qit = new QuadIterator(cfg);

        Set<String> s = new TreeSet<String>();
        VarSet.universalSet = s;

        /* Arguments are always there. */
        int numargs = cfg.getMethod().getParamTypes().length;
        for (int i = 0; i < numargs; i++) {
            s.add("R"+i);
        }

        while (qit.hasNext()) {
            Quad q = qit.next();
            int id = q.getID();
            in[id] = new VarSet();
            out[id] = new VarSet();
            for (RegisterOperand def : q.getDefinedRegisters()) {
                s.add(def.getRegister().toString());
            }
            for (RegisterOperand use : q.getUsedRegisters()) {
                s.add(use.getRegister().toString());
            }
        }

        // initialize the entry and exit points.
        transferfn.val = new VarSet();
        entry = new VarSet();
        exit = new VarSet();

        /************************************************
         * Your remaining initialization code goes here *
         ************************************************/
    }

    /**
     * This method is called after the fixpoint is reached.
     * It must print out the dataflow objects associated with
     * the entry, exit, and all interior points of the CFG.
     * Unless you modify in, out, entry, or exit you shouldn't
     * need to change this method.
     *
     * @param cfg  Unused.
     */
    public void postprocess (ControlFlowGraph cfg) {
        QuadIterator qit = new QuadIterator(cfg);
        TreeSet<Integer> redundantIdSet = new TreeSet<Integer>();
        while (qit.hasNext()) {
            Quad q = qit.next();
            int id = q.getID();

            if ((q.getOperator() instanceof Operator.Move || q.getOperator() instanceof Operator.Binary || q.getOperator() instanceof Operator.Unary) && !q.getOperator().hasSideEffects()) {
                boolean isFaint = true;
                for (RegisterOperand def : q.getDefinedRegisters()) {
                    String defReg = def.getRegister().toString();
                    if (!out[id].hasVar(defReg)) {
                        System.out.println(q.toString());
                        isFaint = false;
                        break;
                    }
                }
                if (isFaint) 
                    qit.remove();
            }
        }
        // System.out.println("entry: " + entry.toString());
        // for (int i=1; i<in.length; i++) {
        //     if (in[i] != null) {
        //         System.out.println(i + " in:  " + in[i].toString());
        //         System.out.println(i + " out: " + out[i].toString());
        //     }
        // }
        // System.out.println("exit: " + exit.toString());
    }

    /**
     * Other methods from the Flow.Analysis interface.
     * See Flow.java for the meaning of these methods.
     * These need to be filled in.
     */
    public boolean isForward() { return false; }

    /* Routines for interacting with dataflow values. */

    public Flow.DataflowObject getEntry() 
    { 
        Flow.DataflowObject result = newTempVar();
        result.copy(entry); 
        return result;
    }
    public Flow.DataflowObject getExit() 
    { 
        Flow.DataflowObject result = newTempVar();
        result.copy(exit); 
        return result;
    }
    public Flow.DataflowObject getIn(Quad q) 
    {
        Flow.DataflowObject result = newTempVar();
        result.copy(in[q.getID()]); 
        return result;
    }
    public Flow.DataflowObject getOut(Quad q) 
    {
        Flow.DataflowObject result = newTempVar();
        result.copy(out[q.getID()]); 
        return result;
    }
    public void setIn(Quad q, Flow.DataflowObject value) 
    { 
        in[q.getID()].copy(value); 
    }
    public void setOut(Quad q, Flow.DataflowObject value) 
    { 
        out[q.getID()].copy(value); 
    }
    public void setEntry(Flow.DataflowObject value) 
    { 
        entry.copy(value); 
    }
    public void setExit(Flow.DataflowObject value) 
    { 
        exit.copy(value); 
    }

    public Flow.DataflowObject newTempVar() { return new VarSet(); }

    /* Actually perform the transfer operation on the relevant
     * quad. */

    private TransferFunction transferfn = new TransferFunction ();
    public void processQuad(Quad q) {
        // system.out.println("Process Quad "+q.getID());
        transferfn.val.copy(out[q.getID()]);
        transferfn.visitQuad(q);
        in[q.getID()].copy(transferfn.val);
    }

    /* The QuadVisitor that actually does the computation */
    public static class TransferFunction extends QuadVisitor.EmptyVisitor {
        VarSet val;

        @Override
        public void visitQuad (Quad q) {

            if (q.getOperator() instanceof Operator.Move || q.getOperator() instanceof Operator.Binary || q.getOperator() instanceof Operator.Unary) {
                String dest;
                if (q.getOperator() instanceof Operator.Move) {
                    dest = Operator.Move.getDest(q).getRegister().toString();
                } else if (q.getOperator() instanceof Operator.Binary) {
                    dest = Operator.Binary.getDest(q).getRegister().toString();
                } else {
                    dest = Operator.Unary.getDest(q).getRegister().toString();
                }

                if (!val.hasVar(dest)) {
                    for (RegisterOperand use : q.getUsedRegisters()) {
                        val.killVar(use.getRegister().toString());
                    }
                }
                for (RegisterOperand def : q.getDefinedRegisters()) {
                    val.genVar(def.getRegister().toString());
                }
            } else {
                // for (RegisterOperand def : q.getDefinedRegisters()) {
                //     val.genVar(def.getRegister().toString());
                // }
                for (RegisterOperand use : q.getUsedRegisters()) {
                    val.killVar(use.getRegister().toString());
                }
            }
        }
    }
}
