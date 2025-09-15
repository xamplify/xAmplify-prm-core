package com.xtremand.controller;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.xtremand.common.bom.Pagination;
import com.xtremand.exception.DuplicateEntryException;
import com.xtremand.formbeans.XtremandResponse;
import com.xtremand.mail.service.AsyncComponent;
import com.xtremand.team.member.dto.TeamMemberGroupDTO;
import com.xtremand.team.member.group.exception.TeamMemberGroupDataAccessException;
import com.xtremand.team.member.group.service.TeamMemberGroupService;
import com.xtremand.util.BadRequestException;
import com.xtremand.util.XamplifyUtils;
import com.xtremand.util.dto.Pageable;
import com.xtremand.vanity.url.dto.VanityUrlDetailsDTO;

@RestController
@RequestMapping(value = "/teamMemberGroup/")
public class TeamMemberGroupController {

	private static final String TEAM_MEMBER_GROUP_NAME_UNIQUE_INDEX = "xt_team_member_group_name_unique_index";

	private static final String DUPLICATENAME = "Name Already Exists";

	@Autowired
	private TeamMemberGroupService teamMemberGroupService;
	
	@Autowired
	private AsyncComponent asyncComponent;

	@PostMapping("/findDefaultModules")
	public ResponseEntity<XtremandResponse> findDefaultModules(@RequestBody VanityUrlDetailsDTO vanityUrlDetailsDTO) {
		return ResponseEntity.ok(teamMemberGroupService.findDefaultModules(vanityUrlDetailsDTO));
	}

	@PostMapping(value = "/findAll")
	public ResponseEntity<XtremandResponse> findAll(@RequestBody Pagination pagination) {
		return ResponseEntity.ok(teamMemberGroupService.findAll(pagination));
	}

	@GetMapping(value = "/findAllGroupIdsAndNames/{loggedInUserId}/{addDefaultOption}")
	public ResponseEntity<XtremandResponse> findAllGroupIdsAndNames(@PathVariable Integer loggedInUserId,
			@PathVariable boolean addDefaultOption) {
		return ResponseEntity
				.ok(teamMemberGroupService.findAllGroupIdsAndNamesByLoggedInUserId(loggedInUserId, addDefaultOption));
	}

	@GetMapping(value = "/hasSuperVisorRole/{teamMemberGroupId}")
	public ResponseEntity<XtremandResponse> hasSuperVisorRole(@PathVariable Integer teamMemberGroupId) {
		return ResponseEntity.ok(teamMemberGroupService.hasSuperVisorRole(teamMemberGroupId));
	}

	@PostMapping(value = "/findById")
	public ResponseEntity<XtremandResponse> findById(@RequestBody VanityUrlDetailsDTO vanityUrlDetailsDTO) {
		return ResponseEntity.ok(teamMemberGroupService.findById(vanityUrlDetailsDTO));
	}

	@PostMapping(value = "/save")
	public ResponseEntity<XtremandResponse> save(@RequestBody TeamMemberGroupDTO teamMemberGroupDto) {
		try {
			return ResponseEntity.ok(teamMemberGroupService.save(teamMemberGroupDto));
		} catch (DataIntegrityViolationException e) {
			if (e.getMessage().indexOf(TEAM_MEMBER_GROUP_NAME_UNIQUE_INDEX) > -1) {
				throw new DuplicateEntryException(DUPLICATENAME);
			} else {
				throw new TeamMemberGroupDataAccessException(e);
			}
		} catch (Exception e) {
			throw new TeamMemberGroupDataAccessException(e);
		}
	}

	@PostMapping(value = "/update")
	public ResponseEntity<XtremandResponse> update(@RequestBody TeamMemberGroupDTO teamMemberGroupDto) {
		XtremandResponse response = new XtremandResponse();
		boolean constraint = false;
		try {
			return ResponseEntity.ok(teamMemberGroupService.update(teamMemberGroupDto, response));
		} catch (DataIntegrityViolationException e) {
			constraint = true;
			if (e.getMessage().indexOf(TEAM_MEMBER_GROUP_NAME_UNIQUE_INDEX) > -1) {
				throw new DuplicateEntryException(DUPLICATENAME);
			} else {
				throw new TeamMemberGroupDataAccessException(e);
			}
		} catch (BadRequestException d) {
			constraint = true;
			throw new BadRequestException(d.getMessage());
		} catch (Exception e) {
			constraint = true;
			throw new TeamMemberGroupDataAccessException(e);
		} finally {
			if (!constraint && XamplifyUtils.isNotEmptyList(teamMemberGroupDto.getWhiteLabeledReApprovalDamIds())
					&& XamplifyUtils.isValidInteger(teamMemberGroupDto.getUserId()) && XamplifyUtils.isValidInteger(teamMemberGroupDto.getCompanyId())) {
				asyncComponent.handleWhiteLabeledAssetsAfterReApproval(teamMemberGroupDto.getWhiteLabeledReApprovalDamIds(),
						teamMemberGroupDto.getCompanyId(), teamMemberGroupDto.getUserId());
			}
		}
	}

	@GetMapping(value = "/delete/{id}")
	public ResponseEntity<XtremandResponse> delete(@PathVariable Integer id) {
		try {
			return ResponseEntity.ok(teamMemberGroupService.delete(id));
		} catch (DuplicateEntryException e) {
			throw new DuplicateEntryException(e.getMessage());
		}

	}

	@GetMapping(value = "/previewById/{id}")
	public ResponseEntity<XtremandResponse> previewById(@PathVariable Integer id) {
		return ResponseEntity.ok(teamMemberGroupService.previewGroupDetailsById(id));
	}

	@GetMapping(value = "/findSelectedTeamMemberIds/{partnershipId}")
	public ResponseEntity<XtremandResponse> findSelectedTeamMemberIds(@PathVariable Integer partnershipId) {
		return ResponseEntity.ok(teamMemberGroupService.findSelectedTeamMemberIdsByPartnershipId(partnershipId));
	}

	@GetMapping(value = "/getPartnersCount/{teamMemberGroupId}")
	public ResponseEntity<XtremandResponse> findPartnersCountByTeamMemberGroupId(
			@PathVariable Integer teamMemberGroupId) {
		return ResponseEntity.ok(teamMemberGroupService.findPartnersCountByTeamMemberGroupId(teamMemberGroupId));
	}

	/*** XNFR-883 ***/
	@PutMapping("/{id}/default-sso")
	public ResponseEntity<XtremandResponse> updateDefaultSSOGroup(@PathVariable Integer id) {
		return new ResponseEntity<>(teamMemberGroupService.updateDefaultSSOGroup(id), HttpStatus.OK);
	}

	/** XNFR-883 ***/
	@GetMapping(value = "/groups/default-sso-first/{companyProfileName}/{loggedInUserId}")
	public ResponseEntity<XtremandResponse> findAllGroupIdsAndNamesWithDefaultSSOFirst(
			@PathVariable String companyProfileName, @PathVariable Integer loggedInUserId) {
		return ResponseEntity.ok(
				teamMemberGroupService.findAllGroupIdsAndNamesWithDefaultSSOFirst(companyProfileName, loggedInUserId));
	}

	/*** XNFR-1051 ***/
	@GetMapping(value = "paginated/signUpUrls")
	public ResponseEntity<XtremandResponse> findPaginatedTeamMemberGroupSignUpUrls(@RequestParam Integer loggedInUserId,
			@RequestParam(required = false, defaultValue = "") String domainName,
			@RequestParam(required = false, defaultValue = "false") boolean isVanityLogin, @Valid Pageable pageable) {
		return ResponseEntity.ok(teamMemberGroupService.findPaginatedTeamMemberGroupSignUpUrls(loggedInUserId,
				domainName, isVanityLogin, pageable));
	}

}
