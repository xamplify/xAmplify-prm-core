package com.xtremand.selectedfields.dao;

import java.util.List;

import com.xtremand.form.bom.FormTypeEnum;
import com.xtremand.form.bom.SelectedFields;
import com.xtremand.form.dto.SelectedFieldsDTO;

public interface SelectedFieldsDAO {

	
	List<SelectedFieldsDTO> getAllSelectedFieldsByLoggedInuserIdAndOpportunity(Integer loggedInUserId,Integer formId,String opportunityType);
	
	List<SelectedFieldsDTO> getFormFieldsByFormId(Integer formId,String userType, FormTypeEnum formType);

	boolean isMyPreferances(Integer userId, Integer formId,String opportunityType);

	Integer deleteUnSelectedFieldsByIds(List<Integer> unselectedIds);

	SelectedFields getSelectedFieldByLableId(String id,Integer userId,Integer formId,String opportunityType);
	
	/*** XNFR-840 ***/
	boolean isFieldsByFormId(Integer formId,String opportunityType);

	List<SelectedFieldsDTO> getSelectedFieldsByFormIdAndOpportunityType(Integer formId, String opportunityType);
	
	List<String> getPrivateFormLableIdsFormFormId(Integer formId,String userType,FormTypeEnum formType);
}
