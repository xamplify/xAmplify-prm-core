package com.xtremand.dam.dto;

import java.io.Serializable;
import java.util.Set;

import lombok.Data;

@Data
public class DamAwsDTO implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -6083465678302463516L;

	private Integer userId;
	
	private Integer damId;
	
	private Integer companyId;
	
	private String completeAssetFileName;
	
	private String copiedAssetFilePath;
	
	private String completeThumbnailFileName;
	
	private String copiedThumbnailFilePath;
	
	private String fileType;
	
	private String assetName;
	
	private Set<Integer> partnerGroupIds;

	private Set<Integer> partnerIds;
	
	private boolean replaceAsset;
	
	
}
