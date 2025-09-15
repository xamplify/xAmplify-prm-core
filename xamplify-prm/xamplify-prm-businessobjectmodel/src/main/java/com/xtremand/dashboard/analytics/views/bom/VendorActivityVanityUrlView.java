package com.xtremand.dashboard.analytics.views.bom;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

import lombok.Data;

@Entity(name = "vendor_activity_vanity_url_view")
@Data
public class VendorActivityVanityUrlView implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3089583913774206434L;
	@Id
	@Column(name = "row_number")
	private Integer rowNumber;

	@Column(name = "partner_company_id")
	private Integer partnerCompanyId;

	@Column(name = "vendor_company_id")
	private Integer vendorCompanyId;

	private boolean regular;

	private boolean video;

	private boolean social;

	private boolean event;

	@Column(name = "landing_page")
	private boolean page;

	@Column(name = "landing_page_campaign")
	private boolean pageCampaign;

	private boolean survey;

	@Column(name = "cnt")
	private Integer count;

}
