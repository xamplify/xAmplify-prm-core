package com.xtremand.vanity.email.templates.bom;

import java.io.Serializable;

import lombok.Data;

@Data
public class DefaultEmailTemplateDTO implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Integer id;
	private String name;
	private String subject;
	private String jsonBody;
	private String htmlBody;
	private String spamScore;
	private DefaultEmailTemplateType type;
	private Integer defaultEmailTemplateId;
	private String companyProfileName;
	private Integer userId;
	private String imagePath;
	private Integer companyId;
	private String typeInString;
	private String cdnImagePath;

}
