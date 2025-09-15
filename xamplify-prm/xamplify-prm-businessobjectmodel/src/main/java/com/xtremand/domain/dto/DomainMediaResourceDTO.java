package com.xtremand.domain.dto;

import java.io.Serializable;

import org.springframework.web.multipart.MultipartFile;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class DomainMediaResourceDTO implements Serializable {

	private static final long serialVersionUID = -2378435876317211831L;
	
	private Integer id;
	
	private String fileName;
	
	private String filePath;
	
	private Integer domainMetadataId;
	
	private String temporaryFilePath;
	
	private String completeFileName;
	
	private String updatedFileName;
	
	private String imageType;
	
	private String fileType;
	
	private transient MultipartFile convertedImageMultipartFile;
	
	private String theme;
	
	private String domainName;
}
