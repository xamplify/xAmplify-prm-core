package com.xtremand.category.bom;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.springframework.util.StringUtils;

@MappedSuperclass
public class CategoryMappedView {
	


	/**
	 * 
	 */
	private static final long serialVersionUID = 3784978659484603493L;
	
	@Id
	private Integer id;
	
	@Column(name="name")
	private String name;
	
	
	@Column(name="description")
	private String description;
	
	@Column(name="icon")
	private String icon;
	
	@Column(name="is_default")
	private boolean defaultCategory;
	
	@Column(name="company_id")
	private Integer companyId;
	
	@Column(name="created_time",columnDefinition="DATETIME")
	@Temporal(TemporalType.TIMESTAMP)
	private Date createdTime;
	
	@Column(name="updated_time",columnDefinition="DATETIME")
	@Temporal(TemporalType.TIMESTAMP)
	private Date updatedTime;
	
	@Column(name="email_id")
	private String emailId;
	
	@Column(name="firstname")
	private String firstName;
	
	@Column(name="lastname")
	private String lastName;
	
	@Column(name="company_name")
	private String companyName;
	
	@Column(name="count")
	private Integer count;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getName() {
		if(StringUtils.hasText(name)) {
			return name.trim();
		}else {
			return name;
		}
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		if(StringUtils.hasText(description)) {
			return description.trim();
		}else {
			return description;
		}
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getIcon() {
		return icon;
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}

	public boolean isDefaultCategory() {
		return defaultCategory;
	}

	public void setDefaultCategory(boolean defaultCategory) {
		this.defaultCategory = defaultCategory;
	}

	public Integer getCompanyId() {
		return companyId;
	}

	public void setCompanyId(Integer companyId) {
		this.companyId = companyId;
	}

	public Date getCreatedTime() {
		return createdTime;
	}

	public void setCreatedTime(Date createdTime) {
		this.createdTime = createdTime;
	}

	public Date getUpdatedTime() {
		return updatedTime;
	}

	public void setUpdatedTime(Date updatedTime) {
		this.updatedTime = updatedTime;
	}

	public String getEmailId() {
		return emailId;
	}

	public void setEmailId(String emailId) {
		this.emailId = emailId;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getCompanyName() {
		return companyName;
	}

	public void setCompanyName(String companyName) {
		this.companyName = companyName;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public Integer getCount() {
		return count;
	}

	public void setCount(Integer count) {
		this.count = count;
	}
	
	



}
