package com.xtremand.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.xtremand.campaign.exception.XamplifyDataAccessException;
import com.xtremand.form.dto.SelectedFieldsResponseDTO;
import com.xtremand.formbeans.XtremandResponse;
import com.xtremand.selectedfield.service.SelectedFieldsService;
import com.xtremand.util.BadRequestException;

@RestController
@RequestMapping(value = "/selected/fields")
public class SelectedFieldsController {
	
	@Autowired
	private SelectedFieldsService selectedFieldsService;
	
	@PostMapping
	public ResponseEntity<XtremandResponse> saveSelectedFields(
			@RequestBody SelectedFieldsResponseDTO selectedFieldsResponseDto, BindingResult result) {		
		XtremandResponse response = new XtremandResponse();
		try {
			response = selectedFieldsService.saveOrUpdateSelectedFields(selectedFieldsResponseDto, result);
			return ResponseEntity.ok(response);
		} catch (BadRequestException e) {
			throw new BadRequestException(e.getMessage());
		} catch (Exception e) {
			throw new XamplifyDataAccessException(e);
		}
	}
	
	@GetMapping(value="/isMyPreferances/{userId}/{companyProfileName}/{opportunityType}")
	public ResponseEntity<XtremandResponse> isMyPreferances(@PathVariable Integer userId, @PathVariable String companyProfileName,@PathVariable String opportunityType) {
		return ResponseEntity.ok(selectedFieldsService.isMyPreferances(userId,companyProfileName,opportunityType));
	}
	

	/** XNFR-840 **/
	@GetMapping(value="export-excel/{companyProfileName}/{userType}/{userId}/{customFormName}/{opportunityType}/{myprofile}")
	public ResponseEntity<XtremandResponse> getExportExcelColumns(@PathVariable String companyProfileName, @PathVariable String userType,@PathVariable Integer userId, @PathVariable String customFormName, @PathVariable String opportunityType,@PathVariable boolean myprofile ) {
		return ResponseEntity.ok(selectedFieldsService.getExportExcelColumns(companyProfileName,userType,userId, customFormName,opportunityType,myprofile));
	}
	/** XNFR-840 **/
}
