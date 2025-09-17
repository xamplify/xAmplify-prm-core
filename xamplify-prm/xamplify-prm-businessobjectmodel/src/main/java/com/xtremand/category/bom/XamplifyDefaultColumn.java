package com.xtremand.category.bom;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import lombok.Getter;
import lombok.Setter;

@MappedSuperclass
@Getter
@Setter
public class XamplifyDefaultColumn implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2298663130897125726L;

	@Column(name = "company_id")
	private Integer companyId;

	@Column(name = "created_time", columnDefinition = "DATETIME")
	@Temporal(TemporalType.TIMESTAMP)
	private Date createdTime;

	@Column(name = "created_user_id")
	private Integer createdUserId;

	@Column(name = "updated_time", columnDefinition = "DATETIME")
	@Temporal(TemporalType.TIMESTAMP)
	private Date updatedTime;

	@Column(name = "updated_user_id")
	private Integer updatedUserId;

}
