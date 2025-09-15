package com.xtremand.util.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import lombok.Data;

@Data
public class ContentSharedEmailNotificationDTO implements Serializable {
	/**
	* 
	*/
	private static final long serialVersionUID = -2310354189158971638L;

	private List<String> names = new ArrayList<>();

	private String partnerListName;

	private boolean publishedToPartnerList;

	private String receiverEmailId;

	private String receiverName;

	private String partnerCompanyName;

	private String partnerEmailId;

	private String moduleName;

	private Date sharedtimeInString;

	private String pageName;

	private boolean vendorEmailNotificationRequired;

}
