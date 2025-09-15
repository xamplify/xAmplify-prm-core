package com.xtremand.white.labeled.dao;

import java.util.List;

import com.xtremand.white.labeled.dto.WhiteLabeledContentDTO;
import com.xtremand.white.labeled.dto.WhiteLabeledFormDTO;

public interface WhiteLabeledFormDao {

	/***** Forms ****/

	public boolean isWhiteLabeledFormSharedWithPartnerCompanyId(Integer vendorCompanyFormId, Integer vendorCompanyId,
			Integer partnerCompanyId);

	public WhiteLabeledContentDTO findSharedByVendorCompanyNameByForm(Integer receivedWhiteLabeledFormId);

	public boolean isVendorCompanyFormWhiteLabeledAndSharedWithPartners(Integer id);

	public List<Integer> findSharedVendorCompanyFormIds(Integer vendorCompanyId, Integer partnerCompanyId);

	public List<WhiteLabeledFormDTO> findWhiteLabeledForms(Integer vendorCompanyId, Integer partnerCompanyId);

}
