package com.xtremand.gdpr.setting.dto;

import java.io.Serializable;

public class GdprSettingDTO extends GdprSettingDTOUtil implements Serializable {
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -8285382860656754215L;

	
	private Integer companyId;
	
	private Integer createdUserId;
	
	private Integer updatedUserId;
	
	public Integer getCompanyId() {
		return companyId;
	}

	public void setCompanyId(Integer companyId) {
		this.companyId = companyId;
	}

	public Integer getCreatedUserId() {
		return createdUserId;
	}

	public void setCreatedUserId(Integer createdUserId) {
		this.createdUserId = createdUserId;
	}

	public Integer getUpdatedUserId() {
		return updatedUserId;
	}

	public void setUpdatedUserId(Integer updatedUserId) {
		this.updatedUserId = updatedUserId;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	
	
	

}
