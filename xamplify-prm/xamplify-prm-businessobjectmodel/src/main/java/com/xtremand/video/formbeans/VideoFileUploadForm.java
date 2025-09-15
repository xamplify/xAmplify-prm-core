package com.xtremand.video.formbeans;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.xtremand.dam.dto.DamUploadPostDTO;
import com.xtremand.video.bom.VideoCategory;
import com.xtremand.video.bom.VideoFile;

import lombok.Getter;
import lombok.Setter;

@Component
public class VideoFileUploadForm {
	private Integer id;
	private String title;
	private Integer categoryId;
	private VideoFile.TYPE viewBy;
	private Integer customerId;
	private String imagePath;
	private MultipartFile imageFile;
	private List<String> tags;
	private String description;
	private String gifImagePath;
	private String action;
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
	private boolean name;
	private boolean skip;
	private String upperText;
	private String lowerText;
	private List<String> imageFiles = new ArrayList<String>();
	private List<String> gifFiles = new ArrayList<String>();
	private List<String> viewByOptions = new ArrayList<String>();
	private int videoLength;
	private int bitRate;
	private int width;
	private String videoPath;
	private String error;
	private Integer transparency;
	private List<VideoCategory> categories = new ArrayList<VideoCategory>();
	private boolean startOfVideo;
	private boolean endOfVideo;
	private VideoCategory category;
	private boolean callACtion;
	private String videoStatus;
	private boolean is360video;
	private boolean defaultSetting;
	private Integer views;
	private String brandingLogoUri;
	private String brandingLogoDescUri;
	private boolean enableVideoCobrandingLogo;
	private boolean isProcessed;
	private Integer damId;
	private Integer folderId;
	
	/*********** XNFR-255 ************/
	@Getter
	@Setter
	private boolean shareAsWhiteLabeledAsset;

	@Getter
	@Setter
	private Integer companyId;

	@Getter
	@Setter
	private Set<Integer> partnerGroupIds;

	@Getter
	@Setter
	private boolean partnerGroupSelected;

	@Getter
	@Setter
	private Set<Integer> partnerIds;

	@Getter
	@Setter
	private DamUploadPostDTO damUploadPostDTO;

	/*********** XNFR-255 ************/
	
	@Getter
	@Setter
	private boolean addedToQuickLinks;
	
	@Getter
	@Setter
	private boolean draft;

	@Getter
	@Setter
	private boolean sendForApproval;
	
	@Getter
	@Setter
	private String slug;

	@Getter
	@Setter
	private String companyProfileName;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getFolderId() {
		return folderId;
	}

	public void setFolderId(Integer folderId) {
		this.folderId = folderId;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Integer getCategoryId() {
		return categoryId;
	}

	public void setCategoryId(Integer categoryId) {
		this.categoryId = categoryId;
	}

	public VideoFile.TYPE getViewBy() {
		return viewBy;
	}

	public void setViewBy(VideoFile.TYPE viewBy) {
		this.viewBy = viewBy;
	}

	public Integer getCustomerId() {
		return customerId;
	}

	public void setCustomerId(Integer customerId) {
		this.customerId = customerId;
	}

	public String getImagePath() {
		return imagePath;
	}

	public void setImagePath(String imagePath) {
		this.imagePath = imagePath;
	}

	public MultipartFile getImageFile() {
		return imageFile;
	}

	public void setImageFile(MultipartFile imageFile) {
		this.imageFile = imageFile;
	}

	public List<String> getTags() {
		return tags;
	}

	public void setTags(List<String> tags) {
		this.tags = tags;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getGifImagePath() {
		return gifImagePath;
	}

	public void setGifImagePath(String gifImagePath) {
		this.gifImagePath = gifImagePath;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
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

	public List<String> getViewByOptions() {
		return viewByOptions;
	}

	public void setViewByOptions(List<String> viewByOptions) {
		this.viewByOptions = viewByOptions;
	}

	public int getVideoLength() {
		return videoLength;
	}

	public void setVideoLength(int videoLength) {
		this.videoLength = videoLength;
	}

	public int getBitRate() {
		return bitRate;
	}

	public void setBitRate(int bitRate) {
		this.bitRate = bitRate;
	}

	public String getVideoPath() {
		return videoPath;
	}

	public void setVideoPath(String videoPath) {
		this.videoPath = videoPath;
	}

	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
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

	public boolean isEnableVideoCobrandingLogo() {
		return enableVideoCobrandingLogo;
	}

	public void setEnableVideoCobrandingLogo(boolean enableVideoCobrandingLogo) {
		this.enableVideoCobrandingLogo = enableVideoCobrandingLogo;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public boolean isProcessed() {
		return isProcessed;
	}

	public void setProcessed(boolean isProcessed) {
		this.isProcessed = isProcessed;
	}

	public Integer getDamId() {
		return damId;
	}

	public void setDamId(Integer damId) {
		this.damId = damId;
	}

}
