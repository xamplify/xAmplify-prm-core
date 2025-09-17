package com.xtremand.views;

import java.io.Serializable;
import java.math.BigInteger;

import com.xtremand.util.dto.CreatedTimeConverter;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
public class ContactAndPartnerListView extends CreatedTimeConverter implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private Integer id;
	
	private String name;
	
	private Integer companyId;
	
	private Integer createdBy;
	
	private BigInteger count;
	
	private String listType;
	
	private boolean publicList;
	
	private boolean partnerList;
	
	private boolean emailValidationInd;
	
	private String creatorName;
	
	private String socialNetwork;
	
	private boolean formList;
	
	private boolean companyList;
	
	private boolean synchronisedList;
}