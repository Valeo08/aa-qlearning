package main.qlearning;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import core.game.StateObservation;
import ontology.Types.ACTIONS;

public class QLearning implements Serializable {
	
	// Implements serializable
	private static final long serialVersionUID = 8903450369057477365L;
	
	// File name of Q-table data and generated policy of it
	public final static String QTABLE_FILENAME = "qtable-george.dat";
	public final static String QPOLICY_FILENAME = "qpolicy-george.dat";
	
	public static double NUM_ITERATIONS;
	public static double ACTUAL_ITERATION;
	
	// Learning and eagerness default rates
	public static final double DEFAULT_ALPHA = 0.1;
	public static final double DEFAULT_GAMMA = 0.9;
	
	// Learning rate
	private final double alpha;
	// Eagerness - 0 looks in the near future, 1 looks in the distant future
	private final double gamma;
	
	// Q table
	public HashMap<State, Integer> frequency;
	private final ArrayList<ACTIONS> actions;
	private HashMap<State, HashMap<ACTIONS, Double>> qtable;
	
	// Simple constructor
	public QLearning(StateObservation so) {
		this(so, DEFAULT_ALPHA, DEFAULT_GAMMA);
	}
	
	// Constructor with alpha and gamma parameters
	public QLearning(StateObservation so, double alpha, final double gamma) {
		this.alpha = alpha;
		this.gamma = gamma;
		
		this.frequency = new HashMap<>();
		for (int i = 0; i < States.NUM_STATES; i++)
			this.frequency.put(States.getStateFromIndex(i), 0);
		
		this.actions = so.getAvailableActions(false);
		
		qtable = new HashMap<>();  
		initQ(so);
	}
	
	private void initQ(StateObservation so) {
		Random r = new Random(System.nanoTime());
		for (int i = 0; i < States.NUM_STATES; i++) {
			qtable.put(States.getStateFromIndex(i), new HashMap<>());
			for (ACTIONS action : so.getAvailableActions(false))
				qtable.get(States.getStateFromIndex(i))
					.put(action, r.nextDouble());
		}
		
		for (ACTIONS action : so.getAvailableActions(false))
			qtable.get(States.getStateFromIndex(States.NUM_STATES - 1))
				.replace(action, 0.0);
	}
	
	public ACTIONS calculateQ(StateObservation so, State prevState, ACTIONS prevAction) {
		Random random = new Random(System.nanoTime());

		State currentState = States.checkState(so);
		
		int prevFreq = this.frequency.get(currentState);
		this.frequency.replace(currentState, prevFreq + 1);
		
		if (prevState != null && prevAction != null) {
			double qa = qtable.get(prevState).get(prevAction);
			double qmax = maxQ(currentState);
			double r = Reward.getReward(so);
			double q = qa + alpha * (r + gamma * qmax - qa);
			
			qtable.get(prevState).replace(prevAction, q);
		}
		
		int randomAction = random.nextInt(actions.size());
		double epsilon = (NUM_ITERATIONS - ACTUAL_ITERATION) / NUM_ITERATIONS;
		double prob = random.nextDouble();
		double greedy = (1.0 - epsilon);
		
		ACTIONS action;
		if (greedy > prob) action = getBestAction(currentState);
		else action = this.actions.get(randomAction);
		
		return action;
	}
	
	private double maxQ(final State state) {
		double max = -Double.MAX_VALUE;
		
		for (ACTIONS action : actions)
			if (qtable.get(state).get(action) > max)
				max = qtable.get(state).get(action);
		
		return max;
	}
	
	private ACTIONS getBestAction(final State state) {
		double max = -Double.MAX_VALUE;
		ACTIONS best = null;
		
		for (ACTIONS action : actions)
			if (qtable.get(state).get(action) > max) {
				max = qtable.get(state).get(action);
				best = action;
			}		
		
		return best;
	}
	
	public void printQ() {
		StringBuilder sb = new StringBuilder();
		
		for (int i = 0; i < States.NUM_STATES; i++) {
			sb.append(States.getStateFromIndex(i).toString()).append("\n");
			
			for (ACTIONS action : actions) {
				sb.append("Action: ").append(action.toString()).append(" --> ");
				sb.append(qtable.get(States.getStateFromIndex(i)).get(action));
				sb.append("\n");
			}
		}
		
		System.out.println(sb.toString());
	}
	
	public static void saveQTable(QLearning q) {
		try {
			File f = new File(QTABLE_FILENAME);
			if (f.exists()) f.delete();
			FileOutputStream fos = new FileOutputStream(f);
			ObjectOutputStream out = new ObjectOutputStream(fos);
			out.writeObject(q);
			out.close();
			fos.close();
		} catch (IOException e) {
			System.err.println("Error when saving qtable data!");
		}
	}
	
	public static QLearning loadQTable() {
		QLearning q = null;
		
		try {
			File f = new File(QTABLE_FILENAME);
			FileInputStream fis = new FileInputStream(f);
			ObjectInputStream in = new ObjectInputStream(fis);
			q = (QLearning) in.readObject();
			in.close();
			fis.close();
		} catch (IOException | ClassNotFoundException e) {
			System.err.println("Error when loading qtable data!");
		}
		
		return q;
	}
	
	public void savePolicy() {
		try {
			File f = new File(QPOLICY_FILENAME);
			if (f.exists()) f.delete();
			FileOutputStream fos = new FileOutputStream(f);
			ObjectOutputStream out = new ObjectOutputStream(fos);
			
			HashMap<State, ACTIONS> policy = new HashMap<>();
			for (int i = 0; i < States.NUM_STATES; i++) {
				ACTIONS action = getBestAction(States.getStateFromIndex(i));
				policy.put(States.getStateFromIndex(i), action);
			}
			
			out.writeObject(policy);
			out.close();
			fos.close();
		} catch (IOException e) {
			System.err.println("Error when saving policy data!");
		}
	}
	
	@SuppressWarnings("unchecked")
	public static HashMap<State, ACTIONS> loadPolicy() {
		HashMap<State, ACTIONS> policy = null;
		
		try {
			File f = new File(QPOLICY_FILENAME);
			FileInputStream fis = new FileInputStream(f);
			ObjectInputStream in = new ObjectInputStream(fis);
			policy = (HashMap<State, ACTIONS>) in.readObject();
			in.close();
			fis.close();
		} catch (IOException | ClassNotFoundException e) {
			System.err.println("Error when loading qtable data!");
		}
		
		return policy;
	}
	
}
