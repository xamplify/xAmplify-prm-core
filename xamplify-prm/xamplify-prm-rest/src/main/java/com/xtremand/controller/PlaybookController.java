package com.xtremand.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.xtremand.campaign.exception.XamplifyDataAccessException;
import com.xtremand.formbeans.XtremandResponse;
import com.xtremand.lms.bom.LearningTrackType;
import com.xtremand.lms.service.LMSService;
import com.xtremand.util.BadRequestException;

@RestController
@RequestMapping(value = "/playbook/")
public class PlaybookController {

	@Autowired
	private LMSService lmsService;

	@GetMapping("{companyId}/{slug}/{loggedInUserId}")
	public ResponseEntity<XtremandResponse> getBySlug(@PathVariable Integer companyId, @PathVariable String slug,
			@PathVariable Integer loggedInUserId) {
		return ResponseEntity.ok(lmsService.getBySlug(LearningTrackType.PLAYBOOK, companyId, slug, loggedInUserId));
	}
	
	
	/** XNFR-745 **/
	@GetMapping("getGroupedPlaybookAssetsBySlug/{companyId}/{slug}/{sortKey}")
	public ResponseEntity<XtremandResponse> getGroupedAssetsBySlug(@PathVariable Integer companyId,
			@PathVariable String slug, @PathVariable String sortKey) {
		XtremandResponse response = new XtremandResponse();
		try {
			response = lmsService.getGroupedAssetsBySlug(LearningTrackType.PLAYBOOK, companyId, slug, sortKey);
			return ResponseEntity.ok(response);
		} catch (BadRequestException e) {
			throw new BadRequestException(e.getMessage());
		} catch (Exception e) {
			throw new XamplifyDataAccessException(e);
		}
	}
	
	/** XNFR-745 **/
	@GetMapping("checkGroupByAssetsEnabledForPlaybook/{companyId}/{slug}/{type}")
	public ResponseEntity<XtremandResponse> checkGroupByAssetsEnabled(@PathVariable Integer companyId,
			@PathVariable String slug, @PathVariable String type) {
		XtremandResponse response = new XtremandResponse();
		try {
			response = lmsService.checkGroupByAssetsEnabled(type, companyId, slug);
			return ResponseEntity.ok(response);
		} catch (BadRequestException e) {
			throw new BadRequestException(e.getMessage());
		} catch (Exception e) {
			throw new XamplifyDataAccessException(e);
		}
	}

	@GetMapping("preview/{type}/{companyId}/{slug}/{loggedInUserId}")
	public ResponseEntity<XtremandResponse> getPreviewBySlug(@PathVariable String type, @PathVariable Integer companyId,
			@PathVariable String slug, @PathVariable Integer loggedInUserId) {
		LearningTrackType trackType = LearningTrackType.valueOf(type);
		return ResponseEntity.ok(lmsService.getPreviewBySlug(trackType, companyId, slug, loggedInUserId));
	}

}
