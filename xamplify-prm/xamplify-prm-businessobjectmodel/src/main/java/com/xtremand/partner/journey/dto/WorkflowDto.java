package com.xtremand.partner.journey.dto;

import java.util.Date;
import java.util.Set;

import lombok.Data;

@Data
public class WorkflowDto {
	
	private Integer id;
	private String title;
	private TriggerComponentDTO triggerSubject;
	private TriggerComponentDTO triggerAction;
	private TriggerComponentDTO triggerTimePhrase;
	private String notificationSubject;
	private String notificationMessage;
	private Integer templateId;
	private Integer createdBy;
	private Date createdDate;
	private String createdDateinUTC;
	private Set<Integer> userlistIds;

}
