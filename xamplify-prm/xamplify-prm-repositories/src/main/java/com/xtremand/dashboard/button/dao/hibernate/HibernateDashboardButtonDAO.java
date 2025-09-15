package com.xtremand.dashboard.button.dao.hibernate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.xtremand.common.bom.Pagination;
import com.xtremand.dashboard.button.dao.DashboardButtonDao;
import com.xtremand.dashboard.buttons.bom.DashboardButton;
import com.xtremand.dashboard.buttons.bom.DashboardButtonsPartnerGroupMapping;
import com.xtremand.dashboard.buttons.dto.DashboardAlternateUrlDTO;
import com.xtremand.dashboard.buttons.dto.DashboardButtonsDTO;
import com.xtremand.dashboard.buttons.dto.DashboardButtonsPartnersDTO;
import com.xtremand.dashboard.buttons.dto.PublishedDashboardButtonDetailsDTO;
import com.xtremand.partnership.bom.Partnership;
import com.xtremand.user.bom.User;
import com.xtremand.user.bom.UserUserList;
import com.xtremand.util.PaginationUtil;
import com.xtremand.util.XamplifyUtils;
import com.xtremand.util.dao.HibernateSQLQueryResultUtilDao;
import com.xtremand.util.dto.HibernateSQLQueryResultRequestDTO;
import com.xtremand.util.dto.QueryParameterDTO;
import com.xtremand.util.dto.XamplifyConstants;

@Repository
@Transactional
public class HibernateDashboardButtonDAO implements DashboardButtonDao {
	
	private static final Logger logger = LoggerFactory.getLogger(HibernateDashboardButtonDAO.class);

	@Autowired
	private HibernateSQLQueryResultUtilDao hibernateSQLQueryResultUtilDao;

	@Autowired
	private SessionFactory sessionFactory;

	@Autowired
	private PaginationUtil paginationUtil;
	
	@Value("${dashboardButtonsToPartner}")
	private String dashboardButtonsToPartner; 
	
	@Value("${dashboardButtonsPartnerGroups}")
	private String dashboardButtonsPartnerGroups; 
	
	@Value("${web_url}")
	private String webUrl;

	@SuppressWarnings("unchecked")
	@Override
	public List<Integer> findPublishedPartnerGroupPartnerIds(Integer userListId, Integer dashboardButtonId) {
		String queryString = "select distinct xuul.user_id from xt_dashboard_buttons_partner_group_mapping dbpgm,xt_user_userlist xuul where dbpgm.dashboard_button_id = :dashboardButtonId and xuul.user_list_id = :userListId and xuul.id = dbpgm.user_user_list_id";
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		hibernateSQLQueryResultRequestDTO.setQueryString(queryString);
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
				.add(new QueryParameterDTO("dashboardButtonId", dashboardButtonId));
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO("userListId", userListId));
		return (List<Integer>) hibernateSQLQueryResultUtilDao.returnList(hibernateSQLQueryResultRequestDTO);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<DashboardButtonsPartnersDTO> findUserListIdAndPartnerIdAndPartnershipIds(Integer userListId,
			Integer dashboardButtonId) {
		List<Integer> totalPublishedPartnerIds = findPublishedPartnerGroupPartnerIds(userListId, dashboardButtonId);
		List<DashboardButtonsPartnersDTO> dashboardButtonsPartnerGroupsDTOs = new ArrayList<>();
		List<List<Integer>> chunkedPublishedPartnerIds = XamplifyUtils.getChunkedList(totalPublishedPartnerIds);
		Session session = sessionFactory.getCurrentSession();
		String sqlString = "select distinct uul.id as \"userListId\",uul.user_id as \"partnerId\",p.id as \"partnershipId\" \r\n"
				+ "from xt_user_userlist uul,xt_partnership p,xt_user_list ul where p.partner_id = uul.user_id and uul.user_list_id = :userListId and ul.user_list_id = uul.user_list_id\r\n"
				+ "and ul.company_id = p.vendor_company_id and uul.user_id not in (:publishedPartnerIds) order by uul.id asc";
		for (List<Integer> chunkedPartnerIds : chunkedPublishedPartnerIds) {
			Query query = session.createSQLQuery(sqlString);
			query.setParameterList("publishedPartnerIds", chunkedPartnerIds);
			List<DashboardButtonsPartnersDTO> list = (List<DashboardButtonsPartnersDTO>) paginationUtil
					.getListDTO(DashboardButtonsPartnersDTO.class, query);
			dashboardButtonsPartnerGroupsDTOs.addAll(list);
		}
		return dashboardButtonsPartnerGroupsDTOs;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<PublishedDashboardButtonDetailsDTO> findPublishedDashboardButtonIdAndTitlesByPartnerListIdAndCompanyId(
			Set<Integer> partnerGroupIds, Integer companyId) {
		if (XamplifyUtils.isNotEmptySet(partnerGroupIds) && XamplifyUtils.isValidInteger(companyId)) {
			List<PublishedDashboardButtonDetailsDTO> dashboardButtonDtos = new ArrayList<>();
			List<List<Integer>> chunkedPartnerGroupIdsList = XamplifyUtils
					.getChunkedList(XamplifyUtils.convertSetToList(partnerGroupIds));
			Session session = sessionFactory.getCurrentSession();
			String sqlString = "select distinct xdb.id as \"id\",xdb.title as \"buttonTitle\",string_agg(distinct cast(xul.user_list_id as text), ',') as \"partnerGroupIdsString\" "
					+ " from xt_dashboard_buttons xdb,xt_dashboard_buttons_partner_group_mapping\r\n"
					+ "xdbgm,xt_user_userlist xuul,xt_user_list xul where xdb.id = xdbgm.dashboard_button_id and xdbgm.user_user_list_id = xuul.id\r\n"
					+ "and xuul.user_list_id in (:partnerGroupIds) and  xdb.vendor_company_id = :companyId and xul.user_list_id = xuul.user_list_id group by xdb.id order by 1 asc";
			for (List<Integer> chunkedPartnerGroupIds : chunkedPartnerGroupIdsList) {
				Query query = session.createSQLQuery(sqlString);
				query.setParameterList("partnerGroupIds", chunkedPartnerGroupIds);
				query.setParameter("companyId", companyId);
				List<PublishedDashboardButtonDetailsDTO> list = (List<PublishedDashboardButtonDetailsDTO>) paginationUtil
						.getListDTO(PublishedDashboardButtonDetailsDTO.class, query);
				dashboardButtonDtos.addAll(list);
			}
			return dashboardButtonDtos;

		} else {
			return Collections.emptyList();
		}

	}

	@SuppressWarnings("unchecked")
	@Override
	public List<DashboardButtonsPartnersDTO> findUserListIdsAndPartnerIdsAndPartnershipIdsByPartnerGroupIdAndPartnerIds(
			Integer partnerGroupId, Set<Integer> partnerIds) {
		List<DashboardButtonsPartnersDTO> dashboardButtonsPublishedPartnerGroupsDTOs = new ArrayList<>();
		if (XamplifyUtils.isNotEmptySet(partnerIds) && XamplifyUtils.isValidInteger(partnerGroupId)) {
			String queryString = "select distinct ul.user_list_id as \"userListId\", uul.id as \"userUserListId\",uul.user_id as \"partnerId\",p.id as \"partnershipId\" from xt_user_userlist uul,xt_partnership p,xt_user_list ul\r\n"
					+ "where p.partner_id = uul.user_id and uul.user_list_id = :partnerGroupId and ul.user_list_id = uul.user_list_id\r\n"
					+ "and ul.company_id = p.vendor_company_id and uul.user_id in (:partnerIds) order by uul.id asc";
			List<List<Integer>> chunkedUserList = XamplifyUtils
					.getChunkedList(XamplifyUtils.convertSetToList(partnerIds));
			Session session = sessionFactory.getCurrentSession();
			for (List<Integer> chunkedPartnerIds : chunkedUserList) {
				Query query = session.createSQLQuery(queryString);
				query.setParameterList("partnerIds", chunkedPartnerIds);
				query.setParameter("partnerGroupId", partnerGroupId);
				List<DashboardButtonsPartnersDTO> list = (List<DashboardButtonsPartnersDTO>) paginationUtil
						.getListDTO(DashboardButtonsPartnersDTO.class, query);
				dashboardButtonsPublishedPartnerGroupsDTOs.addAll(list);
			}
		}
		return dashboardButtonsPublishedPartnerGroupsDTOs;

	}

	@Override
	public void updateStatus(Set<Integer> dashboardButtonIds, boolean isPublishingInProgress) {
		if (XamplifyUtils.isNotEmptySet(dashboardButtonIds)) {
			Session session = sessionFactory.getCurrentSession();
			String sqlString = "update xt_dashboard_buttons set is_publishing_in_progress = :status where id in (:ids)";
			List<List<Integer>> chunkedDashboardButtonIdsList = XamplifyUtils
					.getChunkedList(XamplifyUtils.convertSetToList(dashboardButtonIds));
			for (List<Integer> chunkedDashboardButtonIds : chunkedDashboardButtonIdsList) {
				Query query = session.createSQLQuery(sqlString);
				query.setParameterList("ids", chunkedDashboardButtonIds);
				query.setParameter("status", isPublishingInProgress);
				query.executeUpdate();
			}
		}

	}

	@Override
	public boolean isPublishingInProgress(Integer dashboardButtonId) {
		String sqlString = "select is_publishing_in_progress from xt_dashboard_buttons where id = :id";
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		hibernateSQLQueryResultRequestDTO.setQueryString(sqlString);
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO("id", dashboardButtonId));
		return (boolean) hibernateSQLQueryResultUtilDao.getUniqueResult(hibernateSQLQueryResultRequestDTO);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Integer> findUnPublishedDashboardButtonIdsByCompanyId(Integer companyId) {
		String sqlQueryString = "select xdb.id from xt_dashboard_buttons xdb\n"
				+ "left join xt_dashboard_buttons_partner_company_mapping xdbpcm on xdbpcm.dashboard_button_id  = xdb.id\n"
				+ "left join xt_dashboard_buttons_partner_group_mapping xdbpgm on xdbpgm.dashboard_button_id  = xdb.id\n"
				+ "where xdb.vendor_company_id  = :companyId group by xdb.id having count(xdbpcm.id)=0 and count(xdbpgm.id)=0\n"
				+ "order by 1 asc\n";
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		hibernateSQLQueryResultRequestDTO.setQueryString(sqlQueryString);
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO("companyId", companyId));
		return (List<Integer>) hibernateSQLQueryResultUtilDao.returnList(hibernateSQLQueryResultRequestDTO);
	}

	@Override
	public Map<String, Object> findAllPublishedAndUnPublished(Pagination pagination, String search) {
		String unionSelectQueryString ;
		if(XamplifyUtils.isValidInteger(pagination.getUserUserListId())) {
			unionSelectQueryString = dashboardButtonsToPartner;
		}else {
			unionSelectQueryString = dashboardButtonsPartnerGroups;
		}

		unionSelectQueryString = addSearchAndSort(pagination.getUserUserListId(),search,pagination ,unionSelectQueryString);
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		hibernateSQLQueryResultRequestDTO.setQueryString(unionSelectQueryString);
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
		.add(new QueryParameterDTO(XamplifyConstants.COMPANY_ID, pagination.getCompanyId()));
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
		.add(new QueryParameterDTO(XamplifyConstants.USER_LIST_ID, pagination.getUserListId()));
		if(XamplifyUtils.isValidInteger(pagination.getUserUserListId())) {
			hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
			.add(new QueryParameterDTO("userUserListId", pagination.getUserUserListId()));
		}
		hibernateSQLQueryResultRequestDTO.setClassInstance(DashboardButtonsDTO.class);
		return hibernateSQLQueryResultUtilDao.returnPaginatedDTOList(hibernateSQLQueryResultRequestDTO, pagination,
				search);
	}

	@Override
	public boolean isDashboardButtonPublished(Integer dashboardButtonId, Integer partnershipId, Integer userUserListId,
			Integer partnerUserId) {
		String sqlString = "select case when count(*)>0 then true else false end as published from xt_dashboard_buttons_partner_group_mapping\r\n"
				+ "where partnership_id = :partnershipId and user_user_list_id = :userUserListId "
				+ " and dashboard_button_id = :dashboardButtonId";
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		hibernateSQLQueryResultRequestDTO.setQueryString(sqlString);
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
				.add(new QueryParameterDTO("partnershipId", partnershipId));
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
				.add(new QueryParameterDTO("userUserListId", userUserListId));
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
				.add(new QueryParameterDTO("dashboardButtonId", dashboardButtonId));
		return (boolean) hibernateSQLQueryResultUtilDao.getUniqueResult(hibernateSQLQueryResultRequestDTO);
	}

	@Override
	public void publishDashboardButtonToPartnerCompanyByPartnerId(Integer dashboardButtonId, Integer partnershipId,
			Integer userUserListId, Integer loggedInUserId) {
		DashboardButtonsPartnerGroupMapping dashboardButtonPartnerGroupMapping = new DashboardButtonsPartnerGroupMapping();
		DashboardButton dashboardButton = new DashboardButton();
		dashboardButtonPartnerGroupMapping.setDashboardButton(dashboardButton);
		dashboardButton.setId(dashboardButtonId);
		Partnership partnership = new Partnership();
		dashboardButtonPartnerGroupMapping.setPartnership(partnership);
		partnership.setId(partnershipId);
		UserUserList userUserList = new UserUserList();
		userUserList.setId(userUserListId);
		dashboardButtonPartnerGroupMapping.setUserUserList(userUserList);
		dashboardButtonPartnerGroupMapping.setPublishedOn(new Date());
		User publishedBy = new User();
		publishedBy.setUserId(loggedInUserId);
		dashboardButtonPartnerGroupMapping.setPublishedBy(publishedBy);
		hibernateSQLQueryResultUtilDao.save(dashboardButtonPartnerGroupMapping);

	}

	@Override
	public void updateStatus(Integer dashboardButtonId, boolean isPublishingInProgress) {
		Session session = sessionFactory.openSession();
		try {
			String sqlString = "update xt_dashboard_buttons set is_publishing_in_progress = :status where id = :id";
			session.createSQLQuery(sqlString).setParameter("id", dashboardButtonId)
					.setParameter("status", isPublishingInProgress).executeUpdate();
			session.flush();
			session.clear();
		} catch (HibernateException he) {

		} catch (Exception e) {

		} finally {
			session.close();
		}

	}

	@Override
	public Set<String> saveDashboardButtonsMapping(List<DashboardButtonsPartnersDTO> dashboardButtonsPartnerDTOs,
			Integer dashboardId, Integer vendorId, List<Integer> publishedPartnerIds) {
		Set<String> savedTitles = new HashSet<>();
		String title = getDashboardButtonsTitlesById(dashboardId);
		for (DashboardButtonsPartnersDTO dashboardButtonsPartnerDTO : dashboardButtonsPartnerDTOs) {
			if (publishedPartnerIds.indexOf(dashboardButtonsPartnerDTO.getPartnerId())> -1) {
				logger.info("Already Published");
				continue; 
			}
			Session session = sessionFactory.openSession();
			try {
				savePartnerGroupMapping(dashboardId, vendorId, savedTitles, title, dashboardButtonsPartnerDTO, session);

			} catch (HibernateException he) {
				he.printStackTrace(); 
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				session.close();
			}
		}
		return savedTitles;
	}

	private void savePartnerGroupMapping(Integer dashboardId, Integer vendorId, Set<String> savedTitles, String title,
			DashboardButtonsPartnersDTO dashboardButtonsPartnerDTO, Session session) {
		DashboardButtonsPartnerGroupMapping dashboardButtonsPartnerGroupMapping = framedashboardButtonsPartnerGroupMapping(
				dashboardId, vendorId, dashboardButtonsPartnerDTO);
		session.save(dashboardButtonsPartnerGroupMapping); 
		session.flush();
		session.clear();
		if(XamplifyUtils.isValidString(title)) {
			savedTitles.add(title);
		}
	}

	private DashboardButtonsPartnerGroupMapping framedashboardButtonsPartnerGroupMapping(Integer dashboardId,
			Integer vendorId, DashboardButtonsPartnersDTO dashboardButtonsPartnerDTO) {
		DashboardButtonsPartnerGroupMapping dashboardButtonsPartnerGroupMapping = new DashboardButtonsPartnerGroupMapping();
		DashboardButton dashboardButton = new DashboardButton();
		dashboardButton.setId(dashboardId);
		dashboardButtonsPartnerGroupMapping.setDashboardButton(dashboardButton);
		UserUserList userUserList = new UserUserList();
		userUserList.setId(dashboardButtonsPartnerDTO.getUserListId());
		dashboardButtonsPartnerGroupMapping.setUserUserList(userUserList);
		Partnership partnership = new Partnership();
		partnership.setId(dashboardButtonsPartnerDTO.getPartnershipId());
		dashboardButtonsPartnerGroupMapping.setPartnership(partnership);
		dashboardButtonsPartnerGroupMapping.setPublishedOn(new Date());
		User publishedBy = new User();
		publishedBy.setUserId(vendorId);
		dashboardButtonsPartnerGroupMapping.setPublishedBy(publishedBy);
		return dashboardButtonsPartnerGroupMapping;
	}

	
	@Override
	public Set<String> savePartnerCompanyDashboardbuttons(Integer dashboardId,
			Integer vendorId, DashboardButtonsPartnersDTO dashboardButtonsPartnerDTO) {
		Set<String> savedTitles = new HashSet<>();
		String title = getDashboardButtonsTitlesById(dashboardId);
		Session session = sessionFactory.openSession();
		try {
			savePartnerGroupMapping(dashboardId, vendorId, savedTitles, title, dashboardButtonsPartnerDTO, session);

		} catch (HibernateException he) {
			he.printStackTrace(); 
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			session.close();
		}
		return savedTitles;
	}
	
	public String getDashboardButtonsTitlesById(Integer dashboardButtonsId) {
		if (XamplifyUtils.isValidInteger(dashboardButtonsId)) {
			HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
			hibernateSQLQueryResultRequestDTO.setQueryString(" select title from xt_dashboard_buttons where id in (:id)");
			hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO("id", dashboardButtonsId));
			return (String) hibernateSQLQueryResultUtilDao.getUniqueResult(hibernateSQLQueryResultRequestDTO);
			} else {
			return "";
		}
	}
	
	 private String addSearchAndSort(Integer userUserListId,String search,Pagination pagination ,String unionSelectQueryString ) {
		 if(XamplifyUtils.isValidInteger(userUserListId)) {
				unionSelectQueryString = unionSelectQueryString.replace("replaceuserUserListId", "AND xuul2.id in (:userUserListId)");
			}else {
				unionSelectQueryString = unionSelectQueryString.replace("replaceuserUserListId", "");
			}
			if(XamplifyUtils.isValidString(search)) {
				unionSelectQueryString = unionSelectQueryString.replace("searchkey","AND (LOWER(xdb.title) LIKE LOWER('%" +search + "%')  OR LOWER(xdb.link) LIKE LOWER('%" +search + "%'))");
			}else {
				unionSelectQueryString = unionSelectQueryString.replace("searchkey","");
			}

			if(XamplifyUtils.isValidString(pagination.getSortcolumn())) {
				String sortColumn = pagination.getSortcolumn().equalsIgnoreCase("createdTime") ? "\"createdTime\"" : "\"buttonTitle\"";
				unionSelectQueryString = unionSelectQueryString.replace("sortcolumn", sortColumn + " " + pagination.getSortingOrder() + " nulls last");
				pagination.setSortcolumn("");
			}else {
				unionSelectQueryString = unionSelectQueryString.replace("sortcolumn", "\"createdTime\"" + " " + "desc" + " nulls last");
			}
			return unionSelectQueryString;
	 }
	 
	 @SuppressWarnings("unchecked")
	 @Override
	 public List<Integer> findPublishedPartnershipIds(Integer dashboardButtonsId){
		 String sqlString = "select distinct partnership_id from xt_dashboard_buttons_partner_group_mapping where dashboard_button_id = :id \n" + 
				 " order by partnership_id asc";
		 HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		 hibernateSQLQueryResultRequestDTO.setQueryString(sqlString);
		 hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO("id", dashboardButtonsId));
		 return (List<Integer>) hibernateSQLQueryResultUtilDao.returnList(hibernateSQLQueryResultRequestDTO);


	 }

	 @SuppressWarnings("unchecked")
	 @Override
	 public List<DashboardAlternateUrlDTO> findAlternateUrls(String referenceUrl){
		 String urlString = webUrl;
	     String hostname = urlString.replaceFirst("^(https?://)", "").split("/")[0];

		 String sqlString = " \r\n" + 
		 		"WITH extracted AS (\r\n" + 
		 		"    SELECT \r\n" + 
		 		"        :url AS input_url,\r\n" + 
		 		"        :hostname AS input_hostname,\r\n" + 
		 		"        CASE \r\n" + 
		 		"            WHEN :url ~ ('^(?:https?://)?([^/.]+)\\.' || regexp_replace(:hostname, '\\.', '\\\\.', 'g')) \r\n" + 
		 		"            THEN regexp_replace(:url, '^(?:https?://)?([^/.]+)\\.' || regexp_replace(:hostname, '\\.', '\\\\.', 'g') || '.*', '\\1')\r\n" + 
		 		"            ELSE NULL\r\n" + 
		 		"        END AS subdomain\r\n" + 
		 		"),\r\n" + 
		 		"matched_reference AS (\r\n" + 
		 		"    SELECT id \r\n" + 
		 		"    FROM xt_dashboard_reference_url r, extracted\r\n" + 
		 		"    WHERE extracted.input_url ~ ('^(?:https?://)?([^/]+\\.)?' || regexp_replace(:hostname || r.url, '\\*', '.*', 'g') || '$')\r\n" + 
		 		"),\r\n" + 
		 		"alternate_urls AS (\r\n" + 
		 		"    SELECT DISTINCT \r\n" + 
		 		"        'https://' || \r\n" + 
		 		"        CASE \r\n" + 
		 		"            WHEN extracted.subdomain IS NOT NULL \r\n" + 
		 		"            THEN extracted.subdomain || '.' || extracted.input_hostname || a.alternate_url\r\n" + 
		 		"            ELSE extracted.input_hostname || a.alternate_url\r\n" + 
		 		"        END AS id, \r\n" + 
		 		"        a.title AS \"name\" \r\n" + 
		 		"    FROM xt_dashboard_alternate_url a\r\n" + 
		 		"    JOIN matched_reference mr ON a.reference_url_id = mr.id\r\n" + 
		 		"    LEFT JOIN extracted ON TRUE\r\n" + 
		 		")\r\n" + 
		 		"SELECT * FROM alternate_urls\r\n" + 
		 		"WHERE EXISTS (SELECT 1 FROM matched_reference)\r\n" + 
		 		"UNION ALL\r\n" + 
		 		"(\r\n" + 
		 		"    SELECT DISTINCT \r\n" + 
		 		"        'https://' ||  \r\n" + 
		 		"        CASE    \r\n" + 
		 		"            WHEN extracted.subdomain IS NOT NULL \r\n" + 
		 		"            THEN extracted.subdomain || '.' || extracted.input_hostname || b.alternate_url\r\n" + 
		 		"            ELSE extracted.input_hostname || b.alternate_url\r\n" + 
		 		"        END AS id, \r\n" + 
		 		"        b.title AS \"name\" \r\n" + 
		 		"    FROM xt_dashboard_alternate_url b, extracted\r\n" + 
		 		"    WHERE reference_url_id IS NULL \r\n" + 
		 		"    AND EXISTS (SELECT 1 FROM matched_reference)\r\n" + 
		 		"); " ;
		 HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		 hibernateSQLQueryResultRequestDTO.setQueryString(sqlString);
		 hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO("url", referenceUrl));
		 hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO("hostname", hostname));
		 hibernateSQLQueryResultRequestDTO.setClassInstance(DashboardAlternateUrlDTO.class);
		 return (List<DashboardAlternateUrlDTO>) hibernateSQLQueryResultUtilDao.returnDTOList(hibernateSQLQueryResultRequestDTO);

	 }

}
		

