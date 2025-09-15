package com.xtremand.approve.dao;

import java.util.List;

import com.xtremand.approve.dto.MultiSelectApprovalDTO;
import com.xtremand.approve.dto.PendingApprovalDamAndLmsDTO;
import com.xtremand.approve.dto.TeamMemberApprovalPrivilegesDTO;
import com.xtremand.common.bom.Pagination;
import com.xtremand.company.dto.ApprovalSettingsDTO;
import com.xtremand.dam.bom.ApprovalStatusType;
import com.xtremand.util.dto.ApprovalStatisticsDTO;
import com.xtremand.util.dto.LeftSideNavigationBarItem;
import com.xtremand.util.dto.PaginatedDTO;

public interface ApproveDAO {

	public PaginatedDTO getAllApprovalList(Pagination pagination, String search,
			LeftSideNavigationBarItem leftSideNavigationBarItem);

	public ApprovalStatisticsDTO getStatusTileCounts(Integer companyId, Pagination pagination,
			LeftSideNavigationBarItem leftSideNavigationBarItem);

	Integer updateApprovalStatus(List<Integer> ids, String status, Integer loggedInUserId, String moduleType);

	public boolean isAssociatedWithLMS(List<Integer> damIds);

	public boolean isPublished(List<Integer> trackIds);

	public List<String> findNamesByAssetId(List<Integer> damIds);

	public List<String> findNamesBylmsId(List<Integer> trackIds);

	public PaginatedDTO listTeamMembersForApprovalControlManagement(Pagination pagination, String search);

	public TeamMemberApprovalPrivilegesDTO getTeamMemberApprovalPrivilegeSettingsByTeamMemberId(Integer teamMemberId,
			Integer companyId);

	public Integer updateTeamMemberApprovalPrivilegeSettings(
			TeamMemberApprovalPrivilegesDTO teamMemberApprovalPrivilegesDTO, Integer companyId);

	public boolean isApprovalPrivilegeManager(Integer loggedInUserId);

	public boolean checkIsAssetApproverByTeamMemberIdAndCompanyId(Integer teamMemberId, Integer companyId);

	public boolean checkIsTrackApproverByTeamMemberIdAndCompanyId(Integer teamMemberId, Integer companyId);

	public boolean checkIsPlaybookApproverByTeamMemberIdAndCompanyId(Integer teamMemberId, Integer companyId);

	public Integer updateApprovalConfigurationSettingsForCompany(ApprovalSettingsDTO approvalSettingsDTO);

	public Integer updateApprovalConfigurationSettingsForTeamMembers(TeamMemberApprovalPrivilegesDTO teamMemberApprovalPrivilegesDTO, Integer companyId,
			List<Integer> teamMemberIds);

	public List<Integer> listAssetApproverTeamMembers(Integer companyId);

	public List<Integer> listTrackApproverTeamMembers(Integer companyId);

	public List<Integer> listPlaybookApproverTeamMembers(Integer companyId);

	public List<Integer> findAllApproversByModuleTypeAndCompanyId(Integer companyId, String moduleType);

	public PendingApprovalDamAndLmsDTO fetchPendingApprovalAssetDetails(Integer entityId);

	public PendingApprovalDamAndLmsDTO fetchPendingApprovalLMSDetails(Integer entityId, String moduleType);
	
	public void autoApprovePendingAssetsCreatedByNewlyAssignedApprover(
			TeamMemberApprovalPrivilegesDTO teamMemberApprovalPrivilegesDTO, Integer loggedInUserId, Integer companyId);

	public void autoApprovePendingTracksOrPlaybooksCreatedByNewlyAssignedApprover(
			TeamMemberApprovalPrivilegesDTO teamMemberApprovalPrivilegesDTO, Integer companyId, Integer loggedInUserId,
			String moduleType);
	
	public void autoApprovePendingAssets(Integer loggedInUserId, Integer companyId, List<Integer> cretedById);

	public void autoApprovePendingLMS(List<Integer> createdByIds, Integer companyId, Integer loggedInUserId, String moduleType);

	public List<MultiSelectApprovalDTO> getPendingApprovalEntityIdsByCreatorAndModuleType(List<Integer> createdByIds, String moduleType);

	/** XNFR-813 **/
	public ApprovalStatisticsDTO getApprovalStatusTileCountsByModuleType(Integer companyId, String string);

	public void updateApprovalStatusForAssetInDraft(Integer damId, Integer updatedBy, ApprovalStatusType approvalStatusType);

	public boolean checkIsAssetApproverByTeamMemberId(Integer teamMemberId);

	public List<Integer> getApprovalReferenceIdsByDamIds(List<Integer> damIds);

	public List<Integer> getReApprovalVersionDamIdsByUserIds(List<Integer> damIds, Integer companyId);

	public void deleteVideoTagsAfterReApprovalByNames(List<String> tagNames, Integer videoId);

	public List<Integer> listReApprovalVersionDamIdsByCreatedByIds(List<Integer> createdByIds);

	public List<Integer> getReApprovalVersionDamIdsForMultiSelect(List<Integer> damIds, Integer companyId);

	public List<Integer> listWhiteLabeledParentDamIdsForReApproval(List<Integer> damIds, Integer companyId);

	public List<Integer> getPendingStateParentVersionDamIdsByCompanyId(Integer companyId);

	public void approvePendingAssetsByCompanyIdAndDamIds(Integer companyId, Integer loggedInUserId, List<Integer> damIds);

	public List<Integer> getPendingStateReApprovalVersionDamIdsByCompanyId(Integer companyId);

	public ApprovalStatusType getApprovalStatusByString(String approvalStatusInString);

}
