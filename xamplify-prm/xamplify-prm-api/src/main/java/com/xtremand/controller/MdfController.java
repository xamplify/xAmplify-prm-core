package com.xtremand.controller;

import java.net.URI;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.xtremand.common.bom.Pagination;
import com.xtremand.form.dto.FormDTO;
import com.xtremand.formbeans.XtremandResponse;
import com.xtremand.mdf.dto.MdfDetailsDTO;
import com.xtremand.mdf.dto.MdfRequestCommentDTO;
import com.xtremand.mdf.dto.MdfRequestPostDTO;
import com.xtremand.mdf.dto.MdfRequestUploadDTO;
import com.xtremand.mdf.dto.MdfRequestViewDTO;
import com.xtremand.mdf.exception.DuplicateRequestTitleException;
import com.xtremand.mdf.service.MdfService;
import com.xtremand.vanity.url.dto.VanityUrlDetailsDTO;

@RestController
@RequestMapping(value = "/mdf/")
public class MdfController {
	
	
	@Autowired
	private MdfService mdfService;
	
	/****Used in /home/mdf/details(manage-mdf-details.component.ts)********/
	@GetMapping("getVendorMdfAmountTilesInfo/{vendorCompanyId}/{loggedInUserId}/{applyFilter}")
	@ResponseBody
	public ResponseEntity<XtremandResponse> getVendorMdfAmountTilesInfo(@PathVariable Integer vendorCompanyId,@PathVariable Integer loggedInUserId,@PathVariable boolean applyFilter) {
		return ResponseEntity.ok(mdfService.getVendorMdfAmountTilesInfo(vendorCompanyId, loggedInUserId,applyFilter));
	}
	
	/****Used in /home/mdf/details(manage-mdf-details.component.ts)********/
	@PostMapping("listPartners")
	@ResponseBody
	public ResponseEntity<XtremandResponse> listPartners(@RequestBody Pagination pagination) {
		return ResponseEntity.ok(mdfService.listPartners(pagination));
	}
	
	@GetMapping("getPartnerAndMdfAmountDetails/{partnershipId}")
	@ResponseBody
	public ResponseEntity<XtremandResponse> getPartnerAndMdfAmountDetails(@PathVariable Integer partnershipId) {
		return ResponseEntity.ok(mdfService.getPartnerAndMdfAmountDetails(partnershipId));
	}
	
	/****Used in /home/mdf/details(manage-mdf-details.component.ts)(Add/Remove MDF Amount Modal Popup)********/
	@PostMapping("updateMdfAmount")
	@ResponseBody
	public ResponseEntity<XtremandResponse> updateMdfAmount(@RequestBody MdfDetailsDTO mdfDetailsDTO) {
		return ResponseEntity.ok(mdfService.updateMdfAmount(mdfDetailsDTO));
	}
	
	/**********MDF Requests*********************/
	
	@PostMapping("getMdfRequestTilesInfoForPartners")
	@ResponseBody
	public ResponseEntity<XtremandResponse> getMdfRequestTilesInfoForPartners(@RequestBody VanityUrlDetailsDTO vanityUrlDetailsDto) {
		return ResponseEntity.ok(mdfService.getMdfRequestsPartnerTiles(vanityUrlDetailsDto));
	}
	
	@GetMapping("getMdfRequestTilesInfoForVendors/{vendorCompanyId}/{loggedInUserId}/{applyFilter}")
	@ResponseBody
	public ResponseEntity<XtremandResponse> getMdfRequestTilesInfoForVendors(@PathVariable Integer vendorCompanyId,@PathVariable Integer loggedInUserId,@PathVariable boolean applyFilter) {
		return ResponseEntity.ok(mdfService.getMdfRequestTilesInfoForVendors(vendorCompanyId, loggedInUserId, applyFilter));
	}
	
	@GetMapping("getPartnerMdfAmountTilesInfo/{vendorCompanyId}/{partnerCompanyId}")
	@ResponseBody
	public ResponseEntity<XtremandResponse> getPartnerMdfAmountTilesInfo(@PathVariable Integer vendorCompanyId,@PathVariable Integer partnerCompanyId) {
		return ResponseEntity.ok(mdfService.getPartnerMdfAmountTilesInfo(vendorCompanyId,partnerCompanyId));
	}
	
	
	@PostMapping(value="listMdfAccessVendors")
	@ResponseBody
	public ResponseEntity<XtremandResponse> listMdfAccessVendorCompanyDetailsByPartnerCompanyId(@RequestBody Pagination pagination){
		return ResponseEntity.ok(mdfService.listVendorsAndRequestsCountByPartnerCompanyId(pagination));
	}
	

	@GetMapping("getMdfRequestFormForPartner/{vendorCompanyId}")
	@ResponseBody
	public ResponseEntity<XtremandResponse> getMdfRequestFormForPartner(@PathVariable Integer vendorCompanyId){
		return ResponseEntity.ok(mdfService.getMdfRequestForm(vendorCompanyId,true));
	}
	
	@GetMapping("getMdfRequestForm/{vendorCompanyId}")
	@ResponseBody
	public ResponseEntity<XtremandResponse> getMdfRequestForm(@PathVariable Integer vendorCompanyId){
		return ResponseEntity.ok(mdfService.getMdfRequestForm(vendorCompanyId,false));
	}
	
	
	@PostMapping("saveMdfRequest")
	@ResponseBody
	public ResponseEntity<XtremandResponse> saveMdfRequest(@RequestBody MdfRequestPostDTO mdfRequestPostDTO) {
		try {
			return ResponseEntity.ok(mdfService.saveMdfRequest(mdfRequestPostDTO));
		} catch (DuplicateRequestTitleException e) {
			throw new DuplicateRequestTitleException(e.getMessage());
		}
	}
	
	@PostMapping("updateMdfRequest")
	@ResponseBody
	public ResponseEntity<XtremandResponse> updateMdfRequest(@RequestBody MdfRequestViewDTO mdfRequestViewDTO) {
		return ResponseEntity.ok(mdfService.updateMdfRequest(mdfRequestViewDTO));
	}
	
	@PostMapping("createMdfForm")
	@ResponseBody
	public ResponseEntity<XtremandResponse> createMdfForm(@RequestBody FormDTO formDto) {
		return ResponseEntity.ok(mdfService.createMdfForm(formDto));
	}
	
	
	@PostMapping("updateMdfForm")
	@ResponseBody
	public ResponseEntity<XtremandResponse> updateMdfForm(@RequestBody FormDTO formDto) {
		return ResponseEntity.ok(mdfService.updateMdfForm(formDto));
	}
	
	@PostMapping("listMdfFormDetails")
	@ResponseBody
	public ResponseEntity<XtremandResponse> listMdfFormDetails(@RequestBody Pagination pagination) {
		return ResponseEntity.ok(mdfService.listMdfFormDetails(pagination));
	}
	
	/*****change-mdf-request**********/
	@GetMapping("getRequestDetailsById/{id}/{loggedInUserCompanyId}")
	@ResponseBody
	public ResponseEntity<XtremandResponse> getRequestDetailsById(@PathVariable Integer id,@PathVariable Integer loggedInUserCompanyId) {
		return ResponseEntity.ok(mdfService.getRequestDetailsById(id,loggedInUserCompanyId));
	}
	
	@GetMapping("getMdfDetailsTimeLineHistory/{id}/{loggedInUserCompanyId}")
	@ResponseBody
	public ResponseEntity<XtremandResponse> getMdfDetailsTimeLineHistory(@PathVariable Integer id,@PathVariable Integer loggedInUserCompanyId) {
		return ResponseEntity.ok(mdfService.getMdfDetailsTimeLineHistory(id,loggedInUserCompanyId));
	}
	
	@GetMapping("getRequestDetailsAndTimeLineHistory/{id}/{loggedInUserCompanyId}")
	@ResponseBody
	public ResponseEntity<XtremandResponse> getRequestDetailsAndTimeLineHistory(@PathVariable Integer id,@PathVariable Integer loggedInUserCompanyId) {
		return ResponseEntity.ok(mdfService.getRequestDetailsAndTimeLineHistory(id,loggedInUserCompanyId));
	}
	
	
	@PostMapping(value="uploadDocuments", consumes = { "multipart/form-data" })
	@ResponseBody
	public ResponseEntity<XtremandResponse> save(@RequestPart("uploadedFile") MultipartFile uploadedFile, @RequestPart("mdfRequestUploadDto") MdfRequestUploadDTO mdfRequestUploadDto){
		return ResponseEntity.ok(mdfService.uploadRequestDocuments(uploadedFile,mdfRequestUploadDto));
	}

	
	@PostMapping("listMdfRequestDocuments")
	@ResponseBody
	public ResponseEntity<XtremandResponse> listMdfRequestDocuments(@RequestBody Pagination pagination) {
		return ResponseEntity.ok(mdfService.listRequestDocuments(pagination));
	}
	

	@PostMapping("saveComment")
	@ResponseBody
	public ResponseEntity<XtremandResponse> saveComment(@RequestBody MdfRequestCommentDTO mdfRequestCommentDTO) {
		return ResponseEntity.ok(mdfService.saveComment(mdfRequestCommentDTO));
	}
	
	@GetMapping("listComments/{requestId}")
	@ResponseBody
	public ResponseEntity<XtremandResponse> listComments(@PathVariable Integer requestId) {
		return ResponseEntity.ok(mdfService.listComments(requestId));
	}
	
	@GetMapping(value = "download/{alias}")
	public ResponseEntity<Void> getAndRedirect(@PathVariable String alias) {
		String url = mdfService.getMdfDocumentAwsFilePathByAlias(alias);
		return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(url)).build();
	}
	
	

	

}
