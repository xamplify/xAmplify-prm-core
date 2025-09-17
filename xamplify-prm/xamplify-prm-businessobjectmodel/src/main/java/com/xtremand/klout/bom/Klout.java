package com.xtremand.klout.bom;

import java.util.List;

public class Klout {
	private Integer myInfluencersCount;
    private Integer myInfluenceesCount;
    private List<Influence> myInfluencees;
    private List<Influence> myInfluencers;
	public Integer getMyInfluencersCount() {
		return myInfluencersCount;
	}
	public void setMyInfluencersCount(Integer myInfluencersCount) {
		this.myInfluencersCount = myInfluencersCount;
	}
	public Integer getMyInfluenceesCount() {
		return myInfluenceesCount;
	}
	public void setMyInfluenceesCount(Integer myInfluenceesCount) {
		this.myInfluenceesCount = myInfluenceesCount;
	}
	public List<Influence> getMyInfluencees() {
		return myInfluencees;
	}
	public void setMyInfluencees(List<Influence> myInfluencees) {
		this.myInfluencees = myInfluencees;
	}
	public List<Influence> getMyInfluencers() {
		return myInfluencers;
	}
	public void setMyInfluencers(List<Influence> myInfluencers) {
		this.myInfluencers = myInfluencers;
	}
    
}
