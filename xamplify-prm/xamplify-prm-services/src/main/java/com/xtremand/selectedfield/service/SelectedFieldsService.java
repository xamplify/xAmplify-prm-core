package com.xtremand.selectedfield.service;

import org.springframework.validation.BindingResult;

import com.xtremand.form.dto.SelectedFieldsResponseDTO;
import com.xtremand.formbeans.XtremandResponse;

public interface SelectedFieldsService {

	XtremandResponse saveOrUpdateSelectedFields(SelectedFieldsResponseDTO selectedFieldsResponseDto, BindingResult result);

	XtremandResponse isMyPreferances(Integer userId, String companyProfileName,String opportunityType);

	/** XNFR-840 **/
	XtremandResponse getExportExcelColumns(String companyProfileName, String userType, Integer userId, String customFormName,
			String opportunityType,boolean myprofile);
	/** XNFR-840 **/
}
