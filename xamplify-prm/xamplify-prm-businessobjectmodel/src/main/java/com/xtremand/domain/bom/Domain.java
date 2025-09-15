package com.xtremand.domain.bom;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
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
import com.xtremand.util.bom.XamplifyDefaultColumn;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "xt_allowed_domain")
@Data
@EqualsAndHashCode(callSuper = false)
public class Domain extends XamplifyDefaultColumn implements Serializable {
	/**
	* 
	*/
	private static final long serialVersionUID = -3376772453069555356L;

	private static final String SEQUENCE = "xt_allowed_domain_sequence";

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = SEQUENCE)
	@SequenceGenerator(name = SEQUENCE, sequenceName = SEQUENCE, allocationSize = 1)
	private Integer id;

	@Column(name = "domain_name", nullable = false)
	private String domainName;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "company_id", nullable = false)
	private CompanyProfile company;

	@Column(name = "module_name", nullable = false)
	@org.hibernate.annotations.Type(type = "com.xtremand.domain.bom.DomainModuleNameTypeEnum")
	private DomainModuleNameType domainModuleNameType;
	
	@Column(name = "is_domain_allowed_to_add_to_same_partner_account")
	private boolean isDomainAllowedToAddToSamePartnerAccount;

	@Column(name = "is_domain_deactivated")
	private boolean domainDeactivated;
	
	@Column(name = "deactivated_on", columnDefinition = "DATETIME")
	@Temporal(TemporalType.TIMESTAMP)
	Date deactivatedOn;

}
