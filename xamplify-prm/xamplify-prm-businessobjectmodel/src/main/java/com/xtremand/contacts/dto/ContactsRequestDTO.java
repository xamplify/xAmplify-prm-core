package com.xtremand.contacts.dto;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import lombok.Data;

@Data
public class ContactsRequestDTO implements Serializable {
	/**
	* 
	*/
	private static final long serialVersionUID = 1L;

	private String contactListName;

	private Integer loggedInUserId;

	private Long externalListId;

	private String socialNetwork;

	private String contactType;

	private boolean publicList;

	private Set<ContactFieldsDTO> users = new HashSet<>();

	private Set<String> newlyAddedEmailIds = new HashSet<>();

	private Integer userListId;

	private boolean addingNewContactList;

	private Integer userListUploadHistoryId;

	private boolean updatingContactList;

	private Set<Integer> insertedUserIds = new HashSet<>();

}
