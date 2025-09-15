package com.xtremand.controller;

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
import org.springframework.web.bind.annotation.RestController;

import com.xtremand.campaign.exception.XamplifyDataAccessException;
import com.xtremand.exception.DuplicateEntryException;
import com.xtremand.formbeans.XtremandResponse;
import com.xtremand.partner.journey.bom.TriggerComponentType;
import com.xtremand.partner.journey.dto.WorkflowRequestDTO;
import com.xtremand.util.BadRequestException;
import com.xtremand.util.dto.Pageable;
import com.xtremand.workflow.service.WorkflowService;

@RestController
@RequestMapping(value = "/workflow")
public class WorkflowController {

	@Autowired
	private WorkflowService workflowService;

	@PostMapping
	public ResponseEntity<XtremandResponse> save(@Valid @RequestBody WorkflowRequestDTO workflowrequestDto,
			BindingResult result) {
		XtremandResponse response = new XtremandResponse();
		try {
			response = workflowService.save(workflowrequestDto, result);
			return ResponseEntity.ok(response);
		} catch (DataIntegrityViolationException e) {
			if (e.getMessage().indexOf("work_flow_title_unique_index") > -1) {
				throw new DuplicateEntryException("Duplicate title");
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
			@RequestBody WorkflowRequestDTO workflowRequestDTO, BindingResult result) {
		XtremandResponse response = new XtremandResponse();
		try {
			workflowRequestDTO.setId(id);
			response = workflowService.update(workflowRequestDTO, result);
			return ResponseEntity.ok(response);
		} catch (DataIntegrityViolationException e) {
			if (e.getMessage().indexOf("work_flow_title_unique_index") > -1) {
				throw new DuplicateEntryException("Duplicate title");
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

	@GetMapping("/{loggedInUserId}")
	public ResponseEntity<XtremandResponse> findAll(@PathVariable Integer loggedInUserId, @Valid Pageable pageable,
			BindingResult result) {
		return new ResponseEntity<>(workflowService.findAll(pageable, result, loggedInUserId), HttpStatus.OK);
	}

	@GetMapping("findTriggerTitles/{loggedInUserId}")
	public ResponseEntity<XtremandResponse> findTriggerTitles(@PathVariable Integer loggedInUserId) {
		return ResponseEntity.ok(workflowService.findTriggerTitles(loggedInUserId));
	}

	@GetMapping("id/{id}/loggedInUserId/{loggedInUserId}")
	public ResponseEntity<XtremandResponse> getById(@PathVariable Integer id, @PathVariable Integer loggedInUserId) {
		return ResponseEntity.ok(workflowService.getById(id, loggedInUserId));
	}

	@GetMapping("trigger/{type}/{loggedInUserId}")
	public ResponseEntity<XtremandResponse> getTriggerComponentData(@PathVariable TriggerComponentType type,
			@PathVariable Integer loggedInUserId) {
		return ResponseEntity.ok(workflowService.getTriggerComponentData(type, loggedInUserId));
	}

	@GetMapping("/findDefaultTriggerOptions/{loggedInUserId}")
	public ResponseEntity<XtremandResponse> findDefaultTriggerOptions(@PathVariable Integer loggedInUserId) {
		return ResponseEntity.ok(workflowService.findDefaultTriggerOptions(loggedInUserId));
	}

	@DeleteMapping("id/{id}/loggedInUserId/{loggedInUserId}")
	public ResponseEntity<XtremandResponse> deleteWorkflow(@PathVariable Integer id,
			@PathVariable Integer loggedInUserId) {
		return ResponseEntity.ok(workflowService.deleteWorkflow(id, loggedInUserId));
	}

	//XNFR-921
	@GetMapping("/getWorkflowsByPlaybookId/{playbookId}")
	public ResponseEntity<XtremandResponse> getWorkflowsByPlaybookId(@PathVariable Integer playbookId) {
		return ResponseEntity.ok(workflowService.getWorkflowsByPlaybookId(playbookId));
	}

}
