package com.xtremand.util.dto;

import java.io.Serializable;

import lombok.Data;

@Data
public class DeletedPartnerDTO implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 6487461506902310344L;

	private boolean onlyUser;
	
	private boolean deletedPartnerCompanyUser;

}
