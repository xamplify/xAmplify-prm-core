package com.xtremand.lead.bom;

public enum PipelineType {
	LEAD("LEAD"), DEAL("DEAL");
	protected String type;
	private PipelineType(String type) {
		this.type = type;
	}
	public String getType() {
		return type;
	}
}
