package com.xtremand.comments.dao;

import java.util.List;

import com.xtremand.comments.dto.CommentResponseDTO;
import com.xtremand.dam.bom.ApprovalStatusHistory;
import com.xtremand.dam.dto.ApprovalStatusHistoryDTO;
import com.xtremand.util.bom.ModuleType;

public interface CommentDao {


	List<CommentResponseDTO> findCommentsByModuleNameAndId(Integer id, String moduleName);

	public ApprovalStatusHistoryDTO loadUserDetailsWithApprovalStatus(Integer id, String moduleType);

	public List<ApprovalStatusHistoryDTO> loadCommentsAndTimelineHistory(Integer entityId, String moduleType);
	
	public void createApprovalStatusHistory(Integer entityId, Integer createdBy, ModuleType moduleType);
	
	public Integer updateApprovalStatusByEntityIdAndModuleType(String moduleType, Integer entityId, Integer updatedBy,
			String statusInString);

	public String getFromTableByModuleType(String moduleType);

	public void setApprovalStatusHistoryEntityIdByModuleType(Integer entityId, ModuleType moduleType,
			ApprovalStatusHistory approvalStatusHistory);

	public void replaceReApprovalVersionAssetCommentsToParentAsset(Integer reApprovalVersionDamId, Integer parentVersionDamId);

	public void createApprovalStatusHistory(Integer entityId, Integer createdBy, ModuleType moduleType,
			String approvalStatusTypeInString);

}
