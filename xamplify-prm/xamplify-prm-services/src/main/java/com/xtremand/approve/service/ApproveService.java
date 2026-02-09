package com.xtremand.approve.service;
import java.util.List;
import java.util.Map;

import com.xtremand.approve.dto.ApprovalPrivilegesEmailNotificationDTO;
import com.xtremand.approve.dto.ContentReApprovalDTO;
import com.xtremand.approve.dto.MultiSelectApprovalDTO;
import com.xtremand.approve.dto.TeamMemberApprovalPrivilegesDTO;
import com.xtremand.common.bom.Pagination;
import com.xtremand.dam.bom.ApprovalStatusHistory;
import com.xtremand.dam.bom.DamTag;
import com.xtremand.formbeans.XtremandResponse;
import com.xtremand.util.dto.Pageable;

public interface ApproveService {

	public XtremandResponse getAllApprovalList(Pagination pagination, Integer loggedInUserId);

	public XtremandResponse getStatusTileCounts(Integer loggedInUserId, Pageable pageable);

	public XtremandResponse updateApprovalStatusByTypeForMultiSelect(Integer loggedInUserId, MultiSelectApprovalDTO multiSelectApprovalDto, XtremandResponse response);
	
	public XtremandResponse listTeamMembersForApprovalControlManagement(Pageable pageable, Integer loggedInUserId);

	public XtremandResponse saveOrUpdateApprovalControlManagementSettings(Integer loggedInUserId, List<TeamMemberApprovalPrivilegesDTO> approvalPrivilegesDTOs, 
			List<ApprovalPrivilegesEmailNotificationDTO> approvalPrivilegesEmailNotificationDTOs,MultiSelectApprovalDTO multiSelectApprovalDTO, XtremandResponse response);

	public XtremandResponse isApprovalPrivilegeManager(Integer loggedInUserId);

	public XtremandResponse getApprovalPrivileges(Integer loggedInUserId);

	public XtremandResponse sendReminderToApprovers(XtremandResponse response, Integer loggedInUserId, Integer entityId,
			String moduleType);

	public List<ApprovalStatusHistory> processAndSaveApprovalTimelineHistory(List<Integer> createdByIds, Integer loggedInUserId);

	public XtremandResponse checkIsAssetApprover(Integer loggedInUserId);

	public XtremandResponse getStatusTileCountsByModuleType(Integer loggedInUserId, String moduleType, boolean showTiles, Integer categoryId);

	public Map<String, Object> handleReApprovalVersionForVideoTypeAsset(Integer loggedInUserId,
			List<ContentReApprovalDTO> videoTypeAssetContentDetails, List<ApprovalStatusHistory> approvalHistoryList,
			String comment);

	public List<Integer> processNonVideoAssetReApprovalAndGetIds(Integer loggedInUserId, List<DamTag> allDamTagsToSave,
			List<ContentReApprovalDTO> nonVideoTypeAssetContentDetails, List<ApprovalStatusHistory> approvalHistoryList,
			String comment);

	public void handleSharedAssetPathForPdfTypeAssets(List<ContentReApprovalDTO> pdfTypeAssetContentDetails);
	
}
