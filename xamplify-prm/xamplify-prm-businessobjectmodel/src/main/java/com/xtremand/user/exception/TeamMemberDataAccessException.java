package com.xtremand.user.exception;

import org.hibernate.HibernateException;

public class TeamMemberDataAccessException extends RuntimeException {
	
	
	private static final long serialVersionUID = 4257488507771758213L;
	
	public TeamMemberDataAccessException(){
		super();
	}
	
	public TeamMemberDataAccessException(String message){
		super(message);
	}
	
	public TeamMemberDataAccessException(Exception ex){
		super(ex);
	}
	public TeamMemberDataAccessException(HibernateException ex){
		super(ex);
	}
	
	public TeamMemberDataAccessException(TeamMemberDataAccessException ex){
		super(ex);
	}
}
