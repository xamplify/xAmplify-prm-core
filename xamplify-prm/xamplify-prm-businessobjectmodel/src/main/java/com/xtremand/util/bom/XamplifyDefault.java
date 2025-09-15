package com.xtremand.util.bom;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import lombok.Data;

@MappedSuperclass
@Data
public class XamplifyDefault {

	
	@Column(name="created_time", columnDefinition="DATETIME")
	@Temporal(TemporalType.TIMESTAMP)
	private Date createdTime;
	
	@Column(name="updated_time", columnDefinition="DATETIME")
	@Temporal(TemporalType.TIMESTAMP)
	private Date updatedTime;
	
	@Column(name="created_user_id", nullable=false)
	private Integer createdUserId;
	
	@Column(name="updated_user_id", nullable=true)
	private Integer updatedUserId;
	



}
