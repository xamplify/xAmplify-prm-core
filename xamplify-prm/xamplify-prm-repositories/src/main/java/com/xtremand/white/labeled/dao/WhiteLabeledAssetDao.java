package com.xtremand.white.labeled.dao;

import java.util.List;

import com.xtremand.white.labeled.dto.WhiteLabeledContentDTO;

public interface WhiteLabeledAssetDao {

	/******** DAM *********/

	public WhiteLabeledContentDTO findSharedByVendorCompanyNameByAssetId(Integer receivedWhiteLabeledAssetId);

	public boolean isVendorCompanyAssetWhiteLabeledAndSharedWithPartners(Integer vendorCompanyAssetId);

	public boolean isWhiteLabeledAssetSharedWithPartnerCompany(Integer vendorCompanyAssetId, Integer vendorCompanyId,
			Integer partnerCompanyId);

	public List<Integer> findPartnerCompanyIdsByVendorAssetId(Integer assetId);

}
