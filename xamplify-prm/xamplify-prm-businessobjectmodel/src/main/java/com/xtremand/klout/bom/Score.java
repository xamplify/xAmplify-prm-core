package com.xtremand.klout.bom;

import java.io.Serializable;

public class Score implements Serializable{
	private static final long serialVersionUID = 1L;
	private String bucket;
	private String score;
	private ScoreDeltas scoreDelta;
	
	private String screenName;
	
	public String getBucket() {
		return bucket;
	}
	public void setBucket(String bucket) {
		this.bucket = bucket;
	}
	public String getScore() {
		return score;
	}
	public void setScore(String score) {
		this.score = score;
	}
	public ScoreDeltas getScoreDelta() {
		return scoreDelta;
	}
	public void setScoreDelta(ScoreDeltas scoreDelta) {
		this.scoreDelta = scoreDelta;
	}
}
