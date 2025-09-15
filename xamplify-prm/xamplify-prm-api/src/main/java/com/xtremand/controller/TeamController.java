package com.xtremand.controller;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.xtremand.campaign.exception.XamplifyDataAccessException;
import com.xtremand.common.bom.Pagination;
import com.xtremand.common.bom.StatusCode;
import com.xtremand.exception.DuplicateEntryException;
import com.xtremand.formbeans.UserDTO;
import com.xtremand.formbeans.VendorInvitationDTO;
import com.xtremand.formbeans.XtremandResponse;
import com.xtremand.mail.service.AsyncComponent;
import com.xtremand.mail.service.StatusCodeConstants;
import com.xtremand.team.member.dto.DeleteTeamMemberPartnersRequestDTO;
import com.xtremand.team.member.dto.PartnerPrimaryAdminUpdateDto;
import com.xtremand.team.member.dto.TeamMemberDTO;
import com.xtremand.team.service.TeamService;
import com.xtremand.user.exception.TeamMemberDataAccessException;
import com.xtremand.util.BadRequestException;
import com.xtremand.util.HttpHeaderUtil;
import com.xtremand.util.XamplifyUtils;
import com.xtremand.util.dto.Pageable;
import com.xtremand.util.service.CsvUtilService;

@RestController
@RequestMapping(value = "/teamMember")
public class TeamController {

	private static final String TEAM_MEMBER_UNIQUE_INDEX = "xt_team_member_group_user_mapping_unique_key";

	private static final String TEAM_MEMBER_SECOND_ADMIN_UNIQUE_INDEX = "xt_team_member_second_admin_unique_key";

	private static final String DUPLICATE_TEAM_MEMBER = "Team Member Already Exists";

	@Autowired
	private CsvUtilService csvUtilService;

	@Autowired
	private TeamService teamService;

	@Autowired
	private AsyncComponent asyncComponent;
	
	private static final Logger logger = LoggerFactory.getLogger(TeamController.class);

	@PostMapping(value = "/save")
	public ResponseEntity<XtremandResponse> save(@RequestBody TeamMemberDTO teamMemberDTO) {
		boolean hasError = false;
		XtremandResponse response = new XtremandResponse();
		try {
			response = teamService.saveAll(teamMemberDTO);
			return ResponseEntity.ok(response);
		} catch (DataIntegrityViolationException e) {
			hasError = true;
			if (e.getMessage().indexOf(TEAM_MEMBER_UNIQUE_INDEX) > -1) {
				throw new DuplicateEntryException(DUPLICATE_TEAM_MEMBER);
			} else if (e.getMessage().indexOf(TEAM_MEMBER_SECOND_ADMIN_UNIQUE_INDEX) > -1) {
				throw new DuplicateEntryException(
						"Enable As Admin cannot be enabled as there are at most two admins for this company");
			} else {
				throw new TeamMemberDataAccessException(e);
			}
		} catch (DuplicateEntryException e) {
			hasError = true;
			throw new DuplicateEntryException(e.getMessage());
		} catch (TeamMemberDataAccessException ex) {
			hasError = true;
			throw new TeamMemberDataAccessException(ex);
		} catch (Exception e) {
			hasError = true;
			throw new TeamMemberDataAccessException(e);
		} finally {
			if (!hasError && response.getStatusCode() == 200) {
				teamService.sendEmailsToTeamMembers(response.getMap());
			}

		}
	}

	@PostMapping(value = "/update")
	public ResponseEntity<XtremandResponse> update(@RequestBody TeamMemberDTO teamMemberDTO) {
		XtremandResponse response = new XtremandResponse();
		boolean constraint = false;
		try {
			return ResponseEntity.ok(teamService.update(teamMemberDTO, response));
		} catch (DataIntegrityViolationException e) {
			if (e.getMessage().indexOf(TEAM_MEMBER_UNIQUE_INDEX) > -1) {
				throw new DuplicateEntryException(DUPLICATE_TEAM_MEMBER);
			} else if (e.getMessage().indexOf(TEAM_MEMBER_SECOND_ADMIN_UNIQUE_INDEX) > -1) {
				throw new DuplicateEntryException(
						"Enable As Admin cannot be enabled as there are at most two admins for this company");
			} else {
				throw new TeamMemberDataAccessException(e);
			}
		} catch (DuplicateEntryException e) {
			constraint = true;
			throw new DuplicateEntryException(e.getMessage());
		} catch (BadRequestException d) {
			constraint = true;
			throw new BadRequestException(d.getMessage());
		} catch (TeamMemberDataAccessException ex) {
			constraint = true;
			throw new TeamMemberDataAccessException(ex);
		} catch (Exception e) {
			constraint = true;
			throw new TeamMemberDataAccessException(e);
		} finally {
			if (!constraint && response.getStatusCode() == 200 
					&& XamplifyUtils.isNotEmptyList(teamMemberDTO.getWhiteLabeledReApprovalDamIds())
						&& XamplifyUtils.isValidInteger(teamMemberDTO.getUserId())
						&& XamplifyUtils.isValidInteger(teamMemberDTO.getCompanyId())) {
					asyncComponent.handleWhiteLabeledAssetsAfterReApproval(
							teamMemberDTO.getWhiteLabeledReApprovalDamIds(), teamMemberDTO.getCompanyId(),
							teamMemberDTO.getUserId());
			}
		}

	}

	@GetMapping(value = "/findUsersToTransferData/{userId}")
	public ResponseEntity<List<UserDTO>> findUsersToTransferData(@PathVariable Integer userId) {
		return ResponseEntity.ok(teamService.findUsersToTransferData(userId));
	}

	@PostMapping("/findAll")
	public ResponseEntity<XtremandResponse> findAll(@RequestBody Pagination pagination) {
		return ResponseEntity.ok(teamService.findAll(pagination));
	}

	@GetMapping("/findPrimaryAdmin/{loggedInUserId}")
	public ResponseEntity<XtremandResponse> findPrimaryAdmin(@PathVariable Integer loggedInUserId) {
		return ResponseEntity.ok(teamService.findPrimaryAdmin(loggedInUserId));
	}

	@GetMapping("/downloadDefaultCsv/Add-Team-Members.csv")
	public void downloadDefaultCsv(HttpServletResponse response) {
		csvUtilService.downloadDefaultCsv(response, "Email Id,First Name,Last Name");
	}

	@PostMapping(value = "/delete")
	public ResponseEntity<XtremandResponse> delete(@RequestBody TeamMemberDTO teamMemberDTO) {
		boolean constraint = false;
		try {	
			return ResponseEntity.ok(teamService.delete(teamMemberDTO));
		} catch (BadRequestException d) {
			constraint = true;
			throw new BadRequestException(d.getMessage());
		} catch (TeamMemberDataAccessException ex) {
			constraint = true;
			throw new TeamMemberDataAccessException(ex);
		} catch (Exception e) {
			constraint = true;
			throw new TeamMemberDataAccessException(e);
		}	
		finally {
			if (!constraint && XamplifyUtils.isNotEmptyList(teamMemberDTO.getWhiteLabeledReApprovalDamIds())
					&& XamplifyUtils.isValidInteger(teamMemberDTO.getUserId()) && XamplifyUtils.isValidInteger(teamMemberDTO.getCompanyId())) {
				asyncComponent.handleWhiteLabeledAssetsAfterReApproval(teamMemberDTO.getWhiteLabeledReApprovalDamIds(),
						teamMemberDTO.getCompanyId(), teamMemberDTO.getOrgAdminId());
			}
		}
	}

	@PostMapping(value = "/getVanityUrlRoles")
	public ResponseEntity<XtremandResponse> getVanityUrlRoles(@RequestBody TeamMemberDTO teamMemberInputDTO) {
		return ResponseEntity.ok(teamService.getVanityUrlRoles(teamMemberInputDTO));
	}

	@PostMapping(value = "/resendTeamMemberInvitation")
	public ResponseEntity<XtremandResponse> resendTeamMemberInvitation(@RequestBody TeamMemberDTO teamMemberDTO) {
		return ResponseEntity.ok(teamService.resendTeamMemberInvitation(teamMemberDTO));
	}

	@GetMapping("/findById/{id}")
	public ResponseEntity<XtremandResponse> findById(@PathVariable Integer id) {
		return ResponseEntity.ok(teamService.findById(id));
	}

	@PostMapping("/findAllTeamMembersByGroupId")
	public ResponseEntity<XtremandResponse> findAllTeamMembersByGroupId(@RequestBody Pagination pagination) {
		return ResponseEntity.ok(teamService.findTeamMemberDetailsByTeamMemberGroupId(pagination));
	}

	/******* XNFR-97 ************/
	@PostMapping("/findPartners")
	public ResponseEntity<XtremandResponse> findPartners(@RequestBody Pagination pagination) {
		return ResponseEntity.ok(teamService.findPartners(pagination));
	}

	/******* XNFR-97 ************/
	@PostMapping("/deletePartners")
	public ResponseEntity<XtremandResponse> deletePartners(
			@RequestBody DeleteTeamMemberPartnersRequestDTO deleteTeamMemberPartnersRequestDTO) {
		return ResponseEntity.ok(teamService.deleteTeamMemberPartners(deleteTeamMemberPartnersRequestDTO));
	}

	/******* XNFR-139 ************/
	@GetMapping("/findMaximumAdminsLimitDetails/{loggedInUserId}")
	public ResponseEntity<XtremandResponse> findMaximumAdminsLimitDetails(@PathVariable Integer loggedInUserId) {
		return ResponseEntity.ok(teamService.findMaximumAdminsLimitDetails(loggedInUserId));
	}

	/******* XNFR-139 ************/
	@GetMapping("/updatePrimaryAdmin/{loggedInUserId}/{teamMemberUserId}")
	public ResponseEntity<XtremandResponse> updatePrimaryAdmin(@PathVariable Integer loggedInUserId,
			@PathVariable Integer teamMemberUserId) {
		XtremandResponse response = new XtremandResponse();
		TeamMemberDTO teamMemberDTO = new TeamMemberDTO();
		boolean constraint = false;
		try {	
			return ResponseEntity.ok(teamService.updatePrimaryAdmin(loggedInUserId, teamMemberUserId, response, teamMemberDTO));
		} catch (BadRequestException d) {
			constraint = true;
			throw new BadRequestException(d.getMessage());
		} catch (TeamMemberDataAccessException ex) {
			constraint = true;
			throw new TeamMemberDataAccessException(ex);
		} catch (Exception e) {
			constraint = true;
			throw new TeamMemberDataAccessException(e);
		}	
		finally {
			if (!constraint && XamplifyUtils.isNotEmptyList(teamMemberDTO.getWhiteLabeledReApprovalDamIds())
					&& XamplifyUtils.isValidInteger(loggedInUserId) && XamplifyUtils.isValidInteger(teamMemberDTO.getCompanyId())) {
				asyncComponent.handleWhiteLabeledAssetsAfterReApproval(teamMemberDTO.getWhiteLabeledReApprovalDamIds(),
						teamMemberDTO.getCompanyId(), loggedInUserId);
			}
		}
	}

	/******* XNFR-139 ************/
	@GetMapping("/findMaximumAdminsLimitDetailsByCompanyId/{companyId}")
	public ResponseEntity<XtremandResponse> findMaximumAdminsLimitDetailsByCompanyId(@PathVariable Integer companyId) {
		return ResponseEntity.ok(teamService.findMaximumAdminsLimitDetailsByCompanyId(companyId));
	}

	/******* XNFR-139 ************/
	@GetMapping("/findPrimaryAdminAndExtraAdmins/{loggedInUserId}")
	public ResponseEntity<XtremandResponse> findPrimaryAdminAndExtraAdmins(@PathVariable Integer loggedInUserId) {
		return ResponseEntity.ok(teamService.findPrimaryAdminAndExtraAdmins(loggedInUserId));
	}

	@GetMapping("/getHtmlBody/{userId}")
	public XtremandResponse getInviteTeamMemberTemplate(@PathVariable("userId") Integer userId,
			@RequestParam("companyProfileName") String companyProfileName,
			@RequestParam("templateId") Integer templateId) {
		return teamService.getInviteTeamMemberTemplate(userId, companyProfileName, templateId);
	}

	@PostMapping(value = "/invite-team-member/userId/{userId}")
	public ResponseEntity<XtremandResponse> saveInviteTeamMembersData(@PathVariable Integer userId,
			@RequestBody VendorInvitationDTO vendorInvitationDTO) {
		boolean hasError = false;
		XtremandResponse response = new XtremandResponse();
		try {
			response = teamService.saveInviteTeamMembersData(userId, vendorInvitationDTO);
			return ResponseEntity.ok(response);
		} catch (BadRequestException e) {
			hasError = true;
			throw new BadRequestException(e.getMessage());
		} catch (Exception e) {
			hasError = true;
			throw new TeamMemberDataAccessException(e);
		} finally {
			if (!hasError && response.getStatusCode() == 200) {
				teamService.sendEmailsToTeamMembers(response.getMap());
			}
		}
	}

	@GetMapping(value = "/invite-team-member/count")
	public ResponseEntity<Map<String, Object>> inviteTeamMembersCount(@RequestParam("userId") Integer userId) {
		return ResponseEntity.status(HttpStatus.OK).body(teamService.inviteTeamMembersCount(userId));
	}

	@GetMapping(value = "/invite-team-member/analytics/type/{type}")
	public ResponseEntity<Map<String, Object>> inviteTeamMemberAnalytics(@PathVariable String type,
			@RequestParam("userId") Integer userId, @Valid Pageable pageable) {
		return ResponseEntity.status(HttpStatus.OK).body(teamService.inviteTeamMemberAnalytics(userId, type, pageable));
	}

	@GetMapping(value = "/invite-team-member/downloadCsv/type/{type}")
	public ResponseEntity<Void> inviteTeamMemberDownloadCsv(@PathVariable String type,
			@RequestParam("userId") Integer userId, @Valid Pageable pageable, HttpServletResponse response) {
		teamService.inviteTeamMemberDownloadCsv(userId, type, pageable, response);
		StatusCode statusCode = StatusCode.getstatuscode(StatusCodeConstants.DOWNLOAD_USERLIST_SUCCESS);
		return ResponseEntity.ok().headers(HttpHeaderUtil.getHeader(statusCode)).body(null);
	}

	@PostMapping(value = "/send-team-Member-Recent-Login-email/{loggedInUserId}")
	public ResponseEntity<XtremandResponse> sendTeamMembersSendingMailRecentLoginPortal(
			@RequestBody Pagination pagination, @PathVariable Integer loggedInUserId) {
		return ResponseEntity.status(HttpStatus.OK)
				.body(teamService.sendTeamMemberReminder(pagination, loggedInUserId));

	}

	/*** XNFR-878 ***/
	@PostMapping("/updatePartnerCompanyPrimaryAdmin")
	public ResponseEntity<XtremandResponse> updatePartnerCompanyPrimaryAdmin(
			@RequestBody PartnerPrimaryAdminUpdateDto partnerPrimaryAdminUpdateDto) {
		boolean constraint = false;
		XtremandResponse response = new XtremandResponse();
		try {
			response = teamService.updatePartnerCompanyPrimaryAdmin(partnerPrimaryAdminUpdateDto);
			return ResponseEntity.ok(response);
		} catch (Exception e) {
			constraint = true;
			throw new XamplifyDataAccessException(e);
		} finally {
			if (!constraint && response.getStatusCode() == 200) {
				asyncComponent.sendPartnerPrimaryAdminUpdateEmail(partnerPrimaryAdminUpdateDto);

			}
		}
	}
}
