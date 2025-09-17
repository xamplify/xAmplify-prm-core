package com.xtremand.log.bom;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class VideoStats {
	
	private List<Integer> views = new ArrayList<Integer>();
	private List<Double > minutesWatched = new ArrayList<Double >();
	private List<Double > averageDuration = new ArrayList<Double >();
	HashMap<Integer, String> dates = new HashMap<Integer, String>();
	
	private Integer videoId;
	private String videoTitle;
	private Integer viewsCount;
	private Double minutesWatchedValue;
	private Integer selectedDate;
	private String emailId;
	private String date;
	private String firstName;
	private String lastName;
	
	public List<Integer> getViews() {
		return views;
	}
	public void setViews(List<Integer> views) {
		this.views = views;
	}
	public List<Double> getMinutesWatched() {
		return minutesWatched;
	}
	public void setMinutesWatched(List<Double> minutesWatched) {
		this.minutesWatched = minutesWatched;
	}
	public List<Double> getAverageDuration() {
		return averageDuration;
	}
	public void setAverageDuration(List<Double> averageDuration) {
		this.averageDuration = averageDuration;
	}
	public HashMap<Integer, String> getDates() {
		return dates;
	}
	public void setDates(HashMap<Integer, String> dates) {
		this.dates = dates;
	}
	public Integer getVideoId() {
		return videoId;
	}
	public void setVideoId(Integer videoId) {
		this.videoId = videoId;
	}
	public String getVideoTitle() {
		return videoTitle;
	}
	public void setVideoTitle(String videoTitle) {
		this.videoTitle = videoTitle;
	}
	public Integer getViewsCount() {
		return viewsCount;
	}
	public void setViewsCount(Integer viewsCount) {
		this.viewsCount = viewsCount;
	}
	public Double getMinutesWatchedValue() {
		return minutesWatchedValue;
	}
	public void setMinutesWatchedValue(Double minutesWatchedValue) {
		this.minutesWatchedValue = minutesWatchedValue;
	}
	public Integer getSelectedDate() {
		return selectedDate;
	}
	public void setSelectedDate(Integer selectedDate) {
		this.selectedDate = selectedDate;
	}
	public String getEmailId() {
		return emailId;
	}
	public void setEmailId(String emailId) {
		this.emailId = emailId;
	}
	public String getDate() {
		return date;
	}
	public void setDate(String date) {
		this.date = date;
	}
	public String getFirstName() {
		return firstName;
	}
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}
	public String getLastName() {
		return lastName;
	}
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
}
