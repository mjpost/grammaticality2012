package edu.jhu.coe.sequence.stationary;

import java.util.List;


public interface StationarySequenceModel {


	/**
	 * How many label states are there?
	 * @return
	 */
	public int getNumStates();
	/**
	 * What is the maximum seq. length
	 * @return
	 */
	public int getMaximumSequenceLength() ;

	/**
	 * For each state which states can follow it?
	 * @return
	 */
	public int[][] getAllowableForwardTransitions();
	/**
	 * For each state what can pre-cede it
	 * @return
	 */
	public int[][] getAllowableBackwardTransitions();
	
	/**
	 * The edge potentials don't depend on the individual instance 
	 * It's up to you to change them behind the scenes.   
	 */
	public double[][] getForwardEdgePotentials() ;
	
	public double[][] getBackwardEdgePotentials() ;
}
