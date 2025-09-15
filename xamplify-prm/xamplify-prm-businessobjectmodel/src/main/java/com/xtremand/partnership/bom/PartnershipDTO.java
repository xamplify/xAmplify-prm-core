package com.xtremand.partnership.bom;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PartnershipDTO implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1427663444593115826L;
	
	private Integer id;
	
	private Integer partnerCompanyId;
	
	private String partnerFullName;
	
	private String partnerEmailId;
	
	private String partnerCompanyName;
	
	private String partnerCompanyLogo;
	
	private Integer contactsLimit;
	
	private Double mdfAmount;
	
	private boolean disableNotifyPartnersOption;
	
	private boolean notifyPartners;

	private String viewType;

	private Integer representingPartnerId;
	
	private Integer vendorCompanyId;
	
	private String partnerSalesforceAccountId;
	
	private String partnerSalesforceAccountName;
	
	private String status;
	
	private String domainName;
	
	private Integer domainId;
	
	private boolean domainDeactivated;
	
}
