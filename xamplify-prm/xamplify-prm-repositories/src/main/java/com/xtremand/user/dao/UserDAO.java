
package com.xtremand.user.dao;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.Session;

import com.xtremand.campaign.bom.ModuleAccess;
import com.xtremand.campaign.bom.ModuleAccessDTO;
import com.xtremand.campaign.bom.UnsubscribedUser;
import com.xtremand.campaign.dto.ReceiverMergeTagsDTO;
import com.xtremand.category.dto.CategoryDTO;
import com.xtremand.common.bom.CompanyProfile;
import com.xtremand.common.bom.Criteria;
import com.xtremand.common.bom.Pagination;
import com.xtremand.common.bom.PartnerTeamMemberViewType;
import com.xtremand.company.dto.ApprovalSettingsDTO;
import com.xtremand.dam.dto.SharedAssetDetailsViewDTO;
import com.xtremand.dao.util.FinderDAO;
import com.xtremand.formbeans.UserDTO;
import com.xtremand.partnership.bom.PartnershipDTO;
import com.xtremand.social.formbeans.TeamMemberDTO;
import com.xtremand.user.bom.AllAccountsView;
import com.xtremand.user.bom.ModulesDisplayType;
import com.xtremand.user.bom.User;
import com.xtremand.user.bom.UserCustomer;
import com.xtremand.util.dto.CompanyDTO;
import com.xtremand.util.dto.ShareContentRequestDTO;
import com.xtremand.util.dto.UserDetailsUtilDTO;
import com.xtremand.util.dto.ViewTypePatchRequestDTO;

public interface UserDAO extends FinderDAO<User> {

	public UserCustomer getUserCustomer(User user, User customer);

	// ************This method is DEPRECATED
	public void deletePartnerCompany(List<Integer> partnerIds, Integer companyId);

	public User getUser(Integer customerId);

	public User getUserByEmail(String email);

	public List<Integer> getSubAdminUserIds(Integer userId);

	public Integer getOrgAdminUserId(Integer userId);

	public void loadUnsubscribedUsers(List<Criteria> criterias);

	public String getUserDefaultPage(Integer userId);

	public boolean isGridView(Integer userId);

	public void saveUsers(List<User> users);

	public void deleteAllTeamMemberRoles(List<Integer> userIds);

	public List<String> listAllAdminEmailIds();

	public List<String> listAllRolesEmailIds();

	public void updateTeamMembersCompanyProfile(Integer userId, Integer companyId);

	public List<String> listAllCompanyNames();

	public List<String> listAllCompanyProfileNames();

	public List<Integer> listAllUserIdsByLoggedInUserId(Integer userId);


	public List<Integer> getCompanyUserIds(Integer userId);

	public List<Integer> getCompanyUsers(Integer companyId);

	public Integer getFirstAdminId(Integer userId);

	public List<Object[]> listAllTeamMembers(Integer userId);

	public List<Object[]> findAllAdminsAndSuperVisors(Integer userId);

	public List<String> listAllPartnerEmailIds();

	public List<Integer> listAllUserIdsByCompanyId(Integer companyId);

	public List<Integer> listAllPartnerIdsByCompanyId(Integer companyId);

	public void deletePartnerData(List<Integer> partnerIds, Integer companyId);

	public Object[] getSignUpDetails(String alias, Integer userListId);

	public Integer getCompanyIdByUserId(Integer userId);

	public Boolean getUserOppertunityModule(Integer userId);

	public ModuleAccess getAccessByCompanyId(Integer companyId);

	public boolean hasLoginAsTeamMemberAccess(Integer userId);

	public void updateAccess(ModuleAccess moduleAccess);

	public void updateSource(ModuleAccessDTO moduleAccessDTO);

	public Integer getUserIdByEmail(String emailId);

	Boolean getSMSServiceModule(Integer userId);

	public User findByAlias(String alias);

	public List<Object[]> listPartnerAndHisTeamMembers(Integer userId);

	public List<Object[]> getCompanyIdsByEmailIds(List<String> emailIds);

	public List<User> getAllUsersByCompanyId(Integer companyId);

	public boolean isUserExistsinList(Integer teamMemberId);

	public List<Integer> getPartnerIds();

	public List<Integer> getRoleIdsByUserId(Integer userId);

	public boolean isEmailDnsConfigured(String emailId);

	public String getCompanyLogoPath(Integer companyId);

	public void updateUser(User user);

	public void updateUserEmailValidationInd(User user);

	public String getAboutUsByUserId(Integer userId);

	public List<Integer> listUnsubscribedCompanyIdsByUserId(Integer userId);

	boolean isUserUnsubscribedForCompany(Integer userId, Integer companyId);

	String getEmailIdByUserId(Integer userId);

	boolean isTeamMemberBelongsToLoggedInUserCompanyId(Integer companyId, Integer teamMemberId);

	boolean isTeamMemberBelongsToLoggedInUserIdCompany(Integer loggedInUserId, Integer teamMemberId);

	public List<String> listRolesByUserId(Integer userId);

	public Integer getCompanyIdByProfileName(String vendorCompanyProfileName);

	String getSuperiorSourceType(Integer teamMemberId);

	public CompanyProfile getCompanyProfileByCompanyName(String companyName);

	public void updateDefaultDisplayView(Integer userId, String type);

	public void updateDefaultDashboardForPartner(Integer companyId, String type);

	public Object getDefaultDashboardForPartner(Integer companyId);

	ModulesDisplayType getModulesDisplayDefaultView(Integer userId);

	public User getFirstNameAndEmailIdByUserId(Integer id);

	public User getFirstNameAndUserIdByEmailId(String emailId);

	public CompanyProfile getCompanyNameByCompanyId(Integer companyId);

	public String getStatusByUserId(Integer userId);

	public boolean isPartnerTeamMember(Integer teamMemberId);


	public String getCompanyProfileNameByUserId(Integer userId);

	public Set<User> setSubscribeOrUnsubscribeUsers(ShareContentRequestDTO shareContentRequestDTO, Integer companyId);

	public User getFirstNameLastNameAndEmailIdByUserId(Integer userId);

	public void deleteByRoleId(Integer roleId, Integer userId);

	public void insertRole(Integer roleId, Integer userId);

	public String getDisplayName(Integer userId);

	public List<UserDTO> listAdminsByCompanyId(Integer companyId);

	public UserDTO getEmailIdAndDisplayName(Integer userId);

	boolean hasCompany(Integer userId);

	public boolean isExcludedUserExists(Integer customerCompanyId, Integer excludedUserId);

	public Map<String, Object> findAdminsAndTeamMembersByCompanyId(Pagination pagination);

	void updateSpfConfiguration(Integer companyId);

	public boolean isSpfConfigured(Integer companyId);

	public boolean isDnsConfigured(Integer companyId);

	void updateDnsConfiguration(Integer companyId, boolean isDnsConfigured);

	public Map<String, Object> listExcludedUsers(Integer companyId, Pagination pagination);

	public void deleteExcludedUser(Integer excludedUserId, Integer customerCompanyId);

	public boolean isExcludedDomainExists(Integer customerCompanyId, String domain);

	public void deleteExcludedDomain(String domain, Integer customerCompanyId);

	public Map<String, Object> listExcludedDomains(Integer companyId, Pagination pagination);

	public Map<String, Object> findPartnerCompaniesAndModulesAccess(Pagination pagination);

	public void updatePartnerModules(ModuleAccessDTO moduleAccessDTO);

	public Map<String, Object> findAllUsers(Pagination pagination);

	public Map<String, Object> findRegisteredOrRecentLoggedInUsers(Pagination pagination);

	public List<Integer> findAllCompanyIds();

	public String findCompanyLogoPath(Integer companyId);

	public boolean isUnsubscribed(Session session, Integer companyId, Integer userId);

	public boolean isUnsubscribedUserByCompanyIdAndUserId(Integer companyId, Integer userId);

	public User findReceiverMergeTagsAndUnsubscribeStatus(Integer userId, Integer companyId, Integer userListId,
			Session session);

	public ReceiverMergeTagsDTO findReceiverMergeTagsInfoByUserListIdAndUserId(Integer userListId,
			Integer userId);

	public CompanyProfile findPageMergeTagsInfoByCompanyId(Integer companyId);

	public String getCompanyProfileNameById(Integer companyId);

	public boolean findNotifyPartnersOption(Integer companyId);

	public void updateNotifyPartnersOption(Integer companyId, boolean status);

	public List<Integer> getUserIdsByEmailIds(Set<String> emailIds);

	public List<String> findEmailIdsByCompanyId(Integer companyId);

	/*** XNFR-125 ****/
	public List<Integer> findAdminAndApprovedOrDeclinedOrSupspendedTeamMemberIdsByCompanyId(Integer companyId);

	public List<Integer> getComapnyAdminUserIds(Integer companyId);

	public Integer getCompanyIdByEmailId(String emailId);

	public String getCompanyNameByUserId(Integer userId);

	public List<String> findAllEmailIds();

	public List<User> getUsersByEmailIds(List<String> emailIds);

	public CompanyProfile getCompanyProfileNameAndCompanyNameByCompanyId(Integer companyId);

	public String findCompanyLogoPathPrefix(Integer companyId);

	/**** XNFR-255 ****/
	public List<Integer> findCompanyIdsByUserIds(Set<Integer> userIds);

	public String getCompanyLogoPathByUserId(Integer userId);

	public UserDTO getDisplayNameByEmailId(String emailId);

	public User findByEmailId(String emailId);

	public void declineAllUsersByCompanyId(Integer companyId);

	public void approveOrUnApproveAllUsersByCompanyId(Integer companyId);

	public void updateSpfConfiguration(Integer companyId, boolean isSpfConfigured);

	public UnsubscribedUser getUnsubscribedUser(Integer userId, Integer companyId);

	public CompanyDTO isSpfConfiguredOrDomainConnected(Integer companyId);

	List<UserDTO> findAllApprovedUsersByUserId(Integer companyId);

	List<Integer> findUnsubscribedUserIdsByCompanyId(Integer companyId);

	List<Integer> findExcludedUserIdsByCompanyId(Integer companyId);

	public Set<User> findCampaignUsersByCampaignId(Integer campaignId);

	public boolean isSecondAdmin(AllAccountsView allAccountsView);

	/******* XNFR-426 ********/
	public void updateLeadApprovalOrRejectionStatus(Integer companyId, Boolean leadApprovalStatus);

	public Object getLeadApprovalStatus(Integer companyId);

	public List<CompanyDTO> findAllCompanyNames();

	public List<CompanyDTO> findAllPartnerCompanyNamesByVendorCompanyId(Integer vendorCompanyId);

	public boolean isPasswordExists(String emailId);

	public String getCompanyWebSiteUrlByCompanyId(Integer companyId);

	public CompanyProfile getCompanyProfile(String companyName, Integer addedAdminCompanyId);

	public List<User> findUserIdAndEmailIdAndFullNameByUserIds(List<Integer> userIds);

	public void updateAdminOrPartnerOrTeamMemberViewType(Integer userId, Integer partnershipId, Integer teamMemberId,
			String type, boolean isViewUpdated);

	public String getAdminOrPartnerOrTeamMemberViewType(Integer userId, Integer partnershipId, Integer teamMemberId);

	public List<UserDTO> findAllPrimaryAdminsAndViewTypes();

	public List<PartnershipDTO> getAllPartnershipsByCompanyId(Integer companyId);

	public List<TeamMemberDTO> getAllTeamMembersByUserId(Integer userId);

	public void saveAll(List<PartnerTeamMemberViewType> partnerTeamMemberViewTypes);

	public boolean isEmailAddressExists(String existingEmailAddress);

	public boolean isCampaignAnalyticsSettingsOptionEnabled(Integer loggedInUserId);

	public List<ViewTypePatchRequestDTO> findUpdatedAdminsViewTypeData(Integer adminId, boolean partnership,
			boolean teamMember);

	public String getSupportEmailIdByCompanyId(Integer companyId);

	public List<UserDTO> findAllUserNamesByCompanyId(Integer companyId);

	public List<String> findAllPartnerAndPartnerTeamMemberEmailIdsByVendorCompanyId(Integer vendorCompanyId);

	public List<CompanyDTO> findAllVendorCompanyIdAndNames();

	public User getUserNameByEmail(String emailId);


	List<UserDTO> findAdminAndSuperVisorsFirstNameLastNameEmailIdByCompanyId(Integer vendorCompanyId);

	public boolean checkIfAssetApprovalRequiredByCompanyId(Integer companyId);

	public Integer updateApprovalConfigurationSettings(ApprovalSettingsDTO approvalSettingsDTO);

	public List<Integer> listAdminsUserIdsByCompanyId(Integer companyId);

	public String getAliasByUserId(Integer userId);

	public ApprovalSettingsDTO getApprovalConfigurationSettingsByCompanyId(Integer companyId);

	public void approvePendingTracksOrPlaybooksByModuleType(Integer companyId, Integer loggedInUserId,
			String moduleType);

	public boolean checkIfPlaybooksApprovalRequiredByCompanyId(Integer companyId);

	public boolean checkIfTracksApprovalRequiredByCompanyId(Integer companyId);

	public void updateTeammemberFilterOption(Integer userId, boolean filterOption);

	/** XNFR-821 **/
	public List<Integer> findAllAdminAndSuperVisorUserIdsByCompanyId(Integer companyId);

	/** XNFR-833 **/
	public User getFirstNameLastNameAndJobTitleByUserId(Integer userId);

	public void approveInTimeLineStatusHistoryByCompanyIdAndModuleType(Integer companyId, String moduleType);

	public List<String> getPartnerCompanyByEmailDomain(String domain, Integer vendorCompanyId);

	/** XNFR-908 **/
	boolean checkValidUserOrNotByUserId(Integer userId);

	String getMobileNumberByUserIdAndUserListId(Integer userId, Integer userListId, boolean isCompanyJourney);

	public SharedAssetDetailsViewDTO getSharedAssetDetailsByPartnerDamId(Integer damId, Integer loggedInUserId);

	/** XNFR-923 **/
	public List<UserDTO> getUsersByDamIdAndPartnerCompanyIds(Integer damId, List<Integer> companyIds,
			boolean isPartnerGroupSelected);

	public List<UserDetailsUtilDTO> fetchAdminsAndSupervisorsByPartnerIdAndVendorCompanyId(Integer companyId,
			Integer userId);

	public UserDTO getFullNameAndEmailIdAndCompanyNameByUserId(Integer loggedInUserId);

	public List<CategoryDTO> getAssetDetailsByCategoryId(Integer id, Integer loggedInUserId);

	public List<CategoryDTO> getAssetDetailsByCategoryIdForPartner(Integer id, Integer loggedInUserId);

	public User getFirstNameLastNameMidlleNameByEmailId(String emailId);

	User getFirstNameMiddleNameLastNameAndEmailIdByUserId(Integer userId);

	UserDTO getSendorCompanyDetailsByUserId(Integer loggedInUserId);

	public Map<String, Object> getWelcomeEmailsList(Integer userId, Pagination pagination);

        public Map<String, Object> downloadWelcomeEmailsList(Pagination pagination);

        Integer getOrgAdminsCountByUserId(Integer userId);

        boolean prmAccountExists();

}
