package com.xtremand.controller;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.xtremand.campaign.exception.XamplifyDataAccessException;
import com.xtremand.common.bom.Pagination;
import com.xtremand.domain.bom.DomainModuleNameType;
import com.xtremand.domain.dto.DomainRequestDTO;
import com.xtremand.domain.service.DomainService;
import com.xtremand.exception.DuplicateEntryException;
import com.xtremand.formbeans.XtremandResponse;
import com.xtremand.util.BadRequestException;
import com.xtremand.util.dto.Pageable;
import com.xtremand.util.service.CsvUtilService;

@RestController
@RequestMapping(value = "/domain")
public class DomainController {

	private static final String PARTNER_SUFFIX_URL = "partners";

	@Autowired
	private CsvUtilService csvUtilService;

	@Autowired
	private DomainService domainService;

	@GetMapping("/downloadDefaultCsv/Add-Domains.csv")
	public void downloadDefaultCsv(HttpServletResponse response) {
		csvUtilService.downloadDefaultCsv(response, "DOMAIN NAME");
	}

	@GetMapping("/signUpUrl")
	public ResponseEntity<XtremandResponse> getSignUpUrl(@RequestParam Integer loggedInUserId,
			@RequestParam(required = false, defaultValue = "false") boolean isVanityLogin,
			@RequestParam(required = false, defaultValue = "") String domainName) {
		return new ResponseEntity<>(domainService.getTeamMemberSignUpUrl(loggedInUserId, isVanityLogin, domainName),
				HttpStatus.OK);
	}

	@GetMapping(PARTNER_SUFFIX_URL + "/signUpUrl")
	public ResponseEntity<XtremandResponse> getPartnerSignUpUrl(@RequestParam Integer loggedInUserId,
			@RequestParam(required = false, defaultValue = "false") boolean isVanityLogin,
			@RequestParam(required = false, defaultValue = "") String domainName) {
		return new ResponseEntity<>(domainService.getPartnerSignUpUrl(loggedInUserId, isVanityLogin, domainName),
				HttpStatus.OK);
	}

	@PostMapping
	public ResponseEntity<XtremandResponse> save(@RequestBody DomainRequestDTO domainRequestDto) {
		return savePartnerOrTeamMemberDomain(domainRequestDto, DomainModuleNameType.TEAM_MEMBER);
	}

	@PostMapping(value = "/" + PARTNER_SUFFIX_URL)
	public ResponseEntity<XtremandResponse> savePartnerDomain(@RequestBody DomainRequestDTO domainRequestDto) {
		return savePartnerOrTeamMemberDomain(domainRequestDto, DomainModuleNameType.PARTNER);
	}

	private ResponseEntity<XtremandResponse> savePartnerOrTeamMemberDomain(DomainRequestDTO domainRequestDto,
			DomainModuleNameType type) {
		try {
			return ResponseEntity.ok(domainService.save(domainRequestDto, type));
		} catch (DataIntegrityViolationException e) {
			if (e.getMessage().indexOf("xt_allowed_domain_unique_index") > -1) {
				throw new DuplicateEntryException("Already Exists");
			} else {
				throw new XamplifyDataAccessException(e);
			}
		} catch (DuplicateEntryException e) {
			throw new DuplicateEntryException(e.getMessage());
		} catch (BadRequestException e) {
			throw new BadRequestException(e.getMessage());
		} catch (Exception e) {
			throw new XamplifyDataAccessException(e);
		}
	}

	@GetMapping("/{loggedInUserId}")
	public ResponseEntity<XtremandResponse> findAll(@PathVariable Integer loggedInUserId, @Valid Pageable pageable) {
		return new ResponseEntity<>(domainService.findAll(pageable, loggedInUserId, DomainModuleNameType.TEAM_MEMBER),
				HttpStatus.OK);
	}

	@GetMapping("/" + PARTNER_SUFFIX_URL + "/{loggedInUserId}")
	public ResponseEntity<XtremandResponse> findAllPartnerDomains(@PathVariable Integer loggedInUserId,
			@Valid Pageable pageable) {
		return new ResponseEntity<>(domainService.findAll(pageable, loggedInUserId, DomainModuleNameType.PARTNER),
				HttpStatus.OK);
	}

	/***** 972 *****/
	@GetMapping("/" + PARTNER_SUFFIX_URL + "/{loggedInUserId}/domain-names")
	public ResponseEntity<XtremandResponse> getAllDomainNames(@PathVariable Integer loggedInUserId) {
		return new ResponseEntity<>(domainService.findAllDomainNames(loggedInUserId, DomainModuleNameType.PARTNER),
				HttpStatus.OK);
	}

	@DeleteMapping("id/{id}/loggedInUserId/{loggedInUserId}")
	public ResponseEntity<XtremandResponse> deleteDomain(@PathVariable Integer id,
			@PathVariable Integer loggedInUserId) {
		return ResponseEntity.ok(domainService.deleteDomain(id, loggedInUserId));
	}

	@PostMapping("/downloadDomainsCsv/" + PARTNER_SUFFIX_URL)
	public ResponseEntity<Void> downloadPartnerDomainCsv(@RequestBody Pagination pagination,
			HttpServletResponse response) {
		downloadPartnerOrTeamMemberDomainCsv(pagination, response, DomainModuleNameType.PARTNER);
		return ResponseEntity.status(HttpStatus.OK).body(null);
	}

	@PostMapping("/downloadDomainsCsv")
	public ResponseEntity<Void> downloadDomainCsv(@RequestBody Pagination pagination, HttpServletResponse response) {
		downloadPartnerOrTeamMemberDomainCsv(pagination, response, DomainModuleNameType.TEAM_MEMBER);
		return ResponseEntity.status(HttpStatus.OK).body(null);
	}

	private void downloadPartnerOrTeamMemberDomainCsv(Pagination pagination, HttpServletResponse response,
			DomainModuleNameType type) {
		domainService.downloadDomainCsv(pagination, response, type);
	}

	@PostMapping(value = "/" + PARTNER_SUFFIX_URL + "/updateDomain")
	public ResponseEntity<XtremandResponse> updatePartnerDomain(@RequestBody DomainRequestDTO domainRequestDto) {
		return new ResponseEntity<>(domainService.updatePartnerDomain(domainRequestDto, DomainModuleNameType.PARTNER),
				HttpStatus.OK);
	}

}
