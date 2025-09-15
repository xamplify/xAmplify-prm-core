package com.xtremand.flexi.fields.service.impl;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;

import com.xtremand.common.bom.CompanyProfile;
import com.xtremand.common.bom.Pagination;
import com.xtremand.contact.status.bom.ContactStatus;
import com.xtremand.dao.exception.ObjectNotFoundException;
import com.xtremand.dao.util.GenericDAO;
import com.xtremand.flexi.field.validator.FlexiFieldValidator;
import com.xtremand.flexi.fields.bom.FlexiField;
import com.xtremand.flexi.fields.dao.FlexiFieldDao;
import com.xtremand.flexi.fields.dto.FlexiFieldRequestDTO;
import com.xtremand.flexi.fields.dto.FlexiFieldResponseDTO;
import com.xtremand.flexi.fields.service.FlexiFieldService;
import com.xtremand.formbeans.XtremandResponse;
import com.xtremand.lead.dto.PipelineStageDto;
import com.xtremand.user.bom.User;
import com.xtremand.user.dao.UserDAO;
import com.xtremand.util.XamplifyUtils;
import com.xtremand.util.dto.Pageable;
import com.xtremand.util.dto.XamplifyUtilValidator;
import com.xtremand.util.service.UtilService;

@Service
@Transactional
public class FlexiFieldServiceImpl implements FlexiFieldService {

	@Autowired
	private FlexiFieldDao flexiFieldDao;

	@Autowired
	private FlexiFieldValidator flexiFieldValidator;

	@Autowired
	private XamplifyUtilValidator xamplifyUtilValidator;

	@Autowired
	private GenericDAO genericDao;

	@Autowired
	private UtilService utilService;

	@Autowired
	private UserDAO userDao;

	@Override
	public List<FlexiFieldResponseDTO> getAllFlexiFields(Integer loggedInUserId) {
		return flexiFieldDao.findAll(loggedInUserId);
	}

	@Override
	public XtremandResponse save(FlexiFieldRequestDTO flexiFieldRequestDTO, BindingResult result) {
		XtremandResponse response = new XtremandResponse();
		flexiFieldValidator.validate(flexiFieldRequestDTO, result);
		if (result.hasErrors()) {
			xamplifyUtilValidator.addErrorResponse(result, response);
		} else {
			FlexiField flexiField = new FlexiField();
			BeanUtils.copyProperties(flexiFieldRequestDTO, flexiField);
			CompanyProfile companyProfile = new CompanyProfile();
			companyProfile.setId(flexiFieldRequestDTO.getCompanyId());
			flexiField.setCompany(companyProfile);
			Integer loggedInUserId = flexiFieldRequestDTO.getLoggedInUserId();
			User createdBy = new User();
			createdBy.setUserId(loggedInUserId);
			flexiField.setCreatedBy(createdBy);
			flexiField.setUpdatedBy(createdBy);
			flexiField.setCreatedTime(new Date());
			flexiField.setUpdatedTime(flexiField.getCreatedTime());
			genericDao.save(flexiField);
			XamplifyUtils.addSuccessStatusWithMessage(response, "field created successfully");
		}
		return response;
	}

	@Override
	public FlexiFieldResponseDTO getById(Integer id, Integer loggedInUserId) {
		flexiFieldValidator.validateIdByLoggedInUserId(id, loggedInUserId, false);
		FlexiField customField = flexiFieldDao.getById(id);
		if (customField != null) {
			FlexiFieldResponseDTO customFieldResponseDTO = new FlexiFieldResponseDTO();
			BeanUtils.copyProperties(customField, customFieldResponseDTO);
			return customFieldResponseDTO;
		} else {
			throw new ObjectNotFoundException("CustomField not found for id " + id);
		}

	}

	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public XtremandResponse delete(Integer id, Integer loggedInUserId) {
		XtremandResponse response = new XtremandResponse();
		flexiFieldValidator.validateIdByLoggedInUserId(id, loggedInUserId, true);
		flexiFieldDao.delete(id);
		XamplifyUtils.addSuccessStatusWithMessage(response, "Field Deleted successfully");
		return response;
	}

	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public XtremandResponse update(FlexiFieldRequestDTO flexiFieldRequestDTO, BindingResult result) {
		XtremandResponse response = new XtremandResponse();
		try {
			flexiFieldValidator.validateAllPropertiesForUpdateRequest(flexiFieldRequestDTO, result);
		} catch (AccessDeniedException a) {
			throw new AccessDeniedException(a.getMessage());
		}
		if (result.hasErrors()) {
			xamplifyUtilValidator.addErrorResponse(result, response);
		} else {
			flexiFieldDao.update(flexiFieldRequestDTO);
			XamplifyUtils.addSuccessStatusWithMessage(response, "fieldName Updated Successfully");
		}
		return response;
	}

	@Override
	public XtremandResponse findPaginatedCustomFields(Pageable pageable, Integer loggedInUserId, BindingResult result) {
		XtremandResponse response = new XtremandResponse();
		flexiFieldValidator.validatePagableParameters(pageable, result);
		if (result.hasErrors()) {
			xamplifyUtilValidator.addErrorResponse(result, response);
		} else {
			Pagination pagination = utilService.setPageableParameters(pageable, loggedInUserId);
			Map<String, Object> map = flexiFieldDao.findPaginatedFlexiFields(pagination, pageable.getSearch());
			response.setData(map);
			XamplifyUtils.addSuccessStatus(response);
		}
		return response;
	}

	@Override
	public XtremandResponse findContactStatusStages(Integer loggedInUserId) {
		XtremandResponse response = new XtremandResponse();
		Integer companyId = userDao.getCompanyIdByUserId(loggedInUserId);
		List<PipelineStageDto> contactStatusStages = flexiFieldDao.findContactStatusStages(companyId);
		if (XamplifyUtils.isNotEmptyList(contactStatusStages)) {
			response.setData(contactStatusStages);
		} else {
			response.setData(flexiFieldDao.findContactStatusStages(1));
		}
		XamplifyUtils.addSuccessStatus(response);
		return response;
	}

	@Override
	public XtremandResponse saveOrUpdate(List<PipelineStageDto> pipelineStageDtos, Integer loggedInUserId) {
		XtremandResponse response = new XtremandResponse();

		Set<String> stageNames = new HashSet<>();
		for (PipelineStageDto dto : pipelineStageDtos) {
			String stageName = dto.getStageName();
			if (stageName != null && !stageNames.add(stageName.toLowerCase().replace(" ", ""))) {
				XamplifyUtils.addSuccessStatusWithMessage(response, "Duplicate stage names found : " + stageName);
				response.setStatusCode(401);
				return response;
			}
		}

		Integer companyId = userDao.getCompanyIdByUserId(loggedInUserId);
		List<PipelineStageDto> contactStatusStages = flexiFieldDao.findContactStatusStages(companyId);
		boolean hasExistingStages = XamplifyUtils.isNotEmptyList(contactStatusStages);

		for (PipelineStageDto pipelineStageDto : pipelineStageDtos) {
			if (hasExistingStages && XamplifyUtils.isValidInteger(pipelineStageDto.getId())) {
				ContactStatus contactStatus = genericDao.get(ContactStatus.class, pipelineStageDto.getId());
				if (contactStatus != null) {
					contactStatus.setStageName(pipelineStageDto.getStageName());
					contactStatus.setDefaultStage(pipelineStageDto.isDefaultStage());
					User user = new User();
					user.setUserId(loggedInUserId);
					contactStatus.setUpdatedBy(user);
					contactStatus.setUpdatedTime(new Date());
				}
			} else {
				saveContactStatusStages(loggedInUserId, companyId, pipelineStageDto);
			}
		}

		XamplifyUtils.addSuccessStatusWithMessage(response, "Contact status stages saved successfully");
		return response;
	}

	private void saveContactStatusStages(Integer loggedInUserId, Integer companyId, PipelineStageDto pipelineStageDto) {
		ContactStatus contactStatus = new ContactStatus();
		CompanyProfile companyProfile = new CompanyProfile();
		companyProfile.setId(companyId);
		contactStatus.setCompany(companyProfile);
		contactStatus.setStageName(pipelineStageDto.getStageName());
		contactStatus.setDefaultStage(pipelineStageDto.isDefaultStage());
		User user = new User();
		user.setUserId(loggedInUserId);
		contactStatus.setCreatedBy(user);
		contactStatus.setCreatedTime(new Date());
		contactStatus.setUpdatedBy(user);
		contactStatus.setUpdatedTime(new Date());
		genericDao.save(contactStatus);
	}

	@Override
	public XtremandResponse deleteContactStatusStage(Integer id, Integer loggedInUserId) {
		XtremandResponse response = new XtremandResponse();
		Integer companyId = userDao.getCompanyIdByUserId(loggedInUserId);
		boolean isStageInUse = flexiFieldDao.isValidContactStatusStageId(id);
		List<PipelineStageDto> stages = flexiFieldDao.findContactStatusStages(companyId);
		if (isStageInUse) {
			XamplifyUtils.addSuccessStatusWithMessage(response,
					"This contact status is currently in use and cannot be deleted.");
			response.setStatusCode(401);
		} else if (stages.size() == 1) {
			XamplifyUtils.addSuccessStatusWithMessage(response,
					"You must keep at least one contact status. This one cannot be deleted.");
			response.setStatusCode(401);
		} else {
			flexiFieldDao.deleteContactStatusStage(id);
			XamplifyUtils.addSuccessStatusWithMessage(response, "The contact status stage deleted successfully.");
		}
		return response;
	}

}
