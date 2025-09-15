package com.xtremand.partner.bom;

import org.hibernate.HibernateException;

public class PartnerDataAccessException extends RuntimeException {

	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public PartnerDataAccessException(){
		super();
	}
	
	public PartnerDataAccessException(String message){
		super(message);
	}

	public PartnerDataAccessException(Exception ex){
		super(ex);
	}
	public PartnerDataAccessException(HibernateException ex){
		super(ex);
	}
	
	public PartnerDataAccessException(PartnerDataAccessException ex){
		super(ex);
	}
}
