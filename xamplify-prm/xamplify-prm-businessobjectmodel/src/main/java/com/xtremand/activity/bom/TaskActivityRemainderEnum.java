package com.xtremand.activity.bom;

public enum TaskActivityRemainderEnum {
	
	BEFORE30MIN("BEFORE30MIN"),BEFORE1HOUR("BEFORE1HOUR"),BEFORE1DAY("BEFORE1DAY"),BEFORE1WEEK("BEFORE1WEEK"),CUSTOMDATE("CUSTOMDATE");
	
	protected String taskRemainder;
	
	private TaskActivityRemainderEnum(String taskRemainder) {
		this.taskRemainder = taskRemainder;
	}
	
	public String getTaskRemainder() {
		return taskRemainder;
	}
}
