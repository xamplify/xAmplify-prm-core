package com.xtremand.dashboard.analytics.views.bom;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

import lombok.Data;

@Entity(name = "vendor_activity_view")
@Data
public class VendorActivityView implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1177353290709402557L;

	@Id
	@Column(name = "row_number")
	private Integer rowNumber;

	@Column(name = "company_id")
	private Integer companyId;

	private boolean regular;

	private boolean video;

	private boolean social;

	private boolean event;

	private boolean page;

	private boolean survey;

	@Column(name = "partner_company_id")
	private Integer partnerCompanyId;

	private Integer count;

}
