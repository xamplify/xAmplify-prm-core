package com.xtremand.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.xtremand.activity.dto.ActivityAWSDTO;
import com.xtremand.campaign.exception.XamplifyDataAccessException;
import com.xtremand.common.bom.Pagination;
import com.xtremand.dam.exception.DamDataAccessException;
import com.xtremand.dashboard.buttons.dto.DashboardButtonsDTO;
import com.xtremand.form.emailtemplate.dto.SendTestEmailDTO;
import com.xtremand.formbeans.XtremandResponse;
import com.xtremand.mail.service.AsyncComponent;
import com.xtremand.util.BadRequestException;
import com.xtremand.vanity.url.dto.VanityURLDTO;
import com.xtremand.vanity.url.dto.VanityUrlDetailsDTO;
import com.xtremand.vanity.url.service.VanityURLService;

@RestController
@RequestMapping("/v_url/")
public class VanityURLController {

	@Autowired
	private VanityURLService vanityURLService;

	/****** XNFR-233 ***/
	@Autowired
	private AsyncComponent asyncComponent;

	/****** XNFR-233 ***/

	@GetMapping("companyDetails/{domain:.+}")
	public VanityURLDTO getVanityURLDetails(@PathVariable("domain") String domain) {
		return vanityURLService.getCompanyDetails(domain);
	}

	@GetMapping("getCompanyProfileNameByCustomDomain/{domain:.+}")
	public ResponseEntity<XtremandResponse> getCompanyProfileNameByCustomDomain(@PathVariable("domain") String domain) {
		return ResponseEntity.ok(vanityURLService.getCompanyProfileNameByCustomDomain(domain));
	}

	@GetMapping("validateUser/{companyProfileName}")
	public XtremandResponse checkUserWithCompanyProfile(@RequestParam String emailId,
			@PathVariable String companyProfileName) {
		return vanityURLService.checkUserBelongsToCompany(emailId, companyProfileName);
	}

	@GetMapping("getCompanyProfileName/{companyName}")
	public XtremandResponse checkUserWithCompanyProfile(@PathVariable String companyName) {
		return vanityURLService.getVanityCompanyProfileName(companyName);
	}

	@GetMapping("findCompanyProfileName/{companyId}")
	public XtremandResponse findCompanyProfileName(@PathVariable Integer companyId) {
		return vanityURLService.findCompanyProfileName(companyId);
	}

	@PostMapping("userRoles")
	public XtremandResponse getVanityURLRoles(@RequestParam String userName,
			@RequestBody VanityUrlDetailsDTO dashboardAnalyticsDto) {
		return vanityURLService.getVanityURLRolesForUser(userName, dashboardAnalyticsDto);
	}

	/**** XNFR-571 *******/
	@PostMapping("save/dashboardButton")
	public XtremandResponse saveDashboardButton(@RequestBody DashboardButtonsDTO dbButtonsDto) {
		boolean constraint = false;
		XtremandResponse response = new XtremandResponse();
		try {
			response = vanityURLService.saveDashboardButton(dbButtonsDto);
			return response;
		} catch (DataIntegrityViolationException e) {
			constraint = true;
			throw new DamDataAccessException(e);
		} catch (Exception ex) {
			constraint = true;
			throw new DamDataAccessException(ex);
		} finally {
			if (!constraint && response.getStatusCode() == 200) {
				asyncComponent.publishDashboardButtons(dbButtonsDto);
			}
		}
	}

	/**** XNFR-571 *******/
	@PutMapping("update/dashboardButton")
	public XtremandResponse updateDashboardButton(@RequestBody DashboardButtonsDTO dbButtonsDto) {
		boolean constraint = false;
		XtremandResponse response = new XtremandResponse();
		try {
			response = vanityURLService.updateDashboardButton(dbButtonsDto);
			return response;
		} catch (DataIntegrityViolationException e) {
			constraint = true;
			throw new DamDataAccessException(e);
		} catch (Exception ex) {
			constraint = true;
			throw new DamDataAccessException(ex);
		} finally {
			if (!constraint && response.getStatusCode() == 200) {
				asyncComponent.publishDashboardButtons(dbButtonsDto);
			}
		}

	}

	@PostMapping("getDashboardButtons")
	public XtremandResponse getDashboardButtons(@RequestBody Pagination pagination,
			@RequestParam(required = false, defaultValue = "") String searchKey) {
		return vanityURLService.getDashboardButtonsForPagination(pagination, searchKey);
	}

	@GetMapping("getDashboardButtons/{companyProfileName}/{userId}")
	public XtremandResponse getDashboardButtons(@PathVariable String companyProfileName, @PathVariable Integer userId) {
		return vanityURLService.getDashboardButtonsForCarousel(companyProfileName, userId);
	}

	@GetMapping("delete/dashboardButton/{id}")
	public XtremandResponse deleteDashboardButton(@PathVariable Integer id) {
		return vanityURLService.deleteDashboardButton(id);
	}

	/***** End XNFR-233 ****/

	@GetMapping("company/details/by/profile/name/{companyProfileName}")
	public XtremandResponse getVanityUrlDetailsbyCompanyProfileName(@PathVariable String companyProfileName) {
		return vanityURLService.getVanityUrlDetailsbyCompanyProfileName(companyProfileName);
	}

	@GetMapping("/getHtmlBody/{id}")
	public XtremandResponse getHtmlBody(@PathVariable("id") Integer id, @RequestParam("userId") Integer userId) {
		return vanityURLService.getHtmlBody(id, userId);

	}

	@GetMapping("get-template-id")
	public XtremandResponse getTemplateId(@RequestParam String emailId, @RequestParam("userId") Integer userId,
			@RequestParam("isInactivePartnersDiv") String isInactivePartnersDiv) {
		return vanityURLService.getTemplateId(emailId, userId, isInactivePartnersDiv);
	}

	@GetMapping("supportEmailIdByCompanyProfileName/{companyProfileName}")
	public XtremandResponse getSupportEmailIdByCompanyProfileName(@PathVariable String companyProfileName) {
		return vanityURLService.getSupportEmailIdByCompanyProfileName(companyProfileName);
	}

	@GetMapping("getPartnerRemainderTemplate/{loggedInUserId}")
	public XtremandResponse getPartnerRemainderTemplate(@PathVariable Integer loggedInUserId) {
		return vanityURLService.getPartnerRemainderTemplate(loggedInUserId);
	}

	@PostMapping("sendPartnerSignatureReminder")
	public XtremandResponse sendPartnerSignatureReminder(@RequestBody Pagination pagination) {
		boolean constraint = false;
		XtremandResponse response = new XtremandResponse();
		try {
			response = vanityURLService.sendPartnerSignatureReminder();
			return response;
		} catch (DataIntegrityViolationException e) {
			constraint = true;
			throw new DamDataAccessException(e);
		} catch (Exception ex) {
			constraint = true;
			throw new DamDataAccessException(ex);
		} finally {
			if (!constraint && response.getStatusCode() == 200) {
				asyncComponent.sendPartnerSignatureRemainderEmailNotification(pagination);
			}
		}
	}

//	 XNFR-972
	@GetMapping("getWelcomeTemplateForPartnerDomainWhitelisting/{loggedInUserId}")
	public XtremandResponse getWelcomeTemplateForPartnerDomainWhitelisting(@PathVariable Integer loggedInUserId) {
		return vanityURLService.getWelcomeTemplateForPartnerDomainWhitelisting(loggedInUserId);
	}

	@PostMapping(value = "sendWelcomeMailForPartnerDomainWhitelisting", consumes = { "multipart/form-data" })
	public ResponseEntity<XtremandResponse> sendTestMail(
			@RequestPart(value = "uploadedFiles") List<MultipartFile> uploadedFiles,
			@RequestPart("sendTestEmailDto") SendTestEmailDTO sendTestEmailDTO, BindingResult result) {
		XtremandResponse response = new XtremandResponse();
		try {
			response = vanityURLService.sendWelcomeMailForPartnerDomainWhitelisting(sendTestEmailDTO, uploadedFiles,
					result);
			return ResponseEntity.ok(response);
		} catch (BadRequestException e) {
			throw new BadRequestException(e.getMessage());
		} catch (Exception e) {
			throw new XamplifyDataAccessException(e);
		} finally {
			if (response.getStatusCode() != 401 && response.getData() != null) {
				@SuppressWarnings("unchecked")
				List<ActivityAWSDTO> emailActivityAWSDTOs = (List<ActivityAWSDTO>) response.getData();
				asyncComponent.uploadAttachmentFiles(emailActivityAWSDTOs);
			}
		}
	}

	@GetMapping("getEmailTemplateByType/{type}/loggedInUserId/{loggedInUserId}")
	public ResponseEntity<XtremandResponse> getEmailTemplateByType(@PathVariable String type,
			@PathVariable Integer loggedInUserId) {
		return ResponseEntity.ok(vanityURLService.getEmailTemplateByType(loggedInUserId, type));
	}
}
