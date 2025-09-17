package com.xtremand.campaign.dto;

import java.io.Serializable;

import lombok.Data;

@Data
public class OneClickLaunchSharedLeadsDTO implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8447585759309673929L;

	private Integer userListId;

	private Integer partnershipId;

	private String userListName;

}
