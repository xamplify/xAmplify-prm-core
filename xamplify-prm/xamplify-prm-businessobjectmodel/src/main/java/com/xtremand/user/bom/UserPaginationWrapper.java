package com.xtremand.user.bom;

import com.xtremand.common.bom.Pagination;

public class UserPaginationWrapper {
	private Pagination pagination;
	private User user;
	public Pagination getPagination() {
		return pagination;
	}
	public void setPagination(Pagination pagination) {
		this.pagination = pagination;
	}
	public User getUser() {
		return user;
	}
	public void setUser(User user) {
		this.user = user;
	}
	@Override
	public String toString() {
		return "UserPaginationWrapper [pagination=" + pagination + ", userId=" + user.getUserId() + ", emailId="+user.getEmailId()+"]";
	}
}
