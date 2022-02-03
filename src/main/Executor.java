package main;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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
		int seed;
				
		// Game and level to play
		int gameIdx  = 3;
		String gameName = games[gameIdx][1];
		String game = games[gameIdx][0];
		
		// Q-Learning things
		// Runs the game multiple iterations for learning
		int numIterations = 600;
		int numLevels = 5;
		
		QLearning.NUM_ITERATIONS = numIterations;
		
		States.init();
		
		boolean TRAINING = false;
		
		if (TRAINING) {
			for (int i = 0; i < numLevels; i++) {
				QLearning.ACTUAL_ITERATION = 0;
				
				int levelIndex = i;
				String level = game.replace(gameName, gameName + "_lvl" + levelIndex);
				
				try {
					File f = new File("results/" + gameName + i + ".csv");
					if (f.exists()) f.delete();
					
					BufferedWriter bw = new BufferedWriter(new FileWriter(f));
					bw.append("Iteration,Score\n");
					
					for (int j = 0; j < numIterations; j++) {
						System.out.println("Running iteration " 
								+ (QLearning.ACTUAL_ITERATION + 1) + " on Level " + levelIndex);
						
						seed = new Random(System.nanoTime()).nextInt();
						double[] results = ArcadeMachine.runOneGame(game, level, false, pTrain, null, seed, 0);
						bw.append("" + (j+1)).append("," + results[1]).append("\n");
						
						QLearning.ACTUAL_ITERATION++;
					}
					
					bw.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} else {
			// Calculate an average of score over a number of tries (for validation)
			String filename = gameName + "_test1";
			String levelTest = game.replace(gameName, filename);
			int tries = 1000;
			int wins = 0;
			
			double mean = 0;
			for (int i = 0; i < tries; i++) {
				seed = new Random(System.nanoTime()).nextInt();
				double[] results = ArcadeMachine.runOneGame(game, levelTest, false, pTest, null, seed, 0);
				mean += results[1];
				if (results[0] == 1) wins++;
			}
			mean = mean / tries;
			System.out.println("-----------------------------------------------------------------------");
			System.out.println("Average score with " + tries + " tries: " + mean);
			System.out.println("The agent has won " + wins + " of " + tries + " games.");
		}
		
		System.exit(0);
    }
	
}
