package com.xtremand.controller;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.xtremand.activity.dto.ContactOpportunityRequestDTO;
import com.xtremand.campaign.bom.DownloadDataInfo.DownloadItem;
import com.xtremand.campaign.bom.ModuleAccess;
import com.xtremand.campaign.exception.XamplifyDataAccessException;
import com.xtremand.common.bom.Pagination;
import com.xtremand.form.emailtemplate.dto.SendTestEmailDTO;
import com.xtremand.formbeans.XtremandResponse;
import com.xtremand.integration.service.IntegrationWrapperService;
import com.xtremand.lead.bom.OpportunityType;
import com.xtremand.lead.dto.LeadCountsResponseDTO;
import com.xtremand.lead.dto.LeadCustomFieldDto;
import com.xtremand.lead.dto.LeadDto;
import com.xtremand.lead.service.LeadService;
import com.xtremand.mail.service.AsyncComponent;
import com.xtremand.vanity.url.dto.VanityUrlDetailsDTO;

@RestController
@RequestMapping(value = "/lead")
public class LeadController {

	@Autowired
	LeadService leadService;
	
	@Autowired
	IntegrationWrapperService integrationWrapperService;

	@Autowired
	private AsyncComponent asyncComponent;

	@PostMapping
	public ResponseEntity<XtremandResponse> saveLead(@RequestBody LeadDto leadDto) {
		XtremandResponse response = new XtremandResponse();
		try {
			response = leadService.saveLead(leadDto);
			return ResponseEntity.ok(response);
		} catch (Exception e) {
			throw new XamplifyDataAccessException(e);
		} finally {
			if (response.getStatusCode() == 200) {
				leadDto.setId((Integer) response.getData());
				asyncComponent.saveAndPushLeadToxAmplify(leadDto);
				asyncComponent.sendLeadAddedOrUpdatedEmailToPartner(leadDto, false);
			}
		}
	}

	@PostMapping("/edit")
	public ResponseEntity<XtremandResponse> editLead(@RequestBody LeadDto leadDto) {
		XtremandResponse response = new XtremandResponse();
		try {
			response = leadService.updateLead(leadDto);
			return ResponseEntity.ok(response);
		} catch (Exception e) {
			throw new XamplifyDataAccessException(e);
		} finally {
			if (response.getStatusCode() == 200) {
				asyncComponent.updateAndPushLeadToxAmplify(leadDto);
				asyncComponent.sendLeadAddedOrUpdatedEmailToPartner(leadDto, true);
			}
		}
	}

	@PostMapping("/list/p")
	public ResponseEntity<Map<String, Object>> getLeadsForPartner(@RequestBody Pagination pagination) {
		return ResponseEntity.ok(leadService.getLeadsForPartner(pagination));
	}

	@PostMapping("/list/v")
	public ResponseEntity<Map<String, Object>> getLeadsForVendor(@RequestBody Pagination pagination) {
		return ResponseEntity.ok(leadService.getLeadsForVendor(pagination));
	}

	@GetMapping("/list/v/stages/{loggedInUserId}")
	public ResponseEntity<List<String>> getStageNamesForVendor(@PathVariable Integer loggedInUserId) {
		return ResponseEntity.ok(leadService.getStageNamesForVendor(loggedInUserId));
	}

	@GetMapping("list/p/stages/{loggedInUserId}")
	public ResponseEntity<List<String>> getStageNamesForPartner(@PathVariable Integer loggedInUserId) {
		return ResponseEntity.ok(leadService.getStageNamesForPartner(loggedInUserId));
	}

	@GetMapping("/{leadId}/{loggedInUserId}")
	public ResponseEntity<XtremandResponse> getLead(@PathVariable Integer loggedInUserId,
			@PathVariable Integer leadId) {
		return ResponseEntity.ok(leadService.getLead(loggedInUserId, leadId));
	}

	@PostMapping("/delete")
	public ResponseEntity<XtremandResponse> deleteLead(@RequestBody LeadDto leadDto) {
		return ResponseEntity.ok(leadService.deleteLead(leadDto));
	}

	@GetMapping("/{loggedInUserId}/vendors")
	public ResponseEntity<XtremandResponse> getVendorList(@PathVariable Integer loggedInUserId) {
		return ResponseEntity.ok(leadService.getVendorList(loggedInUserId));
	}

	@GetMapping("/{loggedInUserId}/vendors/loginAsUserId/{loginAsUserId}")
	public ResponseEntity<XtremandResponse> getVendorListForLoginAsUserId(@PathVariable Integer loggedInUserId,
			@PathVariable Integer loginAsUserId) {
		return ResponseEntity.ok(leadService.getVendorListForLoginAsUserId(loggedInUserId, loginAsUserId));
	}

	@PostMapping("counts")
	public ResponseEntity<XtremandResponse> getLeadCounts(@RequestBody VanityUrlDetailsDTO vanityUrlDetails) {
		return ResponseEntity.ok(leadService.getLeadCounts(vanityUrlDetails));
	}

	@GetMapping("vanity/{companyProfileName}/{loggedInUserId}")
	public ResponseEntity<XtremandResponse> getCompanyIdByCompanyProfileName(@PathVariable String companyProfileName,
			@PathVariable Integer loggedInUserId) {
		return ResponseEntity.ok(leadService.getCompanyIdByCompanyProfileName(companyProfileName, loggedInUserId));
	}

	@PostMapping("view/type")
	public ResponseEntity<XtremandResponse> getViewType(@RequestBody VanityUrlDetailsDTO vanityUrlDetails) {
		return ResponseEntity.ok(leadService.getViewType(vanityUrlDetails));
	}

	@GetMapping("getVendorLeadsCount/{userId}/{applyFilter}")
	public ResponseEntity<LeadCountsResponseDTO> vendorLeadsCount(@PathVariable Integer userId,
			@PathVariable boolean applyFilter) {
		return ResponseEntity.ok(leadService.vendorLeadsCount(userId, applyFilter));
	}

	@GetMapping(value = "/{leadId}/chat/{userId}")
	public ResponseEntity<XtremandResponse> getChat(@PathVariable Integer leadId, @PathVariable Integer userId) {
		return new ResponseEntity<>(leadService.getChat(leadId, userId), HttpStatus.OK);
	}

	@PostMapping(value = "download/{filename}.csv")
	@ResponseBody
	public void downloadLeads(HttpServletResponse httpServletResponse, @RequestParam String userType,
			@RequestParam String type, @RequestParam Integer userId, @RequestParam boolean vanityUrlFilter,
			@RequestParam String vendorCompanyProfileName, @RequestParam String searchKey,
			@RequestParam String fromDate, @RequestParam String toDate,
			@RequestParam boolean partnerTeamMemberGroupFilter, @RequestParam String timeZone,
			@PathVariable String filename, @RequestParam String stageName) {
		leadService.downloadLeads(httpServletResponse, userType, type, userId, filename, vanityUrlFilter,
				vendorCompanyProfileName, searchKey, fromDate, toDate, partnerTeamMemberGroupFilter, timeZone,
				stageName);
	}

	@PostMapping("list/p/stages")
	public ResponseEntity<List<String>> getStageNamesForPartner(@RequestBody VanityUrlDetailsDTO vanityUrlDetails) {
		return ResponseEntity.ok(leadService.getStageNamesForPartner(vanityUrlDetails));
	}

	/***** XNFR-456 *****/
	@PostMapping(value = "download/{userId}")
	public ResponseEntity<XtremandResponse> downloadLeads(@PathVariable Integer userId,
			@RequestBody Pagination pagination) {
		XtremandResponse response = new XtremandResponse();
		try {
			response = leadService.downloadLeads(userId);
			return ResponseEntity.ok(response);
		} catch (Exception e) {
			throw new XamplifyDataAccessException(e);
		} finally {
			if (response.getStatusCode() != 401) {
				Integer downloadDataInfoId = (Integer) response.getData();
				asyncComponent.uploadCsvToAws(userId, pagination, DownloadItem.LEADS_DATA, downloadDataInfoId);
			}
		}
	}

	/****** XNFR-426 ***/
	@PostMapping("/update/leadApprovalStatus")
	public ResponseEntity<XtremandResponse> updateLeadApprovalStatus(@RequestBody LeadDto leadDto) {
		return ResponseEntity.ok(leadService.updateLeadApprovalStatus(leadDto));

	}

	/**** XNFR-505 ****/
	@PostMapping("/getLeadsForLeadAttachment/p")
	public ResponseEntity<Map<String, Object>> getLeadsForLeadAttachment(@RequestBody Pagination pagination) {
		return ResponseEntity.ok(leadService.getLeadsForLeadAttachment(pagination));
	}

	@GetMapping("/findRegisteredByCompanies/{loggedInUserId}")
	public ResponseEntity<XtremandResponse> findRegisteredByCompanies(@PathVariable Integer loggedInUserId) {
		return ResponseEntity.ok(leadService.findRegisteredByCompanies(loggedInUserId));
	}

	@GetMapping("/findRegisteredByUsers/{loggedInUserId}")
	public ResponseEntity<XtremandResponse> findRegisteredByUsers(@PathVariable Integer loggedInUserId) {
		return ResponseEntity.ok(leadService.findRegisteredByUsers(loggedInUserId));
	}

	@GetMapping("/findRegisteredByUsersForPartnerView/{loggedInUserId}")
	public ResponseEntity<XtremandResponse> findRegisteredByUsersForPartnerView(@PathVariable Integer loggedInUserId) {
		return ResponseEntity.ok(leadService.findRegisteredByUsersForPartnerView(loggedInUserId));
	}

	@GetMapping("/findRegisteredByUsersByPartnerCompanyId/{partnerCompanyId}/{campaignId}")
	public ResponseEntity<XtremandResponse> findRegisteredByUsersByPartnerCompanyId(
			@PathVariable Integer partnerCompanyId, @PathVariable Integer campaignId) {
		return ResponseEntity.ok(leadService.findRegisteredByUsersByPartnerCompanyId(partnerCompanyId, campaignId));
	}

	@GetMapping("/custom/fields/{loggedInUserId}")
	public ResponseEntity<XtremandResponse> getCustomLeadFields(@PathVariable Integer loggedInUserId) {
		return ResponseEntity.ok(leadService.getCustomLeadFields(loggedInUserId));
	}

	@GetMapping("/vendor/custom/fields/{vendorCompanyId}")
	public ResponseEntity<XtremandResponse> getCustomLeadFieldsByVendorCompanyId(
			@PathVariable Integer vendorCompanyId) {
		return ResponseEntity.ok(leadService.getCustomLeadFieldsByVendorCompanyId(vendorCompanyId));
	}

	@PostMapping("save/custom/fields/{loggedInUserId}")
	public ResponseEntity<XtremandResponse> saveOrupdateCustomLeadFields(@PathVariable Integer loggedInUserId,
			@RequestBody List<LeadCustomFieldDto> leadFieldsDto) {
		return ResponseEntity.ok(leadService.saveOrupdateCustomLeadFields(loggedInUserId, leadFieldsDto));
	}

	@GetMapping("/findLeadAndLeadInfoAndComments/{leadId}")
	public ResponseEntity<XtremandResponse> findLeadAndLeadInfoAndComments(@PathVariable Integer leadId) {
		return ResponseEntity.ok(leadService.findLeadAndLeadInfoAndComments(leadId));
	}

	/** XNFR-553 **/
	@GetMapping("/fetchContactAssociatedLeadsAndCount")
	public ResponseEntity<XtremandResponse> findLeadsAndCountByContactId(
			@Valid ContactOpportunityRequestDTO contactOpportunityRequestDTO) {
		return ResponseEntity.ok(leadService.findLeadsAndCountByContactId(contactOpportunityRequestDTO));
	}

	@GetMapping("/checkIfHasAcessForAddLeadOrDeal")
	public ResponseEntity<XtremandResponse> checkIfHasAcessForAddLeadOrDeal(VanityUrlDetailsDTO vanityUrlDetailsDTO) {
		return ResponseEntity.ok(leadService.checkIfHasAcessForAddLeadOrDeal(vanityUrlDetailsDTO));
	}

	@GetMapping("/checkIfHasOpporunityAcess")
	public ResponseEntity<XtremandResponse> checkIfHasOpporunityAcess(VanityUrlDetailsDTO vanityUrlDetailsDTO) {
		return ResponseEntity.ok(leadService.checkIfHasOpporunityAcess(vanityUrlDetailsDTO));
	}

	@GetMapping("send-reminder/template")
	public ResponseEntity<XtremandResponse> findSendReminderTemplateForLead(
			@RequestParam("loggedInUserId") Integer loggedInUserId, @RequestParam("emailId") String emailId,
			@RequestParam("companyProfileName") String companyProfileName) {
		return new ResponseEntity<>(
				leadService.findSendReminderTemplateForLead(loggedInUserId, companyProfileName, emailId),
				HttpStatus.OK);
	}

	@PostMapping("send-reminder/notification")
	public ResponseEntity<XtremandResponse> sendReminderNotificationForLead(
			@RequestBody SendTestEmailDTO sendTestEmailDTO) {
		return new ResponseEntity<>(leadService.sendReminderNotificationForLead(sendTestEmailDTO), HttpStatus.OK);
	}
	
	@GetMapping("/access/{companyId}")
	public ModuleAccess findCompanyAccess(@PathVariable Integer companyId, @RequestParam String companyProfileName) {
		ModuleAccess moduleAccess = leadService.findCompanyAccess(companyId, companyProfileName);
		if (moduleAccess != null) {
			return moduleAccess;
		} else {
			return new ModuleAccess();
		}
	}
	
	@GetMapping(value = "crm/active/{createdForCompanyId}/{loggedInUserId}")
	public ResponseEntity getActiveCRM(@PathVariable Integer loggedInUserId, @PathVariable Integer createdForCompanyId) {
		ResponseEntity response = null;
		try {
			response = ResponseEntity.ok(integrationWrapperService.getActiveCRMDetails(loggedInUserId, createdForCompanyId));
		} catch (IOException | ParseException e) {
			response = ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Collections.singletonMap("message", e.getMessage()));
		}
		return response;
	}
	
	@GetMapping(value = "crm/active/{loggedInUserId}")
	public ResponseEntity getActiveCRM(@PathVariable Integer loggedInUserId) {
		ResponseEntity response = null;
		try {
			response = ResponseEntity.ok(integrationWrapperService.getActiveCRMDetails(loggedInUserId));
		} catch (IOException | ParseException e) {
			response = ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Collections.singletonMap("message", e.getMessage()));
		}
		return response;
	}
	
	@GetMapping(value = "crm/active/{opportunityType}/custom/form/{companyId}/{opportunityId}/{loggedInUserId}")
	public ResponseEntity getActiveCRMCustomForm(@PathVariable Integer companyId, @PathVariable Integer opportunityId,
			@PathVariable Integer loggedInUserId,
			@PathVariable OpportunityType opportunityType) {
		ResponseEntity response = null;
		response = ResponseEntity.ok(integrationWrapperService.getActiveCRMCustomForm(companyId, opportunityId,
				loggedInUserId, opportunityType));
		return response;
	}
	
	@GetMapping(value = "/sync/custom-form/{userId}")
	public ResponseEntity syncCustomForm(@PathVariable Integer userId) {
		return ResponseEntity.ok(leadService.saveLeadCustomFormFromMcp(userId));
	}
	
	@GetMapping(value = "/sync/pipeline/{userId}")
	public ResponseEntity saveLeadPipelinesFromMcp(@PathVariable Integer userId) {
		return ResponseEntity.ok(leadService.saveLeadPipelinesFromMcp(userId));
	}

}
