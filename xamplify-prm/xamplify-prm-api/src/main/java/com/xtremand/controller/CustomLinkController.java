package com.xtremand.controller;

import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.custom.link.dto.CustomLinkRequestDTO;
import com.xtremand.campaign.exception.XamplifyDataAccessException;
import com.xtremand.custom.link.bom.CustomLinkType;
import com.xtremand.custom.link.service.CustomLinkService;
import com.xtremand.exception.DuplicateEntryException;
import com.xtremand.formbeans.XtremandResponse;
import com.xtremand.util.BadRequestException;
import com.xtremand.util.dto.Pageable;
import com.xtremand.util.dto.XamplifyConstants;

@RestController
@RequestMapping(value = "/customLinks")
public class CustomLinkController {

	@Autowired
	private CustomLinkService customLinkService;

	@PostMapping
	public ResponseEntity<XtremandResponse> save(@Valid @RequestBody CustomLinkRequestDTO customLinkRequestDTO,
			BindingResult result) {
		XtremandResponse response = new XtremandResponse();
		try {
			response = customLinkService.save(customLinkRequestDTO, result, null);
			return ResponseEntity.ok(response);
		} catch (DataIntegrityViolationException e) {
			if (e.getMessage().indexOf(XamplifyConstants.CUSTOM_LINK_UNIQUE_INDEX) > -1) {
				throw new DuplicateEntryException(XamplifyConstants.DUPLICATE_TITLE);
			} else {
				throw new XamplifyDataAccessException(e);
			}
		} catch (BadRequestException e) {
			throw new BadRequestException(e.getMessage());
		} catch (Exception e) {
			throw new XamplifyDataAccessException(e);
		}
	}

	@PutMapping("/{id}")
	public ResponseEntity<XtremandResponse> update(@Valid @PathVariable Integer id,
			@RequestBody CustomLinkRequestDTO customLinkRequestDto, BindingResult result) {
		XtremandResponse response = new XtremandResponse();
		try {
			customLinkRequestDto.setId(id);
			response = customLinkService.update(customLinkRequestDto, result, null);
			return ResponseEntity.ok(response);
		} catch (DataIntegrityViolationException e) {
			if (e.getMessage().indexOf(XamplifyConstants.CUSTOM_LINK_UNIQUE_INDEX) > -1) {
				throw new DuplicateEntryException(XamplifyConstants.DUPLICATE_TITLE);
			} else {
				throw new XamplifyDataAccessException(e);
			}
		} catch (BadRequestException e) {
			throw new BadRequestException(e.getMessage());
		} catch (AccessDeniedException e) {
			throw new AccessDeniedException(e.getMessage());
		} catch (Exception e) {
			throw new XamplifyDataAccessException(e);
		}

	}

	@GetMapping("newsAndAnnouncements/userId/{loggedInUserId}/domainName/{vendorCompanyProfileName}")
	public ResponseEntity<XtremandResponse> findAll(@PathVariable Integer loggedInUserId,
			@PathVariable String vendorCompanyProfileName, @Valid Pageable pageable, BindingResult result) {
		List<String> types = new ArrayList<>();
		types.add(CustomLinkType.NEWS.name());
		types.add(CustomLinkType.ANNOUNCEMENTS.name());
		return new ResponseEntity<>(
				customLinkService.findAll(pageable, result, loggedInUserId, types, vendorCompanyProfileName),
				HttpStatus.OK);
	}

	@GetMapping("dashboardBanners/userId/{loggedInUserId}/domainName/{vendorCompanyProfileName}")
	public ResponseEntity<XtremandResponse> findDashboardBanners(@PathVariable Integer loggedInUserId,
			@PathVariable String vendorCompanyProfileName, @Valid Pageable pageable, BindingResult result) {
		List<String> types = new ArrayList<>();
		types.add(CustomLinkType.DASHBOARD_BANNERS.name());
		return new ResponseEntity<>(
				customLinkService.findAll(pageable, result, loggedInUserId, types, vendorCompanyProfileName),
				HttpStatus.OK);
	}

	@PostMapping(value = "dashboardBanners", consumes = { "multipart/form-data" })
	public ResponseEntity<XtremandResponse> saveDashboardBanner(
			@RequestPart(value = "dashboardBannerImage", required = false) MultipartFile dashboardBannerImage,
			@RequestPart("customLinkDto") CustomLinkRequestDTO customLinkRequestDTO, BindingResult result) {
		XtremandResponse response = new XtremandResponse();
		try {
			response = customLinkService.save(customLinkRequestDTO, result, dashboardBannerImage);
			return ResponseEntity.ok(response);
		} catch (DataIntegrityViolationException e) {
			if (e.getMessage().indexOf(XamplifyConstants.CUSTOM_LINK_UNIQUE_INDEX) > -1) {
				throw new DuplicateEntryException(XamplifyConstants.DUPLICATE_TITLE);
			} else {
				throw new XamplifyDataAccessException(e);
			}
		} catch (AccessDeniedException e) {
			throw new AccessDeniedException(e.getMessage());
		} catch (BadRequestException e) {
			throw new BadRequestException(e.getMessage());
		} catch (Exception e) {
			throw new XamplifyDataAccessException(e);
		}
	}

	@PostMapping(value = "dashboardBanners/{id}", consumes = { "multipart/form-data" })
	public ResponseEntity<XtremandResponse> updateDashboardBannder(@Valid @PathVariable Integer id,
			@RequestPart(value = "dashboardBannerImage", required = false) MultipartFile dashboardBannerImage,
			@RequestPart("customLinkDto") CustomLinkRequestDTO customLinkRequestDto, BindingResult result) {
		XtremandResponse response = new XtremandResponse();
		try {
			customLinkRequestDto.setId(id);
			response = customLinkService.update(customLinkRequestDto, result, dashboardBannerImage);
			return ResponseEntity.ok(response);
		} catch (DataIntegrityViolationException e) {
			if (e.getMessage().indexOf(XamplifyConstants.CUSTOM_LINK_UNIQUE_INDEX) > -1) {
				throw new DuplicateEntryException(XamplifyConstants.DUPLICATE_TITLE);
			} else {
				throw new XamplifyDataAccessException(e);
			}
		} catch (BadRequestException e) {
			throw new BadRequestException(e.getMessage());
		} catch (AccessDeniedException e) {
			throw new AccessDeniedException(e.getMessage());
		} catch (Exception e) {
			throw new XamplifyDataAccessException(e);
		}

	}

	@GetMapping("id/{id}/loggedInUserId/{loggedInUserId}")
	public ResponseEntity<XtremandResponse> getById(@PathVariable Integer id, @PathVariable Integer loggedInUserId) {
		return ResponseEntity.ok(customLinkService.getById(id, loggedInUserId));
	}

	@DeleteMapping("id/{id}/loggedInUserId/{loggedInUserId}")
	public ResponseEntity<XtremandResponse> delete(@PathVariable Integer id, @PathVariable Integer loggedInUserId) {
		return ResponseEntity.ok(customLinkService.delete(id, loggedInUserId));
	}

}
