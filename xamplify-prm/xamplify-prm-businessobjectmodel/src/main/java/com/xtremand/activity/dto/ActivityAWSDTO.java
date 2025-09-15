package com.xtremand.activity.dto;

import java.io.Serializable;
import java.util.List;

import lombok.Data;

@Data
public class ActivityAWSDTO implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8133859613325240297L;
	
	private Integer attachmentId;
	
	private Integer userId;

	private Integer companyId;
	
	private String fileName;
	
	private String filePath;
	
	private String fileType;
	
	private Integer emailActivityId;
	
	private String completeFileName;
	
	private String updatedFileName;
	
	private String temporaryFilePath;
	
	/**XNFR-867**/
	private List<Integer> attachmentIds;
	
}
