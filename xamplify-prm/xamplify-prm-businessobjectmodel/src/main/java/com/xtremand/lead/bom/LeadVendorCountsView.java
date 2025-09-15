package com.xtremand.lead.bom;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity(name="lead_vendor_counts_view")
public class LeadVendorCountsView {

	@Id
	private Integer id;
	
	@Column(name = "company_id")
	Integer companyId;
	
	@Column(name = "total_leads")
	Integer totalLeads = 0;
	
	@Column(name = "won_leads")
	Integer wonLeads = 0;
	
	@Column(name = "lost_leads")
	Integer lostLeads = 0;
	
	@Column(name = "converted_leads")
	Integer convertedLeads = 0;	
}
