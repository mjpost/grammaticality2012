/**
 * 
 */
package edu.jhu.coe.PCFGLA;


import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.List;

import edu.jhu.coe.syntax.StateSet;
import edu.jhu.coe.syntax.Tree;
import edu.jhu.coe.syntax.Trees.PennTreeReader;
import edu.jhu.coe.util.Numberer;
import edu.jhu.coe.util.ScalingTools;

/**
 * @author petrov
 * Takes an unannotated tree a returns the log-likelihood of all derivations corresponding to the given tree.
 *
 */
public class TreeScorer{

	public static class Options {

		@Option(name = "-gr", required = true, usage = "Input File for Grammar (Required)\n")
		public String inFileName;

		@Option(name = "-inputFile", usage = "Read input from this file instead of reading it from STDIN.")
		public String inputFile;

		@Option(name = "-outputFile", usage = "Store output in this file instead of printing it to STDOUT.")
		public String outputFile;
	}
	
	public static void main(String[] args) {
		OptionParser optParser = new OptionParser(Options.class);
		Options opts = (Options) optParser.parse(args, true);
		// provide feedback on command-line arguments
		System.err.println("Calling with " + optParser.getPassedInOptions());

    
    String inFileName = opts.inFileName;
    if (inFileName==null) {
    	throw new Error("Did not provide a grammar.");
    }
    System.err.println("Loading grammar from "+inFileName+".");

    ParserData pData = ParserData.Load(inFileName);
    if (pData==null) {
      System.out.println("Failed to load grammar from file"+inFileName+".");
      System.exit(1);
    }
    Grammar grammar = pData.getGrammar();
    grammar.splitRules();
    SophisticatedLexicon lexicon = (SophisticatedLexicon)pData.getLexicon();
    ArrayParser parser = new ArrayParser(grammar, lexicon);
    
    Numberer.setNumberers(pData.getNumbs());
    Numberer tagNumberer =  Numberer.getGlobalNumberer("tags");
    

    short[] numSubstates = grammar.numSubStates;
    try{
    	InputStreamReader inputData = (opts.inputFile==null) ? new InputStreamReader(System.in) : new InputStreamReader(new FileInputStream(opts.inputFile), "UTF-8");
    	PennTreeReader treeReader = new PennTreeReader(inputData);
    	PrintWriter outputData = (opts.outputFile==null) ? new PrintWriter(new OutputStreamWriter(System.out)) : new PrintWriter(new OutputStreamWriter(new FileOutputStream(opts.outputFile), "UTF-8"), true);

    	Tree<String> tree = null;
    	while(treeReader.hasNext()){
    		tree = treeReader.next(); 
    		if (tree.getYield().get(0).equals("")){ // empty tree -> parse failure
    			outputData.write("()\n");
    			continue;
    		}
    		tree = TreeAnnotations.processTree(tree,pData.v_markov, pData.h_markov,pData.bin,false);
    		Tree<StateSet> stateSetTree = StateSetTreeList.stringTreeToStatesetTree(tree, numSubstates, false, tagNumberer);
    		allocate(stateSetTree);
    		parser.doInsideScores(stateSetTree, false, false, null);
    		double logScore = Math.log(stateSetTree.getLabel().getIScore(0)) + (stateSetTree.getLabel().getIScale()*ScalingTools.LOGSCALE);
    		outputData.write(logScore + "\n");
    		outputData.flush();
    	 }
    }catch (Exception ex) {
      ex.printStackTrace();
    }
    System.exit(0);
	}



	/*
   * Allocate the inside and outside score arrays for the whole tree
   */
  static void allocate(Tree<StateSet> tree) {
    tree.getLabel().allocate();
    for (Tree<StateSet> child : tree.getChildren()) {
      allocate(child);
    }
  }
	
}
