package com.xtremand.contacts.v2;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.xtremand.campaign.bom.DownloadDataInfo;
import com.xtremand.campaign.exception.XamplifyDataAccessException;
import com.xtremand.common.bom.Pagination;
import com.xtremand.contacts.dto.ContactsRequestDTO;
import com.xtremand.exception.DuplicateEntryException;
import com.xtremand.formbeans.XtremandResponse;
import com.xtremand.util.BadRequestException;
import com.xtremand.util.dto.XamplifyConstants;

@RequestMapping(value = "/contacts/v2/")
@RestController
public class ContactsV2Controller {

	private static final String USER_LIST_NAME_UNIQUE_INDEX = "user_list_name_unique_index";

	@Autowired
	private ContactsV2Service contactsV2Service;

	@Autowired
	private ContactsAsyncComponentV2 contactsAsyncComponentV2;

	/** XNFR-713 **/
	@PostMapping(value = "save")
	public ResponseEntity<XtremandResponse> save(@RequestBody ContactsRequestDTO contactsRequestDTO) {
		boolean constraint = false;
		XtremandResponse response = new XtremandResponse();
		try {
			response = contactsV2Service.saveContactList(contactsRequestDTO);
			return ResponseEntity.ok(response);
		} catch (BadRequestException e) {
			constraint = true;
			throw new BadRequestException(e.getMessage());
		} catch (DataIntegrityViolationException e) {
			constraint = true;
			if (e.getMessage().indexOf(USER_LIST_NAME_UNIQUE_INDEX) > -1) {
				throw new DuplicateEntryException(XamplifyConstants.DUPLICATE_NAME);
			} else {
				throw new XamplifyDataAccessException(e);
			}
		} catch (Exception e) {
			constraint = true;
			throw new XamplifyDataAccessException(e);
		} finally {
			if (!constraint && response.getStatusCode() == 200) {
				contactsAsyncComponentV2.saveContacts(contactsRequestDTO);

			}
		}
	}

	/** XNFR-713 **/
	@PutMapping(value = "update")
	public ResponseEntity<XtremandResponse> update(@RequestBody ContactsRequestDTO contactsRequestDTO) {
		boolean constraint = false;
		XtremandResponse response = new XtremandResponse();
		try {
			response = contactsV2Service.updateContactList(contactsRequestDTO);
			return ResponseEntity.ok(response);
		} catch (BadRequestException e) {
			constraint = true;
			throw new BadRequestException(e.getMessage());
		} catch (DataIntegrityViolationException e) {
			constraint = true;
			if (e.getMessage().indexOf(USER_LIST_NAME_UNIQUE_INDEX) > -1) {
				throw new DuplicateEntryException(XamplifyConstants.DUPLICATE_NAME);
			} else {
				throw new XamplifyDataAccessException(e);
			}
		} catch (Exception e) {
			constraint = true;
			throw new XamplifyDataAccessException(e);
		} finally {
			if (!constraint && response.getStatusCode() == 200) {
				contactsAsyncComponentV2.updateContacts(contactsRequestDTO);

			}
		}
	}

	@PutMapping(value = "update-contact-details")
	public ResponseEntity<XtremandResponse> updateEditContact(@RequestBody ContactsRequestDTO contactsRequestDTO) {
		try {
			return ResponseEntity.ok(contactsV2Service.updateEditContact(contactsRequestDTO));
		} catch (BadRequestException e) {
			throw new XamplifyDataAccessException(e);
		}
	}

	@PostMapping("paginated/user-list-contacts")
	public ResponseEntity<?> findUserListContacts(@RequestBody Pagination pagination) {
		try {
			return ResponseEntity.ok(contactsV2Service.findUserListContacts(pagination));
		} catch (IllegalArgumentException ex) {
			return ResponseEntity.badRequest().body("Invalid input: " + ex.getMessage());
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
		}
	}

	@GetMapping("contacts-count")
	public ResponseEntity<XtremandResponse> findUserListContactsCount(@RequestParam Integer loggedInUserId,
			@RequestParam Integer userListId, @RequestParam String moduleName) {
		try {
			return ResponseEntity
					.ok(contactsV2Service.findUserListContactsCount(loggedInUserId, userListId, moduleName));
		} catch (BadRequestException e) {
			throw new XamplifyDataAccessException(e);
		}
	}

	@PostMapping(value = "download/user-list-contacts")
	public ResponseEntity<XtremandResponse> downloadUserList(@RequestBody Pagination pagination) {
		boolean hasError = false;
		XtremandResponse response = new XtremandResponse();
		try {
			return ResponseEntity.ok(contactsV2Service.updateDownloadDataInfoStatus(pagination, response));
		} catch (Exception e) {
			hasError = true;
			throw new XamplifyDataAccessException(e);
		} finally {
			if (!hasError && response.getStatusCode() == 200) {
				DownloadDataInfo dataInfo = (DownloadDataInfo) response.getData();
				contactsAsyncComponentV2.downloadListOfContacts(pagination, dataInfo);
			}
		}
	}

}
