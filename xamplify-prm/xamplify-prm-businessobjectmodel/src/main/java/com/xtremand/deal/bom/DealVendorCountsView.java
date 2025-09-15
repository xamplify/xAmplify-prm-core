package com.xtremand.deal.bom;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity(name="deal_vendor_counts_view")
public class DealVendorCountsView {	
	@Id
	private Integer id;
	
	@Column(name = "company_id")
	Integer companyId;
	
	@Column(name = "total_deals")
	Integer totalDeals = 0;
	
	@Column(name = "won_deals")
	Integer wonDeals = 0;
	
	@Column(name = "lost_deals")
	Integer lostDeals = 0;

}
