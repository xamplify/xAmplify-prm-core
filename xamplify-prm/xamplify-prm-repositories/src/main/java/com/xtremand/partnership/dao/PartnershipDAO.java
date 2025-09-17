package com.xtremand.partnership.dao;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.xtremand.common.bom.CompanyProfile;
import com.xtremand.common.bom.Pagination;
import com.xtremand.dao.util.FinderDAO;
import com.xtremand.dashboard.buttons.dto.DashboardButtonsPartnersDTO;
import com.xtremand.domain.bom.DomainModuleNameType;
import com.xtremand.formbeans.UserDTO;
import com.xtremand.partnership.bom.PartnerTeamGroupMapping;
import com.xtremand.partnership.bom.Partnership;
import com.xtremand.partnership.bom.Partnership.PartnershipSource;
import com.xtremand.partnership.bom.Partnership.PartnershipStatus;
import com.xtremand.partnership.bom.PartnershipDTO;
import com.xtremand.partnership.dto.PartnerJourneyResponseDTO;
import com.xtremand.user.bom.User;
import com.xtremand.user.bom.UserList;
import com.xtremand.util.dto.CompanyDTO;
import com.xtremand.util.dto.PartnerCompanyDTO;

public interface PartnershipDAO extends FinderDAO<Partnership> {

	public Partnership getPartnershipByPartnerCompany(CompanyProfile partnerCompany, CompanyProfile vendorCompany);

	public Partnership getPartnershipByRepresentingPartner(User representingPartner, CompanyProfile vendorCompany);

	public Partnership getPartnershipByRepresentingVendor(User representingVendor, CompanyProfile partnerCompany);

	public List<User> getOrgAdminsOrVendorsOrVendorTiersOrMarketing(Integer id);

	List<User> getOwners(Integer companyId);

	public void deletePartnerShipByPartnerCompany(CompanyProfile partnerCompany, CompanyProfile vendorCompany);

	public void deletePartnerShipByRepresentingPartner(User representingPartner, CompanyProfile vendorCompany);

	public List<Partnership> getInvitedPartnershipsAsVendor(User representingVendor);

	public List<Partnership> getInvitedPartnerships(CompanyProfile companyProfile);

	public Integer getApprovePartnersCount(CompanyProfile vendorCompany, Integer userId, boolean applyFilter);

	public Map<String, Object> getApprovalSectionPartnersDetails(Pagination pagination);

	public List<User> getSuperiors(Integer companyId);

	public List<Partnership> getApprovedPartnershipsByVendorCompany(CompanyProfile companyProfile);

	// TODO: Work Around : remove below methods
	void updateCompanyIdOnUserList();

	public void removePartnerRoleOnTeamMember();


	public List<UserList> getAllDefaultPartnerLists();

	void processExistingPartnerLists();

	void processExistingPartnerList(UserList list);

	// TODO: Work Around : ENDS

	public List<Partnership> getApprovedPartnershipsByPartnerCompany(CompanyProfile companyProfile);

	public Map<String, Object> getAllPartnershipsByPartnerCompany(CompanyProfile partnerCompany,
			PartnershipSource source, Pagination pagination, PartnershipStatus status);

	public Long getAllPartnershipsCountByPartnerCompany(CompanyProfile partnerCompany, PartnershipSource source,
			PartnershipStatus status);


	public PartnershipDTO getPartnerShipByParnterIdAndVendorCompanyId(Integer partnerId, Integer vendorCompanyId);

	List<Integer> getVendorCompanyIdsByPartnerCompany(CompanyProfile companyProfile);

	public Integer getSharedPagesCount(Integer partnerId);

	public Partnership getMdfAmountAndContactsLimitAndNotifyPartnersByVendorCompanyIdAndPartnerId(
			Integer vendorCompanyId, Integer partnerId);

	public void updateContactsLimitAndMDFAmountAndTeamMembers(Integer vendorCompanyId, Integer partnerId,
			Integer contactsLimit, Double mdfAmount);

	List<PartnershipDTO> listPartnerDetailsByVendorCompanyId(Integer vendorCompanyId);

	PartnershipDTO findContactsLimitAndNotifyPartnersByEmailIdAndVendorCompanyId(String emailId,
			Integer vendorCompanyId);

	public Partnership checkPartnership(Integer vendorCompanyId, Integer partnerCompanyId);

	public List<Integer> getSuperiorIds(Integer companyId);

	public Partnership getPartnershipById(Integer partnershipId);

	public boolean isPartnershipEstablished(Integer partnerCompanyId);

	public List<Integer> listAllPartnershipIdsByVendorCompanyId(Integer vendorCompanyId);

	public Integer getPartnershipIdByVendorCompanyIdAndPartnerCompanyId(Integer vendorCompanyId,
			Integer partnerCompanyId);

	public Map<String, Object> listPartnersByVendorCompanyId(Integer vendorCompanyId, Pagination pagination);

	public List<UserDTO> findEmailIdAndFullNameByUserIds(Set<Integer> partnershipIds);

	public List<User> getPartnerCompanySuperior(Integer companyId);

	public Map<String, Object> findPartnerCompanies(Pagination pagination);

	public List<Integer> getPartnershipIdsByPartnerCompanyUserIds(List<Integer> partnerIds, Integer vendorCompanyId);

	public Integer getPartnershipIdByPartnerCompanyUserId(Integer partnerId, Integer vendorCompanyId);

	public Map<String, Object> getApprovedPartnershipsByVendorCompany(Pagination pagination);

	public Map<String, Object> findPartnerGroups(Pagination pagination);

	public List<Integer> findPartnershipIdsByPartnerIdsAndVendorCompanyId(List<Integer> partnerIds,
			Integer vendorCompanyId);

	public Integer findPartnershipIdByPartnerIdAndVendorCompanyId(Integer partnerId, Integer vendorCompanyId);

	public UserDTO findEmailIdAndFullNameByUserId(Integer partnerId);

	/************** XNFR-2 *********************/
	public List<Integer> findPartnerCompanyIdsByVendorCompanyId(Integer vendorCompanyId);

	public List<Integer> findVendorCompanyIdsByPartnerCompanyId(Integer partnerCompanyId);

	public Map<String, Object> findVendorInvitationReports(Pagination pagination);

	public void updateNotifyPartners(Integer partnerId, Integer vendorCompanyId);

	/************** XNFR-81 *********************/
	public List<Integer> findPartnerIdsByVendorCompanyId(Integer vendorCompanyId);

	public Integer findPartnerIdByVendorCompanyIdAndPartnerCompanyId(Integer vendorCompanyId, Integer partnerCompanyId);

	/************** XNFR-85 *********************/
	public void findAndDeletePartnerTeamMemberGroupMappingByPartnershipIdAndTeamMemberGroupMappingIds(
			Integer partnership, List<Integer> teamMemberGroupMappingIds);

	public void savePartnerTeamMemberGroupMapping(Set<PartnerTeamGroupMapping> partnerTeamGroupMappings);

	public boolean isPartnerTeamMemberGroupMappingExists(Integer partnershipId, Integer teamMemberGroupUserMappingId);

	/**** XNFR-125 *********/
	public PartnershipDTO findPartnerIdAndPartnerCompanyIdByPartnershipId(Integer partnershipId);

	public PartnerCompanyDTO findOneClickLaunchCampaignPartnerCompany(Integer partnershipId);

	/**** XNFR-220 *********/
	public Map<String, Object> findAllPartnerCompanies(Pagination pagination);

	public List<PartnerJourneyResponseDTO> findJourney(Integer partnershipId);

	/**** XNFR-220 *********/

	/**** XNFR-222 *********/
	public boolean isPartnershipEstablishedByVendorCompanyIdAndPartnerCompanyId(Integer vendorCompanyId,
			Integer partnerCompanyId);

	/**** XNFR-255 *********/
	public Map<String, Object> findPartnerCompaniesForSharingWhiteLabeledContent(Pagination pagination);

	/**** XNFR-316 *********/
	public Map<String, Object> getActivePartnerCompanies(Pagination pagination);

	/**** XNFR-276 *********/
	public List<Partnership> getPartnershipsByPartnerId(Integer userId);

	public List<Partnership> findAllApprovedPartnerships();

	/**** XBI-2048 ****/
	public Integer getPartnerCompanyIdByPartnershipId(Integer selectedPartnershipId);

	public Integer getPartnerIdByPartnershipId(Integer selectedPartnershipId);

	/**** XBI-2048 ****/

	public List<Integer> getVendorsByPartnerId(Integer partnerId);

	public List<Integer> listAllPartnershipIdsByPartnerCompanyId(Integer partnerCompanyId);

	public List<String> getExistingPartnerCompanyNamesByVendorCompanyId(Integer vendorCompanyId);

	public Integer addedAdminId(Integer partnerId);

	/***** XNFR-571 ****/
	public List<DashboardButtonsPartnersDTO> findPartnerIdsAndPartnershipIdsByPartnerIdsAndCompanyId(
			Set<Integer> partnerIds, Integer companyId);

	public void updateSalesforceAccountIdAndAccountNameByParterCompanyIdAndVendorCompanyId(Integer vendorCompanyId,
			Integer partnerCompanyId, String partnerSalesforceAccountId, String partnerSalesforceAccountName);

	public PartnershipDTO getAccountIdByPartnerCompanyIdAndVendorCompanyId(Integer vendorCompanyId,
			Integer partnerCompanyId);

	public Partnership getPartnershipByPartnerSalesforceAccountId(String partnerSalesforceAccountId,
			Integer vendorCompanyId);

	/***** XNFR-712 ****/
	public Map<String, Object> findVendorCompaniesPagination(Pagination pagination, String search);

	public List<PartnershipDTO> getPartnershipByPartnerCompanyDomain(String partnerCompanyDomain,
			Integer vendorCompanyId);

	List<PartnershipDTO> getPartnershipsByPartnerSalesforceAccountIds(List<String> partnerSalesforceAccountIds,
			Integer vendorCompanyId);

	public void updateAccountDetailsInUserUserListForPartner(UserDTO userDTO, PartnershipDTO partnershipDTO,
			Integer vendorCompanyId);

	public List<String> getDomainsByPartnershipId(Integer partnershipId);

	public boolean isPartnershipEstablishedAndApporved(Integer vendorCompanyId, Integer partnerCompanyId);
	
	List<CompanyDTO> findVendorCompanyDetailsByPartnerUserId(Integer partnerUserId);
	
	public List<PartnershipDTO> getPartnershipDtoByPartnerCompanyDomain(String domainName, Integer companyId);
	
	/**XNFR-891**/
	public void updatePartnershipCompanyIdByPartnerId(Integer partnerId, Integer partnerCompanyId);

	/**XNFR-905**/
	public List<UserDTO> getOwnersAndChannelAccountManagers(Integer companyId, Integer partnerShipId);

	public void updatePartnerShipStatusForPartner(String partnerStatus, List<Integer> partnershipIds);

	public List<String> findDeactivatedDomainsByCompanyId(Integer companyId);

	public List<String> findDeactivatedPartnersByCompanyId(Integer companyId);

	public Map<String, Object> findPartnerCompaniesByDomain(PartnershipDTO partnershipDTO);

	public void updatePartnerCompaniesByDomain(PartnershipDTO partnershipDTO, Date updatedOn);

	public void deactivatePartnerCompanies(List<Integer> deactivateUserIds, Integer vendorCompanyId, Date updatedOn);
	
	public boolean isPartnerCompanyDeactivatedAndDisabledNonVanityLogIn(Integer id);

	public List<String> findDeactivatedPartnerDomainsByCompanyIdAndModuleName(Integer companyId,
			DomainModuleNameType domainModuleNameType);

	public String findPartnerShipStatusByPartnerCompanyIdAndVendorCompanyId(Integer partnerCompanyId, 
			Integer vendorCompanyId);

	public List<Integer> getPartnerCompanyIdsByDomain(PartnershipDTO partnershipDTO);

	public boolean isNonVanityAccessEnabled(Integer id);

	//XNFR-1006
	public  Map<String, Object> getdeactivePartnerCompanies(Pagination pagination);

	public Map<String, Object> findTeamMemberPartnerCompanies(Pagination pagination, Integer teamMemberGroupId);
	
	public List<PartnerCompanyDTO> findTeamMemberPartnerCompanyByTeamMemberGroupIdAndTeamMemberId(Integer teamMemberId, Integer teamMemberGroupId);

	public Map<String, Object> getAllPartnerCompaniesDetails(Pagination pagination);
  
	public List<Integer> findPartnershipsByTeamMemberGroupIdAndTeamMemberId(Integer teamMemberId, Integer teamMemberGroupId);
  
}
