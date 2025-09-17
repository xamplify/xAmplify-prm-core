package com.xtremand.dam.dto;

import java.io.Serializable;
import java.util.Date;

import lombok.Data;

@Data
public class DamBasicInfo implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3331704255101763076L;

	private Integer damId;

	private String assetName;

	private String companyName;

	private boolean whiteLabeledAssetSharedWithPartners;

	private Integer videoId;

	private Date publishedTime;

}
