package com.xtremand.klout.bom;

import java.io.Serializable;

public class ScoreDeltas implements Serializable{
	private static final long serialVersionUID = 1L;
	private String dayChange;
    private String weekChange;
    private String monthChange;
	public String getDayChange() {
		return dayChange;
	}
	public void setDayChange(String dayChange) {
		this.dayChange = dayChange;
	}
	public String getWeekChange() {
		return weekChange;
	}
	public void setWeekChange(String weekChange) {
		this.weekChange = weekChange;
	}
	public String getMonthChange() {
		return monthChange;
	}
	public void setMonthChange(String monthChange) {
		this.monthChange = monthChange;
	}
}
