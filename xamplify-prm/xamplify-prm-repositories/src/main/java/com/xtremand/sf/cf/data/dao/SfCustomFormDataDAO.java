package com.xtremand.sf.cf.data.dao;

import java.util.List;

import com.xtremand.deal.bom.Deal;
import com.xtremand.form.bom.FormLabel;
import com.xtremand.lead.bom.Lead;
import com.xtremand.salesforce.bom.SfCustomFieldsData;

public interface SfCustomFormDataDAO {
	
	void save(List<SfCustomFieldsData> sfCfFieldsData);
	
	void update(List<SfCustomFieldsData> sfCfFieldsData);
	
	public void saveSfCfData(SfCustomFieldsData sfCustomFieldsData);
	
	public void updateSfCfData(SfCustomFieldsData sfCustomFieldsData);

	public void deleteCustomFormLabelByFieldId(Integer fieldId);

	public SfCustomFieldsData getSfCustomFieldDataByDealIdAndLabelId(Deal deal, FormLabel formLabel);

	public boolean checkIfRecordExists(Integer formLabelId);
	
	public SfCustomFieldsData getSfCustomFieldDataByLeadIdAndLabelId(Lead lead, FormLabel formLabel);

}
