package main.qlearning;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import tools.Vector2d;

public class QLearning {
	
	public static final double DEFAULT_ALPHA = 0.1;
	public static final double DEFAULT_GAMMA = 0.9;
	
	// Learning rate
	private final double alpha;
	// Eagerness - 0 looks in the near future, 1 looks in the distant future
	private final double gamma;
	
	// Map size
	private final int mapWidth;
	private final int mapHeight;
	
	// Number of states
	private final int numStates;
	
	// Number of actions
	private int numActions;
	
	// Rewards and penalty for states
	private final int reward = 100;
	private final int penalty = -10;
	
	// States Reward Table
	private int[][] R;
	
	// Q-Table
    private double[][] Q;
    
    // Map representation for simpler comparisons
    // 0 for normal tile - 1 for wall tiles - 2 for final tiles
    private int[][] map;
	
	
	// Simple constructor
	public QLearning(final String path) throws IOException {
		this(path, DEFAULT_ALPHA, DEFAULT_GAMMA);
	}
	
	// Constructor with alpha and gamma parameters
	public QLearning(final String path, 
			final double alpha, final double gamma) throws IOException {
		this.alpha = alpha;
		this.gamma = gamma;
		
		File f = new File(path);
		BufferedReader br = new BufferedReader(new FileReader(f));
		
		String[] size = br.readLine().split("#")[1].split(":");
		
		this.mapWidth = Integer.parseInt(size[0]);
		this.mapHeight = Integer.parseInt(size[1]);
		this.numStates = this.mapWidth * this.mapHeight;
		this.numActions = 4;
		
		this.map = new int[mapHeight][mapWidth];
		for (int i = 0; i < mapHeight; i++)
			for (int j = 0; j < mapWidth; j++)
				this.map[i][j] = 0;
		
		String[] walls = br.readLine().split("#")[1].split(";");
		for (String wall : walls) {
			int x = Integer.parseInt(wall.split(",")[0]);
			int y = Integer.parseInt(wall.split(",")[1]);
			this.map[y][x] = 1;
		}
		
		String[] goals = br.readLine().split("#")[1].split(";");
		for (String goal : goals) {
			int x = Integer.parseInt(goal.split(",")[0]);
			int y = Integer.parseInt(goal.split(",")[1]);
			this.map[y][x] = 2;
		}
		
		br.close();
	}
	
	
	// Initialize Reward and Q tables
	public void init() {
		R = new int[numStates][numActions];
		Q = new double[numStates][numActions];
		
		// Navigating through the Reward table
		for (int i = 0; i < numStates; i++) {
			// Tiles position of an state
			int y = i / mapWidth;
			int x = i - y * mapWidth;
			
			// Fill in the reward table with -1
			for (int j = 0; j < numActions; j++)
				R[i][j] = -1;
			
			// If it's not a wall try all the movements
			if (map[y][x] != 1) {
				
				// Try go left
				int left = x - 1;
				if (left >= 0) {
					int target = 0;
					if (map[y][left] == 0) R[i][target] = 0;
					else if (map[y][left] == 2) R[i][target] = reward;
					else R[i][target] = penalty;
				}
				
				// Try go right
				int right = x + 1;
				if (right >= 0) {
					int target = 1;
					if (map[y][right] == 0) R[i][target] = 0;
					else if (map[y][right] == 2) R[i][target] = reward;
					else R[i][target] = penalty;
				}
				
				// Try go up
				int up = y - 1;
				if (up >= 0) {
					int target = 2;
					if (map[up][x] == 0) R[i][target] = 0;
					else if (map[up][x] == 2) R[i][target] = reward;
					else R[i][target] = penalty;
				}
				
				// Try go down
				int down = y + 1;
				if (down >= 0) {
					int target = 3;
					if (map[down][x] == 0) R[i][target] = 0;
					else if (map[down][x] == 2) R[i][target] = reward;
					else R[i][target] = penalty;
				}
				
			}
		}
		
		initializeQ();
        // printR();
	}
	
	// Initialize Q-Table
	private void initializeQ() {
		// Sets Q values to R values
		for (int i = 0; i < numStates; i++){
            for(int j = 0; j < numActions; j++){
                Q[i][j] = (double)R[i][j];
            }
        }
	}
	
	// For DEBUG purposes - prints the reward table
	public void printR() {
		System.out.printf("%25s", "States: ");
        for (int i = 0; i <= 3; i++)
            System.out.printf("%4s", i);
        System.out.println();

        for (int i = 0; i < numStates; i++) {
            System.out.print("Possible states from " + i + " :[");
            for (int j = 0; j < numActions; j++)
            	System.out.printf("%4s", R[i][j]);
            System.out.println("]");
        }
	}
	
	// Calculates the Q-table values
	public void calculateQ() {
		Random random = new Random(System.nanoTime());
		
		// Train over 1000 iterations
		for (int i = 0; i < 1000; i++) {
			// Select random initial values
			int currentState = random.nextInt(numStates);
			
			while (!isFinalState(currentState)) {
				int[] actionsFromCurrentState = possibleActionsFromState(currentState);
				
				// Pick a possible action from current state
				if (actionsFromCurrentState.length == 0) {
					currentState = random.nextInt(numStates);
					continue;
				}
				int index = random.nextInt(actionsFromCurrentState.length);
				int nextState = actionsFromCurrentState[index];
				int nextStateIndex = getNextStateFromAction(nextState, currentState);
				
				// Calculate the Q value to transition from current to next state
				double q = Q[currentState][nextState];
				double qmax = maxQValue(nextStateIndex);
				int r = R[currentState][nextState];
				
				double value = q + alpha * (r + gamma * qmax - q);
				Q[currentState][nextState] = value;
				
				currentState = nextStateIndex;
			}
		}
	}
	
	// Check if a given state is final
	private boolean isFinalState (int state) {
		int y = state / mapWidth;
		int x = state - y * mapWidth;
		return map[y][x] == 2;
	}

	// Gets a list of possible actions to do from a state
	private int[] possibleActionsFromState (int state) {
        ArrayList<Integer> result = new ArrayList<>();
        for (int i = 0; i < numActions; i++)
            if (R[state][i] != -1) result.add(i);

        return result.stream().mapToInt(i -> i).toArray();
    }
	
	// Gets the max Q value of a given next state - for calculating Q values
	private double maxQValue (int nextState) {
        int[] actionsFromState = possibleActionsFromState(nextState);
        
        //the learning rate and eagerness will keep the W value above the lowest reward
        double maxValue = -10;
        for (int nextAction : actionsFromState) {
            double value = Q[nextState][nextAction];
            if (value > maxValue) maxValue = value;
        }
        
        return maxValue;
    }
	
	// Prints out every state policy
	public void printPolicy() {
        System.out.println("\nPrint policy");
        for (int i = 0; i < numStates; i++)
            System.out.println("At state " 
            		+ i + " do action " + getPolicyFromState(i));
    }
	
	// Get what to do from a given state
	public int getPolicyFromState(int state) {
        int[] actionsFromState = possibleActionsFromState(state);

        double maxValue = Double.MIN_VALUE;
        int policyAction = -1;

        // Pick to move to the state that has the maximum Q value
        // returns the action that makes it possible
        for (int nextState : actionsFromState) {
            double value = Q[state][nextState];

            if (value > maxValue) {
                maxValue = value;
                policyAction = nextState;
            }
        }
        
        return policyAction;
    }

	// Prints the Q-table
	public String printQ() {
		StringBuilder sb = new StringBuilder();
        for (int i = 0; i < Q.length; i++) {
            //System.out.print("From state " + i + ":  ");
        	//sb.append(i).append("#");
            for (int j = 0; j < Q[i].length; j++)
            	sb.append(Q[i][j]).append(";");
                //System.out.printf("%6.2f ", (Q[i][j]));
            sb.deleteCharAt(sb.length() - 1);
            sb.append("\n");
            //System.out.println();
        }
        
        return sb.toString();
    }
	
	// Loads a previous calculated Q-table
	public void loadQTable(final String path) throws IOException {
		File f = new File(path);
		BufferedReader br = new BufferedReader(new FileReader(f));
		
		String line;
		ArrayList<double[]> rows = new ArrayList<>();
		while((line = br.readLine()) != null) {
			String[] values = line.split(";");
			double[] parsedValues = new double[values.length];
			for (int i = 0; i < values.length; i++)
				parsedValues[i] = Double.parseDouble(values[i]);
			rows.add(parsedValues);
		}
		br.close();
		
		if (!rows.isEmpty()) {
			Q = new double[rows.size()][rows.get(0).length];
			for (int i = 0; i < rows.size(); i++)
				Q[i] = rows.get(i);
		}
	}
	
	// Get state from position
	public int getStateFromPosition(final double posx, final double posy) {
		int x = (int) posx;
		int y = (int) posy;
		return (y * mapWidth + x);
	}
	
	// Get next state from making an action in a state
	public int getNextStateFromAction(final int action, final int currentState) {
		Vector2d pos = getTilesPositionFromState(currentState);
		int nextState = currentState;
		
		switch (action) {
		case 0: // left
			pos.x -= 1;
			break;
		case 1: // right
			pos.x += 1;
			break;
		case 2: // up
			pos.y -= 1;
			break;
		case 3: // down
			pos.y += 1;
			break;
		}
		
		nextState = getStateFromPosition(pos.x, pos.y);
		return nextState;
	}
	
	// Get tiles position from state
	public Vector2d getTilesPositionFromState(final int state) {
		int y = state / mapWidth;
		int x = state - y * mapWidth;
		return new Vector2d(x, y);
	}
}
