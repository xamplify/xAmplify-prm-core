package com.xtremand.domain.bom;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "xt_domain_metadata")
@Data
@EqualsAndHashCode(callSuper = false)
public class DomainMetadata {
	
	private static final long serialVersionUID = -3376772453069555356L;

	private static final String SEQUENCE = "xt_domain_metadata_sequence";

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = SEQUENCE)
	@SequenceGenerator(name = SEQUENCE, sequenceName = SEQUENCE, allocationSize = 1)
	private Integer id;

	@Column(name = "domain_name", nullable = false)
	private String domainName;
	
	@Column(name = "company_info_metadata", nullable = false)
	private String companyInfoMetadata;
	
	@Column(name = "created_time", nullable = false)
	private Date createdTime;

}
