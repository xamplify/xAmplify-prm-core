package com.xtremand.gdpr.setting.bom;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.xtremand.common.bom.CompanyProfile;

@Entity
@Table(name="xt_gdpr_setting")
public class GdprSetting  extends  GdprSettingUtil implements Serializable {
	
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -52879705954177663L;

	private static final String SEQUENCE = "xt_gdpr_setting_sequence";
	
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = SEQUENCE)
	@SequenceGenerator(name = SEQUENCE, sequenceName = SEQUENCE, allocationSize = 1)
	@Column(name = "id")
	private Integer id;
	
	@OneToOne
    @JoinColumn(name = "company_id",unique=true)
	private CompanyProfile companyProfile;
	
	
	
	@Column(name="created_time",columnDefinition="DATETIME")
	@Temporal(TemporalType.TIMESTAMP)
	private Date createdTime;
	
	@Column(name="created_user_id")
	private Integer createdUserId;
	
	@Column(name="updated_time",columnDefinition="DATETIME")
	@Temporal(TemporalType.TIMESTAMP)
	private Date updatedTime;
	
	@Column(name="updated_user_id")
	private Integer updatedUserId;
	
	
	
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public CompanyProfile getCompanyProfile() {
		return companyProfile;
	}

	public void setCompanyProfile(CompanyProfile companyProfile) {
		this.companyProfile = companyProfile;
	}

	public Date getCreatedTime() {
		return createdTime;
	}

	public void setCreatedTime(Date createdTime) {
		this.createdTime = createdTime;
	}

	public Integer getCreatedUserId() {
		return createdUserId;
	}

	public void setCreatedUserId(Integer createdUserId) {
		this.createdUserId = createdUserId;
	}

	public Date getUpdatedTime() {
		return updatedTime;
	}

	public void setUpdatedTime(Date updatedTime) {
		this.updatedTime = updatedTime;
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

	public static String getSequence() {
		return SEQUENCE;
	}

	
	
	

}
