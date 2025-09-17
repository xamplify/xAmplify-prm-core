package com.xtremand.white.labeled.bom;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.xtremand.common.bom.CompanyProfile;

import lombok.Getter;
import lombok.Setter;

@MappedSuperclass
@Getter
@Setter
public class WhiteLabeledContentMappedSuperClass implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 617056737636809072L;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "shared_by_vendor_company_id", nullable = false)
	private CompanyProfile sharedByCompany;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "shared_with_partner_company_id", nullable = false)
	private CompanyProfile sharedWithCompany;
	

	@Column(name = "shared_on", columnDefinition = "DATETIME", nullable = false)
	@Temporal(TemporalType.TIMESTAMP)
	private Date sharedOn;


}
