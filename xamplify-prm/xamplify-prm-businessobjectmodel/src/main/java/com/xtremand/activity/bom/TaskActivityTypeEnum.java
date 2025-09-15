package com.xtremand.activity.bom;

public enum TaskActivityTypeEnum {

	NOTE("NOTE"),EMAIL("EMAIL"),TODO("TODO");
	
	protected String taskType;
	
	private TaskActivityTypeEnum(String taskType) {
		this.taskType = taskType;
	}
	
	public String getTaskType() {
		return taskType;
	}
}
