package com.xtremand.controller;

import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.xtremand.common.bom.Pagination;
import com.xtremand.exception.DuplicateEntryException;
import com.xtremand.form.dao.FormDao;
import com.xtremand.form.dto.FormDTO;
import com.xtremand.form.exception.FormDataAccessException;
import com.xtremand.form.service.FormService;
import com.xtremand.form.submit.service.FormSubmitService;
import com.xtremand.formbeans.XtremandResponse;
import com.xtremand.util.service.UtilService;

@RestController
@RequestMapping(value = "/form/")
public class FormController {

	@Autowired
	private FormService formService;

	@Autowired
	private FormSubmitService formSubmitService;

	@Autowired
	private UtilService utilService;

	@Value("${web_url}")
	private String webUrl;

	@Value("${server_url}")
	private String serverUrl;

	@Autowired
	private FormDao formDao;

	@PostMapping(value = "save", consumes = { "multipart/form-data" })
	@ResponseBody
	public ResponseEntity<XtremandResponse> save(
			@RequestPart(value = "thumbnailImage", required = false) MultipartFile thumbnailImageFile,
			@RequestPart("formDto") FormDTO formDto) {
		return ResponseEntity.ok(formService.save(formDto, thumbnailImageFile));
	}

	@PostMapping(value = "update", consumes = { "multipart/form-data" })
	@ResponseBody
	public ResponseEntity<XtremandResponse> update(
			@RequestPart(value = "thumbnailImage", required = false) MultipartFile thumbnailImageFile,
			@RequestPart("formDto") FormDTO formDto) {
		return ResponseEntity.ok(formService.update(formDto, thumbnailImageFile));
	}

	@GetMapping(value = "listFormNames/{userId}")
	@ResponseBody
	public List<String> listFormNames(@PathVariable Integer userId,
			@RequestParam(required = false, defaultValue = "") String companyProfileName) {
		return formService.listFormNames(userId, companyProfileName);
	}

	@PostMapping(value = "getById")
	@ResponseBody
	public XtremandResponse getById(@RequestBody FormDTO formInputDto) {
		return formService.getById(formInputDto);
	}

	@PostMapping(value = "list")
	@ResponseBody
	public ResponseEntity<XtremandResponse> list(@RequestBody Pagination pagination) {
		return ResponseEntity.ok(formService.list(pagination));
	}

	@GetMapping(value = "delete/{id}/{userId}")
	@ResponseBody
	public ResponseEntity<XtremandResponse> delete(@PathVariable Integer id, @PathVariable Integer userId,
			@RequestParam(required = false, defaultValue = "") String companyProfileName) {
		try {
			return ResponseEntity.ok(formService.deleteById(id, userId, companyProfileName));
		} catch (FormDataAccessException e) {
			throw new FormDataAccessException(e);
		}
	}

	@GetMapping(value = "deleteDefaultForm/{formId}")
	@ResponseBody
	public ResponseEntity<XtremandResponse> deleteDefaultForm(@PathVariable Integer formId) {
		try {
			return ResponseEntity.ok(formService.deleteDefaultForm(formId));
		} catch (DuplicateEntryException e) {
			throw new DuplicateEntryException(e.getMessage());
		} catch (FormDataAccessException u) {
			throw new FormDataAccessException(u);
		} catch (Exception e) {
			throw new FormDataAccessException(e);
		}
	}

	@PostMapping(value = "analytics/{alias}")
	@ResponseBody
	public ResponseEntity<XtremandResponse> listNew(@PathVariable String alias, @RequestBody Pagination pagination) {
		return ResponseEntity.ok(formSubmitService.listAnalyticsByFormAlias(alias, pagination));
	}

	@PostMapping(value = "partner-landingPage/analytics")
	@ResponseBody
	public ResponseEntity<XtremandResponse> listPartnerLandingPageFormAnalytics(@RequestBody Pagination pagination) {
		return ResponseEntity.ok(formSubmitService.listAnalyticsByFormAlias("", pagination));
	}

	@PostMapping(value = "checkIn/analytics")
	@ResponseBody
	public ResponseEntity<XtremandResponse> checkInAnalytics(@RequestBody Pagination pagination) {
		return ResponseEntity.ok(formSubmitService.listTotalAttendessForCheckIn(pagination));
	}

	@GetMapping(value = "download/fa/{formAlias}/{userId}")
	@ResponseBody
	public void downloadFormAnalytics(@PathVariable String formAlias, @PathVariable Integer userId,
			@RequestParam String searchKey, HttpServletResponse httpResponse) {
		formSubmitService.downloadFormAnalytics(formAlias, userId, searchKey, httpResponse);
	}

	@GetMapping(value = "download/lpfa/{formAlias}/{landingPageId}/{userId}")
	@ResponseBody
	public void downloadLandingPageFormAnalytics(@PathVariable String formAlias, @PathVariable Integer landingPageId,
			@PathVariable Integer userId, HttpServletResponse httpResponse) {
		formSubmitService.downloadLandingPageFormAnalytics(formAlias, landingPageId, userId, httpResponse);
	}

	@GetMapping(value = "download/cpfa/{formAlias}/{campaignId}/{partnerId}/{userId}")
	@ResponseBody
	public void downloadCampaignPartnerFormAnalytics(@PathVariable String formAlias, @PathVariable Integer campaignId,
			@PathVariable Integer partnerId, @PathVariable Integer userId, HttpServletResponse httpResponse) {
		formSubmitService.downloadCampaignPartnerFormAnalytics(formAlias, campaignId, partnerId, userId, httpResponse);
	}

	@GetMapping(value = "download/plpf/{landingPageAlias}/{formId}/{userId}")
	@ResponseBody
	public void downloadPartnerLandingPageFormAnalytics(@PathVariable String landingPageAlias,
			@PathVariable Integer formId, @PathVariable Integer userId, HttpServletResponse httpResponse) {
		formSubmitService.downloadPartnerLandingPageFormAnalytics(landingPageAlias, formId, userId, httpResponse);
	}

	@GetMapping("/getCompanyLogoPath/{userId}")
	public String getCompanyLogoPath(@PathVariable Integer userId) {
		return utilService.getCompanyLogoPath(userId);
	}

	@GetMapping("/price-types")
	public XtremandResponse getPriceTypes() {
		return formService.getPriceTypes();
	}

	@PostMapping(value = "quiz-list")
	@ResponseBody
	public ResponseEntity<XtremandResponse> quizList(@RequestBody Pagination pagination) {
		return ResponseEntity.ok(formService.quizList(pagination));
	}

	@PostMapping(value = "/default/list")
	@ResponseBody
	public ResponseEntity<XtremandResponse> listDefaultForms(@RequestBody Pagination pagination) {
		return ResponseEntity.ok(formService.listDefaultForms(pagination));
	}

	@GetMapping(value = "survey/analytics/{alias}")
	@ResponseBody
	public ResponseEntity<XtremandResponse> getSurveyAnalytics(@PathVariable String alias) {
		return ResponseEntity.ok(formSubmitService.getSurveyAnalytics(alias, null, null));
	}

	@GetMapping(value = "survey/analytics/{alias}/{campaignId}")
	@ResponseBody
	public ResponseEntity<XtremandResponse> getSurveyAnalytics(@PathVariable String alias,
			@PathVariable Integer campaignId) {
		return ResponseEntity.ok(formSubmitService.getSurveyAnalytics(alias, campaignId, null));
	}

	@GetMapping(value = "survey/analytics/{alias}/{campaignId}/{partnerId}")
	@ResponseBody
	public ResponseEntity<XtremandResponse> getSurveyAnalytics(@PathVariable String alias,
			@PathVariable Integer campaignId, @PathVariable Integer partnerId) {
		return ResponseEntity.ok(formSubmitService.getSurveyAnalytics(alias, campaignId, partnerId));
	}

	@PostMapping(value = "/analytics/list/{alias}")
	@ResponseBody
	public ResponseEntity<XtremandResponse> getAnalytics(@PathVariable String alias,
			@RequestBody Pagination pagination) {
		return ResponseEntity.ok(formSubmitService.getAnalytics(alias, pagination));
	}

	@GetMapping(value = "analytics/response/details/{formSubmitId}")
	@ResponseBody
	public ResponseEntity<XtremandResponse> getDetailedResponse(@PathVariable Integer formSubmitId) {
		return ResponseEntity.ok(formSubmitService.getDetailedResponse(formSubmitId));
	}

	/***** XNFR-467 *****/
	@GetMapping(value = "survey/download/{formSubmitId}")
	public ResponseEntity<?> downloadSurveyAnalytics(@PathVariable Integer formSubmitId,
			HttpServletResponse httpResponse) {
		return ResponseEntity.ok(formSubmitService.downloadSurveyAnalytics(formSubmitId, httpResponse));
	}

	@GetMapping(value = "survey/analytics/download/{alias}/{campaignId}")
	public ResponseEntity<XtremandResponse> downloadSurveyAnalytics(@PathVariable String alias,
			@PathVariable Integer campaignId, HttpServletResponse httpResponse) {
		return ResponseEntity.ok(formSubmitService.downloadSurveyAnalytics(alias, httpResponse, campaignId));

	}

	/******* XNFR-522 *******/
	@GetMapping(value = "vendorJourney/createDefaultPartnerForm")
	public ResponseEntity<XtremandResponse> createDefaultPartnerForm() {
		return ResponseEntity.ok(formService.createdDefaultForms(true));

	}

	@GetMapping(value = "vendorJourney/createDefaultVendorForm")
	public ResponseEntity<XtremandResponse> createDefaultVendorForm() {
		return ResponseEntity.ok(formService.createdDefaultForms(false));

	}

	@GetMapping(value = "{formId}")
	@ResponseBody
	public void getForSubmitId(@PathVariable Integer formId) {
		formDao.findSubmittedDataByFormId(formId);
	}

	@GetMapping(value = "/getUserDtoByFormSubmitId/{formSubmitId}")
	public ResponseEntity<XtremandResponse> listDealTypesByCompanyId(@PathVariable Integer formSubmitId) {
		return ResponseEntity.ok(formSubmitService.convertFormSubmissionToUsersDto(formSubmitId));
	}
}
