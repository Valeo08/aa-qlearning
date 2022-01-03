package main.qlearning;

import java.util.ArrayList;

import core.game.Observation;
import core.game.StateObservation;
import ontology.Types.WINNER;

public class Reward {
	
	public static double getReward(StateObservation so) {
		int width = so.getWorldDimension().width;
		int height = so.getWorldDimension().height;
		double maxLimit = (width * width) + (height * height);
		
		double reward = 
				+ 1.0 * getGameScore(so)
				+ 1.0 * getGameOverScore(so)
				+ 2.0 * (1.0 - (getMeanPortalSquareDistance(so) / maxLimit))
				+ 1.0 * (getMeanImmovableSquareDistance(so) / maxLimit);
		
		return reward;
	}
	
	private static double getMeanObservationSquareDistance(ArrayList<Observation>[] obPosList) {
		double sum = 0;
		int count = 0;
		
		if (obPosList == null) return 0;
		
		for (ArrayList<Observation> obsPos : obPosList)
			for (Observation ob : obsPos) {
				sum += ob.sqDist;
				count++;
			}
		
		if (count == 0) return 0;
		
		return sum / count;
	}
	
	private static double getMeanImmovableSquareDistance(StateObservation so) {
		ArrayList<Observation>[] immovablePos =  so.getImmovablePositions(so.getAvatarPosition());
		return getMeanObservationSquareDistance(immovablePos);
	}
	
	private static double getMeanPortalSquareDistance(StateObservation so) {
		ArrayList<Observation>[] portalPos =  so.getPortalsPositions(so.getAvatarPosition());
		return getMeanObservationSquareDistance(portalPos);
	}
	
	private static double getGameScore(StateObservation so) {
		return so.getGameScore();
	}
	
	private static double getGameOverScore(StateObservation so) {
		double gameWinnerScore = 0;
		WINNER gameWinner = so.getGameWinner();
		
		switch (gameWinner) {
		case PLAYER_WINS:
			gameWinnerScore = 1000;
			break;
		case NO_WINNER:
			gameWinnerScore = 0;
			break;
		case PLAYER_LOSES:
		case PLAYER_DISQ:
			gameWinnerScore = -5;
			break;
		}
		
		return gameWinnerScore;
	}

}
