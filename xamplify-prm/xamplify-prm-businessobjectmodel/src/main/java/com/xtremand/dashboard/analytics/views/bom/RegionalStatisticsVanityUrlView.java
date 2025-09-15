package com.xtremand.dashboard.analytics.views.bom;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity(name = "regional_statistics_vanity_url_view")
@Data
@EqualsAndHashCode(callSuper = true)
public class RegionalStatisticsVanityUrlView extends RegionalStatisticsMappedSuperClassView implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5859279106692384352L;
	
	@Column(name="partner_company_id")
	private Integer partnerCompanyId;

	
	
	
}
