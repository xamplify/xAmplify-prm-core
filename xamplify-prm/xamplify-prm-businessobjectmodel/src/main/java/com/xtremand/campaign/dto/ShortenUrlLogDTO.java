package com.xtremand.campaign.dto;

import java.io.Serializable;

import lombok.Data;

@Data
public class ShortenUrlLogDTO implements Serializable {

	private static final long serialVersionUID = -1803802423432975742L;

	private Integer id;

	private String campaignAlias;

	private String userAlias;

	private String videoAlias;

	private Integer templateId;

	private Integer campaignId;

	private Integer userId;

	private Integer videoId;

	private String alias;

	private boolean testEmail;

	private boolean publicEventCampaign;

	private boolean channelVideo;

	private String emailId;

	private String urlLink;

	private Integer urlId;

	private Integer replyId;

	private String type;
	
	private boolean nurtureCampaign;
	
	private Integer parentCampaignId;
	
	private String website;
	
	private boolean isTestEmail;
	
	private Integer formId;

}
