package com.xtremand.controller;

import java.util.List;

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
import com.xtremand.dao.exception.ObjectNotFoundException;
import com.xtremand.exception.DuplicateEntryException;
import com.xtremand.flexi.fields.dto.FlexiFieldConstants;
import com.xtremand.flexi.fields.dto.FlexiFieldRequestDTO;
import com.xtremand.flexi.fields.dto.FlexiFieldResponseDTO;
import com.xtremand.flexi.fields.service.FlexiFieldService;
import com.xtremand.formbeans.XtremandResponse;
import com.xtremand.lead.dto.PipelineStageDto;
import com.xtremand.util.BadRequestException;
import com.xtremand.util.dto.Pageable;

@RestController
@RequestMapping(value = "/flexi-fields")
public class FlexiFieldController {

	private static final String UNIQUE_INDEX = FlexiFieldConstants.UNIQUE_INDEX;

	private static final String DUPLICATE_FIELD_NAME = FlexiFieldConstants.DUPLICATE_FIELD_NAME;

	@Autowired
	private FlexiFieldService flexiFieldService;

	@GetMapping("/by-user/{loggedInUserId}")
	public List<FlexiFieldResponseDTO> getAllCustomFields(@PathVariable Integer loggedInUserId) {
		return flexiFieldService.getAllFlexiFields(loggedInUserId);
	}

	@PostMapping
	public ResponseEntity<XtremandResponse> save(@Valid @RequestBody FlexiFieldRequestDTO customFieldRequestDTO,
			BindingResult result) {
		XtremandResponse response = new XtremandResponse();
		try {
			response = flexiFieldService.save(customFieldRequestDTO, result);
			return ResponseEntity.ok(response);
		} catch (DataIntegrityViolationException e) {
			if (e.getMessage().indexOf(UNIQUE_INDEX) > -1) {
				throw new DuplicateEntryException(DUPLICATE_FIELD_NAME);
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
			@RequestBody FlexiFieldRequestDTO customFieldRequestDTO, BindingResult result) {
		XtremandResponse response = new XtremandResponse();
		try {
			customFieldRequestDTO.setId(id);
			response = flexiFieldService.update(customFieldRequestDTO, result);
			return ResponseEntity.ok(response);
		} catch (DataIntegrityViolationException e) {
			if (e.getMessage().indexOf(UNIQUE_INDEX) > -1) {
				throw new DuplicateEntryException(DUPLICATE_FIELD_NAME);
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

	@GetMapping("id/{id}/loggedInUserId/{loggedInUserId}")
	public FlexiFieldResponseDTO getById(@PathVariable Integer id, @PathVariable Integer loggedInUserId) {
		try {
			return flexiFieldService.getById(id, loggedInUserId);
		} catch (ObjectNotFoundException e) {
			throw new ObjectNotFoundException(e.getMessage());
		} catch (AccessDeniedException e) {
			throw new AccessDeniedException(e.getMessage());
		} catch (Exception e) {
			throw new XamplifyDataAccessException(e);
		}
	}

	@GetMapping("/paginated/userId/{loggedInUserId}")
	public ResponseEntity<XtremandResponse> findPaginatedCustomFields(@PathVariable Integer loggedInUserId,
			@Valid Pageable pageable, BindingResult result) {
		return new ResponseEntity<>(flexiFieldService.findPaginatedCustomFields(pageable, loggedInUserId, result),
				HttpStatus.OK);
	}

	@DeleteMapping("id/{id}/loggedInUserId/{loggedInUserId}")
	public ResponseEntity<XtremandResponse> delete(@PathVariable Integer id, @PathVariable Integer loggedInUserId) {
		try {
			return ResponseEntity.ok(flexiFieldService.delete(id, loggedInUserId));
		} catch (XamplifyDataAccessException xe) {
			throw new XamplifyDataAccessException(xe.getMessage());
		} catch (AccessDeniedException e) {
			throw new AccessDeniedException(e.getMessage());
		} catch (Exception ex) {
			throw new XamplifyDataAccessException(ex.getMessage());
		}
	}

	@GetMapping("/contact-status-stages/userId/{loggedInUserId}")
	public ResponseEntity<XtremandResponse> findContactStatusStages(@PathVariable Integer loggedInUserId) {
		return new ResponseEntity<>(flexiFieldService.findContactStatusStages(loggedInUserId), HttpStatus.OK);
	}

	@PostMapping("/contact-status-stages/save-update/userId/{loggedInUserId}")
	public ResponseEntity<XtremandResponse> saveOrUpdate(@Valid @RequestBody List<PipelineStageDto> pipelineStageDto,
			@PathVariable Integer loggedInUserId) {
		try {
			return ResponseEntity.ok(flexiFieldService.saveOrUpdate(pipelineStageDto, loggedInUserId));
		} catch (DataIntegrityViolationException e) {
			if (e.getMessage().indexOf(UNIQUE_INDEX) > -1) {
				throw new DuplicateEntryException(DUPLICATE_FIELD_NAME.replace("field", "stage"));
			} else {
				throw new XamplifyDataAccessException(e);
			}
		} catch (BadRequestException e) {
			throw new BadRequestException(e.getMessage());
		} catch (Exception e) {
			throw new XamplifyDataAccessException(e);
		}
	}

	@DeleteMapping("/contact-status-stages/id/{id}/userId/{loggedInUserId}")
	public ResponseEntity<XtremandResponse> deleteContactStatusStage(@PathVariable Integer id,
			@PathVariable Integer loggedInUserId) {
		try {
			return ResponseEntity.ok(flexiFieldService.deleteContactStatusStage(id, loggedInUserId));
		} catch (XamplifyDataAccessException xe) {
			throw new XamplifyDataAccessException(xe.getMessage());
		} catch (AccessDeniedException e) {
			throw new AccessDeniedException(e.getMessage());
		} catch (Exception ex) {
			throw new XamplifyDataAccessException(ex.getMessage());
		}
	}

}
