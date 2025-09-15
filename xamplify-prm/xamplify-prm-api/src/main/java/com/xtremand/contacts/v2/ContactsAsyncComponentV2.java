package com.xtremand.contacts.v2;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.xtremand.campaign.bom.DownloadDataInfo;
import com.xtremand.common.bom.Pagination;
import com.xtremand.contacts.dto.ContactsRequestDTO;

@Component
public class ContactsAsyncComponentV2 {

	private static final Logger logger = LoggerFactory.getLogger(ContactsAsyncComponentV2.class);

	@Autowired
	private ContactsV2Service contactsV2Service;

	/** XNFR-713 **/
	@Async(value = "createUsersInUserList")
	public void saveContacts(ContactsRequestDTO contactsRequestDTO) {
		long startTime = getStartTime();
		contactsV2Service.saveContacts(contactsRequestDTO);
		getExecutionTime(startTime, "saveContacts(" + contactsRequestDTO.getContactListName() + "("
				+ contactsRequestDTO.getUserListId() + ")");

	}

	
	private void getExecutionTime(long startTime, String methodName) {
		long stopTime = getStartTime();
		long elapsedTime = stopTime - startTime;
		long minutes = TimeUnit.MILLISECONDS.toMinutes(elapsedTime);
		String debugMessage = methodName + " Completed In " + minutes + " minutes at " + new Date()
				+ "***Active Thread Count*****" + Thread.activeCount();
		logger.debug(debugMessage);
	}

	private long getStartTime() {
		return System.currentTimeMillis();
	}

	@Async(value = "updateContactList")
	public void updateContacts(ContactsRequestDTO contactsRequestDTO) {
		long startTime = getStartTime();
		contactsV2Service.updateContacts(contactsRequestDTO);
		getExecutionTime(startTime, "updateContacts(" + contactsRequestDTO.getContactListName() + "("
				+ contactsRequestDTO.getUserListId() + ")");

	}

	@Async(value = "downloadListOfContacts")
	public void downloadListOfContacts(Pagination pagination, DownloadDataInfo dataInfo) {
		long startTime = getStartTime();
		contactsV2Service.downloadListOfContacts(pagination, dataInfo);
		getExecutionTime(startTime, "downloadListOfContacts(" + pagination.getUserId() + ")");
	}

}
