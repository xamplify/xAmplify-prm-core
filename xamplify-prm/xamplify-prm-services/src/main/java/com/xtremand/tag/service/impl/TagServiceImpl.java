package com.xtremand.tag.service.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.xtremand.common.bom.CompanyProfile;
import com.xtremand.common.bom.Pagination;
import com.xtremand.exception.DuplicateEntryException;
import com.xtremand.formbeans.XtremandResponse;
import com.xtremand.tag.bom.Tag;
import com.xtremand.tag.dao.TagDao;
import com.xtremand.tag.dto.TagDTO;
import com.xtremand.tag.exception.TagDataAccessException;
import com.xtremand.tag.service.TagService;
import com.xtremand.user.bom.User;
import com.xtremand.user.dao.UserDAO;
import com.xtremand.util.BadRequestException;
import com.xtremand.util.XamplifyUtils;
import com.xtremand.util.service.UtilService;

@Service
@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
public class TagServiceImpl implements TagService {

	@Autowired
	private TagDao tagDao;

	@Autowired
	private UserDAO userDao;

	@Autowired
	private UtilService utilService;

	@Override
	public XtremandResponse save(TagDTO tagDTO) {
		XtremandResponse response = new XtremandResponse();
		String errorMessage = "";
		try {
			List<String> tagNames = tagDTO.getTagNames();
			if(XamplifyUtils.isNotEmptyList(tagNames)) {
				User createdBy = userDao.getUser(tagDTO.getCreatedBy());
				CompanyProfile companyProfile = createdBy.getCompanyProfile();
				validateTags(tagDTO, tagNames, companyProfile, errorMessage);
				for (String tagName : tagNames) {
					Tag tag = new Tag();
					tag.setTagName(tagName);
					tag.setCompanyProfile(companyProfile);
					tag.setCreatedBy(createdBy);
					tag.setUpdatedBy(createdBy);
					tagDao.save(tag);
				}
				response.setStatusCode(200);
				response.setMessage("Tag(s) Added Successfully");
				response.setAccess(true);
			}else {
				response.setStatusCode(400);
				response.setMessage("Tag names cannot be empty");
				response.setAccess(false);
			}
			return response;
		} catch (DuplicateEntryException de) {
			throw new DuplicateEntryException(de.getMessage());
		} catch (TagDataAccessException ae) {
			throw new TagDataAccessException(ae);
		} catch (Exception e) {
			throw new TagDataAccessException(e);
		}
	}

	private void validateTags(TagDTO tagDTO, List<String> tagNames, CompanyProfile company, String errorMessage) {
		Integer companyId;
		Set<String> names = new HashSet<>();
		Set<String> duplicateNames = new HashSet<>();
		List<String> duplicateTagNames = new ArrayList<>();
		if (tagDTO.getCreatedBy() == null || (tagDTO.getCreatedBy() != null && tagDTO.getCreatedBy() <= 0)) {
			errorMessage = "Created by value is not valid \n";
		}
		if (tagDTO.getTagNames().isEmpty()) {
			errorMessage = errorMessage + "Tag names can not be empty";
		}
		if (StringUtils.hasText(errorMessage)) {
			throw new BadRequestException(errorMessage);
		}
		for (String name : tagNames) {
			String lower = name;
			if (!names.add(lower.toLowerCase())) {
				duplicateNames.add(name);
			}
		}
		if (!duplicateNames.isEmpty()) {
			String message = String.join(",", duplicateNames);
			throw new DuplicateEntryException(message + " duplicates not allowed");
		}
		if (company != null) {
			companyId = company.getId();
		} else {
			companyId = null;
		}
		List<String> existingTagNames = tagDao.getTagNames(tagDTO.getCreatedBy(), companyId);
		Map<String, String> newTagNames = tagNames.stream()
				.collect(Collectors.toMap(c -> c = c.trim().toLowerCase(), c -> c));
		duplicateTagNames.addAll(existingTagNames);
		utilService.findCommonStrings(duplicateTagNames, newTagNames.keySet().stream().collect(Collectors.toList()));
		if (!duplicateTagNames.isEmpty()) {
			List<String> tagNameValues = new ArrayList<>();
			duplicateTagNames.stream().forEach(c -> tagNameValues.add(newTagNames.get(c)));
			String message = String.join(",", tagNameValues);
			throw new DuplicateEntryException(message + " Already Exists");
		}
	}

	@Override
	public XtremandResponse update(TagDTO tagDTO) {
		XtremandResponse response = new XtremandResponse();
		String errorMessage = "";
		if (tagDTO.getId() == null || (tagDTO.getId() != null && tagDTO.getId() <= 0)) {
			errorMessage = "Tag id can not be empty \n";
		}
		if (tagDTO.getUpdatedBy() == null || (tagDTO.getUpdatedBy() != null && tagDTO.getUpdatedBy() <= 0)) {
			errorMessage = errorMessage + "Updated by value is not valid \n";
		}
		if (!StringUtils.hasText(tagDTO.getTagName())) {
			errorMessage = errorMessage + "Tag name can not be empty";
		}
		if (StringUtils.hasText(errorMessage)) {
			throw new BadRequestException(errorMessage);
		}
		try {
			Tag existingTag = tagDao.getById(tagDTO.getId());
			User updatedBy = userDao.getUser(tagDTO.getUpdatedBy());
			if (existingTag != null) {
				existingTag.setTagName(tagDTO.getTagName());
				existingTag.setUpdatedBy(updatedBy);
				tagDao.update(existingTag);
				response.setStatusCode(200);
				response.setMessage("Tag Updated Successfully");
				response.setAccess(true);
			}
			return response;
		} catch (TagDataAccessException ae) {
			throw new TagDataAccessException(ae);
		} catch (Exception e) {
			throw new TagDataAccessException(e);
		}
	}

	@Override
	public XtremandResponse delete(TagDTO tagDTO) {
		XtremandResponse response = new XtremandResponse();
		tagDao.delete(tagDTO.getTagIds());
		response.setStatusCode(200);
		response.setMessage("Deleted Successfully");
		response.setAccess(true);
		return response;
	}

	@Override
	public XtremandResponse getAllByCompanyId(Pagination pagination, String searchBy) {
		XtremandResponse response = new XtremandResponse();
		String errorMessage = "";
		if (pagination.getUserId() == null || (pagination.getUserId() != null && pagination.getUserId() <= 0)) {
			errorMessage = "User Id can not be empty";
		}
		if (StringUtils.hasText(errorMessage)) {
			throw new BadRequestException(errorMessage);
		}
		User user = userDao.getUser(pagination.getUserId());
		CompanyProfile companyProfile = user.getCompanyProfile();
		if (companyProfile != null) {
			Map<String, Object> result = tagDao.getAllByCompanyId(companyProfile.getId(), pagination, searchBy);
			response.setStatusCode(200);
			response.setData(result);
			response.setAccess(true);
		}
		return response;
	}

}
