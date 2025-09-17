package com.xtremand.mdf.dto;

import java.io.Serializable;
import java.util.Date;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class MdfRequestDocumentDTO extends MdfUserMappedDTO implements Serializable {/**
	 * 
	 */
	private static final long serialVersionUID = -4617738611625528114L;
	
	private String fileName;
	
	private String filePathAlias;
	
	private String description;
	
	private Date uploadedTime;
	
	private String uploadedTimeInUTCString;
	
	

}
