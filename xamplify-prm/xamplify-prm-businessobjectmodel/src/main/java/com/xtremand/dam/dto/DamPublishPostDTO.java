package com.xtremand.dam.dto;

import java.io.Serializable;
import java.util.List;

import com.xtremand.white.labeled.dto.DamVideoDTO;

import lombok.Data;

@Data
public class DamPublishPostDTO implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2052736002915719675L;

	private Integer damId;

	private List<Integer> partnershipIds;

	private List<Integer> partnerIds;

	private List<Integer> partnerGroupIds;

	private Integer publishedBy;

	private boolean partnerGroupSelected;

	private List<Integer> updatedPartnerIds;

	/******* XNFR-255 ********/
	private boolean sharedWithPartnersAsAWhiteLabeledAsset;

	private DamVideoDTO damVideoDTO;

	private boolean assetSharedEmailNotification;

}
