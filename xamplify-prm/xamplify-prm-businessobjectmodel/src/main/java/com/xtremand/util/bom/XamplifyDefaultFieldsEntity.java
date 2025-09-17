package com.xtremand.util.bom;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.xtremand.user.bom.User;

import lombok.Data;

@MappedSuperclass
@Data
public class XamplifyDefaultFieldsEntity {

	@Column(name = "created_time", columnDefinition = "DATETIME")
	@Temporal(TemporalType.TIMESTAMP)
	private Date createdTime;

	@Column(name = "updated_time", columnDefinition = "DATETIME")
	@Temporal(TemporalType.TIMESTAMP)
	private Date updatedTime;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "created_user_id")
	private User createdBy;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "updated_user_id")
	private User updatedBy;

}
