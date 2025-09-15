package com.xtremand.controller;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.xtremand.common.bom.Pagination;
import com.xtremand.formbeans.XtremandResponse;
import com.xtremand.integration.bom.Integration.IntegrationType;
import com.xtremand.lead.bom.PipelineType;
import com.xtremand.lead.dto.PipelineDto;
import com.xtremand.lead.dto.PipelineRequestDTO;
import com.xtremand.pipeline.service.PipelineService;

@RestController
public class PipelineController {
	private static final Logger logger = LoggerFactory.getLogger(PipelineController.class);

	@Autowired
	PipelineService pipelineService;

	@GetMapping("/pipeline/{type}/{loggedInUserId}/{companyId}/list")
	public ResponseEntity<?> getPipeLines(@PathVariable Integer loggedInUserId, @PathVariable Integer companyId,
			@PathVariable PipelineType type) {
		return new ResponseEntity(pipelineService.getPipeLines(loggedInUserId, companyId, type), HttpStatus.OK);
	}

	@GetMapping("/pipeline/{type}/{loggedInUserId}/list")
	public ResponseEntity<?> getPipeLinesForVendor(@PathVariable Integer loggedInUserId,
			@PathVariable PipelineType type) {
		return new ResponseEntity(pipelineService.getPipeLines(loggedInUserId, null, type), HttpStatus.OK);
	}

	@PostMapping("/v/pipeline/list")
	public ResponseEntity<?> getPipeLinesForVendor(@RequestBody Pagination pagination) {
		Map<String, Object> resultMap = pipelineService.getPipeLinesForVendor(pagination);
		return ResponseEntity.status(HttpStatus.OK).body(resultMap);
	}

	@PostMapping("/pipeline")
	public ResponseEntity<?> savePipeline(@RequestBody PipelineDto pipelineDto) {
		return new ResponseEntity(pipelineService.savePipeline(pipelineDto), HttpStatus.OK);
	}

	@PostMapping("/pipeline/edit")
	public ResponseEntity<?> updatePipeline(@RequestBody PipelineDto pipelineDto) {
		return new ResponseEntity(pipelineService.updatePipeline(pipelineDto), HttpStatus.OK);
	}

	@PostMapping("/pipeline/delete")
	public ResponseEntity<?> deletePipeline(@RequestBody PipelineDto pipelineDto) {
		return new ResponseEntity(pipelineService.deletePipeline(pipelineDto), HttpStatus.OK);
	}

	@GetMapping("/pipeline/{pipelineId}/{loggedInUserId}")
	public ResponseEntity<?> getPipeLine(@PathVariable Integer loggedInUserId, @PathVariable Integer pipelineId) {
		return new ResponseEntity(pipelineService.getPipeLine(loggedInUserId, pipelineId), HttpStatus.OK);
	}

	/****
	 * Added By Sravan To Fix Performance Issue While Creating Campaign
	 **********/
	@GetMapping("/pipeline/campaign/{loggedInUserId}/list")
	public ResponseEntity<XtremandResponse> findLeadAndDealPipeLinesToCreateCampaign(
			@PathVariable Integer loggedInUserId) {
		return ResponseEntity.ok(pipelineService.findLeadAndDealPipeLinesToCreateCampaign(loggedInUserId));
	}

	@SuppressWarnings("unchecked")
	@GetMapping("/pipeline/{type}/{integrationType}/{companyId}/{loggedInUserId}/{halopsaTicketTypeId}")
	public ResponseEntity<?> getPipelinesByIntegrationType(@PathVariable Integer loggedInUserId,
			@PathVariable Integer companyId, @PathVariable PipelineType type,
			@PathVariable IntegrationType integrationType, @PathVariable Long halopsaTicketTypeId) {
		return new ResponseEntity(pipelineService.getPipelinesByIntegrationType(loggedInUserId, companyId, type,
				integrationType, halopsaTicketTypeId), HttpStatus.OK);
	}

	@SuppressWarnings("unchecked")
	@GetMapping("/pipeline/{type}/{integrationType}/{loggedInUserId}")
	public ResponseEntity<?> getPipelinesByIntegrationType(@PathVariable Integer loggedInUserId,
			@PathVariable PipelineType type, @PathVariable IntegrationType integrationType) {
		return new ResponseEntity(
				pipelineService.getPipelinesByIntegrationType(loggedInUserId, type, integrationType, 0L),
				HttpStatus.OK);
	}

	@GetMapping(value = "/pipeline/findLeadPipeLines")
	public ResponseEntity<XtremandResponse> findLeadPipeLines(PipelineRequestDTO pipelineRequestDTO) {
		return ResponseEntity.ok(pipelineService.findLeadPipeLines(pipelineRequestDTO));
	}

	@GetMapping(value = "/pipeline/findPipelineStages/{pipelineId}/{loggedInUserId}")
	public ResponseEntity<XtremandResponse> findPipelineStages(@PathVariable Integer pipelineId,
			@PathVariable Integer loggedInUserId) {
		return ResponseEntity.ok(pipelineService.findPipelineStages(pipelineId, loggedInUserId));
	}

	@GetMapping(value = "findLeadPipeLinesForPartner")
	public ResponseEntity<XtremandResponse> findLeadPipeLinesForPartner(PipelineRequestDTO pipelineRequestDTO) {
		return ResponseEntity.ok(pipelineService.findLeadPipelinesForPartner(pipelineRequestDTO));
	}

	@GetMapping(value = "/pipeline/findPipelinesForCRMSettings")
	public ResponseEntity<XtremandResponse> findPipelinesForCRMSettings(PipelineRequestDTO pipelineRequestDTO) {
		return ResponseEntity.ok(pipelineService.findPipelinesForCRMSettings(pipelineRequestDTO));
	}

}
