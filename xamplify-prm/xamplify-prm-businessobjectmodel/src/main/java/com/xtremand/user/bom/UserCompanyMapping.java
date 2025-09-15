package com.xtremand.user.bom;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.xtremand.common.bom.CompanyProfile;

@Entity
@Table(name="xt_user_company_mapping")
public class UserCompanyMapping implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator="user_company_mapping_id_seq")
	@SequenceGenerator(
			name="user_company_mapping_id_seq",
			sequenceName="user_company_mapping_id_seq",
			allocationSize=1
			)
	@Column(name = "id")
	private Integer id;
	
	@ManyToOne
	@JoinColumn(name = "user_id")  
	private User user;
	
	@ManyToOne
	@JoinColumn(name = "company_id")  
	private CompanyProfile companyProfile;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="created_time", columnDefinition="DATETIME")
	private Date createdDate;
	
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="updated_time", columnDefinition="DATETIME",nullable=true)
	private Date updatedDate;


	public Integer getId() {
		return id;
	}


	public void setId(Integer id) {
		this.id = id;
	}


	public User getUser() {
		return user;
	}


	public void setUser(User user) {
		this.user = user;
	}


	public CompanyProfile getCompanyProfile() {
		return companyProfile;
	}


	public void setCompanyProfile(CompanyProfile companyProfile) {
		this.companyProfile = companyProfile;
	}


	public Date getCreatedDate() {
		return createdDate;
	}


	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
	}


	public Date getUpdatedDate() {
		return updatedDate;
	}


	public void setUpdatedDate(Date updatedDate) {
		this.updatedDate = updatedDate;
	}


	public static long getSerialversionuid() {
		return serialVersionUID;
	}
	
	
	

}
