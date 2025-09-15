package com.xtremand.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.xtremand.campaign.exception.XamplifyDataAccessException;
import com.xtremand.comments.dto.CommentRequestDTO;
import com.xtremand.comments.service.CommentService;
import com.xtremand.dam.dto.ApprovalStatusHistoryDTO;
import com.xtremand.formbeans.XtremandResponse;
import com.xtremand.mail.service.AsyncComponent;
import com.xtremand.util.BadRequestException;
import com.xtremand.util.XamplifyUtils;

@RestController
@RequestMapping(value = "/comments")
public class CommentController {

	@Autowired
	private CommentService commentService;

	@Autowired
	private AsyncComponent asyncComponent;

	@PostMapping
	public ResponseEntity<XtremandResponse> save(@RequestBody CommentRequestDTO commentRequestDTO) {
		return ResponseEntity.ok(commentService.save(commentRequestDTO));
	}

	@GetMapping(value = "/moduleName/{moduleName}/{id}")
	public ResponseEntity<XtremandResponse> findCommentsByModuleNameAndId(@PathVariable String moduleName,
			@PathVariable Integer id) {
		return ResponseEntity.ok(commentService.findCommentsByModuleNameAndId(id, moduleName));
	}

	/** XNFR-824 start **/
	@GetMapping(value = "/loadUserDetailsWithApprovalStatus/{entityId}/{moduleType}")
	public ResponseEntity<XtremandResponse> loadUserDetailsWithDamApprovalStatus(@PathVariable Integer entityId,
			@PathVariable String moduleType) {
		return ResponseEntity.ok(commentService.loadUserDetailsWithApprovalStatus(entityId, moduleType));
	}

	@GetMapping(value = "/loadCommentsAndTimelineHistory/{entityId}/{moduleType}")
	public ResponseEntity<XtremandResponse> loadDamStatusHistoyTimeline(@PathVariable Integer entityId,
			@PathVariable String moduleType) {
		return ResponseEntity.ok(commentService.loadCommentsAndTimelineHistory(entityId, moduleType));
	}

	@PostMapping(value = "/updateApprovalStatusAndSaveComment")
	public ResponseEntity<XtremandResponse> updateApprovalStatusAndSaveComment(
			@RequestBody ApprovalStatusHistoryDTO approvalStatusHistoryDTO) {

		XtremandResponse response = new XtremandResponse();
		boolean constraint = false;
		try {
			response = commentService.updateApprovalStatusAndSaveComment(approvalStatusHistoryDTO);
			return ResponseEntity.ok(response);
		} catch (BadRequestException e) {
			constraint = true;
			throw new BadRequestException(e.getMessage());
		} catch (Exception e) {
			constraint = true;
			throw new XamplifyDataAccessException(e);
		} finally {
			if (!constraint && response.getStatusCode() == 200 && approvalStatusHistoryDTO != null
					&& approvalStatusHistoryDTO.isStatusUpdated()) {
				asyncComponent.sendContentApprovalStatusEmailNotification(approvalStatusHistoryDTO);
			}

			if (!constraint && response.getStatusCode() == 200 && approvalStatusHistoryDTO != null
					&& XamplifyUtils.isNotEmptyList(approvalStatusHistoryDTO.getWhiteLabeledParentDamIds())) {
				asyncComponent.handleWhiteLabeledAssetsAfterReApproval(
						approvalStatusHistoryDTO.getWhiteLabeledParentDamIds(), approvalStatusHistoryDTO.getCompanyId(),
						approvalStatusHistoryDTO.getLoggedInUserId());
			}
		}
	}
	/** XNFR-824 end **/

}
