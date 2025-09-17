package com.xtremand.activity.dto;

import java.io.Serializable;

import lombok.Data;

@Data
public class NoteResponseDTO implements Serializable{

private static final long serialVersionUID = 5435050001560008540L;
	
	private Integer id;
	private String title;
	private String content;
	private String visibility;
	private String associationType;
	private boolean pinned;
	private Integer loggedInUserId;
	private Integer companyId;
	private Integer createdBy;
	private Integer updatedBy;
	private String createdTime;
	private String updatedTime;
	private String createdByFirstName;
	private String createdByLastName;
	private String createdByCompanyName;
	private String createdByEmailId;
	private boolean editEnabled;
	private boolean deleteEnabled;
	private String addedForEmailId;
	
}
