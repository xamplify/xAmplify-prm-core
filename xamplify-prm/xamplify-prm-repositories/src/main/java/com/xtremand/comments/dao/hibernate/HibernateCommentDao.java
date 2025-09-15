package com.xtremand.comments.dao.hibernate;

import java.util.Date;
import java.util.List;

import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.xtremand.approve.dao.ApproveDAO;
import com.xtremand.comments.dao.CommentDao;
import com.xtremand.comments.dto.CommentResponseDTO;
import com.xtremand.company.dto.ApprovalSettingsDTO;
import com.xtremand.dam.bom.ApprovalStatusHistory;
import com.xtremand.dam.bom.ApprovalStatusType;
import com.xtremand.dam.bom.Dam;
import com.xtremand.dam.dto.ApprovalStatusHistoryDTO;
import com.xtremand.dao.util.GenericDAO;
import com.xtremand.lms.bom.LearningTrack;
import com.xtremand.user.bom.User;
import com.xtremand.user.dao.UserDAO;
import com.xtremand.util.PaginationUtil;
import com.xtremand.util.XamplifyUtil;
import com.xtremand.util.XamplifyUtils;
import com.xtremand.util.bom.ModuleType;
import com.xtremand.util.dao.HibernateSQLQueryResultUtilDao;
import com.xtremand.util.dto.HibernateSQLQueryResultRequestDTO;
import com.xtremand.util.dto.QueryParameterDTO;
import com.xtremand.util.dto.XamplifyConstants;

@Repository
public class HibernateCommentDao implements CommentDao {

	@Autowired
	private SessionFactory sessionFactory;

	@Autowired
	private PaginationUtil paginationUtil;

	@Autowired
	private XamplifyUtil xamplifyUtil;
	
	@Autowired
	private HibernateSQLQueryResultUtilDao hibernateSQLQueryResultUtilDao;
	
	@Autowired
	private GenericDAO genericDAO;
	
	@Autowired
	private UserDAO userDao;
	
	@Autowired
	private ApproveDAO approveDao;


	@SuppressWarnings("unchecked")
	@Override
	public List<CommentResponseDTO> findCommentsByModuleNameAndId(Integer id, String moduleName) {
		Session session = sessionFactory.getCurrentSession();
		String tableName = "";
		String columnName = "";
		if ("emailTemplates".equals(moduleName)) {
			tableName = "xt_email_template_comments";
			columnName = "c.email_template_id";
		}
		String queryString = "select c.commented_by as \"commentedByUserId\", c.comment as \"comment\",c.commented_on as \"commentedOn\", "
				+ "case when length(TRIM(coalesce(u.firstname,'') ||' '||coalesce(u.lastname,'')))>0 then \r\n"
				+ "TRIM(coalesce(u.firstname,'') ||' '||coalesce(u.lastname,'')) else LOWER(TRIM(u.email_id)) end as \"commentedBy\",\r\n"
				+ " u.profile_image as \"commentedUserProfilePicture\" from " + tableName + " c,xt_user_profile u\r\n"
				+ "where u.user_id = c.commented_by and " + columnName + " = :id order by c.commented_on asc";
		SQLQuery query = session.createSQLQuery(queryString);
		query.setParameter("id", id);
		return (List<CommentResponseDTO>) paginationUtil.getListDTO(CommentResponseDTO.class, query);
	}
	/** XNFR-824 start **/
	@Override
	public ApprovalStatusHistoryDTO loadUserDetailsWithApprovalStatus(Integer entityId, String moduleType) {
		
		String fromTable = getFromTableByModuleType(moduleType);
		if (!XamplifyUtils.isValidString(fromTable)) {
			return new ApprovalStatusHistoryDTO();
		}
		
		String queryString = "SELECT e.id AS \"entityId\", COALESCE(CAST(e.approval_status AS TEXT), '') AS \"status\", "
				+ "e.created_time AS \"createdTime\", cp.company_logo AS \"companyLogoPath\", "
				+ "CASE WHEN LENGTH(TRIM(COALESCE(xup.firstname, '') || ' ' || COALESCE(xup.lastname, ''))) > 0 THEN "
				+ "TRIM(COALESCE(xup.firstname, '') || ' ' || COALESCE(xup.lastname, '')) ELSE LOWER(TRIM(xup.email_id)) "
				+ "END AS \"createdByName\", xup.email_id AS \"emailId\", e.created_by as \"createdBy\" FROM "+ fromTable +" e "
				+ "INNER JOIN xt_user_profile xup ON e.created_by = xup.user_id "
				+ "INNER JOIN xt_company_profile cp ON xup.company_id = cp.company_id " + "WHERE e.id = :entityId";
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		hibernateSQLQueryResultRequestDTO.setQueryString(queryString);
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO("entityId", entityId));
		return (ApprovalStatusHistoryDTO) hibernateSQLQueryResultUtilDao.getDto(hibernateSQLQueryResultRequestDTO,
				ApprovalStatusHistoryDTO.class);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<ApprovalStatusHistoryDTO> loadCommentsAndTimelineHistory(Integer entityId, String moduleType) {
		
		String whereCondition = "";
		
		if(ModuleType.DAM.name().equals(moduleType)) {
			whereCondition = " xash.dam_id = :entityId ";
		} else if (ModuleType.TRACK.name().equals(moduleType) || ModuleType.PLAYBOOK.name().equals(moduleType)) {
			whereCondition = " xash.learning_track_id = :entityId ";
		}
		
		String queryString = "SELECT "
				+ "CASE WHEN LENGTH(TRIM(COALESCE(xup.firstname, '') || ' ' || COALESCE(xup.lastname, ''))) > 0 "
				+ "THEN TRIM(COALESCE(xup.firstname, '') || ' ' || COALESCE(xup.lastname, '')) "
				+ "ELSE LOWER(TRIM(xup.email_id)) END AS \"createdByName\", "
				+ "xash.status AS \"status\", xash.comment as \"comment\" ,"
				+ "xash.created_time AS \"createdTime\", xash.created_by as \"createdBy\", "
				+ "SUBSTRING(CASE WHEN LENGTH(TRIM(COALESCE(xup.firstname, '') || ' ' || COALESCE(xup.lastname, ''))) > 0 "
				+ "THEN LOWER(TRIM(COALESCE(xup.firstname, '') || ' ' || COALESCE(xup.lastname, ''))) "
				+ "ELSE LOWER(TRIM(xup.email_id)) END FROM 1 FOR 1) AS \"userLogoPathOrFirstLetter\" "
				+ "FROM xt_approval_status_history xash "
				+ "INNER JOIN xt_user_profile xup ON xash.created_by = xup.user_id WHERE" + whereCondition
				+ "and module_type = '" + moduleType + "' " + "ORDER BY xash.created_time";

		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		hibernateSQLQueryResultRequestDTO.setQueryString(queryString);
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO("entityId", entityId));
		return (List<ApprovalStatusHistoryDTO>) hibernateSQLQueryResultUtilDao.getListDto(hibernateSQLQueryResultRequestDTO,
				ApprovalStatusHistoryDTO.class);
	}

	@Override
	public Integer updateApprovalStatusByEntityIdAndModuleType(String moduleType, Integer entityId, Integer updatedBy,
			String statusInString) {
		Integer updatedCount = null;

		if (XamplifyUtils.isValidString(moduleType) && XamplifyUtils.isValidInteger(entityId)
				&& XamplifyUtils.isValidInteger(updatedBy)) {

			String fromTable = getFromTableByModuleType(moduleType);
			if (!XamplifyUtils.isValidString(fromTable)) {
				return null;
			}

			String queryString = "UPDATE " + fromTable
					+ " SET approval_status = cast(:statusInString as approval_status_type), approval_status_updated_by = :updatedBy, "
					+ "approval_status_updated_time = :updatedTime WHERE id = :id";
			HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
			hibernateSQLQueryResultRequestDTO.setQueryString(queryString);
			hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
					.add(new QueryParameterDTO(XamplifyConstants.ID, entityId));
			hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
					.add(new QueryParameterDTO("statusInString", statusInString));
			hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO("updatedBy", updatedBy));
			hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
					.add(new QueryParameterDTO("updatedTime", new Date()));
			updatedCount = hibernateSQLQueryResultUtilDao.updateAndReturnCount(hibernateSQLQueryResultRequestDTO);
		}
		return updatedCount;
	}
	
	@Override
	public String getFromTableByModuleType(String moduleType) {
		if (ModuleType.DAM.name().equals(moduleType)) {
			return "xt_dam";
		} else if (ModuleType.TRACK.name().equals(moduleType) || ModuleType.PLAYBOOK.name().equals(moduleType)) {
			return "xt_learning_track";
		}
		return "";
	}
	
	@Override
	public void createApprovalStatusHistory(Integer entityId, Integer createdBy, ModuleType moduleType) {
		if (XamplifyUtils.isValidInteger(entityId) && XamplifyUtils.isValidInteger(createdBy)
				&& XamplifyUtils.isValidString(moduleType.name())) {
			ApprovalStatusHistory approvalStatusHistory = new ApprovalStatusHistory();
			User user = new User();
			user.setUserId(createdBy);
			approvalStatusHistory.setCreatedBy(user);
			
			setApprovalStatusHistoryEntityIdByModuleType(entityId, moduleType, approvalStatusHistory);
			
			ApprovalStatusHistoryDTO approvalStatusHistoryDTO = new ApprovalStatusHistoryDTO();
			approvalStatusHistoryDTO.setModuleType(moduleType.name());
			setApprovalStatusHistoryModuleType(approvalStatusHistoryDTO, approvalStatusHistory);
			
			Integer companyId = userDao.getCompanyIdByUserId(createdBy);
			boolean isApprovalPrivilegeManager = approveDao.isApprovalPrivilegeManager(createdBy);
			boolean canAutoApprove = false;

			canAutoApprove = checkForAutoApproval(createdBy, moduleType, isApprovalPrivilegeManager, companyId,
					canAutoApprove);

			approvalStatusHistory.setStatus(canAutoApprove ? ApprovalStatusType.APPROVED : ApprovalStatusType.CREATED);

			approvalStatusHistory.setCreatedTime(new Date());
			genericDAO.save(approvalStatusHistory);
		}
	}

	/** XNFR-821 **/
	private boolean checkForAutoApproval(Integer createdBy, ModuleType moduleType, boolean isApprovalPrivilegeManager,
			Integer companyId, boolean canAutoApprove) {
		boolean isAssetApprover = false;
		boolean isTrackApprover = false;
		boolean isPlaybookApprover = false;
		if (XamplifyUtils.isValidInteger(companyId)) {
		    ApprovalSettingsDTO approvalSettingsDTO = userDao.getApprovalConfigurationSettingsByCompanyId(companyId);

		    switch (moduleType) {
		        case DAM:
		            isAssetApprover = approveDao.checkIsAssetApproverByTeamMemberIdAndCompanyId(createdBy, companyId);
		            canAutoApprove = !approvalSettingsDTO.isApprovalRequiredForAssets() || isApprovalPrivilegeManager || isAssetApprover;
		            break;
		        case TRACK:
		            isTrackApprover = approveDao.checkIsTrackApproverByTeamMemberIdAndCompanyId(createdBy, companyId);
		            canAutoApprove = !approvalSettingsDTO.isApprovalRequiredForTracks() || isApprovalPrivilegeManager || isTrackApprover;
		            break;
		        case PLAYBOOK:
		            isPlaybookApprover = approveDao.checkIsPlaybookApproverByTeamMemberIdAndCompanyId(createdBy, companyId);
		            canAutoApprove = !approvalSettingsDTO.isApprovalRequiredForPlaybooks() || isApprovalPrivilegeManager || isPlaybookApprover;
		            break;
		        default:
		            break;
		    }
		}
		return canAutoApprove;
	}
	
	@Override
	public void setApprovalStatusHistoryEntityIdByModuleType(Integer entityId, ModuleType moduleType,
			ApprovalStatusHistory approvalStatusHistory) {
		if (ModuleType.DAM.equals(moduleType)) {
			Dam dam = new Dam();
			dam.setId(entityId);
			approvalStatusHistory.setDam(dam);
		} else if (ModuleType.TRACK.equals(moduleType) || ModuleType.PLAYBOOK.equals(moduleType)) {
			 LearningTrack learningTrack = new LearningTrack();
		     learningTrack.setId(entityId);
		     approvalStatusHistory.setLearningTrack(learningTrack);
		}
	}
	
	private void setApprovalStatusHistoryModuleType(ApprovalStatusHistoryDTO approvalStatusHistoryDTO,	ApprovalStatusHistory approvalStatusHistory) {
		String moduleType = approvalStatusHistoryDTO.getModuleType();

		if (ModuleType.DAM.name().equals(moduleType)) {
			approvalStatusHistory.setModuleType(ModuleType.DAM);
		} else if (ModuleType.TRACK.name().equals(moduleType)) {
			approvalStatusHistory.setModuleType(ModuleType.TRACK);
		} else if (ModuleType.PLAYBOOK.name().equals(moduleType)) {
			approvalStatusHistory.setModuleType(ModuleType.PLAYBOOK);
		}
	}
	/** XNFR-824 **/
	
	@Override
	public void replaceReApprovalVersionAssetCommentsToParentAsset(Integer reApprovalVersionDamId, Integer parentVersionDamId) {
		if (XamplifyUtils.isValidInteger(reApprovalVersionDamId) && XamplifyUtils.isValidInteger(parentVersionDamId)) {
			String queryString = "update xt_approval_status_history set dam_id =:parentVersionDamId where dam_id =:reApprovalVersionDamId";
			HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		    hibernateSQLQueryResultRequestDTO.setQueryString(queryString);
		    hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO("parentVersionDamId", parentVersionDamId));
		    hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO("reApprovalVersionDamId", reApprovalVersionDamId));
			hibernateSQLQueryResultUtilDao.update(hibernateSQLQueryResultRequestDTO);
		}
	}	
	
	@Override
	public void createApprovalStatusHistory(Integer entityId, Integer createdBy, ModuleType moduleType, 
			String approvalStatusTypeInString) {
		if (XamplifyUtils.isValidInteger(entityId) && XamplifyUtils.isValidInteger(createdBy)
				&& XamplifyUtils.isValidString(moduleType.name())) {
			ApprovalStatusHistory approvalStatusHistory = new ApprovalStatusHistory(); 
			User user = new User();
			user.setUserId(createdBy);
			approvalStatusHistory.setCreatedBy(user);
			setApprovalStatusHistoryEntityIdByModuleType(entityId, moduleType, approvalStatusHistory);
			ApprovalStatusHistoryDTO approvalStatusHistoryDTO = new ApprovalStatusHistoryDTO();
			approvalStatusHistoryDTO.setModuleType(moduleType.name());
			setApprovalStatusHistoryModuleType(approvalStatusHistoryDTO, approvalStatusHistory);
			ApprovalStatusType approvalStatusType = approveDao.getApprovalStatusByString(approvalStatusTypeInString);
			approvalStatusHistory.setStatus(approvalStatusType);
			approvalStatusHistory.setCreatedTime(new Date());
			genericDAO.save(approvalStatusHistory);
		}
	}

}
