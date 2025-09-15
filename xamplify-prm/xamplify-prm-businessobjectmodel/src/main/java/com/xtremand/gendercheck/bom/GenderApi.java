package com.xtremand.gendercheck.bom;

import java.util.List;

public class GenderApi {
	private List<Result> result;
	private String duration;
	private String name;

	public List<Result> getResult() {
		return result;
	}

	public void setResult(List<Result> result) {
		this.result = result;
	}

	public String getDuration() {
		return duration;
	}

	public void setDuration(String duration) {
		this.duration = duration;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return "GenderApi [result=" + result + ", duration=" + duration + ", name=" + name + "]";
	}

}
