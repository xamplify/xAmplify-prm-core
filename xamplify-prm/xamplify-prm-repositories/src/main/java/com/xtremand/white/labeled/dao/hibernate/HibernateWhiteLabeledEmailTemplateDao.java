package com.xtremand.white.labeled.dao.hibernate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.xtremand.white.labeled.dao.WhiteLabeledDao;
import com.xtremand.white.labeled.dao.WhiteLabeledEmailTemplateDao;
import com.xtremand.white.labeled.dto.WhiteLabeledContentDTO;

@Repository
public class HibernateWhiteLabeledEmailTemplateDao implements WhiteLabeledEmailTemplateDao {
	
	/********** Email Templates ************/
	private static final String XT_WHITE_LABELED_EMAIL_TEMPLATES = "xt_white_labeled_email_templates";

	private static final String VENDOR_COMPANY_EMAIL_TEMPLATE_ID = "vendor_company_email_template_id";

	@Autowired
	private WhiteLabeledDao whiteLabeledDao;

	@Override
	public WhiteLabeledContentDTO findSharedByVendorCompanyNameByEmailTemplateId(
			Integer receivedWhiteLabeledEmailTemplateId) {
		return whiteLabeledDao.findSharedByVendorCompanyNameByContentId(receivedWhiteLabeledEmailTemplateId, XT_WHITE_LABELED_EMAIL_TEMPLATES,
				"received_white_labeled_email_template_id");
	}

	@Override
	public boolean isVendorCompanyEmailTemplateWhiteLabeledAndSharedWithPartners(Integer vendorCompanyEmailTemplateId) {
		return whiteLabeledDao.isVendorCompanyContentWhiteLabeledAndSharedWithPartners(vendorCompanyEmailTemplateId, XT_WHITE_LABELED_EMAIL_TEMPLATES, VENDOR_COMPANY_EMAIL_TEMPLATE_ID);
	}

	@Override
	public boolean isWhiteLabeledTemplateSharedWithPartnerCompany(Integer vendorCompanyEmailTemplateId,
			Integer vendorCompanyId, Integer partnerCompanyId) {
		return whiteLabeledDao.isWhiteLabeledContentSharedWithPartnerCompany(vendorCompanyEmailTemplateId, vendorCompanyId, partnerCompanyId, XT_WHITE_LABELED_EMAIL_TEMPLATES, VENDOR_COMPANY_EMAIL_TEMPLATE_ID);
	}

}
