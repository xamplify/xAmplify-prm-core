package com.xtremand.integration.dto;

import java.io.Serializable;
import java.util.Date;

import com.xtremand.util.dto.DateInString;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;

@Data
public class IntegrationResponseDTO implements Serializable {
	/**
	* 
	*/
	private static final long serialVersionUID = -5692546129822580800L;

	private Integer id;

	private Integer companyId;

	private String companyName;

	private String type;

	private String accessToken;

	private String refreshToken;

	private Date expiry;

	@Getter(value = AccessLevel.NONE)
	private String expriyDateInUTCString;

	private Date createdTime;

	@Getter(value = AccessLevel.NONE)
	private String createdTimeInUTCString;

	private Date updatedTime;

	@Getter(value = AccessLevel.NONE)
	private String updatedTimeInUTCString;

	private Integer createdBy;

	private String createdByEmailAddress;

	private Integer updatedBy;

	private String updatedByEmailAddress;

	private boolean sandBox;

	private String instanceUrl;

	private boolean pushLeads;

	private String externalEmail;

	private String externalUserId;

	private String externalDisplayName;

	private String externalUserName;

	private String externalOrganizationId;

	private String externalThumbnail;

	private String webApiInstanceUrl;

	private boolean active;

	private String clientId;

	private String clientSecret;

	private String apiKey;

	private String externalOrganizationName;

	private String publicKey;

	private String privateKey;

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public String getExpriyDateInUTCString() {
		if (createdTime != null) {
			return DateInString.getUtcString(expiry);
		} else {
			return "";
		}
	}

	public String getCreatedTimeInUTCString() {
		if (createdTime != null) {
			return DateInString.getUtcString(createdTime);
		} else {
			return "";
		}
	}

	public String getUpdatedTimeInUTCString() {
		if (updatedTime != null) {
			return DateInString.getUtcString(updatedTime);
		} else {
			return "";
		}
	}

}
