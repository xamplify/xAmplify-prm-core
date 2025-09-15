package com.xtremand.social.formbeans;

public class UserPassword {
	
	private String oldPassword;
	
	private String newPassword;
	
	private Integer userId;

	public String getOldPassword() {
		return oldPassword;
	}

	public void setOldPassword(String oldPassword) {
		this.oldPassword = oldPassword;
	}

	public String getNewPassword() {
		return newPassword;
	}

	public void setNewPassword(String newPassword) {
		this.newPassword = newPassword;
	}

	@Override
	public String toString() {
		return "UserPassword [oldPassword=" + oldPassword + ", newPassword=" + newPassword + ", userId=" + userId + "]";
	}

	public Integer getUserId() {
		return userId;
	}

	public void setUserId(Integer userId) {
		this.userId = userId;
	}
	
	
}
