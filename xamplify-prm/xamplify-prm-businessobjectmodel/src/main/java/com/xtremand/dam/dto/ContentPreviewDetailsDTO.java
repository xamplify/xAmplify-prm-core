package com.xtremand.dam.dto;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;

@Data
public class ContentPreviewDetailsDTO implements Serializable {
	/**
	* 
	*/
	private static final long serialVersionUID = -8592873345222898407L;

	private String fileType;

	private String filePath;

	private boolean imageFile;

	private String fileName;

	@JsonIgnore
	private String videoUri;

}
