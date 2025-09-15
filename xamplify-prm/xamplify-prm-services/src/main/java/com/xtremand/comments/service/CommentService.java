package com.xtremand.comments.service;

import com.xtremand.comments.dto.CommentRequestDTO;
import com.xtremand.dam.bom.ApprovalStatusHistory;
import com.xtremand.dam.dto.ApprovalStatusHistoryDTO;
import com.xtremand.formbeans.XtremandResponse;

public interface CommentService {

	XtremandResponse findCommentsByModuleNameAndId(Integer id, String moduleType);

	XtremandResponse save(CommentRequestDTO commentRequestDTO);


	public XtremandResponse loadUserDetailsWithApprovalStatus(Integer entityId, String moduleType);

	public XtremandResponse loadCommentsAndTimelineHistory(Integer entityId, String moduleType);

	public XtremandResponse updateApprovalStatusAndSaveComment(ApprovalStatusHistoryDTO damStatusHistoryDTO);

	public void setApprovalStatusHistoryModuleType(ApprovalStatusHistoryDTO approvalStatusHistoryDTO,
			ApprovalStatusHistory approvalStatusHistory);

}
