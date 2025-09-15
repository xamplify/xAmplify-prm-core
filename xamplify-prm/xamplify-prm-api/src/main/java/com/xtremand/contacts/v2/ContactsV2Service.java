package com.xtremand.contacts.v2;

import java.util.Map;

import com.xtremand.campaign.bom.DownloadDataInfo;
import com.xtremand.common.bom.Pagination;
import com.xtremand.contacts.dto.ContactsRequestDTO;
import com.xtremand.formbeans.XtremandResponse;

public interface ContactsV2Service {

	XtremandResponse saveContactList(ContactsRequestDTO contactsRequestDTO);

	void saveContacts(ContactsRequestDTO contactsRequestDTO);

	XtremandResponse updateContactList(ContactsRequestDTO contactsRequestDTO);
	
	void updateContacts(ContactsRequestDTO contactsRequestDTO);

	XtremandResponse updateEditContact(ContactsRequestDTO contactsRequestDTO);

	Map<String, Object> findUserListContacts(Pagination pagination);

	XtremandResponse findUserListContactsCount(Integer loggedInUserId, Integer userListId, String moduleName);

	XtremandResponse updateDownloadDataInfoStatus(Pagination pagination, XtremandResponse response);

	void downloadListOfContacts(Pagination pagination, DownloadDataInfo dataInfo);

}
