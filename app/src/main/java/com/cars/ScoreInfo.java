package com.cars;

public final class ScoreInfo {
	public final String playerName;
	public final int score;
	
	public ScoreInfo(String playerName, int score) {
		if(playerName == null)
			throw new NullPointerException("playerName");
		this.playerName = playerName;
		this.score = score;
	}
}
