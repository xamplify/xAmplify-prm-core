package com.xtremand.mdf.bom;

public enum MdfAmountType {

	FUND_ADDED("FUND_ADDED"), FUND_REMOVED("FUND_REMOVED");

	
	protected String type;

	private MdfAmountType(String type) {
		this.type = type;
	}

	
	public String getType() {
		return type;
	}

}
