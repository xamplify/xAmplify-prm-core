package com.xtremand.form.emailtemplate.dto;

import java.io.Serializable;
import java.util.List;

import lombok.Data;

@Data
public class SendTestEmailDTO implements Serializable {
	/**
	* 
	*/
	private static final long serialVersionUID = -3059711876711777924L;

	private String fromEmail;
	
	private String toEmail;

	private String subject;

	private String body;
	
	private Integer id;
	
	private String fromName;
	
	private boolean channelCampaign;
	
	private String preHeader;
	
	private boolean emailCampaign;
	
   	 private List<String> ccEmailIds;
		
	private List<String> bccEmailIds;
		
	private List<String> toEmailIds;

	private Integer loggedInUserId;

	private String companyProfileName;
	
	private Integer companyId;
	
	private Integer trackId;

}
