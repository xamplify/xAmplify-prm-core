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
@Table(name="xt_call_action")
public class CallAction implements Serializable {

	@GenericGenerator(name = "generator", strategy = "foreign", parameters = @Parameter(name = "property", value = "videoFile"))
	@Id
	@GeneratedValue(generator = "generator")
	@Column(name = "id", unique = true, nullable = false)
	private Integer id;

	private boolean name;

	private boolean skip;

	@Column(name="uppertext")
	private String upperText;

	@Column(name="lowertext")
	private String lowerText;
	
	@Column(name="start_of_video") 
	private boolean startOfVideo;
	
	@Column(name="end_of_video") 
	private boolean endOfVideo;
	
	@Column(name="call_action")
	private boolean callACtion;

	@OneToOne(fetch = FetchType.LAZY)
	@PrimaryKeyJoinColumn
	private VideoFile videoFile;

	public CallAction(){

	}

	public CallAction(boolean name, boolean skip,String upperText,String lowerText, boolean startOfVideo, boolean endOfVideo, boolean callACtion){
		this.name = name;
		this.skip= skip;
		this.upperText = upperText;
		this.lowerText = lowerText;
		this.startOfVideo= startOfVideo;
		this.endOfVideo=endOfVideo;
		this.callACtion=callACtion;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
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

	@JsonIgnore
	public VideoFile getVideoFile() {
		return videoFile;
	}

	public void setVideoFile(VideoFile videoFile) {
		this.videoFile = videoFile;
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

	public boolean isCallACtion() {
		return callACtion;
	}

	public void setCallACtion(boolean callACtion) {
		this.callACtion = callACtion;
	}
	
}
