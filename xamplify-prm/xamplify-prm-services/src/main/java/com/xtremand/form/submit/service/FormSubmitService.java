package com.xtremand.form.submit.service;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.springframework.web.multipart.MultipartFile;

import com.xtremand.common.bom.Pagination;
import com.xtremand.form.submit.dto.FormSubmitAnswerDTO;
import com.xtremand.form.submit.dto.FormSubmitDTO;
import com.xtremand.formbeans.UserDTO;
import com.xtremand.formbeans.XtremandResponse;
import com.xtremand.mdf.bom.MdfRequest;

public interface FormSubmitService {

	public XtremandResponse save(FormSubmitDTO formSubmitDTO, MdfRequest mdfRequest);

	public XtremandResponse update(FormSubmitDTO formSubmitDTO, Integer formSubmitId);

	public XtremandResponse exportFormAnalyticsByFormAlias(String alias, Pagination pagination);

	public XtremandResponse listAnalyticsByFormAlias(String alias, Pagination pagination);

	public void exportPublicEventCampaignLeads(Integer campaignId, boolean isTotalAttendees,
			boolean isTotalPartnerLeads, HttpServletResponse httpResponse);

	public XtremandResponse listTotalAttendessForCheckIn(Pagination pagination);

	public void exportCheckInList(Integer campaignId, HttpServletResponse httpResponse);

	public void downloadFormAnalytics(String formAlias, Integer userId, String searchKey,
			HttpServletResponse httpResponse);

	public void downloadLandingPageFormAnalytics(String formAlias, Integer landingPageId, Integer userId,
			HttpServletResponse httpResponse);

	public void downloadCampaignLandingPageFormAnalytics(String formAlias, Integer campaignId, Integer userId,
			HttpServletResponse httpResponse);

	public void downloadCampaignPartnerFormAnalytics(String formAlias, Integer campaignId, Integer partnerId,
			Integer userId, HttpServletResponse httpResponse);

	public void downloadPartnerLandingPageFormAnalytics(String landingPageAlias, Integer formId, Integer userId,
			HttpServletResponse httpResponse);

	public XtremandResponse uploadFile(MultipartFile uploadedFile, FormSubmitDTO formSubmitDTO);

	public XtremandResponse saveLmsForm(FormSubmitDTO formSubmitDTO, MdfRequest mdfRequest);

	public XtremandResponse getAnalytics(String alias, Pagination pagination);

	public XtremandResponse getDetailedResponse(Integer formSubmitId);

	public XtremandResponse getSurveyAnalytics(String alias, Integer campaignId, Integer partnerId);

	public Map<Integer, FormSubmitAnswerDTO> getQuizFormSubmittedData(Integer FormSubmitId);

	/***** XNFR-467 *****/
	public HttpServletResponse downloadSurveyAnalytics(Integer formSubmitId, HttpServletResponse httpResponse);

	/***** XNFR-467 *****/
	public XtremandResponse downloadSurveyAnalytics(String alias, HttpServletResponse httpResponse, Integer campaignId);

	public List<UserDTO> formSubmissionUsers(Integer formId);
	
	public XtremandResponse convertFormSubmissionToUsersDto(Integer formSubmitId);
}
