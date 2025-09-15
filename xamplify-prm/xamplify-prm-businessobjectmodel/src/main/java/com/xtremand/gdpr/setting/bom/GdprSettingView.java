package com.xtremand.gdpr.setting.bom;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="gdpr_setting_view")
public class GdprSettingView extends GdprSettingUtil implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -4588194322052807183L;

	@Id
	@Column(name="id")
	private Integer id;
	
	@Column(name="company_id")
	private Integer companyId;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
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
	

}
