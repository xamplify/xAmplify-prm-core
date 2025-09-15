package com.xtremand.activity.bom;

public enum TaskActivityPriorityEnum {

	LOW("LOW"),MEDIUM("MEDIUM"),HIGH("MEDIUM");
	
	protected String taskPriority;
	
	private TaskActivityPriorityEnum(String taskPriority) {
		this.taskPriority = taskPriority;
	}
	
	public String getTaskPriority() {
		return taskPriority;
	}
}
