package com.xtremand.dashboard.layout.dao.hibernate;

import java.util.Date;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.exception.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.xtremand.custom.html.block.bom.CustomHtmlBlock;
import com.xtremand.dao.util.GenericDAO;
import com.xtremand.dashboard.layout.bom.DashboardCustomLayout;
import com.xtremand.dashboard.layout.bom.DashboardLayout;
import com.xtremand.dashboard.layout.dao.DashboardLayoutDao;
import com.xtremand.dashboard.layout.dto.DashboardLayoutDTO;
import com.xtremand.dashboard.layout.dto.DashboardLayoutRequestDTO;
import com.xtremand.util.XamplifyUtils;
import com.xtremand.util.dao.HibernateSQLQueryResultUtilDao;
import com.xtremand.util.dto.HibernateSQLQueryResultRequestDTO;
import com.xtremand.util.dto.QueryParameterDTO;
import com.xtremand.util.dto.XamplifyConstants;

@Repository
@Transactional
public class HibernateDashboardLayoutDao implements DashboardLayoutDao {

	private static final Logger logger = LoggerFactory.getLogger(HibernateDashboardLayoutDao.class);

	@Autowired
	private GenericDAO genericDao;

	@Autowired
	private SessionFactory sessionFactory;

	@Autowired
	private HibernateSQLQueryResultUtilDao hibernateSQLQueryResultUtilDao;

	public static final String XT_DASHBOARD_LAYOUT = "xt_dashboard_layout";

	public static final String XT_DASHBOARD_CUSTOM_LAYOUT = "xt_dashboard_custom_layout";

	public static final String CUSTOM_DASHBOARD_SELECT_QUERY = "select xdl.div_name as \"divName\", "
			+ "xdcl.display_index as \"displayIndex\" \n from " + XT_DASHBOARD_LAYOUT + " xdl, "
			+ XT_DASHBOARD_CUSTOM_LAYOUT + " xdcl \n where xdl.id = xdcl.dashboard_layout_id "
			+ "and xdcl.created_user_id = :userId";

	public static final String CUSTOM_DASHBOARD_UPDATE_QUERY = "update " + XT_DASHBOARD_CUSTOM_LAYOUT
			+ " set updated_user_id = :updatedUserId, updated_time = :updatedTime, display_index = :displayIndex \n"
			+ "where dashboard_layout_id = :id";

	@Override
	public List<DashboardLayoutDTO> findCustomDashboardLayout(Integer userId, Integer companyId) {
		List<DashboardLayoutDTO> dashboardLayoutDtos = findDashboardLayout(userId, companyId);
		if (XamplifyUtils.isNotEmptyList(dashboardLayoutDtos)) {
			return dashboardLayoutDtos;
		} else {
			return findDefaultDashboardLayout();
		}
	}

	@SuppressWarnings("unchecked")
	private List<DashboardLayoutDTO> findDashboardLayout(Integer userId, Integer companyId) {
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		String sqlString = CUSTOM_DASHBOARD_SELECT_QUERY;
		if (XamplifyUtils.isValidInteger(companyId)) {
			sqlString += " and xdcl.company_id = :companyId \n";
		} else {
			sqlString += " and xdcl.company_id is null \n";
		}
		sqlString += "order by xdcl.display_index";
		hibernateSQLQueryResultRequestDTO.setQueryString(sqlString);
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
		.add(new QueryParameterDTO(XamplifyConstants.USER_ID, userId));
		if (XamplifyUtils.isValidInteger(companyId)) {
			hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
			.add(new QueryParameterDTO(XamplifyConstants.COMPANY_ID, companyId));
		}
		return (List<DashboardLayoutDTO>) hibernateSQLQueryResultUtilDao.getListDto(hibernateSQLQueryResultRequestDTO,
				DashboardLayoutDTO.class);
	}

	@SuppressWarnings("unchecked")
	private List<DashboardLayoutDTO> findDefaultDashboardLayout() {
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		String sqlString = "select xdl.div_id as \"divId\", xdl.div_name as \"divName\" \n" + "from "
				+ XT_DASHBOARD_LAYOUT + " xdl order by xdl.div_id";
		hibernateSQLQueryResultRequestDTO.setQueryString(sqlString);
		return (List<DashboardLayoutDTO>) hibernateSQLQueryResultUtilDao.getListDto(hibernateSQLQueryResultRequestDTO,
				DashboardLayoutDTO.class);
	}

	@Override
	public void updateCustomDashboardLayout(DashboardLayoutRequestDTO dashboardLayoutRequestDTO, Integer companyId) {
		Integer loggedInUserId = dashboardLayoutRequestDTO.getUserId();
		List<DashboardLayoutDTO> defaultDashboardLayout = findDashboardLayout(loggedInUserId, companyId);
		List<DashboardLayoutDTO> dashboardLayoutDtos = dashboardLayoutRequestDTO.getDashboardLayoutDTOs();
		if (XamplifyUtils.isNotEmptyList(defaultDashboardLayout)) {
			updateDashboardLayout(loggedInUserId, companyId, dashboardLayoutDtos);
		} else {
			createDashboardLayout(loggedInUserId, companyId, dashboardLayoutDtos);
		}
	}

	private void updateDashboardLayout(Integer userId, Integer companyId, List<DashboardLayoutDTO> dashboardLayoutDtos) {
		Integer index = 1;
		String queryString = CUSTOM_DASHBOARD_UPDATE_QUERY;
		if (XamplifyUtils.isValidInteger(companyId)) {
			queryString += findDefaultDashboardSettings(companyId) 
					? " and company_id = :companyId" 
							: " and created_user_id = :userId and company_id = :companyId";
		} else {
			queryString += " and created_user_id = :userId and company_id is null";
		}

		for (DashboardLayoutDTO dashboardLayoutDto : dashboardLayoutDtos) {
			try (Session session = sessionFactory.openSession()) {
				if (XamplifyUtils.isValidString(dashboardLayoutDto.getTitle())) {
					updateCustomHtmlBlock(dashboardLayoutDto.getCustomHtmlBlockId(), index);
				} else {
					updateDashboardLayout(session, queryString, userId, companyId, dashboardLayoutDto, index);
				}
				index++;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void updateDashboardLayout(Session session, String queryString, Integer userId, Integer companyId, DashboardLayoutDTO dashboardLayoutDto, Integer index) {
		Query query = session.createSQLQuery(queryString);
		query.setParameter("updatedUserId", userId);
		query.setParameter("updatedTime", new Date());
		query.setParameter("displayIndex", index);
		query.setParameter("id", dashboardLayoutDto.getDivId());

		if (XamplifyUtils.isValidInteger(companyId)) {
			if (!findDefaultDashboardSettings(companyId)) {
				query.setParameter(XamplifyConstants.USER_ID, userId);
			}
			query.setParameter(XamplifyConstants.COMPANY_ID, companyId);
		} else {
			query.setParameter(XamplifyConstants.USER_ID, userId);
		}

		query.executeUpdate();
		session.flush();
		session.clear();
	}

	private void createDashboardLayout(Integer userId, Integer companyId, List<DashboardLayoutDTO> dashboardLayoutDtos) {
		Integer index = 1;
		for (DashboardLayoutDTO dashboardLayoutDto : dashboardLayoutDtos) {
			Session session = sessionFactory.openSession();
			try {
				if (XamplifyUtils.isValidString(dashboardLayoutDto.getTitle())) {
					updateCustomHtmlBlock(dashboardLayoutDto.getCustomHtmlBlockId(), index);
				} else {
					DashboardCustomLayout dashboardCustomLayout = new DashboardCustomLayout();
					DashboardLayout dashboardLayout = new DashboardLayout();
					dashboardLayout.setId(dashboardLayoutDto.getDivId());
					dashboardCustomLayout.setDashboardLayout(dashboardLayout);
					dashboardCustomLayout.setCompanyId(companyId);
					dashboardCustomLayout.setCreatedUserId(userId);
					dashboardCustomLayout.setCreatedTime(new Date());
					dashboardCustomLayout.setDisplayIndex(index);
					session.save(dashboardCustomLayout);
				}

				String debugMessage = "Dashboard layout divs order : " + index;
				logger.debug(debugMessage);

				index++;
				session.flush();
				session.clear();
			} catch (ConstraintViolationException constraintViolationException) {
				constraintViolationException.printStackTrace();
			} catch (HibernateException hibernateException) {
				hibernateException.printStackTrace();
			} catch (DataIntegrityViolationException dataIntegrityViolationException) {
				dataIntegrityViolationException.printStackTrace();
			} finally {
				session.close();
			}
		}
	}

	private void updateCustomHtmlBlock(Integer customHtmlBlockId, Integer displayIndex) {
		CustomHtmlBlock customHtmlBlock = genericDao.get(CustomHtmlBlock.class, customHtmlBlockId);
		customHtmlBlock.setDisplayIndex(displayIndex);

	}

	@Override
	public boolean findDefaultDashboardSettings(Integer companyId) {
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		String queryString = "select dashboard_layout_settings from xt_company_profile where company_id = :companyId"; 
		hibernateSQLQueryResultRequestDTO.setQueryString(queryString);
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO("companyId", companyId));
		return (boolean) hibernateSQLQueryResultUtilDao.getUniqueResult(hibernateSQLQueryResultRequestDTO);
	}

	@Override
	public void updateDefaultDashboardSettings(Integer companyId, boolean isLayoutUpdated) {
		String sqlString = "update xt_company_profile set dashboard_layout_settings = :isLayoutUpdated \n"
				+ "where company_id = :companyId";
		Session session = sessionFactory.getCurrentSession();
		session.createSQLQuery(sqlString).setParameter("isLayoutUpdated", isLayoutUpdated)
		.setParameter(XamplifyConstants.COMPANY_ID, companyId).executeUpdate();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<DashboardLayoutDTO> findCustomHtmlBlocks(Integer companyId) {
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		String sqlString = "select xchb.id as \"customHtmlBlockId\", xchb.title as \"title\", "
				+ "xchb.html_body as \"htmlBody\", xchb.left_html_body as \"leftHtmlBody\", "
				+ "xchb.right_html_body as \"rightHtmlBody\",xchb.display_index as \"displayIndex\","
				+ "xchb.is_title_visible as \"titleVisible\" \n from xt_custom_html_block xchb \n "
				+ "where xchb.is_selected = true and xchb.company_id = :companyId \n "
				+ "order by COALESCE(xchb.display_index, xchb.id)";
		hibernateSQLQueryResultRequestDTO.setQueryString(sqlString);
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO("companyId", companyId));
		return (List<DashboardLayoutDTO>) hibernateSQLQueryResultUtilDao.getListDto(hibernateSQLQueryResultRequestDTO,
				DashboardLayoutDTO.class);
	}

}
