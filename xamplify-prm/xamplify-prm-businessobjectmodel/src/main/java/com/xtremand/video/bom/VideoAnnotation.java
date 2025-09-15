package com.xtremand.video.bom;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.xtremand.common.bom.XamplifyTimeStamp;


@Entity
@Table(name="xt_video_annotation")
public class VideoAnnotation extends XamplifyTimeStamp {
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private Integer id;
	
	@ManyToOne
	@JoinColumn(name="video_id")
	private VideoFile videoFile;
	
	private String text;
	private String time;
	@Column(name="thumbnail_image")
	private String thumbnailImage;
	@Transient
	private String fullImagePath;
	
	public VideoAnnotation() {
	}
	
	public VideoAnnotation(VideoFile videoFile, String text, String time, String thumbnailImage) {
		super();
		this.videoFile = videoFile;
		this.text = text;
		this.time = time;
		this.thumbnailImage = thumbnailImage;
	}

	public String getFullImagePath() {
		return fullImagePath;
	}
	public void setFullImagePath(String fullImagePath) {
		this.fullImagePath = fullImagePath;
	}
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	public String getTime() {
		return time;
	}
	public void setTime(String time) {
		this.time = time;
	}
	public String getThumbnailImage() {
		return thumbnailImage;
	}
	public void setThumbnailImage(String thumbnailImage) {
		this.thumbnailImage = thumbnailImage;
	}
}
