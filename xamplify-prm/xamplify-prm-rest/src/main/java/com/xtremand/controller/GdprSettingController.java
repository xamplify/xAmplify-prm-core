package com.xtremand.controller;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.xtremand.exception.DuplicateEntryException;
import com.xtremand.formbeans.XtremandResponse;
import com.xtremand.gdpr.setting.dto.GdprSettingDTO;
import com.xtremand.gdpr.setting.dto.LegalBasisSaveRequest;
import com.xtremand.gdpr.setting.exception.GdprSettingDataAccessException;
import com.xtremand.gdpr.setting.service.GdprSettingService;
import com.xtremand.user.service.UserService;

@RequestMapping(value = "/gdpr/setting/")
@RestController
public class GdprSettingController {

	@Autowired
	private GdprSettingService gdprSettingService;
	
	@Autowired
	private UserService userService;

	@PostMapping(value = "save")
	public ResponseEntity<XtremandResponse> save(@RequestBody GdprSettingDTO gdprSettingDto) {
		try {
			return ResponseEntity.ok(gdprSettingService.save(gdprSettingDto));
		} catch (DataIntegrityViolationException e) {
			if (e.getMessage().indexOf("xt_gdpr_setting_company_id_unique") > -1) {
				throw new DuplicateEntryException(
						"Gpdr settings already exists for this company.New settings can't be created.");
			} else {
				throw new GdprSettingDataAccessException(e);
			}
		} catch (Exception e) {
			throw new GdprSettingDataAccessException(e);
		}
	}
	
	
	@GetMapping(value = "getByCompanyId/{companyId}")
	public ResponseEntity<XtremandResponse> getGdprSettingsByCompanyId(@PathVariable Integer companyId) {
		try {
			return ResponseEntity.ok(gdprSettingService.getByCompanyId(companyId));
		}catch (Exception e) {
			throw new GdprSettingDataAccessException(e);
		}
	}
	
	
	@PostMapping(value = "update")
	public ResponseEntity<XtremandResponse> update(@RequestBody GdprSettingDTO gdprSettingDto) {
		try {
			return ResponseEntity.ok(gdprSettingService.update(gdprSettingDto));
		}catch (Exception e) {
			throw new GdprSettingDataAccessException(e);
		}
	}

	
	@GetMapping(value = "isGdprEnabled/{userId}")
	public ResponseEntity<Boolean> isGdprEnabled(@PathVariable Integer userId) {
		try {
			return ResponseEntity.ok(gdprSettingService.isGdprEnabled(userService.getCompanyIdByUserId(userId)));
		}catch (Exception e) {
			throw new GdprSettingDataAccessException(e);
		}
	}
	//***************************** SWATHI'S CODE ***************************************************
	
	@GetMapping(value = "legal_basis/{companyId}")
	public ResponseEntity<XtremandResponse> getLegalBasis(
			@PathVariable @NotNull @Min(value = 1, message = "User ID should be greater than or equal to 1") Integer companyId) {
		try {
			return ResponseEntity.ok(gdprSettingService.getLegalBasis(companyId));
		}catch (Exception e) {
			throw new GdprSettingDataAccessException(e);
		}
	}
	
	@PostMapping(value = "legal_basis")
	public ResponseEntity<XtremandResponse> saveLegalBasis(
			@RequestBody @NotNull LegalBasisSaveRequest request) {
		try {
			return ResponseEntity.ok(gdprSettingService.saveLegalBasis(request));
		}catch (Exception e) {
			throw new GdprSettingDataAccessException(e);
		}
	}
}
