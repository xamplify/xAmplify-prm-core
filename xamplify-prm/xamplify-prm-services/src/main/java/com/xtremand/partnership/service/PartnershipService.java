package com.xtremand.partnership.service;

import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;

import com.xtremand.common.bom.CompanyProfile;
import com.xtremand.common.bom.Pagination;
import com.xtremand.formbeans.UserDTO;
import com.xtremand.formbeans.UserListDTO;
import com.xtremand.formbeans.VendorInvitationDTO;
import com.xtremand.formbeans.XtremandResponse;
import com.xtremand.partnership.bom.Partnership;
import com.xtremand.partnership.bom.PartnershipDTO;
import com.xtremand.user.bom.User;
import com.xtremand.user.bom.UserList;
import com.xtremand.util.dto.UserListOperationsAsyncDTO;

public interface PartnershipService {

	public Map<String, Object> updatePartnerList(UserList partnerList, Set<UserDTO> partners, Integer userId,
			String companyProfileName, UserListOperationsAsyncDTO userListOperationsAsyncDTO);

	public Partnership getPartnershipByPartnerComapny(CompanyProfile partnerCompany, CompanyProfile vendorCompany);

	public Partnership getPartnershipByRepresentingPartner(User representingPartner, CompanyProfile vendorCompany);

	public XtremandResponse deletePartnersFromUserList(UserList userList, Integer customerId,
			List<Integer> removePartnerIds);

	public XtremandResponse sendVendorInvitation(Integer senderId, VendorInvitationDTO vendorInvitationDTO);

	public List<UserList> getAllPartnerLists(Integer companyId);

	public XtremandResponse saveAsPartnerList(Set<UserDTO> users, Integer userId, UserListDTO userListDTO,
			XtremandResponse xtremandResponse, String csvPath);

	public void deletePartnerList(UserList userList);

	public Map<String, Object> getApprovalSectionPartnersDetails(Pagination pagination);

	public XtremandResponse approvePartner(Integer vendorId, Integer partnerId,
			VendorInvitationDTO vendorInvitationDTO, UserListOperationsAsyncDTO userListOperationsAsyncDTO);

	public XtremandResponse declinePartner(Integer vendorId, Integer partnerId,
			VendorInvitationDTO vendorInvitationDTO);


	public Map<String, Object> referVendorAnalytics(Pagination pagination);

	public Map<String, Object> vendorInvitationsCount(Integer partnerId);

	public XtremandResponse findPartnerCompanies(Pagination pagination);

	public XtremandResponse findPartnerGroups(Pagination pagination);

	public XtremandResponse findVendorInvitationReports(Pagination pagination);

	public XtremandResponse findPartnerCompanies(Pagination pagination, Integer userId);

	public void deletePartnersFromDefaultUserList(UserList userList, Integer customerId,
			List<Integer> removePartnerIds);

	public void publishDAMAndLMSToNewlyAddedPartners(Set<Integer> userListIds, Integer loggedInUserId,
			Set<UserDTO> allPartners);

	/******** XNFR-255 ****/
	public XtremandResponse findPartnerCompaniesForSharingWhiteLabeledContent(Pagination pagination);

	public List<String> getExistingPartnerCompanyNamesByVendorCompanyId(Integer vendorCompanyId);
	
	/******** XNFR-711 ****/
	public XtremandResponse findVendorCompanies(Pagination pagination);

	public XtremandResponse updatePartnerShipStatusForPartner(String partnerStatus, List<Integer> partnershipIds);

	public XtremandResponse findPartnerCompaniesByDomain(PartnershipDTO partnershipDTO, Integer domainId);

	public XtremandResponse updatePartnerCompaniesByDomain(PartnershipDTO partnershipDTO, Integer loggedInUserId);

	public XtremandResponse deactivatePartnerCompanies(List<Integer> deactivateUserIds, Integer loggedInUserId);

	public List<String> findDeactivedPartnersByCompanyId(Integer companyId);

	/******** XNFR-1046 ****/
	public XtremandResponse findTeamMemberPartnerCompany(Pagination pagination, Integer teamMemberGroupId);
	
	public XtremandResponse findTeamMemberPartnerCompanyByTeamMemberGroupIdAndTeamMemberId(Integer teamMemberId, Integer teamMemberGroupId);

	void referVendorAnalyticsDownloadCsv(Pagination pagination, HttpServletResponse response);
	
	void approvePartnersDownloadCsv(Pagination pagination, HttpServletResponse response);
}
