package com.xtremand.util.dto;

import java.io.Serializable;
import java.util.List;

import lombok.Data;

@Data
public class ActiveAndInActivePartnerEmailNotificationDTO implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7076196846048875540L;

	private String subject;

	private List<String> activePartners;

	private List<String> inActivePartners;

}
