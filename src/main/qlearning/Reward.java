package main.qlearning;

import java.util.ArrayList;

import core.game.Observation;
import core.game.StateObservation;
import ontology.Types.WINNER;

public class Reward {
	
	public static double getReward(StateObservation so) {
		int width = so.getWorldDimension().width;
		int height = so.getWorldDimension().height;
		double maxLimit = width * height;
		
		double reward =
				+ 1.0 * getGameOverScore(so)
				+ 1.0 * getGameScore(so)
				+ 5.0 * (1.0 - (getMeanNPCSquareDistance(so) / maxLimit));
		
		return reward;
	}
	
	private static double getMeanObservationSquareDistance(ArrayList<Observation> obPosList) {
		double sum = 0;
		int count = 0;
		
		if (obPosList == null) return 0;
		
		for (Observation ob : obPosList) {
			sum += ob.sqDist;
			count++;
		}
			
		if (count == 0) return 0;
		
		return sum / count;
	}
	
	private static double getMeanNPCSquareDistance(StateObservation so) {
		ArrayList<Observation>[] AllNPCPos =  so.getNPCPositions(so.getAvatarPosition());
		ArrayList<Observation> friendlyNPCPos = new ArrayList<>();
		
		for (ArrayList<Observation> al : AllNPCPos)
			for (Observation ob : al)
				if (ob.category == 3 && (ob.itype == 4)) // itype -> Normal = 6; Molestos = 4
					friendlyNPCPos.add(ob);
		
		return getMeanObservationSquareDistance(friendlyNPCPos);
	}
	
	private static double getGameScore(StateObservation so) {
		return so.getGameScore() / 10;
	}
	
	private static double getGameOverScore(StateObservation so) {
		double gameWinnerScore = 0;
		WINNER gameWinner = so.getGameWinner();
		
		switch (gameWinner) {
		case PLAYER_WINS:
			gameWinnerScore = 100;
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
