package com.xtremand.partner.journey.dto;

import java.io.Serializable;
import java.util.Set;

import lombok.Data;

@Data
public class WorkflowUtilDTO implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4474663484210219470L;
	private Integer id;
	private String title;
	private Integer subjectId;
	private Integer actionId;
	private Integer timePhraseId;
	private String queryBuilderInputString;
	private Set<Integer> selectedPartnerListIds;
	private boolean customTemplateSelected;
	private Integer templateId;
	private String notificationSubject;
	private String notificationMessage;
	private Integer customDays;
	private String preHeader;
	private Integer fromEmailUserId;
	private Integer companyId;
	
	//XNFR-921
	private Set<Integer> selectedPartnerIds;
	
	private boolean partnerGroupSelected;
	
	//XNFR-993
	private Integer learningTrackId;
	
	private Set<Integer> partnerShipIds;


}
