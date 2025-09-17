package com.xtremand.partnership.bom;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import com.xtremand.common.bom.XamplifyTimeStamp;

import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "xt_partner_company_domain")
public class PartnerCompanyDomain extends XamplifyTimeStamp{
	
	private static final long serialVersionUID = 1L;
	private static final String SEQUENCE = "xt_partner_company_domain_sequence";
	
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = SEQUENCE)
	@SequenceGenerator(name = SEQUENCE, sequenceName = SEQUENCE, allocationSize = 1)
	@Column(name = "id")
	private Integer id;
	
	@JoinColumn(name = "partnership_id")
	@ManyToOne
	private Partnership partnership;
	
	@Column(name = "partner_company_domain")
	private String partnerCompanyDomainName;
	
	@Column(name = "created_by")
	private Integer createdBy;
	
}
