package com.xtremand.dashboard.analytics.views.bom;

import java.io.Serializable;
import java.math.BigInteger;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

import lombok.Data;

@Entity(name="email_stats_vanity_url_view")
@Data
public class EmailStatsVanityUrlView implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8820130612841515910L;
	
	@Id
	@Column(name="row_number")
	private Integer rowNumber;
	
	@Column(name="vendor_company_id")
	private Integer vendorCompanyId;
	
	@Column(name="partner_company_id")
	private Integer partnerCompanyId;
	
	private BigInteger opened;
	
	private BigInteger clicked;
	
	private BigInteger views;

}
