package com.xtremand.category.dto;

import java.io.Serializable;
import java.util.Date;

import org.springframework.util.StringUtils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.xtremand.util.bom.DateUtils;

@JsonInclude(value = Include.NON_NULL)
public class CategoryDTO implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8865133041641038104L;

	private DateUtils dateUtils = new DateUtils();

	private Integer id;

	private String name;

	private String description;

	private String icon;

	private String companyName;

	private String emailId;

	private String firstName;

	private String lastName;

	private String createdBy;

	private String createdTimeInString;

	private boolean defaultCategory;

	private Integer count;

	private Integer createdUserId;

	private Integer companyId;

	private boolean expanded;

	private String domainName;

	private String sharedAssetPath;

	private Date createdTime;

	private String createdTimeInUTC;

	private String assetName;

	private Integer assetId;

	private String assetPath;

	private Integer damId;

	private Integer videoId;

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
		if (StringUtils.hasText(name)) {
			if (name.length() > 55) {
				this.name = name.trim().substring(0, 54);
			} else {
				this.name = name.trim();
			}
		} else {
			this.name = name;
		}

	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		if (StringUtils.hasText(description)) {
			if (description.length() > 1000) {
				this.description = description.trim().substring(0, 999);
			} else {
				this.description = description.trim();
			}
		} else {
			this.description = description;
		}

	}

	public String getIcon() {
		return icon;
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}

	public String getCompanyName() {
		return companyName;
	}

	public void setCompanyName(String companyName) {
		this.companyName = companyName;
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

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public String getCreatedTimeInString() {
		return createdTimeInString;
	}

	public void setCreatedTimeInString(String createdTimeInString) {
		this.createdTimeInString = createdTimeInString;
	}

	public boolean isDefaultCategory() {
		return defaultCategory;
	}

	public void setDefaultCategory(boolean defaultCategory) {
		this.defaultCategory = defaultCategory;
	}

	public Integer getCount() {
		if (count != null) {
			return count;
		} else {
			return 0;
		}

	}

	public void setCount(Integer count) {
		this.count = count;
	}

	public Integer getCreatedUserId() {
		return createdUserId;
	}

	public void setCreatedUserId(Integer createdUserId) {
		this.createdUserId = createdUserId;
	}

	public Integer getCompanyId() {
		return companyId;
	}

	public void setCompanyId(Integer companyId) {
		this.companyId = companyId;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public boolean isExpanded() {
		return expanded;
	}

	public void setExpanded(boolean expanded) {
		this.expanded = expanded;
	}

	public String getDomainName() {
		return domainName;
	}

	public void setDomainName(String domainName) {
		this.domainName = domainName;
	}

	public String getSharedAssetPath() {
		return sharedAssetPath;
	}

	public void setSharedAssetPath(String sharedAssetPath) {
		this.sharedAssetPath = sharedAssetPath;
	}

	public void setCreatedTime(Date createdTime) {
		this.createdTime = createdTime;
		if (createdTime != null) {
			setCreatedTimeInUTC(dateUtils.getUTCString(createdTime));
		}
	}

	private void setCreatedTimeInUTC(String createdTimeInUtc) {
		this.createdTimeInUTC = createdTimeInUtc;
	}

	public String getCreatedTimeInUTC() {
		return createdTimeInUTC;
	}

	public Date getCreatedTime() {
		return createdTime;
	}

	public String getAssetName() {
		return assetName;
	}

	public void setAssetName(String assetName) {
		this.assetName = assetName;
	}

	public Integer getAssetId() {
		return assetId;
	}

	public void setAssetId(Integer assetId) {
		this.assetId = assetId;
	}

	public String getAssetPath() {
		return assetPath;
	}

	public void setAssetPath(String assetPath) {
		this.assetPath = assetPath;
	}

	public Integer getDamId() {
		return damId;
	}

	public void setDamId(Integer damId) {
		this.damId = damId;
	}

	public Integer getVideoId() {
		return videoId;
	}

	public void setVideoId(Integer videoId) {
		this.videoId = videoId;
	}

}
