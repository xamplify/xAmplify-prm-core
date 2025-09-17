package com.xtremand.gdpr.setting.dao;

import java.util.List;

import com.xtremand.gdpr.setting.bom.GdprSetting;
import com.xtremand.gdpr.setting.bom.GdprSettingView;
import com.xtremand.user.bom.LegalBasis;

public interface GdprSettingDao {

	void save(GdprSetting gdprSetting);
	
	GdprSettingView getByCompanyId(Integer companyId);
	
	GdprSetting getSettingByCompanyId(Integer companyId);
	
	List<LegalBasis> getLegalBasisListForCompany(Integer companyId);
	
	List<LegalBasis> getLegalBasisAddedByCompanyId(Integer companyId);
	
	void deleteLegalBasis(LegalBasis legalbasis);

	List<LegalBasis> getSelectByDefaultLegalBasis();
	
	public void removeLegalBasis(List<Integer> userIdsList, List<Integer> userListIds);

}
