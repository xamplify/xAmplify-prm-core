package com.xtremand.white.labeled.dao;

import com.xtremand.white.labeled.dto.WhiteLabeledContentDTO;

public interface WhiteLabeledEmailTemplateDao {
	
	public WhiteLabeledContentDTO findSharedByVendorCompanyNameByEmailTemplateId(
			Integer receivedWhiteLabeledEmailTemplateId);

	public boolean isVendorCompanyEmailTemplateWhiteLabeledAndSharedWithPartners(Integer vendorCompanyEmailTemplateId);

	public boolean isWhiteLabeledTemplateSharedWithPartnerCompany(Integer vendorCompanyEmailTemplateId,
			Integer vendorCompanyId, Integer partnerCompanyId);

}
