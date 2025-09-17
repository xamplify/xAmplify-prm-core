package com.xtremand.activity.dto;

import java.io.Serializable;
import java.util.List;

import lombok.Data;

@Data
public class NoteDTO implements Serializable{
	
	private static final long serialVersionUID = 5435050001560008540L;
	
	private Integer id;
	private String title;
	private String content;
	private boolean pinned;
	private Integer loggedInUserId;
	private Integer companyId;
	private String visibility;
	private String associationType;
	private Integer contactId;
	private Integer createdBy;
	private Integer updatedBy;
	private String createdTime;
	private String updatedTime;
	
	private Boolean isCompanyJourney;
	private List<Integer> userIds;
	private String addedForEmailId;
	
}
