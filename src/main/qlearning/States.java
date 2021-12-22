package main.qlearning;

import java.util.ArrayList;

import core.game.Observation;
import core.game.StateObservation;
import tools.Vector2d;

public class States {

	public static int NUM_STATES = 13;
	
	public static State EAST_COLLISION_STATE = new State("Colliding Left");
	public static State WEST_COLLISION_STATE = new State("Colliding Right");
	public static State NORTH_COLLISION_STATE = new State("Colliding Up");
	public static State SOUTH_COLLISION_STATE = new State("Colliding Down");
	
	public static State NE_COLLISION_STATE = new State("Colliding Up & Left");
	public static State NW_COLLISION_STATE = new State("Colliding Up & Right");
	public static State SE_COLLISION_STATE = new State("Colliding Down & Left");
	public static State SW_COLLISION_STATE = new State("Colliding Down & Right");
	
	public static State WE_COLLISION_STATE = new State("Colliding Left & Right");
	public static State NS_COLLISION_STATE = new State("Colliding Up & Down");
	
	public static State GOAL_RIGHT_STATE = new State("Goal to the Right");
	public static State GOAL_LEFT_STATE = new State("Goal to the Left");
	public static State GAME_OVER_STATE = new State("Game Finished");
	
	public static State checkState(StateObservation so) {
		State state = null;
		
		state = checkGoalPosition(so);
		state = checkCollisions(so, state);
		
		if (so.isGameOver()) state = GAME_OVER_STATE;
		
		return state;
	}
	
	/*
	private static State checkCollision(Vector2d obPos, StateObservation so) {
		State state = null;
		int bz = so.getBlockSize();
		Vector2d avPos = so.getAvatarPosition();
		int obx = (int)obPos.x / bz, oby = (int)obPos.y / bz;
		int avx = (int)avPos.x / bz, avy = (int)avPos.y / bz;
		
		boolean left = (obx == avx - 1 && oby == avy);
		boolean right = (obx == avx + 1 && oby == avy);
		boolean up = (oby == avy - 1 && obx == avx);
		boolean down = (oby == avy + 1 && obx == avx);
		
		if (left) state = EAST_COLLISION_STATE;
		else if (right) state = WEST_COLLISION_STATE;
		else if (up) state = NORTH_COLLISION_STATE;
		else if (down) state = SOUTH_COLLISION_STATE;
		
		if (state != null) state = 
				checkMultipleCollision(obPos, so, state);
		
		return state;
	}
	
	private static State checkMultipleCollision(Vector2d collider, 
			StateObservation so, State state) {
		State newState = null;
		int bz = so.getBlockSize();
		Vector2d avPos = so.getAvatarPosition();
		int avx = (int)avPos.x / bz, avy = (int)avPos.y / bz;
		
		ArrayList<Observation>[] immovablePos = so.getImmovablePositions();
		for (int i = 0; newState == null && i < immovablePos.length; i++) {
			for (int j = 0; newState == null && j < immovablePos[i].size(); j++) {
				Vector2d obPos = immovablePos[i].get(j).position;
				if (obPos.equals(collider)) continue;
				int obx = (int)obPos.x / bz, oby = (int)obPos.y / bz;
				
				boolean left = (obx == avx - 1 && oby == avy);
				boolean right = (obx == avx + 1 && oby == avy);
				boolean up = (oby == avy - 1 && obx == avx);
				boolean down = (oby == avy + 1 && obx == avx);
				
				if (state.equals(NORTH_COLLISION_STATE)) {
					if (left) newState = NW_COLLISION_STATE;
					else if (right) newState = NW_COLLISION_STATE;
				} else if (state.equals(SOUTH_COLLISION_STATE)) {
					if (left) newState = SW_COLLISION_STATE;
					else if (right) newState = SE_COLLISION_STATE;
				} else if (state.equals(WEST_COLLISION_STATE)) {
					if (up) newState = NW_COLLISION_STATE;
					else if (down) newState = SW_COLLISION_STATE;
				} else if (state.equals(EAST_COLLISION_STATE)) {
					if (up) newState = NE_COLLISION_STATE;
					else if (down) newState = SE_COLLISION_STATE;
				}
			}
		}
		
		return (newState == null) ? state : newState;
	}*/
	
	private static State checkCollisions(StateObservation so, State state) {
		State newState = state;
		
		Vector2d avPos = so.getAvatarPosition();
		int ax = (int)avPos.x / so.getBlockSize();
		int ay = (int)avPos.y / so.getBlockSize();
		ArrayList<Observation> immovablePos = so.getImmovablePositions()[0];
		
		for (Observation ob1 : immovablePos) {
			int ox1 = (int) ob1.position.x / so.getBlockSize(),
				oy1 = (int) ob1.position.y / so.getBlockSize();
			
			boolean left1 = (oy1 == ay) && (ox1 - 1 == ax);
			boolean right1 = (oy1 == ay) && (ox1 + 1 == ax);
			boolean up1 = (ox1 == ax) && (oy1 - 1 == ay);
			boolean down1 = (ox1 == ax) && (oy1 + 1 == ay);
			
			if (left1) newState = WEST_COLLISION_STATE;
			else if (right1) newState = EAST_COLLISION_STATE;
			else if (up1) newState = NORTH_COLLISION_STATE;
			else if (down1) newState = SOUTH_COLLISION_STATE;
			
			for (Observation ob2 : immovablePos) {
				if (ob1.equals(ob2)) continue;
				
				int ox2 = (int) ob2.position.x / so.getBlockSize(),
					oy2 = (int) ob2.position.y / so.getBlockSize();
				
				boolean left2 = (oy2 == ay) && (ox2 - 1 == ax);
				boolean right2 = (oy2 == ay) && (ox2 + 1 == ax);
				boolean up2 = (ox2 == ax) && (oy2 - 1 == ay);
				boolean down2 = (ox2 == ax) && (oy2 + 1 == ay);
				
				if (left1 && right2)
					newState = WE_COLLISION_STATE;
				else if (up1 && down2)
					newState = NS_COLLISION_STATE;
				else if (up1 && left2)
					newState = NW_COLLISION_STATE;
				else if (up1 && right2)
					newState = NE_COLLISION_STATE;
				else if (down1 && left2)
					newState = SW_COLLISION_STATE;
				else if (down1 && right2)
					newState = SE_COLLISION_STATE;
			}
		}
			
		
		return newState;
	}
	
	private static State checkGoalPosition(StateObservation so) {
		State state = null;
		Vector2d avPos = so.getAvatarPosition();
		double sumGoalXPos = 0;
		int numGoals = 0;
		
		ArrayList<Observation>[] goalPos = so.getPortalsPositions();
		for (ArrayList<Observation> obsPos : goalPos)
			for (Observation ob : obsPos) {
				numGoals++;
				sumGoalXPos += ob.position.x;
			}
		
		if (numGoals == 0) return null;
		
		double meanGoalXPos = sumGoalXPos / numGoals;
		
		if (meanGoalXPos > avPos.x) state = GOAL_RIGHT_STATE;
		else if (meanGoalXPos < avPos.x) state = GOAL_LEFT_STATE;
		
		return state;
	}
	
	public static State getStateFromIndex(final int index) {
		State state = null;
	
		switch (index) {
		case 0:
			state = EAST_COLLISION_STATE;
			break;
		case 1:
			state = WEST_COLLISION_STATE;
			break;
		case 2:
			state = NORTH_COLLISION_STATE;
			break;
		case 3:
			state = SOUTH_COLLISION_STATE;
			break;
		case 4:
			state = NE_COLLISION_STATE;
			break;
		case 5:
			state = NW_COLLISION_STATE;
			break;
		case 6:
			state = SE_COLLISION_STATE;
			break;
		case 7:
			state = SW_COLLISION_STATE;
			break;
		case 8:
			state = WE_COLLISION_STATE;
			break;
		case 9:
			state = NS_COLLISION_STATE;
			break;
		case 10:
			state = GOAL_RIGHT_STATE;
			break;
		case 11:
			state = GOAL_LEFT_STATE;
			break;
		case 12:
			state = GAME_OVER_STATE;
			break;
		}
		
		return state;
	}
	
}
