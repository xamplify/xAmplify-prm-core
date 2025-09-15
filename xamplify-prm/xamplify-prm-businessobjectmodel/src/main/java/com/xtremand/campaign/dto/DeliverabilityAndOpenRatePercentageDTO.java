package com.xtremand.campaign.dto;

import java.io.Serializable;

import lombok.Data;

@Data
public class DeliverabilityAndOpenRatePercentageDTO implements Serializable {
	/**
	* 
	*/
	private static final long serialVersionUID = -1626025921473397185L;

	private String openRate;

	private String delivered;

}
