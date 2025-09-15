package com.xtremand.signature.dao.hibernate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.signature.dto.SignatureDTO;
import com.signature.dto.SignatureResponseDTO;
import com.xtremand.signature.dao.SignatureDao;
import com.xtremand.util.dao.HibernateSQLQueryResultUtilDao;
import com.xtremand.util.dto.HibernateSQLQueryResultRequestDTO;
import com.xtremand.util.dto.QueryParameterDTO;
import com.xtremand.util.dto.XamplifyConstants;

@Repository
@Transactional
public class HibernateSignatureDao implements SignatureDao {

	private static final String USER_ID = XamplifyConstants.USER_ID;
	@Autowired
	private HibernateSQLQueryResultUtilDao hibernateSQLQueryResultUtilDao;

	@Override
	public void saveTypedSignature(SignatureDTO signatureRequestDTO) {
		String queryString = "update xt_user_profile set typed_signature_text = :typedSignatureText, typed_signature_font = :typedSignatureFont where user_id = :userId";
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		hibernateSQLQueryResultRequestDTO.setQueryString(queryString);
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
				.add((new QueryParameterDTO("typedSignatureText", signatureRequestDTO.getTypedSignatureText())));
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
				.add((new QueryParameterDTO("typedSignatureFont", signatureRequestDTO.getTypedSignatureFont())));
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
				.add((new QueryParameterDTO(USER_ID, signatureRequestDTO.getLoggedInUserId())));
		hibernateSQLQueryResultUtilDao.update(hibernateSQLQueryResultRequestDTO);
	}

	@Override
	public SignatureDTO getTypedSignatureDetailsByUserId(Integer loggedInUserId) {
		String queryString = "select typed_signature_text  as \"typedSignatureText\", typed_signature_font as \"typedSignatureFont\", typed_signature_text_image_path as \"typedSignatureTextImagePath\""
				+ " from xt_user_profile where user_id = :userId";
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		hibernateSQLQueryResultRequestDTO.setQueryString(queryString);
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add((new QueryParameterDTO(USER_ID, loggedInUserId)));
		return (SignatureDTO) hibernateSQLQueryResultUtilDao.getDto(hibernateSQLQueryResultRequestDTO,
				SignatureDTO.class);
	}

	@Override
	public void updateDrawSignaturePath(Integer userId, String drawSignatureImagePath) {
		updateImagePath(userId, drawSignatureImagePath,
				"update xt_user_profile set draw_signature_image_path = :imagePath where user_id = :userId");
	}

	private void updateImagePath(Integer userId, String imagePath, String queryString) {
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		hibernateSQLQueryResultRequestDTO.setQueryString(queryString);
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add((new QueryParameterDTO("imagePath", imagePath)));
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add((new QueryParameterDTO(USER_ID, userId)));
		hibernateSQLQueryResultUtilDao.update(hibernateSQLQueryResultRequestDTO);
	}

	@Override
	public void updateUploadedSignatureImagePath(Integer userId, String imagePath) {
		updateImagePath(userId, imagePath,
				"update xt_user_profile set uploaded_signature_image_path = :imagePath where user_id = :userId");
	}

	@Override
	public SignatureResponseDTO getExistingSignaturesByLoggedInUserId(Integer userId) {
		String queryString = "select draw_signature_image_path  as \"drawSignatureImagePath\", typed_signature_text as \"typedSignatureText\", typed_signature_font as \"typedSignatureFont\", "
				+ "uploaded_signature_image_path as \"uploadedSignatureImagePath\", typed_signature_text_image_path  as \"typedSignatureImagePath\" from xt_user_profile where user_id = :userId";
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		hibernateSQLQueryResultRequestDTO.setQueryString(queryString);
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add((new QueryParameterDTO(USER_ID, userId)));
		return (SignatureResponseDTO) hibernateSQLQueryResultUtilDao.getDto(hibernateSQLQueryResultRequestDTO,
				SignatureResponseDTO.class);
	}

	@Override
	public void removeExistingSignature(SignatureDTO signatureDto) {
		String queryString = "";
		if ("draw".equalsIgnoreCase(signatureDto.getSignatureType())) {
			queryString = "update xt_user_profile set draw_signature_image_path = NULL where user_id = :userId";
		} else if ("type".equalsIgnoreCase(signatureDto.getSignatureType())) {
			queryString = "update xt_user_profile set typed_signature_text = NULL, typed_signature_font = NULL where user_id = :userId";
		} else if ("upload".equalsIgnoreCase(signatureDto.getSignatureType())) {
			queryString = "update xt_user_profile set uploaded_signature_image_path = NULL where user_id = :userId";
		}
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		hibernateSQLQueryResultRequestDTO.setQueryString(queryString);
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
				.add((new QueryParameterDTO(USER_ID, signatureDto.getLoggedInUserId())));
		hibernateSQLQueryResultUtilDao.update(hibernateSQLQueryResultRequestDTO);

	}

	@Override
	public void updateTypedSignaturePath(Integer userId, String imagePath) {
		updateImagePath(userId, imagePath,
				"update xt_user_profile set typed_signature_text_image_path = :imagePath where user_id = :userId");

	}

}
