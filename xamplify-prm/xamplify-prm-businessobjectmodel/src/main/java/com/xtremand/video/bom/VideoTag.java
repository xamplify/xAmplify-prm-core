package com.xtremand.video.bom;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity(name="xt_video_tags")
@Table
public class VideoTag implements Serializable{
	
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator="video_tags_id_seq")
	@SequenceGenerator(
		    name="video_tags_id_seq",
		    sequenceName="video_tags_id_seq",
		    allocationSize=1
		)
	Integer id;
	
	@Column(name="video_id")
	Integer videoId;
	
	@Column(name="video_tags")
	String tag;
	
	public VideoTag() {
		// TODO Auto-generated constructor stub
	}
	public VideoTag(Integer videoId, String tag){
		this.videoId = videoId;
		this.tag = tag;
	}
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getVideoId() {
		return videoId;
	}
	public void setVideoId(Integer videoId) {
		this.videoId = videoId;
	}
	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}
	
	
}
