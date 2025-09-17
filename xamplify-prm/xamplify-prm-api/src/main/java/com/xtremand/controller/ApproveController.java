package com.xtremand.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.xtremand.approve.dto.ApprovalPrivilegesEmailNotificationDTO;
import com.xtremand.approve.dto.MultiSelectApprovalDTO;
import com.xtremand.approve.dto.PendingApprovalDamAndLmsDTO;
import com.xtremand.approve.dto.TeamMemberApprovalPrivilegesDTO;
import com.xtremand.approve.service.ApproveService;
import com.xtremand.campaign.exception.XamplifyDataAccessException;
import com.xtremand.common.bom.Pagination;
import com.xtremand.formbeans.XtremandResponse;
import com.xtremand.mail.service.AsyncComponent;
import com.xtremand.util.BadRequestException;
import com.xtremand.util.XamplifyUtils;
import com.xtremand.util.dto.Pageable;

@RestController
@RequestMapping(value = "/approve/")
public class ApproveController {
	
	@Autowired
	ApproveService approveService;
	
	@Autowired
	private AsyncComponent asyncComponent;

	@PostMapping("getAllApprovalList/{loggedInUserId}")
	public ResponseEntity<XtremandResponse> getAllApprovalList(@PathVariable Integer loggedInUserId,
			@RequestBody Pagination pagination) {
		return new ResponseEntity<>(approveService.getAllApprovalList(pagination, loggedInUserId), HttpStatus.OK);
	}
	
	@GetMapping("getStatusTileCounts/{loggedInUserId}")
	public ResponseEntity<XtremandResponse> getStatusTileCounts(@PathVariable Integer loggedInUserId,@Valid Pageable pageable) {
		return new ResponseEntity<>(approveService.getStatusTileCounts(loggedInUserId,pageable), HttpStatus.OK);
	}
	
	@PostMapping("updateApprovalStatus/{loggedInUserId}")
	public ResponseEntity<XtremandResponse> updateApprovalStatusByType(@PathVariable Integer loggedInUserId,
			@RequestBody MultiSelectApprovalDTO multiSelectApprovalDto) {
		
		boolean constraint = false;
		XtremandResponse response = new XtremandResponse();
		try {
			response = approveService.updateApprovalStatusByTypeForMultiSelect(loggedInUserId, multiSelectApprovalDto, response);
			return new ResponseEntity<>(response, HttpStatus.OK);
		} catch (BadRequestException e) {
			constraint = true;
			throw new BadRequestException(e.getMessage());
		} catch (Exception e) {
			constraint = true;
			throw new XamplifyDataAccessException(e);
		} finally {
			if (!constraint && response.getStatusCode() == 200 && multiSelectApprovalDto != null
					&& XamplifyUtils.isNotEmptyList(multiSelectApprovalDto.getWhiteLabeledReApprovalDamIds())) {
				asyncComponent.handleWhiteLabeledAssetsAfterReApproval(multiSelectApprovalDto.getWhiteLabeledReApprovalDamIds(),
						multiSelectApprovalDto.getCompanyId(), multiSelectApprovalDto.getLoggedInUserId());
			}
		}
		
	}	
	
	/** XNFR-821 **/
	@GetMapping("listTeamMembersForApprovalControlManagement/{loggedInUserId}")
	public ResponseEntity<XtremandResponse> listTeamMembersForApprovalControlManagement(@PathVariable Integer loggedInUserId,
			@Valid Pageable pageable) {
		try {
			return new ResponseEntity<>(approveService.listTeamMembersForApprovalControlManagement(pageable, loggedInUserId), HttpStatus.OK);
		} catch (BadRequestException e) {
			throw new BadRequestException(e.getMessage());
		} catch (Exception e) {
			throw new XamplifyDataAccessException(e);
		}
	}
	
	/** XNFR-821 **/
	@PostMapping("saveOrUpdateApprovalControlSettings/{loggedInUserId}")
	public ResponseEntity<XtremandResponse> saveOrUpdateApprovalControlManagementSettings(@PathVariable Integer loggedInUserId,
			@RequestBody List<TeamMemberApprovalPrivilegesDTO> teamMemberApprovalPrivilegesDTOs) {	
		boolean constraint = false;
		List<ApprovalPrivilegesEmailNotificationDTO> approvalPrivilegesEmailNotificationDTOs = new ArrayList<>();
		MultiSelectApprovalDTO multiSelectApprovalDTO = new MultiSelectApprovalDTO();
		XtremandResponse response = new XtremandResponse();
		try {
			return new ResponseEntity<>(approveService.saveOrUpdateApprovalControlManagementSettings(loggedInUserId, teamMemberApprovalPrivilegesDTOs, approvalPrivilegesEmailNotificationDTOs, multiSelectApprovalDTO, response),
					HttpStatus.OK);
		} catch (BadRequestException e) {
			constraint = true;
			throw new BadRequestException(e.getMessage());
		} catch (Exception e) {
			constraint = true;
			throw new XamplifyDataAccessException(e);
		} finally {
			if (!constraint && XamplifyUtils.isNotEmptyList(approvalPrivilegesEmailNotificationDTOs)) {
				asyncComponent.sendTeamMemberPrivilegesUpdatedForApprovalProccessEmailNotification(approvalPrivilegesEmailNotificationDTOs, loggedInUserId);
			}
			
			if (!constraint && response.getStatusCode() == 200 && XamplifyUtils.isNotEmptyList(multiSelectApprovalDTO.getWhiteLabeledReApprovalDamIds())) {
				asyncComponent.handleWhiteLabeledAssetsAfterReApproval(multiSelectApprovalDTO.getWhiteLabeledReApprovalDamIds(),
						multiSelectApprovalDTO.getCompanyId(), multiSelectApprovalDTO.getLoggedInUserId());
			}
			
		}
	}
	
	@GetMapping("getApprovalPrivileges/{loggedInUserId}")
	public ResponseEntity<XtremandResponse> getApprovalPrivileges(@PathVariable Integer loggedInUserId) {
		return new ResponseEntity<>(approveService.getApprovalPrivileges(loggedInUserId), HttpStatus.OK);		
	}
	
	@GetMapping("checkApprovalPrivilegeManager/{loggedInUserId}")
	public ResponseEntity<XtremandResponse> checkIsApprovalPrivilegeManager(@PathVariable Integer loggedInUserId){
		return new ResponseEntity<>(approveService.isApprovalPrivilegeManager(loggedInUserId), HttpStatus.OK);		
	}
	
	@SuppressWarnings("unchecked")
	@GetMapping(value = "/sendReminderToApprovers/{loggedInUserId}/{entityId}/{moduleType}")
	public ResponseEntity<XtremandResponse> sendReminderToApprovers(@PathVariable Integer entityId, @PathVariable Integer loggedInUserId, @PathVariable String moduleType) {
		XtremandResponse response = new XtremandResponse();
		boolean constraint = false;
		try {
			response = approveService.sendReminderToApprovers(response, loggedInUserId, entityId, moduleType);
			return ResponseEntity.ok(response);
		} catch (BadRequestException e) {
			constraint = true;
			throw new BadRequestException(e.getMessage());
		} catch (Exception e) {
			constraint = true;
			throw new XamplifyDataAccessException(e);
		} finally {
			Map<String, Object> approversMap = (Map<String, Object>) response.getData();
			List<Integer> allApproversIds = (List<Integer>) approversMap.get("allApproversIds");
			PendingApprovalDamAndLmsDTO pendingApprovalDamAndLmsDTO = (PendingApprovalDamAndLmsDTO) approversMap.get("pendingApprovalDamAndLmsDTO");
			if (!constraint && response.getStatusCode() == 200 && XamplifyUtils.isNotEmptyList(allApproversIds) && pendingApprovalDamAndLmsDTO != null) {
				pendingApprovalDamAndLmsDTO.setApprovalReminder(true);
				asyncComponent.sendApprovalReminderNotificationForPendingContent(allApproversIds, pendingApprovalDamAndLmsDTO);
			}
		}
	}
	
	@GetMapping("checkApprovalPrivilegeForAssets/{loggedInUserId}")
	public ResponseEntity<XtremandResponse> checkIsAssetApprover(@PathVariable Integer loggedInUserId) {
		return new ResponseEntity<>(approveService.checkIsAssetApprover(loggedInUserId), HttpStatus.OK);		
	}
	
	/** XNFR-813 **/
	@GetMapping("getStatusTileCountsByModuleType/{loggedInUserId}/{moduleType}")
	public ResponseEntity<XtremandResponse> getStatusTileCountsByModuleType(@PathVariable Integer loggedInUserId,
			@PathVariable String moduleType) {
		XtremandResponse response = approveService.getStatusTileCountsByModuleType(loggedInUserId, moduleType);
		return ResponseEntity.ok(response);
	}
	
}
