package com.xtremand.controller;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.xtremand.common.bom.Pagination;
import com.xtremand.dam.exception.DamDataAccessException;
import com.xtremand.formbeans.XtremandResponse;
import com.xtremand.lms.bom.LearningTrackType;
import com.xtremand.lms.dto.LearningTrackDto;
import com.xtremand.lms.service.LMSService;
import com.xtremand.mail.service.AsyncComponent;
import com.xtremand.partner.journey.dto.WorkflowRequestDTO;
import com.xtremand.util.ResponseUtilException;
import com.xtremand.util.XamplifyUtils;
import com.xtremand.util.dto.Pageable;
import com.xtremand.util.dto.ShareContentRequestDTO;

@RestController
@RequestMapping(value = "/lms/")
public class LMSController {
	@Autowired
	private LMSService lmsService;

	@Autowired
	private AsyncComponent asyncComponent;

	@PostMapping(value = "save", consumes = { "multipart/form-data" })
	public ResponseEntity<XtremandResponse> save(
			@RequestPart(value = "featuredImage", required = false) MultipartFile featuredImage,
			@RequestPart("learningTrack") LearningTrackDto learningTrackDto) {
		XtremandResponse response = new XtremandResponse();
		boolean exception = false;
		List<WorkflowRequestDTO> workflowRequestDTOs = learningTrackDto.getWorkflowDtos();
		try {
			response = lmsService.save(featuredImage, learningTrackDto);
			return ResponseEntity.ok(response);
		} catch (Exception e) {
			exception = true;
			throw new DamDataAccessException(e);
		} finally {
			int statusCode = response.getStatusCode();
			if (!exception && statusCode == 200) {
				asyncComponent.publishTrackOrPlayBook(learningTrackDto);
				if(XamplifyUtils.isNotEmptyList(workflowRequestDTOs)) {
					asyncComponent.savePlaybookWorkFlows(workflowRequestDTOs, learningTrackDto.getId(), learningTrackDto.getDeletedWorkflowIds(), learningTrackDto.getTitle());					
				}
			}

		}
	}

	@PostMapping(value = "edit", consumes = { "multipart/form-data" })
	public ResponseEntity<XtremandResponse> edit(
			@RequestPart(value = "featuredImage", required = false) MultipartFile featuredImage,
			@RequestPart("learningTrack") LearningTrackDto learningTrackDto) {
		XtremandResponse response = new XtremandResponse();
		boolean exception = false;
		List<WorkflowRequestDTO> workflowRequestDTOs = learningTrackDto.getWorkflowDtos();

		try {
			response = lmsService.edit(featuredImage, learningTrackDto);
			return ResponseEntity.ok(response);
		} catch (Exception e) {
			exception = true;
			throw new DamDataAccessException(e);
		} finally {
			int statusCode = response.getStatusCode();
			if (!exception && statusCode == 200) {
				asyncComponent.updatePublishTrackOrPlayBook(learningTrackDto);
				if(XamplifyUtils.isNotEmptyList(workflowRequestDTOs) || XamplifyUtils.isNotEmptyList(learningTrackDto.getDeletedWorkflowIds())) {
					asyncComponent.savePlaybookWorkFlows(workflowRequestDTOs, learningTrackDto.getId(),learningTrackDto.getDeletedWorkflowIds(), learningTrackDto.getTitle());					
				}
			}

		}
	}

	@PostMapping("list/v")
	public ResponseEntity<XtremandResponse> getLearningTracksForVendor(@RequestBody Pagination pagination) {
		return ResponseEntity.ok(lmsService.getLearningTracksForVendor(pagination));
	}

	@GetMapping("{loggedInUserId}/partner/companies")
	@Deprecated
	public ResponseEntity<XtremandResponse> getPartnerList(@PathVariable Integer loggedInUserId) {
		return ResponseEntity.ok(lmsService.getPartnerList(loggedInUserId));
	}

	@PostMapping("partner/companies")
	public ResponseEntity<XtremandResponse> getPartnerList(@RequestBody Pagination pagination) {
		return ResponseEntity.ok(lmsService.getPartnerList(pagination));
	}

	@PostMapping("slug/validate")
	public ResponseEntity<XtremandResponse> validateSlug(@RequestBody LearningTrackDto learningTrackDto) {
		return ResponseEntity.ok(lmsService.validateSlug(learningTrackDto));
	}

	@PostMapping("title/validate")
	public ResponseEntity<XtremandResponse> validateTitle(@RequestBody LearningTrackDto learningTrackDto) {
		return ResponseEntity.ok(lmsService.validateTitle(learningTrackDto));
	}

	@PostMapping("list/p")
	public ResponseEntity<XtremandResponse> getLearningTracksForPartner(@RequestBody Pagination pagination) {
		return ResponseEntity.ok(lmsService.getLearningTracksForPartner(pagination));
	}

	@GetMapping("{learningTrackId}/publish/{publish}/{loggedInUserId}")
	public ResponseEntity<XtremandResponse> publishLearningTrack(@PathVariable boolean publish,
			@PathVariable Integer learningTrackId, @PathVariable Integer loggedInUserId) {
		return ResponseEntity.ok(lmsService.publishLearningTrack(publish, learningTrackId, loggedInUserId));
	}

	@GetMapping("{companyId}/{slug}/{loggedInUserId}")
	public ResponseEntity<XtremandResponse> getBySlug(@PathVariable Integer companyId, @PathVariable String slug,
			@PathVariable Integer loggedInUserId) {
		return ResponseEntity.ok(lmsService.getBySlug(LearningTrackType.TRACK, companyId, slug, loggedInUserId));
	}

	@GetMapping("{learningTrackId}/{loggedInUserId}")
	public ResponseEntity<XtremandResponse> getById(@PathVariable Integer learningTrackId,
			@PathVariable Integer loggedInUserId) {
		return ResponseEntity.ok(lmsService.getById(learningTrackId, loggedInUserId));
	}

	@PostMapping("delete")
	public ResponseEntity<XtremandResponse> deleteLearningTrack(@RequestBody LearningTrackDto learningTrackDto) {
		return ResponseEntity.ok(lmsService.delete(learningTrackDto));
	}

	@PostMapping("p/progress")
	public ResponseEntity<XtremandResponse> updatePartnerProgress(@RequestBody LearningTrackDto learningTrackDto) {
		return ResponseEntity.ok(lmsService.updatePartnerProgress(learningTrackDto));
	}

	@PostMapping("analytics")
	public ResponseEntity<XtremandResponse> getAnalytics(@RequestBody Pagination pagination) {
		return ResponseEntity.ok(lmsService.getAnalytics(pagination));
	}

	/** XNFR-1040 **/
	@GetMapping("downloadTrackAnalytics/userId/{userId}/learningTrackId/{learningTrackId}/lmsType/{lmsType}")
	public void findAllPartnersByDamId(@PathVariable Integer userId,@PathVariable Integer learningTrackId,
			@PathVariable String lmsType, @Valid Pagination pagination, @Valid Pageable pageable, HttpServletResponse response) {
		lmsService.downloadTrackAnalytics(pageable,userId, learningTrackId,lmsType,response);
	}
		
	@PostMapping("partner/analytics")
	public ResponseEntity<XtremandResponse> getPartnerAnalytics(@RequestBody Pagination pagination) {
		return ResponseEntity.ok(lmsService.getPartnerAnalytics(pagination));
	}
	
	/** XNFR-1040 **/
	@GetMapping("downloadPartnerTrackAnalytics/userId/{userId}/learningTrackId/{learningTrackId}/partnerCompanyId/{partnerCompanyId}/lmsType/{lmsType}")
	public void downloadPartnerTrackAnalytics(@PathVariable Integer userId, @PathVariable Integer learningTrackId,
			@PathVariable Integer partnerCompanyId, @PathVariable String lmsType, @Valid Pageable pageable,
			HttpServletResponse response) {
		lmsService.downloadPartnerTrackAnalytics(pageable,userId, learningTrackId, partnerCompanyId, lmsType, response);
	}
	@PostMapping("partner/analytics/activities")
	public ResponseEntity<XtremandResponse> getPartnerActivities(@RequestBody Pagination pagination) {
		return ResponseEntity.ok(lmsService.getPartnerActivities(pagination));
	}

	// This method is used to download the designed document created using BEE as
	// PDF
	@PostMapping(value = "/download/pdf")
	public ResponseEntity<XtremandResponse> downloadAssetPdf(@RequestParam Integer id, @RequestParam Integer contentId,
			@RequestParam Integer userId) throws IOException {
		return ResponseEntity.ok(lmsService.downloadPDF(id, contentId, userId));
	}

	@PostMapping("save-as")
	public ResponseEntity<XtremandResponse> saveAsPlaybook(@RequestBody LearningTrackDto learningTrackDto) {
		return ResponseEntity.ok(lmsService.saveAsPlaybook(learningTrackDto));
	}

	@PostMapping("partner/quiz/analytics")
	public ResponseEntity<XtremandResponse> getPartnerQuizAnalytics(@RequestBody LearningTrackDto learningTrackDto) {
		return ResponseEntity.ok(lmsService.getPartnerQuizAnalytics(learningTrackDto));
	}

	@PostMapping("add/existing/quiz")
	public ResponseEntity<XtremandResponse> addExistingQuiz() {
		try {
			return ResponseEntity.ok(lmsService.addExistingQuiz());
		} catch (ResponseUtilException e) {
			throw new ResponseUtilException(e.getMessage());
		}
	}

	/**** XNFR-342 ****/
	@GetMapping("/findAllUnPublishedTracks/{loggedInUserId}/{userListId}/{userListUserId}")
	public ResponseEntity<XtremandResponse> findAllUnPublishedTracks(@PathVariable Integer loggedInUserId,
			@PathVariable Integer userListId, @PathVariable Integer userListUserId, @Valid Pageable pageable,
			BindingResult result) {
		return new ResponseEntity<>(lmsService.findAllUnPublishedTracksOrPlayBooks(pageable, result, loggedInUserId,
				userListId, userListUserId, LearningTrackType.TRACK.name()), HttpStatus.OK);
	}

	/**** XNFR-342 ****/
	@PutMapping(value = "/shareSelectedTracks")
	public ResponseEntity<XtremandResponse> shareSelectedTracks(
			@RequestBody ShareContentRequestDTO shareContentRequestDTO) {
		return ResponseEntity
				.ok(lmsService.shareSelectedTracksOrPlayBooks(shareContentRequestDTO, LearningTrackType.TRACK.name()));
	}

	/**** XNFR-342 ****/
	@GetMapping("/findAllUnPublishedPlayBooks/{loggedInUserId}/{userListId}/{userListUserId}")
	public ResponseEntity<XtremandResponse> findAllUnPublishedPlayBooks(@PathVariable Integer loggedInUserId,
			@PathVariable Integer userListId, @PathVariable Integer userListUserId, @Valid Pageable pageable,
			BindingResult result) {
		return new ResponseEntity<>(lmsService.findAllUnPublishedTracksOrPlayBooks(pageable, result, loggedInUserId,
				userListId, userListUserId, LearningTrackType.PLAYBOOK.name()), HttpStatus.OK);
	}

	/**** XNFR-342 ****/
	@PutMapping(value = "/shareSelectedPlayBooks")
	public ResponseEntity<XtremandResponse> shareSelectedPlayBooks(
			@RequestBody ShareContentRequestDTO shareContentRequestDTO) {
		return ResponseEntity.ok(
				lmsService.shareSelectedTracksOrPlayBooks(shareContentRequestDTO, LearningTrackType.PLAYBOOK.name()));
	}

	/**** XNFR-342 ****/
	@GetMapping(value = "findPublishedPartnerIds/{userListId}/{id}")
	public ResponseEntity<XtremandResponse> findPublishedPartnerIds(@PathVariable Integer userListId,
			@PathVariable Integer id) {
		return ResponseEntity.ok(lmsService.findPublishedPartnerIdsByUserListIdAndId(userListId, id));
	}

	/** XNFR-750 ***/
	@GetMapping("publish/userListId/{userListId}/partnerUserId/{partnerUserId}/id/{trackOrPlayBookId}/loggedInUserId/{loggedInUserId}")
	public ResponseEntity<XtremandResponse> publishTrackOrPlayBookToPartnerCompany(@PathVariable Integer userListId,
			@PathVariable Integer partnerUserId, @PathVariable Integer trackOrPlayBookId,
			@PathVariable Integer loggedInUserId) {
		return new ResponseEntity<>(lmsService.publishTrackOrPlayBookToPartnerCompany(userListId, partnerUserId,
				trackOrPlayBookId, loggedInUserId), HttpStatus.OK);
	}

	@GetMapping("addPartnerGroup/userListId/{userListId}/partnershipId/{partnershipId}/id/{trackOrPlayBookId}/loggedInUserId/{loggedInUserId}")
	public ResponseEntity<XtremandResponse> addPartnerGroup(@PathVariable Integer userListId,
			@PathVariable Integer partnershipId, @PathVariable Integer trackOrPlayBookId,
			@PathVariable Integer loggedInUserId) {
		return new ResponseEntity<>(
				lmsService.addPartnerGroup(userListId, partnershipId, trackOrPlayBookId, loggedInUserId),
				HttpStatus.OK);
	}
	
	//XNFR-1032
	@GetMapping("content/counts/{loggedInUserId}/{vendorCompanyProfileName}")
	public ResponseEntity<XtremandResponse> getContentCounts(@PathVariable Integer loggedInUserId, @PathVariable String vendorCompanyProfileName) {
		return ResponseEntity.ok(lmsService.getContentCounts(loggedInUserId,vendorCompanyProfileName));
	}
	
	@GetMapping("manage/content/counts/{loggedInUserId}/{moduleContentType}")
	public ResponseEntity<XtremandResponse> getManageContentCounts(@PathVariable Integer loggedInUserId, @PathVariable String moduleContentType) {
		return ResponseEntity.ok(lmsService.getManageContentCounts(loggedInUserId, moduleContentType));
	}
	
	@GetMapping("manage/shared/content/counts/{loggedInUserId}/{moduleContentType}/{vendorCompanyProfileName}")
	public ResponseEntity<XtremandResponse> getManageSharedContentCounts(@PathVariable Integer loggedInUserId, @PathVariable String moduleContentType, @PathVariable String vendorCompanyProfileName) {
		return ResponseEntity.ok(lmsService.getManageSharedContentCounts(loggedInUserId, moduleContentType, vendorCompanyProfileName));
	} 
	  
	@GetMapping("checkAWSCredentials")
	public ResponseEntity<XtremandResponse> checkAWSCredentials() {
		return ResponseEntity.ok(lmsService.checkAWSCredentials());
	}
		
}
