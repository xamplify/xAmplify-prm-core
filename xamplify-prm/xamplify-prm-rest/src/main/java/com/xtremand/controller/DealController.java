package com.xtremand.controller;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

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
import com.xtremand.campaign.exception.XamplifyDataAccessException;
import com.xtremand.common.bom.Pagination;
import com.xtremand.deal.dto.DealCountsResponseDTO;
import com.xtremand.deal.dto.DealDto;
import com.xtremand.deal.dto.VendorSelfDealRequestDTO;
import com.xtremand.deal.service.DealService;
import com.xtremand.formbeans.XtremandResponse;
import com.xtremand.mail.service.AsyncComponent;
import com.xtremand.util.dto.Pageable;
import com.xtremand.vanity.url.dto.VanityUrlDetailsDTO;

@RestController
@RequestMapping(value = "/deal")
public class DealController {

	@Autowired
	private DealService dealService;

	@Autowired
	private AsyncComponent asyncComponent;

	@PostMapping
	public ResponseEntity<XtremandResponse> saveLead(@RequestBody DealDto dealDto) {
		XtremandResponse response = new XtremandResponse();
		try {
			response = dealService.saveDeal(dealDto);
			return ResponseEntity.ok(response);
		} catch (Exception e) {
			throw new XamplifyDataAccessException(e);
		} finally {
			if (response.getStatusCode() == 200) {
				dealDto.setId((Integer) response.getData());
				asyncComponent.sendDealAddedOrUpdatedEmailToPartner(dealDto, false);
			}
		}
	}

	@PostMapping("/edit")
	public ResponseEntity<XtremandResponse> updateDeal(@RequestBody DealDto dealDto) {
		XtremandResponse response = new XtremandResponse();
		try {
			response = dealService.updateDeal(dealDto);
			return ResponseEntity.ok(response);
		} catch (Exception e) {
			throw new XamplifyDataAccessException(e);
		} finally {
			if (response.getStatusCode() == 200) {
				asyncComponent.sendDealAddedOrUpdatedEmailToPartner(dealDto, true);
			}
		}
	}

	@PostMapping("/list/p")
	public ResponseEntity<Map<String, Object>> getDealsForPartner(@RequestBody Pagination pagination) {
		return ResponseEntity.ok(dealService.getDealsForPartner(pagination));
	}

	@PostMapping("/list/v")
	public ResponseEntity<Map<String, Object>> getDealsForVendor(@RequestBody Pagination pagination) {
		return ResponseEntity.ok(dealService.getDealsForVendor(pagination));
	}

	@GetMapping("/{leadId}/{loggedInUserId}")
	public ResponseEntity<XtremandResponse> getDeal(@PathVariable Integer loggedInUserId,
			@PathVariable Integer leadId) {
		return ResponseEntity.ok(dealService.getDeal(loggedInUserId, leadId));
	}

	@PostMapping("/delete")
	public ResponseEntity<XtremandResponse> deleteDeal(@RequestBody DealDto dealDto) {
		return ResponseEntity.ok(dealService.deleteDeal(dealDto));
	}

	@PostMapping("/status/change")
	public ResponseEntity<XtremandResponse> changeDealStatus(@RequestBody DealDto dealDto) {
		XtremandResponse response = new XtremandResponse();
		try {
			response = dealService.changeDealStatus(dealDto);
			return ResponseEntity.ok(response);
		} catch (Exception e) {
			throw new XamplifyDataAccessException(e);
		} finally {
			if (response.getStatusCode() == 200) {
				asyncComponent.sendDealAddedOrUpdatedEmailToPartner(dealDto, true);
			}
		}
	}

	@PostMapping("counts")
	public ResponseEntity<XtremandResponse> getDealCounts(@RequestBody VanityUrlDetailsDTO vanityUrlDetails) {
		return ResponseEntity.ok(dealService.getDealCounts(vanityUrlDetails));
	}

	@GetMapping("getVendorDealsCount/{userId}/{applyFilter}")
	public ResponseEntity<DealCountsResponseDTO> getVendorDealsCount(@PathVariable Integer userId,
			@PathVariable boolean applyFilter) {
		return ResponseEntity.ok(dealService.getVendorDealsCount(userId, applyFilter));
	}

	@GetMapping(value = "/property/{propertyId}/chat/{userId}")
	public ResponseEntity<XtremandResponse> getChatByProperty(@PathVariable Integer propertyId,
			@PathVariable Integer userId) {
		return new ResponseEntity<>(dealService.getChatByProperty(propertyId, userId), HttpStatus.OK);
	}

	@GetMapping(value = "/{dealId}/chat/{userId}")
	public ResponseEntity<XtremandResponse> getChat(@PathVariable Integer dealId, @PathVariable Integer userId) {
		return new ResponseEntity<>(dealService.getChat(dealId, userId), HttpStatus.OK);
	}

	@PostMapping(value = "download/{filename}.csv")
	@ResponseBody
	public void downloadDeals(HttpServletResponse httpServletResponse, @RequestParam String userType,
			@RequestParam String type, @RequestParam Integer userId, @RequestParam boolean vanityUrlFilter,
			@RequestParam String vendorCompanyProfileName, @RequestParam String searchKey,
			@RequestParam String fromDate, @RequestParam String toDate, boolean partnerTeamMemberGroupFilter,
			@RequestParam String timeZone, @PathVariable String filename, @RequestParam String stageName,
			@RequestParam Integer createdForCompanyId) {
		dealService.downloadDeals(httpServletResponse, userType, type, userId, filename, vanityUrlFilter,
				vendorCompanyProfileName, searchKey, fromDate, toDate, partnerTeamMemberGroupFilter, timeZone,
				stageName, createdForCompanyId);
	}

	@GetMapping(value = "/list/v/stages/{loggedInUserId}")
	public ResponseEntity<List<String>> getStageNamesForVendor(@PathVariable Integer loggedInUserId) {
		return ResponseEntity.ok(dealService.getStageNamesForVendor(loggedInUserId));
	}

	@GetMapping(value = "/list/p/stages/{loggedInUserId}/{vendorCompanyId}")
	public ResponseEntity<List<String>> getStageNamesForPartner(@PathVariable Integer loggedInUserId,
			@PathVariable Integer vendorCompanyId) {
		return ResponseEntity.ok(dealService.getStageNamesForPartner(loggedInUserId, vendorCompanyId));
	}


	@GetMapping(value = "/list/partner/stages/{loggedInUserId}")
	public ResponseEntity<List<String>> getStageNamesOfPartner(@PathVariable Integer loggedInUserId) {
		return ResponseEntity.ok(dealService.getStageNamesForPartner(loggedInUserId));
	}

	@GetMapping(value = "/list/vendor/stages/{loggedInUserId}")
	public ResponseEntity<List<String>> getStageNamesOfVendor(@PathVariable Integer loggedInUserId) {
		return ResponseEntity.ok(dealService.getStageNamesForVendor(loggedInUserId));
	}

	@GetMapping(value = "campaign/deal/stages/{loggedInUserId}")
	public ResponseEntity<List<String>> getStageNamesForVendorInCampaign(@PathVariable Integer loggedInUserId) {
		return ResponseEntity.ok(dealService.getStageNamesForPartner(loggedInUserId));
	}

	@GetMapping(value = "campaign/deal/list/stages/{loggedInUserId}")
	public ResponseEntity<List<String>> getStageNamesForPartnerInCampaign(@PathVariable Integer loggedInUserId) {
		return ResponseEntity.ok(dealService.getStageNamesForVendorInCampaign(loggedInUserId));
	}

	@GetMapping(value = "/partner/company/stages/{companyId}")
	public ResponseEntity<List<String>> getStageNamesForPartnerCompanyId(@PathVariable Integer companyId) {
		return ResponseEntity.ok(dealService.getStageNamesForPartnerCompanyId(companyId));
	}

	/***** XNFR-470 *****/
	@PostMapping(value = "download/{userId}")
	public ResponseEntity<XtremandResponse> downloadLeads(@PathVariable Integer userId,
			@RequestBody Pagination pagination) {
		XtremandResponse response = new XtremandResponse();
		try {
			response = dealService.downloadDeals(userId);
			return ResponseEntity.ok(response);
		} catch (Exception e) {
			throw new XamplifyDataAccessException(e);
		} finally {
			if (response.getStatusCode() != 401) {
				Integer downloadDataInfoId = (Integer) response.getData();
				asyncComponent.uploadCsvToAws(userId, pagination, DownloadItem.DEALS_DATA, downloadDataInfoId);
			}
		}
	}

	@GetMapping("/findRegisteredByCompanies/{loggedInUserId}")
	public ResponseEntity<XtremandResponse> findRegisteredByCompanies(@PathVariable Integer loggedInUserId) {
		return ResponseEntity.ok(dealService.findRegisteredByCompanies(loggedInUserId));
	}

	@GetMapping("/findRegisteredByUsers/{loggedInUserId}")
	public ResponseEntity<XtremandResponse> findRegisteredByUsers(@PathVariable Integer loggedInUserId) {
		return ResponseEntity.ok(dealService.findRegisteredByUsers(loggedInUserId));
	}

	@GetMapping("/findRegisteredByUsersForPartnerView/{loggedInUserId}")
	public ResponseEntity<XtremandResponse> findRegisteredByUsersForPartnerView(@PathVariable Integer loggedInUserId) {
		return ResponseEntity.ok(dealService.findRegisteredByUsersForPartnerView(loggedInUserId));
	}


	@GetMapping("/findRegisteredByUsersByPartnerCompanyId/{partnerCompanyId}/{campaignId}")
	public ResponseEntity<XtremandResponse> findRegisteredByUsersByPartnerCompanyId(
			@PathVariable Integer partnerCompanyId, @PathVariable Integer campaignId) {
		return ResponseEntity.ok(dealService.findRegisteredByUsersByPartnerCompanyId(partnerCompanyId, campaignId));
	}

	@GetMapping("/findDealAndLeadInfoAndComments/{dealId}")
	public ResponseEntity<XtremandResponse> findDealAndLeadInfoAndComments(@PathVariable Integer dealId) {
		return ResponseEntity.ok(dealService.findDealAndLeadInfoAndComments(dealId));
	}

	@GetMapping("/findVendorDetailsWithSelfDealsCount")
	public ResponseEntity<XtremandResponse> findVendorDetailsWithSelfDealsCount(
			VendorSelfDealRequestDTO vendorSelfDealRequestDTO) {
		return ResponseEntity.ok(dealService.findVendorDetailsWithSelfDealsCount(vendorSelfDealRequestDTO));
	}

	/** XNFR-553 **/
	@GetMapping("/fetchContactAssociatedDealsAndCount")
	public ResponseEntity<XtremandResponse> findDealsAndCountByContactId(
			@Valid ContactOpportunityRequestDTO contactOpportunityRequestDTO) {
		return ResponseEntity.ok(dealService.findDealsAndCountByContactId(contactOpportunityRequestDTO));
	}

	@GetMapping(value = "/fetchContactsForDealAttachment/{loggedInUserId}")
	public ResponseEntity<XtremandResponse> fetchContactsForDealAttachment(@PathVariable Integer loggedInUserId,
			@Valid Pageable pageable) {
		return ResponseEntity.ok(dealService.fetchContactsForDealAttachment(loggedInUserId, pageable));
	}

	@GetMapping(value = "/fetchTotalDealAmount")
	public ResponseEntity<XtremandResponse> fetchTotalDealAmount(
			@Valid ContactOpportunityRequestDTO contactOpportunityRequestDTO) {
		return ResponseEntity.ok(dealService.fetchTotalDealAmountForCompanyJourney(contactOpportunityRequestDTO));
	}

}
