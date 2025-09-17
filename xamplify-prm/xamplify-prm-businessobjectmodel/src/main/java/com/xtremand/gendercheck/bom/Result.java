package com.xtremand.gendercheck.bom;

public class Result {
    private String name;
    private String gender;
    private String accuracy;
    private String samples;


    public Result(String name, String gender) {
		super();
		this.name = name;
		this.gender = gender;
	}


	public String getName() {
		return name;
	}


	public void setName(String name) {
		this.name = name;
	}


	public String getGender() {
		return gender;
	}


	public void setGender(String gender) {
		this.gender = gender;
	}


	public String getAccuracy() {
		return accuracy;
	}


	public void setAccuracy(String accuracy) {
		this.accuracy = accuracy;
	}


	public String getSamples() {
		return samples;
	}


	public void setSamples(String samples) {
		this.samples = samples;
	}

	@Override
	public String toString() {
		return "Result [name=" + name + ", gender=" + gender + ", accuracy=" + accuracy + ", samples=" + samples + "]";
	}
}
