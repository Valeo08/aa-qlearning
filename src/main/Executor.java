package main;

import java.util.Random;

import tools.Utils;
import tracks.ArcadeMachine;

public class Executor {

	public static void main(String[] args) {
		// Q-Learning things - For generating the Q-table
		/*
		String map = "map-camel0.dat";
		String table = "q-table.dat";
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
		*/
		
		
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
		int levelIdx = 0; // level names from 0 to 4 (game_lvlN.txt).
		
		String gameName = games[gameIdx][1];
		String game = games[gameIdx][0];
		String level1 = game.replace(gameName, gameName + "_lvl" + levelIdx);

		
		// 1. This starts a game, in a level, played by a human.
		//ArcadeMachine.playOneGame(game, level1, null, seed);

		// 2. This plays a game in a level by the controller.
		ArcadeMachine.runOneGame(game, level1, visuals, p0, null, seed, 0);
				
		//System.exit(0);
    }
	
}
