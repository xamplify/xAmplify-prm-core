package com.xtremand.company.bom;

public enum CompanySource {
	XAMPLIFY("XAMPLIFY"), CONNECTWISE("CONNECTWISE");
	protected String type;
	private CompanySource(String type) {
		this.type = type;
	}
	public String getType() {
		return type;
	}
}
