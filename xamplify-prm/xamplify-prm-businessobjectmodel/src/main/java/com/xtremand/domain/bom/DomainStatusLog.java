package com.xtremand.domain.bom;

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

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name="xt_domain_status_log")
public class DomainStatusLog {
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator="domain_status_log_seq")
	@SequenceGenerator(
			name="domain_status_log_seq",
			sequenceName="domain_status_log_seq",
			allocationSize=1
			)
	@Column(name="id")
	private Integer id;
	
	@Column(name="vendor_id")
	private Integer vendorId;
	
	@Column(name="domain_id")
	private Integer domainId;
	
	@Column(name="company_id")
	private Integer companyId;
	
	@Column(name="updated_on", columnDefinition="DATETIME")
	@Temporal(TemporalType.TIMESTAMP)
	private Date updatedOn;
	
	private boolean deactivated;
	
}
