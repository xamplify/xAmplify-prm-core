package com.xtremand.video.bom;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name="xt_video_control")
public class VideoControl implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -6886486228209426301L;
	@GenericGenerator(name = "generator", strategy = "foreign", parameters = @Parameter(name = "property", value = "videoFile"))
	@Id
	@GeneratedValue(generator = "generator")
	@Column(name = "id", unique = true, nullable = false)
	private Integer id;
	@Column(name="player_color")
	private String playerColor;
	@Column(name="enable_videocontroller")
	private boolean enableVideoController;
	@Column(name="controller_color")
	private String controllerColor;
	@Column(name="allow_sharing")
	private boolean allowSharing;
	@Column(name="enable_settings")
	private boolean enableSettings;
	@Column(name="allow_fullscreen")
	private boolean allowFullscreen;
	@Column(name="allow_comments")
	private boolean allowComments;
	@Column(name="allow_likes")
	private boolean allowLikes;
	@Column(name="enable_casting")
	private boolean enableCasting;
	@Column(name="allow_embed")
	private boolean  allowEmbed;
	@Column(name="transparency")
	private Integer transparency;
	@OneToOne(fetch = FetchType.LAZY)
	@PrimaryKeyJoinColumn
	private VideoFile videoFile;
	@Column(name="is_360video")
	private boolean is360video;
	@Column(name="default_setting")
	private boolean defaultSetting;
	@Column(name="branding_logo_uri")
	private String brandingLogoUri;
	@Column(name="branding_logo_desc_uri")
	private String brandingLogoDescUri;
	@Column(name="enable_video_cobranding_logo")
	private boolean enableVideoCobrandingLogo;

	public VideoControl(){

	}

	public VideoControl(String playerColor, boolean enableVideoController, String controllerColor, boolean allowSharing, boolean enableSettings, boolean allowFullscreen, boolean allowComments, boolean allowLikes, boolean enableCasting, boolean  allowEmbed, Integer transparency, boolean is360video, boolean defaultSetting, String brandingLogoUri, String brandingLogoDescUri, boolean enableVideoCobrandingLogo){
		this.playerColor= playerColor;
		this.enableVideoController=enableVideoController;
		this.controllerColor=controllerColor;
		this.allowSharing=allowSharing;
		this.enableSettings=enableSettings;
		this.allowFullscreen=allowFullscreen;
		this.allowComments=allowComments;
		this.allowLikes=allowLikes;
		this.enableCasting=enableCasting;
		this.allowEmbed=allowEmbed;
		this.transparency= transparency;
		this.is360video = is360video;
		this.defaultSetting = defaultSetting;
		this.brandingLogoUri = brandingLogoUri;
		this.brandingLogoDescUri= brandingLogoDescUri;
		this.enableVideoCobrandingLogo = enableVideoCobrandingLogo;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
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

	@JsonIgnore
	public VideoFile getVideoFile() {
		return videoFile;
	}

	public void setVideoFile(VideoFile videoFile) {
		this.videoFile = videoFile;
	}

	public Integer getTransparency() {
		return transparency;
	}

	public void setTransparency(Integer transparency) {
		this.transparency = transparency;
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
}
