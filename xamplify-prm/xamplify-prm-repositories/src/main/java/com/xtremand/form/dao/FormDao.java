package com.xtremand.form.dao;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.xtremand.common.bom.Pagination;
import com.xtremand.dao.util.FinderDAO;
import com.xtremand.form.bom.Form;
import com.xtremand.form.bom.FormLabel;
import com.xtremand.form.bom.FormLabelChoice;
import com.xtremand.form.bom.FormLabelType;
import com.xtremand.form.bom.FormTypeEnum;
import com.xtremand.form.dto.FormChoiceDTO;
import com.xtremand.form.dto.FormDTO;
import com.xtremand.form.dto.FormLabelDTO;
import com.xtremand.form.submit.bom.FormSubmit;
import com.xtremand.formbeans.XtremandResponse;
import com.xtremand.integration.dto.CustomChoicesDTO;
import com.xtremand.salesforce.dto.OpportunityFormFieldsDTO;

public interface FormDao extends FinderDAO<Form> {

	public void save(Form form);

	public void update(Form form);

	public void save(FormLabel formLabel);

	public void update(FormLabel formLabel);

	public void save(FormLabelChoice formLabelChoice);

	public void update(FormLabelChoice formLabelChoice);

	public FormLabelType findByType(String type);

	public List<String> listFormNamesByCompanyId(Integer userId, String companyProfileName);

	public XtremandResponse deleteById(Integer id, XtremandResponse response);

	public Form getById(Integer id);

	public Form getByAlias(String alias);

	public FormLabel getFormLabelId(Integer id);

	public FormLabelChoice getFormLabelChoiceById(Integer choiceId);

	public void deleteFormLabelByIds(List<Integer> ids);

	public void deleteFormChoiceByIds(List<Integer> ids);

	public List<FormLabelDTO> listFormLabelsById(Integer id, boolean totalLeads, boolean mdfForm,
			Integer additionalColumnsSize);

	public Form getFormIdByAlias(String alias);

	public List<String> listAliasesByUserId(Integer userId);

	public List<FormDTO> listFormIdAndAliasesByUserId(Integer userId);

	public List<FormDTO> findDefaultFormIdsAndAliases();

	public List<FormDTO> listFormIdAndAliasesByCompanyId(Integer companyId);

	boolean isFormExists(String alias);

	public Integer getFormCountByCampaignId(Integer campaignId);

	public Integer getSubmittedCountByFormId(Integer formId);

	public Integer getSubmittedCountByFormIdAndCampaignId(Integer formId, Integer campaignId);

	public Integer getSubmittedCountByFormIdAndLandingPageId(Integer formId, Integer landingPageId);

	public Integer getLandingPageCampaignFormSubmitCount(Integer formId, Integer campaignId, Integer landingPageId);


	public String getFormAliasByCampaignId(Integer campaignId);

	List<String> getLeadEmailIdsByCampaignFormSubmittedIds(List<Integer> formCampaignSubmitIds);

	List<String> getLeadEmailIdsForVendorAndPartnerCampaigns(Integer campaignId);

	public Integer getSfCustomFormIdByCompanyIdAndUserIdAndFormType(Integer companyId, Integer userId,
			FormTypeEnum formType);

	public List<FormLabel> listSfCustomLabelsByFormId(Form form);

	public void deleteSfCustomLabelById(Integer id);

	public FormLabel getFormLabelByFormIdAndLabelId(Form form, String labelId);

	public String getFormAliasByPageFormAlias(String pageFormAlias);

	public Form getMDFFormByCompanyId(Integer companyId);

	public Date getDefaultMdfFormCreatedDate(Integer companyId);

	public Integer getSfCustomFormIdByCompanyIdAndFormType(Integer companyId, FormTypeEnum formType);

	public Map<String, Object> findQuizList(Pagination pagination);

	public void deleteLearningTrackQuizSubmissions(Integer learningTrackId);

	public FormSubmit getLearningTrackFormSubmission(Integer learningTrackId, Integer userId);

	public Map<String, Object> listDefaultForms(Pagination pagination);

	public List<String> findDefaultFormAliases();

	public void delete(Integer formId);

	public Integer findFormsCountByUserId(Integer userId);

	public List<Form> getAllForms();

	public Integer getSubmissionsCount(Integer formId, Integer campaignId, Integer partnerCompanyId);

	public Integer getFormSubmitByFormCampaignAndUser(Integer formId, Integer campaignId, Integer userId);

	/******* XNFR-108 *********/

	public void findAndDeleteFormTeamMemberGroupMappingByFormIdAndTeamMemberGroupMappingIds(Integer formId,
			List<Integer> teamMemberGroupMappingIds);

	public boolean isFormTeamMemberGroupMappingExists(Integer formId, Integer teamMemberGroupUserMappingId);

	public List<Integer> getAllSelecetedGroupIds(Integer formId);

	public List<Integer> getAllSelecetedTeamMemberIds(Integer formId, Integer groupId);

	public List<Integer> getSelectedTeamMemberIdsByFormId(Integer formId);

	/******* XNFR-108 ENDS *********/

	public void updateMaxScore(Integer formId);

	public FormSubmit getLearningTrackFormSubmissionByFormID(Integer learningTrackId, Integer userId, Integer formId);

	public void deleteLearningTrackQuizSubmissionsByUserId(Integer learningTrackId, Integer userId);

	public void deleteLearningTrackQuizSubmissionsByFormId(Integer learningTrackId, Integer formId);

	/**** XNFR-255 ****/
	public List<FormLabelChoice> findFormLabelChoicesByFormLabelId(Integer formLabelId);

	public List<String> findFormAliasesByFormIds(List<Integer> formIds);

	public List<String> findFormLandingPageAliasesByFormIdsAndLandingPageId(List<Integer> formIds,
			Integer landingPageId);

	public String getFormAliasByFormId(Integer formId);

	/***** XBI-2332 *****/
	public String getFromSubType(String alias);

	public boolean checkIfRecordExists(Integer formLabelId);

	/***** XNFR-522 *****/

	public Integer getDefaulatVendorJourneyForm(boolean isVendorJourney);

	public boolean checkFormNameExists(String formname);

	public String getDefaultDependentChoicesJson();

	public Integer getIdByParentChoice(String parentChoice, OpportunityFormFieldsDTO parentLabel);

	public List<CustomChoicesDTO> getCustomChoicesData();

	public List<CustomChoicesDTO> getCustomChoicesDataByParentChoice(String parentLabelId, String parentChoice);

	public List<FormLabelDTO> getFormLabelDtoByFormId(Integer formId);

	public List<FormLabelDTO> getFormLabelDtoByFormIdForCSV(Integer formId);

	public List<FormDTO> getFormIdAndFormNamesByCompanyId(Integer companyId);

	/** XNFR-730 ****/
	public List<FormSubmit> findSubmittedDataByFormId(Integer formId);

	public List<FormChoiceDTO> getFormLabelChoices(Integer formLabelId);

	public List<FormLabelDTO> getFormLabelDtoByFormIdForCSVXamplifyIntegration(Integer formId);

}
