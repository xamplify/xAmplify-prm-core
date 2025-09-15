package com.xtremand.customfields.service;

import com.xtremand.custom.field.dto.CustomFieldsDTO;
import com.xtremand.form.dto.FormDTO;
import com.xtremand.formbeans.XtremandResponse;
import com.xtremand.lead.bom.OpportunityType;
import com.xtremand.user.bom.User;

public interface CustomFieldsService {
	
	public XtremandResponse syncCustomForm(CustomFieldsDTO customFieldsDTO);

	public XtremandResponse saveCustomField(CustomFieldsDTO customFieldsDTO);

	public XtremandResponse getCustomFields(Integer loggedInUserId, OpportunityType opportunityType);

	public XtremandResponse deleteCustomField(Integer customFieldId, Integer loggedInUserId);
	
	public FormDTO getCustomForm(Integer companyId, Integer opportunityId, User loggedInUser, Long halopsaTicketTypeId,
			OpportunityType opportunityType);

	public XtremandResponse getLeadCountForCustomField(Integer customFieldId);
	
}
