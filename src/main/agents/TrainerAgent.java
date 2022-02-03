package main.agents;

import java.io.File;

import core.game.StateObservation;
import core.player.AbstractPlayer;
import main.qlearning.QLearning;
import main.qlearning.State;
import main.qlearning.States;
import ontology.Types.ACTIONS;
import tools.ElapsedCpuTimer;

public class TrainerAgent extends AbstractPlayer {

	private QLearning learn;
	private State prevState;
	private ACTIONS prevAction;
	
	public TrainerAgent(StateObservation so, ElapsedCpuTimer et) {
		// IT IS BORN
		File f = new File(QLearning.QTABLE_FILENAME);
		if (f.exists()) learn = QLearning.loadQTable();
		else learn = new QLearning(so);
	}
	
	@Override
	public ACTIONS act(StateObservation so, ElapsedCpuTimer et) {	
		// THINK
		ACTIONS action = ACTIONS.ACTION_NIL;
		
		action = learn.calculateQ(so, prevState, prevAction);
		
		prevState = States.checkState(so);
		prevAction = action;
		
		// ACT
		return action;
	}
	
	@Override
	public void result(StateObservation so, ElapsedCpuTimer et) {
		// Learn about last action before game over
		learn.calculateQ(so, prevState, prevAction);
		
		// Save q-table for next iteration
		QLearning.saveQTable(learn);
		
		// Update the policy with the new knowledge
		if (QLearning.ACTUAL_ITERATION == QLearning.NUM_ITERATIONS - 1) {
			learn.printQ();
			
			// DEBUG: Print the frequency of the states the current policy.
			// System.out.println(learn.frequency.toString());
			
			learn.savePolicy();
		}
	}
	
}
