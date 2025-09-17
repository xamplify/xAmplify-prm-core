package com.xtremand.util.dto;

import java.io.Serializable;
import java.util.Set;

import lombok.Data;

@Data
public class ShareContentRequestDTO implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7040438895306762301L;

	private Integer userListId;

	private Set<Integer> campaignIds;

	private Set<PartnerOrContactInputDTO> partnersOrContactDtos;

	private Integer loggedInUserId;

	private String type;

	private Set<Integer> damIds;

	private Set<Integer> trackOrPlaybookIds;

	private boolean publishingToPartnerList;

	private Integer partnershipId;

}
