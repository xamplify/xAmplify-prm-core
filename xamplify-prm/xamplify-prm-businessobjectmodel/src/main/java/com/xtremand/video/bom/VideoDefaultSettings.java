package com.xtremand.video.bom;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import com.xtremand.common.bom.CompanyProfile;

@Entity
@Table(name = "xt_video_default_settings")
public class VideoDefaultSettings {
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "video_default_settings_id_seq")
	@SequenceGenerator(name = "video_default_settings_id_seq", sequenceName = "video_default_settings_id_seq", allocationSize = 1)
	private Integer id;
	@OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	@JoinColumn(name = "company_id", nullable = true)
	private CompanyProfile companyProfile;
	@Column(name = "player_color")
	private String playerColor;
	@Column(name = "enable_videocontroller")
	private boolean enableVideoController;
	@Column(name = "controller_color")
	private String controllerColor;
	@Column(name = "allow_sharing")
	private boolean allowSharing;
	@Column(name = "enable_settings")
	private boolean enableSettings;
	@Column(name = "allow_fullscreen")
	private boolean allowFullscreen;
	@Column(name = "allow_comments")
	private boolean allowComments;
	@Column(name = "allow_likes")
	private boolean allowLikes;
	@Column(name = "enable_casting")
	private boolean enableCasting;
	@Column(name = "allow_embed")
	private boolean allowEmbed;
	@Column(name = "transparency")
	private Integer transparency;
	@Column(name = "is_360video")
	private boolean is360video;
	@Column(name = "branding_logo_uri")
	private String brandingLogoUri;
	@Column(name = "branding_logo_desc_uri")
	private String brandingLogoDescUri;

	/*
	 * public VideoDefaultSettings(String playerColor, boolean
	 * enableVideoController, String controllerColor, boolean allowSharing, boolean
	 * enableSettings, boolean allowFullscreen, boolean allowComments, boolean
	 * allowLikes, boolean enableCasting, boolean allowEmbed, Integer transparency,
	 * boolean is360video){ this.playerColor= playerColor;
	 * this.enableVideoController=enableVideoController;
	 * this.controllerColor=controllerColor; this.allowSharing=allowSharing;
	 * this.enableSettings=enableSettings; this.allowFullscreen=allowFullscreen;
	 * this.allowComments=allowComments; this.allowLikes=allowLikes;
	 * this.enableCasting=enableCasting; this.allowEmbed=allowEmbed;
	 * this.transparency= transparency; this.is360video = is360video; }
	 */

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	/*
	 * public Integer getUserId() { return userId; } public void setUserId(Integer
	 * userId) { this.userId = userId; }
	 */
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

	public CompanyProfile getCompanyProfile() {
		return companyProfile;
	}

	public void setCompanyProfile(CompanyProfile companyProfile) {
		this.companyProfile = companyProfile;
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
}
