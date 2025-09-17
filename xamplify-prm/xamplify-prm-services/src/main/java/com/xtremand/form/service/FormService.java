package com.xtremand.form.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.xtremand.common.bom.Pagination;
import com.xtremand.form.bom.Form;
import com.xtremand.form.bom.FormLabel;
import com.xtremand.form.bom.FormLabelChoice;
import com.xtremand.form.dto.FormDTO;
import com.xtremand.form.dto.FormLabelDTO;
import com.xtremand.formbeans.XtremandResponse;

public interface FormService {

	XtremandResponse save(FormDTO formDto, MultipartFile thumbnailImageFile);

	XtremandResponse update(FormDTO formDto, MultipartFile thumbnailImageFile);

	List<String> listFormNames(Integer userId, String companyProfileName);

	XtremandResponse list(Pagination pagination);

	XtremandResponse deleteById(Integer id, Integer userId, String companyProfileName);

	XtremandResponse getById(FormDTO formInputDto);

	XtremandResponse getByAlias(String alias, boolean vendorJourney, boolean isPartnerJourneyPage);

	List<FormLabelDTO> listFormLabelsById(Integer id);

	public void setFormDto(XtremandResponse response, Form form, boolean isFromAlias, FormDTO formDTO,
			FormDTO formInputDto);

	XtremandResponse getPriceTypes();

	XtremandResponse getMdfFormByCompanyId(Integer companyId, boolean showingForPartner);

	XtremandResponse quizList(Pagination pagination);

	XtremandResponse listDefaultForms(Pagination pagination);

	XtremandResponse deleteDefaultForm(Integer formId);

	public XtremandResponse createdDefaultForms(boolean isPartner);

	public void saveFormLabel(Form form, int i, FormLabelDTO formLabelDTO, FormLabel formLabel,
			List<FormLabelChoice> formLabelChoices, XtremandResponse response, int columnOrder);

	public XtremandResponse updateCustomFormDetails(FormDTO formDto, Form form, XtremandResponse response);
}
