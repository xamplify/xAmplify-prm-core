package com.xtremand.guide.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.xtremand.common.bom.Pagination;
import com.xtremand.formbeans.XtremandResponse;
import com.xtremand.guide.service.UserGuideService;
import com.xtremand.vanity.url.dto.VanityUrlDetailsDTO;

@RestController
@RequestMapping("/user/guide/")
public class UserGuideController {

	@Autowired
	private UserGuideService userGuideService;

	@GetMapping(value = "/get/{mergeTagName}")
	public ResponseEntity<XtremandResponse> getTagIdByName(@PathVariable String mergeTagName) {
		return ResponseEntity.ok(userGuideService.getTagIdByName(mergeTagName));
	}
	
	@PostMapping(value = "/moduleName")
	public ResponseEntity<XtremandResponse> getUserGudesByModuleName(@RequestBody Pagination pagination) {
		return ResponseEntity.ok(userGuideService.getUserGudesByModuleName(pagination));
	}
	
	@PostMapping(value = "/title")
	public ResponseEntity<XtremandResponse> getGuideLinkByTitle(@RequestBody Pagination pagination) {
		return ResponseEntity.ok(userGuideService.getGuideLinkByTitle(pagination));
	}
	
	@PostMapping(value = "/slug")
	public ResponseEntity<XtremandResponse> getUserGuideBySlug(@RequestBody Pagination pagination) {
		return ResponseEntity.ok(userGuideService.getUserGuideBySlug(pagination));
	}
	@PostMapping(value = "/search")
	public ResponseEntity<XtremandResponse> getSearchResults(@RequestBody Pagination pagination) {
		return ResponseEntity.ok(userGuideService.getSearchResults(pagination));
	}
	
	@PostMapping(value = "/getGuidesForDashboard")
	public ResponseEntity<XtremandResponse> getUserRolesForDashBoard(@RequestBody VanityUrlDetailsDTO vanityUrlDetailsDTO) {
		return ResponseEntity.ok(userGuideService.getUserRolesForDashBoard(vanityUrlDetailsDTO));
	}
	
	@GetMapping(value="moduleId/{moduleId}")
	public ResponseEntity<XtremandResponse> getModuleNameByModuleId(@PathVariable Integer moduleId){
		return ResponseEntity.ok(userGuideService.getModuleNameByModuleId(moduleId));
	}
	

}
