package com.xtremand.userlist.dao;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.Session;
import org.hibernate.criterion.Criterion;

import com.xtremand.campaign.dto.OneClickLaunchSharedLeadsDTO;
import com.xtremand.campaign.dto.ReceiverMergeTagsDTO;
import com.xtremand.common.bom.Criteria;
import com.xtremand.common.bom.FindLevel;
import com.xtremand.common.bom.Pagination;
import com.xtremand.company.dto.CompanyProfileDTO;
import com.xtremand.dao.util.FinderDAO;
import com.xtremand.dashboard.buttons.dto.DashboardButtonsPartnersDTO;
import com.xtremand.formbeans.UserDTO;
import com.xtremand.formbeans.UserListDTO;
import com.xtremand.partner.bom.UpdatedContactsHistory;
import com.xtremand.user.bom.ShareListPartner;
import com.xtremand.user.bom.User;
import com.xtremand.user.bom.UserList;
import com.xtremand.user.bom.UserListDetails;
import com.xtremand.user.bom.UserListUsersCount;
import com.xtremand.user.bom.UserUserList;
import com.xtremand.user.list.dto.ContactsCSVDTO;
import com.xtremand.user.list.dto.ContactsCountDTO;
import com.xtremand.user.list.dto.CopyGroupUsersDTO;
import com.xtremand.util.dto.UserListAndUserId;
import com.xtremand.util.dto.UserUserListDTO;

public interface UserListDAO extends FinderDAO<UserList> {

	public void deleteByPrimaryKey(Integer id);

	public void resubscribeUser(Integer userId, Integer customerCompanyId);

	public void deletePartnersFromPartnerLists(List<Integer> userIdsList, List<Integer> userListIds);

	public Integer saveUserList(UserList userList);

	public UserList getUserListByCustomerAndName(Integer customerId, String name);

	public void removeInvalidUser(Integer userId, Integer userListId);

	public List<String> listUserlistNames(Integer[] userIdArray);

	public Map<String, Object> findCampaignUserLists(List<Criteria> criterias, FindLevel[] levels,
			Pagination pagination, List<Integer> userListIds);

	public UserUserList getUserUserList(Integer userId, List<Integer> userListIds);

	public UserUserList getCampaignUserUserList(Integer userId, List<Integer> userListIds, Integer campaignId);

	public UserUserList getByIdAndUserId(Integer id, Integer userId);

	public Integer getDefaultPartnerListId(Integer customerId);

	public List<Integer> getPartnerCompanyIds(Integer userId, Integer userListId);

	public UserList getDefaultPartnerList(Integer companyId);

	public void saveUserUserList(UserUserList userUserList);

	public void makeContactsValid(List<Integer> userIds);

	public void updatedContactsHistory(List<UpdatedContactsHistory> updatedContactsHistoryList);

	public void updateUserListProcessingStatus(UserList userList);

	public List<Integer> listLegalBasisByContactListIdAndUserId(Integer contactListId, Integer userId);

	public List<Integer> getContactUploadTeamMemberIds(Integer companyId);

	public Map<String, Object> find(User user, Pagination pagination, FindLevel[] levels);

	public List<Criterion> getContactsCriterias(User user, Pagination pagination);

	public Collection<UserList> findLists(List<Criterion> criterions, FindLevel[] levels);

	public List<Integer> getCampaignUploadTeamMemberIds(Integer companyId);

	public List<Integer> getCampaignUploadTeamMemberIdsAndSupervisorIds(Integer companyId);

	public List<Integer> getContactUploadTeamMemberAndSupervisorsIds(Integer companyId);

	boolean isLegalBasisExists(Integer userId, Integer userListId, Integer legalBasisId);

	public void deleteLegalBasis(List<Integer> userIds, Integer userListId);

	public List<UserDTO> listPartnersByUserListId(Integer userListId);

	public Integer getCompanyIdByUserListId(Integer userListId);

	public boolean isDefaultPartnerList(Integer userListId);

	public UserList getMarketoMasterContactList(Integer companyId);

	public UserList getDealContactList(Integer companyId);

	public UserUserList getContactFromDealContactList(Integer userId, Integer userListId);

	public ArrayList<Integer> getUserListIdsBYUserId(boolean isPartnerUserList, Integer userId, Integer companyId);

	public List<String> listPartnerEmailsByOrganization(Integer companyId);

	public List<String> listUserlistNames(List<Criterion> criterions);

	public Map<String, Object> fetchUserListUsers(User loggedInUser, List<Integer> userListIds, String searchHQL,
			String sortSQL, String dataSortSQL, String sortColumnSQL, String sortingOrder, Pagination pagination,
			UserListDTO userListDTO);

	public List<Integer> getUserListIds(List<Criterion> criterions);

	public List<Object[]> getExcludedUserIds(List<Integer> userListIds, Integer customerCompanyId);

	public List<Object[]> getExcludedAssignedLeasdUserIds(List<Integer> userListIds, Integer customerCompanyId);

	public List<Object[]> getExcludedSharedUserIds(List<Integer> userListIds, Integer logedInUserId,
			Integer customerCompanyId);

	public ContactsCountDTO getContactsCount(List<Integer> userListIds, Integer customerCompanyId,
			UserListDTO userListDTO, Integer loggedInUserId);

	public ArrayList<Integer> getUserListIdsBYLeadId(boolean isPartnerUserList, Integer userId, Integer companyId);

	public Integer shareLeadsListsAvailable(Integer companyId, Integer vendorCompanyId);

	public List<UserListAndUserId> findUserIdsAndUserListIds(List<Integer> userListIds);

	public Map<String, Object> findUsersByUserListId(Pagination pagination);

	public Integer validContactsCount(List<Integer> userListIds, Integer customerCompanyId);

	public Integer allUsersCount(List<Integer> userListIds);

	public Map<String, Object> findContactAndPartnerLists(Pagination pagination);

	public Integer findAllUsersCountByUserListIds(List<Integer> userListIds);

	public List<Integer> findSelectedUserListIdsByCampaignIds(Integer campaignId);

	public Map<String, Object> listUserLists(List<Criteria> criterias, Pagination pagination);

	public Map<String, Object> listShareLists(Pagination pagination);

	public List<String> findUserListNamesByUserListIds(List<Integer> userListIds);

	public UserListUsersCount getUserListUsersCount(Integer userListId);

	public List<Integer> getUserListIds(List<Criterion> criterions, Pagination pagination);

	public ReceiverMergeTagsDTO findReceiverMergeTagsInfo(Integer campaignId, Integer userListId);

	public List<Integer> getNewlyAddedUserIdsForShareCampaign(Integer campaignId, Integer userListId);

	public UserUserList getUserUserList(Integer userId, Integer userListId);

	public Set<User> setSubscribeOrUnsubscribeUsers(Set<Integer> newlyAddedUserIds, Integer userListId,
			Integer companyId);

	public UserList getUserListByNameAndCompany(String name, Integer companyId);

	public UserList getFormContactListForOrgAdmin(Integer formId, Integer companyId);

	public UserList getFormContactListByLandingPageId(Integer formId, Integer companyId, Integer landingPageId);

	public Integer findAllPartnersCountByUserId(Integer userId);

	public UserList getFormContactListByCampaignId(Integer formId, Integer campaignId);

	public Map<String, Object> listSharedLists(Pagination pagination);

	public List<Integer> getSharedPartnerIds(Integer userListId, Integer partnershipId);

	public List<Object[]> getShareListPartnerIds(Integer partnershipId);

	public void deleteShareListPartnerMapping(Integer shareListPartnerId);

	public void deleteShareListPartner(Integer shareListPartnerId);

	public List<Integer> listSharedLeadsListIds(boolean isVanityUrlFilter, Integer partnerId,
			Integer assignedByCompanyId);

	public void deleteShareList(Integer vendorId, List<Integer> partnerIds);

	public Integer getShareListPartnerId(Integer partnershipId, Integer userListId);

	public void updateUserList(Integer userListId);

	public ContactsCountDTO getContactsCount(List<Integer> userListIds, Integer customerCompanyId);

	public Map<String, Object> getListSharedCompanyDetails(Integer userListId, Pagination pagination);

	public boolean isShareLeadsListAssignedToPartner(Integer partnerId);

	public boolean isShareLeadsListAssignedToPartner(Integer partnershipId, Integer partnerId);

	public List<ShareListPartner> getShareListPartner(Integer partnershipId);

	public boolean isUserListNameExists(UserListDTO userListDTO);

	public List<Integer> getUnsubscribedUsers(Integer customerCompanyId);

	/******** XNFR-81 ********/
	public UserList findActivePartnerListByCompanyId(Integer companyId);

	public UserList findInActivePartnerListByCompanyId(Integer companyId);

	public boolean isActiveMasterPartnerListExists(Integer companyId);

	public boolean isInActiveMasterPartnerListExists(Integer companyId);

	public void saveUserUserList(UserList userList, UserUserListDTO userListDTO, User user);

	public List<Integer> getUserListIdsByRemovePartnerIds(Integer companyId, List<Integer> userIds);

	public List<Integer> getTeamMemberGroupedPartnerIds(Integer teamMemberId);

	/******** XNFR-98 *************/
	public Integer findUserListIdByTeamMemberId(Integer teamMemberId, Integer companyId);

	public void updateTeamMemberPartnerList(Integer userListId);

	public boolean isTeamMemberPartnerList(Integer userListId);

	public void updateTeamMemberPartnerListName(Integer teamMemberId, String fullName);

	public void deleteUserUserListByUserListId(Integer userListId);

	public void deletePartnerFromTeamMemberPartnerListByPartnerIdAndTeamMemberIds(Integer partnerId,
			List<Integer> teamMemberIds);

	public UserDTO findPartnerDetailsByPartnerListId(Integer partnerListId, Integer partnerId);

	public boolean isPartnerExistsInPartnerList(Integer partnerId, Integer partnerListId);

	public Integer findUserListIdByTeamMemberId(Integer teamMemberId);

	public List<Integer> findUserListIdsByTeamMemberIds(List<Integer> teamMemberIds);

	public List<Integer> findTeamMemberPartnerListIdsByCompanyId(Integer companyId);

	public void deleteFromUserUserListByUserIdAndUserListIds(List<Integer> partnerIds, List<Integer> userListIds);

	public void deleteUsersFromUserListByUserListIdAndUserIds(Integer userListId, List<Integer> userIds);

	/*********** XNFR-107 ***********/
	public UserDTO findPartnerDetailsByPartnershipId(Integer partnershipId);

	public boolean isUserExists(Integer userListId, String emailId);

	public void deleteLegalBasis(Session session, Integer userId, List<Integer> userListIds);

	/********** XNFR-125 *********/
	public Integer findShareListPartnerIdByUserListId(Integer userListId);

	public void updateSharedToCompanyById(Integer id);

	public void updateShareListCompanyIdAsNull(Integer shareListId);

	public List<OneClickLaunchSharedLeadsDTO> findUnAssignedSharedLeadsListsInCampaign(Integer companyId);

	public Integer findOneClickLaunchCampaignPartnershipIdByUserListId(Integer userListId);

	public Integer findOneClickLaunchCampaignPartnershipIdByUserListIds(List<Integer> userListIds);

	public List<UserUserListDTO> findSubscribedUserUserLists(List<Integer> unsubscribedUserIds,
			List<Integer> userListIds);

	public List<Integer> findValidEmailUserIdsByUserListIds(List<Integer> userListIds);

	public boolean isDuplicateListName(String listName, Integer companyId, String module);

	public List<Integer> findUserIdsByUserListId(Integer userListId);

	public boolean hasUserUserList(Integer userId, Integer id);

	public boolean isAtLeastOneShareLeadsListSharedByVendorCompanyWithPartnerCompany(Integer vendorCompanyId,
			Integer partnerCompanyId, Integer partnerId);

	public boolean isAtLeastOneShareLeadsListSharedByVendorCompanyWithPartnerCompany(Integer partnerCompanyId,
			Integer partnerId);

	/**** XNFR-255 ****/
	public List<Integer> findPartnerCompanyIdsByUserListIds(Set<Integer> userListIds);

	/************ XNFR-278 ********/
	public Map<String, Object> findGroupsForMerging(Pagination pagination);

	public Map<String, Object> copyUsersToUserGroups(CopyGroupUsersDTO copyGroupUserDto);

	/************ XNFR-278 ********/

	/************ XNFR-368 ********/
	public List<Integer> findPartnerListIdsByCompanyId(Integer companyId);

	public CompanyProfileDTO getPartnerDetails(Integer partnerId);

	public String getUserListNameByUserListId(Integer userListId);

	/*********** XBI-1816 ********/
	public void updateHubspotOrPipedriveContactList(Integer companyId, String type);

	public boolean isExistingUserListNameExists(UserListDTO userListDTO);

	public List<ContactsCSVDTO> findAllContactsOfAllPartnersByVendorCompanyId(Integer vendorCompanyId,
			List<Integer> partnerCompanyIds);

	public List<ContactsCSVDTO> findAllContactsCountOfAllPartnersByVendorCompanyId(Integer companyId,
			List<Integer> partnerCompanyIds);

	public boolean checkDomineExcluded(Integer customerCompanyId, String emailId);

	public void deleteUserFromExcludedUser(Integer customerCompanyId, Integer userId);

	/********** XBI-1149 **********/
	public void updateFormContactListName(Integer formId, String userListName);

	// XNFR_427
	public void setContactCompanyAsNull(Integer contactCompanyId);

	public List<UserUserList> getAllUserUserListsByUserId(Integer userId, Integer loggedinCompanyId);

	public Boolean isUserExistsInCompanyContactList(Integer userId, Integer loggedinCompanyId);

	public Integer getCompanyContactListId(Integer contactCompanyId);

	public void updateUserUserListByCompanyDetails(Integer contactCompanyId, String contactCompany,
			Integer loggedinCompanyId, Integer userId);

	public Object getUserListByUserListId(Integer userListId);

	public void updateCompanyOnAllUserUserlists(Integer companyId, String companyName, List<Integer> userId,
			Integer loggedinCompanyId);

	public UserUserList getContactInCompanyContactList(Integer userId, Integer loggedinCompanyId);

	public List<String> getExistingExcludedUsers(Integer companyId, List<String> existingMailIds);

	/****** XNFR-450 ******/

	public List<UserDTO> getAllUserContacts(Integer companyId, Integer userListId);

	public List<Integer> getAllExistingCompanyIds();

	public UserList getDefaultContactList(Integer companyId);

	public Boolean isUserExistsInDefaultPartnerList(Integer partnerId, Integer loggedInUserCompanyId);

	public Integer getAllContactsCount(List<Integer> userListIds, Integer customerCompanyId, String moduleName,
			UserListDTO userListDTO);

	public Integer getActiveCount(List<Integer> userListIds, Integer customerCompanyId, String moduleName,
			Integer logedInUserId, UserListDTO userListDTO);

	public Integer getInActiveCount(List<Integer> userListIds, Integer customerCompanyId, String moduleName,
			Integer logedInUserId, UserListDTO userListDTO);

	public Integer getUndeliverableCount(List<Integer> userListIds, Integer customerCompanyId, String moduleName,
			Integer logedInUserId, UserListDTO userListDTO);

	public Integer getUnSubscribedCount(List<Integer> userListIds, Integer customerCompanyId, String moduleName,
			Integer logedInUserId, UserListDTO userListDTO);

	public UserList getUserList(Integer userListId);

	public List<UserList> getInProcessUserLists();

	public Integer getDefaultPartnerListIdByCompanyId(Integer companyId);

	public UserDTO findUserUserListDetailsByUserListIdAndEmailId(Integer userListId, String emailId);

	public Map<String, Object> getActiveUsers(User logedInUser, List<Integer> userListIds, String searchHQL,
			String sortSQL, String dataSortSQL, String sortColumnSQL, String sortingOrder, Pagination pagination,
			UserListDTO userListDTO);

	public List<Integer> getValidData(List<Integer> userListIds, User loggedInUser, String searchHQL, String sortSQL,
			String sortColumnSQL, String sortingOrder, UserListDTO userListDTO, Pagination pagination);

	public Integer getExcludedUsersCount(UserListDTO userListDTO, List<Integer> userListIds, Integer customerCompanyId,
			Integer loggedInUserId);

	public List<Integer> getExcludedUserIds(List<Integer> userListIds, User loggedInUser, String searchHQL,
			String sortColumnSQL, String sortingOrder, UserListDTO userListDTO);

	/***** XNFR-571 ******/
	public List<DashboardButtonsPartnersDTO> findUserListIdsAndPartnerIdsAndPartnershipIdsByUserListIds(
			Set<Integer> partnerGroupIds);

	public List<Integer> findUserIdsByUserListIds(List<Integer> userListIds);

	public List<Integer> findPublishedPartnerIdsById(Integer dashboardButtonId);

	public List<Integer> sharedListUserIds(Integer userId, Integer companyId, Integer vendorCompanyId, boolean isVanity,
			boolean isLoginAsPartner);

	Integer getValidContactsCountByUserListId(Integer userListId);

	Integer getInvalidContactsCountByUserList(Integer userListId);

	Integer getValidContactsCountByUserListIdAndUserIds(Integer userListId, Set<Integer> userIds);

	Integer getInvalidContactsCountByUserListIdAndUserIds(Integer userListId, Set<Integer> userIds);

	boolean isDefaultMasterContactListExists(Integer companyId);

	public List<UserListDetails> onlylistUserLists(List<Criteria> criterias, Pagination pagination);

	public Integer getUserUserListIdByUserListIdAndUserId(Integer userListId, Integer partnerId);

	/**** XNFR-791 *******/
	public boolean isPublishedAllUserswithInList(Integer dashboardButtonId, List<Integer> publishedPartnerGroupIds);

	public boolean validateListName(String listName, Integer companyId, String module);

	public boolean validateAssignedListName(String listName, Integer assignedCompanyId, String module);

	public Map<String, Object> fetchContactsFromUserList(Pagination pagination);

	public Integer findDefaultContactListIdByCompanyId(Integer companyId, String moduleName);

	public void updatePreviousUsersListByUserListId(Integer userListId);

	public Map<String, Object> deleteContactFromAllContactLists(Integer contactId, Integer loggedInUserId);

}
