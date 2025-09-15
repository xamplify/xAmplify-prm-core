package com.xtremand.video.bom;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import com.xtremand.common.bom.XamplifyTimeStamp;
import com.xtremand.dam.bom.Dam;
import com.xtremand.user.bom.User;

@Entity
@Table(name = "xt_video_files")
public class VideoFile extends XamplifyTimeStamp implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1769878291911570248L;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "video_id_seq")
	@SequenceGenerator(name = "video_id_seq", sequenceName = "video_id_seq", allocationSize = 1)
	private Integer id;

	@Column(name = "video_id")
	private String videoID;

	@ManyToOne
	@JoinColumn(name = "categories_id")
	private VideoCategory category;

	private String title;

	@Column(name = "videouri")
	private String uri;

	private String description;

	@ManyToOne
	@JoinColumn(name = "customer_id", referencedColumnName = "user_id")
	private User customer;

	@Column(name = "video_status")
	@org.hibernate.annotations.Type(type = "com.xtremand.video.bom.VideoStatusType")
	private VideoStatus videoStatus;

	@Column(name = "view_by")
	@org.hibernate.annotations.Type(type = "com.xtremand.video.bom.ViewByType")
	private VideoFile.TYPE viewBy;

	@Column(name = "big_thumbnail_image")
	private String bigThumbnailImage;

	@Column(name = "imageuri")
	private String imageUri;

	private String alias;

	@Column(name = "video_length")
	private String videoLength;

	@Column(name = "video_size")
	private Double videoSize;

	@Column(name = "server_url")
	private String serverUrl;

	@Column(name = "bitrate")
	private Double bitrate;

	@Column(name = "gifuri")
	private String gifUri;

	@OneToMany(mappedBy = "videoFile")
	private Set<VideoAnnotation> videoAnnotations;

	@Fetch(FetchMode.SUBSELECT)
	@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@JoinColumn(name = "video_id", referencedColumnName = "id")
	private Set<VideoTag> videoTags = new HashSet<>(0);

	@OneToOne(fetch = FetchType.LAZY, mappedBy = "videoFile", cascade = CascadeType.ALL)
	private VideoImage videoImage;

	@OneToOne(fetch = FetchType.LAZY, mappedBy = "videoFile", cascade = CascadeType.ALL)
	private CallAction callAction;

	@OneToOne(fetch = FetchType.LAZY, mappedBy = "videoFile", cascade = CascadeType.ALL)
	private VideoControl videoControl;


	@Fetch(FetchMode.SUBSELECT)
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "videoFile")
	private Set<VideoLead> videoLeads = new HashSet<>();

	private Integer views;

	@Column(name = "is_processed")
	private boolean isProcessed;

	@Transient
	private String fullImagePath;
	@Transient
	private String fullVideoPath;
	@Transient
	private String fullGifPath;

	@OneToOne(mappedBy = "videoFile")
	private Dam dam;

	@Column(name = "is_custom_thumbnail_uploaded")
	private boolean customThumbnailUploaded;

	public enum VideoStatus {
		UAPPROVED("UnApproved"), APPROVED("APPROVED"), DECLINE("DECLINE"), BLOCK("BLOCK"), SUSPEND("SUSPEND"),
		DELETE("DELETE");

		protected String status;

		private VideoStatus(String status) {
			this.status = status;
		}

		public String getStatus() {
			return status;
		}

	}

	public enum TYPE {

		PUBLIC("PUBLIC"), PRIVATE("PRIVATE"), DRAFT("DRAFT"), CHANNEL("CHANNEL"), UNLISTED("Unlisted");

		protected String viewByType;

		private TYPE(String viewByType) {
			this.viewByType = viewByType;
		}

		public String getViewByType() {
			return viewByType;
		}
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getVideoID() {
		return videoID;
	}

	public void setVideoID(String videoID) {
		this.videoID = videoID;
	}

	public VideoCategory getCategory() {
		return category;
	}

	public void setCategory(VideoCategory category) {
		this.category = category;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public User getCustomer() {
		return customer;
	}

	public void setCustomer(User customer) {
		this.customer = customer;
	}

	public VideoStatus getVideoStatus() {
		return videoStatus;
	}

	public void setVideoStatus(VideoStatus videoStatus) {
		this.videoStatus = videoStatus;
	}

	public VideoFile.TYPE getViewBy() {
		return viewBy;
	}

	public void setViewBy(VideoFile.TYPE viewBy) {
		this.viewBy = viewBy;
	}

	public String getImageUri() {
		return imageUri;
	}

	public void setImageUri(String imageUri) {
		this.imageUri = imageUri;
		if (this.imageUri != null) {
			setBigThumbnailImage(this.imageUri.substring(0, this.imageUri.lastIndexOf(".")) + "_big.jpg");
		}
	}

	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

	public String getVideoLength() {
		return videoLength;
	}

	public void setVideoLength(String videoLength) {
		this.videoLength = videoLength;
	}

	public Double getVideoSize() {
		return videoSize;
	}

	public void setVideoSize(Double videoSize) {
		this.videoSize = videoSize;
	}

	public String getServerUrl() {
		return serverUrl;
	}

	public void setServerUrl(String serverUrl) {
		this.serverUrl = serverUrl;
	}

	public Double getBitrate() {
		return bitrate;
	}

	public void setBitrate(Double bitrate) {
		this.bitrate = bitrate;
	}

	public String getGifUri() {
		return gifUri;
	}

	public void setGifUri(String gifUri) {
		this.gifUri = gifUri;
	}

	public Set<VideoAnnotation> getVideoAnnotations() {
		return videoAnnotations;
	}

	public void setVideoAnnotations(Set<VideoAnnotation> videoAnnotations) {
		this.videoAnnotations = videoAnnotations;
	}

	public Set<VideoTag> getVideoTags() {
		return videoTags;
	}

	public void setVideoTags(Set<VideoTag> videoTags) {
		this.videoTags = videoTags;
	}

	public String getFullImagePath() {
		return fullImagePath;
	}

	public void setFullImagePath(String fullImagePath) {
		this.fullImagePath = fullImagePath;
	}

	public String getFullVideoPath() {
		return fullVideoPath;
	}

	public void setFullVideoPath(String fullVideoPath) {
		this.fullVideoPath = fullVideoPath;
	}

	public String getFullGifPath() {
		return fullGifPath;
	}

	public void setFullGifPath(String fullGifPath) {
		this.fullGifPath = fullGifPath;
	}

	public String getBigThumbnailImage() {
		return bigThumbnailImage;
	}

	public void setBigThumbnailImage(String bigThumbnailImage) {
		this.bigThumbnailImage = bigThumbnailImage;
	}

	public VideoImage getVideoImage() {
		return videoImage;
	}

	public void setVideoImage(VideoImage videoImage) {
		this.videoImage = videoImage;
	}

	public CallAction getCallAction() {
		return callAction;
	}

	public void setCallAction(CallAction callAction) {
		this.callAction = callAction;
	}

	public VideoControl getVideoControl() {
		return videoControl;
	}

	public void setVideoControl(VideoControl videoControl) {
		this.videoControl = videoControl;
	}


	public Integer getViews() {
		return views;
	}

	public void setViews(Integer views) {
		this.views = views;
	}

	public boolean isProcessed() {
		return isProcessed;
	}

	public void setProcessed(boolean isProcessed) {
		this.isProcessed = isProcessed;
	}

	public Set<VideoLead> getVideoLeads() {
		return videoLeads;
	}

	public void setVideoLeads(Set<VideoLead> videoLeads) {
		this.videoLeads = videoLeads;
	}

	public Dam getDam() {
		return dam;
	}

	public void setDam(Dam dam) {
		this.dam = dam;
	}

	public boolean isCustomThumbnailUploaded() {
		return customThumbnailUploaded;
	}

	public void setCustomThumbnailUploaded(boolean customThumbnailUploaded) {
		this.customThumbnailUploaded = customThumbnailUploaded;
	}

}
