package com.xtremand.custom.link.validator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import com.custom.link.dto.CustomLinkRequestDTO;
import com.xtremand.custom.link.bom.CustomLink;
import com.xtremand.custom.link.bom.CustomLinkType;
import com.xtremand.custom.link.dao.CustomLinkDao;
import com.xtremand.dao.util.GenericDAO;
import com.xtremand.user.dao.UserDAO;
import com.xtremand.util.XamplifyUtils;
import com.xtremand.util.dto.XamplifyUtilValidator;
import com.xtremand.util.service.UtilService;
import com.xtremand.validator.PageableValidator;

@Component
public class CustomLinkValidator implements Validator {

	/**** Title ***/
	@Value("${partnerJourney.workflow.title}")
	private String titleParameter;

	@Value("${partnerJourney.workflow.title.missing}")
	private String titleNameMissingParameterErrorMessage;

	@Value("${partnerJourney.workflow.title.empty}")
	private String emptyTitleErrorMessage;

	@Value("${partnerJourney.workflow.title.invalid}")
	private String invalidTitleNameErrorMessage;

	@Value("${partnerJourney.workflow.title.duplicate}")
	private String duplicateTitleErrorMessage;

	/**** Link ***/
	@Value("${customLink.link}")
	private String linkParameter;

	@Value("${customLink.link.missing}")
	private String linkMissingParameterErrorMessage;

	@Value("${customLink.link.empty}")
	private String emptyLinkErrorMessage;

	@Value("${customLink.link.invalid}")
	private String invalidLinkErrorMessage;

	/**** Type ***/
	@Value("${customLink.type}")
	private String typeParameter;

	@Value("${customLink.type.missing}")
	private String typeMissingParameterErrorMessage;

	@Value("${customLink.type.empty}")
	private String emptyTypeErrorMessage;

	@Value("${customLink.type.invalid}")
	private String invalidTypeErrorMessage;

	/****** LoggedInUserId *****/
	@Value("${partnerJourney.workflow.loggedInUserId}")
	private String loggedInUserIdParameter;

	@Value("${partnerJourney.workflow.loggedInUserId.missing}")
	private String loggedInUserIdMissingParameterErrorMessage;

	@Value("${partnerJourney.workflow.loggedInUserId.empty}")
	private String emptyLoggedInUserIdErrorMessage;

	@Value("${partnerJourney.workflow.loggedInUserId.invalid}")
	private String invalidLoggedInUserIdErrorMessage;

	/**** Sort Options ******/

	@Value("${pageable.sort.parameter}")
	private String sortParameter;

	@Value("${pageable.sort.invalid}")
	private String invalidSortParameterMessage;

	@Value("${customLink.sort.columns}")
	private String sortColumnsString;

	@Value("${customLink.invalid.sort.column}")
	private String invalidSortColumnErrorMessage;

	@Autowired
	private XamplifyUtilValidator xamplifyUtilValidator;

	@Autowired
	private CustomLinkDao customLinkDao;

	@Autowired
	private UserDAO userDao;

	@Autowired
	private PageableValidator pageableValidator;

	@Autowired
	private UtilService utilService;

	@Autowired
	private GenericDAO genericDao;

	@Override
	public boolean supports(Class<?> clazz) {
		return CustomLinkRequestDTO.class.isAssignableFrom(clazz);
	}

	@Override
	public void validate(Object target, Errors errors) {
		CustomLinkRequestDTO customLinkRequestDTO = (CustomLinkRequestDTO) target;

		validateAllProperties(errors, customLinkRequestDTO);

	}

	private void validateAllProperties(Errors errors, CustomLinkRequestDTO customLinkRequestDTO) {
		validateType(errors, customLinkRequestDTO);

		validateLoggedInUserId(errors, customLinkRequestDTO);

		validateTitle(errors, customLinkRequestDTO);

		validateLink(errors, customLinkRequestDTO);
	}

	private void validateType(Errors errors, CustomLinkRequestDTO customLinkRequestDTO) {
		Integer id = customLinkRequestDTO.getId();
		if (id == null) {
			String type = customLinkRequestDTO.getType();
			String typeParameterKey = typeParameter;
			if (type == null) {
				addFieldError(errors, typeParameterKey, typeMissingParameterErrorMessage);
			} else if (!StringUtils.hasText(type)) {
				addFieldError(errors, typeParameterKey, emptyTypeErrorMessage);
			} else if (StringUtils.hasText(type)) {
				List<Enum<CustomLinkType>> enumValues = Arrays.asList(CustomLinkType.values());
				List<String> types = enumValues.stream().map(Enum::name).collect(Collectors.toList());
				String convertedType = type.trim().toUpperCase();
				boolean isTypeMatched = types.indexOf(convertedType) > -1;
				if (isTypeMatched) {
					CustomLinkType customLinkType = CustomLinkType.valueOf(convertedType);
					customLinkRequestDTO.setCustomLinkType(customLinkType);
				} else {
					String errorMessage = invalidTypeErrorMessage
							+ ".The value of type does not match any of the given options " + types;
					addFieldError(errors, typeParameterKey, errorMessage);
				}
			}
		} else if (XamplifyUtils.isValidInteger(id)) {
			CustomLink customLink = genericDao.get(CustomLink.class, id);
			customLinkRequestDTO.setCustomLinkType(customLink.getCustomLinkType());
		}

	}

	private void validateLink(Errors errors, CustomLinkRequestDTO customLinkRequestDTO) {
		String link = customLinkRequestDTO.getLink();
		String linkParamterKey = linkParameter;
		if (link == null) {
			addFieldError(errors, linkParamterKey, linkMissingParameterErrorMessage);
		} else if (!StringUtils.hasText(link)) {
			addFieldError(errors, linkParamterKey, emptyLinkErrorMessage);
		}
	}

	private void validateTitle(Errors errors, CustomLinkRequestDTO customLinkRequestDTO) {
		String title = customLinkRequestDTO.getTitle();
		if (title == null) {
			addFieldError(errors, titleParameter, titleNameMissingParameterErrorMessage);
		} else if (!StringUtils.hasText(title)) {
			addFieldError(errors, titleParameter, emptyTitleErrorMessage);
		} else if (StringUtils.hasText(title)) {
			checkDuplicateTitle(errors, customLinkRequestDTO, title);
		}
	}

	private void checkDuplicateTitle(Errors errors, CustomLinkRequestDTO customLinkRequestDTO, String title) {
		boolean isEdit = customLinkRequestDTO.getId() != null && customLinkRequestDTO.getId() > 0;
		List<String> existingTitles = new ArrayList<>();
		if (isEdit) {
			existingTitles.addAll(customLinkDao.findAllTitlesAndExcludeTitleById(customLinkRequestDTO));
		} else {
			existingTitles.addAll(customLinkDao.findAllTitles(customLinkRequestDTO));
		}
		String lowerCaseTitle = title.toLowerCase().trim();
		if (!existingTitles.isEmpty()) {
			boolean isDuplicateTitle = existingTitles.indexOf(lowerCaseTitle) > -1;
			if (isDuplicateTitle) {
				String errorMessage = title + " " + duplicateTitleErrorMessage;
				addFieldError(errors, titleParameter, errorMessage);
			}
		}
	}

	private void validateLoggedInUserId(Errors errors, CustomLinkRequestDTO customLinkRequestDTO) {
		Integer loggedInUserId = customLinkRequestDTO.getLoggedInUserId();
		if (loggedInUserId == null) {
			addFieldError(errors, loggedInUserIdParameter, loggedInUserIdMissingParameterErrorMessage);
		} else if (loggedInUserId <= 0) {
			addFieldError(errors, loggedInUserIdParameter, emptyLoggedInUserIdErrorMessage);
		} else if (loggedInUserId > 0) {
			Integer companyId = userDao.getCompanyIdByUserId(customLinkRequestDTO.getLoggedInUserId());
			customLinkRequestDTO.setCompanyId(companyId);

		}
	}

	private void addFieldError(Errors errors, String field, String errorMessage) {
		xamplifyUtilValidator.addFieldError(errors, field, errorMessage);

	}

	public void validatePagableParameters(Object target, Errors errors) {
		pageableValidator.validatePagableParameters(target, errors, sortParameter, sortColumnsString,
				invalidSortColumnErrorMessage, invalidSortParameterMessage);
	}

	public void validateId(Integer id, Integer loggedInUserId, boolean isDelete) {
		Integer companyId = userDao.getCompanyIdByUserId(loggedInUserId);
		List<Integer> ids = customLinkDao.findIdsByCompanyId(companyId);
		utilService.validateId(id, isDelete, ids);
		if (isDelete) {
			List<Integer> dashboardBannerIds = customLinkDao.findDashboardBannerIdsByCompanyId(companyId);
			if (XamplifyUtils.isNotEmptyList(dashboardBannerIds) && dashboardBannerIds.size() == 1
					&& dashboardBannerIds.indexOf(id) > -1) {
				throw new AccessDeniedException("It is mandatory to have at least one dashboard banner.");
			}

		}
	}

	public void validateCustomLinkPropertiesForUpdateRequest(CustomLinkRequestDTO customLinkRequestDto, Errors errors) {
		validateId(customLinkRequestDto.getId(), customLinkRequestDto.getLoggedInUserId(), false);
		validateAllProperties(errors, customLinkRequestDto);

	}

}
