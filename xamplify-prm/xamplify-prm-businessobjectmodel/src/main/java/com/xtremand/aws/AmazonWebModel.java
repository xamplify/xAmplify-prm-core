package com.xtremand.aws;

import java.util.Date;
import java.util.List;

import javax.persistence.Transient;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AmazonWebModel {

	private String key;
	
	private String filePath;
	
	private String filesizeInKb;
	
	private Integer userId;
	
	private Integer companyId;
	
	private String fileName;
	
	private Date lastModifiedDate;
	
	private String type;
	
	private List<String> awsFileKeys;
	
	private String category;
	
	@Transient
	private String utcTimeString;
	
	private boolean generatedImages;
	
	private Integer formId;
	
	private String downloadLink;
	
	private String folderSuffixPath;
	
	private Integer id;
}
