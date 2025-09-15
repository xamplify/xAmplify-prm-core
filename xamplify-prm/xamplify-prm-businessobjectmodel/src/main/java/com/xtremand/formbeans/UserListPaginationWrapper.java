package com.xtremand.formbeans;

import com.xtremand.common.bom.Pagination;

public class UserListPaginationWrapper {
	private UserListDTO userList;
	private Pagination pagination;
	
	public UserListDTO getUserList() {
		return userList;
	}
	public void setUserList(UserListDTO userList) {
		this.userList = userList;
	}
	public Pagination getPagination() {
		return pagination;
	}
	public void setPagination(Pagination pagination) {
		this.pagination = pagination;
	}
}
