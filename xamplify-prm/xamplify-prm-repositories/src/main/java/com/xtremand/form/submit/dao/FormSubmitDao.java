package com.xtremand.form.submit.dao;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.xtremand.common.bom.Criteria;
import com.xtremand.common.bom.FindLevel;
import com.xtremand.common.bom.Pagination;
import com.xtremand.dao.util.FinderDAO;
import com.xtremand.form.submit.bom.FormSubmit;
import com.xtremand.form.submit.bom.FormSubmitMultiChoice;
import com.xtremand.form.submit.dto.FormSubmitFieldsValuesDTO;
import com.xtremand.formbeans.EventCheckInDTO;
import com.xtremand.formbeans.UserDTO;

public interface FormSubmitDao extends FinderDAO<FormSubmit> {

	void save(FormSubmit formSubmit);

	public Map<String, Object> list(List<Criteria> criterias, Pagination pagination, Integer formId);

	public FormSubmit getFormSubmitById(Integer id);

	public UserDTO getVendorOrPartnerData(Integer formSubmitId, Integer campaignId,
			Set<Integer> redistributedCampaignIds);


	public EventCheckInDTO getCheckedInDetails(Integer formSumbitId);

	public Integer getFormSubmitIdByTrackId(Integer userId, Integer learningTrackId, Integer quizId);

	public Map<String, Object> getSubmissionDetails(Pagination pagination);

	public List<FormSubmit> getSubmissions(Integer formId, Integer campaignId, Integer partnerCompanyId);

	public Map<String, Object> findMdfFormSubmittedDetails(List<Criteria> criterias, FindLevel[] levels,
			Pagination pagination);

	public FormSubmit getFormSubmitByTrackId(Integer userId, Integer learningTrackId, Integer quizId);

	public List<Object[]> getFormSubmitSingleChoiceData(Integer formSubmitId);

	public List<FormSubmitMultiChoice> getFormSubmitMultiChoiceData(Integer formSubmitId);
	
	/******XNFR-583*******/
	public Map<String, Object> findMasterLandingPageDetailsByFormId(Integer formId, Pagination pagination);
	
	public Map<String, Object> getVendorDetailsRhoughMasterLandingPageSubmissions(Pagination pagination, Integer masterLandingPageId);
	

	/******XNFR-766*******/
	public List<FormSubmitFieldsValuesDTO> getFormSubmitValueDetailsById(Integer formSubmitId);
}
