package com.xtremand.controller;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.xtremand.campaign.bom.UnsubscribeUserDTO;
import com.xtremand.campaign.exception.XamplifyDataAccessException;
import com.xtremand.common.bom.Pagination;
import com.xtremand.common.bom.StatusCode;
import com.xtremand.exception.DuplicateEntryException;
import com.xtremand.formbeans.AddPartnerResponseDTO;
import com.xtremand.formbeans.UserDTO;
import com.xtremand.formbeans.UserListDTO;
import com.xtremand.formbeans.UserListPaginationWrapper;
import com.xtremand.formbeans.XtremandResponse;
import com.xtremand.mail.service.AsyncComponent;
import com.xtremand.mail.service.StatusCodeConstants;
import com.xtremand.partnership.service.PartnershipService;
import com.xtremand.unsubscribe.service.UnsubscribeService;
import com.xtremand.user.bom.ShareLeadsDTO;
import com.xtremand.user.bom.User;
import com.xtremand.user.bom.UserPaginationWrapper;
import com.xtremand.user.bom.UserUserListWrapper;
import com.xtremand.user.list.dto.CopiedUserListUsersDTO;
import com.xtremand.user.list.dto.CopyGroupUsersDTO;
import com.xtremand.user.service.UserService;
import com.xtremand.userlist.exception.UserListException;
import com.xtremand.userlist.service.UserListService;
import com.xtremand.util.BadRequestException;
import com.xtremand.util.HttpHeaderUtil;
import com.xtremand.util.ResponseUtil;
import com.xtremand.util.XamplifyUtils;
import com.xtremand.util.dto.Pageable;
import com.xtremand.util.dto.UserListOperationsAsyncDTO;
import com.xtremand.util.dto.XamplifyConstants;

@RestController
@RequestMapping("/userlists")
public class ContactController {

	private static final Logger logger = LoggerFactory.getLogger(ContactController.class);

	@Autowired
	UserService userService;

	@Autowired
	UserListService userListService;

	@Autowired
	com.xtremand.user.validation.UserListValidator userListValidator;

	@Autowired
	private PartnershipService partnershipService;

	@Autowired
	private UnsubscribeService unsubscribeService;

	@Autowired
	private AsyncComponent asyncComponent;

	/***************** to get list of userLists *************************/
	@PostMapping
	public ResponseEntity<?> userlists(@RequestBody Pagination pagination, @RequestParam Integer userId) {
		try {
			Map<String, Object> resultMap = userListService.userlists(userId, pagination);
			return resultMap.isEmpty()
					? (ResponseUtil.getResponse(HttpStatus.OK, StatusCodeConstants.NO_CONTACTLIST_FOUND, null))
					: (ResponseUtil.getResponse(HttpStatus.OK, StatusCodeConstants.CONTACTLIST_FOUND, resultMap));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(String.valueOf(e.getCause()));
		}
	}

	@RequestMapping(value = "/campaign-user-lists", method = RequestMethod.POST)
	public ResponseEntity<?> getCampaignuserlists(@RequestBody Pagination pagination) {
		try {
			Map<String, Object> resultMap = userListService.listCampaignUserLists(pagination);
			return resultMap.isEmpty()
					? (ResponseUtil.getResponse(HttpStatus.OK, StatusCodeConstants.NO_CONTACTLIST_FOUND, null))
					: (ResponseUtil.getResponse(HttpStatus.OK, StatusCodeConstants.CONTACTLIST_FOUND, resultMap));
		} catch (UserListException e) {
			logger.error("error occurred in getCampaignuserlists() :" + e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(String.valueOf(e.getCause()));
		}
	}

	/***************** list of userLists names *************************/
	@RequestMapping(value = { "/names" }, method = RequestMethod.GET)
	public ResponseEntity<?> listUserlistNames(@RequestParam Integer userId) {
		try {
			List<String> names = userListService.listUserlistNames(userId);
			logger.info("user list names size :" + names.size());
			return ResponseEntity.status(HttpStatus.OK).body(Collections.singletonMap("names", names));
		} catch (Exception e) {
			logger.error("error occurred in listUserlistNames() :" + e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(String.valueOf(e.getCause()));
		}
	}

	/*****************
	 * list of contacts from the all userLists for a logged in user
	 *************************/
	@RequestMapping(value = { "/{userId}/all-contacts" }, method = RequestMethod.POST)
	public ResponseEntity<?> listAllUserListContacts(@RequestBody UserListPaginationWrapper userListPaginationWrapper,
			@PathVariable Integer userId) {
		logger.debug("from listAllUserListContacts()");
		try {
			Map<String, Object> resultMap = userListService.listAllUserListContacts(userListPaginationWrapper, userId);
			return resultMap.isEmpty()
					? (ResponseUtil.getResponse(HttpStatus.OK, StatusCodeConstants.NO_CONTACTS_FOUND, null))
					: (ResponseUtil.getResponse(HttpStatus.OK, StatusCodeConstants.All_CONTACTS_FOUND, resultMap));
		} catch (Exception e) {
			logger.error("error occurred in listAllUserListContacts() :" + e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
		}
	}

	/*****************
	 * list of contacts for a particular userList
	 *************************/
	/*****************
	 * in edit functionality when we click on a particular userlist we will get all
	 * users of that particular userlist here contactType=null or required=false
	 *************************/
	@RequestMapping(value = { "/{userId}/contacts" }, method = RequestMethod.POST)
	public ResponseEntity<?> listuserListContacts(@RequestBody UserListPaginationWrapper userListPaginationWrapper,
			@PathVariable Integer userId) {
		logger.debug("from getUserListContactsByContactType()");
		try {
			Map<String, Object> resultMap = userListService.listuserListContacts(userListPaginationWrapper, userId);
			return resultMap.isEmpty()
					? (ResponseUtil.getResponse(HttpStatus.OK, StatusCodeConstants.NO_CONTACTS_FOUND, null))
					: (ResponseUtil.getResponse(HttpStatus.OK, StatusCodeConstants.All_CONTACTS_FOUND, resultMap));
		} catch (Exception e) {
			logger.error("error occurred in userListContactsByContactType() :" + e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
		}
	}


	/************* Add/Updating Partner List *************/
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/update", method = RequestMethod.POST)
	public ResponseEntity<?> updateUserList(@RequestBody UserUserListWrapper userUserListWrapper,
			@RequestParam Integer userId, @RequestParam String companyProfileName) {
		ResponseEntity<?> response = null;
		boolean constraint = false;
		UserListOperationsAsyncDTO userListOperationsAsyncDTO = new UserListOperationsAsyncDTO();
		Map<String, Object> resultMap = new HashMap<>();
		try {
			resultMap = userListService.updateUserList(userUserListWrapper, userId,
					companyProfileName, userListOperationsAsyncDTO);
			response = ResponseUtil.getResponse(HttpStatus.OK, StatusCodeConstants.USERLIST_UPDATED, resultMap);
		} catch (UserListException ex) {
			constraint = true;
			if (StringUtils.containsIgnoreCase(ex.getMessage(), "Please Launch or Delete those campaigns first")) {
				response = ResponseUtil.getResponse(HttpStatus.BAD_REQUEST, StatusCodeConstants.USERLIST_CAMPAIGNS_ERR,
						ex.getMessage());
			} else if (StringUtils.containsIgnoreCase(ex.getMessage(),
					"Following email address(es)'s organization(s) have been already added as partner(s)")) {
				response = ResponseUtil.getResponse(HttpStatus.CONFLICT, StatusCodeConstants.USERLIST_CAMPAIGNS_ERR,
						ex.getMessage());
			}
		} catch (Exception e) {
			constraint = true;
			response = ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
		} finally {
			if (!constraint && userListOperationsAsyncDTO.isPartnerList()
					&& userListOperationsAsyncDTO.getStatusCode() != null
					&& userListOperationsAsyncDTO.getStatusCode().equals(200)) {
				Set<UserDTO> partners = userListOperationsAsyncDTO.getPartners();
				Set<Integer> partnerListIds = userListOperationsAsyncDTO.getPartnerListIds();
				Integer companyId = userService.getCompanyIdByUserId(userId);
				List<String> deactivatedPartners = partnershipService.findDeactivedPartnersByCompanyId(companyId);
				partners.removeIf(dto -> deactivatedPartners.contains(dto.getEmailId()));
				if (XamplifyUtils.isNotEmptySet(partners)) {
					/** XNFR-891-start **/
					List<AddPartnerResponseDTO> partnershipsList = (List<AddPartnerResponseDTO>) resultMap
							.get("detailedResponse");
					Set<UserDTO> users = userUserListWrapper.getUsers();
					asyncComponent.updatePartnerModulesAccess(partnershipsList, users);
					/** XNFR-891-end **/
					Integer defaultMasterPartnerListId = userListService.getDefaultPartnerListIdByCompanyId(companyId);
					partnerListIds.add(defaultMasterPartnerListId);
					XamplifyUtils.removeNullsFromSet(partnerListIds);
					userListOperationsAsyncDTO.setPartnerListIds(partnerListIds);
					partnershipService.publishDAMAndLMSToNewlyAddedPartners(partnerListIds, userId, partners);
					/**** XNFR-597 ******/
					asyncComponent.publishDashboardButtonsToNewlyAddedPartners(userListOperationsAsyncDTO, userId);
				}
			}
		}
		return response;
	}

	@RequestMapping(value = "/{id}/edit", method = RequestMethod.POST)
	public ResponseEntity<?> updateUserDetails(@RequestBody UserPaginationWrapper userPaginationWrapper,
			@PathVariable Integer id, @RequestParam Integer userId) {
		ResponseEntity<?> response = null;
		try {

			Map<String, Object> resultMap = userListService.updateUserDetails(userPaginationWrapper, id, userId);
			response = ResponseUtil.getResponse(HttpStatus.OK, StatusCodeConstants.USERLIST_UPDATED, resultMap);
		} catch (Exception e) {
			logger.error("error occurred in updateUserDetails :" + e.getMessage());
			response = ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
		}
		return response;
	}

	@RequestMapping(value = { "{userId}/rename" }, method = RequestMethod.POST)
	public XtremandResponse renameUserList(@RequestBody UserListDTO userListDTO, @PathVariable Integer userId) {
		logger.info("renameUserList called with id " + userListDTO.getId());
		return userListService.renameUserList(userListDTO, userId);

	}

	@RequestMapping(value = "/{userListId}/removeUsers", method = RequestMethod.POST)
	public ResponseEntity<?> removeUsersFromUserList(@RequestBody List<Integer> removeUserIds,
			@PathVariable Integer userListId, @RequestParam Integer userId) {
		ResponseEntity<?> response = null;
		logger.info("Removing Users from UserList with the user ids: " + removeUserIds);
		try {
			Map<String, Object> resultMap = userListService.removeUsersFromUserList(userListId, userId, removeUserIds);
			if (((String) resultMap.get("message")).contains("CONTACT LIST UPDATED SUCCESSFULLY")) {
				response = ResponseUtil.getResponse(HttpStatus.OK, StatusCodeConstants.USERLIST_UPDATED, resultMap);
			} else {
				response = ResponseUtil.getResponse(HttpStatus.OK, StatusCodeConstants.CONTACTLIST_REMOVED, resultMap);
			}
		} catch (UserListException ex) {
			logger.error("error occurred in removeUsersFromUserList :" + ex.getMessage());
			if (StringUtils.containsIgnoreCase(ex.getMessage(), "Please Launch or Delete those campaigns first")) {
				response = ResponseUtil.getResponse(HttpStatus.BAD_REQUEST, StatusCodeConstants.USERLIST_CAMPAIGNS_ERR,
						ex.getMessage());
			}
		} catch (Exception e) {
			response = ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
		}
		return response;
	}

	@RequestMapping(value = "/resubscribeUser/{id}", method = RequestMethod.POST)
	public ResponseEntity<?> resubscribeUser(@PathVariable Integer id, @RequestParam Integer userId) {
		logger.info("entered into resubscribeUser() with userid: " + id);
		userListService.resubscribeUser(id, userId);
		return ResponseEntity.status(HttpStatus.OK).body("User is successfully resubscribed");
	}

	@RequestMapping(value = "/{userId}/{assignLeads}/removeInvalidUsers", method = RequestMethod.POST)
	public ResponseEntity<?> removeInvalidUsers(@PathVariable Integer userId, @RequestBody List<Integer> removeUserIds,
			@PathVariable boolean assignLeads) {
		logger.debug("from removeInvalidUsers() method:");
		try {
			XtremandResponse xtremandResponse = userListService.removeInvalidUsers(userId, removeUserIds, assignLeads);
			xtremandResponse = userListService.removeZeroUsersLists(xtremandResponse);
			return ResponseUtil.getResponse(HttpStatus.OK, StatusCodeConstants.INVALID_USERS_DELETED, xtremandResponse);
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
		}
	}

	@RequestMapping(value = { "/save-userlist/{userId}" }, method = RequestMethod.POST)
	public XtremandResponse saveUserList(@RequestBody UserUserListWrapper userUserListWrapper,
			@PathVariable Integer userId) {
		return userListService.saveUserList(userId, userUserListWrapper);
	}

	/***************** to remove a particular userList *************************/
	@RequestMapping(value = "/{id}/remove", method = RequestMethod.POST)
	public ResponseEntity<?> removeUserList(@PathVariable Integer id, @RequestParam Integer userId) {
		logger.info("remove userlist called with id " + id);
		ResponseEntity<?> response = null;
		try {
			XtremandResponse xtremandResponse = userListService.removeUserList(id, userId, true);
			response = ResponseUtil.getResponse(HttpStatus.OK, StatusCodeConstants.CONTACTLIST_REMOVED,
					xtremandResponse);
		} catch (UserListException ex) {
			logger.error("error occurred in removeUserList() :" + ex.getMessage());
			if (StringUtils.containsIgnoreCase(ex.getMessage(), "Please Launch or Delete those campaigns first")) {
				response = ResponseUtil.getResponse(HttpStatus.BAD_REQUEST, StatusCodeConstants.USERLIST_CAMPAIGNS_ERR,
						ex.getMessage());
			}
		} catch (Exception e) {
			logger.error("error occurred " + e.getMessage());
			response = ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
		}
		return response;
	}

	/*********** download contactList ********************/
	@RequestMapping(value = "/download", method = RequestMethod.POST)
	public ResponseEntity<?> downloadUserList(@RequestBody UserListPaginationWrapper userListPaginationWrapper, HttpServletResponse response)
			throws IOException {
		try {
			logger.info("download userlist called with id " + userListPaginationWrapper.getUserList().getId());

			userListService.downloadUserList(userListPaginationWrapper, response);
			StatusCode statusCode = StatusCode.getstatuscode(StatusCodeConstants.DOWNLOAD_USERLIST_SUCCESS);
			return ResponseEntity.status(HttpStatus.OK).headers(HttpHeaderUtil.getHeader(statusCode)).body(null);
		} catch (UserListException e) {
			logger.error("error Occurred " + e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Collections.singletonMap("message", e.getMessage()));
		}
	}

	/*********** Contacts Count ********************/
	@RequestMapping(value = { "/contacts_count/{userId}" }, method = RequestMethod.POST)
	public ResponseEntity<?> getContactsCount(@PathVariable Integer userId, @RequestBody UserListDTO userListDTO) {
		logger.debug("from getContactsCount()");
		try {
			Map<String, Object> resultMap = userListService.getContactsCount(userId, userListDTO);
			return resultMap.isEmpty()
					? (ResponseUtil.getResponse(HttpStatus.OK, StatusCodeConstants.NO_CONTACTS_FOUND, null))
					: (ResponseUtil.getResponse(HttpStatus.OK, StatusCodeConstants.All_CONTACTS_FOUND, resultMap));
		} catch (Exception e) {
			logger.error("error occurred in getContactsCount() :" + e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
		}
	}

	/*********** sending partner email on email button click ********************/
	@RequestMapping(value = { "/send-partner-mail" }, method = RequestMethod.POST)
	public ResponseEntity<?> sendPartnerMail(@RequestBody Pagination pagination) {
		try {
			XtremandResponse xtremandResponse = userListService.sendPartnerMail(pagination);
			return ResponseEntity.status(HttpStatus.OK).body(xtremandResponse);
		} catch (Exception e) {
			logger.error("error occurred in sendPartnerMail() :" + e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
		}
	}

	@GetMapping(value = "{id}/process-userlist")
	public ResponseEntity<?> processUserListById(@PathVariable Integer id) {
		try {
			userListService.findUsersAndProcessTheList(id);
			return ResponseEntity.status(HttpStatus.OK).body(Collections.singletonMap("message", "success"));
		} catch (Exception e) {
			logger.error("error occurred in processUserList() :" + id);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
		}
	}

	@RequestMapping(value = { "{assignLeads}/makeContactsValid" }, method = RequestMethod.POST)
	public ResponseEntity<?> makeContactsValid(@RequestParam Integer userId, @RequestBody List<Integer> userIds,
			@PathVariable boolean assignLeads) {
		try {
			XtremandResponse response = userListService.makeContactsValid(userIds, userId, assignLeads);
			return ResponseEntity.status(HttpStatus.OK).body(response);
		} catch (Exception e) {
			logger.error("error occurred in makeContactsValid() userIds:" + userIds + " and customerId :" + userId);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
		}
	}

	@RequestMapping(value = { "valid-contacts-count" }, method = RequestMethod.POST)
	public ResponseEntity<?> validContactsCount(@RequestParam Integer userId, @RequestBody List<Integer> userListIds) {
		try {
			Map<String, Object> resultMap = userListService.validContactsCount(userListIds, userId);
			return ResponseEntity.status(HttpStatus.OK).body(resultMap);
		} catch (Exception e) {
			logger.error(
					"error occurred in validContactsCount() userListIds:" + userListIds + " and customerId :" + userId);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
		}
	}

	@RequestMapping(value = "/{userId}/{isPartnerUserList}/has-access", method = RequestMethod.GET)
	public ResponseEntity<?> hascAccess(@PathVariable Integer userId, @PathVariable boolean isPartnerUserList) {
		XtremandResponse response = userListService.hasAccess(isPartnerUserList, userId, false);
		return ResponseEntity.status(HttpStatus.OK).body(response);
	}

	@PostMapping(value = "validatePartners/{userListId}")
	@ResponseBody
	public ResponseEntity<XtremandResponse> validatePartners(@RequestBody List<User> users,
			@PathVariable Integer userListId) {
		return ResponseEntity.ok(userListService.validatePartners(users, userListId));
	}

	@PostMapping(value = "getContactsLimit/{userId}")
	@ResponseBody
	public ResponseEntity<XtremandResponse> getContactsLimit(@RequestBody List<User> users,
			@PathVariable Integer userId) {
		return ResponseEntity.ok(userListService.getContactsLimit(users, userId));
	}

	@GetMapping(value = "partner-emails/{userId}")
	@ResponseBody
	public XtremandResponse listPartnerEmailsByOrganization(@PathVariable Integer userId) {
		return userListService.listPartnerEmailsByOrganization(userId);
	}

	@RequestMapping(value = { "/save-assign-leads-list/{userId}" }, method = RequestMethod.POST)
	public ResponseEntity<?> saveLeadsList(@RequestBody UserUserListWrapper userUserListWrapper,
			@PathVariable Integer userId) {
		ResponseEntity<?> response = null;
		XtremandResponse xtremandResponse = userListService.saveLeadsList(userUserListWrapper, userId);
		response = ResponseEntity.status(HttpStatus.OK).body(xtremandResponse);
		return response;
	}

	/***************** to get list of userLists *************************/
	@RequestMapping(value = { "/assign-leads-lists/{userId}" }, method = RequestMethod.POST)
	public ResponseEntity<?> assignedLeadLists(@RequestBody Pagination pagination, @PathVariable Integer userId) {
		logger.debug("entered into assignedLeadLists()");
		try {
			Map<String, Object> resultMap = userListService.assignedLeadLists(userId, pagination);
			return resultMap.isEmpty()
					? (ResponseUtil.getResponse(HttpStatus.OK, StatusCodeConstants.NO_CONTACTLIST_FOUND, null))
					: (ResponseUtil.getResponse(HttpStatus.OK, StatusCodeConstants.CONTACTLIST_FOUND, resultMap));
		} catch (Exception e) {
			logger.error("error occurred in userlists() :" + e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(String.valueOf(e.getCause()));
		}
	}

	@RequestMapping(value = { "/list-partners/{userId}" }, method = RequestMethod.POST)
	public Map<String, Object> listPartnersByVendorCompanyId(@PathVariable Integer userId,
			@RequestBody Pagination pagination) {
		return userListService.listPartnersByVendorCompanyId(userId, pagination);
	}

	@RequestMapping(value = { "/save-as-share-leads/{userId}" }, method = RequestMethod.POST)
	public XtremandResponse saveAsShareLeadsList(@PathVariable Integer userId, @RequestBody UserListDTO userlistDTO) {
		return userListService.saveAsShareLeadsList(userId, userlistDTO);
	}

	@PostMapping(value = "deleteFromAllContactLists/{loggedInUserId}")
	public ResponseEntity<XtremandResponse> deleteFromAllContactLists(@RequestBody List<Integer> contactIds,
			@PathVariable Integer loggedInUserId) {
		return ResponseEntity.ok(userListService.deleteContactFromAllContactLists(contactIds, loggedInUserId));
	}

	@PostMapping(value = "findUsersByUserListId")
	public ResponseEntity<XtremandResponse> findUsersByUserListId(@RequestBody Pagination pagination) {
		return ResponseEntity.ok(userListService.findUsersByUserListId(pagination));
	}

	@PostMapping(value = "findContactAndPartnerLists")
	public ResponseEntity<XtremandResponse> findContactAndPartnerLists(@RequestBody Pagination pagination) {
		return ResponseEntity.ok(userListService.findContactAndPartnerLists(pagination));
	}

	@PostMapping(value = "findAllAndValidUsersCount/{loggedInUserId}")
	public ResponseEntity<Map<String, Object>> findAllAndValidUsersCount(@RequestBody List<Integer> userListIds,
			@PathVariable Integer loggedInUserId) {
		return ResponseEntity.ok(userListService.findAllAndValidUsersCount(userListIds, loggedInUserId));
	}

	@RequestMapping(value = { "/share-leads-list-to-partners" }, method = RequestMethod.POST)
	public ResponseEntity<?> shareLeadsListToPartners(@RequestBody ShareLeadsDTO shareLeadsDTO) {
		XtremandResponse xtremandResponse = userListService.shareLeadsListToPartners(shareLeadsDTO);
		ResponseEntity<?> response = ResponseEntity.status(HttpStatus.OK).body(xtremandResponse);
		return response;
	}

	@RequestMapping(value = { "/update-share-leads-list-data" }, method = RequestMethod.GET)
	public XtremandResponse updateShareListData() {
		XtremandResponse xtremandResponse = userListService.updateShareListData();
		return xtremandResponse;
	}

	@RequestMapping(value = { "{userListId}/list-shared-details" }, method = RequestMethod.POST)
	public XtremandResponse listSharedDetails(@PathVariable Integer userListId, @RequestBody Pagination pagination) {
		return userListService.listSharedDetails(userListId, pagination);
	}

	@RequestMapping(value = { "/save-as-new-list/{userId}" }, method = RequestMethod.POST)
	public XtremandResponse saveAsUserList(@PathVariable Integer userId, @RequestBody UserListDTO userlistDTO) {
		return userListService.saveAsNewUserList(userId, userlistDTO);
	}

	@GetMapping(value = "download-default-list/{userId}")
	public ResponseEntity<?> downloadPartnerListCsv(@PathVariable Integer userId, HttpServletResponse response)
			throws IOException {
		userListService.downloadPartnerListCsv(response, userId);
		StatusCode statusCode = StatusCode.getstatuscode(StatusCodeConstants.DOWNLOAD_USERLIST_SUCCESS);
		return ResponseEntity.status(HttpStatus.OK).headers(HttpHeaderUtil.getHeader(statusCode)).body(null);
	}

	/************ XNFR-278 ********/
	@PostMapping(value = "findGroupsForMerging")
	public ResponseEntity<XtremandResponse> findGroupsForMerging(@RequestBody Pagination pagination) {
		return ResponseEntity.ok(userListService.findPartnerGroupsForMerging(pagination));
	}

	/************ XNFR-278 ********/
	@PostMapping(value = "copyGroupUsers")
	public ResponseEntity<XtremandResponse> copyGroupUsers(@RequestBody CopyGroupUsersDTO copyGroupUsersDTO) {
		XtremandResponse response = new XtremandResponse();
		boolean hasError = false;
		try {
			response = userListService.copyGroupUsers(copyGroupUsersDTO);
			return ResponseEntity.ok(response);
		} catch (DataIntegrityViolationException e) {
			hasError = true;
			if (e.getMessage().indexOf("user_userlist_unique") > -1) {
				throw new DuplicateEntryException("Duplicate User");
			} else {
				throw new XamplifyDataAccessException(e);
			}
		} catch (DuplicateEntryException e) {
			hasError = true;
			throw new DuplicateEntryException(e.getMessage());
		} catch (BadRequestException e) {
			hasError = true;
			throw new BadRequestException(e.getMessage());
		} catch (Exception e) {
			hasError = true;
			throw new XamplifyDataAccessException(e);
		} finally {
			/*** XNFR-768 ***/
			if(response.getStatusCode() == 200 && copyGroupUsersDTO.isMove()) {
				userListService.removeUsersFromUserList(copyGroupUsersDTO.getUserGroupId(), copyGroupUsersDTO.getLoggedInUserId(), 
						XamplifyUtils.convertSetToList(copyGroupUsersDTO.getUserIds()));
			} else {
				publishAndShareWhiteLabelContentToCopiedPartners(response, hasError, copyGroupUsersDTO);
			}
		}

	}

	@SuppressWarnings("unchecked")
	private void publishAndShareWhiteLabelContentToCopiedPartners(XtremandResponse response, boolean hasError,
			CopyGroupUsersDTO copyGroupUsersDTO) {
		if (!hasError && XamplifyUtils.isSuccessfulResponse(response)) {
			List<CopiedUserListUsersDTO> copiedUserListUsersDTOs = (List<CopiedUserListUsersDTO>) response.getMap()
					.get(XamplifyConstants.COPIED_USERLIST_USERS_KEY);
			if (XamplifyUtils.isNotEmptyList(copiedUserListUsersDTOs)) {
				asyncComponent.publishAndWhiteLabelContentToCopiedPartners(copiedUserListUsersDTOs, copyGroupUsersDTO);
			}
		}
	}

	@GetMapping("/findAllUnsubscribeReasons/{userId}")
	public ResponseEntity<XtremandResponse> findAllUnsubscribedReasons(@PathVariable Integer userId) {
		Integer companyId = userService.getCompanyIdByUserId(userId);
		return ResponseEntity.ok(unsubscribeService.findAll(companyId));
	}

	@PostMapping(value = "/unsubscribe-or-resubscribe-User/{loggedInUserId}")
	public ResponseEntity<?> unsubscribeOrResubscribeUser(@PathVariable Integer loggedInUserId,
			@RequestBody UnsubscribeUserDTO unsubscribeUserDTO) {
		Map<String, Object> resultMap = userListService.unsubscribeOrResubscribeUser(loggedInUserId,
				unsubscribeUserDTO);
		return ResponseEntity.status(HttpStatus.OK).body(resultMap);
	}

	@PostMapping(value = "/validate-partners/{userListId}/{loggedInUserId}")
	public ResponseEntity<XtremandResponse> validatePartners(@PathVariable Integer userListId,
			@PathVariable Integer loggedInUserId, @RequestBody List<User> partners) {
		return ResponseEntity.ok(userListService.validatePartners(loggedInUserId, userListId, partners));
	}

	@GetMapping("/partner-details/{partnerId}")
	public ResponseEntity<?> getPartnerDetails(@PathVariable Integer partnerId) {
		return ResponseEntity.ok(userService.getCompanyProfileByUserId(partnerId));
	}

	@RequestMapping(value = {
			"validatePartnerCompany/{loggedInUserId}/{partnerCompanyId}/{isAdd}" }, method = RequestMethod.POST)
	public ResponseEntity<XtremandResponse> validatePartnerCompany(@PathVariable Integer loggedInUserId,
			@PathVariable Integer partnerCompanyId, @PathVariable boolean isAdd, @RequestBody User user) {
		return ResponseEntity
				.ok(userListService.validatePartnerCompany(loggedInUserId, user, partnerCompanyId, false, isAdd));
	}

	@RequestMapping(value = { "excluded-user-make-as-valid/{customerId}" }, method = RequestMethod.POST)
	public ResponseEntity<?> excludedUserMakeAsValid(@PathVariable Integer customerId, @RequestBody UserDTO user) {
		try {
			XtremandResponse response = userListService.excludedUserMakeAsValid(customerId, user);
			return ResponseEntity.status(HttpStatus.OK).body(response);
		} catch (Exception e) {
			logger.error("error occurred in excludedUserMakeAsValid() userId:" + user.getId() + " and emailId :"
					+ user.getEmailId());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
		}
	}

	/***** XNFR-472 *****/
	@PostMapping(value = "/download/{userId}")
	public ResponseEntity<XtremandResponse> downloadUserList(
			@RequestBody UserListPaginationWrapper userListPaginationWrapper, @PathVariable Integer userId) {
		XtremandResponse response = new XtremandResponse();
		try {
			String moduleName = StringUtils
					.deleteWhitespace(userListPaginationWrapper.getUserList().getModuleName().toLowerCase());
			response = userListService.downloadUserList(userId, moduleName);
			return ResponseEntity.ok(response);
		} catch (Exception e) {
			throw new XamplifyDataAccessException(e);
		} finally {
			if (response.getStatusCode() != 401) {
				Integer downloadDataInfoId = (Integer) response.getData();
				asyncComponent.uploadUserList(userId, userListPaginationWrapper, downloadDataInfoId);
			}
		}
	}

	@GetMapping("/list/userListId/{userListId}")
	public ResponseEntity<XtremandResponse> getUserListByUserListId(@PathVariable Integer userListId) {
		return ResponseEntity.ok(userListService.getUserListByUserListId(userListId));
	}

	@GetMapping("/findUserByUserIdAndUserListId/{userId}/{userListId}")
	public ResponseEntity<XtremandResponse> findUserByUserIdAndUserListId(@PathVariable Integer userId,
			@PathVariable Integer userListId, @RequestParam Integer loggedInUserId) {
		return ResponseEntity.ok(userListService.findUserByUserIdAndUserListId(userId, userListId, loggedInUserId));
	}

	@GetMapping("/findUserListDetails/{userListId}/{isFromCompanyModule}")
	public ResponseEntity<XtremandResponse> findUserListDetails(@PathVariable Integer userListId,
			@PathVariable boolean isFromCompanyModule) {
		return ResponseEntity.ok(userListService.findUserListDetails(userListId, isFromCompanyModule));
	}

	@GetMapping(value = "download-default-contact-csv/{userId}")
	public ResponseEntity<Void> downloadContactListCsv(@PathVariable Integer userId, HttpServletResponse response) {
		userListService.downloadContactListCsv(userId, response);
		StatusCode statusCode = StatusCode.getstatuscode(StatusCodeConstants.DOWNLOAD_USERLIST_SUCCESS);
		return ResponseEntity.status(HttpStatus.OK).headers(HttpHeaderUtil.getHeader(statusCode)).body(null);
	}
	
	@GetMapping("/fetchContactsAndCountByUserListId/{userListId}")
	public ResponseEntity<XtremandResponse> fetchContactsAndCountByUserListId(@PathVariable Integer userListId) {
		return ResponseEntity.ok(userListService.fetchContactsAndCountByUserListId(userListId));
	}
	

	@PostMapping(value = "welcomeEmails/{loggedInUserId}")
	public ResponseEntity<XtremandResponse> getWelcomeEmailsList(@RequestBody Pagination pagination,@PathVariable("loggedInUserId") Integer userId) {
		return ResponseEntity.ok(userListService.getWelcomeEmailsList(userId,pagination));
	}
	@GetMapping("downloadWelcomeEmailsList/userId/{userId}")
	public void downloadWelcomeEmailsList(@PathVariable Integer userId, @Valid Pageable pageable,
			HttpServletResponse response) {
		userListService.downloadWelcomeEmailsList(pageable,userId,response);
	}
}
