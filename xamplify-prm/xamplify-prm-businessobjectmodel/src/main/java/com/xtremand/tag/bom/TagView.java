package com.xtremand.tag.bom;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name="xt_tag_view")
public class TagView {
	
	@Id
	@Column(name="tag_id")
	private Integer tagId;
	
	@Column(name="tag_name")
	private String tagName;
	
	@Column(name="created_time")
	private Date createdTime;
	
	@Column(name="updated_time")
	private Date updatedTime;
	
	@Column(name="created_by")
	private Integer createdBy;
	
	@Column(name="updated_by")
	private Integer updatedBy;
	
	@Column(name="company_id")
	private Integer companyId;
	
	@Column(name="email_id")
	private String emailId;
	
	@Column(name="full_name")
	private String fullName;

}



