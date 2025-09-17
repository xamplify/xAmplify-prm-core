package com.xtremand.util.dto;

import java.io.Serializable;
import java.util.Date;

import lombok.Data;

@Data
public class VideoDTO implements Serializable {
	


	/**
	 * 
	 */
	private static final long serialVersionUID = -3419681632030220319L;

	private Integer id;

	private Integer companyId;

	private String alias;

	private String title;

	private Integer uploadedUserId;

	private Integer categoryId;

	private String categoryName;

	private Date createdTime;

	private String videoLength;

	private String viewBy;

	private Integer views;

	private String imagePath;

	private String videoPath;

	private boolean isProcessed;

	private String uploadedUserName;

	private String companyName;

	private String gifImagePath;

	private String description;

	
	private String uploadedDate;

	
	private boolean whiteLabeledAssetReceivedFromVendor;

	
	private String whiteLabeledAssetSharedByVendorCompanyName;



}
