package com.xtremand.userlist.service;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;

import com.xtremand.campaign.bom.UnsubscribeUserDTO;
import com.xtremand.common.bom.Criteria;
import com.xtremand.common.bom.FindLevel;
import com.xtremand.common.bom.Pagination;
import com.xtremand.company.dto.CompanyProfileDTO;
import com.xtremand.form.bom.Form;
import com.xtremand.formbeans.UserDTO;
import com.xtremand.formbeans.UserListDTO;
import com.xtremand.formbeans.UserListPaginationWrapper;
import com.xtremand.formbeans.XtremandResponse;
import com.xtremand.user.bom.ShareLeadsDTO;
import com.xtremand.user.bom.User;
import com.xtremand.user.bom.UserList;
import com.xtremand.user.bom.UserList.SocialNetwork;
import com.xtremand.user.bom.UserPaginationWrapper;
import com.xtremand.user.bom.UserUserList;
import com.xtremand.user.bom.UserUserListWrapper;
import com.xtremand.user.exception.UserDataAccessException;
import com.xtremand.user.list.dto.CopyGroupUsersDTO;
import com.xtremand.userlist.exception.UserListException;
import com.xtremand.util.dto.Pageable;
import com.xtremand.util.dto.UserListOperationsAsyncDTO;

public interface UserListService {
	public UserList findByPrimaryKey(Serializable pk, FindLevel[] levels) throws UserListException;

	public List<UserList> find(List<Criteria> criterias, FindLevel[] levels) throws UserListException;

	public XtremandResponse saveUserList(Integer userId, UserUserListWrapper userUserListWrapper)
			throws UserListException;

	public void processContacts(List<User> nonProcessedUsers, boolean isPartnerUserList, UserList userList, User user);

	public void sendContactListMail(User user, UserList userList, List<Integer> totalUnsubscribedUserIds);

	public XtremandResponse removeUserList(Integer id, Integer userId, boolean deleteTeamMemberPartnerList)
			throws UserListException;

	public Map<String, Object> updateUserList(UserUserListWrapper userUserListWrapper, Integer customerId,
			String companyProfileName, UserListOperationsAsyncDTO userListOperationsAsyncDTO) throws UserListException;

	public XtremandResponse renameUserList(UserListDTO userListDTO, Integer userId) throws UserListException;

	public Map<String, Object> userlists(Integer userId, Pagination pagination) throws UserListException;

	public XtremandResponse sendPartnerMail(Pagination pagination) throws UserDataAccessException;

	public Map<String, Object> removeUsersFromUserList(Integer userListId, Integer customerId,
			List<Integer> removeUserIdsList) throws UserListException;

	public HttpServletResponse downloadUserList(UserListPaginationWrapper userListPaginationWrapper,
			HttpServletResponse response) throws UserListException;

	public Map<String, Object> listAllUserListContacts(UserListPaginationWrapper userListPaginationWrapper,
			Integer userId);

	public Map<String, Object> listuserListContacts(UserListPaginationWrapper userListPaginationWrapper,
			Integer userId);

	public Map<String, List<User>> getSubscribedAndUnsubscribedUsers(List<User> exUsers);

	public Map<String, List<User>> getSubscribedAndUnsubscribedUsers(List<User> exUsers, Integer customerCompanyId);

	public XtremandResponse removeInvalidUsers(Integer userId, List<Integer> removeUserIds, boolean assignLeads);

	public Map<String, Object> getContactsCount(Integer userId, UserListDTO userListDTO) throws UserListException;

	public Map<String, Boolean> checkAuthentications(Integer userId);

	public List<String> listUserlistNames(Integer userId) throws UserListException;

	public void unlinkAccount(Integer userId, SocialNetwork socialNetwork, boolean deleteContactList)
			throws UserListException;

	public Map<String, Object> updateUserDetails(UserPaginationWrapper userPaginationWrapper, Integer userListId,
			Integer userId) throws UserDataAccessException;

	public List<Integer> partnerCompanyIds(Integer userId, Integer userListId);

	public Object listPartners(Integer userId, Pagination pagination);

	public void startProcessUserLists();

	public void processUserList(Integer id);

	public Map<String, Object> listCampaignUserLists(Pagination pagination);

	public User updateUserDetailsWithCampaignUserListData(Integer campaignId, User user);

	public void resubscribeUser(Integer userId, Integer customerId);

	public UserUserList getByIdAndUserId(Integer id, Integer userId);

	public UserListDTO getOrCreateDefaultPartnerList(Integer userId);

	public UserList getDefaultPartnerList(User user);

	public UserList createPartnerList(User loggedinUser, String userListName, boolean isDefault, String csvPath);

	public UserList CreateOrGetDefaultPartnerList(User loggedinUser);

	public XtremandResponse makeContactsValid(List<Integer> userIds, Integer customerId, boolean assignLeads);

	public Map<String, Object> validContactsCount(List<Integer> userListIds, Integer customerId);

	public XtremandResponse hasAccess(boolean isPartnerUserList, Integer userId, boolean assignLeads);

	public boolean getAccess(boolean isPartnerUserList, Integer userId, boolean assignLeads);

	public XtremandResponse validatePartners(List<User> users, Integer userListId);

	public XtremandResponse getContactsLimit(List<User> users, Integer userId);

	XtremandResponse removeMarketoMasterContactList(Integer companyId) throws UserListException;

	public XtremandResponse removeZeroUsersLists(XtremandResponse xtremandResponse);

	public XtremandResponse listPartnerEmailsByOrganization(Integer userId);

	public XtremandResponse saveLeadsList(UserUserListWrapper userUserListWrapper, Integer userId);

	public Map<String, Object> assignedLeadLists(Integer userId, Pagination pagination) throws UserListException;

	public Map<String, Object> listPartnersByVendorCompanyId(Integer userId, Pagination pagination);

	public XtremandResponse saveAsShareLeadsList(Integer userId, UserListDTO userlistDTO);

	public Integer shareLeadsListsAvailable(Integer userId, Integer vendorCompanyId);

	public XtremandResponse findUsersByUserListId(Pagination pagination);

	public XtremandResponse findContactAndPartnerLists(Pagination pagination);

	public Map<String, Object> findAllAndValidUsersCount(List<Integer> userListIds, Integer loggedInUserId);

	public void createFormContactList(Form form, String listName);

	public UserList getUserListByNameAndCompany(String name, Integer companyId);

	public UserList getFormContactListForOrgAdmin(Integer formId, Integer companyId);

	public UserList getFormContactListByLandingPageId(Integer formId, Integer companyId, Integer landingPageId);

	public UserList getFormContactListByCampaignId(Integer formId, Integer campaignId);

	public Map<String, Object> updateContactList(UserList userList, Set<UserDTO> users, Integer customerId);

	public void createFormContactList(Form form, String listName, Integer listOwnerUserId);

	public XtremandResponse shareLeadsListToPartners(ShareLeadsDTO shareLeadsDTO);

	public XtremandResponse updateShareListData();

	public XtremandResponse listSharedDetails(Integer userListId, Pagination pagination);

	public void shareLeadsListToNewlyAddedTeamMembers(Integer teamMemberId, Integer teamMemberCompanyId);

	public List<Integer> getUnsubscribedUsers(Integer customerCompanyId);

	public void deleteUsersFromUserList(UserList userList, Integer customerId, List<Integer> removeUserIdsList);

	public XtremandResponse removeContactList(boolean access, UserList userList, XtremandResponse xtremandResponse)
			throws UserListException;

	public List<String> getPartnerListNamesByCompany(Integer companyId);

	public XtremandResponse saveAsNewUserList(Integer userId, UserListDTO userlistDTO);

	public HttpServletResponse downloadPartnerListCsv(HttpServletResponse httpServletResponse, Integer userId);

	/************* XNFR-98 ****************/
	public void deleteTeamMemberPartnerList(Integer userListId, Integer userId);

	public UserUserList createUserUserList(User user, UserList userList, UserDTO userDTO, User loggedInUser,
			boolean isGdprOn);

	public UserUserList getUserUserList(User user, UserList userList, UserDTO userDTO, User loggedInUser,
			boolean isGdprOn);

	/************ XNFR-278 ********/
	public XtremandResponse findPartnerGroupsForMerging(Pagination pagination);

	public XtremandResponse copyGroupUsers(CopyGroupUsersDTO copyGroupUsersDTO);

	/************ XNFR-278 ********/
	public Map<String, Object> unsubscribeOrResubscribeUser(Integer loggedInUserId,
			UnsubscribeUserDTO unsubscribeUserDTO);

	public XtremandResponse validatePartners(Integer loggedInUserId, Integer userListId, List<User> partners);

	public CompanyProfileDTO getPartnerDetails(Integer partnerId);

	public void downloadTotalContactsOfAllPartnersByVendorCompanyId(Integer vendorCompanyId,
			HttpServletResponse httpServletResponse, String fileName);

	public void downloadTotalContactsCountOfAllPartners(Integer companyId, HttpServletResponse httpServletResponse,
			String fileName);

	public XtremandResponse validatePartnerCompany(Integer loggedInUserId, User partner, Integer partnerCompanyId,
			boolean isCompanyProfileSubmit, boolean isUpdate);

	public XtremandResponse excludedUserMakeAsValid(Integer customerId, UserDTO userDTO);

	public void updateFormContactList(Form form);

	public XtremandResponse downloadUserList(Integer userId, String moduleName);

	public XtremandResponse getUserListByUserListId(Integer userListId);

	/**** XNFR-450 ***/

	public void findUsersAndProcessTheList(Integer id);

	/**** Added By Sravan To Fix Production Issue On 24/04/2024 *****/

	public Integer findAllUserListUsersCount(Integer companyId);

	public Integer findAllActiveUserListUsersCount(Integer companyId);

	public Integer findAllInActiveUserListUsersCount(Integer companyId);

	public Integer findAllInvalidUserListUsersCount(Integer companyId);

	public Integer findAllUnsubscribeUserListUsersCount(Integer companyId);

	public Integer findAllExcludedUserListUsersCount(Integer companyId);

	public Map<String, Object> listUserListscontacts(Pagination pagination, List<Integer> userListIds,
			User loggedInUser, UserListDTO userListDTO);

	public XtremandResponse deleteAllContacts(Integer loggedInUserId);

	/*** XNFR-553 ***/
	public XtremandResponse findUserByUserIdAndUserListId(Integer userId, Integer userListId, Integer loggedInUserId);

	public XtremandResponse findUserListDetails(Integer userListId, boolean isFromCompanyModule);

	/*** XNFR-553 ***/

	HttpServletResponse downloadContactListCsv(Integer userId, HttpServletResponse httpServletResponse);

	public UserList createFormContactListAndGetContactListId(Form form, String listName);

	public Integer getDefaultPartnerListIdByCompanyId(Integer companyId);

	/** XNFR-848 **/
	XtremandResponse fetchContactsAndCountByUserListId(Integer userListId);

	List<Integer> fetchUserIdsByUserListId(Integer userListId);

	/** XNFR-867 **/

	public XtremandResponse getWelcomeEmailsList(Integer userId, Pagination pagination);

	public HttpServletResponse downloadWelcomeEmailsList(Pageable pageable, Integer userId,
			HttpServletResponse response);

	public XtremandResponse deleteContactFromAllContactLists(List<Integer> contactIds, Integer loggedInUserId);

}
