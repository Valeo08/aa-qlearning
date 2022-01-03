package main.qlearning;

import java.util.ArrayList;

import core.game.Observation;
import core.game.StateObservation;
import tools.Vector2d;

public class States {

	public static int NUM_STATES;
	
	private static State EAST_COLLISION_STATE = new State("Colliding Right");
	private static State WEST_COLLISION_STATE = new State("Colliding Left");
	
	private static State NE_COLLISION_STATE = new State("Colliding Up & Right");
	private static State NW_COLLISION_STATE = new State("Colliding Up & Left");
	private static State SE_COLLISION_STATE = new State("Colliding Down & Right");
	private static State SW_COLLISION_STATE = new State("Colliding Down & Left");
	
	private static State WE_COLLISION_STATE = new State("Colliding Left & Right");
	private static State NS_COLLISION_STATE = new State("Colliding Up & Down");
	
	private static State TRAPPED_RIGHT_STATE = new State("Trapped - Goal to the Right");
	private static State TRAPPED_LEFT_STATE = new State("Trapped - Goal to the Left");
	
	private static State MAYBE_TRAPPED_RIGHT_GAP_ON_TOP_STATE = 
			new State("May be trapped if keeps going right - Gap on top");
	private static State MAYBE_TRAPPED_RIGHT_GAP_BOTTOM_TOP_STATE = 
			new State("May be trapped if keeps going right - Gap on bottom");
	private static State MAYBE_TRAPPED_LEFT_GAP_ON_TOP_STATE = 
			new State("May be trapped if keeps going left - Gap on top");
	private static State MAYBE_TRAPPED_LEFT_GAP_BOTTOM_TOP_STATE = 
			new State("May be trapped if keeps going left - Gap on bottom");
	
	private static State GOAL_RIGHT_STATE = new State("Goal to the Right");
	private static State GOAL_LEFT_STATE = new State("Goal to the Left");
	private static State GAME_OVER_STATE = new State("Game Finished");
	
	private static ArrayList<State> states = new ArrayList<>();
	
	public static void init() {
		// Reset
		if (!states.isEmpty()) for (State s : states) states.remove(s);
		
		// Init states
		states.add(EAST_COLLISION_STATE);
		states.add(WEST_COLLISION_STATE);
		
		states.add(NE_COLLISION_STATE);
		states.add(NW_COLLISION_STATE);
		states.add(SE_COLLISION_STATE);
		states.add(SW_COLLISION_STATE);
		
		states.add(WE_COLLISION_STATE);
		states.add(NS_COLLISION_STATE);
		
		states.add(TRAPPED_RIGHT_STATE);
		states.add(TRAPPED_LEFT_STATE);
		
		states.add(MAYBE_TRAPPED_RIGHT_GAP_ON_TOP_STATE);
		states.add(MAYBE_TRAPPED_RIGHT_GAP_BOTTOM_TOP_STATE);
		states.add(MAYBE_TRAPPED_LEFT_GAP_ON_TOP_STATE);
		states.add(MAYBE_TRAPPED_LEFT_GAP_BOTTOM_TOP_STATE);
		
		states.add(GOAL_RIGHT_STATE);
		states.add(GOAL_LEFT_STATE);
		states.add(GAME_OVER_STATE);
		
		// Set the size
		NUM_STATES = states.size();
	}
	
	public static State checkState(StateObservation so) {
		State state = null;
		
		boolean goalRight = checkGoalPosition(so);
		boolean gapOnTop = true, gapOnBottom = true;
		boolean maybeTrapped = checkWillBeTrapped(so, goalRight, gapOnTop, gapOnBottom);
		boolean isTrapped = checkTrapped(so, so.getAvatarPosition(), goalRight);
		
		state = checkCollisions(so, goalRight, isTrapped);
		
		if (state == null)
			state = (goalRight) ? GOAL_RIGHT_STATE : GOAL_LEFT_STATE;
		
		if (!isTrapped && maybeTrapped && goalRight) {
			if (gapOnTop) state = MAYBE_TRAPPED_RIGHT_GAP_ON_TOP_STATE;
			else if (gapOnBottom) state = MAYBE_TRAPPED_RIGHT_GAP_BOTTOM_TOP_STATE;
			else state = TRAPPED_RIGHT_STATE;
		}
		
		if (!isTrapped && maybeTrapped && !goalRight) {
			if (gapOnTop) state = MAYBE_TRAPPED_LEFT_GAP_ON_TOP_STATE;
			else if (gapOnBottom) state = MAYBE_TRAPPED_LEFT_GAP_BOTTOM_TOP_STATE;
			else state = TRAPPED_LEFT_STATE;
		}
		
		if (so.isGameOver()) state = GAME_OVER_STATE;
		
		return state;
	}
	
	// True if right - False if left
	private static boolean checkGoalPosition(StateObservation so) {
		Vector2d avPos = so.getAvatarPosition();
		double sumGoalXPos = 0;
		int numGoals = 0;
		
		ArrayList<Observation>[] goalPos = so.getPortalsPositions();
		for (ArrayList<Observation> obsPos : goalPos)
			for (Observation ob : obsPos) {
				numGoals++;
				sumGoalXPos += ob.position.x;
			}
		
		double meanGoalXPos = sumGoalXPos / numGoals;
		
		return (meanGoalXPos > avPos.x);
	}
	
	// True if the avatar is going to be trapped if keeps goinf forward.
	// Being trapped is defined below.
	private static boolean checkWillBeTrapped(StateObservation so, boolean goalRight,
			boolean gapOnTop, boolean gapOnBottom) {
		Vector2d avPos = so.getAvatarPosition();
		int ax = (int)avPos.x / so.getBlockSize();
		int ay = (int)avPos.y / so.getBlockSize();
		
		ArrayList<Observation> grid[][] = so.getObservationGrid();
		
		int wallx = -1;
		int wally = ay;
		
		boolean trapForward = false;
		
		// Check where's the wall x tile position (based on the goal position)
		if (goalRight) {
			int x = ax + 1;
			while (wallx == -1 && x < (so.getWorldDimension().width / so.getBlockSize()) - 1) {
				for (Observation ob : grid[x][wally])
					if (ob.category == 4) wallx = (int)ob.position.x / so.getBlockSize();
				x++;
			}
			
			if (wallx != -1) {
				// Check if trapped at position (wallx - 1, ay)
				Vector2d pos = new Vector2d((wallx-1)*so.getBlockSize(), avPos.y);
				trapForward = checkTrapped(so, pos, goalRight);
				
				if (trapForward) {
					// Detect top wall
					int topWallYPos = -1;
					for (int y = ay - 1; topWallYPos == -1 && y >= 0; y--)
						for (Observation ob : grid[wallx-1][y])
							if (ob.category == 4) 
								topWallYPos = (int)ob.position.y / so.getBlockSize();
					
					// Detect bottom wall
					int bottomWallYPos = -1;
					for (int y = ay + 1; bottomWallYPos == -1 
							&& y < (so.getWorldDimension().height/so.getBlockSize()); y++)
						for (Observation ob : grid[wallx-1][y])
							if (ob.category == 4) bottomWallYPos = (int)ob.position.y / so.getBlockSize();
					
					// Check if there's a gap onto the top and bottom walls from
					// the wallx - 1 position to the avatar x position
					int numWallsTop = 0, numWallBottom = 0;
					for (x = wallx - 1; x > ax; x--) {
						for (Observation ob : grid[x][topWallYPos])
							if (ob.category == 4) numWallsTop++;
						
						for (Observation ob : grid[x][bottomWallYPos])
							if (ob.category == 4) numWallBottom++;
					}
					
					// Detect if there's a gap on top and bottom of the trap from the avatar position
					if ((numWallsTop == (wallx - 1) - ax) || (numWallBottom == (wallx - 1) - ax)) {
						for (Observation ob : grid[ax][topWallYPos])
							if (ob.category == 4) gapOnTop = false;
						for (Observation ob : grid[ax][bottomWallYPos])
							if (ob.category == 4) gapOnBottom = false;
						
						return (!gapOnTop && !gapOnBottom);
					} else return false;
				}
			}
		} else {
			int x = ax - 1;
			while (wallx == -1 && x > 1) {
				for (Observation ob : grid[x][wally])
					if (ob.category == 4) wallx = (int)ob.position.x / so.getBlockSize();
				x--;
			}
			
			if (wallx != -1) {
				// Check if trapped at position (wallx + 1, ay)
				Vector2d pos = new Vector2d((wallx+1)*so.getBlockSize(), avPos.y);
				trapForward = checkTrapped(so, pos, goalRight);
				
				if (trapForward) {
					// Detect top wall
					int topWallYPos = -1;
					for (int y = ay - 1; topWallYPos == -1 && y >= 0; y--)
						for (Observation ob : grid[wallx+1][y])
							if (ob.category == 4) 
								topWallYPos = (int)ob.position.y / so.getBlockSize();
					
					// Detect bottom wall
					int bottomWallYPos = -1;
					for (int y = ay + 1; bottomWallYPos == -1 
							&& y < (so.getWorldDimension().height/so.getBlockSize()); y++)
						for (Observation ob : grid[wallx+1][y])
							if (ob.category == 4) bottomWallYPos = (int)ob.position.y / so.getBlockSize();
					
					// Check if there's a gap onto the top and bottom walls from
					// the wallx + 1 position to the avatar x position
					int numWallsTop = 0, numWallBottom = 0;
					for (x = wallx + 1; x < ax; x++) {
						for (Observation ob : grid[x][topWallYPos])
							if (ob.category == 4) numWallsTop++;
						
						for (Observation ob : grid[x][bottomWallYPos])
							if (ob.category == 4) numWallBottom++;
					}
					
					// Detect if there's a gap on top and bottom of the trap from the avatar position
					if ((numWallsTop == ax - (wallx + 1)) || (numWallBottom == ax - (wallx + 1))) {
						for (Observation ob : grid[ax][topWallYPos])
							if (ob.category == 4) gapOnTop = false;
						for (Observation ob : grid[ax][bottomWallYPos])
							if (ob.category == 4) gapOnBottom = false;
						
						return (!gapOnTop && !gapOnBottom);
					} else return false;
				}
			}
		}
		
		return false;
	}
	
	// True if the avatar at the position is trapped: there's a wall forward,
	// at the top and at the bottom.
	private static boolean checkTrapped(StateObservation so, Vector2d pos, boolean goalRight) {
		int ax = (int)pos.x / so.getBlockSize();
		int ay = (int)pos.y / so.getBlockSize();
		
		ArrayList<Observation> grid[][] = so.getObservationGrid();
		
		int wallx = (goalRight) ? (ax + 1) : (ax - 1);
		int wally = ay;
		
		// Check if there's a wall tile forward
		boolean wallForward = false;
		for (Observation ob : grid[wallx][wally])
			if (ob.category == 4) wallForward = true;
		
		// If so - continue with the verification
		if (wallForward) {
			// Detect top wall
			int topWallYPos = -1;
			for (int y = ay - 1; topWallYPos == -1 && y >= 0; y--)
				for (Observation ob : grid[ax][y])
					if (ob.category == 4) 
						topWallYPos = (int)ob.position.y / so.getBlockSize();
			
			// Detect bottom wall
			int bottomWallYPos = -1;
			for (int y = ay + 1; bottomWallYPos == -1 && y < (so.getWorldDimension().height/so.getBlockSize()); y++)
				for (Observation ob : grid[ax][y])
					if (ob.category == 4) bottomWallYPos = (int)ob.position.y / so.getBlockSize();
			
			// Check now the forward entire wall
			if (topWallYPos != -1 && bottomWallYPos != -1) {
				int numWallsForward = 0;
				for (int y = topWallYPos + 1; y <= bottomWallYPos - 1; y++)
					for (Observation ob : grid[wallx][y])
						if (ob.category == 4) numWallsForward++;
				
				// Verification to check if there's a entire wall forward
				if (numWallsForward == (bottomWallYPos - topWallYPos - 1))
					return true;
			}
		}
		
		return false;
	}
	
	// Check states of the avatar relative to collisions with walls
	private static State checkCollisions(StateObservation so, 
			boolean goalRight, boolean isTrapped) {
		State newState = null;
	
		Vector2d avPos = so.getAvatarPosition();
		
		// Prevent errors when game over
		if (avPos.x == -1 || avPos.y == -1) return null;
		
		int ax = (int)avPos.x / so.getBlockSize();
		int ay = (int)avPos.y / so.getBlockSize();
		
		ArrayList<Observation> grid[][] = so.getObservationGrid();
		
		boolean left, right, up, down;
		left = right = up = down = false;
		
		// Check left
		for (Observation ob : grid[ax-1][ay])
			if (ob.category == 4) left = true;
		
		// Check right
		for (Observation ob : grid[ax+1][ay])
			if (ob.category == 4) right = true;
		
		// Check up
		for (Observation ob : grid[ax][ay-1])
			if (ob.category == 4) up = true;
		
		// Check down
		for (Observation ob : grid[ax][ay+1])
			if (ob.category == 4) down = true;
		
		// Check simple collisions
		if (left)
			newState = (goalRight) ? GOAL_RIGHT_STATE 
					: (isTrapped) ? TRAPPED_LEFT_STATE : WEST_COLLISION_STATE;
		else if (right) 
			newState = (!goalRight) ? GOAL_LEFT_STATE
					: (isTrapped) ? TRAPPED_RIGHT_STATE : EAST_COLLISION_STATE;
		
		// Check multiple collisions
		if (left && right) newState = WE_COLLISION_STATE;
		
		if (up && down) newState = NS_COLLISION_STATE;
		
		if (up && left && !goalRight) 
			newState = (isTrapped) ? TRAPPED_LEFT_STATE : NW_COLLISION_STATE;
		
		if (up && right && goalRight) 
			newState = (isTrapped) ? TRAPPED_RIGHT_STATE : NE_COLLISION_STATE;
		
		if (down && left && !goalRight)
			newState = (isTrapped) ? TRAPPED_LEFT_STATE : SW_COLLISION_STATE;
		
		if (down && right && goalRight) 
			newState = (isTrapped) ? TRAPPED_RIGHT_STATE : SE_COLLISION_STATE;
		
		return newState;
	}
	
	public static State getStateFromIndex(final int index) {
		return states.get(index);
	}
	
}
