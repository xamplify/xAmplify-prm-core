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
@Table(name="xt_video_image")
public class VideoImage  implements Serializable{

	@GenericGenerator(name = "generator", strategy = "foreign", parameters = @Parameter(name = "property", value = "videoFile"))
	@Id
	@GeneratedValue(generator = "generator")
	@Column(name = "id", unique = true, nullable = false)
	private Integer id;


	@Column(name="image1")
	private String image1;


	@Column(name="image2")
	private String image2;


	@Column(name="image3")
	private String image3;


	@Column(name="gif1")
	private String gif1;

	@Column(name="gif2")
	private String gif2;

	@Column(name="gif3")
	private String gif3;

	@OneToOne(fetch = FetchType.LAZY)
	@PrimaryKeyJoinColumn
	private VideoFile videoFile;

	public VideoImage(){

	}

	public VideoImage(String image1, String image2, String image3, String gif1, String gif2, String gif3){
		this.image1 = image1;
		this.image2 = image2;
		this.image3 = image3;
		this.gif1 = gif1;
		this.gif2 = gif2;
		this.gif3 = gif3;
	}


	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getImage1() {
		return image1;
	}

	public void setImage1(String image1) {
		this.image1 = image1;
	}

	public String getImage2() {
		return image2;
	}

	public void setImage2(String image2) {
		this.image2 = image2;
	}

	public String getImage3() {
		return image3;
	}

	public void setImage3(String image3) {
		this.image3 = image3;
	}

	public String getGif1() {
		return gif1;
	}

	public void setGif1(String gif1) {
		this.gif1 = gif1;
	}

	public String getGif2() {
		return gif2;
	}

	public void setGif2(String gif2) {
		this.gif2 = gif2;
	}

	public String getGif3() {
		return gif3;
	}

	public void setGif3(String gif3) {
		this.gif3 = gif3;
	}

	@JsonIgnore
	public VideoFile getVideoFile() {
		return videoFile;
	}

	public void setVideoFile(VideoFile videoFile) {
		this.videoFile = videoFile;
	}
}
