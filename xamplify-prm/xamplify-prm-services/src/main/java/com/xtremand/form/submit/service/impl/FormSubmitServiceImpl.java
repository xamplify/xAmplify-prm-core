package com.xtremand.form.submit.service.impl;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.services.s3.AmazonS3;
import com.xtremand.aws.AmazonWebModel;
import com.xtremand.aws.AmazonWebService;
import com.xtremand.campaign.exception.XamplifyDataAccessException;
import com.xtremand.common.bom.Criteria;
import com.xtremand.common.bom.FindLevel;
import com.xtremand.common.bom.Pagination;
import com.xtremand.dao.util.GenericDAO;
import com.xtremand.form.bom.Form;
import com.xtremand.form.bom.FormLabel;
import com.xtremand.form.bom.FormLabelChoice;
import com.xtremand.form.bom.FormLabelType;
import com.xtremand.form.bom.FormQuizAnswer;
import com.xtremand.form.bom.FormSubTypeEnum;
import com.xtremand.form.bom.FormTypeEnum;
import com.xtremand.form.dao.FormDao;
import com.xtremand.form.dto.FormDataDto;
import com.xtremand.form.dto.FormDataForLeadNotification;
import com.xtremand.form.dto.FormLabelDTO;
import com.xtremand.form.dto.FormSubmissionDTO;
import com.xtremand.form.dto.FormValue;
import com.xtremand.form.dto.SurveyAnalyticsDTO;
import com.xtremand.form.dto.SurveyChoiceSummaryDTO;
import com.xtremand.form.dto.SurveyQuestionSummaryDTO;
import com.xtremand.form.dto.SurveyTextResponseDTO;
import com.xtremand.form.service.FormService;
import com.xtremand.form.submit.bom.FormSubmit;
import com.xtremand.form.submit.bom.FormSubmitEnum;
import com.xtremand.form.submit.bom.FormSubmitMultiChoice;
import com.xtremand.form.submit.bom.FormSubmitSingleChoice;
import com.xtremand.form.submit.dao.FormSubmitDao;
import com.xtremand.form.submit.dto.FormSubmitAnswerDTO;
import com.xtremand.form.submit.dto.FormSubmitDTO;
import com.xtremand.form.submit.dto.FormSubmitFieldsDTO;
import com.xtremand.form.submit.dto.FormSubmitFieldsValuesDTO;
import com.xtremand.form.submit.service.FormSubmitService;
import com.xtremand.formbeans.UserDTO;
import com.xtremand.formbeans.XtremandResponse;
import com.xtremand.landing.page.analytics.bom.GeoLocationAnalytics;
import com.xtremand.landing.page.analytics.bom.GeoLocationAnalyticsEnum;
import com.xtremand.landing.page.analytics.dto.GeoLocationAnalyticsDTO;
import com.xtremand.lms.bom.LearningTrack;
import com.xtremand.lms.bom.LearningTrackContent;
import com.xtremand.lms.bom.PartnerActivityType;
import com.xtremand.lms.dao.LMSDAO;
import com.xtremand.lms.dto.LearningTrackDto;
import com.xtremand.lms.service.LMSService;
import com.xtremand.lms.service.impl.LMSServiceImpl;
import com.xtremand.mail.service.AsyncComponent;
import com.xtremand.mdf.bom.MdfRequest;
import com.xtremand.mdf.dao.MdfDao;
import com.xtremand.mdf.exception.DuplicateRequestTitleException;
import com.xtremand.user.bom.User;
import com.xtremand.user.bom.UserList;
import com.xtremand.user.dao.UserDAO;
import com.xtremand.userlist.service.UserListService;
import com.xtremand.util.DateUtils;
import com.xtremand.util.FileUtil;
import com.xtremand.util.ResponseUtil;
import com.xtremand.util.XamplifyUtils;

import au.com.bytecode.opencsv.CSVWriter;

@Service
@Transactional
public class FormSubmitServiceImpl implements FormSubmitService {

	private static final Logger logger = LoggerFactory.getLogger(FormSubmitServiceImpl.class);

	private static final String EMAIL = "email";

	private static final String UPLOAD = "upload";

	private static final String SUBMITTED_DATA = "submittedData";

	private static final String SUCCESS = "Success";

	private static final String TEXT_CSV = "text/csv; charset=UTF-8";

	private static final String CONTENT_DISPOSITION = "Content-disposition";

	private static final String UTF_8 = "UTF-8";

	private static final String ATTACHMENT_AND_FILE_NAME = "attachment;filename=";

	@Autowired
	private FormSubmitDao formSubmitDao;

	@Autowired
	private FormService formService;

	@Autowired
	private FormDao formDao;

	@Autowired
	private UserDAO userDao;

	@Autowired
	private MdfDao mdfDao;

	@Autowired
	@Lazy
	private AsyncComponent asyncComponent;

	@Autowired
	private AmazonWebService amazonWebService;

	@Autowired
	private LMSServiceImpl lMSServiceImpl;

	@Autowired
	private GenericDAO genericDAO;

	@Autowired
	private LMSDAO lmsDAO;

	@Autowired
	private LMSService lmsService;

	@Value("${web_url}")
	private String webUrl;

	@Value("${form.file.max.size}")
	private String maxFileSizeInString;

	Properties props = new Properties();

	@Value("${local.server.vod.path}")
	private String localServerVodPath;

	@Value("${spring.profiles.active}")
	private String profiles;

	@Value("${server_path}")
	String serverPath;

	@Autowired
	private FileUtil fileUtil;

	@Autowired
	private UserListService userListService;

	private static final String CHECK_BOX = "checkbox";

	@Value("${media_base_path}")
	String mediaBasePath;

	@Value("#{'${email.labels}'.split(',')}")
	private List<String> emailLabels;

	@Value("#{'${firstname.labels}'.split(',')}")
	private List<String> firstNameLabels;

	@Value("#{'${lastname.labels}'.split(',')}")
	private List<String> lastNameLabels;

	@Value("#{'${mobilenumber.labels}'.split(',')}")
	private List<String> mobileNumberLabels;

	@Value("#{'${state.labels}'.split(',')}")
	private List<String> stateLabels;

	@Value("#{'${zip.labels}'.split(',')}")
	private List<String> zipLabels;

	@Value("#{'${jobtitle.labels}'.split(',')}")
	private List<String> jobTitleLabels;

	@Value("#{'${contact.company.labels}'.split(',')}")
	private List<String> contactCompanyLabels;

	@Value("#{'${address.labels}'.split(',')}")
	private List<String> addressLabels;

	@Value("#{'${city.labels}'.split(',')}")
	private List<String> cityLabels;

	@Value("#{'${country.labels}'.split(',')}")
	private List<String> countryLabels;

	@Value("#{'${multi.choice.types}'.split(',')}")
	private List<String> multiChoiceTypes;

	@Value("${show_shorten_url_log}")
	private boolean showShortenUrlLog;

	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public XtremandResponse save(FormSubmitDTO formSubmitDTO, MdfRequest mdfRequest) {
		XtremandResponse response = new XtremandResponse();
		/********* xt_form_submit ************/
		FormSubmit formSubmit = new FormSubmit();
		/*******
		 * This condition checks if form is submitted from campaign/landingpage/public
		 * url or not
		 *************/
		setCampaignOrPageFormData(formSubmitDTO, response, formSubmit);
		if (response.getStatusCode() != 404) {
			formSubmit.setSubmittedOn(new Date());
			Form form = formDao.getById(formSubmitDTO.getId());
			Integer companyId = form.getCompanyProfile().getId();
			formSubmit.setForm(form);
			List<FormSubmitFieldsDTO> fieldsDTOs = formSubmitDTO.getFields();
			Set<FormSubmitSingleChoice> singleChoices = new HashSet<>();
			Set<FormSubmitMultiChoice> multiChoices = new HashSet<>();
			List<FormDataForLeadNotification> formDataForLeadNotifications = new ArrayList<>();
			AmazonS3 amazonClient = amazonWebService.getAmazonClient();
			List<String> requestTitles = new ArrayList<>();
			if (FormTypeEnum.MDF_REQUEST_FORM.equals(form.getFormTypeEnum())) {
				requestTitles = mdfDao.listAllRequestTitlesByPartnershipId(mdfRequest.getPartnership().getId());
			}
			setLmsFormProperties(formSubmitDTO, formSubmit);
			UserDTO user = new UserDTO();
			for (FormSubmitFieldsDTO fieldsDTO : fieldsDTOs) {
				String value = fieldsDTO.getValue();
				Integer formLabelId = fieldsDTO.getId();
				FormLabel formLabel = formDao.getFormLabelId(formLabelId);
				if (formLabel != null) {
					FormDataForLeadNotification formDataForLeadNotification = new FormDataForLeadNotification();
					formDataForLeadNotification.setLabelName(formLabel.getLabelName());
					formDataForLeadNotification.setRequired(formLabel.isRequired());
					String labelType = formLabel.getLabelType().getLabelType();
					if (EMAIL.equals(labelType) || "text".equals(labelType) || "textarea".equals(labelType)
							|| "number".equals(labelType) || "date".equals(labelType) || "price".equals(labelType)
							|| "country".equals(labelType)) {
						formDataForLeadNotification.setLabelValue(value);
						setRequestTitleAndRequestAmount(form, requestTitles, value, formLabel);
						setSingleChoiceData(formSubmit, singleChoices, value, formLabel);
					} else if (UPLOAD.equals(labelType) && StringUtils.hasText(value)) {
						uploadFile(formSubmit, form, companyId, singleChoices, amazonClient, value, formLabel,
								formDataForLeadNotification);
					} else if (CHECK_BOX.equals(labelType) || "quiz_checkbox".equals(labelType)) {
						setCheckBoxChoicesData(formSubmit, multiChoices, fieldsDTO, formLabel, formLabelId,
								formDataForLeadNotification);
					} else {
						setOtherChoicesData(formSubmit, multiChoices, value, formLabelId, formLabel,
								formDataForLeadNotification);
					}
					formDataForLeadNotifications.add(formDataForLeadNotification);
					setContactData(user, formLabel, value);
				}
			}
			formSubmit.setSingleChoices(singleChoices);
			formSubmit.setMultiChoices(multiChoices);
			/****** If it is mdf request *************/
			if (FormTypeEnum.MDF_REQUEST_FORM.equals(formSubmitDTO.getFormType()) && mdfRequest != null) {
				formSubmit.setMdfRequest(mdfRequest);
			}

			formSubmitDao.save(formSubmit);
			Map<String, Object> data = new LinkedHashMap<>();
			data.put("formSubmitId", formSubmit.getId());
			response.setMessage("Your data submitted successfully");
			response.setData(data);
			response.setStatusCode(200);

			saveGeoLocationDetails(formSubmit, formSubmitDTO);

			sendEmailNotifications(formSubmitDTO, mdfRequest, formSubmit, form, formDataForLeadNotifications,
					form.getRequestTitle(), form.getRequestAmount());
			updateLearingTrackProgress(formSubmitDTO, formSubmit);
			createContact(user, form, formSubmit);

			setQuizProperties(formSubmit, form, data);

			response.setData(data);
		}
		return response;
	}

	private void setRequestTitleAndRequestAmount(Form form, List<String> requestTitles, String value,
			FormLabel formLabel) {
		String requestTitle;
		String requestAmount;
		if (FormTypeEnum.MDF_REQUEST_FORM.equals(form.getFormTypeEnum()) && formLabel.isDefaultColumn()) {
			if (formLabel.getLabelType().getId().equals(1)) {
				requestTitle = value;
				form.setRequestTitle(requestTitle);
				String convertedRequestTitle = XamplifyUtils.convertToLowerCaseAndExcludeSpace(requestTitle);
				if (requestTitles.indexOf(convertedRequestTitle) > -1) {
					throw new DuplicateRequestTitleException("Duplicate title");
				}
			} else if (formLabel.getLabelType().getId().equals(8)) {
				requestAmount = "$ " + XamplifyUtils.convertStringToDoubleString(value);
				form.setRequestAmount(requestAmount);
			}
		}
	}

	private void setLmsFormProperties(FormSubmitDTO formSubmitDTO, FormSubmit formSubmit) {
		if (FormTypeEnum.LMS_FORM.equals(formSubmitDTO.getFormType())) {
			User user = genericDAO.get(User.class, formSubmitDTO.getUserId());
			LearningTrack learningTrack = genericDAO.get(LearningTrack.class, formSubmitDTO.getLearningTrackId());
			formSubmit.setUser(user);
			formSubmit.setLearningTrack(learningTrack);
			formSubmit.setFormSubmitEnum(FormSubmitEnum.LMS_FORM);
		}
	}

	private void setQuizProperties(FormSubmit formSubmit, Form form, Map<String, Object> data) {
		if (form.isQuizForm()) {
			Map<Integer, FormSubmitAnswerDTO> submittedData = getQuizFormSubmittedData(formSubmit.getId());
			data.put(SUBMITTED_DATA, submittedData);
			data.put("score", formSubmit.getScore());
			data.put("maxScore", form.getMaxScore());
		}
	}

	private void updateLearingTrackProgress(FormSubmitDTO formSubmitDTO, FormSubmit formSubmit) {
		if (FormTypeEnum.LMS_FORM.equals(formSubmitDTO.getFormType())) {
			evaluateLMSQuiz(formSubmit);
			updateLearningTrackProgress(formSubmitDTO);
		}
	}

	private void sendEmailNotifications(FormSubmitDTO formSubmitDTO, MdfRequest mdfRequest, FormSubmit formSubmit,
			Form form, List<FormDataForLeadNotification> formDataForLeadNotifications, String requestTitle,
			String requestAmount) {
		if (FormTypeEnum.XAMPLIFY_FORM.equals(form.getFormTypeEnum())
				|| FormTypeEnum.MASTER_PARTNER_FORM.equals(form.getFormTypeEnum())) {
			asyncComponent.sendFormLeadsNotification(formSubmit, formSubmitDTO, formDataForLeadNotifications);
		}
		if (FormTypeEnum.MDF_REQUEST_FORM.equals(formSubmitDTO.getFormType()) && mdfRequest != null) {
			asyncComponent.sendNewMdfRequestNotification(mdfRequest, requestTitle, requestAmount);
		}
	}

	private void updateLearningTrackProgress(FormSubmitDTO formSubmitDTO) {
		LearningTrackDto learningTrackDto = new LearningTrackDto();
		learningTrackDto.setId(formSubmitDTO.getLearningTrackId());
		learningTrackDto.setUserId(formSubmitDTO.getUserId());
		learningTrackDto.setContentId(formSubmitDTO.getId());
		learningTrackDto.setTypeQuizId(true);
		learningTrackDto.setStatus(PartnerActivityType.SUBMITTED);
		lmsService.updatePartnerProgress(learningTrackDto);
	}

	private void saveGeoLocationDetails(FormSubmit formSubmit, FormSubmitDTO formSubmitDTO) {
		GeoLocationAnalytics geoLocationAnalytics = new GeoLocationAnalytics();
		if (formSubmitDTO.getGeoLocationAnalyticsDTO() != null) {
			BeanUtils.copyProperties(formSubmitDTO.getGeoLocationAnalyticsDTO(), geoLocationAnalytics);
		} else {
			geoLocationAnalytics.setDeviceType("Computer");
		}
		geoLocationAnalytics.setAnalyticsType(GeoLocationAnalyticsEnum.FORM_SUBMIT);
		geoLocationAnalytics.setFormSubmit(formSubmit);
		geoLocationAnalytics.setOpenedTime(new Date());
		genericDAO.save(geoLocationAnalytics);
	}

	private void createContact(UserDTO user, Form form, FormSubmit formSubmit) {
		if (user != null && !(StringUtils.isEmpty(user.getEmailId()))) {
			List<UserList> formContactLists = getFormContactLists(formSubmit, form);
			String emailDebugMessage = "Email : " + user.getEmailId();
			String formName = "Form Name : " + form.getFormName();
			String formSubmitId = "Form Submit Id : " + formSubmit.getId();
			String debugMessage = "Create Contact " + emailDebugMessage + "  " + formName + "  " + formSubmitId;
			String methodStartedDebugMessage = debugMessage + "  Started At " + new Date();
			logger.debug(methodStartedDebugMessage);
			long startTime = System.currentTimeMillis();
			if (!formContactLists.isEmpty()) {
				String formContactsListFoundDebugMessage = "Form Contacts Lists Size : " + formContactLists.size()
						+ " For " + debugMessage;
				logger.debug(formContactsListFoundDebugMessage);
				Set<UserDTO> users = new HashSet<>();
				users.add(user);
				for (UserList formContactList : formContactLists) {
					if (formContactList != null) {
						userListService.updateContactList(formContactList, users,
								formContactList.getOwner().getUserId());
					}
				}
			}
			long stopTime = System.currentTimeMillis();
			long elapsedTime = stopTime - startTime;
			long minutes = TimeUnit.MILLISECONDS.toMinutes(elapsedTime);
			String completedDebugMessage = debugMessage + " Completed In " + minutes + " minutes at " + new Date()
					+ "***Active Thread Count*****" + Thread.activeCount();
			logger.debug(completedDebugMessage);
		}
	}

	private List<UserList> getFormContactLists(FormSubmit formSubmit, Form form) {
		List<UserList> formContactLists = new ArrayList<>();
		UserList orgAdminFormContactList = userListService.getFormContactListForOrgAdmin(form.getId(),
				form.getCompanyProfile().getId());
		formContactLists.add(orgAdminFormContactList);
		return formContactLists;
	}

	private void setContactData(UserDTO userDTO, FormLabel formLabel, String value) {
		String labelName = formLabel.getLabelName().toLowerCase().trim();
		setUserDtoByFormLabelName(userDTO, formLabel, value, labelName, null);
	}

	private void setUserDtoByFormLabelName(UserDTO userDTO, FormLabel formLabel, String value, String labelName,
			Integer labelTypeId) {
		if (shouldSetEmail(userDTO, formLabel, labelName, labelTypeId)) {
			setLowerCaseTrimmedEmailId(userDTO, value);
		} else if (shouldSetMobileNumber(formLabel, labelName)) {
			userDTO.setMobileNumber(value);
		} else if (firstNameLabels.contains(labelName)) {
			userDTO.setFirstName(value);
		} else if (lastNameLabels.contains(labelName)) {
			userDTO.setLastName(value);
		} else if (stateLabels.contains(labelName)) {
			userDTO.setState(value);
		} else if (zipLabels.contains(labelName)) {
			userDTO.setZipCode(value);
		} else if (jobTitleLabels.contains(labelName)) {
			userDTO.setJobTitle(value);
		} else if (contactCompanyLabels.contains(labelName)) {
			userDTO.setContactCompany(value);
		} else if (addressLabels.contains(labelName)) {
			userDTO.setAddress(value);
		} else if (cityLabels.contains(labelName)) {
			userDTO.setCity(value);
		} else if (countryLabels.contains(labelName)) {
			userDTO.setCountry(value);
		}
	}

	private boolean shouldSetEmail(UserDTO userDTO, FormLabel formLabel, String labelName, Integer labelTypeId) {
		return StringUtils.isEmpty(userDTO.getEmailId()) && (((labelTypeId != null && labelTypeId.equals(3))
				|| (formLabel != null && EMAIL.equals(formLabel.getLabelType().getLabelType()))
				|| emailLabels.contains(labelName)));
	}

	private boolean shouldSetMobileNumber(FormLabel formLabel, String labelName) {
		return (formLabel != null && "phone".equals(formLabel.getLabelType().getLabelType()))
				|| mobileNumberLabels.contains(labelName);
	}

	private void setLowerCaseTrimmedEmailId(UserDTO userDTO, String value) {
		String lowerCaseTrimmedEmailId = value;
		if (StringUtils.hasText(value)) {
			lowerCaseTrimmedEmailId = value.toLowerCase().trim();
		}
		userDTO.setEmailId(lowerCaseTrimmedEmailId);
	}

	private void evaluateLMSQuiz(FormSubmit formSubmit) {
		int score = 0;
		Set<FormSubmitMultiChoice> choices = formSubmit.getMultiChoices();
		if (choices != null && !choices.isEmpty()) {
			Map<Integer, List<Integer>> correctAnswersMap = getCorrectAnswersMap(formSubmit.getForm());
			if (!correctAnswersMap.isEmpty()) {
				Map<Integer, List<Integer>> submittedAnswersMap = getSubmittedAnswersMap(choices);
				if (!submittedAnswersMap.isEmpty()) {
					score = calculateScoreBasedOnCorrectAnswers(score, correctAnswersMap, submittedAnswersMap);
				}
			}
		}
		formSubmit.setScore(score);
	}

	private int calculateScoreBasedOnCorrectAnswers(int score, Map<Integer, List<Integer>> correctAnswersMap,
			Map<Integer, List<Integer>> submittedAnswersMap) {
		for (Map.Entry<Integer, List<Integer>> entry : correctAnswersMap.entrySet()) {
			List<Integer> correctAnswerList = entry.getValue();
			List<Integer> submittedAnswerList = submittedAnswersMap.get(entry.getKey());
			if (correctAnswerList != null && submittedAnswerList != null
					&& correctAnswerList.equals(submittedAnswerList)) {
				score++;
			}
		}
		return score;
	}

	private Map<Integer, List<Integer>> getCorrectAnswersMap(Form form) {
		Map<Integer, List<Integer>> correctAnswersMap = new HashMap<>();
		List<FormLabel> formLabels = form.getFormLabels();
		if (formLabels != null && !formLabels.isEmpty()) {
			for (FormLabel formLabel : formLabels) {
				if (formLabel != null && formLabel.getLabelType() != null) {
					Integer labelId = formLabel.getId();
					FormLabelType labelType = formLabel.getLabelType();
					if (labelType != null && ("quiz_radio".equals(labelType.getLabelType())
							|| "quiz_checkbox".equals(labelType.getLabelType()))) {
						List<FormLabelChoice> choices = formLabel.getFormLabelChoices();
						addCorrectChoicesByLabelId(correctAnswersMap, labelId, choices);
					}
				}
			}
		}
		return correctAnswersMap;
	}

	private void addCorrectChoicesByLabelId(Map<Integer, List<Integer>> correctAnswersMap, Integer labelId,
			List<FormLabelChoice> choices) {
		if (choices != null && !choices.isEmpty()) {
			for (FormLabelChoice choice : choices) {
				FormQuizAnswer quizAnswer = choice.getFormQuizAnswer();
				if (quizAnswer != null) {
					if (correctAnswersMap.keySet().contains(labelId)) {
						correctAnswersMap.get(labelId).add(choice.getId());
					} else {
						ArrayList<Integer> choiceList = new ArrayList<>();
						choiceList.add(choice.getId());
						correctAnswersMap.put(labelId, choiceList);
					}
				}
			}
		}
	}

	private Map<Integer, List<Integer>> getSubmittedAnswersMap(Set<FormSubmitMultiChoice> choices) {
		Map<Integer, List<Integer>> submittedAnswersMap = new HashMap<>();
		for (FormSubmitMultiChoice choice : choices) {
			if (choice != null) {
				Integer labelId = choice.getFormLabel().getId();
				if (submittedAnswersMap.keySet().contains(labelId)) {
					submittedAnswersMap.get(labelId).add(choice.getFormLabelChoice().getId());
				} else {
					ArrayList<Integer> choiceList = new ArrayList<>();
					choiceList.add(choice.getFormLabelChoice().getId());
					submittedAnswersMap.put(labelId, choiceList);
				}
			}
		}
		return submittedAnswersMap;
	}

	private void uploadFile(FormSubmit formSubmit, Form form, Integer companyId,
			Set<FormSubmitSingleChoice> singleChoices, AmazonS3 amazonClient, String value, FormLabel formLabel,
			FormDataForLeadNotification formDataForLeadNotification) {
		String completeFilePath = mediaBasePath + value;
		File uploadedFile = new File(completeFilePath);
		if (uploadedFile.exists()) {
			AmazonWebModel amazonWebModel = new AmazonWebModel();
			amazonWebModel.setCompanyId(companyId);
			amazonWebModel.setFormId(form.getId());
			amazonWebModel.setFilePath(completeFilePath);
			amazonWebModel.setFileName(value.substring(value.lastIndexOf('/') + 1));
			String awsFilePath = amazonWebService.uploadFormContent(amazonWebModel, amazonClient);
			formDataForLeadNotification.setLabelValue(XamplifyUtils.getOrignialFileNameWithExtension(value));
			formDataForLeadNotification.setFile(true);
			setSingleChoiceData(formSubmit, singleChoices, awsFilePath, formLabel);
		} else {
			String errorMessage = "Unable to upload file to amazon" + completeFilePath + "::" + form.getId() + "::::"
					+ formLabel.getLabelName();
			logger.error(errorMessage);
		}
	}

	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public XtremandResponse update(FormSubmitDTO formSubmitDTO, Integer formSubmitId) {
		XtremandResponse response = new XtremandResponse();
		if (formSubmitId != null) {
			FormSubmit formSubmit = formSubmitDao.getFormSubmitById(formSubmitId);
			formSubmit.getSingleChoices().clear();
			formSubmit.getMultiChoices().clear();
			List<FormSubmitFieldsDTO> fieldsDTOs = formSubmitDTO.getFields();
			Set<FormSubmitSingleChoice> singleChoices = formSubmit.getSingleChoices();
			Set<FormSubmitMultiChoice> multiChoices = formSubmit.getMultiChoices();
			Map<Integer, MultipartFile> uploadedFiles = new HashMap<>();
			for (FormSubmitFieldsDTO fieldsDTO : fieldsDTOs) {
				String value = fieldsDTO.getValue();
				Integer formLabelId = fieldsDTO.getId();
				FormLabel formLabel = formDao.getFormLabelId(formLabelId);
				if (formLabel != null) {
					String labelType = formLabel.getLabelType().getLabelType();
					if (EMAIL.equals(labelType) || "text".equals(labelType) || "textarea".equals(labelType)
							|| "number".equals(labelType) || "date".equals(labelType) || "price".equals(labelType)
							|| "country".equals(labelType)) {
						setSingleChoiceData(formSubmit, singleChoices, value, formLabel);
					} else if (UPLOAD.equals(labelType)) {
						uploadFiles(formSubmit, singleChoices, uploadedFiles, fieldsDTO, formLabelId, formLabel);
					} else if (CHECK_BOX.equals(labelType)) {
						setCheckBoxChoicesData(formSubmit, multiChoices, fieldsDTO, formLabel, formLabelId, null);
					} else {
						setOtherChoicesData(formSubmit, multiChoices, value, formLabelId, formLabel, null);
					}
				}

			}
			formSubmit.setSingleChoices(singleChoices);
			formSubmit.setMultiChoices(multiChoices);
			response.setMessage("Your data updated successfully");
			response.setData(Collections.singletonMap("formSubmitId", formSubmit.getId()));
			response.setStatusCode(200);
		}
		return response;
	}

	private void uploadFiles(FormSubmit formSubmit, Set<FormSubmitSingleChoice> singleChoices,
			Map<Integer, MultipartFile> uploadedFiles, FormSubmitFieldsDTO fieldsDTO, Integer formLabelId,
			FormLabel formLabel) {
		String value;
		if (fieldsDTO.getFile() != null) {
			MultipartFile file = fieldsDTO.getFile();
			value = file.getOriginalFilename();
			setSingleChoiceData(formSubmit, singleChoices, value, formLabel);
			uploadedFiles.put(formLabelId, file);
		}
	}

	private void setCampaignOrPageFormData(FormSubmitDTO formSubmitDTO, XtremandResponse response,
			FormSubmit formSubmit) {

		/******* check if it is public form url/mdf form *************/
		boolean isPublicFormAlias = formDao.isFormExists(formSubmitDTO.getAlias().trim());
		if (isPublicFormAlias) {
			setFormSubmitEnumValue(formSubmitDTO, response, formSubmit);
		}
	}

	private void setFormSubmitEnumValue(FormSubmitDTO formSubmitDTO, XtremandResponse response, FormSubmit formSubmit) {
		if (FormTypeEnum.MDF_REQUEST_FORM.equals(formSubmitDTO.getFormType())) {
			formSubmit.setFormSubmitEnum(FormSubmitEnum.MDF_REQUEST_FORM);
		} else {
			formSubmit.setFormSubmitEnum(FormSubmitEnum.FORM);
		}
		response.setStatusCode(200);
	}

	private void setOtherChoicesData(FormSubmit formSubmit, Set<FormSubmitMultiChoice> multiChoices, String value,
			Integer formLabelId, FormLabel formLabel, FormDataForLeadNotification formDataForLeadNotification) {
		if (value != null && StringUtils.hasText(value.trim())) {
			FormSubmitMultiChoice multiChoice = new FormSubmitMultiChoice();
			multiChoice.setFormLabel(formLabel);
			multiChoice.setFormSubmit(formSubmit);
			Integer choiceId = Integer.parseInt(value);
			HashSet<FormLabelChoice> labelChoiceSet = (HashSet<FormLabelChoice>) formLabel.getFormLabelChoices()
					.stream().filter(choice -> (choice.getId().equals(choiceId)
							&& choice.getFormLabel().getId().equals(formLabelId)))
					.collect(Collectors.toSet());
			if (!labelChoiceSet.isEmpty()) {
				FormLabelChoice formLabelChoice = labelChoiceSet.iterator().next();
				multiChoice.setFormLabelChoice(formLabelChoice);
				multiChoices.add(multiChoice);
				if (formDataForLeadNotification != null) {
					formDataForLeadNotification.setLabelValue(formLabelChoice.getLabelChoiceName());
				}
			}
		}
	}

	private void setCheckBoxChoicesData(FormSubmit formSubmit, Set<FormSubmitMultiChoice> multiChoices,
			FormSubmitFieldsDTO fieldsDTO, FormLabel formLabel, Integer formLabelId,
			FormDataForLeadNotification formDataForLeadNotification) {
		List<Integer> dropDownIds = fieldsDTO.getDropdownIds();
		List<String> selectedValues = new ArrayList<>();
		if (dropDownIds != null && !dropDownIds.isEmpty()) {
			for (Integer dropdownId : dropDownIds) {
				FormSubmitMultiChoice dropdownChoice = new FormSubmitMultiChoice();
				dropdownChoice.setFormLabel(formLabel);
				dropdownChoice.setFormSubmit(formSubmit);
				HashSet<FormLabelChoice> labelChoice = (HashSet<FormLabelChoice>) formLabel.getFormLabelChoices()
						.stream().filter(choice -> (choice.getId().equals(dropdownId)
								&& choice.getFormLabel().getId().equals(formLabelId)))
						.collect(Collectors.toSet());
				if (!labelChoice.isEmpty()) {
					FormLabelChoice formLabelChoice = labelChoice.iterator().next();
					dropdownChoice.setFormLabelChoice(formLabelChoice);
					multiChoices.add(dropdownChoice);
					selectedValues.add(formLabelChoice.getLabelChoiceName());
				}
			}
		}

		if (formDataForLeadNotification != null && !selectedValues.isEmpty()) {
			formDataForLeadNotification.setLabelValue(String.join(",", selectedValues));
		}

	}

	private void setSingleChoiceData(FormSubmit formSubmit, Set<FormSubmitSingleChoice> singleChoices, String value,
			FormLabel formLabel) {
		if (value != null && StringUtils.hasText(value.trim())) {
			FormSubmitSingleChoice formSubmitSingleChoice = new FormSubmitSingleChoice();
			formSubmitSingleChoice.setFormLabel(formLabel);
			formSubmitSingleChoice.setFormSubmit(formSubmit);
			formSubmitSingleChoice.setValue(value);
			singleChoices.add(formSubmitSingleChoice);
		}

	}

	@SuppressWarnings("unchecked")
	@Override
	public XtremandResponse exportFormAnalyticsByFormAlias(String alias, Pagination pagination) {
		try {
			XtremandResponse response = new XtremandResponse();
			Form form = formDao.getFormIdByAlias(alias);
			Integer formId = form.getId();
			if (formId != null) {
				Authentication loggedInUser = SecurityContextHolder.getContext().getAuthentication();
				String userName = loggedInUser.getName();
				Integer userId = userDao.getUserIdByEmail(userName);
				List<String> availableFormAliases = formDao.listAliasesByUserId(userId);
				if (availableFormAliases.indexOf(alias) > -1) {
					List<Criteria> criterias = new ArrayList<>();
					Map<String, Object> hashMap = new HashMap<>();
					List<FormLabelDTO> columns = formService.listFormLabelsById(formId);
					hashMap.put("columns", columns);
					Map<String, Object> submittedDataMap = formSubmitDao.list(criterias, pagination, formId);
					List<FormSubmit> list = (List<FormSubmit>) submittedDataMap.get("formSubmitData");
					List<FormDataDto> formDataDtos = new ArrayList<>();
					for (FormSubmit formSubmit : list) {
						FormDataDto formDataDto = new FormDataDto();
						List<FormValue> formValues = new ArrayList<>();
						setSingleChoiceSubmittedData(formSubmit, formValues);
						setMulitChoiceSubmittedData(formSubmit, formValues);
						formDataDto.setValues(formValues);
						addMissingOrderIdsData(columns, formValues);
						Collections.sort(formValues);
						formDataDto.setExpanded(false);
						formDataDtos.add(formDataDto);
					}
					hashMap.put(SUBMITTED_DATA, formDataDtos);
					hashMap.put("totalRecords", submittedDataMap.get("totalRecords"));
					hashMap.put("formName", form.getFormName());
					response.setStatusCode(200);
					response.setData(hashMap);
				} else {
					ResponseUtil.getAccessDeniedResponse(response);
				}

			} else {
				ResponseUtil.getPageNotFoundResponse(response);
			}
			return response;

		} catch (XamplifyDataAccessException e) {
			throw new XamplifyDataAccessException(e.getMessage());
		}
	}

	private void addMissingOrderIdsData(List<FormLabelDTO> columns, List<FormValue> formValues) {
		List<Integer> columnOrderIds = columns.stream().map(FormLabelDTO::getOrder).collect(Collectors.toList());
		List<Integer> submittedValueOrderIds = formValues.stream().map(FormValue::getOrder)
				.collect(Collectors.toList());
		columnOrderIds.removeAll(submittedValueOrderIds);
		for (Integer missedOrderId : columnOrderIds) {
			FormValue missedFormValue = new FormValue();
			missedFormValue.setOrder(missedOrderId);
			missedFormValue.setValue("-");
			formValues.add(missedFormValue);
		}
	}

	private void setSingleChoiceSubmittedData(FormSubmit formSubmit, List<FormValue> formValues) {
		for (FormSubmitSingleChoice singleChoice : formSubmit.getSingleChoices()) {
			FormValue formValue = new FormValue();
			formValue.setOrder(singleChoice.getFormLabel().getOrder());
			formValue.setValue(singleChoice.getValue());
			formValues.add(formValue);
		}
	}

	private void setMulitChoiceSubmittedData(FormSubmit formSubmit, List<FormValue> formValues) {
		FormValue checkBoxFormValue = new FormValue();
		List<String> checkBoxChoices = new ArrayList<>();
		for (FormSubmitMultiChoice multiChoice : formSubmit.getMultiChoices()) {
			FormValue formValue = new FormValue();
			String choiceName = multiChoice.getFormLabelChoice().getLabelChoiceName();
			Integer order = multiChoice.getFormLabel().getOrder();
			if (!CHECK_BOX.equals(multiChoice.getFormLabel().getLabelType().getLabelType())) {
				formValue.setOrder(order);
				formValue.setValue(choiceName);
				formValues.add(formValue);
			} else {
				checkBoxChoices.add(choiceName);
				checkBoxFormValue.setOrder(order);
				if (!checkBoxChoices.isEmpty()) {
					String value = checkBoxChoices.stream().map(Object::toString).collect(Collectors.joining(","));
					checkBoxFormValue.setValue(value);
				}

			}
		}
		if (checkBoxFormValue.getOrder() > 0) {
			formValues.add(checkBoxFormValue);
		}
	}

	@Override
	public XtremandResponse listAnalyticsByFormAlias(String alias, Pagination pagination) {
		XtremandResponse response = new XtremandResponse();
		List<Criteria> criterias = new ArrayList<>();
		if (StringUtils.hasText(alias)) {
			getFormAnalytics(alias, pagination, response, criterias);
		}
		return response;
	}

	private void getFormAnalytics(String alias, Pagination pagination, XtremandResponse response,
			List<Criteria> criterias) {
		Form form = formDao.getFormIdByAlias(alias);
		if (form != null) {
			pagination.setCompanyId(form.getId());
			pagination.setCampaignType(form.getFormName());
			pagination.setFormName(form.getFormName());
			Integer userId = pagination.getUserId();
			List<String> availableFormAliases = formDao.listAliasesByUserId(userId);
			if (availableFormAliases.indexOf(alias) > -1 || pagination.isTotalLeads()) {
				Map<String, Object> formsMap = formSubmitDao.find(criterias, new FindLevel[] { FindLevel.SHALLOW },
						pagination);
				response.setData(formsMap);
				response.setStatusCode(200);
			} else {
				ResponseUtil.getAccessDeniedResponse(response);
			}
		} else {
			ResponseUtil.getPageNotFoundResponse(response);
		}
	}

	@Override
	public void exportPublicEventCampaignLeads(Integer campaignId, boolean isTotalAttendees,
			boolean isTotalPartnerLeads, HttpServletResponse httpResponse) {
		Pagination pagination = new Pagination();
		pagination.setTotalAttendees(isTotalAttendees);
		pagination.setTotalPartnerLeads(isTotalPartnerLeads);
		pagination.setTotalLeads(true);
		setPaginationObjectForExport(campaignId, httpResponse, pagination);

	}

	private void setPaginationObjectForExport(Integer campaignId, HttpServletResponse httpResponse,
			Pagination pagination) {
		List<Criteria> criterias = new ArrayList<>();
		pagination.setCampaignId(campaignId);
		pagination.setPageIndex(1);
		pagination.setExportToExcel(true);
		Map<String, Object> formsMap = formSubmitDao.find(criterias, new FindLevel[] { FindLevel.SHALLOW }, pagination);
		String reportName = "";
		if (pagination.isTotalAttendees() && !pagination.isCheckInLeads()) {
			reportName = "Total Attendees-" + campaignId + ".csv";
		} else if (pagination.isTotalAttendees() && pagination.isCheckInLeads()) {
			reportName = "Total Check-Ins-" + campaignId + ".csv";
		} else {
			if (pagination.isTotalPartnerLeads()) {
				reportName = "Total Partner Leads-" + campaignId + ".csv";
			} else {
				reportName = "Total Leads-" + campaignId + ".csv";
			}
		}
		prepareCsv(httpResponse, formsMap, reportName);
	}

	@SuppressWarnings("unchecked")
	/************ https://javahonk.com/spring-mvc-csv-download **************/
	private void prepareCsv(HttpServletResponse httpResponse, Map<String, Object> formsMap, String reportName) {
		try {
			httpResponse.setContentType(TEXT_CSV);
			httpResponse.setCharacterEncoding(UTF_8);
			httpResponse.setHeader(CONTENT_DISPOSITION, ATTACHMENT_AND_FILE_NAME + reportName);
			List<FormLabelDTO> labels = (List<FormLabelDTO>) formsMap.get("columns");
			List<FormDataDto> formDataDtos = (List<FormDataDto>) formsMap.get(SUBMITTED_DATA);
			List<String> columnHeaders = labels.stream().map(FormLabelDTO::getLabelName).collect(Collectors.toList());
			/**** XBI-2085 ***/
			List<String> updatedColumnHeaders = new ArrayList<>();
			for (String updatedColumnHeader : columnHeaders) {
				updatedColumnHeaders.add(XamplifyUtils.appendDQ(updatedColumnHeader));
			}
			/**** XBI-2085 ***/
			ArrayList<String> rows = new ArrayList<>();
			String arraylistToString = StringUtils.collectionToCommaDelimitedString(updatedColumnHeaders);
			rows.add(arraylistToString);
			rows.add("\n");
			for (int i = 0; i < formDataDtos.size(); i++) {
				List<String> data = formDataDtos.get(i).getValues().stream().map(FormValue::getValue)
						.collect(Collectors.toList());
				List<String> updatedData = XamplifyUtils.replaceCommaInString(data);
				rows.add(StringUtils.collectionToCommaDelimitedString(updatedData));
				rows.add("\n");
			}
			Iterator<String> iter = rows.iterator();
			while (iter.hasNext()) {
				String outputString = iter.next();
				httpResponse.getOutputStream().write(outputString.getBytes(StandardCharsets.UTF_8));
			}
			httpResponse.getOutputStream().flush();
		} catch (IOException e) {
			throw new XamplifyDataAccessException(e);
		}
	}

	@Override
	public XtremandResponse listTotalAttendessForCheckIn(Pagination pagination) {
		List<Criteria> criterias = new ArrayList<>();
		XtremandResponse response = new XtremandResponse();
		Integer campaignId = pagination.getCampaignId();
		String formAlias = formDao.getFormAliasByCampaignId(campaignId);
		if (StringUtils.hasText(formAlias)) {
			Form form = formDao.getFormIdByAlias(formAlias);
			pagination.setCompanyId(form.getId());
			Map<String, Object> formsMap = formSubmitDao.find(criterias, new FindLevel[] { FindLevel.SHALLOW },
					pagination);
			response.setData(formsMap);
			response.setStatusCode(200);
		} else {
			ResponseUtil.getPageNotFoundResponse(response);
		}
		return response;
	}

	@Override
	public void exportCheckInList(Integer campaignId, HttpServletResponse httpResponse) {
		Pagination pagination = new Pagination();
		pagination.setTotalAttendees(true);
		pagination.setTotalPartnerLeads(false);
		pagination.setTotalLeads(false);
		pagination.setCheckInLeads(true);
		setPaginationObjectForExport(campaignId, httpResponse, pagination);

	}

	@Override
	public void downloadFormAnalytics(String formAlias, Integer userId, String searchKey,
			HttpServletResponse httpResponse) {
		Pagination pagination = new Pagination();
		pagination.setSearchKey(searchKey);
		setFormAnalyticsDownloadUtilContent(formAlias, userId, httpResponse, pagination);
	}

	@SuppressWarnings("unchecked")
	private void setFormAnalyticsDownloadUtilContent(String formAlias, Integer userId, HttpServletResponse httpResponse,
			Pagination pagination) {
		pagination.setUserId(userId);
		pagination.setExportToExcel(true);
		pagination.setPageIndex(1);
		XtremandResponse response = listAnalyticsByFormAlias(formAlias, pagination);
		if (response.getStatusCode() == 200) {
			String formSubType = formDao.getFromSubType(formAlias);
			Map<String, Object> formsMap = (Map<String, Object>) response.getData();
			String fileName = "";
			if (formSubType.equals("QUIZ")) {
				fileName = "Quiz-form-data.csv";
			} else if (formSubType.equals("REGULAR")) {
				fileName = "Regular-form-data.csv";
			} else if (formSubType.equals("SURVEY")) {
				fileName = "Survey-form-data.csv";
			} else {
				fileName = "Form-Leads.csv";
			}
			prepareCsv(httpResponse, formsMap, fileName);
		}
	}

	@Override
	public void downloadLandingPageFormAnalytics(String formAlias, Integer landingPageId, Integer userId,
			HttpServletResponse httpResponse) {
		Pagination pagination = new Pagination();
		pagination.setLandingPageId(landingPageId);
		setFormAnalyticsDownloadUtilContent(formAlias, userId, httpResponse, pagination);

	}

	@Override
	public void downloadCampaignLandingPageFormAnalytics(String formAlias, Integer campaignId, Integer userId,
			HttpServletResponse httpResponse) {
		Pagination pagination = new Pagination();
		pagination.setCampaignId(campaignId);
		setFormAnalyticsDownloadUtilContent(formAlias, userId, httpResponse, pagination);
	}

	@Override
	public void downloadCampaignPartnerFormAnalytics(String formAlias, Integer campaignId, Integer partnerId,
			Integer userId, HttpServletResponse httpResponse) {
		Pagination pagination = new Pagination();
		pagination.setCampaignId(campaignId);
		pagination.setPartnerId(partnerId);
		setFormAnalyticsDownloadUtilContent(formAlias, userId, httpResponse, pagination);
	}

	@Override
	public void downloadPartnerLandingPageFormAnalytics(String landingPageAlias, Integer formId, Integer userId,
			HttpServletResponse httpResponse) {
		Pagination pagination = new Pagination();
		pagination.setFormId(formId);
		pagination.setLandingPageAlias(landingPageAlias);
		pagination.setPartnerLandingPageForm(true);
		setFormAnalyticsDownloadUtilContent(null, userId, httpResponse, pagination);
	}

	@Override
	public XtremandResponse uploadFile(MultipartFile uploadedFile, FormSubmitDTO formSubmitDTO) {
		XtremandResponse response = new XtremandResponse();
		if (uploadedFile.isEmpty()) {
			response.setStatusCode(404);
			response.setMessage("File is empty");
		} else {
			long size = uploadedFile.getSize();
			long fileSize = size / 1024;
			Integer maxFileSize = Integer.valueOf(maxFileSizeInString);
			if (fileSize > maxFileSize) {
				response.setStatusCode(400);
				response.setMessage("The maximum file size is 3 MB.");
			} else {
				String filePath = fileUtil.formFilePath(uploadedFile, formSubmitDTO);
				response.setStatusCode(200);
				response.setData(filePath);
			}

		}
		return response;

	}

	@Override
	public XtremandResponse saveLmsForm(FormSubmitDTO formSubmitDTO, MdfRequest mdfRequest) {
		XtremandResponse response = new XtremandResponse();
		formSubmitDTO.setFormType(FormTypeEnum.LMS_FORM);
		LearningTrack learningTrack = genericDAO.get(LearningTrack.class, formSubmitDTO.getLearningTrackId());
		Integer loggedInUserId = formSubmitDTO.getUserId();
		Integer loggedInCompanyId = userDao.getCompanyIdByUserId(loggedInUserId);
		LearningTrackContent content = lmsDAO.getContentByQuizID(learningTrack.getId(), formSubmitDTO.getId());
		if (content != null) {
			if (lMSServiceImpl.checkVisibility(learningTrack, loggedInCompanyId, loggedInUserId)) {
				return save(formSubmitDTO, mdfRequest);
			} else {
				response.setStatusCode(400);
				response.setMessage("Learning track is not available.");
				return response;
			}
		} else {
			response.setStatusCode(400);
			response.setMessage("Quiz is not associated to this Learning track.");
			return response;
		}
	}

	@Override
	public XtremandResponse getSurveyAnalytics(String alias, Integer campaignId, Integer partnerId) {
		XtremandResponse response = new XtremandResponse();
		SurveyAnalyticsDTO surveyAnalyticsDTO = retrieveSurveyAnalytics(alias, campaignId, partnerId);
		response.setStatusCode(200);
		response.setMessage(SUCCESS);
		response.setData(surveyAnalyticsDTO);
		return response;
	}

	private SurveyAnalyticsDTO retrieveSurveyAnalytics(String alias, Integer campaignId, Integer partnerId) {
		SurveyAnalyticsDTO surveyAnalyticsDTO = new SurveyAnalyticsDTO();
		if (!StringUtils.isEmpty(alias)) {
			Form form = formDao.getByAlias(alias);
			if (form != null && FormSubTypeEnum.SURVEY == form.getFormSubTypeEnum()) {
				surveyAnalyticsDTO.setFormName(form.getFormName());
				surveyAnalyticsDTO.setDescription(form.getDescription());
				Integer partnerCompanyId = null;
				if (partnerId != null && partnerId > 0) {
					partnerCompanyId = userDao.getCompanyIdByUserId(partnerId);
				}
				List<FormSubmit> submissions = formSubmitDao.getSubmissions(form.getId(), campaignId, partnerCompanyId);
				if (submissions != null && !submissions.isEmpty()) {
					analyzeSurveySubmissions(surveyAnalyticsDTO, form, submissions);
				}

			}
		}
		return surveyAnalyticsDTO;
	}

	private void analyzeSurveySubmissions(SurveyAnalyticsDTO surveyAnalyticsDTO, Form form,
			List<FormSubmit> submissions) {
		surveyAnalyticsDTO.setResponseCount(submissions.size());
		Map<Integer, Set<Integer>> answeredSubmissions = new HashMap<>();
		Map<Integer, Integer> labelTotalAnsweredCounts = new HashMap<>();
		Map<Integer, Map<Integer, SurveyChoiceSummaryDTO>> labelChoiceSummaries = new HashMap<>();
		Map<Integer, List<SurveyTextResponseDTO>> labelTextResponses = new HashMap<>();

		for (FormSubmit submission : submissions) {
			processMultiChoices(submission, answeredSubmissions, labelChoiceSummaries, labelTotalAnsweredCounts);
			processSingleChoices(submission, answeredSubmissions, labelTextResponses);
		}
		surveyAnalyticsDTO.setQuestionSummaries(prepareSurveyQuestionSummaries(form, labelChoiceSummaries,
				labelTextResponses, answeredSubmissions, labelTotalAnsweredCounts, submissions.size()));
	}

	private List<SurveyQuestionSummaryDTO> prepareSurveyQuestionSummaries(Form form,
			Map<Integer, Map<Integer, SurveyChoiceSummaryDTO>> labelChoiceSummaries,
			Map<Integer, List<SurveyTextResponseDTO>> labelTextResponses,
			Map<Integer, Set<Integer>> answeredSubmissions, Map<Integer, Integer> labelTotalAnsweredCounts,
			int totalSubmissionCount) {
		List<SurveyQuestionSummaryDTO> questionSummaryDTOList = null;
		List<FormLabel> labels = form.getFormLabels();
		if (labels != null && !labels.isEmpty()) {
			questionSummaryDTOList = new ArrayList<>();
			for (FormLabel label : labels) {
				SurveyQuestionSummaryDTO questionSummaryDTO = new SurveyQuestionSummaryDTO();
				questionSummaryDTO.setQuestion(label.getLabelName());
				Integer answeredCount = 0;
				Set<Integer> questionSubmissionList = answeredSubmissions.get(label.getId());
				if (questionSubmissionList != null) {
					answeredCount = questionSubmissionList.size();
				}
				questionSummaryDTO.setAnsweredCount(answeredCount);
				questionSummaryDTO.setSkippedCount(totalSubmissionCount - answeredCount);

				if (multiChoiceTypes.contains(label.getLabelType().getLabelType())) {
					populateChoiceSummariesForLabel(labelChoiceSummaries, labelTotalAnsweredCounts, label,
							questionSummaryDTO);
				} else {
					List<SurveyTextResponseDTO> textResponses = labelTextResponses.get(label.getId());
					if (textResponses == null) {
						textResponses = new ArrayList<>();
					}
					questionSummaryDTO.setTextResponses(textResponses);
				}
				questionSummaryDTOList.add(questionSummaryDTO);
			}
		}
		return questionSummaryDTOList;
	}

	private void populateChoiceSummariesForLabel(
			Map<Integer, Map<Integer, SurveyChoiceSummaryDTO>> labelChoiceSummaries,
			Map<Integer, Integer> labelTotalAnsweredCounts, FormLabel label,
			SurveyQuestionSummaryDTO questionSummaryDTO) {
		List<SurveyChoiceSummaryDTO> choiceSummaries = new ArrayList<>();
		Map<Integer, SurveyChoiceSummaryDTO> choiceSummaryMap = labelChoiceSummaries.get(label.getId());
		if (choiceSummaryMap != null) {
			List<FormLabelChoice> choices = label.getFormLabelChoices();
			for (FormLabelChoice choice : choices) {
				SurveyChoiceSummaryDTO surveyChoiceSummaryDTO = getSurveyChoiceSummaryDTO(choiceSummaryMap, choice);
				Integer choiceResponseCount = surveyChoiceSummaryDTO.getResponseCount();
				DecimalFormat df = new DecimalFormat("##.##");
				Float percentage = (float) (choiceResponseCount * 100.0 / labelTotalAnsweredCounts.get(label.getId()));
				surveyChoiceSummaryDTO.setResponsePercentage(Float.parseFloat(df.format(percentage)));
				choiceSummaries.add(surveyChoiceSummaryDTO);
			}
		}
		questionSummaryDTO.setChoiceSummaries(choiceSummaries);
	}

	private void processSingleChoices(FormSubmit submission, Map<Integer, Set<Integer>> answeredSubmissions,
			Map<Integer, List<SurveyTextResponseDTO>> labelTextResponses) {
		Set<FormSubmitSingleChoice> singleChoices = submission.getSingleChoices();
		if (singleChoices != null && !singleChoices.isEmpty()) {
			for (FormSubmitSingleChoice singleChoice : singleChoices) {
				if (singleChoice != null) {
					FormLabel formLabel = singleChoice.getFormLabel();
					addSubmissionsForLabel(answeredSubmissions, formLabel.getId(), submission.getId());
					List<SurveyTextResponseDTO> textResponseList = labelTextResponses.get(formLabel.getId());
					textResponseList = addLabelTextResponses(labelTextResponses, formLabel, textResponseList);
					SurveyTextResponseDTO surveyTextResponseDTO = new SurveyTextResponseDTO();
					String value = singleChoice.getValue();
					value = extractAndFormatUploadFileName(formLabel, surveyTextResponseDTO, value);
					surveyTextResponseDTO.setValue(value);
					surveyTextResponseDTO.setCreatedDateUTC(DateUtils.getUtcString(submission.getSubmittedOn()));
					surveyTextResponseDTO.setFormSubmitId(submission.getId());

					textResponseList.add(surveyTextResponseDTO);
				}
			}
		}
	}

	private String extractAndFormatUploadFileName(FormLabel formLabel, SurveyTextResponseDTO surveyTextResponseDTO,
			String value) {
		if (UPLOAD.equals(formLabel.getLabelType().getLabelType())) {
			surveyTextResponseDTO.setQuestionType(UPLOAD);
			surveyTextResponseDTO.setFilePath(value);
			value = value.substring(value.lastIndexOf("/") + 1);
			int timeStamp = value.lastIndexOf('_');
			String newValue = "";
			String extension = "";
			if (timeStamp >= 0) {
				newValue = value.substring(0, timeStamp);
				extension = value.substring(value.lastIndexOf('.'));
				value = newValue + extension;
			}
		}
		return value;
	}

	private List<SurveyTextResponseDTO> addLabelTextResponses(
			Map<Integer, List<SurveyTextResponseDTO>> labelTextResponses, FormLabel formLabel,
			List<SurveyTextResponseDTO> textResponseList) {
		if (textResponseList == null) {
			textResponseList = new ArrayList<>();
			labelTextResponses.put(formLabel.getId(), textResponseList);
		}
		return textResponseList;
	}

	private void processMultiChoices(FormSubmit submission, Map<Integer, Set<Integer>> answeredSubmissions,
			Map<Integer, Map<Integer, SurveyChoiceSummaryDTO>> labelChoiceSummaries,
			Map<Integer, Integer> labelTotalAnsweredCounts) {
		Set<FormSubmitMultiChoice> multiChoices = submission.getMultiChoices();
		if (multiChoices != null && !multiChoices.isEmpty()) {
			for (FormSubmitMultiChoice multiChoice : multiChoices) {
				if (multiChoice != null) {
					FormLabel formLabel = multiChoice.getFormLabel();
					addSubmissionsForLabel(answeredSubmissions, formLabel.getId(), submission.getId());
					if (labelTotalAnsweredCounts.containsKey(formLabel.getId())) {
						labelTotalAnsweredCounts.put(formLabel.getId(),
								labelTotalAnsweredCounts.get(formLabel.getId()) + 1);
					} else {
						labelTotalAnsweredCounts.put(formLabel.getId(), 1);
					}
					Map<Integer, SurveyChoiceSummaryDTO> choiceSummaries = getChoiceSummaryMap(labelChoiceSummaries,
							formLabel.getId());
					FormLabelChoice formLabelChoice = multiChoice.getFormLabelChoice();
					SurveyChoiceSummaryDTO surveyChoiceSummaryDTO = getSurveyChoiceSummaryDTO(choiceSummaries,
							formLabelChoice);
					surveyChoiceSummaryDTO.setResponseCount(surveyChoiceSummaryDTO.getResponseCount() + 1);
				}
			}
		}
	}

	private com.xtremand.form.dto.SurveyChoiceSummaryDTO getSurveyChoiceSummaryDTO(
			Map<Integer, SurveyChoiceSummaryDTO> choiceSummaries, FormLabelChoice formLabelChoice) {
		SurveyChoiceSummaryDTO surveyChoiceSummaryDTO = choiceSummaries.get(formLabelChoice.getId());
		if (surveyChoiceSummaryDTO == null) {
			surveyChoiceSummaryDTO = new SurveyChoiceSummaryDTO();
			surveyChoiceSummaryDTO.setChoice(formLabelChoice.getLabelChoiceName());
			choiceSummaries.put(formLabelChoice.getId(), surveyChoiceSummaryDTO);
		}
		return surveyChoiceSummaryDTO;
	}

	private void addSubmissionsForLabel(Map<Integer, Set<Integer>> answeredSubmissions, Integer labelId,
			Integer submissionId) {
		if (labelId != null && answeredSubmissions != null) {
			Set<Integer> submissionsSet = answeredSubmissions.get(labelId);
			if (submissionsSet == null) {
				submissionsSet = new HashSet<>();
				answeredSubmissions.put(labelId, submissionsSet);
			}
			submissionsSet.add(submissionId);
		}
	}

	private Map<Integer, SurveyChoiceSummaryDTO> getChoiceSummaryMap(
			Map<Integer, Map<Integer, SurveyChoiceSummaryDTO>> labelChoiceSummaries, Integer labelId) {
		Map<Integer, SurveyChoiceSummaryDTO> choiceSummaries = null;
		if (labelId != null && labelChoiceSummaries != null) {
			choiceSummaries = labelChoiceSummaries.get(labelId);
			if (choiceSummaries == null) {
				choiceSummaries = new HashMap<>();
				labelChoiceSummaries.put(labelId, choiceSummaries);
			}
		}
		return choiceSummaries;
	}

	@SuppressWarnings("unchecked")
	@Override
	public XtremandResponse getAnalytics(String alias, Pagination pagination) {
		XtremandResponse response = new XtremandResponse();
		String responseMessage = "Failed";
		Integer responseStatusCode = 400;
		if (StringUtils.hasText(alias)) {
			Form form = formDao.getFormIdByAlias(alias);
			if (form != null) {
				pagination.setFormId(form.getId());
				if (pagination.getPartnerId() != null && pagination.getPartnerId() > 0) {
					Integer partnerCompanyId = userDao.getCompanyIdByUserId(pagination.getPartnerId());
					pagination.setPartnerCompanyId(partnerCompanyId);
				}
				Map<String, Object> resultMap = formSubmitDao.getSubmissionDetails(pagination);
				if (resultMap != null && !resultMap.isEmpty() && resultMap.get("data") != null) {
					List<GeoLocationAnalytics> geoLocationAnalyticsList = (List<GeoLocationAnalytics>) resultMap
							.get("data");
					resultMap.put("data", getGeoLocationAnalyticsDtoList(geoLocationAnalyticsList));
					response.setData(resultMap);
				}
				responseMessage = SUCCESS;
				responseStatusCode = 200;
			}
		}
		response.setMessage(responseMessage);
		response.setStatusCode(responseStatusCode);
		return response;
	}

	private Object getGeoLocationAnalyticsDtoList(List<GeoLocationAnalytics> geoLocationAnalyticsList) {
		List<GeoLocationAnalyticsDTO> geoLocationAnalyticsDTOList = new ArrayList<>();
		if (geoLocationAnalyticsList != null && !geoLocationAnalyticsList.isEmpty()) {
			for (GeoLocationAnalytics geoLocationAnalytics : geoLocationAnalyticsList) {
				if (geoLocationAnalytics != null) {
					GeoLocationAnalyticsDTO geoLocationAnalyticsDTO = new GeoLocationAnalyticsDTO();
					BeanUtils.copyProperties(geoLocationAnalytics, geoLocationAnalyticsDTO);
					geoLocationAnalyticsDTO.setFormSubmitId(geoLocationAnalytics.getFormSubmit().getId());
					geoLocationAnalyticsDTO
							.setOpenedTimeInString(DateUtils.getUtcString(geoLocationAnalytics.getOpenedTime()));
					geoLocationAnalyticsDTOList.add(geoLocationAnalyticsDTO);
				}
			}
		}
		return geoLocationAnalyticsDTOList;
	}

	@Override
	public XtremandResponse getDetailedResponse(Integer formSubmitId) {
		XtremandResponse response = new XtremandResponse();
		FormSubmissionDTO formSubmissionDTO = detailedResponse(formSubmitId);
		response.setData(formSubmissionDTO);
		response.setMessage(SUCCESS);
		response.setStatusCode(200);
		return response;
	}

	private FormSubmissionDTO detailedResponse(Integer formSubmitId) {
		FormSubmissionDTO formSubmissionDTO = new FormSubmissionDTO();
		if (formSubmitId != null && formSubmitId > 0) {
			FormSubmit formSubmit = genericDAO.get(FormSubmit.class, formSubmitId);
			if (formSubmit != null) {
				Form form = formSubmit.getForm();
				Map<Integer, List<String>> multiChoiceAnswersMap = getMultiChoiceAnswersMap(formSubmit);
				Set<FormSubmitSingleChoice> singleChoiceAnswers = formSubmit.getSingleChoices();
				Map<Integer, String> singleChoiceAnswersMap = singleChoiceAnswers.stream()
						.collect(Collectors.toMap(a -> a.getFormLabel().getId(), FormSubmitSingleChoice::getValue));
				if (form != null) {
					formSubmissionDTO.setQuestionAndAnswers(
							getQuestionAndAnswerList(form, multiChoiceAnswersMap, singleChoiceAnswersMap));
					GeoLocationAnalytics geoLocationAnalytics = formSubmit.getGeoLocationAnalytics();
					if (geoLocationAnalytics != null) {
						GeoLocationAnalyticsDTO geoLocationAnalyticsDTO = new GeoLocationAnalyticsDTO();
						BeanUtils.copyProperties(geoLocationAnalytics, geoLocationAnalyticsDTO);
						geoLocationAnalyticsDTO.setFormSubmitId(formSubmit.getId());
						geoLocationAnalyticsDTO
								.setOpenedTimeInString(DateUtils.getUtcString(geoLocationAnalytics.getOpenedTime()));
						formSubmissionDTO.setGeoLocationAnalytics(geoLocationAnalyticsDTO);
					}
				}
			}
		}
		return formSubmissionDTO;
	}

	private List<SurveyTextResponseDTO> getQuestionAndAnswerList(Form form,
			Map<Integer, List<String>> multiChoiceAnswersMap, Map<Integer, String> singleChoiceAnswersMap) {
		List<SurveyTextResponseDTO> surveyTextResponseDTOList = new ArrayList<>();
		List<FormLabel> labels = form.getFormLabels();
		if (labels != null && !labels.isEmpty()) {
			for (FormLabel label : labels) {
				SurveyTextResponseDTO surveyTextResponseDTO = new SurveyTextResponseDTO();
				surveyTextResponseDTO.setQuestion(label.getLabelName());
				String value = "-----";
				if (multiChoiceTypes.contains(label.getLabelType().getLabelType())) {
					List<String> answers = multiChoiceAnswersMap.get(label.getId());
					if (answers != null && !answers.isEmpty()) {
						value = String.join(", ", answers);
					}
				} else {
					value = extractSingleChoiceAnswerValue(singleChoiceAnswersMap, label, surveyTextResponseDTO, value);
				}
				surveyTextResponseDTO.setValue(value);
				surveyTextResponseDTOList.add(surveyTextResponseDTO);
			}
		}
		return surveyTextResponseDTOList;
	}

	private String extractSingleChoiceAnswerValue(Map<Integer, String> singleChoiceAnswersMap, FormLabel label,
			SurveyTextResponseDTO surveyTextResponseDTO, String value) {
		if (!StringUtils.isEmpty(singleChoiceAnswersMap.get(label.getId()))) {
			value = singleChoiceAnswersMap.get(label.getId());
			value = extractAndFormatUploadFileName(label, surveyTextResponseDTO, value);
		}
		return value;
	}

	private Map<Integer, List<String>> getMultiChoiceAnswersMap(FormSubmit formSubmit) {
		Map<Integer, List<String>> multiChoiceAnswersMap = new HashMap<>();
		Set<FormSubmitMultiChoice> multiChoices = formSubmit.getMultiChoices();
		for (FormSubmitMultiChoice multiChoice : multiChoices) {
			Integer labelId = multiChoice.getFormLabel().getId();
			List<String> multiChoiceAnswerList = multiChoiceAnswersMap.get(labelId);
			if (multiChoiceAnswerList == null) {
				multiChoiceAnswerList = new ArrayList<>();
			}
			multiChoiceAnswerList.add(multiChoice.getFormLabelChoice().getLabelChoiceName());
			multiChoiceAnswersMap.put(labelId, multiChoiceAnswerList);
		}
		return multiChoiceAnswersMap;
	}

	@Override
	public Map<Integer, FormSubmitAnswerDTO> getQuizFormSubmittedData(Integer formSubmitId) {
		Map<Integer, FormSubmitAnswerDTO> data = new HashMap<>();
		if (formSubmitId != null) {
			List<FormSubmitMultiChoice> multiChoiceSubmittedData = formSubmitDao
					.getFormSubmitMultiChoiceData(formSubmitId);
			setQuizMultiChoiceData(data, multiChoiceSubmittedData);
			List<Object[]> singleChoiceSubmittedData = formSubmitDao.getFormSubmitSingleChoiceData(formSubmitId);
			setQuizSingleChoiceData(data, singleChoiceSubmittedData);
		}
		return data;
	}

	private void setQuizSingleChoiceData(Map<Integer, FormSubmitAnswerDTO> data, List<Object[]> singleChoices) {
		for (Object[] singleChoice : singleChoices) {
			FormSubmitAnswerDTO formSubmitAnswerDTO = new FormSubmitAnswerDTO();
			Integer labelId = (Integer) singleChoice[0];
			String value = (String) singleChoice[1];
			formSubmitAnswerDTO.setLabelId(labelId);
			formSubmitAnswerDTO.setSubmittedAnswer(value);
			data.put(labelId, formSubmitAnswerDTO);
		}
	}

	private void setQuizMultiChoiceData(Map<Integer, FormSubmitAnswerDTO> data,
			List<FormSubmitMultiChoice> multiChoices) {
		for (FormSubmitMultiChoice multiChoice : multiChoices) {
			if (multiChoice.getFormLabel() != null && multiChoice.getFormLabelChoice() != null) {
				Integer labelId = multiChoice.getFormLabel().getId();
				Integer labelChoiceId = multiChoice.getFormLabelChoice().getId();
				if (data.containsKey(labelId) && data.get(labelId) != null) {
					FormSubmitAnswerDTO formSubmitAnswerDTO = data.get(labelId);
					formSubmitAnswerDTO.getSubmittedAnswers().add(labelChoiceId);
				} else {
					FormSubmitAnswerDTO formSubmitAnswerDTO = new FormSubmitAnswerDTO();
					Set<Integer> submittedAnswers = new HashSet<>();
					formSubmitAnswerDTO.setLabelId(labelId);
					submittedAnswers.add(labelChoiceId);
					formSubmitAnswerDTO.setSubmittedAnswers(submittedAnswers);
					setCorrectChoiceIdsByLabelId(multiChoice, formSubmitAnswerDTO);
					data.put(labelId, formSubmitAnswerDTO);
				}
			}
		}
		for (Entry<Integer, FormSubmitAnswerDTO> answerDto : data.entrySet()) {
			if (answerDto != null && answerDto.getValue() != null) {
				answerDto.getValue().checkSubmittedAnswers();
			}
		}
	}

	private void setCorrectChoiceIdsByLabelId(FormSubmitMultiChoice multiChoice,
			FormSubmitAnswerDTO formSubmitAnswerDTO) {
		Set<Integer> correctAnswerLabelChoiceIds = new HashSet<>();
		for (FormLabelChoice choice : multiChoice.getFormLabel().getFormLabelChoices()) {
			if (choice.getFormQuizAnswer() != null) {
				correctAnswerLabelChoiceIds.add(choice.getId());
			}
		}
		formSubmitAnswerDTO.setCorrectAnswers(correctAnswerLabelChoiceIds);
	}

	/***** XNFR-467 *****/
	public HttpServletResponse downloadSurveyAnalytics(Integer formSubmitId, HttpServletResponse httpResponse) {
		FormSubmissionDTO formSubmissionDTO = new FormSubmissionDTO();
		if (formSubmitId != null && formSubmitId > 0) {
			FormSubmit formSubmit = genericDAO.get(FormSubmit.class, formSubmitId);
			if (formSubmit != null) {
				Form form = formSubmit.getForm();
				Map<Integer, List<String>> multiChoiceAnswersMap = getMultiChoiceAnswersMap(formSubmit);
				Set<FormSubmitSingleChoice> singleChoiceAnswers = formSubmit.getSingleChoices();
				Map<Integer, String> singleChoiceAnswersMap = singleChoiceAnswers.stream()
						.collect(Collectors.toMap(a -> a.getFormLabel().getId(), FormSubmitSingleChoice::getValue));
				if (form != null) {
					formSubmissionDTO.setQuestionAndAnswers(
							getQuestionAndAnswerList(form, multiChoiceAnswersMap, singleChoiceAnswersMap));
				}
			}
		}
		List<SurveyTextResponseDTO> surveyTextResponseDTO = formSubmissionDTO.getQuestionAndAnswers();
		String fileName = "Survey-Data.csv";
		return prepareSurveyCsv(httpResponse, surveyTextResponseDTO, fileName);
	}

	/***** XNFR-467 *****/
	private HttpServletResponse prepareSurveyCsv(HttpServletResponse httpResponse,
			List<SurveyTextResponseDTO> surveyTextResponseDTO, String fileName) {
		try {
			httpResponse.setContentType(TEXT_CSV);
			httpResponse.setCharacterEncoding(UTF_8);
			httpResponse.setHeader(CONTENT_DISPOSITION, ATTACHMENT_AND_FILE_NAME + fileName);

			CSVWriter writer = new CSVWriter(httpResponse.getWriter());
			List<String[]> data = new ArrayList<>();
			String[] questionArray = new String[surveyTextResponseDTO.size()];
			int index = 0;
			for (SurveyTextResponseDTO surveyQuestions : surveyTextResponseDTO) {
				questionArray[index++] = surveyQuestions.getQuestion();
			}
			data.add(questionArray);
			String[] answerArray = new String[surveyTextResponseDTO.size()];
			index = 0;
			for (SurveyTextResponseDTO surveyQuestions : surveyTextResponseDTO) {
				if (UPLOAD.equals(surveyQuestions.getQuestionType())) {
					String value = surveyQuestions.getValue();
					int timeStamp = value.lastIndexOf('_');
					String newValue = "";
					String extension = "";
					if (timeStamp >= 0) {
						newValue = value.substring(0, timeStamp);
						extension = value.substring(value.lastIndexOf('.'));
						value = newValue + extension;
					}
					answerArray[index++] = value;
				} else {
					answerArray[index++] = surveyQuestions.getValue();
				}
			}
			data.add(answerArray);

			writer.writeAll(data);
			writer.flush();
			writer.close();
		} catch (IOException e) {
			throw new XamplifyDataAccessException(e);
		}
		return httpResponse;
	}

	/***** XNFR-467 *****/
	@Override
	public XtremandResponse downloadSurveyAnalytics(String alias, HttpServletResponse httpResponse,
			Integer campaignId) {
		XtremandResponse response = new XtremandResponse();
		try {
			String fileName = "surveyanalytics.csv";
			SurveyAnalyticsDTO surveyAnalyticsDTO;
			if (XamplifyUtils.isValidInteger(campaignId)) {
				surveyAnalyticsDTO = retrieveSurveyAnalytics(alias, campaignId, null);
			} else {
				surveyAnalyticsDTO = retrieveSurveyAnalytics(alias, null, null);
			}
			List<SurveyQuestionSummaryDTO> questionSummaries = surveyAnalyticsDTO.getQuestionSummaries();
			httpResponse.setContentType(TEXT_CSV);
			httpResponse.setCharacterEncoding(UTF_8);
			httpResponse.setHeader(CONTENT_DISPOSITION, ATTACHMENT_AND_FILE_NAME + fileName);

			CSVWriter writer = new CSVWriter(httpResponse.getWriter());
			List<String[]> data = new ArrayList<>();
			String[] headers = new String[questionSummaries.size()];
			int index = 0;
			Integer row = 0;
			for (SurveyQuestionSummaryDTO questions : questionSummaries) {
				headers[index++] = questions.getQuestion();
			}
			data.add(headers);

			iterateDtoAndAddToRows(questionSummaries, data, row);
			writer.writeAll(data);
			writer.flush();
			writer.close();
			response.setStatusCode(200);
			response.setMessage(SUCCESS);
			response.setData(surveyAnalyticsDTO);
		} catch (Exception e) {
			logger.error("Error in downloadSurveyAnalytics()");
		}
		return response;
	}

	private void iterateDtoAndAddToRows(List<SurveyQuestionSummaryDTO> questionSummaries, List<String[]> data,
			Integer row) {
		for (SurveyQuestionSummaryDTO questions : questionSummaries) {
			List<SurveyTextResponseDTO> textResponses = questions.getTextResponses();
			if (!textResponses.isEmpty() && row == 0) {
				for (SurveyTextResponseDTO textResponse : textResponses) {
					Integer formSubmitId = textResponse.getFormSubmitId();
					FormSubmissionDTO formSubmissionDTO = detailedResponse(formSubmitId);
					List<SurveyTextResponseDTO> answers = formSubmissionDTO.getQuestionAndAnswers();
					List<String> rowList = new ArrayList<>();
					iterateAnswersAndAddToRowList(answers, rowList);
					String[] rows = rowList.toArray(new String[0]);
					data.add(rows);
				}
				row++;
			}
		}
	}

	private void iterateAnswersAndAddToRowList(List<SurveyTextResponseDTO> answers, List<String> rowList) {
		for (SurveyTextResponseDTO answer : answers) {
			if (UPLOAD.equals(answer.getQuestionType())) {
				String value = answer.getValue();
				int timeStamp = value.lastIndexOf('_');
				String newValue = "";
				String extension = "";
				if (timeStamp >= 0) {
					newValue = value.substring(0, timeStamp);
					extension = value.substring(value.lastIndexOf('.'));
					value = newValue + extension;
				}
				rowList.add(value);
			} else {
				rowList.add(answer.getValue().equals("-----") ? " " : answer.getValue());
			}

		}
	}

	@Override
	public List<UserDTO> formSubmissionUsers(Integer formId) {
		List<FormSubmit> formSubmits = formDao.findSubmittedDataByFormId(formId);
		List<UserDTO> userDtos = new ArrayList<>();
		for (FormSubmit formSubmit : formSubmits) {
			UserDTO userDto = convertFormSubmissionToUsers(formSubmit);
			addUserIfEmailNotExists(userDtos, userDto);
		}
		return userDtos;
	}

	private UserDTO convertFormSubmissionToUsers(FormSubmit formSubmit) {
		UserDTO userDTO = new UserDTO();
		Set<FormSubmitSingleChoice> formSubmitSingleChoices = formSubmit.getSingleChoices();
		for (FormSubmitSingleChoice formSubmitSingleChoice : formSubmitSingleChoices) {
			String value = formSubmitSingleChoice.getValue();
			FormLabel formLabel = formSubmitSingleChoice.getFormLabel();
			setContactData(userDTO, formLabel, value);
		}
		return userDTO;
	}

	private void addUserIfEmailNotExists(List<UserDTO> userDTOS, UserDTO userDTO) {
		List<String> addedEmailIds = userDTOS.stream().map(x -> x.getEmailId()).collect(Collectors.toList());
		String emailId = userDTO.getEmailId();
		boolean isValidEmailId = XamplifyUtils.isValidString(emailId);
		boolean isEmailIdAlreadyAdded = false;
		if (isValidEmailId) {
			String lowerCaseEmailId = emailId.trim().toLowerCase();
			isEmailIdAlreadyAdded = addedEmailIds.indexOf(lowerCaseEmailId) > -1;
		}
		if (!isEmailIdAlreadyAdded && isValidEmailId) {
			userDTOS.add(userDTO);
		}
	}

	public XtremandResponse convertFormSubmissionToUsersDto(Integer formSubmitId) {
		XtremandResponse response = new XtremandResponse();

		UserDTO userDTO = new UserDTO();
		List<FormSubmitFieldsValuesDTO> formSubmitData = formSubmitDao.getFormSubmitValueDetailsById(formSubmitId);
		boolean isMandatoryEmailPopulated = false;
		for (FormSubmitFieldsValuesDTO formSubmitFieldsValuesDTO : formSubmitData) {
			if (formSubmitFieldsValuesDTO.getLabelTypeId().equals(3) && formSubmitFieldsValuesDTO.isRequired()
					&& formSubmitFieldsValuesDTO.getValue() != null && !isMandatoryEmailPopulated) {
				isMandatoryEmailPopulated = true;
				userDTO.setEmailId(null);
			}
			String labelName = formSubmitFieldsValuesDTO.getLabelName().toLowerCase().trim();
			String value = formSubmitFieldsValuesDTO.getValue();
			setUserDtoByFormLabelName(userDTO, null, value, labelName, formSubmitFieldsValuesDTO.getLabelTypeId());
		}
		if (StringUtils.hasText(userDTO.getEmailId())) {
			Integer userId = userDao.getUserIdByEmail(userDTO.getEmailId());
			if (userId != null) {
				userDTO.setUserId(userId);
			}
		}
		response.setData(userDTO);
		return response;
	}

}