package com.xtremand.white.labeled.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class DamVideoDTO implements Serializable {
	/**
	* 
	*/
	private static final long serialVersionUID = 8830387704270051071L;

	private Integer damId;

	private Integer companyId;

	private String assetName;

	private String assetType;

	private String assetStatus;

	private Integer videoId;

	private String videoIdAsString;

	private String videoTitle;

	private String videoUri;

	private String description;

	private String videoStatus;

	private String viewBy;

	private String bigThumbnailImage;

	private String imageUri;

	private String videoLength;

	private Integer videoSize;

	private Integer bitRate;

	private String gifUri;

	private boolean processed;

	private boolean customThumbnailUploaded;

	private Integer videoControlId;

	private String playerColor;

	private boolean enableVideoController;

	private String controllerColor;

	private boolean allowSharing;

	private boolean enableSettings;

	private boolean allowFullScreen;

	private boolean allowComments;

	private boolean allowLikes;

	private boolean enableCasting;

	private boolean allowEmbed;

	private Integer transparency;

	private boolean is360Video;

	private boolean defaultSetting;

	private String brandingLogoUri;

	private String brandingLogoDescUri;

	private boolean enableVideoCobrandingLogo;

	private String image1;

	private String image2;

	private String image3;

	private String gif1;

	private String gif2;

	private String gif3;

	private String tagsInString;

	private List<String> tags = new ArrayList<>();

	private Integer callToActionId;

	private boolean callToActionName;

	private boolean skip;

	private String upperText;

	private String lowerText;

	private boolean startOfVideo;

	private boolean endOfVideo;

	private boolean callAction;

	/***** XNFR-255 ****/
	private boolean shareAsWhiteLabeledAsset;

	private String whiteLabeledToolTipMessage;

	private boolean disableWhiteLabelOption;

	/***** XNFR-255 ****/

	private boolean addedToQuickLinks;

	public static long getSerialversionuid() {
		return serialVersionUID;
	}
	
	/** XNFR-884 **/
	private String approvalStatus;
	
	private boolean draft;
	
	private Integer createdBy;
	
	/** XNFR-885 **/
	private Integer id;
	
	private Integer categoriesId;
	
	private Integer customerId;
	
	private String createdTime;
	
	private String updatedTime;
	
	private Integer updatedBy;
	
	private String alias;
	
	private Integer views;

	/** XNFR-955 **/
	private String slug;
	

}
