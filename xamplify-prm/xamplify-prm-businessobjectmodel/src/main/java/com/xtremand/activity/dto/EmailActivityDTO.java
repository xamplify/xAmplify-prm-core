package com.xtremand.activity.dto;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import lombok.Data;

@Data
public class EmailActivityDTO implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 272901525159373482L;
	
	private Integer id;
		
	private String subject;
	
	private String body;
	
	private Integer recipient;
	
	private Integer sender;
	
	private String createdTime;
	
	private String senderEmailId;
	
	private String status;
	
	private String openedTime;
	
	private List<String> ccEmailIds;
	
	private List<String> bccEmailIds;
	
	private String ccEmailIdsString;
	
	private String bccEmailIdsString;
	
	private List<ActivityAttachmentDTO> emailAttachmentDTOs;
	
	private String addedForEmailId;
	
	private String fullName;
	
	private String attachmentPaths;
	
	private String openAIFileId;
	
	private String recipientName;
	
	private String senderName;
	
	private String attachmentFiles;
	
	private Integer emailActivityId;
	
	private String fileNames;
	
	private String toEmailIdsString;
	
	private Date sentOn;
	
	private Integer userId;
	
}
