package com.xtremand.customfields.dao;

import java.util.List;

import com.xtremand.form.bom.FormLabel;
import com.xtremand.form.bom.FormTypeEnum;
import com.xtremand.salesforce.dto.OpportunityFormFieldsDTO;

public interface CustomFieldsDao {

	boolean checkFormExistByFormType(Integer companyId, FormTypeEnum formType);

	public  void updateFormFields(Integer labelId);

	public void deleteCustomField(Integer id);

	public List<OpportunityFormFieldsDTO> getDefaultLeadFilds();

	public List<FormLabel> getFormLabelsByFormId(Integer formId);

	public List<FormLabel> getSelectedFormLabelsByFormId(Integer formId);

	public Integer getLeadCountForCustomField(Integer customFieldId);

	public List<OpportunityFormFieldsDTO> getDealDefaultCustomFieldsDto();

}
