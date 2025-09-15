package com.xtremand.gdpr.setting.service;

import java.util.List;

import com.xtremand.formbeans.XtremandResponse;
import com.xtremand.gdpr.setting.dto.GdprSettingDTO;
import com.xtremand.gdpr.setting.dto.LegalBasisSaveRequest;
import com.xtremand.user.bom.LegalBasis;

public interface GdprSettingService {

	XtremandResponse save(GdprSettingDTO gdprSettingDto);
	
	XtremandResponse update(GdprSettingDTO gdprSettingDto);
	
	XtremandResponse getByCompanyId(Integer companyId);

	XtremandResponse getLegalBasis(Integer companyId);
	
	XtremandResponse saveLegalBasis(LegalBasisSaveRequest request);
	
	boolean isGdprEnabled(Integer companyId);

	List<LegalBasis> getSelectByDefaultLegalBasis();
	
	public void removeLegalBasis(List<Integer> userIdsList, List<Integer> userListIds);

}
