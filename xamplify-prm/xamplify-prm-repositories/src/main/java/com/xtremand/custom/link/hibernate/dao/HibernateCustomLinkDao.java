package com.xtremand.custom.link.hibernate.dao;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.custom.link.dto.CustomLinkRequestDTO;
import com.custom.link.dto.CustomLinkResponseDTO;
import com.xtremand.common.bom.Pagination;
import com.xtremand.custom.link.bom.CustomLinkType;
import com.xtremand.custom.link.dao.CustomLinkDao;
import com.xtremand.util.PaginationUtil;
import com.xtremand.util.XamplifyUtil;
import com.xtremand.util.XamplifyUtils;
import com.xtremand.util.dao.HibernateSQLQueryResultUtilDao;
import com.xtremand.util.dto.HibernateSQLQueryResultRequestDTO;
import com.xtremand.util.dto.QueryParameterDTO;
import com.xtremand.util.dto.QueryParameterListDTO;
import com.xtremand.util.dto.SortColumnDTO;
import com.xtremand.util.dto.XamplifyConstants;

@Repository
@Transactional
public class HibernateCustomLinkDao implements CustomLinkDao {

	@Autowired
	private HibernateSQLQueryResultUtilDao hibernateSQLQueryResultUtilDao;

	@Autowired
	private PaginationUtil paginationUtil;

	@Autowired
	private XamplifyUtil xamplifyUtil;

	@SuppressWarnings("unchecked")
	@Override
	public List<String> findAllTitlesAndExcludeTitleById(CustomLinkRequestDTO customLinkRequestDTO) {
		CustomLinkType customLinkType = customLinkRequestDTO.getCustomLinkType();
		Integer companyId = customLinkRequestDTO.getCompanyId();
		Integer id = customLinkRequestDTO.getId();
		if (customLinkType != null && XamplifyUtils.isValidInteger(companyId) && XamplifyUtils.isValidInteger(id)) {
			HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
			hibernateSQLQueryResultRequestDTO.setQueryString(
					"select lower(trim(title)) from xt_custom_link where company_id = :companyId and cast(custom_link_type  as text) = :type and id!=:id");
			hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
					.add(new QueryParameterDTO(XamplifyConstants.COMPANY_ID, companyId));
			hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
					.add(new QueryParameterDTO(XamplifyConstants.TYPE, customLinkType.name()));
			hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
					.add(new QueryParameterDTO(XamplifyConstants.ID, id));
			return (List<String>) hibernateSQLQueryResultUtilDao.returnList(hibernateSQLQueryResultRequestDTO);
		} else {
			return Collections.emptyList();
		}

	}

	@SuppressWarnings("unchecked")
	@Override
	public List<String> findAllTitles(CustomLinkRequestDTO customLinkRequestDTO) {
		CustomLinkType customLinkType = customLinkRequestDTO.getCustomLinkType();
		Integer companyId = customLinkRequestDTO.getCompanyId();
		if (customLinkType != null && XamplifyUtils.isValidInteger(companyId)) {
			HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
			hibernateSQLQueryResultRequestDTO.setQueryString(
					"select lower(trim(title)) from xt_custom_link where company_id = :companyId and  cast(custom_link_type  as text) = :type");
			hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
					.add(new QueryParameterDTO(XamplifyConstants.COMPANY_ID, customLinkRequestDTO.getCompanyId()));
			hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
					.add(new QueryParameterDTO("type", customLinkType.name()));

			return (List<String>) hibernateSQLQueryResultUtilDao.returnList(hibernateSQLQueryResultRequestDTO);
		} else {
			return Collections.emptyList();
		}

	}

	@Override
	public Map<String, Object> findAll(Pagination pagination, String searchKey, List<String> types) {
		String queryString = "select cl.id as \"id\",cl.title as \"title\", cl.link as \"link\",cl.icon as \"icon\", case when cl.description is null then '' else TRIM(cl.description) end as \"description\", cl.banner_image_path as \"bannerImagePath\",  "
				+ " case when length(TRIM(concat(up.firstname,'',up.lastname)))>0 then TRIM(concat(up.firstname,' ',up.lastname)) else up.email_id end "
				+ " as \"createdBy\",cl.created_time as \"createdTime\", case when cast(cl.custom_link_type as text)= :newsType then 'News' when cast(cl.custom_link_type as text)= :announcementType then 'Announcements'"
				+ " when cast(cl.custom_link_type as text)= :dashboardBannerType then 'Dashboard Banners' end as \"type\",cl.open_link_in_new_tab as \"openLinkInNewTab\", cl.is_display_title as \"displayTitle\", cl.button_text as \"buttonText\"  from xt_custom_link cl,xt_user_profile up \r\n"
				+ " where up.user_id = cl.created_user_id and cl.company_id = :companyId and cast(cl.custom_link_type  as text) in  (:types) ";
		String sortQueryString = addSortColumns(pagination);
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		hibernateSQLQueryResultRequestDTO.setQueryString(queryString);
		QueryParameterDTO queryParameterDTO = new QueryParameterDTO(XamplifyConstants.COMPANY_ID,
				pagination.getCompanyId());
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(queryParameterDTO);
		addTypeParameters(hibernateSQLQueryResultRequestDTO);
		hibernateSQLQueryResultRequestDTO.getQueryParameterListDTOs()
				.add(new QueryParameterListDTO(XamplifyConstants.TYPES, types));
		hibernateSQLQueryResultRequestDTO.setSortQueryString(sortQueryString);
		hibernateSQLQueryResultRequestDTO.setClassInstance(CustomLinkResponseDTO.class);
		List<String> searchColumns = new ArrayList<>();
		searchColumns.add("up.email_id");
		searchColumns.add("up.firstname");
		searchColumns.add("up.lastname");
		searchColumns.add("cl.title");
		searchColumns.add("cl.link");
		searchColumns.add("cl.icon");
		hibernateSQLQueryResultRequestDTO.setSearchColumns(searchColumns);
		return hibernateSQLQueryResultUtilDao.returnPaginatedDTOList(hibernateSQLQueryResultRequestDTO, pagination,
				searchKey);

	}

	private void addTypeParameters(HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO) {
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
				.add(new QueryParameterDTO("newsType", CustomLinkType.NEWS.name()));
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
				.add(new QueryParameterDTO("announcementType", CustomLinkType.ANNOUNCEMENTS.name()));
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
				.add(new QueryParameterDTO("dashboardBannerType", CustomLinkType.DASHBOARD_BANNERS.name()));
	}

	private String addSortColumns(Pagination pagination) {
		List<SortColumnDTO> sortColumnDTOs = new ArrayList<>();
		SortColumnDTO idSortOption = new SortColumnDTO("id", "cl.id", true, false, false);
		SortColumnDTO createdTimeSortOption = new SortColumnDTO("createdTime", "cl.created_time", false, true, false);
		SortColumnDTO titleSortOption = new SortColumnDTO("title", "cl.title", false, true, false);
		sortColumnDTOs.add(createdTimeSortOption);
		sortColumnDTOs.add(titleSortOption);
		sortColumnDTOs.add(idSortOption);
		return paginationUtil.generateSortQuery(pagination, sortColumnDTOs, "desc");
	}

	@Override
	public CustomLinkResponseDTO findById(Integer id) {
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		String queryString = "select id as \"id\", title as \"title\", link as \"link\", case when cast(custom_link_type as text)= :newsType then 'News'"
				+ " when cast(custom_link_type as text)= :announcementType then 'Announcements' when cast(custom_link_type as text)= :dashboardBannerType"
				+ " then 'Dashboard Banners' end as \"type\",  icon as \"icon\", open_link_in_new_tab as \"openLinkInNewTab\", case when description is null then '' else TRIM(description) end as \"description\", "
				+ "banner_image_path as \"bannerImagePath\", is_display_title as \"displayTitle\", button_text as \"buttonText\"  from xt_custom_link where id = :id";
		hibernateSQLQueryResultRequestDTO.setQueryString(queryString);
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO("id", id));
		addTypeParameters(hibernateSQLQueryResultRequestDTO);
		return (CustomLinkResponseDTO) hibernateSQLQueryResultUtilDao.getDto(hibernateSQLQueryResultRequestDTO,
				CustomLinkResponseDTO.class);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Integer> findIdsByCompanyId(Integer companyId) {
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		hibernateSQLQueryResultRequestDTO.setQueryString("select id from xt_custom_link where company_id = :companyId");
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
				.add(new QueryParameterDTO(XamplifyConstants.COMPANY_ID, companyId));
		return (List<Integer>) hibernateSQLQueryResultUtilDao.returnList(hibernateSQLQueryResultRequestDTO);
	}

	@Override
	public void delete(Integer id) {
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		hibernateSQLQueryResultRequestDTO.setQueryString("delete from xt_custom_link where id = :id");
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO(XamplifyConstants.ID, id));
		hibernateSQLQueryResultUtilDao.update(hibernateSQLQueryResultRequestDTO);
	}

	@Override
	public boolean isMaximumDashboardBannersUploaded(Integer companyId) {
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		hibernateSQLQueryResultRequestDTO.setQueryString(
				"select case when count(*)=7 then true else false end from xt_custom_link where company_id = :companyId and cast(custom_link_type as text) = :customLinkType");
		setCompanyIdAndDashboardBannersQueryParameters(companyId, hibernateSQLQueryResultRequestDTO);
		return hibernateSQLQueryResultUtilDao.returnBoolean(hibernateSQLQueryResultRequestDTO);
	}

	private void setCompanyIdAndDashboardBannersQueryParameters(Integer companyId,
			HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO) {
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
				.add(new QueryParameterDTO(XamplifyConstants.COMPANY_ID, companyId));
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
				.add(new QueryParameterDTO("customLinkType", CustomLinkType.DASHBOARD_BANNERS.name()));
	}

	@Override
	public boolean isDefaultDashboardBannerExists(Integer companyId) {
		return isDefaultImageExists(companyId, xamplifyUtil.getDefaultDashboardBannerImagePath());
	}

	private boolean isDefaultImageExists(Integer companyId, String imagePath) {
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		hibernateSQLQueryResultRequestDTO.setQueryString(
				"select case when count(*)>0 then true else false end from xt_custom_link where company_id = :companyId and banner_image_path = :imagePath");
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
				.add(new QueryParameterDTO(XamplifyConstants.COMPANY_ID, companyId));
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO("imagePath", imagePath));
		return hibernateSQLQueryResultUtilDao.returnBoolean(hibernateSQLQueryResultRequestDTO);
	}

	@Override
	public boolean isDefaultHelpBannerExists(Integer companyId) {
		return isDefaultImageExists(companyId, xamplifyUtil.getDefaultHelpImagePath());
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Integer> findDashboardBannerIdsByCompanyId(Integer companyId) {
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		hibernateSQLQueryResultRequestDTO.setQueryString(
				"select id from xt_custom_link where company_id = :companyId and cast(custom_link_type as text) = :customLinkType");
		setCompanyIdAndDashboardBannersQueryParameters(companyId, hibernateSQLQueryResultRequestDTO);
		return (List<Integer>) hibernateSQLQueryResultUtilDao.returnList(hibernateSQLQueryResultRequestDTO);
	}

}
