package com.xtremand.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.xtremand.common.bom.Pagination;
import com.xtremand.exception.DuplicateEntryException;
import com.xtremand.formbeans.XtremandResponse;
import com.xtremand.unsubscribe.dto.UnsubscribePageDetailsDTO;
import com.xtremand.unsubscribe.dto.UnsubscribeReasonDTO;
import com.xtremand.unsubscribe.exception.UnsubscribeDataAccessException;
import com.xtremand.unsubscribe.service.UnsubscribeService;

@RestController
@RequestMapping(value = "/unsubscribe/")
public class UnsubscribeController {

	private static final String UNSUBSCRIBED_REASON_UNIQUE_INDEX = "xt_unsubscribe_reasons_reason_unique_key";

	private static final String DUPLICATENAME = "Reason Already Exists";

	private static final String CUSTOM_REASON_UNIQUE_INDEX = "xt_unsubscribe_reasons_custom_reason_unique_key";

	private static final String CUSTOM_REASON_UNIQUE_INDEX_MESSAGE = "Custom Reason option is not available";

	@Autowired
	private UnsubscribeService unsubscribeService;

	@PostMapping(value = "save")
	public ResponseEntity<XtremandResponse> save(@RequestBody UnsubscribeReasonDTO unsubscribeReasonDTO) {
		try {
			return ResponseEntity.ok(unsubscribeService.save(unsubscribeReasonDTO));
		} catch (DataIntegrityViolationException e) {
			if (e.getMessage().indexOf(UNSUBSCRIBED_REASON_UNIQUE_INDEX) > -1) {
				throw new DuplicateEntryException(DUPLICATENAME);
			} else if (e.getMessage().indexOf(CUSTOM_REASON_UNIQUE_INDEX) > -1) {
				throw new DuplicateEntryException(CUSTOM_REASON_UNIQUE_INDEX_MESSAGE);
			} else {
				throw new UnsubscribeDataAccessException(e);
			}
		} catch (DuplicateEntryException e) {
			throw new DuplicateEntryException(e.getMessage());
		} catch (UnsubscribeDataAccessException u) {
			throw new UnsubscribeDataAccessException(u);
		} catch (Exception e) {
			throw new UnsubscribeDataAccessException(e);
		}
	}
	
	@PostMapping(value = "update")
	public ResponseEntity<XtremandResponse> update(@RequestBody UnsubscribeReasonDTO unsubscribeReasonDTO) {
		try {
			return ResponseEntity.ok(unsubscribeService.update(unsubscribeReasonDTO));
		} catch (DataIntegrityViolationException e) {
			if (e.getMessage().indexOf(UNSUBSCRIBED_REASON_UNIQUE_INDEX) > -1) {
				throw new DuplicateEntryException(DUPLICATENAME);
			} else if (e.getMessage().indexOf(CUSTOM_REASON_UNIQUE_INDEX) > -1) {
				throw new DuplicateEntryException(CUSTOM_REASON_UNIQUE_INDEX_MESSAGE);
			} else {
				throw new UnsubscribeDataAccessException(e);
			}
		} catch (DuplicateEntryException e) {
			throw new DuplicateEntryException(e.getMessage());
		} catch (UnsubscribeDataAccessException u) {
			throw new UnsubscribeDataAccessException(u);
		} catch (Exception e) {
			throw new UnsubscribeDataAccessException(e);
		}
	}
	
	@PostMapping(value="findAll")
	public ResponseEntity<XtremandResponse> findAll(@RequestBody Pagination pagination) {
		try {
			return ResponseEntity.ok(unsubscribeService.findAll(pagination));
		} catch (UnsubscribeDataAccessException u) {
			throw new UnsubscribeDataAccessException(u);
		} catch (Exception ex) {
			throw new UnsubscribeDataAccessException(ex);
		}

	}
	
	@GetMapping(value = "/findById/{id}")
	public ResponseEntity<XtremandResponse> findById(@PathVariable Integer id) {
		try {
			return ResponseEntity.ok(unsubscribeService.findById(id));
		} catch (UnsubscribeDataAccessException u) {
			throw new UnsubscribeDataAccessException(u);
		} catch (Exception ex) {
			throw new UnsubscribeDataAccessException(ex);
		}

	}
	
	@GetMapping(value = "/delete/{id}")
	public ResponseEntity<XtremandResponse> delete(@PathVariable Integer id) {
		try {
			return ResponseEntity.ok(unsubscribeService.delete(id));
		} catch (UnsubscribeDataAccessException u) {
			throw new UnsubscribeDataAccessException(u);
		} catch (Exception ex) {
			throw new UnsubscribeDataAccessException(ex);
		}
	}
	
	@GetMapping(value = "/findHeaderAndFooterText/{userId}")
	public ResponseEntity<XtremandResponse> findHeaderAndFooterText(@PathVariable Integer userId) {
		try {
			return ResponseEntity.ok(unsubscribeService.findUnsubscribePageDetailsByCompanyId(userId));
		} catch (UnsubscribeDataAccessException u) {
			throw new UnsubscribeDataAccessException(u);
		} catch (Exception ex) {
			throw new UnsubscribeDataAccessException(ex);
		}

	}
	
	@PostMapping(value = "/updateHeaderAndFooterText")
	public ResponseEntity<XtremandResponse> updateHeaderText(@RequestBody UnsubscribePageDetailsDTO unsubscribePageDetailsDTO) {
		try {
			return ResponseEntity.ok(unsubscribeService.updateHeaderAndFooterText(unsubscribePageDetailsDTO));
		} catch (UnsubscribeDataAccessException u) {
			throw new UnsubscribeDataAccessException(u);
		} catch (Exception ex) {
			throw new UnsubscribeDataAccessException(ex);
		}

	}
	
	@GetMapping(value = "/findUnsubscribePageContent/{userId}")
	public ResponseEntity<XtremandResponse> findUnsubscribePageContent(@PathVariable Integer userId) {
		try {
			return ResponseEntity.ok(unsubscribeService.findUnsubscribePageContent(userId));
		} catch (UnsubscribeDataAccessException u) {
			throw new UnsubscribeDataAccessException(u);
		} catch (Exception ex) {
			throw new UnsubscribeDataAccessException(ex);
		}

	}
	
	
	

}
