package main;

import java.util.Random;

import main.qlearning.QLearning;
import main.qlearning.States;
import tools.Utils;
import tracks.ArcadeMachine;

public class Executor {

	public static void main(String[] args) {
		// GVGAI Execution things
		
		// Agents
		String pTrain = "main.agents.TrainerAgent";
		String pTest = "main.agents.TestAgent";
		
		//Load available games
		String spGamesCollection = "examples/all_games_sp.csv";
		String[][] games = Utils.readGames(spGamesCollection);

		//Game settings
		int seed = new Random().nextInt();
				
		// Game and level to play
		int gameIdx  = 15;
		String gameName = games[gameIdx][1];
		String game = games[gameIdx][0];

		// Q-Learning things
		// Runs the game multiple iterations for learning
		int numIterations = 500;
		int numLevels = 10;
		Random random;
		
		QLearning.NUM_ITERATIONS = numIterations;
		States.init();
		
		boolean TRAINING = false;
		
		if (TRAINING) {
			for (int i = 0; i < numLevels; i++) {
				QLearning.ACTUAL_ITERATION = 0;
				for (int j = 0; j < numIterations; j++) {
					random = new Random(System.nanoTime());
					int levelIndex = random.nextInt(i + 1);
					System.out.println("Running iteration " 
							+ (QLearning.ACTUAL_ITERATION + 1) + " on Level " + levelIndex);
					String level = game.replace(gameName, gameName + "_lvl" + levelIndex);
					ArcadeMachine.runOneGame(game, level, false, pTrain, null, seed, 0);
					QLearning.ACTUAL_ITERATION++;
				}	
			}
		} else {
			String levelTest = game.replace(gameName, gameName + "_test1");
			ArcadeMachine.runOneGame(game, levelTest, true, pTest, null, seed, 0);
		}
		
		System.exit(0);
    }
	
}
