package com.xtremand.partnership.bom;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.xtremand.partnership.bom.Partnership.PartnershipStatus;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name="xt_partnership_status_log")
public class PartnershipStatusLog {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator="partnership_status_log_seq")
	@SequenceGenerator(
			name="partnership_status_log_seq",
			sequenceName="partnership_status_log_seq",
			allocationSize=1
			)
	@Column(name="id")
	private Integer id;
	
	@Column(name="vendor_id")
	private Integer vendorId;
	
	@Column(name="partnership_id")
	private Integer partnershipId;
	
	@Column(name="company_id")
	private Integer companyId;
	
	@Column(name="updated_on", columnDefinition="DATETIME")
	@Temporal(TemporalType.TIMESTAMP)
	private Date updatedOn;
	
	@Column(name = "status")
	@org.hibernate.annotations.Type(type = "com.xtremand.partner.bom.PartnershipStatusType")
	private PartnershipStatus status;
}
