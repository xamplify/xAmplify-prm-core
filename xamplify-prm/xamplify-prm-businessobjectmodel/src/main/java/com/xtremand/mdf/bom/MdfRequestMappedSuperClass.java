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

import com.xtremand.partnership.bom.Partnership;

import lombok.Getter;
import lombok.Setter;

@MappedSuperclass
@Getter
@Setter
public class MdfRequestMappedSuperClass implements Serializable {
	
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -4940578102242602749L;

	@ManyToOne
	@JoinColumn(name = "partnership_id", referencedColumnName = "id")
	private Partnership partnership;
	
	
	@Column(name="allocation_amount")
	private Double allocationAmount;
	
	@Column(name="allocation_date")
	private Date allocationDate;
	
	@Column(name="allocation_expiration_date")
	private Date allocationExpirationDate;
	
	@Column(name="reimburse_amount")
	private Double reimbursementAmount;
	
	@Column(name = "created_time", columnDefinition = "DATETIME")
	@Temporal(TemporalType.TIMESTAMP)
	private Date createdTime;
	
	@Column(name="created_by")
	private Integer createdBy;
	
	@Column(name = "updated_time", columnDefinition = "DATETIME")
	@Temporal(TemporalType.TIMESTAMP)
	private Date updatedTime;
	
	@Column(name="updated_by")
	private Integer updatedBy;
	
	@Column(name = "status")
	@Type(type = "com.xtremand.mdf.bom.MdfWorkFlowStepTypeEnum")
	private MdfWorkFlowStepType mdfWorkFlowStepType;
	
	@Column(name="description")
	private String description;

}
