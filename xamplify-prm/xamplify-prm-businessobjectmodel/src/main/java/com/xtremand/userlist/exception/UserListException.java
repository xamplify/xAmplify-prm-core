package com.xtremand.userlist.exception;

public class UserListException extends RuntimeException{
	private static final long serialVersionUID = 4257488507771758213L;

	public UserListException() {
		super();
	}

	public UserListException(String arg0, Throwable arg1, boolean arg2, boolean arg3) {
		super(arg0, arg1, arg2, arg3);
	}

	public UserListException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

	public UserListException(String arg0) {
		super(arg0);
	}

	public UserListException(Throwable arg0) {
		super(arg0);
	}
	
}
