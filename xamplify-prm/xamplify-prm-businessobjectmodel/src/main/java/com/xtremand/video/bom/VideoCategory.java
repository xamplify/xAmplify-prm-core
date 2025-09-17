package com.xtremand.video.bom;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name="xt_video_categories")
public class VideoCategory {
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator="categories_id_seq")
	@SequenceGenerator(
		    name="categories_id_seq",
		    sequenceName="categories_id_seq",
		    allocationSize=1
		)
	@Column(name="categories_id")
	private Integer id;
	
	@Column(name="categories")
	private String name;
	
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
}
