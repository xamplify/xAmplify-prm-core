package com.xtremand.klout.bom;

public class Payload {
	private ScoreDeltas scoreDeltas;
    private String nick;
    private Score score;
    private String kloutId;
    
	public ScoreDeltas getScoreDeltas() {
		return scoreDeltas;
	}
	public void setScoreDeltas(ScoreDeltas scoreDeltas) {
		this.scoreDeltas = scoreDeltas;
	}
	public String getNick() {
		return nick;
	}
	public void setNick(String nick) {
		this.nick = nick;
	}
	public Score getScore() {
		return score;
	}
	public void setScore(Score score) {
		this.score = score;
	}
	public String getKloutId() {
		return kloutId;
	}
	public void setKloutId(String kloutId) {
		this.kloutId = kloutId;
	}
}
