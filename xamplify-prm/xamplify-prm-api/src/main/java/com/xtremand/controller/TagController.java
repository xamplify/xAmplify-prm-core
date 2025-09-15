package com.xtremand.controller;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.xtremand.common.bom.Pagination;
import com.xtremand.exception.DuplicateEntryException;
import com.xtremand.formbeans.XtremandResponse;
import com.xtremand.tag.dto.TagDTO;
import com.xtremand.tag.exception.TagDataAccessException;
import com.xtremand.tag.service.TagService;
import com.xtremand.util.BadRequestException;

@RestController
@RequestMapping(value = "/tag/")
public class TagController {

	@Autowired
	private TagService tagService;

	private static final String TAG_NAME_UNIQUE_INDEX = "tag_name_unique_index";

	private static final String DUPLICATENAME = "Already Exists";

	@PostMapping(value = "save")
	public ResponseEntity<XtremandResponse> save(@Valid @RequestBody TagDTO tagDTO) {
		try {
			return ResponseEntity.ok(tagService.save(tagDTO));
		} catch (DataIntegrityViolationException e) {
			String message = e.getRootCause().toString();
			String tagName = message.substring(message.lastIndexOf('(')+1, message.lastIndexOf(','));
			if (e.getMessage().indexOf(TAG_NAME_UNIQUE_INDEX) > -1) {
				throw new DuplicateEntryException(tagName+" "+DUPLICATENAME);
			} else {
				throw new TagDataAccessException(e);
			}
		} catch (DuplicateEntryException de) {
			throw new DuplicateEntryException(de.getLocalizedMessage());
		} catch (BadRequestException be) {
			throw new BadRequestException(be.getMessage());
		} catch (Exception e) {
			throw new TagDataAccessException(e);
		}
	}

	@PostMapping(value = "update")
	public ResponseEntity<XtremandResponse> update(@RequestBody TagDTO tagDTO) {
		try {
			return ResponseEntity.ok(tagService.update(tagDTO));
		} catch (DataIntegrityViolationException e) {
			String message = e.getRootCause().toString();
			String tagName = message.substring(message.lastIndexOf('(')+1, message.lastIndexOf(','));
			if (e.getMessage().indexOf(TAG_NAME_UNIQUE_INDEX) > -1) {
				throw new DuplicateEntryException(tagName+" "+DUPLICATENAME);
			} else {
				throw new TagDataAccessException(e);
			}
		} catch (BadRequestException be) {
			throw new BadRequestException(be.getMessage());
		} catch (Exception e) {
			throw new TagDataAccessException(e);
		}
	}

	@PostMapping(value = "delete")
	public ResponseEntity<XtremandResponse> delete(@RequestBody TagDTO tagDTO) {
		try {
			return ResponseEntity.ok(tagService.delete(tagDTO));
		} catch (BadRequestException be) {
			throw new BadRequestException(be.getMessage());
		} catch (TagDataAccessException ae) {
			throw new TagDataAccessException(ae);
		} catch (Exception e) {
			throw new TagDataAccessException(e);
		}
	}

	@PostMapping(value = "getTagsByCompanyId")
	public ResponseEntity<XtremandResponse> getTagsByCompanyId(@RequestBody Pagination pagination) {
		try {
			return ResponseEntity.ok(tagService.getAllByCompanyId(pagination, null));
		} catch (BadRequestException be) {
			throw new BadRequestException(be.getMessage());
		} catch (TagDataAccessException ae) {
			throw new TagDataAccessException(ae);
		} catch (Exception e) {
			throw new TagDataAccessException(e);
		}
	}
	
	@PostMapping(value = "getTagsByCompanyId/tag-name-search")
	public ResponseEntity<XtremandResponse> getTagsByCompanyIdAndTagNameSearch(@RequestBody Pagination pagination) {
		try {
			return ResponseEntity.ok(tagService.getAllByCompanyId(pagination, "tagName"));
		} catch (BadRequestException be) {
			throw new BadRequestException(be.getMessage());
		} catch (TagDataAccessException ae) {
			throw new TagDataAccessException(ae);
		} catch (Exception e) {
			throw new TagDataAccessException(e);
		}
	}
}
