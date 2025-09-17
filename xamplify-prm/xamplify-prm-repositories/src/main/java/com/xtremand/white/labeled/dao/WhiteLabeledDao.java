package com.xtremand.white.labeled.dao;

import java.util.List;

import com.xtremand.white.labeled.dto.WhiteLabeledContentDTO;

public interface WhiteLabeledDao {

	public boolean isVendorCompanyContentWhiteLabeledAndSharedWithPartners(Integer vendorCompanyContentId,
			String tableName, String columnName);

	public WhiteLabeledContentDTO findSharedByVendorCompanyNameByContentId(Integer receivedWhiteLabeledEmailTemplateId,
			String tableName, String columnName);

	boolean isWhiteLabeledContentSharedWithPartnerCompany(Integer contentId, Integer vendorCompanyId,
			Integer partnerCompanyId, String tableName, String vendorCompanyEmailTemplateIdColumnName);

	public List<Integer> findPartnerCompanyIdsByContentId(Integer assetId, String tableName,
			String columnName);
	

}
