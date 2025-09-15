package com.xtremand.dashboard.analytics.views.bom;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;

@Entity(name = "opportunities_vanity_url_partner_analytics_view")
public class OpportunitiesVanityUrlPartnerAnalyticsView extends OpportunitiesAnalyticsMappedSuperClassView implements Serializable {/**
	 * 
	 */
	private static final long serialVersionUID = -3668716867313694798L;
	
	
	@Column(name = "vendor_organization_id")
	private Integer vendorOrgainzationId;


	public Integer getVendorOrgainzationId() {
		return vendorOrgainzationId;
	}


	public void setVendorOrgainzationId(Integer vendorOrgainzationId) {
		this.vendorOrgainzationId = vendorOrgainzationId;
	}


	public static long getSerialversionuid() {
		return serialVersionUID;
	}

}
