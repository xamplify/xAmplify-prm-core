
package com.xtremand.user.service;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;

import org.springframework.web.multipart.MultipartFile;

import com.xtremand.account.dto.ResendEmailDTO;
import com.xtremand.campaign.bom.ModuleAccessDTO;
import com.xtremand.common.bom.CompanyProfile;
import com.xtremand.common.bom.Criteria;
import com.xtremand.common.bom.FindLevel;
import com.xtremand.common.bom.Pagination;
import com.xtremand.company.dto.ApprovalSettingsDTO;
import com.xtremand.flexi.fields.bom.UserListFlexiField;
import com.xtremand.flexi.fields.dto.FlexiFieldRequestDTO;
import com.xtremand.formbeans.AccountDTO;
import com.xtremand.formbeans.EmailDTO;
import com.xtremand.formbeans.EmailLogReport;
import com.xtremand.formbeans.RoleDTO;
import com.xtremand.formbeans.UserDTO;
import com.xtremand.formbeans.XtremandResponse;
import com.xtremand.integration.bom.ExternalContactDTO;
import com.xtremand.signup.dto.SignUpRequestDTO;
import com.xtremand.social.formbeans.UserPassword;
import com.xtremand.user.bom.User;
import com.xtremand.user.bom.UserList;
import com.xtremand.user.bom.UserUserList;
import com.xtremand.user.exception.UserDataAccessException;
import com.xtremand.util.bom.ModuleType;
import com.xtremand.vanity.url.dto.VanityUrlDetailsDTO;

public interface UserService {
	public User findByPrimaryKey(Serializable pk, FindLevel[] levels) throws UserDataAccessException;

	public Collection<User> find(List<Criteria> criterias, FindLevel[] levels) throws UserDataAccessException;

	public void saveUser(User user) throws UserDataAccessException;

	public void saveSubAdmin(User user, String subAdminRole) throws UserDataAccessException;

	public void forgotPassword(String modifiedEmailId, String companyProfileName);

	public void approveUser(String emailId);

	public void uploadUsers(MultipartFile file, UserList userList, Integer customerId) throws UserDataAccessException;

	public void userWithRefreshToken(User user) throws UserDataAccessException;

	public User loadUser(int id) throws UserDataAccessException;

	public User createUserWithEmail(UserDTO userDTO, UserList userList, Integer customerId)
			throws UserDataAccessException;

	public void updatePassword(UserPassword userPassword);

	public void updateUser(User user);

	public UserDTO getUserDTO(String userName, boolean isSuperAdmin);

	public boolean comparePassword(String password, Integer userId);

	String uploadProfilePicture(UserDTO user);

	public void uploadUsers(String[][] res, UserList userList, Integer userId);

	public void addCustomer(User user, User customer, boolean isOrgAdmin) throws UserDataAccessException;

	public List<Integer> getSubAdminUserIds(Integer userId);

	public Integer getOrgAdminUserId(Integer userId);

	public List<User> uploadUsers(List<User> users, UserList userList, Integer customerId)
			throws UserDataAccessException;

	public void loadUnsubscribedUsers(List<Criteria> criterias, FindLevel[] levels);

	public XtremandResponse registerUser(UserDTO user) throws UserDataAccessException;

	public User loadUser(List<Criteria> criterias, FindLevel[] levels);


	public String generateUserAlias(Integer userId, long time);

	public void embedVideoSaveUser(UserDTO userDTO) throws UserDataAccessException;

	public String getUserDefaultPage(Integer userId);

	public void setUserDefaultPage(Integer userId, String defaultPage);

	public boolean isGridView(Integer userId);

	public void setGridView(Integer userId, boolean isListView);

	public List<String> listAllOrgAdminEmailIds();

	public CompanyProfile getCompanyProfileByUser(Integer userId);

	public XtremandResponse getCompanyProfileByUserId(Integer userId);

	public XtremandResponse save(CompanyProfile companyProfile, Integer userId);

	public XtremandResponse update(CompanyProfile companyProfile, Integer userId);

	public XtremandResponse listAllCompanyNames();

	public XtremandResponse listAllCompanyProfileNames();

	public List<Integer> getCompanyUserIds(Integer userId);

	public List<Integer> getCompanyUsers(Integer companyId);


	public Integer getFirstOrgAdminId(Integer userId);

	public Map<String, Object> logSaveCallToActionUser(EmailLogReport emailLogReport, Integer videoId) throws Exception;

	public Integer getOrgAdminCountByUserId(Integer userId);


	public void addPartnerCompany(User partner, CompanyProfile company, String brandingLogoUri);

	public List<String> listAllPartnerEmailIds();

	public List<String> getNonExistingUsers(List<String> emailIds);

	String getCompanyLogoPath(MultipartFile imageFile, Integer userId);

	String getCompanyBackGroundImagePath(MultipartFile imageFile, Integer userId);

	public List<UserDTO> listTeamMembers(Integer userId);

	public List<UserDTO> findAllAdminsAndSupervisors(Integer userId);

	public List<UserDTO> listPartnerAndHisTeamMembers(Integer userId);


	RoleDTO getSuperiorRole(Integer teamMemberId);

	public void resendActivationEmail(String emailId);

	public UserDTO getSignUpDetails(String alias);

	public Integer getCompanyIdByUserId(Integer userId);

	public UserDTO getUser(Integer customerId);

	List<User> uploadContacts(List<ExternalContactDTO> contacts, UserList userList, Integer customerId)
			throws UserDataAccessException;

	public List<String> listAllCompanyProfileImages(Integer userId);

	public XtremandResponse getUserOppertunityModule(Integer userId);

	public ModuleAccessDTO getAccessByCompanyId(Integer companyId);

	public XtremandResponse updateModulesAccess(ModuleAccessDTO moduleAccessDto);

	public XtremandResponse getSMSServiceModule(Integer userId);

	public List<UserDTO> getCompanyIdsByEmailIds(List<String> emailIds);

	public List<User> getAllUsersByCompanyId(Integer companyId);

	public XtremandResponse getRolesByUserId(Integer userId);

	public void sendWelcomeEmail(User user);

	public XtremandResponse createAccount(AccountDTO accountDTO);

	public XtremandResponse saveAccountPassword(UserDTO user);

	public XtremandResponse sendUserWelcomeEmailThroughAdmin(EmailDTO emailDTO);

	public XtremandResponse getUserAndCompanyProfileByEmailId(String emailId);

	public XtremandResponse updateAccount(AccountDTO accountDTO);

	public XtremandResponse accountApproval(UserDTO user, boolean approve);

	public XtremandResponse saveUserAndCompanyProfile(MultipartFile file, AccountDTO accountDto);

	public XtremandResponse getUserByAlias(String alias, String companyProfileName);

	public boolean isOnlyUser(User loggedInUser);

	public List<Integer> listUnsubscribedCompanyIdsByUserId(Integer userId);

	public XtremandResponse getRoleDtosByUserId(Integer userId);

	public XtremandResponse getModulesDisplayDefaultView(Integer userId);

	public XtremandResponse updateDefaultDisplayView(Integer userId, String type);

	public int getRolesCountByUserId(Integer userId);


	public XtremandResponse getCompanyFavIconPath(Integer userId, MultipartFile file);

	public XtremandResponse getCompanyBgImagePath(Integer userId, MultipartFile file);

	public List<User> uploadUsers(Set<UserDTO> users, UserList userList, User user);

	public String getEmailIdByUserId(Integer userId);

	public XtremandResponse findAdminsAndTeamMembers(Pagination pagination);

	public XtremandResponse updateSpfConfiguration(Integer companyId);

	public XtremandResponse isSpfConfigured(Integer companyId);

	public XtremandResponse isDnsConfigured(Integer companyId);

	public XtremandResponse updateDnsConfiguration(Integer companyId, boolean isDnsConfigured);

	public XtremandResponse listExcludedUsers(Integer userId, Pagination pagination);

	public XtremandResponse deleteExcludedUser(Integer userId, Integer excludedUserId);


	public XtremandResponse deleteExcludedDomain(Integer userId, String domain);

	public XtremandResponse listExcludedDomains(Integer userId, Pagination pagination);

	public XtremandResponse findPartnerCompaniesAndModulesAccess(Pagination pagination);

	public XtremandResponse updatePartnerModules(ModuleAccessDTO moduleAccessDTO);

	public XtremandResponse findAllUsers(String filter);

	public void downloadAllUsers(String filter, HttpServletResponse httpServletResponse);

	public XtremandResponse findRegisteredOrRecentLoggedInUsers(Pagination pagination);

	public void removeNullEmailUsers(Collection<User> users);

	public void removeNullEmailUserDTOs(Collection<UserDTO> users);

	public XtremandResponse saveExcludedUsers(Set<UserDTO> userDTOs, Integer userId);

	public XtremandResponse saveExcludedDomains(Set<String> domainNames, Integer userId);

	/************ XNFR-2 *******************/

	public XtremandResponse updateNotifyPartnersOption(Integer companyId, boolean status);

	public XtremandResponse findNotifyPartnersOption(Integer companyId);

	public Integer getUserIdByEmail(String emailId);

	public boolean isNonProcessedUser(String emailId);

	/********* XNFR-83 *****************/
	public List<String> findVendorCompanyImages(Integer userId, String domainName);

	public XtremandResponse getFirstNameLastNameAndEmailIdByUserId(Integer userId);

	/********* XNFR-211 *****************/
	public User saveOrUpdateUser(UserDTO userDTO, Integer userId);

	/********* XNFR-334 *****************/
	public XtremandResponse resendActivationEmail(ResendEmailDTO resendEmailDTO);

	public XtremandResponse updateSpfConfiguration(Integer companyId, boolean isSpfConfigured);

	public XtremandResponse isSpfConfiguredOrDomainConnected(Integer companyId);

	/* -- XNFR-415 -- */
	public Object updateDefaultDashboardForPartner(Integer companyId, String type);

	public Object getDefaultDashboardForPartner(Integer companyId);

	public XtremandResponse validateExcludedUsersExist(Set<UserDTO> excludedUsers, Integer userId);

	public XtremandResponse validateExcludedDomainsExist(Set<String> excludedDomains, Integer userId);

	/****** XNFR-426 *******/
	public Object updateLeadApprovalOrRejectionStatus(Integer companyId, Boolean leadApprovalStatus);

	public Object getLeadApprovalStatus(Integer companyId);

	public XtremandResponse getDefaultDashboardPageForPartner(VanityUrlDetailsDTO vanityUrlDetails);

	public XtremandResponse validateCompany(CompanyProfile companyProfile, User PartnerUser);

	public XtremandResponse updateCompanyProfileName(Integer companyId, String companyProfileName);

	public XtremandResponse findAllCompanyNames();

	public XtremandResponse findAllPartnerCompanyNames(String vendorCompanyProfileName);

	public void updateUserDefaultPage(String emailId); // XNFR-560

	public XtremandResponse getDisplayViewType(Integer userId, String companyProfileName);

	public XtremandResponse updateDisplayViewType(Integer userId, String type, String companyProfileName);

	public XtremandResponse setViewTypeForExistingUsers();

	public XtremandResponse isPaymentOverDue(Integer loggedInUserId, String companyProfileName);

	public Map<String, Object> loadUserDefaultPage(Integer loggedInUserId, String companyProfileName);

	public XtremandResponse importPartnersFromExternalCSV(Integer companyId);

	public XtremandResponse findAllVendorCompanyNames();

	public void createUserListFlexiField(UserUserList userUserList, Set<UserListFlexiField> updatedFlexiFields,
			FlexiFieldRequestDTO dtoFlexiField);

	public void setUserListFlexiFields(UserUserList userUserList, List<FlexiFieldRequestDTO> dtoFlexiFields,
			Set<UserListFlexiField> updatedFlexiFields);

	public Integer addPartnerManagerDefaultGroup(Integer userId, Integer companyId);

	public XtremandResponse updateExistingLeadsData(Integer companyId);

	public XtremandResponse getApprovalConfigurationSettingsByUserId(Integer companyId);

	public XtremandResponse updateApprovalConfigurationSettings(ApprovalSettingsDTO approvalSettingsDTO);

	public XtremandResponse getNonExistingUsersfromCSV(Integer companyId);

	public XtremandResponse createTeamMembersByCompanyId(Integer companyId);

	public void approvePendingAssetsAndTimeLineStatusHistory(ApprovalSettingsDTO approvalSettingsDTO,
			Map<String, Object> resultMap);

	public void approvePendingTracksOrPlaybooksByModuleTypeAndTimeLineStatusHistory(
			ApprovalSettingsDTO approvalSettingsDTO, ModuleType moduleType);

	public XtremandResponse saveTeamMemberFilter(Integer userId, Integer filterOption);

    public void setTeamMemberPartnerFilter(UserDTO userDTO, Integer userId);

    public XtremandResponse registerPrmAccount(SignUpRequestDTO signUpRequestDTO);

}
