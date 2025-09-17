package com.xtremand.common.bom;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="xt_player_controls")
public class PlayerControls extends XamplifyTimeStamp{

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private Integer id;
	@Column(name="video_id")
	private Integer videoId;
	private Integer sharing;
	private Integer settings;
	private Integer fullscreen;
	private Integer comments;
	private Integer likes;
	private Integer casting;
	private Integer embed;
	
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
	public Integer getSharing() {
		return sharing;
	}
	public void setSharing(Integer sharing) {
		this.sharing = sharing;
	}
	public Integer getSettings() {
		return settings;
	}
	public void setSettings(Integer settings) {
		this.settings = settings;
	}
	public Integer getFullscreen() {
		return fullscreen;
	}
	public void setFullscreen(Integer fullscreen) {
		this.fullscreen = fullscreen;
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
	public Integer getCasting() {
		return casting;
	}
	public void setCasting(Integer casting) {
		this.casting = casting;
	}
	public Integer getEmbed() {
		return embed;
	}
	public void setEmbed(Integer embed) {
		this.embed = embed;
	}
}

