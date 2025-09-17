package com.xtremand.activity.dto;

import java.io.Serializable;

import lombok.Data;

@Data
public class ActivityAttachmentDTO implements Serializable {/**
	 * 
	 */
	private static final long serialVersionUID = -8833043961959962355L;
	
	private Integer id;
	
	private String fileName;
	
	private String filePath;
	
	private boolean isUploadedFile;
	
	private String fileType;
	
	private String size;
	
	private String temporaryFilePath;
	
	private Integer loggedInUserId;
	
	private Integer companyId;
	
	private String openAIFileId;

}
