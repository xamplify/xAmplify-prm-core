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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.xtremand.campaign.exception.XamplifyDataAccessException;
import com.xtremand.common.bom.Pagination;
import com.xtremand.formbeans.VendorInvitationDTO;
import com.xtremand.formbeans.XtremandResponse;
import com.xtremand.mail.service.AsyncComponent;
import com.xtremand.partnership.bom.PartnershipDTO;
import com.xtremand.partnership.service.PartnershipService;
import com.xtremand.util.dto.Pageable;
import com.xtremand.util.dto.UserListOperationsAsyncDTO;

@RestController
@RequestMapping("/partnership")
public class PartnershipController {

	@Autowired
	private PartnershipService partnershipService;
	
	@Autowired
	private AsyncComponent asyncComponent;

	@RequestMapping(value = "/vendor-invitation/{senderId}", method = RequestMethod.POST)
	public XtremandResponse sendVendorInvitation(@PathVariable Integer senderId,
			@RequestBody VendorInvitationDTO vendorInvitationDTO) {
		return partnershipService.sendVendorInvitation(senderId, vendorInvitationDTO);
	}

	@RequestMapping(value = "/approve-partners", method = RequestMethod.POST)
	public ResponseEntity<?> approvePartnersDetails(@RequestBody Pagination pagination) {
		Map<String, Object> resultMap = partnershipService.getApprovalSectionPartnersDetails(pagination);
		return ResponseEntity.status(HttpStatus.OK).body(resultMap);
	}

	@RequestMapping(value = "/approve-partner/{vendorId}/{partnerId}", method = RequestMethod.POST)
	public XtremandResponse approvePartner(@PathVariable Integer vendorId, @PathVariable Integer partnerId,
			@RequestBody VendorInvitationDTO vendorInvitationDTO) {
		UserListOperationsAsyncDTO userListOperationsAsyncDTO = new UserListOperationsAsyncDTO();
		boolean hasError = false;
		try {
			return partnershipService.approvePartner(vendorId, partnerId, vendorInvitationDTO, userListOperationsAsyncDTO);
		} catch (Exception e) {
			hasError = true;
			throw new XamplifyDataAccessException(e);
		} finally {
			if (!hasError && userListOperationsAsyncDTO.isPartnerList()
					&& userListOperationsAsyncDTO.getStatusCode() != null
					&& userListOperationsAsyncDTO.getStatusCode().equals(200)) {
				partnershipService.publishDAMAndLMSToNewlyAddedPartners(userListOperationsAsyncDTO.getPartnerListIds(), vendorId, userListOperationsAsyncDTO.getPartners());
				asyncComponent.publishDashboardButtonsToNewlyAddedPartners(userListOperationsAsyncDTO, vendorId);
			}
		}
	}

	@RequestMapping(value = "/decline-partner/{vendorId}/{partnerId}", method = RequestMethod.POST)
	public XtremandResponse declinePartner(@PathVariable Integer vendorId, @PathVariable Integer partnerId,
			@RequestBody VendorInvitationDTO vendorInvitationDTO) {
		return partnershipService.declinePartner(vendorId, partnerId, vendorInvitationDTO);
	}

	@RequestMapping(value = "/vendor-invitation/analytics", method = RequestMethod.POST)
	public ResponseEntity<?> referVendorAnalytics(@RequestBody Pagination pagination) {
		Map<String, Object> resultMap = partnershipService.referVendorAnalytics(pagination);
		return ResponseEntity.status(HttpStatus.OK).body(resultMap);
	}

    //   XNFR-1108
	@GetMapping("/vendor-invitation/download/Csv")
	public void referVendorAnalyticsDownloadCsv(@Valid Pagination pagination, @Valid Pageable pageable ,HttpServletResponse response)
	{
		pagination.setUserId(pageable.getLoginAsUserId());
		pagination.setFilterBy(pageable.getFilterBy());
		pagination.setSearchKey(pageable.getSearch());
		pagination.setSortcolumn(pageable.getSortcolumn());
		pagination.setSortingOrder(pageable.getSort());
		pagination.setMaxResults(Integer.parseInt(pageable.getSize()));
		pagination.setPageIndex(Integer.parseInt(pageable.getPage()));
		partnershipService.referVendorAnalyticsDownloadCsv(pagination, response);
	}
	
	@GetMapping("/approve-partner/downloadCsv")
	public void approvePartnersDownloadCsv(@Valid Pagination pagination, @Valid Pageable pageable ,HttpServletResponse response)
	{
		pagination.setUserId(pageable.getLoginAsUserId());
		pagination.setFilterBy(pageable.getFilterBy());
		pagination.setSearchKey(pageable.getSearch());
		pagination.setSortcolumn(pageable.getSortcolumn());
		pagination.setSortingOrder(pageable.getSort());
		pagination.setMaxResults(Integer.parseInt(pageable.getSize()));
		pagination.setPageIndex(Integer.parseInt(pageable.getPage()));
		partnershipService.approvePartnersDownloadCsv(pagination, response);
	}
	
	@RequestMapping(value = "/vendor-invitations/count/{partnerId}", method = RequestMethod.GET)
	public ResponseEntity<?> vendorInvitationsCount(@PathVariable Integer partnerId) {
		Map<String, Object> resultMap = partnershipService.vendorInvitationsCount(partnerId);
		return ResponseEntity.status(HttpStatus.OK).body(resultMap);
	}



	@PostMapping(value = "findPartnerCompanies")
	public ResponseEntity<XtremandResponse> findPartnerCompanies(@RequestBody Pagination pagination) {
		return ResponseEntity.ok(partnershipService.findPartnerCompanies(pagination));
	}

	@PostMapping(value = "findPartnerGroups")
	public ResponseEntity<XtremandResponse> findPartnerGroups(@RequestBody Pagination pagination) {
		return ResponseEntity.ok(partnershipService.findPartnerGroups(pagination));
	}

	@PostMapping(value = "loadPartnerCompanies/{userId}")
	public ResponseEntity<XtremandResponse> findPartnerCompanies(@RequestBody Pagination pagination,
			@PathVariable Integer userId) {
		return ResponseEntity.ok(partnershipService.findPartnerCompanies(pagination, userId));
	}
	
	/********XNFR-255****/
	@PostMapping(value = "findPartnerCompaniesForSharingWhiteLabeledContent")
	public ResponseEntity<XtremandResponse> findPartnerCompaniesForSharingWhiteLabeledContent(@RequestBody Pagination pagination) {
		return ResponseEntity.ok(partnershipService.findPartnerCompaniesForSharingWhiteLabeledContent(pagination));
	}
	
	@PostMapping(value = "findVendorCompanies")
	public ResponseEntity<XtremandResponse> findVendorCompanies(@RequestBody Pagination pagination) {
		return ResponseEntity.ok(partnershipService.findVendorCompanies(pagination));
	}

	@PostMapping(value = "/findPartnerCompaniesByDomain/{loggedInUserId}")
	public XtremandResponse findPartnerCompaniesByDomain(@RequestBody PartnershipDTO partnershipDTO,
			@PathVariable Integer loggedInUserId) {
		return partnershipService.findPartnerCompaniesByDomain(partnershipDTO, loggedInUserId);
	}

	@PostMapping(value = "/updatePartnerCompaniesByDomain/{loggedInUserId}")
	public XtremandResponse updatePartnerCompaniesByDomain(@RequestBody PartnershipDTO partnershipDTO,
			@PathVariable Integer loggedInUserId) {
		return partnershipService.updatePartnerCompaniesByDomain(partnershipDTO, loggedInUserId);
	}

	@PostMapping(value = "/deactivatePartnerCompanies/{loggedInUserId}")
	public XtremandResponse deactivatePartnerCompanies(@RequestBody List<Integer> deactivateUserIds,
			@PathVariable Integer loggedInUserId) {
		return partnershipService.deactivatePartnerCompanies(deactivateUserIds, loggedInUserId);
	}

	@PutMapping(value = "updatePartnerShipStatusForPartner")
	public ResponseEntity<XtremandResponse> updatePartnerShipStatusForPartner(
			@RequestParam("partnerStatus") String partnerStatus, @RequestBody List<Integer> partnershipIds) {
		return ResponseEntity.ok(partnershipService.updatePartnerShipStatusForPartner(partnerStatus, partnershipIds));
	}

	@PostMapping(value = "findTeamMemberPartnerCompany/{teamMemberGroupId}")
	public ResponseEntity<XtremandResponse> findTeamMemberPartnerCompany(@RequestBody Pagination pagination,
			@PathVariable Integer teamMemberGroupId) {
		return ResponseEntity.ok(partnershipService.findTeamMemberPartnerCompany(pagination, teamMemberGroupId));
	}

	@GetMapping(value = "findTeamMemberPartnerCompanyByTeamMemberGroupIdAndTeamMemberId/{teamMemberId}/{teamMemberGroupId}")
	public ResponseEntity<XtremandResponse> findTeamMemberPartnerCompany(@PathVariable Integer teamMemberId,
			@PathVariable Integer teamMemberGroupId) {
		return ResponseEntity.ok(partnershipService.findTeamMemberPartnerCompanyByTeamMemberGroupIdAndTeamMemberId(teamMemberId, teamMemberGroupId));
	}

}
