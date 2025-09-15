package com.xtremand.integration.bom;

import java.util.List;

import lombok.Data;

@Data
public class SaveExternalContactsRequest {
	private String listName;
	private List<ExternalContactDTO> contacts;
	private String type;
	private Integer userId;
	private Long externalListId;
	private boolean publicList;
	private String moduleName;
}
