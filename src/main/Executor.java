package main;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

import main.qlearning.QLearning;
import tools.Utils;
import tracks.ArcadeMachine;

public class Executor {

	public static void main(String[] args) {
		// GVGAI Execution things
		String p0 = "main.qlearning.QLearner";
		
		//Load available games
		String spGamesCollection = "examples/all_games_sp.csv";
		String[][] games = Utils.readGames(spGamesCollection);

		//Game settings
		boolean visuals = true;
		int seed = new Random().nextInt();
				
		// Game and level to play
		int gameIdx  = 15;
		int levelIdx = 1; // level names from 0 to 4 (game_lvlN.txt).
		
		String gameName = games[gameIdx][1];
		String game = games[gameIdx][0];
		String level1 = game.replace(gameName, gameName + "_lvl" + levelIdx);

		
		// Q-Learning things - For generating the Q-table
		String map = "map-camel" + levelIdx + ".dat";
		String table = "q-table-camel" + levelIdx + ".dat";
		QLearning learn;
		
		try {
			learn = new QLearning(map);
			
			// To generate the Q-table for the map
			learn.init();
			learn.calculateQ();
			
			// Save Q-table to file
			File f = new File(table);
			if (f.exists()) f.delete();
			BufferedWriter bw = new BufferedWriter(new FileWriter(f));
			bw.write(learn.printQ());
			bw.close();
		} catch (IOException ex) {
			System.err.println(ex.getMessage());
		}

		// Runs the game
		ArcadeMachine.runOneGame(game, level1, visuals, p0, null, seed, 0);
				
		//System.exit(0);
    }
	
}
