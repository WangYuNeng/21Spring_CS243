package submit;

// some useful things to import. add any additional imports you need.
import joeq.Compiler.Quad.*;

import java.util.Iterator;

import flow.Flow;

/**
 * Skeleton class for implementing the Flow.Solver interface.
 */
public class MySolver implements Flow.Solver {

    protected Flow.Analysis analysis;

    /**
     * Sets the analysis.  When visitCFG is called, it will
     * perform this analysis on a given CFG.
     *
     * @param analyzer The analysis to run
     */
    public void registerAnalysis(Flow.Analysis analyzer) {
        this.analysis = analyzer;
    }

    /**
     * Runs the solver over a given control flow graph.  Prior
     * to calling this, an analysis must be registered using
     * registerAnalysis
     *
     * @param cfg The control flow graph to analyze.
     */
    public void visitCFG(ControlFlowGraph cfg) {

        // this needs to come first.
        analysis.preprocess(cfg);

        /***********************
         * Your code goes here *
         ***********************/
        Boolean hasUpdate = true;

        if (analysis.isForward()) {
            while (hasUpdate == true) {
                QuadIterator qit = new QuadIterator(cfg);
                hasUpdate = false;
                while (qit.hasNext()) {
                    Quad q = qit.next();
                    Flow.DataflowObject inOld = analysis.newTempVar(), meetObj = analysis.newTempVar();
                    meetObj.setToTop();
                    inOld.copy(analysis.getIn(q));
                    Iterator<Quad> predQit = qit.predecessors();

                    while (predQit.hasNext()) {
                        Quad predQ = predQit.next();
                        if (predQ == null) { // predecessor is entry
                            meetObj.meetWith(analysis.getEntry());
                        }
                        else {
                            meetObj.meetWith(analysis.getOut(predQ));
                        }
                    }
                    analysis.setIn(q, meetObj);
                    analysis.processQuad(q);

                    if (!analysis.getIn(q).equals(inOld)) {
                        hasUpdate = true;
                    }
                }
            }

            // Handle exit node
            Flow.DataflowObject meetObj = analysis.newTempVar();
            meetObj.setToTop();
            QuadIterator qit = new QuadIterator(cfg);
            while (qit.hasNext()) {
                Quad q = qit.next();
                
                Iterator<Quad> succQit = qit.successors();

                while (succQit.hasNext()) {
                    Quad succQ = succQit.next();
                    if (succQ == null) { // successor is exit
                        meetObj.meetWith(analysis.getOut(q));
                    }
                }
                analysis.setExit(meetObj);
            }
        }

        else {
            while (hasUpdate == true) {
                QuadIterator qit = new QuadIterator(cfg, false);
                hasUpdate = false;
                while (qit.hasPrevious()) {
                    Quad q = qit.previous();
                    Flow.DataflowObject outOld = analysis.newTempVar(), meetObj = analysis.newTempVar();
                    meetObj.setToTop();
                    outOld.copy(analysis.getOut(q));
                    Iterator<Quad> succQit = qit.successors();

                    while (succQit.hasNext()) {
                        Quad succQ = succQit.next();
                        if (succQ == null) { // successor is exit
                            meetObj.meetWith(analysis.getExit());
                        }
                        else {
                            meetObj.meetWith(analysis.getIn(succQ));
                        }
                    }
                    analysis.setOut(q, meetObj);
                    analysis.processQuad(q);

                    if (!analysis.getOut(q).equals(outOld)) {
                        hasUpdate = true;
                    }
                }
            }

            // Handle exit node
            Flow.DataflowObject meetObj = analysis.newTempVar();
            meetObj.setToTop();
            QuadIterator qit = new QuadIterator(cfg, false);
            while (qit.hasPrevious()) {
                Quad q = qit.previous();
                
                Iterator<Quad> predQit = qit.predecessors();

                while (predQit.hasNext()) {
                    Quad predQ = predQit.next();
                    if (predQ == null) { // predecessor is entry
                        meetObj.meetWith(analysis.getIn(q));
                    }
                }
                analysis.setEntry(meetObj);
            }
        }
        
        // this needs to come last.
        analysis.postprocess(cfg);
    }
}
