package com.xtremand.activity.service;

import java.util.List;

import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartFile;

import com.xtremand.activity.dto.ActivityAWSDTO;
import com.xtremand.activity.dto.EmailActivityRequestDTO;
import com.xtremand.formbeans.XtremandResponse;
import com.xtremand.util.dto.Pageable;

public interface EmailActivityService {

	XtremandResponse save(EmailActivityRequestDTO emailDTO, List<MultipartFile> attachments, BindingResult result);
	
	XtremandResponse fetchAllEmailActivities(Pageable pageable, Integer loggedInUserId, Integer contactId, Boolean isCompanyJourney, BindingResult result);
	
	XtremandResponse fetchEmailActivityById(Integer emailActivityId);
	
	void updateEmailActivity(Integer emailActivityId);
	
	XtremandResponse fetchLoggedInUserEmailId(Integer loggedInUserId);
	
	XtremandResponse sendTestMail(EmailActivityRequestDTO emailDTO, List<MultipartFile> attachments, BindingResult result);
	
	void updateEmailAttachmentFilePath(Integer id, String awsFilePath);
	
	List<Integer> saveEmailActivityAttachment(EmailActivityRequestDTO emailRequestDTO,
			ActivityAWSDTO emailActivityAWSDTO, MultipartFile uploadedFile);
	
	EmailActivityRequestDTO getEmailAddressByUserId(Integer userId);

	XtremandResponse fetchWelcomeEmailActivityById(Integer emailActivityId);
}
