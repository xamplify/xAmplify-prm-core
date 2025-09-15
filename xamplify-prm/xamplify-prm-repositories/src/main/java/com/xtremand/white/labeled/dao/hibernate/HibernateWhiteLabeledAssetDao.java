package com.xtremand.white.labeled.dao.hibernate;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.xtremand.white.labeled.dao.WhiteLabeledAssetDao;
import com.xtremand.white.labeled.dao.WhiteLabeledDao;
import com.xtremand.white.labeled.dto.WhiteLabeledContentDTO;

@Repository
public class HibernateWhiteLabeledAssetDao implements WhiteLabeledAssetDao {

	/********** DAM ************/
	private static final String XT_WHITE_LABELED_ASSETS = "xt_white_labeled_assets";

	private static final String VENDOR_COMPANY_ASSET_ID = "vendor_company_asset_id";

	@Autowired
	private WhiteLabeledDao whiteLabeledDao;

	@Override
	public WhiteLabeledContentDTO findSharedByVendorCompanyNameByAssetId(Integer receivedWhiteLabeledAssetId) {
		return whiteLabeledDao.findSharedByVendorCompanyNameByContentId(receivedWhiteLabeledAssetId,
				XT_WHITE_LABELED_ASSETS, "received_white_labeled_asset_id");
	}

	@Override
	public boolean isVendorCompanyAssetWhiteLabeledAndSharedWithPartners(Integer vendorCompanyAssetId) {
		return whiteLabeledDao.isVendorCompanyContentWhiteLabeledAndSharedWithPartners(vendorCompanyAssetId,
				XT_WHITE_LABELED_ASSETS, VENDOR_COMPANY_ASSET_ID);

	}

	@Override
	public boolean isWhiteLabeledAssetSharedWithPartnerCompany(Integer vendorCompanyAssetId, Integer vendorCompanyId,
			Integer partnerCompanyId) {
		return whiteLabeledDao.isWhiteLabeledContentSharedWithPartnerCompany(vendorCompanyAssetId, vendorCompanyId,
				partnerCompanyId, XT_WHITE_LABELED_ASSETS, VENDOR_COMPANY_ASSET_ID);
	}

	@Override
	public List<Integer> findPartnerCompanyIdsByVendorAssetId(Integer assetId) {
		return whiteLabeledDao.findPartnerCompanyIdsByContentId(assetId, XT_WHITE_LABELED_ASSETS,
				VENDOR_COMPANY_ASSET_ID);
	}

}
