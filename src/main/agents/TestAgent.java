package main.agents;

import java.util.HashMap;

import core.game.StateObservation;
import core.player.AbstractPlayer;
import main.qlearning.QLearning;
import main.qlearning.State;
import main.qlearning.States;
import ontology.Types.ACTIONS;
import tools.ElapsedCpuTimer;

public class TestAgent extends AbstractPlayer {

	private HashMap<State, ACTIONS> policy;
	
	public TestAgent(StateObservation so, ElapsedCpuTimer et) {
		// IT IS BORN
		policy = QLearning.loadPolicy();
		
		// DEBUG: Print the policy in use
		// System.out.println(policy.toString());
		
		// DEBUG: Print the frequency of the states during the training
		// for the current policy.
		// System.out.println(QLearning.loadQTable().frequency.toString());
	}
	
	@Override
	public ACTIONS act(StateObservation so, ElapsedCpuTimer et) {
		State currentState = States.checkState(so);
		ACTIONS action = policy.get(currentState);
		
		// DEBUG: Tell in which current state the agent is and
		// which action the policy says it has to choose
		// System.out.println(currentState + " ---> " + action);
		
		return action;
	}

}
