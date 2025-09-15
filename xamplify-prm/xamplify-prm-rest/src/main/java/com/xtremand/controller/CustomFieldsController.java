package com.xtremand.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.xtremand.custom.field.dto.CustomFieldsDTO;
import com.xtremand.customfields.service.CustomFieldsService;
import com.xtremand.formbeans.XtremandResponse;
import com.xtremand.lead.bom.OpportunityType;

@RestController
@RequestMapping("/customFields")
public class CustomFieldsController {

	@Autowired
	CustomFieldsService customFieldsService;

	@GetMapping("/{loggedInUserId}/{opportunityType}")
	public ResponseEntity<XtremandResponse> getCustomFields(@PathVariable Integer loggedInUserId, @PathVariable OpportunityType opportunityType) {
		return ResponseEntity.ok(customFieldsService.getCustomFields(loggedInUserId, opportunityType));
	}

	@PostMapping("/save")
	public ResponseEntity<XtremandResponse> saveCustomField(@RequestBody CustomFieldsDTO customFieldsDTO) {
		return ResponseEntity.ok(customFieldsService.saveCustomField(customFieldsDTO));
	}

	@PostMapping("/sync")
	public ResponseEntity<XtremandResponse> syncCustomForm(@RequestBody CustomFieldsDTO customFieldsDTO) {
		return ResponseEntity.ok(customFieldsService.syncCustomForm(customFieldsDTO));
	}

	@DeleteMapping("/delete/{loggedInUserId}/{customFieldId}")
	public ResponseEntity<XtremandResponse> deleteCustomField(@PathVariable Integer customFieldId,
			@PathVariable Integer loggedInUserId) {
		return ResponseEntity.ok(customFieldsService.deleteCustomField(customFieldId, loggedInUserId));
	}

	@GetMapping("/leads/count/{customFieldId}")
	public ResponseEntity<XtremandResponse> getLeadCountForCustomField(@PathVariable Integer customFieldId) {
		return ResponseEntity.ok(customFieldsService.getLeadCountForCustomField(customFieldId));
	}

}
