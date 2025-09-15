package com.signature.dto;

import java.io.Serializable;

import lombok.Data;

@Data
public class SignatureDTO implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String typedSignatureText;

	private String typedSignatureFont;

	private String typedSignatureTextImagePath;

	private Integer loggedInUserId;

	private String drawSignatureEncodedImage;

	private String signatureType;
	
	private String typedSignatureEncodedImage;

}
