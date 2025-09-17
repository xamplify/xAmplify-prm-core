package com.xtremand.integration.dto;

import lombok.Data;

@Data
public class CallIntegrationDTO {
	
	private Integer id;
	private String type;
	private String accessToken;
	private String refreshToken;
	private Integer createdBy;
	private boolean active;
	private String externalName;
	private String externalEmailId;
	private boolean enableUnlink;

}
