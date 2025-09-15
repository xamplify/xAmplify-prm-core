package com.xtremand.flexi.field.validator;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import com.xtremand.flexi.fields.dao.FlexiFieldDao;
import com.xtremand.flexi.fields.dto.FlexiFieldRequestDTO;
import com.xtremand.user.dao.UserDAO;
import com.xtremand.util.XamplifyUtils;
import com.xtremand.util.dto.Pageable;
import com.xtremand.util.dto.XamplifyUtilValidator;
import com.xtremand.util.service.UtilService;
import com.xtremand.validator.PageableValidator;

@Component
public class FlexiFieldValidator implements Validator {

	/**** Field Name ***/
	@Value("${flexi.field.name}")
	private String fieldNameParameter;

	@Value("${flexi.field.name.missing}")
	private String fieldNameNameMissingParameterErrorMessage;

	@Value("${flexi.field.name.empty}")
	private String emptyFieldNameErrorMessage;

	@Value("${flexi.field.name.invalid}")
	private String invalidFieldNameNameErrorMessage;

	@Value("${flexi.field.name.duplicate}")
	private String duplicateFieldNameErrorMessage;

	@Value("${flexi.field.name.maxlength.limit}")
	private String maxLengthLimitReachedForFieldNameErrorMessage;

	@Value("${default.field.name.error}")
	private String defaultFieldNameErrorMessage;

	/****** LoggedInUserId *****/
	@Value("${parameter.loggedInUserId}")
	private String loggedInUserIdParameter;

	@Value("${parameter.loggedInUserId.missing}")
	private String loggedInUserIdMissingParameterErrorMessage;

	@Value("${parameter.loggedInUserId.empty}")
	private String emptyLoggedInUserIdErrorMessage;

	@Value("${parameter.loggedInUserId.invalid}")
	private String invalidLoggedInUserIdErrorMessage;

	@Value("${default.userlist.field.names}")
	private String defaultUserListFieldNames;

	/**** Sort Options ******/

	@Value("${pageable.sort.parameter}")
	private String sortParameter;

	@Value("${pageable.sort.invalid}")
	private String invalidSortParameterMessage;

	@Value("${flexi.field.sort.columns}")
	private String sortColumnsString;

	@Value("${flexi.field.invalid.sort.column}")
	private String invalidSortColumnErrorMessage;

	@Autowired
	private XamplifyUtilValidator xamplifyUtilValidator;

	@Autowired
	private PageableValidator pageableValidator;

	@Autowired
	private UserDAO userDao;

	@Autowired
	private FlexiFieldDao customFieldDao;

	@Autowired
	private UtilService utilService;

	@Override
	public boolean supports(Class<?> clazz) {
		return FlexiFieldRequestDTO.class.isAssignableFrom(clazz);
	}

	@Override
	public void validate(Object target, Errors errors) {
		FlexiFieldRequestDTO flexiFieldRequestDTO = (FlexiFieldRequestDTO) target;

		validateAllProperties(errors, flexiFieldRequestDTO);

	}

	private void validateAllProperties(Errors errors, FlexiFieldRequestDTO flexiFieldRequestDTO) {

		/******* LoggedInUserId ******/
		validateLoggedInUserId(errors, flexiFieldRequestDTO);

		/*** Field Name ****/
		validateFieldName(errors, flexiFieldRequestDTO);

	}

	private void validateFieldName(Errors errors, FlexiFieldRequestDTO flexiFieldRequestDTO) {
		String fieldName = XamplifyUtils.convertToLowerCaseAndExcludeSpace(flexiFieldRequestDTO.getFieldName());
		if (fieldName == null) {
			xamplifyUtilValidator.addFieldError(errors, fieldNameParameter, fieldNameNameMissingParameterErrorMessage);
		} else if (!StringUtils.hasText(fieldName)) {
			xamplifyUtilValidator.addFieldError(errors, fieldNameParameter, emptyFieldNameErrorMessage);
		} else if (StringUtils.hasText(fieldName)) {
			List<String> defaultFieldNames = XamplifyUtils.convertStringToArrayList(defaultUserListFieldNames);
			defaultFieldNames = XamplifyUtils.convertToLowerCaseStrings(defaultFieldNames);
			boolean isDefaultFieldNameMatched = XamplifyUtils.isMatched(defaultFieldNames, fieldName);
			if (isDefaultFieldNameMatched) {
				xamplifyUtilValidator.addFieldError(errors, fieldNameParameter, defaultFieldNameErrorMessage);
			} else if (fieldName.length() > 55) {
				xamplifyUtilValidator.addFieldError(errors, fieldNameParameter,
						maxLengthLimitReachedForFieldNameErrorMessage);
			} else {
				checkDuplicateFieldName(errors, flexiFieldRequestDTO, fieldName);
			}
		}
	}

	private void checkDuplicateFieldName(Errors errors, FlexiFieldRequestDTO flexiFieldRequestDTO, String fieldName) {
		Integer id = flexiFieldRequestDTO.getId();
		Integer companyId = flexiFieldRequestDTO.getCompanyId();
		boolean isEdit = XamplifyUtils.isValidInteger(id);
		List<String> existingFieldNames = new ArrayList<>();
		if (isEdit) {
			existingFieldNames
					.addAll(customFieldDao.findAllFlexiFieldNamesByCompanyIdAndExcludeFieldNameById(companyId, id));
		} else {
			existingFieldNames.addAll(customFieldDao.findAllFlexiFieldNamesByCompanyId(companyId));
		}

		if (!existingFieldNames.isEmpty()) {
			existingFieldNames = existingFieldNames.stream().map(name -> name.replace(" ", ""))
					.collect(Collectors.toList());
			boolean isDuplicateFieldName = XamplifyUtils.isMatched(existingFieldNames, fieldName);
			if (isDuplicateFieldName) {
				String errorMessage = fieldName + " " + duplicateFieldNameErrorMessage;
				xamplifyUtilValidator.addFieldError(errors, fieldNameParameter, errorMessage);
			}
		}
	}

	private void validateLoggedInUserId(Errors errors, FlexiFieldRequestDTO flexiFieldRequestDTO) {
		Integer loggedInUserId = flexiFieldRequestDTO.getLoggedInUserId();
		if (loggedInUserId == null) {
			xamplifyUtilValidator.addFieldError(errors, loggedInUserIdParameter,
					loggedInUserIdMissingParameterErrorMessage);
		} else if (loggedInUserId <= 0) {
			xamplifyUtilValidator.addFieldError(errors, loggedInUserIdParameter, emptyLoggedInUserIdErrorMessage);
		} else if (loggedInUserId > 0) {
			Integer companyId = userDao.getCompanyIdByUserId(loggedInUserId);
			flexiFieldRequestDTO.setCompanyId(companyId);

		}
	}

	public void validateIdByLoggedInUserId(Integer id, Integer loggedInUserId, boolean isDelete) {
		if (XamplifyUtils.isValidInteger(loggedInUserId)) {
			Integer companyId = userDao.getCompanyIdByUserId(loggedInUserId);
			findFieldIdsByCompanyIdAndValidate(id, isDelete, companyId);
		}

	}

	private void findFieldIdsByCompanyIdAndValidate(Integer id, boolean isDelete, Integer companyId) {
		List<Integer> ids = customFieldDao.findIdsByCompanyId(companyId);
		utilService.validateId(id, isDelete, ids);
	}

	public void validateAllPropertiesForUpdateRequest(FlexiFieldRequestDTO flexiFieldRequestDTO, BindingResult result) {
		validateLoggedInUserId(result, flexiFieldRequestDTO);

		findFieldIdsByCompanyIdAndValidate(flexiFieldRequestDTO.getId(), false, flexiFieldRequestDTO.getCompanyId());

		validateFieldName(result, flexiFieldRequestDTO);

	}

	public void validatePagableParameters(Pageable pageable, BindingResult result) {
		pageableValidator.validatePagableParameters(pageable, result, sortParameter, sortColumnsString,
				invalidSortColumnErrorMessage, invalidSortParameterMessage);

	}

}
