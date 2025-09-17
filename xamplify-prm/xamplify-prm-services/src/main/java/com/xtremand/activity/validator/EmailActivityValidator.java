package com.xtremand.activity.validator;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.web.multipart.MultipartFile;

import com.xtremand.activity.dto.EmailActivityRequestDTO;
import com.xtremand.util.XamplifyUtils;
import com.xtremand.util.dto.Pageable;
import com.xtremand.util.dto.XamplifyUtilValidator;
import com.xtremand.validator.PageableValidator;

@Component
public class EmailActivityValidator implements Validator {
	
	@Value("${pageable.sort.parameter}")
	private String sortParameter;
	
	@Value("${email.field.body}")
	private String bodyParameter;
	
	@Value("${email.field.body.missing}")
	private String bodyParameterMissing;
	
	@Value("${email.field.subject.missing}")
	private String subjectParameterMissing;
	
	@Value("${email.field.subject}")
	private String subjectParameter;
	
	@Value("${email.field.userId.missing}")
	private String userIdParameterMissing;
	
	@Value("${email.field.userId}")
	private String userIdParameter;
	
	@Value("${parameter.loggedInUserId}")
	private String loggedInUserIdParameter;
	
	@Value("${email.sort.column.invalid.message}")
	private String sortColumnInvalidMessage;
	
	@Value("${email.sort.parameter.invalid.message}")
	private String sortParameterInvalidMessage;
	
	@Value("${parameter.loggedInUserId.missing}")
	private String loggedInUserIdParameterMissing;
	
	@Value("${parameter.loggedInUserId.invalid}")
	private String invalidLoggedInUserIdParameter;
	
	@Value("${email.sort.column.invalid.message}")
	private String invalidSortColumn;
	
	@Value("${email.sort.parameter.invalid.message}")
	private String invalidSortParameter;
	
	@Value("${email.sort.column.string}")
	private String sortColumnStrings;
	
	@Value("${email.excluded.file.types}")
	private List<String> emailExcludedFileTypes;
	
	@Autowired
	private XamplifyUtilValidator xamplifyUtilValidator;
	
	@Autowired
	private PageableValidator pageableValidator;

	@Override
	public boolean supports(Class<?> clazz) {
		return false;
	}

	@Override
	public void validate(Object target, Errors errors) {
		EmailActivityRequestDTO emailRequestDTO = (EmailActivityRequestDTO) target;
		
		validateAllProperties(emailRequestDTO, errors);
	}
	
	private void validateAllProperties(EmailActivityRequestDTO emailRequestDTO, Errors errors) {
		
		/***LoggedInUserId***/
		Integer loggedInUserId = emailRequestDTO.getUserId();
		validateLoggedInUserId(errors, loggedInUserId);
		
		validateSubjectAndBodyAndContactId(emailRequestDTO, errors);
		
	}
	
	public void validateProperties(EmailActivityRequestDTO emailRequestDTO, List<MultipartFile> attachments, Errors errors) {
		
		validateAllProperties(emailRequestDTO, errors);
		
		validateAttachmentFile(attachments, errors);
	}

	public void validateSubjectAndBodyAndContactId(EmailActivityRequestDTO emailRequestDTO, Errors errors) {
		/***Subject validation***/
		String subject = emailRequestDTO.getSubject();
		validateSubject(subject, errors);
		
		/***Body validation***/
		String body = null;
		if (XamplifyUtils.isValidInteger(emailRequestDTO.getTemplateId())) {
			body = emailRequestDTO.getTemplateId() + "";
		} else {
			body = emailRequestDTO.getBody();
		}
		validateBody(body, errors);
		
		/***contact id validation***/
		Integer contactId = emailRequestDTO.getUserId();
		validateContactId(contactId, errors);
	}
	
	public void validateContactId(Integer contactId, Errors errors) {
		if (!XamplifyUtils.isValidInteger(contactId)) {
			xamplifyUtilValidator.addFieldError(errors, userIdParameter, userIdParameterMissing);
		}
	}
	
	public void validateBody(String body, Errors errors) {
		if (body == null || body.equals("") || body.isEmpty()) {
			xamplifyUtilValidator.addFieldError(errors, bodyParameter, bodyParameterMissing);
		}
	}
	
	public void validateSubject(String subject, Errors errors) {
		if (subject == null || subject.equals("")) {
			xamplifyUtilValidator.addFieldError(errors, subjectParameter, subjectParameterMissing);
		}
	}
	
	public void validateLoggedInUserId(Errors errors, Integer loggedInUserId) {
		if (loggedInUserId == null) {
			xamplifyUtilValidator.addFieldError(errors, loggedInUserIdParameter,
					loggedInUserIdParameterMissing);
		} else if (loggedInUserId <= 0) {
			xamplifyUtilValidator.addFieldError(errors, loggedInUserIdParameter, invalidLoggedInUserIdParameter);
		}
	}
	
	public void validatePagableParameters(Pageable pageable, Errors errors) {
		pageableValidator.validatePagableParameters(pageable, errors, sortParameter, sortColumnStrings,
				invalidSortColumn, invalidSortParameter);

	}
	
	public void validateDataToFetchAllEmailActivities(Pageable pageable, Integer loggedInUserId, Integer contactId,
			Errors errors) {
		validateContactId(contactId, errors);
		validateLoggedInUserId(errors, loggedInUserId);
		validatePagableParameters(pageable, errors);
	}
	
	public void validateDataToFetchAllActivities(Integer loggedInUserId, Integer contactId,
			Errors errors) {
		validateContactId(contactId, errors);
		validateLoggedInUserId(errors, loggedInUserId);
	}
	
	public void validateAttachmentFile(List<MultipartFile> attachments, Errors errors) {
		if (XamplifyUtils.isNotEmptyList(attachments)) {
			for (MultipartFile file : attachments) {
				String fileName = file.getOriginalFilename();
				Integer lastDotIndex = fileName.lastIndexOf(".");
				String fileType = lastDotIndex != -1 ? fileName.substring(lastDotIndex+1) : "";
				if (emailExcludedFileTypes.contains(fileType)) {
					xamplifyUtilValidator.addFieldError(errors, file.getName(), "Invalid file(" + fileType + ") type.");
				}
			}
		}
	}

}
