package com.xtremand.signature.service.impl;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.signature.dto.SignatureDTO;
import com.signature.dto.SignatureResponseDTO;
import com.xtremand.formbeans.XtremandResponse;
import com.xtremand.signature.dao.SignatureDao;
import com.xtremand.signature.service.SignatureService;
import com.xtremand.user.dao.UserDAO;
import com.xtremand.util.FileUtil;
import com.xtremand.util.XamplifyUtil;
import com.xtremand.util.XamplifyUtils;
import com.xtremand.util.dto.XamplifyConstants;

@Service
@Transactional
public class SignatureServiceImpl implements SignatureService {

	private static final String SIGNATURE_ADDED_SUCCESSFULLY = "Signature added successfully";

	private static final String DRAW_SIGNATURE_PNG = "draw-signature.png";
	
	private static final String TYPED_SINATURE_PNG = "typed-signature.png";

	@Value("${separator}")
	String sep;

	@Value("${dev.host}")
	String devHost;

	@Value("${prod.host}")
	String productionHost;


	@Value("${images.folder}")
	String vod;

	@Value("${spring.profiles.active}")
	private String profiles;

	@Autowired
	private SignatureDao signatureDao;

	@Autowired
	private FileUtil fileUtil;

	@Autowired
	private XamplifyUtil xamplifyUtil;

	@Autowired
	private UserDAO userDao;

	private static final String HOST = "http://localhost:8000/";

	private static final String SIGNATURE_PREFIX_PATH = "signatures";

	@Override
	public XtremandResponse saveTypedSignature(SignatureDTO signatureRequestDTO) {
		XtremandResponse response = new XtremandResponse();
		String typedSignatureImagePath = writeTypedSignatureToFile(signatureRequestDTO);
		if (!XamplifyConstants.ERROR.equals(typedSignatureImagePath)) {
			XamplifyUtils.addSuccessStatusWithMessage(response, SIGNATURE_ADDED_SUCCESSFULLY);
			signatureDao.updateTypedSignaturePath(signatureRequestDTO.getLoggedInUserId(), TYPED_SINATURE_PNG);
		}
		if (!StringUtils.hasText(signatureRequestDTO.getTypedSignatureText())) {
			signatureRequestDTO.setTypedSignatureFont("");
		}
		signatureDao.saveTypedSignature(signatureRequestDTO);
		XamplifyUtils.addSuccessStatusWithMessage(response, SIGNATURE_ADDED_SUCCESSFULLY);
		return response;
	}

	private String writeTypedSignatureToFile(SignatureDTO signatureRequestDTO) {
		String base64Data = signatureRequestDTO.getTypedSignatureEncodedImage().split(",")[1];
		byte[] imageBytes = Base64.getDecoder().decode(base64Data);
		Integer loggedInUserId = signatureRequestDTO.getLoggedInUserId();
		String userAlias = userDao.getAliasByUserId(loggedInUserId);
		String signaturePath = fileUtil.createSignatureFolderPath(userAlias);
		String completeSignaturePath = signaturePath + sep + TYPED_SINATURE_PNG;
		try (OutputStream outputStream = new FileOutputStream(completeSignaturePath)) {
			outputStream.write(imageBytes);
		} catch (FileNotFoundException fnf) {
			fnf.printStackTrace();
			return XamplifyConstants.ERROR;
		} catch (IOException ioe) {
			ioe.printStackTrace();
			return XamplifyConstants.ERROR;
		}
		return completeSignaturePath;
	}

	@Override
	public XtremandResponse getExistingSignatures(Integer loggedInUserId) throws IOException {
		XtremandResponse response = new XtremandResponse();
		SignatureResponseDTO signatureResponseDTO = signatureDao.getExistingSignaturesByLoggedInUserId(loggedInUserId);
		if (signatureResponseDTO != null) {
			String userAlias = userDao.getAliasByUserId(loggedInUserId);
			handleDrawSignatureImagePath(signatureResponseDTO, userAlias);

			handleTypedSignature(signatureResponseDTO, userAlias);

			handledUploadedSignatureImagePath(signatureResponseDTO, userAlias);

		}
		response.setData(signatureResponseDTO);
		XamplifyUtils.addSuccessStatus(response);
		return response;
	}

	private void handledUploadedSignatureImagePath(SignatureResponseDTO signatureResponseDTO, String userAlias) {
		String uploadedSignatureImagePath = signatureResponseDTO.getUploadedSignatureImagePath();
		boolean isUploadedSignatureExists = XamplifyUtils.isValidString(uploadedSignatureImagePath);
		signatureResponseDTO.setUploadedSignatureExits(isUploadedSignatureExists);
		if (isUploadedSignatureExists) {
			String completedUploadedImagePath = "";
			String signaturePrefixPath = SIGNATURE_PREFIX_PATH + sep + userAlias + sep + uploadedSignatureImagePath;
			if (xamplifyUtil.isDev()) {
				completedUploadedImagePath = HOST + signaturePrefixPath;
			} else if (xamplifyUtil.isQA()) {
				completedUploadedImagePath = devHost + vod + signaturePrefixPath;
			} else if (xamplifyUtil.isProduction()) {
				completedUploadedImagePath = productionHost + vod + signaturePrefixPath;
			}
			signatureResponseDTO.setUploadedSignatureImagePath(completedUploadedImagePath);
		}
	}

	private void handleTypedSignature(SignatureResponseDTO signatureResponseDTO, String userAlias) {
		String typedSignatureImagePath = signatureResponseDTO.getTypedSignatureImagePath();
		String typedSignatureFont = signatureResponseDTO.getTypedSignatureFont();
		String typedSignatureText = signatureResponseDTO.getTypedSignatureText();
		boolean isTypedSignatureExists = XamplifyUtils.isValidString(typedSignatureText)
				&& XamplifyUtils.isValidString(typedSignatureFont);
		signatureResponseDTO.setTypedSignatureExists(isTypedSignatureExists);
		if (isTypedSignatureExists) {
			String completedTextImagePath = "";
			String signaturePrefixPath = SIGNATURE_PREFIX_PATH + sep + userAlias + sep + typedSignatureImagePath;
			if (xamplifyUtil.isDev()) {
				completedTextImagePath = HOST + signaturePrefixPath;
			} else if (xamplifyUtil.isQA()) {
				completedTextImagePath = devHost + vod + signaturePrefixPath;
			} else if (xamplifyUtil.isProduction()) {
				completedTextImagePath = productionHost + vod + signaturePrefixPath;
			}
			signatureResponseDTO.setTypedSignatureImagePath(completedTextImagePath);
		}
	}

	private void handleDrawSignatureImagePath(SignatureResponseDTO signatureResponseDTO, String userAlias) {
		String drawSignatureImagePath = signatureResponseDTO.getDrawSignatureImagePath();
		boolean drawSignatureExits = XamplifyUtils.isValidString(drawSignatureImagePath);
		signatureResponseDTO.setDrawSignatureExits(drawSignatureExits);
		if (drawSignatureExits) {
			String completeDrawSignatureImagePath = "";
			String signaturePrefixPath = SIGNATURE_PREFIX_PATH + sep + userAlias + sep + drawSignatureImagePath;
			if (xamplifyUtil.isDev()) {
				completeDrawSignatureImagePath = HOST + signaturePrefixPath;
			} else if (xamplifyUtil.isQA()) {
				completeDrawSignatureImagePath = devHost + vod + signaturePrefixPath;
			} else if (xamplifyUtil.isProduction()) {
				completeDrawSignatureImagePath = productionHost + vod + signaturePrefixPath;
			}
			signatureResponseDTO.setDrawSignatureImagePath(completeDrawSignatureImagePath);
		}
	}

	@Override
	public XtremandResponse saveDrawSignature(SignatureDTO signatureDTO) {
		XtremandResponse response = new XtremandResponse();
		String drawSignatureImagePath = writeSignatureToFile(signatureDTO);
		if (!XamplifyConstants.ERROR.equals(drawSignatureImagePath)) {
			XamplifyUtils.addSuccessStatusWithMessage(response, SIGNATURE_ADDED_SUCCESSFULLY);
			signatureDao.updateDrawSignaturePath(signatureDTO.getLoggedInUserId(), DRAW_SIGNATURE_PNG);
		}
		return response;
	}

	private String writeSignatureToFile(SignatureDTO signatureDTO) {
		String base64Data = signatureDTO.getDrawSignatureEncodedImage().split(",")[1];
		byte[] imageBytes = Base64.getDecoder().decode(base64Data);
		Integer loggedInUserId = signatureDTO.getLoggedInUserId();
		String userAlias = userDao.getAliasByUserId(loggedInUserId);
		String signaturePath = fileUtil.createSignatureFolderPath(userAlias);
		String completeSignaturePath = signaturePath + sep + DRAW_SIGNATURE_PNG;
		try (OutputStream outputStream = new FileOutputStream(completeSignaturePath)) {
			outputStream.write(imageBytes);
		} catch (FileNotFoundException fnf) {
			fnf.printStackTrace();
			return XamplifyConstants.ERROR;
		} catch (IOException ioe) {
			ioe.printStackTrace();
			return XamplifyConstants.ERROR;
		}
		return completeSignaturePath;
	}

	@Override
	public String uploadFile(MultipartFile file, Integer userId) throws IOException {
		if (file.isEmpty()) {
			throw new IllegalArgumentException("No file was uploaded.");
		}
		String userAlias = userDao.getAliasByUserId(userId);
		String signaturePath = fileUtil.createSignatureFolderPath(userAlias);
		String originalFilename = file.getOriginalFilename();
		String completeSignaturePath = signaturePath + sep + originalFilename;
		try (FileOutputStream fos = new FileOutputStream(completeSignaturePath)) {
			fos.write(file.getBytes());
		}
		signatureDao.updateUploadedSignatureImagePath(userId, originalFilename);
		return "Signature uploaded successfully";
	}

	@Override
	public XtremandResponse removeExistingSignature(SignatureDTO signatureDTO) {
		XtremandResponse response = new XtremandResponse();
		signatureDao.removeExistingSignature(signatureDTO);
		XamplifyUtils.addSuccessStatusWithMessage(response, "Signature deleted successfully");
		return response;
	}

}
