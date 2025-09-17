package com.xtremand.common.bom;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@MappedSuperclass
public abstract class XamplifyTimeStamp implements Serializable {
	private static final long serialVersionUID = 1L;

	@Column(name = "created_time", columnDefinition = "DATETIME")
	@Temporal(TemporalType.TIMESTAMP)
	Date createdTime;

	@Column(name = "updated_time", columnDefinition = "DATETIME")
	@Temporal(TemporalType.TIMESTAMP)
	Date updatedTime;

	@Column(name = "updated_by")
	Integer updatedBy;

	public void initialiseCommonFields(boolean isCreate, int updatedBy) {
		if (isCreate)
			this.setCreatedTime(new Date());
		this.setUpdatedTime(new Date());
		this.setUpdatedBy(updatedBy);
	}

	public void addCreatedAndUpdatedTime(boolean isCreate, int updatedBy) {
		if (isCreate) {
			this.setCreatedTime(new Date());
		} else {
			this.setUpdatedTime(new Date());
			this.setUpdatedBy(updatedBy);
		}
	}
}