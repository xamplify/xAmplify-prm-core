package com.xtremand.controller;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.xtremand.dam.exception.DamDataAccessException;
import com.xtremand.dashboard.button.service.DashboardButtonService;
import com.xtremand.dashboard.buttons.dto.DashboardButtonsToPartnersDTO;
import com.xtremand.formbeans.XtremandResponse;
import com.xtremand.mail.service.AsyncComponent;
import com.xtremand.util.dto.Pageable;
import com.xtremand.util.service.UtilServiceWithOutTransactional;

@RestController
@RequestMapping(value = "/dashboardButtons/")
public class DashboardButtonController {

	@Autowired
	private DashboardButtonService dashboardButtonService;

	@Autowired
	private UtilServiceWithOutTransactional utilServiceWithOutTransactional;
	
	@Autowired
	private AsyncComponent asyncComponent;

	@GetMapping("findPublishedPartnerIds/{userListId}/{dashboardButtonId}")
	public XtremandResponse findPublishedPartnerGroupPartnerIdMappingIds(@PathVariable Integer userListId,
			@PathVariable Integer dashboardButtonId) {
		return dashboardButtonService.findPublishedPartnerGroupPartnerIdMappingIds(userListId, dashboardButtonId);
	}

	@PutMapping("publish/{userListId}/{dashboardButtonId}")
	public ResponseEntity<XtremandResponse> publish(@PathVariable Integer userListId,
			@PathVariable Integer dashboardButtonId) {
		return new ResponseEntity<>(utilServiceWithOutTransactional.publish(userListId, dashboardButtonId),
				HttpStatus.OK);
	}

	@GetMapping("isPublished/{dashboardButtonId}")
	public ResponseEntity<XtremandResponse> isPublished(@PathVariable Integer dashboardButtonId) {
		return new ResponseEntity<>(dashboardButtonService.isPublished(dashboardButtonId), HttpStatus.OK);
	}

	/**** XNFR-599 ****/
	@GetMapping("/published-and-unpublished/loggedInUserId/{loggedInUserId}/userListId/{userListId}/partnerUserId/{partnerUserId}")
	public ResponseEntity<XtremandResponse> findAllPublishedAndUnPublished(@PathVariable Integer loggedInUserId,
			@PathVariable Integer userListId, @PathVariable Integer partnerUserId, @Valid Pageable pageable,
			BindingResult result) {
		return new ResponseEntity<>(dashboardButtonService.findAllPublishedAndUnPublished(pageable, result,
				loggedInUserId, userListId, partnerUserId), HttpStatus.OK);
	}

	/**** XNFR-743 ****/
	@GetMapping("publish/userListId/{userListId}/partnerUserId/{partnerUserId}/id/{dashboardButtonId}/loggedInUserId/{loggedInUserId}")
	public ResponseEntity<XtremandResponse> publishDashboardButtonToPartnerCompany(@PathVariable Integer userListId,
			@PathVariable Integer partnerUserId, @PathVariable Integer dashboardButtonId,
			@PathVariable Integer loggedInUserId) {
		return new ResponseEntity<>(dashboardButtonService.publishDashboardButtonToPartnerCompany(userListId,
				partnerUserId, dashboardButtonId, loggedInUserId), HttpStatus.OK);
	}

	@GetMapping("bulkButtons/companyId/{companyId}/size/{size}")
	public ResponseEntity<XtremandResponse> uploadBulkDummyDashboardButtons(@PathVariable Integer companyId,
			@PathVariable Integer size) {
		return new ResponseEntity<>(utilServiceWithOutTransactional.uploadBulkDummyDashboardButtons(companyId, size),
				HttpStatus.OK);
	}

	@PutMapping("publishAll/companyId/{companyId}")
	public ResponseEntity<XtremandResponse> publishAllDashboardButtonsToDefaultPartnerGroup(
			@PathVariable Integer companyId) {
		return new ResponseEntity<>(
				utilServiceWithOutTransactional.publishAllDashboardButtonsToDefaultPartnerGroups(companyId),
				HttpStatus.OK);
	}
	
	/**** XNFR-599 ****/
	@PutMapping("sharedashboardbuttons")
	public ResponseEntity<XtremandResponse> shareSelectedDashboardButtons(@RequestBody DashboardButtonsToPartnersDTO dashboardButtonsToPartnersDTO) {
		boolean hasError = false;
		try {
			hasError = false;
			return ResponseEntity.ok(dashboardButtonService.updateDashboardButtonStatus(dashboardButtonsToPartnersDTO.getIds()));
			
		} catch (DamDataAccessException e) {
			hasError = true;
			throw new DamDataAccessException(e);
		} catch (Exception ex) {
			hasError = true;
			throw new DamDataAccessException(ex);
		} finally {
			if (!hasError) {
				asyncComponent.shareDashboardButtonsToPartners(dashboardButtonsToPartnersDTO);
			}
		}
		
	}
	
	@GetMapping("findAlternateUrls")
	public ResponseEntity<XtremandResponse> findAlternateUrls(@RequestParam("url") String referenceUrl) {
		return new ResponseEntity<>(dashboardButtonService.findAlternateUrls(referenceUrl), HttpStatus.OK);
	}

}
