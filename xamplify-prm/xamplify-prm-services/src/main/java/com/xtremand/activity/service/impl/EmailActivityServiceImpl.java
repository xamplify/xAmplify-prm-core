package com.xtremand.activity.service.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jcodec.common.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartFile;

import com.xtremand.activity.bom.EmailActivity;
import com.xtremand.activity.bom.EmailRecipient;
import com.xtremand.activity.bom.EmailRecipientEnum;
import com.xtremand.activity.dao.EmailActivityDAO;
import com.xtremand.activity.dto.ActivityAWSDTO;
import com.xtremand.activity.dto.ActivityAttachmentDTO;
import com.xtremand.activity.dto.EmailActivityDTO;
import com.xtremand.activity.dto.EmailActivityRequestDTO;
import com.xtremand.activity.dto.EmailActivityStatusEnum;
import com.xtremand.activity.dto.EmailMergeTagDTO;
import com.xtremand.activity.service.EmailActivityService;
import com.xtremand.activity.validator.EmailActivityValidator;
import com.xtremand.aws.AmazonWebService;
import com.xtremand.aws.CopiedFileDetails;
import com.xtremand.common.bom.CompanyProfile;
import com.xtremand.common.bom.Pagination;
import com.xtremand.dao.util.GenericDAO;
import com.xtremand.formbeans.XtremandResponse;
import com.xtremand.user.bom.User;
import com.xtremand.user.dao.UserDAO;
import com.xtremand.util.FileUtil;
import com.xtremand.util.XamplifyUtils;
import com.xtremand.util.dao.HibernateSQLQueryResultUtilDao;
import com.xtremand.util.dto.HibernateSQLQueryResultRequestDTO;
import com.xtremand.util.dto.Pageable;
import com.xtremand.util.dto.QueryParameterDTO;
import com.xtremand.util.dto.XamplifyUtilValidator;
import com.xtremand.util.service.ThymeLeafService;
import com.xtremand.util.service.UtilService;

@Service
@Transactional
public class EmailActivityServiceImpl implements EmailActivityService {

	@Autowired
	private EmailActivityValidator emailValidator;

	@Autowired
	private XamplifyUtilValidator xamplifyUtilValidator;

	@Autowired
	private GenericDAO genericDao;

	@Autowired
	private HibernateSQLQueryResultUtilDao hibernateSQLQueryResultUtilDao;

	@Autowired
	private ThymeLeafService thymeLeafService;

	@Autowired
	private EmailActivityDAO emailActivityDAO;

	@Autowired
	private UserDAO userDAO;

	@Autowired
	private UtilService utilService;

	@Autowired
	private FileUtil fileUtil;

	@Autowired
	private AmazonWebService amazonWebService;

	@Value("${parameter.loggedInUserId.invalid}")
	private String invalidLoggedInUserId;

	@Value("${email.activity.attachments}")
	private String attachmentPath;

	@Override
	public XtremandResponse save(EmailActivityRequestDTO emailRequestDTO, List<MultipartFile> attachments,
			BindingResult result) {
		XtremandResponse response = new XtremandResponse();
		emailValidator.validateProperties(emailRequestDTO, attachments, result);
		if (result.hasErrors()) {
			xamplifyUtilValidator.addErrorResponse(result, response);
		} else {
			Map<Integer, Integer> map = saveEmail(emailRequestDTO);
			emailRequestDTO.setIds(new ArrayList<>(map.values()));
			uploadFiles(emailRequestDTO, attachments, response);
			saveCCAndBCCEmailIds(emailRequestDTO);
			sendEmail(emailRequestDTO, attachments, map);
			XamplifyUtils.addSuccessStatusWithMessage(response, "Email sent successfully");
		}
		return response;
	}

	private XtremandResponse uploadFiles(EmailActivityRequestDTO emailRequestDTO, List<MultipartFile> attachments,
			XtremandResponse response) {
		try {
			List<ActivityAWSDTO> emailActivityAWSDTOs = new ArrayList<>();
			if (XamplifyUtils.isNotEmptyList(attachments)) {
				for (MultipartFile uploadedFile : attachments) {
					emailActivityAWSDTOs.add(upload(uploadedFile, emailRequestDTO));
				}
			}
			response.setData(emailActivityAWSDTOs);
		} catch (Exception e) {
			Logger.debug(e.getMessage());
		}
		return response;
	}

	private ActivityAWSDTO upload(MultipartFile uploadedFile, EmailActivityRequestDTO emailRequestDTO)
			throws IOException {
		ActivityAWSDTO emailActivityAWSDTO = new ActivityAWSDTO();
		copyFilesAndUpload(uploadedFile, emailRequestDTO, emailActivityAWSDTO);
		List<Integer> attachmentIds = saveEmailActivityAttachment(emailRequestDTO, emailActivityAWSDTO, uploadedFile);
		emailActivityAWSDTO.setAttachmentIds(attachmentIds);
		return emailActivityAWSDTO;
	}

	@Override
	public List<Integer> saveEmailActivityAttachment(EmailActivityRequestDTO emailRequestDTO,
			ActivityAWSDTO emailActivityAWSDTO, MultipartFile uploadedFile) {
		return Collections.emptyList();
	}

	private void copyFilesAndUpload(MultipartFile uploadedFile, EmailActivityRequestDTO emailRequestDTO,
			ActivityAWSDTO emailActivityAWSDTO) throws IOException {
		CopiedFileDetails copiedAwsFileDetails = new CopiedFileDetails();
		copiedAwsFileDetails.setIsFromEmailActivity(true);
		String filePathSuffix = attachmentPath + emailRequestDTO.getCompanyId();
		getCopiedAwsFileDetails(uploadedFile, emailRequestDTO, emailActivityAWSDTO, copiedAwsFileDetails,
				filePathSuffix);
	}

	private void getCopiedAwsFileDetails(MultipartFile uploadedFile, EmailActivityRequestDTO emailRequestDTO,
			ActivityAWSDTO emailActivityAWSDTO, CopiedFileDetails copiedAwsFileDetails, String filePathSuffix)
			throws IOException {
		String fileName = uploadedFile.getOriginalFilename();
		amazonWebService.copyFileToXamplifyServer(uploadedFile, emailRequestDTO.getLoggedInUserId(), filePathSuffix,
				fileName, copiedAwsFileDetails, false);
		String completeFileName = copiedAwsFileDetails.getCompleteName();
		completeFileName = completeFileName.substring(0, completeFileName.lastIndexOf('.')) + "."
				+ (completeFileName.substring(completeFileName.lastIndexOf('.') + 1)).toLowerCase();
		emailActivityAWSDTO.setCompleteFileName(completeFileName);
		emailActivityAWSDTO.setCompanyId(emailRequestDTO.getCompanyId());
		emailActivityAWSDTO.setEmailActivityId(emailRequestDTO.getIds().get(0));
		emailActivityAWSDTO.setUserId(emailRequestDTO.getLoggedInUserId());
		String fileType = fileUtil.getFileExtension(uploadedFile);
		emailActivityAWSDTO.setFileType(fileType);
		emailActivityAWSDTO.setFileName(fileName);
		emailActivityAWSDTO.setFilePath(copiedAwsFileDetails.getCopiedImageFilePath());
		emailActivityAWSDTO.setUpdatedFileName(copiedAwsFileDetails.getUpdatedFileName());
		emailActivityAWSDTO.setTemporaryFilePath(copiedAwsFileDetails.getCopiedImageFilePath());
	}

	private void sendEmail(EmailActivityRequestDTO emailRequestDTO, List<MultipartFile> attachments,
			Map<Integer, Integer> emailActivityIdsMap) {
		Integer loggedInUserId = emailRequestDTO.getLoggedInUserId();
		EmailActivityRequestDTO responseEmailRequestDTO = getEmailAddressByUserId(loggedInUserId);
		emailRequestDTO.setFirstName(responseEmailRequestDTO.getFirstName());
		emailRequestDTO.setLastName(responseEmailRequestDTO.getLastName());
		emailRequestDTO.setFromEmailId(responseEmailRequestDTO.getFromEmailId());
		thymeLeafService.sendMailToUser(emailRequestDTO, attachments, emailActivityIdsMap);
	}

	@Override
	public EmailActivityRequestDTO getEmailAddressByUserId(Integer userId) {
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		String queryString = "select firstname as \"firstName\", lastname as \"lastName\", middle_name as \"middleName\", email_id as \"FromEmailId\""
				+ " from xt_user_profile where user_id = :userId";
		hibernateSQLQueryResultRequestDTO.setQueryString(queryString);
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO("userId", userId));
		return (EmailActivityRequestDTO) hibernateSQLQueryResultUtilDao.getDto(hibernateSQLQueryResultRequestDTO,
				EmailActivityRequestDTO.class);
	}

	private Map<Integer, Integer> saveEmail(EmailActivityRequestDTO emailRequestDTO) {
		Map<Integer, Integer> emailActivityIdsMap = new HashMap<>();
		String body = null;
		String subject = emailRequestDTO.getSubject();
		if (XamplifyUtils.isValidInteger(emailRequestDTO.getTemplateId())) {
			body = emailActivityDAO.getTemplateById(emailRequestDTO.getTemplateId());
			emailRequestDTO.setBody(body);
		} else {
			body = emailRequestDTO.getBody();
		}
		for (Integer userId : emailRequestDTO.getUserIds()) {
			EmailActivity email = new EmailActivity();
			email.setBody(body);
			email.setSubject(subject);
			Integer loggedInUserId = emailRequestDTO.getLoggedInUserId();
			User sender = new User();
			sender.setUserId(loggedInUserId);
			email.setSender(sender);
			User recipient = new User();
			recipient.setUserId(userId);
			email.setRecipient(recipient);
			email.setCreatedTime(new Date());
			CompanyProfile companyProfile = new CompanyProfile();
			Integer companyId = userDAO.getCompanyIdByUserId(loggedInUserId);
			companyProfile.setId(companyId);
			emailRequestDTO.setCompanyId(companyId);
			email.setCompanyProfile(companyProfile);
			email.setStatus(EmailActivityStatusEnum.DELIVERED);
			Integer taskId = genericDao.save(email);
			emailActivityIdsMap.put(userId, taskId);
		}
		return emailActivityIdsMap;
	}

	@Override
	public XtremandResponse fetchAllEmailActivities(Pageable pageable, Integer loggedInUserId, Integer contactId,
			Boolean isCompanyJourney, BindingResult result) {
		XtremandResponse response = new XtremandResponse();
		emailValidator.validateDataToFetchAllEmailActivities(pageable, loggedInUserId, contactId, result);
		if (result.hasErrors()) {
			xamplifyUtilValidator.addErrorResponse(result, response);
		} else {
			Pagination pagination = utilService.setPageableParameters(pageable, loggedInUserId);
			pagination.setContactId(contactId);
			pagination.setIsCompanyJourney(isCompanyJourney);
			Map<String, Object> map = emailActivityDAO.fetchAllEmailActivities(pagination, pagination.getSearchKey());
			response.setData(map);
			XamplifyUtils.addSuccessStatus(response);
		}
		return response;
	}

	@Override
	public XtremandResponse fetchEmailActivityById(Integer emailActivityId) {
		XtremandResponse response = new XtremandResponse();
		if (XamplifyUtils.isValidInteger(emailActivityId)) {
			EmailActivityDTO emailActivityDto = emailActivityDAO.fetchEmailActivityById(emailActivityId);
			List<ActivityAttachmentDTO> emailAttachmentDTOs = emailActivityDAO.fetchEmailAttachments(emailActivityId);
			emailActivityDto.setEmailAttachmentDTOs(emailAttachmentDTOs);
			response.setData(emailActivityDto);
			XamplifyUtils.addSuccessStatus(response);
		}
		return response;
	}

	@Override
	public void updateEmailActivity(Integer emailActivityId) {
		if (XamplifyUtils.isValidInteger(emailActivityId)) {
			HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
			String queryString = "update xt_email_activity set status = 'OPENED', opened_time = :openedTime where id = :emailActivityId";
			hibernateSQLQueryResultRequestDTO.setQueryString(queryString);
			hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
					.add(new QueryParameterDTO("emailActivityId", emailActivityId));
			hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
					.add(new QueryParameterDTO("openedTime", new Date()));
			hibernateSQLQueryResultUtilDao.update(hibernateSQLQueryResultRequestDTO);
		}
	}

	@Override
	public XtremandResponse fetchLoggedInUserEmailId(Integer loggedInUserId) {
		XtremandResponse response = new XtremandResponse();
		if (XamplifyUtils.isValidInteger(loggedInUserId)) {
			HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
			String queryString = "select email_id from xt_user_profile where user_id = :loggedInUserId";
			hibernateSQLQueryResultRequestDTO.setQueryString(queryString);
			hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
					.add(new QueryParameterDTO("loggedInUserId", loggedInUserId));
			String emailId = (String) hibernateSQLQueryResultUtilDao.getUniqueResult(hibernateSQLQueryResultRequestDTO);
			response.setData(emailId);
			XamplifyUtils.addSuccessStatus(response);
		} else {
			XamplifyUtils.addErorMessageWithStatusCode(response, invalidLoggedInUserId, 401);
		}
		return response;
	}

	@Override
	public XtremandResponse sendTestMail(EmailActivityRequestDTO emailDTO, List<MultipartFile> attachments,
			BindingResult result) {
		XtremandResponse response = new XtremandResponse();
		emailValidator.validateProperties(emailDTO, attachments, result);
		if (result.hasErrors()) {
			xamplifyUtilValidator.addErrorResponse(result, response);
		} else {
			sendTestEmail(emailDTO, attachments);
			XamplifyUtils.addSuccessStatusWithMessage(response, "Email sent successfully");
		}
		return response;
	}

	private void saveCCAndBCCEmailIds(EmailActivityRequestDTO emailRequestDTO) {
		if (XamplifyUtils.isNotEmptyList(emailRequestDTO.getIds())) {
			for (Integer id : emailRequestDTO.getIds()) {
				EmailActivity emailActivity = new EmailActivity();
				emailActivity.setId(id);
				saveEmailIds(emailRequestDTO.getCcEmailIds(), EmailRecipientEnum.CC, emailActivity);
				saveEmailIds(emailRequestDTO.getBccEmailIds(), EmailRecipientEnum.BCC, emailActivity);
			}
		}
	}

	private void saveEmailIds(List<String> emailIds, EmailRecipientEnum recipientType, EmailActivity emailActivity) {
		if (XamplifyUtils.isNotEmptyList(emailIds)) {
			for (String emailId : emailIds) {
				saveEmailId(emailId, recipientType, emailActivity);
			}
		}
	}

	private void saveEmailId(String emailId, EmailRecipientEnum emailRecipientEnum, EmailActivity emailActivity) {
		EmailRecipient emailRecipient = new EmailRecipient();
		emailRecipient.setEmailId(emailId);
		emailRecipient.setEmailActivity(emailActivity);
		emailRecipient.setEmailRecipientEnum(emailRecipientEnum);
		genericDao.save(emailRecipient);
	}

	@Override
	public void updateEmailAttachmentFilePath(Integer id, String awsFilePath) {
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		String queryString = "update xt_activity_attachment set file_path = :awsFilePath, temporary_file_path = null where id = :id";
		hibernateSQLQueryResultRequestDTO.setQueryString(queryString);
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO("id", id));
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
				.add(new QueryParameterDTO("awsFilePath", awsFilePath));
		hibernateSQLQueryResultUtilDao.update(hibernateSQLQueryResultRequestDTO);
	}

	private void sendTestEmail(EmailActivityRequestDTO emailRequestDTO, List<MultipartFile> attachments) {
		Integer loggedInUserId = emailRequestDTO.getLoggedInUserId();
		Integer contactId = emailRequestDTO.getUserIds().get(0);
		EmailActivityRequestDTO responseEmailRequestDTO = getEmailAddressByUserId(loggedInUserId);
		emailRequestDTO.setFirstName(responseEmailRequestDTO.getFirstName());
		emailRequestDTO.setLastName(responseEmailRequestDTO.getLastName());
		emailRequestDTO.setFromEmailId(responseEmailRequestDTO.getFromEmailId());
		EmailMergeTagDTO emailMergeTagDTO = emailActivityDAO.fetchEmailMergeTagsData(loggedInUserId, contactId);
		thymeLeafService.sendTestMailToUser(emailRequestDTO, attachments, emailMergeTagDTO);
	}

	@Override
	public XtremandResponse fetchWelcomeEmailActivityById(Integer emailActivityId) {
		XtremandResponse response = new XtremandResponse();
		if (XamplifyUtils.isValidInteger(emailActivityId)) {
			EmailActivityDTO emailActivityDto = emailActivityDAO.fetchWelcomeEmailActivityById(emailActivityId);
			response.setData(emailActivityDto);
			XamplifyUtils.addSuccessStatus(response);
		}
		return response;
	}

}
