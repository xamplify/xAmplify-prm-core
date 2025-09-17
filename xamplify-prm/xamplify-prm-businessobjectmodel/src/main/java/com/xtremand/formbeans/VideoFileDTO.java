package com.xtremand.formbeans;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.xtremand.video.bom.VideoCategory;
import com.xtremand.video.bom.VideoImage;

import lombok.Getter;
import lombok.Setter;

public class VideoFileDTO implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Integer id;
	private String title;
	private String uploadedDate;
	private String uploadedBy;
	private String uploadedUserName;
	private Integer uploadedUserId;
	private String imagePath;
	private String description;
	private String videoLength;
	private String alias;
	private String videoPath;
	private String viewBy;
	private String gifImagePath;
	private Integer categoryId;
	private List<String> tags = new ArrayList<String>(0);
	private VideoImage videoImage;
	List<String> imageFiles = new ArrayList<String>();
	List<String> gifFiles = new ArrayList<String>();;
	private String playerColor;
	private boolean enableVideoController;
	private String controllerColor;
	private boolean allowSharing;
	private boolean enableSettings;
	private boolean allowFullscreen;
	private boolean allowComments;
	private boolean allowLikes;
	private boolean enableCasting;
	private boolean allowEmbed;
	private String videoStatus;
	private boolean is360video;
	private boolean defaultSetting;
	private boolean enableVideoCobrandingLogo;

	private boolean name;
	private boolean skip;
	private String upperText;
	private String lowerText;
	private List<VideoCategory> categories = new ArrayList<VideoCategory>();
	private Integer transparency;
	private boolean startOfVideo;
	private boolean endOfVideo;
	private VideoCategory category;
	private boolean callACtion;
	private Integer views;

	private Integer leads;
	private Integer favourites;
	private Integer comments;
	private Integer likes;
	private Integer dislikes;

	private Float watchedFully;

	private String shortenerURLAlias;

	private String brandingLogoUri;
	private String brandingLogoDescUri;
	private String companyName;
	private boolean isProcessed;
	private boolean access;
	private Integer folderId;

	/***** XNFR-255 ****/
	@Getter
	@Setter
	private boolean shareAsWhiteLabeledAsset;

	@Getter
	@Setter
	private String whiteLabeledToolTipMessage;

	@Getter
	@Setter
	private boolean disableWhiteLabelOption;

	@Getter
	@Setter
	private Integer damId;

	@Getter
	@Setter
	private boolean whiteLabeledAssetReceivedFromVendor;

	@Getter
	@Setter
	private String whiteLabeledAssetSharedByVendorCompanyName;

	@Getter
	@Setter
	private boolean publishedToPartnerGroups;

	@Getter
	@Setter
	private List<Integer> partnerGroupIds;

	@Getter
	@Setter
	private List<Integer> partnerIds;

	@Getter
	@Setter
	private List<Integer> partnershipIds;

	@Getter
	@Setter
	private boolean published;

	/***** XNFR-255 ****/

	@Getter
	@Setter
	private boolean addedToQuickLinks;
	
	@Getter
	@Setter
	private String approvalStatus;
	

	@Getter
	@Setter
	private boolean createdByAnyApprover;
	
	@Getter
	@Setter
	private String slug;
	

	public Integer getFolderId() {
		return folderId;
	}

	public void setFolderId(Integer folderId) {
		this.folderId = folderId;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getUploadedDate() {
		return uploadedDate;
	}

	public void setUploadedDate(String uploadedDate) {
		this.uploadedDate = uploadedDate;
	}

	public String getUploadedBy() {
		return uploadedBy;
	}

	public void setUploadedBy(String uploadedBy) {
		this.uploadedBy = uploadedBy;
	}

	public String getVideoLength() {
		return videoLength;
	}

	public void setVideoLength(String videoLength) {
		this.videoLength = videoLength;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

	public String getViewBy() {
		return viewBy;
	}

	public void setViewBy(String viewBy) {
		this.viewBy = viewBy;
	}

	public String getGifImagePath() {
		return gifImagePath;
	}

	public void setGifImagePath(String gifImagePath) {
		this.gifImagePath = gifImagePath;
	}

	public Integer getCategoryId() {
		return categoryId;
	}

	public void setCategoryId(Integer categoryId) {
		this.categoryId = categoryId;
	}

	public VideoImage getVideoImage() {
		return videoImage;
	}

	public void setVideoImage(VideoImage videoImage) {
		this.videoImage = videoImage;
	}

	public List<String> getImageFiles() {
		return imageFiles;
	}

	public void setImageFiles(List<String> imageFiles) {
		this.imageFiles = imageFiles;
	}

	public List<String> getGifFiles() {
		return gifFiles;
	}

	public void setGifFiles(List<String> gifFiles) {
		this.gifFiles = gifFiles;
	}

	public String getImagePath() {
		return imagePath;
	}

	public void setImagePath(String imagePath) {
		this.imagePath = imagePath;
	}

	public String getVideoPath() {
		return videoPath;
	}

	public void setVideoPath(String videoPath) {
		this.videoPath = videoPath;
	}

	public String getPlayerColor() {
		return playerColor;
	}

	public void setPlayerColor(String playerColor) {
		this.playerColor = playerColor;
	}

	public boolean isEnableVideoController() {
		return enableVideoController;
	}

	public void setEnableVideoController(boolean enableVideoController) {
		this.enableVideoController = enableVideoController;
	}

	public String getControllerColor() {
		return controllerColor;
	}

	public void setControllerColor(String controllerColor) {
		this.controllerColor = controllerColor;
	}

	public boolean isAllowSharing() {
		return allowSharing;
	}

	public void setAllowSharing(boolean allowSharing) {
		this.allowSharing = allowSharing;
	}

	public boolean isEnableSettings() {
		return enableSettings;
	}

	public void setEnableSettings(boolean enableSettings) {
		this.enableSettings = enableSettings;
	}

	public boolean isAllowFullscreen() {
		return allowFullscreen;
	}

	public void setAllowFullscreen(boolean allowFullscreen) {
		this.allowFullscreen = allowFullscreen;
	}

	public boolean isAllowComments() {
		return allowComments;
	}

	public void setAllowComments(boolean allowComments) {
		this.allowComments = allowComments;
	}

	public boolean isAllowLikes() {
		return allowLikes;
	}

	public void setAllowLikes(boolean allowLikes) {
		this.allowLikes = allowLikes;
	}

	public boolean isEnableCasting() {
		return enableCasting;
	}

	public void setEnableCasting(boolean enableCasting) {
		this.enableCasting = enableCasting;
	}

	public boolean isAllowEmbed() {
		return allowEmbed;
	}

	public void setAllowEmbed(boolean allowEmbed) {
		this.allowEmbed = allowEmbed;
	}

	public boolean isName() {
		return name;
	}

	public void setName(boolean name) {
		this.name = name;
	}

	public boolean isSkip() {
		return skip;
	}

	public void setSkip(boolean skip) {
		this.skip = skip;
	}

	public String getUpperText() {
		return upperText;
	}

	public void setUpperText(String upperText) {
		this.upperText = upperText;
	}

	public String getLowerText() {
		return lowerText;
	}

	public void setLowerText(String lowerText) {
		this.lowerText = lowerText;
	}

	public List<String> getTags() {
		return tags;
	}

	public void setTags(List<String> tags) {
		this.tags = tags;
	}

	public List<VideoCategory> getCategories() {
		return categories;
	}

	public void setCategories(List<VideoCategory> categories) {
		this.categories = categories;
	}

	public Integer getTransparency() {
		return transparency;
	}

	public void setTransparency(Integer transparency) {
		this.transparency = transparency;
	}

	public boolean isStartOfVideo() {
		return startOfVideo;
	}

	public void setStartOfVideo(boolean startOfVideo) {
		this.startOfVideo = startOfVideo;
	}

	public boolean isEndOfVideo() {
		return endOfVideo;
	}

	public void setEndOfVideo(boolean endOfVideo) {
		this.endOfVideo = endOfVideo;
	}

	public VideoCategory getCategory() {
		return category;
	}

	public void setCategory(VideoCategory category) {
		this.category = category;
	}

	public boolean isCallACtion() {
		return callACtion;
	}

	public void setCallACtion(boolean callACtion) {
		this.callACtion = callACtion;
	}

	public String getVideoStatus() {
		return videoStatus;
	}

	public void setVideoStatus(String videoStatus) {
		this.videoStatus = videoStatus;
	}

	public boolean isIs360video() {
		return is360video;
	}

	public void setIs360video(boolean is360video) {
		this.is360video = is360video;
	}

	public boolean isDefaultSetting() {
		return defaultSetting;
	}

	public void setDefaultSetting(boolean defaultSetting) {
		this.defaultSetting = defaultSetting;
	}

	public Integer getViews() {
		return views;
	}

	public void setViews(Integer views) {
		this.views = views;
	}

	public Integer getLeads() {
		return leads;
	}

	public void setLeads(Integer leads) {
		this.leads = leads;
	}

	public Integer getFavourites() {
		return favourites;
	}

	public void setFavourites(Integer favourites) {
		this.favourites = favourites;
	}

	public Integer getComments() {
		return comments;
	}

	public void setComments(Integer comments) {
		this.comments = comments;
	}

	public Integer getLikes() {
		return likes;
	}

	public void setLikes(Integer likes) {
		this.likes = likes;
	}

	public Integer getDislikes() {
		return dislikes;
	}

	public void setDislikes(Integer dislikes) {
		this.dislikes = dislikes;
	}

	public Float getWatchedFully() {
		return watchedFully;
	}

	public void setWatchedFully(Float watchedFully) {
		this.watchedFully = watchedFully;
	}

	public Integer getUploadedUserId() {
		return uploadedUserId;
	}

	public void setUploadedUserId(Integer uploadedUserId) {
		this.uploadedUserId = uploadedUserId;
	}

	public String getUploadedUserName() {
		return uploadedUserName;
	}

	public void setUploadedUserName(String uploadedUserName) {
		this.uploadedUserName = uploadedUserName;
	}

	public String getShortenerURLAlias() {
		return shortenerURLAlias;
	}

	public void setShortenerURLAlias(String shortenerURLAlias) {
		this.shortenerURLAlias = shortenerURLAlias;
	}

	public String getBrandingLogoUri() {
		return brandingLogoUri;
	}

	public void setBrandingLogoUri(String brandingLogoUri) {
		this.brandingLogoUri = brandingLogoUri;
	}

	public String getBrandingLogoDescUri() {
		return brandingLogoDescUri;
	}

	public void setBrandingLogoDescUri(String brandingLogoDescUri) {
		this.brandingLogoDescUri = brandingLogoDescUri;
	}

	public String getCompanyName() {
		return companyName;
	}

	public void setCompanyName(String companyName) {
		this.companyName = companyName;
	}

	public boolean isEnableVideoCobrandingLogo() {
		return enableVideoCobrandingLogo;
	}

	public void setEnableVideoCobrandingLogo(boolean enableVideoCobrandingLogo) {
		this.enableVideoCobrandingLogo = enableVideoCobrandingLogo;
	}

	public boolean isProcessed() {
		return isProcessed;
	}

	public void setProcessed(boolean isProcessed) {
		this.isProcessed = isProcessed;
	}

	public boolean isAccess() {
		return access;
	}

	public void setAccess(boolean access) {
		this.access = access;
	}

}
