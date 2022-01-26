package main.qlearning;

import java.util.ArrayList;

import core.game.Observation;
import core.game.StateObservation;
import tools.Vector2d;

public class States {

	public static int NUM_STATES;
	
	// Single collisions without escaping
	private static State LEFT_COLLISION_STATE = new State("Collision LEFT");
	private static State RIGHT_COLLISION_STATE = new State("Collision RIGHT");
	private static State TOP_COLLISION_STATE = new State("Collision TOP");
	private static State BOTTOM_COLLISION_STATE = new State("Collision BOTTOM");
	
	// Multiple collisions without escaping
	private static State LEFT_TOP_COLLISION_STATE = new State("Collision LEFT and TOP");
	private static State RIGHT_TOP_COLLISION_STATE = new State("Collision RIGHT and TOP");
	private static State LEFT_BOTTOM_COLLISION_STATE = new State("Collision LEFT and BOTTOM");
	private static State RIGHT_BOTTOM_COLLISION_STATE = new State("Collision RIGHT and BOTTOM");
	
	// Single collisions escaping from George
	private static State ESCAPING_LEFT_COLLISION_STATE = new State("Escaping - Collision LEFT");
	private static State ESCAPING_RIGHT_COLLISION_STATE = new State("Escaping - Collision RIGHT");
	private static State ESCAPING_TOP_COLLISION_STATE = new State("Escaping - Collision TOP");
	private static State ESCAPING_BOTTOM_COLLISION_STATE = new State("Escaping - Collision BOTTOM");
	
	// Multiple collisions escaping from George
	private static State ESCAPING_LEFT_TOP_COLLISION_STATE = new State("Escaping - Collision LEFT and TOP");
	private static State ESCAPING_RIGHT_TOP_COLLISION_STATE = new State("Escaping - Collision RIGHT and TOP");
	private static State ESCAPING_LEFT_BOTTOM_COLLISION_STATE = new State("Escaping - Collision LEFT and BOTTOM");
	private static State ESCAPING_RIGHT_BOTTOM_COLLISION_STATE = new State("Escaping - Collision RIGHT and BOTTOM");
	
	// Escaping from George without collisions
	private static State ESCAPING_GEORGE_LEFT_STATE = new State("Escaping - George to the LEFT");
	private static State ESCAPING_GEORGE_RIGHT_STATE = new State("Escaping - George to the RIGHT");
	private static State ESCAPING_GEORGE_TOP_STATE = new State("Escaping - George to the TOP");
	private static State ESCAPING_GEORGE_BOTTOM_STATE = new State("Escaping - George to the BOTTOM");
	
	// Positioning of nearest annoyed NPC
	private static State NEAREST_NPC_LEFT_STATE = new State("Nearest NPC to the LEFT");
	private static State NEAREST_NPC_RIGHT_STATE = new State("Nearest NPC to the RIGHT");
	private static State NEAREST_NPC_TOP_STATE = new State("Nearest NPC to the TOP");
	private static State NEAREST_NPC_BOTTOM_STATE = new State("Nearest NPC to the BOTTOM");
	
	// Next to target NPC
	private static State NPC_AT_LEFT_STATE = new State("NPC at LEFT");
	private static State NPC_AT_RIGHT_STATE = new State("NPC at RIGHT");
	private static State NPC_AT_TOP_STATE = new State("NPC at TOP");
	private static State NPC_AT_BOTTOM_STATE = new State("NPC at BOTTOM");

	private static State IDLE_STATE = new State("Idle");
	private static State GAME_OVER_STATE = new State("Game Finished");
	
	private static ArrayList<State> states = new ArrayList<>();
	
	public static void init() {
		// Reset
		if (!states.isEmpty()) for (State s : states) states.remove(s);
		
		// Init states
		states.add(LEFT_COLLISION_STATE);
		states.add(RIGHT_COLLISION_STATE);
		states.add(TOP_COLLISION_STATE);
		states.add(BOTTOM_COLLISION_STATE);
		
		states.add(LEFT_TOP_COLLISION_STATE);
		states.add(LEFT_BOTTOM_COLLISION_STATE);
		states.add(RIGHT_TOP_COLLISION_STATE);
		states.add(RIGHT_BOTTOM_COLLISION_STATE);
		
		states.add(ESCAPING_LEFT_COLLISION_STATE);
		states.add(ESCAPING_RIGHT_COLLISION_STATE);
		states.add(ESCAPING_TOP_COLLISION_STATE);
		states.add(ESCAPING_BOTTOM_COLLISION_STATE);
		
		states.add(ESCAPING_LEFT_TOP_COLLISION_STATE);
		states.add(ESCAPING_LEFT_BOTTOM_COLLISION_STATE);
		states.add(ESCAPING_RIGHT_TOP_COLLISION_STATE);
		states.add(ESCAPING_RIGHT_BOTTOM_COLLISION_STATE);
		
		states.add(ESCAPING_GEORGE_LEFT_STATE);
		states.add(ESCAPING_GEORGE_RIGHT_STATE);
		states.add(ESCAPING_GEORGE_TOP_STATE);
		states.add(ESCAPING_GEORGE_BOTTOM_STATE);
		
		states.add(NEAREST_NPC_LEFT_STATE);
		states.add(NEAREST_NPC_RIGHT_STATE);
		states.add(NEAREST_NPC_TOP_STATE);
		states.add(NEAREST_NPC_BOTTOM_STATE);
		
		states.add(NPC_AT_LEFT_STATE);
		states.add(NPC_AT_RIGHT_STATE);
		states.add(NPC_AT_TOP_STATE);
		states.add(NPC_AT_BOTTOM_STATE);
		
		states.add(IDLE_STATE);
		states.add(GAME_OVER_STATE);
		
		// Set the size
		NUM_STATES = states.size();
	}
	
	public static State checkState(StateObservation so) {
		State state = null;
		
		boolean escaping = checkIfEscaping(so);
		state = checkCollisions(so, escaping);
		
		if (state == null)
			state = checkWhereToEscape(so, escaping);
		
		if (state == null)
			state = checkNPCToHelp(so);
		
		if (so.isGameOver()) state = GAME_OVER_STATE;
		
		if (state == null) state = IDLE_STATE;
		
		return state;
	}

	// Check if the avatar needs to avoid George
	private static boolean checkIfEscaping(StateObservation so) {
		boolean needsToEscape = false;
		
		// Detect where is George
		Observation georgeOb = null;
		for (ArrayList<Observation> al : so.getNPCPositions())
			for (Observation ob : al)
				if (ob.category == 3 && ob.itype == 7)
					georgeOb = ob;
		
		// Check if the avatar needs to avoid George based on his position
		if (georgeOb != null) {
			Vector2d avPos = so.getAvatarPosition();
			Vector2d gPos = georgeOb.position;
			
			// Prevent errors when game over
			if (avPos.x == -1 || avPos.y == -1) return false;
			
			int dist = 4;
			double distThreshold = (double)dist * so.getBlockSize();
			
			double euclideanDist = Math.sqrt(((gPos.x - avPos.x) * (gPos.x - avPos.x)) 
					+ ((gPos.y - avPos.y) * (gPos.y - avPos.y)));
			
			needsToEscape = euclideanDist <= distThreshold;
		}
		
		return needsToEscape;
	}
	
	// Check states of the avatar relative to collisions with walls
	private static State checkCollisions(StateObservation so, boolean escaping) {
		State newState = null;
		Vector2d avPos = so.getAvatarPosition();
		
		// Prevent errors when game over
		if (avPos.x == -1 || avPos.y == -1) return null;
		
		int ax = (int)avPos.x / so.getBlockSize();
		int ay = (int)avPos.y / so.getBlockSize();
		ArrayList<Observation> grid[][] = so.getObservationGrid();
		boolean left, right, top, bottom;
		left = right = top = bottom = false;
		
		// Check left collision
		for (Observation ob : grid[ax-1][ay])
			if (ob.category == 4) left = true;
		
		// Check right collision
		for (Observation ob : grid[ax+1][ay])
			if (ob.category == 4) right = true;
		
		// Check up collision
		for (Observation ob : grid[ax][ay-1])
			if (ob.category == 4) top = true;
		
		// Check down collision
		for (Observation ob : grid[ax][ay+1])
			if (ob.category == 4) bottom = true;
		
		// Check simple collisions
		if (left) newState = (escaping) ? ESCAPING_LEFT_COLLISION_STATE : LEFT_COLLISION_STATE;
		if (right && newState != null) newState = (escaping) ? ESCAPING_RIGHT_COLLISION_STATE : RIGHT_COLLISION_STATE;
		if (top) newState = (escaping) ? ESCAPING_TOP_COLLISION_STATE : TOP_COLLISION_STATE;
		if (bottom && newState != null) newState = (escaping) ? ESCAPING_BOTTOM_COLLISION_STATE : BOTTOM_COLLISION_STATE;
		
		// Check multiple collisions
		if (left && top) newState = (escaping) ? ESCAPING_LEFT_TOP_COLLISION_STATE : LEFT_TOP_COLLISION_STATE;
		if (left && bottom) newState = (escaping) ? ESCAPING_LEFT_BOTTOM_COLLISION_STATE : LEFT_BOTTOM_COLLISION_STATE;
		if (right && top) newState = (escaping) ? ESCAPING_RIGHT_TOP_COLLISION_STATE : RIGHT_TOP_COLLISION_STATE;
		if (right && bottom) newState = (escaping) ? ESCAPING_RIGHT_BOTTOM_COLLISION_STATE : RIGHT_BOTTOM_COLLISION_STATE;
		
		return newState;
	}
	
	// Check the position of George from the avatar when escaping
	private static State checkWhereToEscape(StateObservation so, boolean escaping) {
		if (!escaping) return null;
		
		State newState = null;
		
		// Detect where is George
		Observation georgeOb = null;
		for (ArrayList<Observation> al : so.getNPCPositions())
			for (Observation ob : al)
				if (ob.category == 3 && ob.itype == 7)
					georgeOb = ob;
		
		// Check if the avatar needs to avoid George based on his position
		if (georgeOb != null) {
			Vector2d avPos = so.getAvatarPosition();
			Vector2d gPos = georgeOb.position;
			
			// Prevent errors when game over
			if (avPos.x == -1 || avPos.y == -1) return null;
			
			if (Math.abs(avPos.x - gPos.x) > Math.abs(avPos.y - gPos.y))
				newState = (avPos.x > gPos.x) ? ESCAPING_GEORGE_LEFT_STATE : ESCAPING_GEORGE_RIGHT_STATE;
			else
				newState = (avPos.y > gPos.y) ? ESCAPING_GEORGE_TOP_STATE : ESCAPING_GEORGE_BOTTOM_STATE;
		}
		
		return newState;
	}
	
	// Check the nearest NPC position from the avatar
	private static State checkNPCToHelp(StateObservation so) {
		State newState = null;
		
		Observation npc = null;
		ArrayList<Observation> friendlyNPCs = new ArrayList<>();
		for (ArrayList<Observation> arr : so.getNPCPositions(so.getAvatarPosition()))
			for (Observation ob : arr)
				if (ob.category == 3 && ob.itype == 4)
					friendlyNPCs.add(ob);
		
		if (friendlyNPCs.size() > 0) {
			npc = friendlyNPCs.get(0);
			
			Vector2d avPos = so.getAvatarPosition();
			Vector2d npcPos = npc.position;
			
			int avX = (int) avPos.x / so.getBlockSize();
			int avY = (int) avPos.y / so.getBlockSize();
			int npcX = (int) npcPos.x / so.getBlockSize();
			int npcY = (int) npcPos.y / so.getBlockSize();
			
			if (Math.abs(avX - npcX) >= Math.abs(avY - npcY)) {
				if (avX >= npcX)
					newState = (avX == npcX + 1) ? NPC_AT_LEFT_STATE : NEAREST_NPC_LEFT_STATE;
				else
					newState = (avX == npcX - 1) ? NPC_AT_RIGHT_STATE : NEAREST_NPC_RIGHT_STATE;
			} else {
				if (avY >= npcY)
					newState = (avY == npcY + 1) ? NPC_AT_TOP_STATE : NEAREST_NPC_TOP_STATE;
				else
					newState = (avY == npcY - 1) ? NPC_AT_BOTTOM_STATE : NEAREST_NPC_BOTTOM_STATE;
			}
			
		}
		
		return newState;
	}
	
	public static State getStateFromIndex(final int index) {
		return states.get(index);
	}
	
}
