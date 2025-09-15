package com.xtremand.dashboard.analytics.views.bom;

import java.io.Serializable;
import java.math.BigInteger;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
@MappedSuperclass
public class OpportunitiesAnalyticsMappedSuperClassView implements Serializable {



	/**
	 * 
	 */
	private static final long serialVersionUID = -1644842061638469812L;
	
	

	@Id
	@Column(name="row_number")
	private Integer rowNumber;
	
	@Column(name="company_id")
	private Integer companyId;
	
	@Column(name="total_leads")
	private BigInteger totalLeads;
	
	@Column(name="total_deals")
	private BigInteger totalDeals;
	
	@Column(name="opened_deal")
	private BigInteger openedDeals;
	
	@Column(name="hold")
	private BigInteger dealsOnHold;
	
	@Column(name="approved")
	private BigInteger approvedDeals;
	
	@Column(name="rejected")
	private BigInteger rejectedDeals;
	
	
	public Integer getRowNumber() {
		return rowNumber;
	}

	public void setRowNumber(Integer rowNumber) {
		this.rowNumber = rowNumber;
	}

	public Integer getCompanyId() {
		return companyId;
	}

	public void setCompanyId(Integer companyId) {
		this.companyId = companyId;
	}

	public BigInteger getTotalLeads() {
		if(totalLeads!=null) {
			return totalLeads;
		}else {
			return BigInteger.valueOf(0);
		}
		
	}

	public void setTotalLeads(BigInteger totalLeads) {
		this.totalLeads = totalLeads;
	}

	public BigInteger getTotalDeals() {
		if(totalDeals!=null) {
			return totalDeals;
		}else {
			return BigInteger.valueOf(0);
		}
		
	}

	public void setTotalDeals(BigInteger totalDeals) {
		this.totalDeals = totalDeals;
	}

	public BigInteger getOpenedDeals() {
		if(openedDeals!=null) {
			return openedDeals;
		}else {
			return BigInteger.valueOf(0);
		}
	}

	public void setOpenedDeals(BigInteger openedDeals) {
		this.openedDeals = openedDeals;
	}

	public BigInteger getDealsOnHold() {
		if(dealsOnHold!=null) {
			return dealsOnHold;
		}else {
			return BigInteger.valueOf(0);
		}
	}

	public void setDealsOnHold(BigInteger dealsOnHold) {
		this.dealsOnHold = dealsOnHold;
	}

	public BigInteger getApprovedDeals() {
		if(approvedDeals!=null) {
			return approvedDeals;
		}else {
			return BigInteger.valueOf(0);
		}
	}

	public void setApprovedDeals(BigInteger approvedDeals) {
		this.approvedDeals = approvedDeals;
	}

	public BigInteger getRejectedDeals() {
		if(rejectedDeals!=null) {
			return rejectedDeals;
		}else {
			return BigInteger.valueOf(0);
		}
	}

	public void setRejectedDeals(BigInteger rejectedDeals) {
		this.rejectedDeals = rejectedDeals;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}
	
	
	
	
	

}
