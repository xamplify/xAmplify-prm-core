package com.xtremand.mdf.bom;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.Type;

import com.xtremand.common.bom.CompanyProfile;
import com.xtremand.partnership.bom.Partnership;

import lombok.Data;

@MappedSuperclass
@Data
public class MdfDetailsMappedSuperClass implements Serializable {
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -7837019861974815308L;

	@ManyToOne
	@JoinColumn(name = "partnership_id", referencedColumnName = "id")
	private Partnership partnership;
	
	@Column(name="mdf_amount_type")
	@Type(type = "com.xtremand.mdf.bom.MdfAmountTypeEnum")
	private MdfAmountType mdfAmountType;
	
	@Column(name="mdf_amount")
	private Double mdfAmount;
	
	@Column(name = "allocation_date", columnDefinition = "DATETIME")
	@Temporal(TemporalType.TIMESTAMP)
	private Date allocationDate;
	
	@Column(name = "expiration_date", columnDefinition = "DATETIME")
	@Temporal(TemporalType.TIMESTAMP)
	private Date expirationDate;
	
	@Column(name="description")
	private String description;
	
	@Column(name="created_by")
	private Integer createdBy;
	
	@Column(name = "created_time", columnDefinition = "DATETIME")
	@Temporal(TemporalType.TIMESTAMP)
	private Date createdTime;
	
	@Column(name="updated_by")
	private Integer updatedBy;
	
	@Column(name = "updated_time", columnDefinition = "DATETIME")
	@Temporal(TemporalType.TIMESTAMP)
	private Date updatedTime;
	
	@ManyToOne
	@JoinColumn(name = "company_id")
	private CompanyProfile companyProfile;
	
	

}
