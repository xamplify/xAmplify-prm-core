package com.xtremand.signature.dao;

import com.signature.dto.SignatureDTO;
import com.signature.dto.SignatureResponseDTO;

public interface SignatureDao {

	void saveTypedSignature(SignatureDTO signatureRequestDTO);

	SignatureDTO getTypedSignatureDetailsByUserId(Integer loggedInUserId);

	void updateDrawSignaturePath(Integer userId, String drawSignatureImagePath);

	void updateUploadedSignatureImagePath(Integer userId, String drawSignatureImagePath);

	SignatureResponseDTO getExistingSignaturesByLoggedInUserId(Integer userId);

	void removeExistingSignature(SignatureDTO signatureDto);

	void updateTypedSignaturePath(Integer loggedInUserId, String typedSinatureImagePath);

}
