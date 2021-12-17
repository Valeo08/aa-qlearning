package main.qlearning;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import core.game.Observation;
import core.game.StateObservation;
import core.player.AbstractPlayer;
import ontology.Types.ACTIONS;
import tools.ElapsedCpuTimer;
import tools.Vector2d;

public class QLearner extends AbstractPlayer {
	
	private final int levelIdx = 0;
	private final String map = "map-camel" + levelIdx + ".dat";
	private final String table = "q-table-camel" + levelIdx + ".dat";
	
	private QLearning learn;

	public QLearner(StateObservation so, ElapsedCpuTimer et) {
		// IT IS BORN
		try {
			// For generating the map representation file
			//generateMapFile(map, so);
			
			// For behavior
			learn = new QLearning(map);
			learn.init();
			
			// Loads a previous generated Q-table
			learn.loadQTable(table);
		} catch (IOException ex) {
			System.err.println(ex.getMessage());
		}
	}
	
	@Override
	public ACTIONS act(StateObservation so, ElapsedCpuTimer et) {	
		// THINK
		ACTIONS action = ACTIONS.ACTION_NIL;
		
		Vector2d plyPos = so.getAvatarPosition();
		int currentState = learn.getStateFromPosition(plyPos.x / so.getBlockSize(),
				plyPos.y / so.getBlockSize());
		int actionIndex = learn.getPolicyFromState(currentState);
		
		System.out.print("State: " + currentState + " -> Action " + actionIndex + "\n");
		
		switch (actionIndex) {
		case 0:
			action = ACTIONS.ACTION_LEFT;
			break;
		case 1:
			action = ACTIONS.ACTION_RIGHT;
			break;
		case 2:
			action = ACTIONS.ACTION_UP;
			break;
		case 3:
			action = ACTIONS.ACTION_DOWN;
			break;
		default:
			action = ACTIONS.ACTION_NIL;
			break;
		}
		
		System.out.println("Action selected: " + action);
		
		// ACT
		return action;
	}
	
	@SuppressWarnings("unused")
	private static void generateMapFile(String path, StateObservation so) throws IOException {
		File f = new File(path);
		if (f.exists()) f.delete();
		BufferedWriter bw = new BufferedWriter(new FileWriter(f));
		
		StringBuilder sb = new StringBuilder();
		
		// Map size
		sb.append("S#");
		sb.append(so.getWorldDimension().width / so.getBlockSize());
		sb.append(":");
		sb.append(so.getWorldDimension().height / so.getBlockSize());
		sb.append("\n");
		
		// Walls
		sb.append("W#");
		for (Observation ob : so.getImmovablePositions()[0])
			sb.append((int)ob.position.x / so.getBlockSize()).append(",")
				.append((int)ob.position.y / so.getBlockSize()).append(";");
		sb.deleteCharAt(sb.length() - 1);
		sb.append("\n");
		
		// Goals - Portals - Final tiles
		sb.append("F#");
		for (Observation ob : so.getPortalsPositions()[0])
			sb.append((int)ob.position.x / so.getBlockSize()).append(",")
				.append((int)ob.position.y / so.getBlockSize()).append(";");
		sb.deleteCharAt(sb.length() - 1);
		
		bw.write(sb.toString());
		bw.close();
	}
	
}
