package com.signature.dto;

import java.io.Serializable;

import lombok.Data;

@Data
public class SignatureResponseDTO implements Serializable {
	/**
	* 
	*/
	private static final long serialVersionUID = -7560324858546704070L;

	private boolean drawSignatureExits;
	
	private String drawSignatureImagePath;

	private String typedSignatureFont;

	private String typedSignatureText;

	private boolean typedSignatureExists;
	
	private String uploadedSignatureImagePath;

	private boolean uploadedSignatureExits;
	
	private String typedSignatureImagePath;

}
