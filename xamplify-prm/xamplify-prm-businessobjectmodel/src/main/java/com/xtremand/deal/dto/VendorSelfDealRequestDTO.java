package com.xtremand.deal.dto;

import java.io.Serializable;

import lombok.Data;

@Data
public class VendorSelfDealRequestDTO implements Serializable{/**
	 * 
	 */
	private static final long serialVersionUID = -6804513959142623354L;
	
	private Integer loggedInUserId;
	
	private Integer campaignId;

}
