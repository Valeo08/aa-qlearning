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
	}
	
	@Override
	public ACTIONS act(StateObservation so, ElapsedCpuTimer et) {
		State currentState = States.checkState(so);
		
		System.out.println(policy.toString());
		System.out.println(currentState);
		
		return policy.get(currentState);
	}

}
