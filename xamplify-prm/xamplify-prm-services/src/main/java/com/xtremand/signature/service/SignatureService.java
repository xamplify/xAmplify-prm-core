package com.xtremand.signature.service;

import java.io.IOException;

import org.springframework.web.multipart.MultipartFile;

import com.signature.dto.SignatureDTO;
import com.xtremand.formbeans.XtremandResponse;

public interface SignatureService {

	XtremandResponse saveTypedSignature(SignatureDTO signatureDTO);

	XtremandResponse getExistingSignatures(Integer loggedInUserId) throws IOException;
	
	XtremandResponse saveDrawSignature(SignatureDTO signatureDTO);

	String uploadFile(MultipartFile file, Integer userId) throws IOException;

	XtremandResponse removeExistingSignature(SignatureDTO signatureDTO);
	
}
