package com.xtremand.integration.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.xtremand.email.thread.dto.EmailRequestDTO;
import com.xtremand.formbeans.XtremandResponse;

public interface DefaultMailIntegrationService {
	
	boolean authorizeUser(Integer userId);
	
//	/**** XNFR-1105 ***/
//	XtremandResponse sendOrReply(EmailRequestDTO emailRequestDTO, List<MultipartFile> uploadedFiles);

	/**** XNFR-1105 **/
	XtremandResponse sendMail(EmailRequestDTO request, List<MultipartFile> uploadedFiles);

	XtremandResponse replyMail(EmailRequestDTO request, List<MultipartFile> uploadedFiles);

	XtremandResponse forwardMail(EmailRequestDTO request, List<MultipartFile> uploadedFiles);

}
