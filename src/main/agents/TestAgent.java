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
		System.out.println(policy.toString());
		
		/*
		int width = so.getWorldDimension().width;
		int height = so.getWorldDimension().height;
		double maxLimit = (width) * (height);
		System.out.println("\n\nBLOCK SIZE: " + so.getBlockSize() + "\n");
		System.out.println("WIDTH: " + width + "\n");
		System.out.println("HEIGHT: " + height + "\n");
		System.out.println("MAXLIMIT: " + maxLimit + "\n\n");
		
		System.out.println("\n\nAVAILABLE ACTIONS: " + so.getAvailableActions() + "\n\n");
		
		for (ArrayList<Observation> a : so.getNPCPositions(so.getAvatarPosition()))
			System.out.println(a);
		
		System.out.println("\n\n\nObservation Grid: \n");
		
		for (ArrayList<Observation>[] arr : so.getObservationGrid())
			for (ArrayList<Observation> a : arr)
				System.out.println(a);
		*/
	}
	
	@Override
	public ACTIONS act(StateObservation so, ElapsedCpuTimer et) {
		State currentState = States.checkState(so);
		ACTIONS action = policy.get(currentState);
		System.out.println(currentState + " ---> " + action);
		return action;
		
		/*
		System.out.println("Tick: " + so.getGameTick());
		System.out.println("Recompensa: " + Reward.getReward(so));
		for (ArrayList<Observation> a : so.getNPCPositions(so.getAvatarPosition()))
			System.out.println(a);
		System.out.println("-----------------------------------------------------------");
		
		return ACTIONS.ACTION_LEFT;
		*/
	}

}
