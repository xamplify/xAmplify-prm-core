package com.xtremand.form.service.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.hibernate.HibernateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.xtremand.aws.AmazonWebService;
import com.xtremand.category.bom.CategoryModule;
import com.xtremand.category.bom.CategoryModuleEnum;
import com.xtremand.category.dao.CategoryDao;
import com.xtremand.common.bom.CompanyProfile;
import com.xtremand.common.bom.Criteria;
import com.xtremand.common.bom.FindLevel;
import com.xtremand.common.bom.Pagination;
import com.xtremand.custom.css.bom.CustomModule;
import com.xtremand.custom.css.bom.ThemeProperties;
import com.xtremand.custom.css.dao.hibernate.HibernateCustomSkinDao;
import com.xtremand.custom.css.dto.CompanyThemeActiveDTO;
import com.xtremand.custom.css.service.impl.CustomSkinServiceImpl;
import com.xtremand.dao.util.GenericDAO;
import com.xtremand.exception.DuplicateEntryException;
import com.xtremand.form.bom.Form;
import com.xtremand.form.bom.FormLabel;
import com.xtremand.form.bom.FormLabelChoice;
import com.xtremand.form.bom.FormLabelType;
import com.xtremand.form.bom.FormQuizAnswer;
import com.xtremand.form.bom.FormSubTypeEnum;
import com.xtremand.form.bom.FormTeamGroupMapping;
import com.xtremand.form.bom.FormTypeEnum;
import com.xtremand.form.dao.FormDao;
import com.xtremand.form.dto.FormChoiceDTO;
import com.xtremand.form.dto.FormDTO;
import com.xtremand.form.dto.FormLabelDTO;
import com.xtremand.form.dto.FormLabelDTORow;
import com.xtremand.form.exception.FormDataAccessException;
import com.xtremand.form.service.FormService;
import com.xtremand.formbeans.XtremandResponse;
import com.xtremand.landing.page.analytics.bom.GeoLocationAnalyticsEnum;
import com.xtremand.lms.dao.LMSDAO;
import com.xtremand.team.member.group.bom.TeamMemberGroupUserMapping;
import com.xtremand.team.member.group.dao.TeamMemberGroupDao;
import com.xtremand.user.bom.User;
import com.xtremand.user.dao.UserDAO;
import com.xtremand.userlist.service.UserListService;
import com.xtremand.util.DateUtils;
import com.xtremand.util.GenerateRandomPassword;
import com.xtremand.util.XamplifyUtil;
import com.xtremand.util.XamplifyUtils;
import com.xtremand.util.dao.UtilDao;
import com.xtremand.util.service.UtilService;
import com.xtremand.vanity.url.dto.VanityUrlDetailsDTO;

@Service
@Transactional
public class FormServiceImpl implements FormService {

	private static final Logger logger = LoggerFactory.getLogger(FormServiceImpl.class);

	private static final String SUBMIT = "Submit";

	@Autowired
	private FormDao formDao;

	@Autowired
	private XamplifyUtil xamplifyUtil;

	@Autowired
	private UserDAO userDao;

	@Value("${dropdown.default}")
	private String defaultDropdownOption;

	@Autowired
	private CategoryDao categoryDao;

	@Autowired
	private UtilService utilService;

	@Autowired
	private GenericDAO genericDao;

	@Autowired
	private UserListService userListService;

	@Autowired
	private AmazonWebService amazonWebService;

	@Autowired
	private TeamMemberGroupDao teamMemberGroupDao;

	@Autowired
	private UtilDao utilDao;

	@Autowired
	private LMSDAO lmsDao;

	@Autowired
	private CustomSkinServiceImpl customSkinServiceImpl;

	@Autowired
	private HibernateCustomSkinDao hibernateCustomSkinDao;

	@Value("${web_url}")
	String webUrl;

	@Value("${separator}")
	String sep;

	@Value("${media_base_path}")
	String mediaBasePath;

	@Value("${amazon.form.folder}")
	String formFolder;

	@Value("${files.uploaded.successfully}")
	String filesUploadedSuccessfully;

	@Value("${amazon.base.url}")
	String amazonBaseUrl;

	@Value("${amazon.bucket.name}")
	String amazonBucketName;

	@Value("${amazon.env.folder}")
	String amazonEnvFolder;

	@Value("${images.folder}")
	String imageFolder;

	@Value("${server_url}")
	String serverUrl;

	@Value("${price.types}")
	String priceTypes;

	@Value("${google.captcha.site.key}")
	String googleCaptchaSitetKey;

	@Value("${show_shorten_url_log}")
	private boolean showShortenUrlLog;

	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public XtremandResponse save(FormDTO formDto, MultipartFile thumbnailImageFile) {
		XtremandResponse response = new XtremandResponse();
		response.setAccess(true);
		validateFormName(formDto, response);
		if (response.getStatusCode() != 100) {
			if (formDto.isQuizForm()) {
				validateQuizForm(formDto, response);
			}
			if (response.getStatusCode() != 100 && response.getStatusCode() != 400) {
				Form form = addOrUpdateForm(formDto);
				List<FormLabel> formLabels = new ArrayList<>();
				int i = 1;
				formDto.setHasEmailField(false);
				List<FormLabelDTORow> formLabelDTORows = formDto.getFormLabelDTORows();
				for (FormLabelDTORow formLabelDTORow : formLabelDTORows) {
					formDto.setColumnOrder(1);
					for (FormLabelDTO formLabelDTO : formLabelDTORow.getFormLabelDTOs()) {
						addFormLabelInfo(formDto, response, form, formLabels, i, formLabelDTO);
						if (response.getStatusCode() != 200 && response.getStatusCode() != 0) {
							return response;
						}
					}
					i++;
				}
				form.setFormLabels(formLabels);
				if (formDto.isQuizForm()) {
					setMaxScore(form);
				}
				updateFormTeamMemberGroupMapping(formDto.getSelectedTeamMemberIds(), form, false);
				formDao.save(form);

				handleDependentDropdowns(formDto, form);
				addFormCategory(formDto, form);

				setThumbnailImage(formDto, thumbnailImageFile, form);
				/**** XNFR-428 ***/
				formDto.setAlias(form.getAlias());
				formDto.setId(form.getId());
				response.setData(formDto);

			}
		}
		return response;
	}

	private void handleDependentDropdowns(FormDTO formDto, Form form) {
		for (FormLabelDTO formLabelDTO : formDto.getFormLabelDTOs()) {
			if ("select".equals(formLabelDTO.getLabelType())
					&& !StringUtils.isEmpty(formLabelDTO.getParentFormLabelId())) {
				Optional<FormLabel> formLabelObj = form.getFormLabels().stream()
						.filter(x -> x.getLabelId().equals(formLabelDTO.getLabelId())).findFirst();

				Optional<FormLabel> parentFormLabelObj = form.getFormLabels().stream()
						.filter(x -> x.getLabelId().equals(formLabelDTO.getParentFormLabelId())).findFirst();

				if (formLabelObj.isPresent() && parentFormLabelObj.isPresent()) {
					FormLabel formLabel = formLabelObj.get();
					FormLabel parentFormLabel = parentFormLabelObj.get();
					formLabel.setParentLabelId(parentFormLabel);
					for (FormChoiceDTO choiceDTO : formLabelDTO.getDropDownChoices()) {
						handleDependentChoices(formLabel, parentFormLabel, choiceDTO);
					}
				} else {
					if (formLabelObj.isPresent() && !(parentFormLabelObj.isPresent())) {
						FormLabel formLabel = formLabelObj.get();
						formLabel.setParentLabelId(null);
					}
				}
			}
		}
	}

	private void handleDependentChoices(FormLabel formLabel, FormLabel parentFormLabel, FormChoiceDTO choiceDTO) {
		if (choiceDTO != null && choiceDTO.getParentChoiceLabelIds() != null
				&& !choiceDTO.getParentChoiceLabelIds().isEmpty()) {
			Optional<FormLabelChoice> formLabelChoiceObj = formLabel.getFormLabelChoices().stream()
					.filter(x -> x.getLabelChoiceId().equals(choiceDTO.getLabelId())).findFirst();
			if (formLabelChoiceObj.isPresent()) {
				FormLabelChoice formLabelChoice = formLabelChoiceObj.get();
				for (String parentChoiceLabelId : choiceDTO.getParentChoiceLabelIds()) {
					handleParentChoices(parentFormLabel, formLabelChoice, parentChoiceLabelId);
				}
			}
		}
	}

	private void handleParentChoices(FormLabel parentFormLabel, FormLabelChoice formLabelChoice,
			String parentChoiceLabelId) {
		if (StringUtils.hasText(parentChoiceLabelId)) {
			Optional<FormLabelChoice> parentFormLabelChoiceObj = parentFormLabel.getFormLabelChoices().stream()
					.filter(x -> x.getLabelChoiceId().equals(parentChoiceLabelId)).findFirst();
			if (parentFormLabelChoiceObj.isPresent()) {
				FormLabelChoice parentFormLabelChoice = parentFormLabelChoiceObj.get();
				parentFormLabelChoice.getDependentChoices().add(formLabelChoice);
				formLabelChoice.getParentChoices().add(parentFormLabelChoice);
			}
		}
	}

	/******* XNFR-108 *********/
	public void updateFormTeamMemberGroupMapping(Set<Integer> selectedTeamMemberGroupUserMappingIdsSet, Form form,
			boolean isUpdate) {
		List<Integer> selectedTeamMemberGroupUserMappingIds = XamplifyUtils
				.convertSetToList(selectedTeamMemberGroupUserMappingIdsSet);
		List<Integer> assignedTeamMemberGroupUserMappingIds = new ArrayList<>();
		if (form != null) {
			if (isUpdate && form.getId() != null && form.getId() > 0) {
				assignedTeamMemberGroupUserMappingIds = XamplifyUtils.convertSetToList(
						teamMemberGroupDao.findSelectedTeamMemberGroupUserMappingIdsByFormId(form.getId()));
				Collections.sort(selectedTeamMemberGroupUserMappingIds);
				Collections.sort(assignedTeamMemberGroupUserMappingIds);
				if (!selectedTeamMemberGroupUserMappingIds.equals(assignedTeamMemberGroupUserMappingIds)) {
					/****
					 * Find unchecked teammemberGroupUserMappingIds and remove them from database
					 * from user input
					 *********/
					assignedTeamMemberGroupUserMappingIds.removeAll(selectedTeamMemberGroupUserMappingIds);
					if (!assignedTeamMemberGroupUserMappingIds.isEmpty()
							&& assignedTeamMemberGroupUserMappingIds.size() > 0) {
						formDao.findAndDeleteFormTeamMemberGroupMappingByFormIdAndTeamMemberGroupMappingIds(
								form.getId(), assignedTeamMemberGroupUserMappingIds);
					}
					/******
					 * Finally get all new selected teammemberGroupUserMappingIds to insert into
					 * table
					 *********/
					removeUnselectedTeamMemberIds(form.getId(), selectedTeamMemberGroupUserMappingIds);
				}

			}
			if (selectedTeamMemberGroupUserMappingIds != null && !selectedTeamMemberGroupUserMappingIds.isEmpty()) {
				Set<FormTeamGroupMapping> formTeamGroupMappings = new HashSet<FormTeamGroupMapping>();
				for (Integer teamMemberGroupUserMappingId : selectedTeamMemberGroupUserMappingIds) {
					boolean isFormTeamMemberGroupMappingExists = false;
					if (isUpdate && form.getId() != null && form.getId() > 0) {
						isFormTeamMemberGroupMappingExists = formDao.isFormTeamMemberGroupMappingExists(form.getId(),
								teamMemberGroupUserMappingId);
					}
					if (!isFormTeamMemberGroupMappingExists) {
						FormTeamGroupMapping formTeamGroupMapping = new FormTeamGroupMapping();
						formTeamGroupMapping.setForm(form);
						TeamMemberGroupUserMapping teamMemberGroupUserMapping = new TeamMemberGroupUserMapping();
						teamMemberGroupUserMapping.setId(teamMemberGroupUserMappingId);
						formTeamGroupMapping.setTeamMemberGroupUserMapping(teamMemberGroupUserMapping);
						formTeamGroupMapping.setCreatedTime(new Date());
						formTeamGroupMapping.setUpdatedTime(new Date());
						formTeamGroupMappings.add(formTeamGroupMapping);
					}
				}
				form.setFormTeamGroupMappings(formTeamGroupMappings);

			}

		}
	}

	private void removeUnselectedTeamMemberIds(Integer formId, List<Integer> teamMemberIds) {
		List<Integer> existingSelectedTeamMemberIds = XamplifyUtils
				.convertSetToList(teamMemberGroupDao.findSelectedTeamMemberGroupUserMappingIdsByFormId(formId));
		if (!teamMemberIds.equals(existingSelectedTeamMemberIds)) {
			teamMemberIds.removeAll(existingSelectedTeamMemberIds);
		}

	}

	private void setThumbnailImage(FormDTO formDto, MultipartFile thumbnailImageFile, Form form) {
		String thumbnailPath = uploadToAwsAndGetThumbnailPath(thumbnailImageFile, form, formDto);
		if (StringUtils.hasText(thumbnailPath)) {
			form.setThumbnailImage(thumbnailPath);
		}
	}

	private String uploadToAwsAndGetThumbnailPath(MultipartFile thumbnailFile, Form form, FormDTO formDto) {
		String thumbnailPath = "";
		if (thumbnailFile != null && !thumbnailFile.isEmpty()) {
			String orginalFileileName = thumbnailFile.getOriginalFilename();
			String extension = orginalFileileName.substring(orginalFileileName.lastIndexOf('.') + 1);
			String fileName = "thumbnail_" + System.currentTimeMillis() + "." + extension;
			String folderPath = mediaBasePath + formFolder + form.getCompanyProfile().getId() + sep + form.getId()
					+ sep;

			File imageDir = new File(folderPath);
			if (!imageDir.exists()) {
				imageDir.mkdirs();
			}
			String imageFilePath = folderPath + sep + fileName;
			File newImageFile = new File(imageFilePath);
			if (!newImageFile.exists()) {
				try {
					FileOutputStream fileOutputStream = new FileOutputStream(newImageFile);
					try {
						fileOutputStream.write(thumbnailFile.getBytes());
						fileOutputStream.flush();
						fileOutputStream.close();
					} finally {
						fileOutputStream.close();
					}
				} catch (Exception e) {
					logger.error("uploadToAwsAndGetThumbnailPath(" + thumbnailFile + "," + formDto.getCreatedBy() + ")",
							e);
				}
			}
			thumbnailPath = amazonWebService.uploadFormThumbnailImage(imageFilePath,
					form.getCompanyProfile().getId() + sep + form.getId() + sep + fileName);
		}
		return thumbnailPath;
	}

	private void validateQuizForm(FormDTO formDto, XtremandResponse response) {
		int requiredCount = 0;
		int requiredEmailFieldsCount = 0;
		String errorMessage = "";
		String requiredCountErrorMessage = "Atleast one quiz field should be mandatory.";
		String requiredEmailFieldsCountErrorMessage = "Atleast one email field should be mandatory for a quiz form.";
		for (FormLabelDTORow formLabelDTORow : formDto.getFormLabelDTORows()) {
			for (FormLabelDTO formLabelDTO : formLabelDTORow.getFormLabelDTOs()) {
				if (StringUtils.hasText(formLabelDTO.getLabelType())
						&& ("quiz_radio".equalsIgnoreCase(formLabelDTO.getLabelType())
								|| "quiz_checkbox".equalsIgnoreCase(formLabelDTO.getLabelType()))
						&& formLabelDTO.isRequired()) {
					requiredCount++;
				}
				if (StringUtils.hasText(formLabelDTO.getLabelType())
						&& ("email".equalsIgnoreCase(formLabelDTO.getLabelType()) && formLabelDTO.isRequired())) {
					requiredEmailFieldsCount++;
				}
				if (requiredCount > 0 && requiredEmailFieldsCount > 0) {
					break;
				}
			}
		}
		if (requiredCount <= 0) {
			errorMessage = errorMessage.concat(requiredCountErrorMessage);
		}
		if (requiredEmailFieldsCount <= 0) {
			if (StringUtils.hasText(errorMessage)) {
				errorMessage = errorMessage.concat("<br>").concat(requiredEmailFieldsCountErrorMessage);
			} else {
				errorMessage = errorMessage.concat(requiredEmailFieldsCountErrorMessage);
			}
		}
		if (StringUtils.hasText(errorMessage)) {
			response.setStatusCode(400);
			response.setMessage(errorMessage);
		}
	}

	private void setMaxScore(Form form) {
		int maxScore = 0;
		List<FormLabel> formLabels = form.getFormLabels();
		if (formLabels != null && !formLabels.isEmpty()) {
			for (FormLabel formLabel : formLabels) {
				if (formLabel != null && formLabel.getLabelType() != null) {
					FormLabelType labelType = formLabel.getLabelType();
					if (labelType != null && ("quiz_radio".equals(labelType.getLabelType())
							|| "quiz_checkbox".equals(labelType.getLabelType()))) {
						maxScore++;
					}
				}
			}
		}
		form.setMaxScore(maxScore);
	}

	private int getMaxScore(FormDTO formDto) {
		int maxScore = 0;
		if (formDto != null) {
			List<FormLabelDTORow> formLabelDTORowWise = formDto.getFormLabelDTORows();
			if (formLabelDTORowWise != null && !formLabelDTORowWise.isEmpty()) {
				for (FormLabelDTORow formLabelDTORow : formDto.getFormLabelDTORows()) {
					List<FormLabelDTO> formLabels = formLabelDTORow.getFormLabelDTOs();
					if (formLabels != null && !formLabels.isEmpty()) {
						for (FormLabelDTO formLabel : formLabels) {
							if (formLabel != null && formLabel.getLabelType() != null) {
								String labelType = formLabel.getLabelType();
								if (labelType != null
										&& ("quiz_radio".equals(labelType) || "quiz_checkbox".equals(labelType))) {
									maxScore++;
								}
							}
						}
					}
				}
			}
		}
		return maxScore;
	}

	private void addFormCategory(FormDTO formDto, Form form) {
		if (FormTypeEnum.XAMPLIFY_FORM.equals(formDto.getFormType())
				|| FormTypeEnum.MASTER_PARTNER_FORM.equals(formDto.getFormType())) {
			CategoryModule categoryModule = new CategoryModule();
			Integer categoryId = formDto.getCategoryId();
			Integer userId = formDto.getCreatedBy();
			Integer companyId = userDao.getCompanyIdByUserId(userId);
			if (categoryId == null || categoryId == 0) {
				categoryId = categoryDao.getDefaultCategoryIdByCompanyId(companyId);
			}
			categoryModule.setCategoryId(categoryId);
			categoryModule.setCategoryModuleEnum(CategoryModuleEnum.FORM);
			categoryModule.setCreatedTime(new Date());
			categoryModule.setCreatedUserId(userId);
			categoryModule.setCompanyId(companyId);
			categoryModule.setFormId(form.getId());
			categoryDao.saveCategoryModule(categoryModule);
		}
	}

	private Form addOrUpdateForm(FormDTO formDto) {
		Form form = null;
		if (formDto.getId() != null && !formDto.isSaveAs()) {
			form = setFormForUpdate(formDto);
		} else {
			form = setFormForSave(formDto);
		}
		String formName = formDto.getName();
		if (StringUtils.hasText(formName)) {
			if (formName.length() > 1000) {
				formName = formName.substring(0, 999);
			}
		} else {
			formName = "form_" + System.currentTimeMillis();
		}
		String updatedFormName = XamplifyUtils.removeExtraSpace(formName);
		form.setFormName(updatedFormName);

		setDescription(formDto, form);
		return form;
	}

	private Form setFormForUpdate(FormDTO formDto) {
		Form form;
		form = formDao.getById(formDto.getId());
		form.setId(formDto.getId());
		form.setUpdatedTime(new Date());
		form.setUpdatedUserId(formDto.getCreatedBy());
		form.setBackgroundColor(formDto.getBackgroundColor());
		form.setLabelColor(formDto.getLabelColor());
		if (StringUtils.hasText(formDto.getButtonValue())) {
			form.setButtonValue(formDto.getButtonValue());
		} else {
			form.setButtonValue(SUBMIT);
		}
		form.setButtonValueColor(formDto.getButtonValueColor());
		form.setFormSubmitMessage(formDto.getFormSubmitMessage());
		form.setButtonColor(formDto.getButtonColor());
		form.setBackgroundImage(formDto.getBackgroundImage());
		form.setCompanyLogo(formDto.getCompanyLogo());
		form.setShowCompanyLogo(formDto.isShowCompanyLogo());
		if (StringUtils.hasText(formDto.getFooter())) {
			if (formDto.getFooter().length() > 225) {
				form.setFooter((formDto.getFooter()).substring(0, 224));
			} else {
				form.setFooter(formDto.getFooter());
			}
		}
		form.setShowFooter(formDto.isShowFooter());
		form.setShowCaptcha(formDto.isShowCaptcha());
		form.setTitleColor(formDto.getTitleColor());
		form.setBorderColor(formDto.getBorderColor());
		form.setPageBackgroundColor(formDto.getPageBackgroundColor());
		form.setShowBackgroundImage(formDto.isShowBackgroundImage());
		form.setFormSubmissionUrl(formDto.getFormSubmissionUrl());
		form.setOpenLinkInNewTab(formDto.isOpenLinkInNewTab());
		form.setQuizForm(formDto.isQuizForm());
		form.setShowTitleHeader(formDto.isShowTitleHeader());
		form.setDescriptionColor(formDto.getDescriptionColor());

		return form;
	}

	private Form setFormForSave(FormDTO formDto) {
		Form form;
		form = new Form();
		form.setCreatedTime(new Date());
		form.setCreatedUserId(formDto.getCreatedBy());
		form.setDefaultForm(formDto.isSaveAsDefaultForm());
		form.setFormTypeEnum(formDto.getFormType());
		setCompanyProfile(formDto, form);
		GenerateRandomPassword password = new GenerateRandomPassword();
		form.setAlias(password.getPassword());
		form.setBackgroundColor(formDto.getBackgroundColor());
		form.setLabelColor(formDto.getLabelColor());
		if (StringUtils.hasText(formDto.getButtonValue())) {
			form.setButtonValue(formDto.getButtonValue());
		} else {
			form.setButtonValue(SUBMIT);
		}
		form.setButtonValueColor(formDto.getButtonValueColor());
		form.setButtonColor(formDto.getButtonColor());
		form.setFormSubmitMessage(formDto.getFormSubmitMessage());
		form.setBackgroundImage(formDto.getBackgroundImage());
		form.setCompanyLogo(formDto.getCompanyLogo());
		form.setShowCompanyLogo(formDto.isShowCompanyLogo());
		if (StringUtils.hasText(formDto.getFooter())) {
			if (formDto.getFooter().length() > 224) {
				form.setFooter((formDto.getFooter()).substring(0, 224));
			} else {
				form.setFooter(formDto.getFooter());
			}
		}
		form.setShowFooter(formDto.isShowFooter());
		form.setShowCaptcha(formDto.isShowCaptcha());
		form.setTitleColor(formDto.getTitleColor());
		form.setBorderColor(formDto.getBorderColor());
		form.setPageBackgroundColor(formDto.getPageBackgroundColor());
		form.setShowBackgroundImage(formDto.isShowBackgroundImage());
		form.setFormSubmissionUrl(formDto.getFormSubmissionUrl());
		form.setOpenLinkInNewTab(formDto.isOpenLinkInNewTab());
		form.setQuizForm(formDto.isQuizForm());
		form.setThumbnailImage(formDto.getThumbnailImage());
		form.setShowTitleHeader(formDto.isShowTitleHeader());
		form.setDescriptionColor(formDto.getDescriptionColor());

		if (formDto.isQuizForm()) {
			form.setFormSubTypeEnum(FormSubTypeEnum.QUIZ);
		} else {
			if (FormTypeEnum.XAMPLIFY_FORM.equals(formDto.getFormType()) && formDto.getFormSubType() == null) {
				form.setFormSubTypeEnum(FormSubTypeEnum.REGULAR);
			} else {
				form.setFormSubTypeEnum(formDto.getFormSubType());
			}
		}
		return form;
	}

	private void setDescription(FormDTO formDto, Form form) {
		if (StringUtils.hasText(formDto.getDescription())) {
			String trimmedDescription = formDto.getDescription().trim();
			if (trimmedDescription.length() > 1000) {
				form.setDescription(trimmedDescription.substring(0, 999));
			} else {
				form.setDescription(trimmedDescription);
			}
		} else {
			form.setDescription(null);
		}
	}

	private void setCompanyProfile(FormDTO formDto, Form form) {
		Integer companyId = userDao.getCompanyIdByUserId(formDto.getCreatedBy());
		CompanyProfile companyProfile = new CompanyProfile();
		companyProfile.setId(companyId);
		form.setCompanyProfile(companyProfile);
	}

	private void validateFormName(FormDTO formDto, XtremandResponse response) {
		Integer userId = formDto.getCreatedBy();
		String companyProfileName = formDto.getCompanyProfileName();
		List<String> formNames = formDao.listFormNamesByCompanyId(userId, companyProfileName);
		formNames = formNames != null && !formNames.isEmpty() ? formNames : Collections.emptyList();
		if (formDto.getId() != null && !formDto.isSaveAs()) {
			String existingFormName = formDao.getById(formDto.getId()).getFormName().trim();
			if (formNames.indexOf(formDto.getName().toLowerCase()) > -1
					&& !existingFormName.equalsIgnoreCase(formDto.getName().trim())) {
				response.setStatusCode(100);
				response.setMessage("duplicate form name");
			}
		} else {
			if (formNames.indexOf(formDto.getName().toLowerCase()) > -1) {
				response.setStatusCode(100);
				response.setMessage("duplicate form name");
			}
		}
	}

	private void addFormLabel(Form form, int i, FormLabelDTO formLabelDTO, FormLabel formLabel, int columnOrder,
			boolean isCreateMDFRequestForm) {
		if (formLabelDTO.getId() != null) {
			formLabel.setId(formLabelDTO.getId());
		}
		formLabel.setLabelId(formLabelDTO.getLabelId());
		formLabel.setHiddenLabelId(formLabelDTO.getHiddenLabelId());
		formLabel.setLabelName(formLabelDTO.getLabelName());
		formLabel.setPlaceHolder(formLabelDTO.getPlaceHolder());
		formLabel.setRequired(formLabelDTO.isRequired());
		formLabel.setDefaultColumn(formLabelDTO.isDefaultColumn());
		// XNFR-412
		formLabel.setDisplayName(formLabelDTO.getDisplayName());
		formLabel.setFormDefaultFieldType(formLabelDTO.getFormDefaultFieldType());
		formLabel.setNonInteractive(formLabelDTO.isNonInteractive());
		formLabel.setPrivate(formLabelDTO.isPrivate());
		formLabel.setLookUpExternalReference(formLabelDTO.getLookUpReferenceTo());
		formLabel.setActive(formLabelDTO.isActive());
		formLabel.setFormFieldType(formLabelDTO.getFormFieldType());
		formLabel.setFormLookUpDefaultFieldType(formLabelDTO.getFormLookUpDefaultFieldType());
		formLabel.setEmailNotificationEnabledOnUpdate(formLabelDTO.isEmailNotificationEnabledOnUpdate());
		if (formLabelDTO.isNonInteractive() && !StringUtils.isEmpty(formLabelDTO.getDefaultChoiceLabel())) {
			formLabel.setDefaultChoiceLabel(formLabelDTO.getDefaultChoiceLabel());
		}
		formLabel.setForm(form);
		if (formLabelDTO.getLabelLength() != null) {
			formLabel.setLabelLength(formLabelDTO.getLabelLength());
		}
		if (formLabelDTO.getLabelType().equalsIgnoreCase("price")) {
			formLabel.setPriceType(formLabelDTO.getPriceType());
		}
		if (FormSubTypeEnum.QUIZ.equals(form.getFormSubTypeEnum())) {
			if (formLabelDTO.getDescription() != null)
				formLabel.setDescription(formLabelDTO.getDescription().trim());
			else
				formLabel.setDescription(formLabelDTO.getDescription());
		}
		/*** Added By Sravan On 06/07/2024 To Fix Page Break Issue ***/
		if (isCreateMDFRequestForm) {
			formLabel.setOrder(columnOrder);
		} else {
			formLabel.setOrder(i);
		}
		formLabel.setColumnOrder(columnOrder);
		/******* XNFR-424 ENDS *********/
		// XNFR-621
		if ("select".equals(formLabelDTO.getLabelType()) && (formLabelDTO.getParentFormLabelId() == null
				|| StringUtils.isEmpty(formLabelDTO.getParentFormLabelId()))) {
			formLabel.setParentLabelId(null);
		}
		formLabel.setEmailNotificationEnabledOnUpdate(formLabelDTO.isEmailNotificationEnabledOnUpdate());
	}

	private void addDropDowns(FormLabelDTO formLabelDTO, FormLabel formLabel, List<FormLabelChoice> formLabelChoices,
			XtremandResponse response) {
		if (formLabelDTO.getDropDownChoices() != null && !formLabelDTO.getDropDownChoices().isEmpty()) {
			List<FormChoiceDTO> dropDownDtos = formLabelDTO.getDropDownChoices();
			addChoices(formLabel, formLabelChoices, dropDownDtos, response);
		}
	}

	private void addRadioButtons(FormLabelDTO formLabelDTO, FormLabel formLabel, List<FormLabelChoice> formLabelChoices,
			XtremandResponse response) {
		if (formLabelDTO.getRadioButtonChoices() != null && !formLabelDTO.getRadioButtonChoices().isEmpty()) {
			List<FormChoiceDTO> radioButtonDtos = formLabelDTO.getRadioButtonChoices();
			addChoices(formLabel, formLabelChoices, radioButtonDtos, response);
		}
	}

	private void addCheckBoxes(FormLabelDTO formLabelDTO, FormLabel formLabel, List<FormLabelChoice> formLabelChoices,
			XtremandResponse response) {
		if (formLabelDTO.getCheckBoxChoices() != null && !formLabelDTO.getCheckBoxChoices().isEmpty()) {
			List<FormChoiceDTO> checkBoxDtos = formLabelDTO.getCheckBoxChoices();
			addChoices(formLabel, formLabelChoices, checkBoxDtos, response);
		}
	}

	private void addChoices(FormLabel formLabel, List<FormLabelChoice> formLabelChoices,
			List<FormChoiceDTO> formChoiceDtos, XtremandResponse response) {
		int count = 0;
		for (FormChoiceDTO formChoiceDTO : formChoiceDtos) {
			FormLabelChoice choice = new FormLabelChoice();
			choice.setLabelChoiceName(formChoiceDTO.getName().trim());
			choice.setLabelChoiceId(formChoiceDTO.getLabelId().trim());
			if (formChoiceDTO.getHiddenLabelId() != null && formChoiceDTO.getHiddenLabelId().trim() != null && !formChoiceDTO.getHiddenLabelId().trim().isEmpty()) {
				choice.setLabelChoiceHiddenId(formChoiceDTO.getHiddenLabelId().trim());
			} else {
				choice.setLabelChoiceHiddenId("");
			}
			choice.setFormLabel(formLabel);
			choice.setDefaultColumn(formChoiceDTO.isDefaultColumn());
			if (formChoiceDTO.isCorrect()) {
				choice.setFormQuizAnswer(new FormQuizAnswer(choice));
				count++;
			}
			if (formLabel.isNonInteractive() && !StringUtils.isEmpty(formLabel.getDefaultChoiceLabel())
					&& formLabel.getDefaultChoiceLabel().equals(formChoiceDTO.getName())) {
				formLabel.setDefaultChoice(choice);
			}
			formLabelChoices.add(choice);
		}
		if (formLabel.getLabelType().getLabelType().equalsIgnoreCase("quiz_radio")
				|| formLabel.getLabelType().getLabelType().equalsIgnoreCase("quiz_checkbox")) {
			if (count == 0) {
				response.setStatusCode(400);
				response.setMessage("Default answer is mandatory");
			} else if (formLabel.getLabelType().getLabelType().equalsIgnoreCase("quiz_radio") && count > 1) {
				response.setStatusCode(400);
				response.setMessage("Can't have more than one default answer for label:" + formLabel.getLabelName());
			}
		} else {
			response.setStatusCode(200);
		}
	}

	private void addLabelType(FormLabelDTO formLabelDTO, FormLabel formLabel) {
		FormLabelType formLabelType = formDao.findByType(formLabelDTO.getLabelType());
		formLabel.setLabelType(formLabelType);
	}

	private void addOriginalCRMLabelType(FormLabelDTO formLabelDTO, FormLabel formLabel) {
		FormLabelType formLabelType = formDao.findByType(formLabelDTO.getOriginalCRMType());
		formLabel.setOriginalCRMType(formLabelType);
	}

	@Override
	public List<String> listFormNames(Integer userId, String companyProfileName) {
		return formDao.listFormNamesByCompanyId(userId, companyProfileName);
	}

	@Override
	public XtremandResponse list(Pagination pagination) {
		XtremandResponse response = new XtremandResponse();
		getList(pagination, response);
		return response;
	}

	private void getList(Pagination pagination, XtremandResponse response) {
		List<Criteria> criterias = new ArrayList<>();
		Map<String, Object> formsMap = formDao.find(criterias, new FindLevel[] { FindLevel.SHALLOW }, pagination);
		/***** XNFR-930 ***/
		@SuppressWarnings("unchecked")
		List<FormDTO> list = (List<FormDTO>) formsMap.get("forms");
		list.forEach(item -> {
			if (XamplifyUtils.isValidString(item.getThumbnailImage())) {
				String updatedThumbnailImage = xamplifyUtil.replaceS3WithCloudfrontViceVersa(item.getThumbnailImage());
				item.setCdnThumbnailImage(updatedThumbnailImage);
			}
		});
		formsMap.put("forms", list);
		/***** XNFR-930 ***/
		response.setData(formsMap);
		response.setStatusCode(200);
	}

	@Override
	public XtremandResponse deleteById(Integer formId, Integer userId, String companyProfileName) {
		XtremandResponse response = new XtremandResponse();
		boolean hasAccess = utilService.hasFormAccess(userId);
		response.setAccess(hasAccess);
		if (response.isAccess()) {
			try {
				return formDao.deleteById(formId, response);
			} catch (FormDataAccessException ex) {
				throw new FormDataAccessException(ex);
			}
		} else {
			return response;
		}
	}

	@Override
	public XtremandResponse getById(FormDTO formInputDto) {
		XtremandResponse response = new XtremandResponse();
		if (formInputDto != null && formInputDto.getId() != null) {
			Form form = formDao.getById(formInputDto.getId());
			setFormDto(response, form, false, new FormDTO(), formInputDto);
		} else {
			setFormNotFoundMessage(response);
		}
		return response;
	}

	@Override
	public void setFormDto(XtremandResponse response, Form form, boolean isFromAlias, FormDTO formDTO,
			FormDTO formInputDto) {
		if (form != null) {
			setFormDTOProperties(form, formDTO, formInputDto);
			setFormLabelsDTO(form, isFromAlias, formDTO);
			setFormTeamMemberMappings(form, isFromAlias, formDTO);
			formDTO.setCountryNames(XamplifyUtils.getCountryNames());
			response.setStatusCode(200);
			response.setData(formDTO);
		} else {
			setFormNotFoundMessage(response);
		}
	}

	private void setFormTeamMemberMappings(Form form, boolean isFromAlias, FormDTO formDTO) {
		Set<Integer> selectedGroupIds = new HashSet<>();
		Set<Integer> selectedTeamMemberIds = new HashSet<>();
		Set<FormTeamGroupMapping> mappings = form.getFormTeamGroupMappings();
		for (FormTeamGroupMapping mapping : mappings) {
			if (mapping.getTeamMemberGroupUserMapping() != null
					&& mapping.getTeamMemberGroupUserMapping().getTeamMemberGroup() != null) {
				selectedTeamMemberIds.add(mapping.getTeamMemberGroupUserMapping().getId());
				selectedGroupIds.add(mapping.getTeamMemberGroupUserMapping().getTeamMemberGroup().getId());
			}
		}
		formDTO.setSelectedTeamMemberIds(selectedTeamMemberIds);
		formDTO.setSelectedGroupIds(selectedGroupIds);
	}

	private void setFormLabelsDTO(Form form, boolean isFromAlias, FormDTO formDTO) {
		List<FormLabelDTO> formLabelDtos = formDTO.getFormLabelDTOs();
		List<FormLabel> formLables = form.getFormLabels();
		Map<Integer, FormLabelDTORow> formLabelDtoRowsMap = new HashMap<>();
		for (FormLabel formLabel : formLables) {
			FormLabelDTORow formLabelDtoRow = new FormLabelDTORow();
			FormLabelDTO formLabelDTO = setFormLabelDtoData(isFromAlias, formLabel);
			if (formLabelDtoRowsMap.containsKey(formLabel.getOrder())
					&& formLabelDtoRowsMap.get(formLabel.getOrder()) != null
					&& formLabelDtoRowsMap.get(formLabel.getOrder()).getFormLabelDTOs() != null) {
				formLabelDtoRowsMap.get(formLabel.getOrder()).getFormLabelDTOs().add(formLabelDTO);
			} else {
				formLabelDtoRow.getFormLabelDTOs().add(formLabelDTO);
				formLabelDtoRowsMap.put(formLabel.getOrder(), formLabelDtoRow);
			}
			formLabelDtos.add(formLabelDTO);
		}
		List<FormLabelDTORow> formLabelDtoRowsList = formLabelDtoRowsMap.values().stream().collect(Collectors.toList());
		setIndex(formLabelDtoRowsList);
		formDTO.setFormLabelDTORows(formLabelDtoRowsList);
		formDTO.setFormLabelDTOs(formLabelDtos);
	}

	private void setFormDTOProperties(Form form, FormDTO formDTO, FormDTO formInputDto) {
		formDTO.setId(form.getId());
		formDTO.setName(form.getFormName());
		formDTO.setAlias(form.getAlias());
		formDTO.setDescription(form.getDescription());
		formDTO.setCategoryId(categoryDao.getCategoryIdByType(form.getId(), "F"));
		if (StringUtils.hasText(form.getButtonValue())) {
			formDTO.setButtonValue(form.getButtonValue());
		} else {
			formDTO.setButtonValue(SUBMIT);
		}
		formDTO.setButtonColor(form.getButtonColor());
		formDTO.setFormSubmitMessage(form.getFormSubmitMessage());
		if (form.getBackgroundImage() != null) {
			formDTO.setBackgroundImage(form.getBackgroundImage());
		}
		if (form.getCompanyLogo() == null) {
			formDTO.setCompanyLogo(form.getCompanyProfile().getCompanyLogoPath());
		} else {
			formDTO.setCompanyLogo(form.getCompanyLogo());
		}
		formDTO.setShowCompanyLogo(form.isShowCompanyLogo());
		formDTO.setFooter(form.getFooter());
		formDTO.setShowFooter(form.isShowFooter());
		formDTO.setShowCaptcha(form.isShowCaptcha());
		formDTO.setShowBackgroundImage(form.isShowBackgroundImage());
		User user = userDao.getUser(form.getCreatedUserId());
		formDTO.setCreatedName(XamplifyUtils.setDisplayName(user));
		formDTO.setCreatedDateString(DateUtils.getUtcString(form.getCreatedTime()));
		formDTO.setCompanyName(form.getCompanyProfile().getCompanyName());
		formDTO.setOpenLinkInNewTab(form.isOpenLinkInNewTab());
		formDTO.setFormSubmissionUrl(form.getFormSubmissionUrl());
		formDTO.setThumbnailImage(form.getThumbnailImage());
		formDTO.setFormSubType(form.getFormSubTypeEnum());
		formDTO.setFormType(form.getFormTypeEnum());
		formDTO.setShowTitleHeader(form.isShowTitleHeader());
		Integer trackCount = lmsDao.getTracksCountByFormId(formDTO.getId());
		if (trackCount > 0) {
			formDTO.setAssociatedWithTrack(true);
		}
		formDTO.setQuizForm(form.isQuizForm());
		setCustomThemeColorsForForm(form, formDTO, formInputDto);
	}

	private void setCustomThemeColorsForForm(Form form, FormDTO formDTO, FormDTO formInputDto) {
		VanityUrlDetailsDTO vanityUrlDetailsDTO = new VanityUrlDetailsDTO();
		CompanyThemeActiveDTO activeTheme = null;
		ThemeProperties themeProperties = null;
		if (formInputDto != null) {
			vanityUrlDetailsDTO.setUserId(formInputDto.getUserId());
			vanityUrlDetailsDTO.setVanityUrlFilter(formInputDto.isVanityUrlFilter());
			vanityUrlDetailsDTO.setVendorCompanyProfileName(formInputDto.getVendorCompanyProfileName());
			activeTheme = customSkinServiceImpl.getActiveThemeID(vanityUrlDetailsDTO);
			if (activeTheme != null && activeTheme.getThemeId() > 0) {
				themeProperties = hibernateCustomSkinDao.getThemePropertyByThemeIdAndModule(activeTheme.getThemeId(),
						CustomModule.MAIN_CONTENT);
			}
		}
		if (themeProperties != null && themeProperties.getId() > 0) {
			if (StringUtils.hasText(form.getLabelColor())) {
				formDTO.setLabelColor(form.getLabelColor());
			} else {
				formDTO.setLabelColor(themeProperties.getTextColor());
			}
			if (StringUtils.hasText(form.getTitleColor())) {
				formDTO.setTitleColor(form.getTitleColor());
			} else {
				formDTO.setTitleColor(themeProperties.getTextColor());
			}
			if (StringUtils.hasText(form.getDescriptionColor())) {
				formDTO.setDescriptionColor(form.getDescriptionColor());
			} else {
				formDTO.setDescriptionColor(themeProperties.getTextColor());
			}
			if (StringUtils.hasText(form.getBorderColor())) {
				formDTO.setBorderColor(form.getBorderColor());
			} else {
				formDTO.setBorderColor(themeProperties.getButtonBorderColor());
			}
			if (StringUtils.hasText(form.getBackgroundColor())) {
				formDTO.setBackgroundColor(form.getBackgroundColor());
			} else {
				formDTO.setBackgroundColor(themeProperties.getDivBgColor());
			}
			if (StringUtils.hasText(form.getPageBackgroundColor())) {
				formDTO.setPageBackgroundColor(form.getPageBackgroundColor());
			} else {
				formDTO.setPageBackgroundColor(themeProperties.getBackgroundColor());
			}
			if (StringUtils.hasText(form.getButtonValueColor())) {
				formDTO.setButtonValueColor(form.getButtonValueColor());
			} else {
				formDTO.setButtonValueColor(themeProperties.getTextColor());
			}
			formDTO.setCustomSkinBackgroundColor(themeProperties.getBackgroundColor());
			formDTO.setCustomSkinButtonBorderColor(themeProperties.getButtonBorderColor());
			formDTO.setCustomSkinDivBackgroundColor(themeProperties.getDivBgColor());
			formDTO.setCustomSkinTextColor(themeProperties.getTextColor());
		} else {
			formDTO.setLabelColor(form.getLabelColor());
			formDTO.setTitleColor(form.getTitleColor());
			formDTO.setDescriptionColor(form.getDescriptionColor());
			formDTO.setBorderColor(form.getBorderColor());
			formDTO.setBackgroundColor(form.getBackgroundColor());
			formDTO.setPageBackgroundColor(form.getPageBackgroundColor());
			formDTO.setButtonValueColor(form.getButtonValueColor());
		}
	}

	private String addPriceSymbolPrefix(String priceType) {
		String symbol = "";
		if ("Rupee".equalsIgnoreCase(priceType)) {
			symbol = "₹";
		} else if ("Dollar".equalsIgnoreCase(priceType)) {
			symbol = "$";
		} else if ("Yen".equalsIgnoreCase(priceType)) {
			symbol = "¥";
		} else if ("Pound".equalsIgnoreCase(priceType)) {
			symbol = "£";
		} else if ("Euro".equalsIgnoreCase(priceType)) {
			symbol = "€";
		}
		return symbol;
	}

	private void setFormNotFoundMessage(XtremandResponse response) {
		response.setStatusCode(404);
		response.setMessage("No Record Found For Given Input");
	}

	private void setMulitiChoiceData(FormLabel formLabel, FormLabelDTO formLabelDTO, boolean isFromAlias) {
		String labelType = formLabel.getLabelType().getLabelType();
		formLabelDTO.setLabelType(labelType);
		List<FormLabelChoice> formLabelChoices = formLabel.getFormLabelChoices();
		List<FormChoiceDTO> radioButtonChoiceDtos = new ArrayList<>();
		List<FormChoiceDTO> checkBoxChoiceDtos = new ArrayList<>();
		List<FormChoiceDTO> dropDownChoiceDtos = new ArrayList<>();
		Set<Integer> correctAnswerLabelChoiceIds = new HashSet<>();
		int index = 0;
		for (FormLabelChoice choice : formLabelChoices) {
			FormChoiceDTO formChoiceDto = addChoiceData(choice);
			if (choice.getFormQuizAnswer() != null) {
				formChoiceDto.setCorrect(true);
				correctAnswerLabelChoiceIds.add(choice.getId());
			}
			if ("radio".equals(labelType)) {
				radioButtonChoiceDtos.add(formChoiceDto);
			} else if ("checkbox".equals(labelType)) {
				checkBoxChoiceDtos.add(formChoiceDto);
			} else if ("select".equals(labelType)) {
				index = setDefaultDropDownOption(isFromAlias, dropDownChoiceDtos, index);
				dropDownChoiceDtos.add(formChoiceDto);
			} else if ("quiz_radio".equals(labelType)) {
				radioButtonChoiceDtos.add(formChoiceDto);
			} else if ("quiz_checkbox".equals(labelType)) {
				checkBoxChoiceDtos.add(formChoiceDto);
			}
		}
		formLabelDTO.setRadioButtonChoices(radioButtonChoiceDtos);
		formLabelDTO.setCheckBoxChoices(checkBoxChoiceDtos);
		formLabelDTO.setDropDownChoices(dropDownChoiceDtos);
		formLabelDTO.setCorrectAnswerLabelChoiceIds(correctAnswerLabelChoiceIds);
	}

	private int setDefaultDropDownOption(boolean isFromAlias, List<FormChoiceDTO> dropDownChoiceDtos, int index) {
		if (index == 0 && isFromAlias) {
			index++;
			FormChoiceDTO f = new FormChoiceDTO();
			f.setId(0);
			f.setName(defaultDropdownOption);
			dropDownChoiceDtos.add(f);
		}
		return index;
	}

	private FormChoiceDTO addChoiceData(FormLabelChoice choice) {
		FormChoiceDTO formChoiceDto = new FormChoiceDTO();
		formChoiceDto.setId(choice.getId());
		formChoiceDto.setLabelId(choice.getLabelChoiceId());
		formChoiceDto.setHiddenLabelId(choice.getLabelChoiceHiddenId());
		formChoiceDto.setName(choice.getLabelChoiceName());
		formChoiceDto.setDefaultColumn(choice.isDefaultColumn());
		return formChoiceDto;
	}

	@Override
	public XtremandResponse getByAlias(String alias, boolean vendorJourney, boolean isPartnerJourneyPage) {
		XtremandResponse response = new XtremandResponse();
		FormDTO formDto = new FormDTO();
		formDto.setCountryNames(XamplifyUtils.getCountryNames());
		getFormFields(alias, response, formDto);
		if (response.getStatusCode() == 200) {
			formDto.setAnalyticsType(GeoLocationAnalyticsEnum.FORM);
		}
		response.setCaptchaSiteKey(googleCaptchaSitetKey);
		return response;
	}

	private void getFormFields(String alias, XtremandResponse response, FormDTO formDto) {
		Form form = formDao.getByAlias(alias);
		setFormDto(response, form, true, formDto, null);
	}

	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public XtremandResponse update(FormDTO formDto, MultipartFile thumbnailImageFile) {
		XtremandResponse response = new XtremandResponse();
		response.setAccess(true);
		response = checkWhetherFormEditable(formDto, response);
		if (response != null && response.getStatusCode() > 0 && response.getStatusCode() == 400) {
			return response;
		}
		validateFormName(formDto, response);
		if (formDto.isQuizForm()) {
			validateQuizForm(formDto, response);
		}
		if (response.getStatusCode() != 100 && formDto.getId() != null && response.getStatusCode() != 400) {
			Form form = formDao.getById(formDto.getId());
			String updatedFormName = XamplifyUtils.removeExtraSpace(formDto.getName());
			form.setFormName(updatedFormName);
			form.setUpdatedTime(new Date());
			form.setUpdatedUserId(formDto.getCreatedBy());
			form.setBackgroundColor(formDto.getBackgroundColor());
			form.setLabelColor(formDto.getLabelColor());
			if (StringUtils.hasText(formDto.getButtonValue())) {
				form.setButtonValue(formDto.getButtonValue());
			} else {
				form.setButtonValue(SUBMIT);
			}
			form.setBackgroundImage(formDto.getBackgroundImage());
			form.setCompanyLogo(formDto.getCompanyLogo());
			form.setShowCompanyLogo(formDto.isShowCompanyLogo());
			if (StringUtils.hasText(formDto.getFooter())) {
				if (formDto.getFooter().length() > 225) {
					form.setFooter((formDto.getFooter()).substring(0, 224));
				} else {
					form.setFooter(formDto.getFooter());
				}
			}
			form.setShowFooter(formDto.isShowFooter());
			form.setShowCaptcha(formDto.isShowCaptcha());
			form.setButtonValueColor(formDto.getButtonValueColor());
			form.setButtonColor(formDto.getButtonColor());
			form.setFormSubmitMessage(formDto.getFormSubmitMessage());
			form.setTitleColor(formDto.getTitleColor());
			form.setBorderColor(formDto.getBorderColor());
			form.setPageBackgroundColor(formDto.getPageBackgroundColor());
			form.setShowBackgroundImage(formDto.isShowBackgroundImage());
			form.setFormSubmissionUrl(formDto.getFormSubmissionUrl());
			form.setOpenLinkInNewTab(formDto.isOpenLinkInNewTab());
			form.setQuizForm(formDto.isQuizForm());
			form.setShowTitleHeader(formDto.isShowTitleHeader());
			form.setDescriptionColor(formDto.getDescriptionColor());

			if (formDto.isQuizForm()) {
				form.setFormSubTypeEnum(FormSubTypeEnum.QUIZ);
			} else if (FormSubTypeEnum.QUIZ == form.getFormSubTypeEnum()) {
				form.setFormSubTypeEnum(FormSubTypeEnum.REGULAR);
			}

			setDescription(formDto, form);
			setMaxScore(formDto, form);
			updateFieldLabels(formDto, form);
			updateFormDetails(formDto, form, response);
			if (response.getStatusCode() != 200 && response.getStatusCode() != 0) {
				return response;
			}

			updateFormTeamMemberGroupMapping(formDto.getSelectedTeamMemberIds(), form, true);

			/********** Update Category *******************/
			updateFormCategory(formDto, form);
			String oldImageFilePath = "";
			if (StringUtils.hasText(formDto.getThumbnailImage())) {
				String filePath = formDto.getThumbnailImage();
				String oldImageFileName = filePath.substring(filePath.lastIndexOf('/') + 1);
				oldImageFilePath = form.getCompanyProfile().getId() + sep + form.getId() + sep + oldImageFileName;
			}
			String thumbnailPath = uploadToAwsAndGetThumbnailPath(thumbnailImageFile, form, formDto);
			if (StringUtils.hasText(thumbnailPath)) {
				form.setThumbnailImage(thumbnailPath);
			}
			if (StringUtils.hasText(oldImageFilePath)) {
				amazonWebService.deleteFormThumbnailImage(oldImageFilePath);
			}

		}

		return response;
	}

	private XtremandResponse checkWhetherFormEditable(FormDTO formDto, XtremandResponse response) {
		if (formDto.getId() != null) {
			Integer trackCount = lmsDao.getTracksCountByFormId(formDto.getId());
			if (trackCount > 0) {
				response.setStatusCode(400);
				response.setMessage("Form is not editable as it is associated with a track");
			}
		}
		return response;
	}

	private void setMaxScore(FormDTO formDto, Form form) {
		if (formDto.isQuizForm()) {
			form.setMaxScore(getMaxScore(formDto));
			// formDao.updateMaxScore(form.getId());
		}
	}

	private void updateFormCategory(FormDTO formDto, Form form) {
		if (formDto.getCategoryId() != null && formDto.getCategoryId() > 0
				&& FormTypeEnum.XAMPLIFY_FORM.equals(formDto.getFormType())) {
			categoryDao.updateCategoryIdByType(form.getId(), formDto.getCategoryId(), formDto.getCreatedBy(), "F");
		}
	}

	private XtremandResponse updateFormDetails(FormDTO formDto, Form form, XtremandResponse response) {
		int i = 1;
		boolean hasEmailField = false;
		for (FormLabelDTORow formLabelDTORow : formDto.getFormLabelDTORows()) {
			formDto.setColumnOrder(1);
			for (FormLabelDTO formLabelDTO : formLabelDTORow.getFormLabelDTOs()) {
				Integer id = formLabelDTO.getId();
				FormLabel formLabel = new FormLabel();
				List<FormLabelChoice> formLabelChoices = new ArrayList<>();
				if (id != null) {
					updateIntoTables(form, i, formLabelDTO, id, response, formDto.getColumnOrder(), formDto);
				} else {
					saveIntoTables(form, i, formLabelDTO, formLabel, formLabelChoices, response,
							formDto.getColumnOrder(), formDto);
				}
				if (!hasEmailField && "email".equals(formLabelDTO.getLabelType())) {
					hasEmailField = true;
				}
			}
			i++;
		}
		if (response.getStatusCode() != 200 && response.getStatusCode() != 0) {
			return response;
		}

		formDao.update(form);

		handleDependentDropdowns(formDto, form);

		return response;
	}

	private void saveIntoTables(Form form, int i, FormLabelDTO formLabelDTO, FormLabel formLabel,
			List<FormLabelChoice> formLabelChoices, XtremandResponse response, int columnOrder, FormDTO formDto) {
		saveFormLabel(form, i, formLabelDTO, formLabel, formLabelChoices, response, columnOrder);
		form.getFormLabels().add(formLabel);
		columnOrder++;
		formDto.setColumnOrder(columnOrder);
	}

	public void saveFormLabel(Form form, int i, FormLabelDTO formLabelDTO, FormLabel formLabel,
			List<FormLabelChoice> formLabelChoices, XtremandResponse response, int columnOrder) {
		addFormLabel(form, i, formLabelDTO, formLabel, columnOrder, false);

		addLabelType(formLabelDTO, formLabel);

		addOriginalCRMLabelType(formLabelDTO, formLabel);

		addCheckBoxes(formLabelDTO, formLabel, formLabelChoices, response);

		addRadioButtons(formLabelDTO, formLabel, formLabelChoices, response);

		addDropDowns(formLabelDTO, formLabel, formLabelChoices, response);

		formLabel.setFormLabelChoices(formLabelChoices);

		formDao.save(formLabel);

	}

	private XtremandResponse updateIntoTables(Form form, int i, FormLabelDTO formLabelDTO, Integer id,
			XtremandResponse response, int columnOrder, FormDTO formDto) {
		FormLabel formLabel;
		List<FormLabelChoice> formLabelChoices;
		formLabel = genericDao.get(FormLabel.class, id);
		addFormLabel(form, i, formLabelDTO, formLabel, columnOrder, false);
		addLabelType(formLabelDTO, formLabel);
		addOriginalCRMLabelType(formLabelDTO, formLabel);
		formLabelChoices = formLabel.getFormLabelChoices();

		List<Integer> existingChoiceIds = formLabelChoices.stream().map(choice -> choice.getId())
				.collect(Collectors.toList());

		List<Integer> updatedChoiceIds = new ArrayList<>();

		addAllChoiceIds(formLabelDTO, updatedChoiceIds);

		existingChoiceIds.removeAll(updatedChoiceIds);

		formDao.deleteFormChoiceByIds(existingChoiceIds);

		List<FormChoiceDTO> allChoiceDtos = addAllChoiceDtos(formLabelDTO);

		setFormChoices(formLabel, allChoiceDtos, response);

		if (response.getStatusCode() != 200 && response.getStatusCode() != 0) {
			return response;
		}
		columnOrder++;
		formDto.setColumnOrder(columnOrder);
		formDao.update(formLabel);

		return response;
	}

	private void setFormChoices(FormLabel formLabel, List<FormChoiceDTO> allChoiceDtos, XtremandResponse response) {
		int count = 0;

		for (FormChoiceDTO formChoiceDTO : allChoiceDtos) {
			Integer choiceId = formChoiceDTO.getId();
			if (choiceId != null && choiceId > 0) {
				FormLabelChoice choice = formDao.getFormLabelChoiceById(choiceId);
				choice.setId(choiceId);
				choice.setFormLabel(formLabel);
				choice.setLabelChoiceHiddenId(formChoiceDTO.getHiddenLabelId().trim());
				choice.setLabelChoiceId(formChoiceDTO.getLabelId().trim());
				choice.setLabelChoiceName(formChoiceDTO.getName().trim());
				if (formChoiceDTO.isCorrect()) {
					choice.setFormQuizAnswer(new FormQuizAnswer(choice));
					count++;
				} else {
					choice.setFormQuizAnswer(null);
				}

				if (formLabel.isNonInteractive() && !StringUtils.isEmpty(formLabel.getDefaultChoiceLabel())
						&& formLabel.getDefaultChoiceLabel().equals(formChoiceDTO.getName())) {
					formLabel.setDefaultChoice(choice);
				}
				formDao.update(choice);
			} else {
				FormLabelChoice choice = new FormLabelChoice();
				choice.setFormLabel(formLabel);
				choice.setLabelChoiceHiddenId(formChoiceDTO.getHiddenLabelId().trim());
				choice.setLabelChoiceId(formChoiceDTO.getLabelId().trim());
				choice.setLabelChoiceName(formChoiceDTO.getName().trim());
				if (formChoiceDTO.isCorrect()) {
					choice.setFormQuizAnswer(new FormQuizAnswer(choice));
					count++;
				}
				if (formLabel.isNonInteractive() && !StringUtils.isEmpty(formLabel.getDefaultChoiceLabel())
						&& formLabel.getDefaultChoiceLabel().equals(formChoiceDTO.getName())) {
					formLabel.setDefaultChoice(choice);
				}

				formDao.save(choice);
			}
		}
		if (formLabel.getLabelType() != null && (formLabel.getLabelType().getLabelType().equalsIgnoreCase("quiz_radio")
				|| formLabel.getLabelType().getLabelType().equalsIgnoreCase("quiz_checkbox"))) {
			if (count == 0) {
				response.setStatusCode(400);
				response.setMessage("Default answer is mandatory");
			} else if (formLabel.getLabelType().getLabelType().equalsIgnoreCase("quiz_radio") && count > 1) {
				response.setStatusCode(400);
				response.setMessage("Can't have more than one default answer for label:" + formLabel.getLabelName());
			}
		} else {
			response.setStatusCode(200);
		}
	}

	private List<FormChoiceDTO> addAllChoiceDtos(FormLabelDTO formLabelDTO) {
		List<FormChoiceDTO> choiceDtos = new ArrayList<>();
		List<FormChoiceDTO> radioButtonChoiceDtos = formLabelDTO.getRadioButtonChoices();
		List<FormChoiceDTO> checkBoxChoiceDtos = formLabelDTO.getCheckBoxChoices();
		List<FormChoiceDTO> dropDownChoiceDtos = formLabelDTO.getDropDownChoices();
		choiceDtos.addAll(radioButtonChoiceDtos);
		choiceDtos.addAll(checkBoxChoiceDtos);
		choiceDtos.addAll(dropDownChoiceDtos);
		return choiceDtos;
	}

	private void addAllChoiceIds(FormLabelDTO formLabelDTO, List<Integer> updatedChoiceIds) {
		List<Integer> updatedCheckBoxIds = formLabelDTO.getCheckBoxChoices().stream().map(choice -> choice.getId())
				.collect(Collectors.toList());
		List<Integer> updatedRadioButtonIds = formLabelDTO.getRadioButtonChoices().stream()
				.map(choice -> choice.getId()).collect(Collectors.toList());
		List<Integer> updatedDropDownIds = formLabelDTO.getDropDownChoices().stream().map(choice -> choice.getId())
				.collect(Collectors.toList());
		updatedChoiceIds.addAll(updatedCheckBoxIds);
		updatedChoiceIds.addAll(updatedRadioButtonIds);
		updatedChoiceIds.addAll(updatedDropDownIds);
	}

	private void updateFieldLabels(FormDTO formDto, Form form) {
		List<Integer> allFormLabelsIds = new ArrayList<>();
		List<Integer> updatedFormLabelIds = new ArrayList<>();
		for (FormLabelDTORow formLabelDTORow : formDto.getFormLabelDTORows()) {
			updatedFormLabelIds.addAll(formLabelDTORow.getFormLabelDTOs().stream().map(formLabel -> formLabel.getId())
					.collect(Collectors.toList()));
		}
		List<Integer> exisitngFormLabelIds = form.getFormLabels().stream().map(formLabel -> formLabel.getId())
				.collect(Collectors.toList());
		allFormLabelsIds.addAll(exisitngFormLabelIds);
		allFormLabelsIds.addAll(updatedFormLabelIds);
		exisitngFormLabelIds.removeAll(updatedFormLabelIds);
		formDao.deleteFormLabelByIds(exisitngFormLabelIds);// Delete removed columns from UI.
	}

	@Override
	public List<FormLabelDTO> listFormLabelsById(Integer id) {
		return formDao.listFormLabelsById(id, false, false, 0);

	}

	@Override
	public XtremandResponse getPriceTypes() {
		XtremandResponse response = new XtremandResponse();
		List<String> list = new ArrayList<>(Arrays.asList(priceTypes.replace(" ", "").trim().split(",")));
		response.setData(list);
		response.setAccess(true);
		response.setStatusCode(200);
		return response;
	}

	@Override
	public XtremandResponse getMdfFormByCompanyId(Integer companyId, boolean showingForPartner) {
		XtremandResponse response = new XtremandResponse();
		Form form = formDao.getMDFFormByCompanyId(companyId);
		if (form != null) {
			setFormDto(response, form, showingForPartner, new FormDTO(), null);
		} else {
			response.setStatusCode(404);
			response.setMessage("Mdf Form Not Available For This Company");
		}
		return response;
	}

	@Override
	public XtremandResponse quizList(Pagination pagination) {
		XtremandResponse response = new XtremandResponse();
		Map<String, Object> formsMap = formDao.findQuizList(pagination);
		response.setData(formsMap);
		response.setStatusCode(200);
		return response;
	}

	@Override
	public XtremandResponse listDefaultForms(Pagination pagination) {
		XtremandResponse response = new XtremandResponse();
		Map<String, Object> formsMap = formDao.listDefaultForms(pagination);
		response.setData(formsMap);
		response.setStatusCode(200);
		response.setAccess(true);
		return response;
	}

	@Override
	public XtremandResponse deleteDefaultForm(Integer formId) {
		try {
			XtremandResponse response = new XtremandResponse();
			formDao.delete(formId);
			response.setStatusCode(200);
			response.setMessage("Default Form is deleted successfully");
			return response;
		} catch (DuplicateEntryException de) {
			throw new DuplicateEntryException(de.getMessage());
		} catch (HibernateException | FormDataAccessException e) {
			throw new FormDataAccessException(e);
		} catch (Exception ex) {
			throw new FormDataAccessException(ex);
		}

	}

	/******* XNFR-424 *******/
	private void addFormLabelInfo(FormDTO formDto, XtremandResponse response, Form form, List<FormLabel> formLabels,
			int i, FormLabelDTO formLabelDTO) {
		FormLabel formLabel = new FormLabel();
		if (formDto.isSaveAs()) {
			formLabelDTO.setId(null);
		}
		addFormLabel(form, i, formLabelDTO, formLabel, formDto.getColumnOrder(), formDto.isCreatingMdfForm());

		addLabelType(formLabelDTO, formLabel);

		addOriginalCRMLabelType(formLabelDTO, formLabel);

		List<FormLabelChoice> formLabelChoices = new ArrayList<>();

		addCheckBoxes(formLabelDTO, formLabel, formLabelChoices, response);

		addRadioButtons(formLabelDTO, formLabel, formLabelChoices, response);

		addDropDowns(formLabelDTO, formLabel, formLabelChoices, response);

		formLabel.setFormLabelChoices(formLabelChoices);

		formLabels.add(formLabel);

		if (!formDto.isHasEmailField() && "email".equals(formLabel.getLabelType().getLabelType())) {
			formDto.setHasEmailField(true);
		}
		int columnOrder = formDto.getColumnOrder();
		columnOrder++;
		formDto.setColumnOrder(columnOrder);
	}

	private FormLabelDTO setFormLabelDtoData(boolean isFromAlias, FormLabel formLabel) {
		FormLabelDTO formLabelDTO = new FormLabelDTO();
		formLabelDTO.setId(formLabel.getId());
		formLabelDTO.setLabelId(formLabel.getLabelId());
		formLabelDTO.setLabelName(formLabel.getLabelName());
		formLabelDTO.setHiddenLabelId(formLabel.getHiddenLabelId());
		formLabelDTO.setOrder(formLabel.getOrder());
		formLabelDTO.setPlaceHolder(formLabel.getPlaceHolder());
		formLabelDTO.setRequired(formLabel.isRequired());
		formLabelDTO.setNonInteractive(formLabel.isNonInteractive());
		formLabelDTO.setPrivate(formLabel.isPrivate());
		if (formLabel.isNonInteractive() && !StringUtils.isEmpty(formLabel.getDefaultChoiceLabel())) {
			formLabelDTO.setDefaultChoiceLabel(formLabel.getDefaultChoiceLabel());
		}
		formLabelDTO.setDefaultColumn(formLabel.isDefaultColumn());
		if (StringUtils.hasText(formLabel.getPriceType())) {
			formLabelDTO.setPriceType(formLabel.getPriceType());
		} else {
			formLabelDTO.setPriceType("Dollar");
		}
		formLabelDTO.setPriceSymbol(addPriceSymbolPrefix((formLabel.getPriceType())));
		if (StringUtils.hasText(formLabel.getDescription())) {
			formLabelDTO.setDescription(formLabel.getDescription());
		}
		/******* Form Label Type ***********/
		setMulitiChoiceData(formLabel, formLabelDTO, isFromAlias);
		/**** XNFR-423 ****/
		if ("country".equals(formLabel.getLabelType().getLabelType())) {
			if (isFromAlias) {
				formLabelDTO.setValue("Please Select Country");
			}

		}
		return formLabelDTO;
	}

	private void setIndex(List<FormLabelDTORow> formLabelDtoRowsList) {
		int index = 1;
		for (FormLabelDTORow formLabelDtoRow : formLabelDtoRowsList) {
			List<FormLabelDTO> formLabelDTOListRowWise = formLabelDtoRow.getFormLabelDTOs();
			for (FormLabelDTO formLabelDTO : formLabelDTOListRowWise) {
				formLabelDTO.setIndex(index);
				index++;
			}
		}
	}

	/******* XNFR-424 ENDS *******/

	/******* XNFR-522 *******/
	public XtremandResponse createdDefaultForms(boolean isPartner) {
		XtremandResponse response = new XtremandResponse();

		CompanyProfile companyProfile = genericDao.get(CompanyProfile.class, 1);

		Form form = new Form();
		GenerateRandomPassword password = new GenerateRandomPassword();
		form.setAlias(password.getPassword());

		form.setBackgroundColor("#ffffff");
		form.setBackgroundImage("");
		form.setBorderColor("#ddd");
		form.setButtonColor("");
		form.setButtonValue("Submit");
		form.setCompanyLogo(companyProfile.getCompanyLogoPath());
		form.setCompanyProfile(companyProfile);
		form.setCreatedTime(new Date());
		form.setCreatedUserId(1);
		form.setDefaultForm(true);
		form.setDescription("Default Form");
		form.setDescriptionColor("#000");
		form.setFormSubTypeEnum(FormSubTypeEnum.REGULAR);
		if (isPartner) {
			form.setFormName("Default_Master_Partner_form");
			form.setFormTypeEnum(FormTypeEnum.MASTER_PARTNER_FORM);
		}
		form.setLabelColor("#000");
		form.setPageBackgroundColor("#F1F3FA");
		form.setShowTitleHeader(true);
		form.setTitleColor("#000");

		List<FormLabel> formLabels = new ArrayList<>();

		FormLabel formLabel1 = new FormLabel();
		formLabel1.setForm(form);
		FormLabel formLabel2 = new FormLabel();
		formLabel2.setForm(form);
		FormLabel formLabel3 = new FormLabel();
		formLabel3.setForm(form);
		FormLabel formLabel4 = new FormLabel();
		formLabel4.setForm(form);
		FormLabel formLabel5 = new FormLabel();
		formLabel5.setForm(form);
		FormLabel formLabel6 = new FormLabel();
		formLabel6.setForm(form);
		FormLabel formLabel7 = new FormLabel();
		formLabel7.setForm(form);

		FormLabelType emailType = formDao.findByType("email");
		FormLabelType textType = formDao.findByType("text");
		FormLabelType dateType = formDao.findByType("date");
		FormLabelType priceType = formDao.findByType("price");
		FormLabelType uploadType = formDao.findByType("upload");

		formLabel1.setColumnOrder(1);
		formLabel2.setColumnOrder(1);
		formLabel3.setColumnOrder(1);
		formLabel4.setColumnOrder(1);
		formLabel5.setColumnOrder(1);
		formLabel6.setColumnOrder(1);
		formLabel7.setColumnOrder(1);

		formLabel1.setHiddenLabelId("email");
		formLabel2.setHiddenLabelId("firstname");
		formLabel3.setHiddenLabelId("lastname");
		formLabel4.setHiddenLabelId("mobilenumber");
		formLabel5.setHiddenLabelId("date");
		formLabel6.setHiddenLabelId("price");
		formLabel7.setHiddenLabelId("upload");

		formLabel1.setLabelId("email");
		formLabel2.setLabelId("first_name");
		formLabel3.setLabelId("last_name");
		formLabel4.setLabelId("mobile_number");
		formLabel5.setLabelId("date");
		formLabel6.setLabelId("price");
		formLabel7.setLabelId("upload");

		formLabel1.setLabelName("Email");
		formLabel2.setLabelName("First Name");
		formLabel3.setLabelName("Last Name");
		formLabel4.setLabelName("Mobile Number");
		formLabel5.setLabelName("Date");
		formLabel6.setLabelName("Price");
		formLabel7.setLabelName("Upload");

		formLabel1.setLabelType(emailType);
		formLabel2.setLabelType(textType);
		formLabel3.setLabelType(textType);
		formLabel4.setLabelType(textType);
		formLabel5.setLabelType(dateType);
		formLabel6.setLabelType(priceType);
		formLabel7.setLabelType(uploadType);

		formLabel1.setOrder(1);
		formLabel2.setOrder(2);
		formLabel3.setOrder(3);
		formLabel4.setOrder(4);
		formLabel5.setOrder(5);
		formLabel6.setOrder(6);
		formLabel7.setOrder(7);

		formLabel1.setPlaceHolder("Email");
		formLabel2.setPlaceHolder("First Name");
		formLabel3.setPlaceHolder("Last Name");
		formLabel4.setPlaceHolder("Mobile Number");
		formLabel5.setPlaceHolder("Date");
		formLabel6.setPlaceHolder("Price");
		formLabel7.setPlaceHolder("Upload");

		formLabel6.setPriceType("Dollar");

		formLabel4.setRequired(true);

		formLabels.add(formLabel1);
		formLabels.add(formLabel2);
		formLabels.add(formLabel3);
		formLabels.add(formLabel4);
		formLabels.add(formLabel5);
		formLabels.add(formLabel6);
		formLabels.add(formLabel7);

		form.setFormLabels(formLabels);

		genericDao.save(form);
		response.setStatusCode(200);
		response.setMessage("SUCCESS");
		return response;
	}

	public XtremandResponse updateCustomFormDetails(FormDTO formDto, Form form, XtremandResponse response) {
		int i = 1;
		boolean hasEmailField = false;
		for (FormLabelDTORow formLabelDTORow : formDto.getFormLabelDTORows()) {
			formDto.setColumnOrder(1);
			for (FormLabelDTO formLabelDTO : formLabelDTORow.getFormLabelDTOs()) {
				Integer id = formLabelDTO.getId();
				FormLabel formLabel = new FormLabel();
				List<FormLabelChoice> formLabelChoices = new ArrayList<>();
				if (id != null) {
					updateIntoTables(form, i, formLabelDTO, id, response, formDto.getColumnOrder(), formDto);
				} else {
					saveIntoTables(form, i, formLabelDTO, formLabel, formLabelChoices, response,
							formDto.getColumnOrder(), formDto);
				}
				if (!hasEmailField && "email".equals(formLabelDTO.getLabelType())) {
					hasEmailField = true;
				}
			}
			i++;
		}
		if (response.getStatusCode() != 200 && response.getStatusCode() != 0) {
			return response;
		}

		formDao.update(form);

		handleDependentDropdowns(formDto, form);

		return response;
	}

}
