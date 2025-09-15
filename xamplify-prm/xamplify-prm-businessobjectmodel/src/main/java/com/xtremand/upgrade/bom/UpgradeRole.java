package com.xtremand.upgrade.bom;

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

import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "xt_upgrade_role")
@Data
@EqualsAndHashCode(callSuper = false)
public class UpgradeRole  implements Serializable {/**
	 * 
	 */
	private static final long serialVersionUID = 6968870792386278114L;
	
	
	private static final String UPGRADE_ROLE_SEQUENCE = "xt_upgrade_role_sequence";
	
	
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = UPGRADE_ROLE_SEQUENCE)
	@SequenceGenerator(name = UPGRADE_ROLE_SEQUENCE, sequenceName = UPGRADE_ROLE_SEQUENCE, allocationSize = 1)
	@Column(name = "id")
	private Integer id;
	
	@JoinColumn(name="requested_company_id")
	@ManyToOne
	private CompanyProfile requestedCompany;
	
	
	@Column(name="request_status")
	@org.hibernate.annotations.Type(type = "com.xtremand.upgrade.bom.UpgradeRoleRequestStatusType")
	private UpgradeRoleRequestStatus upgradeRoleRequestStatus;
	
	@Column(name="created_time", columnDefinition="DATETIME")
	@Temporal(TemporalType.TIMESTAMP)
	private Date createdTime;
	
	@Column(name="updated_time", columnDefinition="DATETIME")
	@Temporal(TemporalType.TIMESTAMP)
	private Date updatedTime;
	
	@Column(name="created_by", nullable=false)
	private Integer createdBy;
	
	@Column(name="updated_by", nullable=true)
	private Integer updatedBy;
	
	
	
	

}
