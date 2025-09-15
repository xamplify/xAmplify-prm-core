package com.xtremand.custom.css.dao.hibernate;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projection;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.sql.JoinType;
import org.hibernate.transform.Transformers;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.xtremand.campaign.exception.XamplifyDataAccessException;
import com.xtremand.common.bom.CompanyProfile;
import com.xtremand.custom.css.bom.CompanyThemeActive;
import com.xtremand.custom.css.bom.CustomModule;
import com.xtremand.custom.css.bom.CustomSkin;
import com.xtremand.custom.css.bom.Theme;
import com.xtremand.custom.css.bom.Theme.ThemeStatus;
import com.xtremand.custom.css.bom.ThemeProperties;
import com.xtremand.custom.css.dao.CustomSkinDao;
import com.xtremand.custom.css.dto.CompanyThemeActiveDTO;
import com.xtremand.custom.css.dto.CustomSkinDTO;
import com.xtremand.custom.css.dto.ThemeDTO;
import com.xtremand.custom.css.dto.ThemePropertiesDTO;
import com.xtremand.dao.util.GenericDAO;
import com.xtremand.user.dao.UserDAO;
import com.xtremand.util.XamplifyUtils;

@Repository
@Transactional
public class HibernateCustomSkinDao implements CustomSkinDao {

	@Autowired
	private SessionFactory sessionFactory;

	@Autowired
	private UserDAO userDao;

	@Autowired
	GenericDAO genericDAO;

	@Override
	public CustomSkin findByType(Integer companyId, String type) {
		try {
			if (companyId != null && companyId > 0) {
				Session session = sessionFactory.getCurrentSession();
				CompanyProfile companyProfile = genericDAO.get(CompanyProfile.class, companyId);
				return (CustomSkin) session.createCriteria(CustomSkin.class)
						.add(Restrictions.eq("companyProfile", companyProfile))
						.add(Restrictions.eq("moduleType", CustomModule.valueOf(type))).uniqueResult();
			} else {
				return null;
			}

		} catch (HibernateException | XamplifyDataAccessException e) {
			throw new XamplifyDataAccessException(e);
		} catch (Exception ex) {
			throw new XamplifyDataAccessException(ex);
		}

	}

	@SuppressWarnings("unchecked")
	@Override
	public List<CustomSkinDTO> findByCompanyId(Integer id) {
		try {
			String sqlQuery = "SELECT c.id as \"id\", c.company_id as \"companyId\", c.created_user_id as \"createdBy\", c.updated_user_id as \"updatedBy\","
					+ " c.created_time as \"createdDate\", c.updated_time as \"updatedDate\", cast(c.module_name  as text) as \"moduleTypeString\", c.text_color as \"textColor\", c.button_border_color as \"buttonBorderColor\", c.icon_color as \"iconColor\","
					+ " c.font_family as \"fontFamily\", c.text_content as \"textContent\", c.background_color as \"backgroundColor\", c.button_color as \"buttonColor\", c.button_value_color as \"buttonValueColor\","
					+ " c.is_default as \"defaultSkin\",c.is_show_footer as\"showFooter\",c.div_color as \"divBgColor\",c.header_text_color  as \"headerTextColor\" FROM xt_custom_skin c where c.company_id =:id";
			SQLQuery query = sessionFactory.getCurrentSession().createSQLQuery(sqlQuery);
			query.setParameter("id", id);
			return query.setResultTransformer(Transformers.aliasToBean(CustomSkinDTO.class)).list();

		} catch (HibernateException | XamplifyDataAccessException e) {
			throw new XamplifyDataAccessException(e);
		} catch (Exception ex) {
			throw new XamplifyDataAccessException(ex);
		}

	}

	@Override
	public CustomSkinDTO findById(Integer id) {
		try {
			String sqlQuery = "SELECT c.id as \"id\", c.company_id as \"companyId\", c.created_user_id as \"createdBy\", c.updated_user_id as \"updatedBy\","
					+ " c.created_time as \"createdDate\", c.updated_time as \"updatedDate\", cast(c.module_name  as text) as \"moduleTypeString\", c.text_color as \"textColor\", c.button_border_color as \"buttonBorderColor\", c.icon_color as \"iconColor\","
					+ " c.font_family as \"fontFamily\", c.text_content as \"textContent\", c.background_color as \"backgroundColor\", c.button_color as \"buttonColor\", c.button_value_color as \"buttonValueColor\","
					+ " c.is_default as \"defaultSkin\",c.is_show_footer as\"showFooter\",c.div_color as \"divBgColor\",c.header_text_color  as \"headerTextColor\" FROM xt_custom_skin c where c.id =:id";
			SQLQuery query = sessionFactory.getCurrentSession().createSQLQuery(sqlQuery);
			query.setParameter("id", id);
			return (CustomSkinDTO) query.setResultTransformer(Transformers.aliasToBean(CustomSkinDTO.class))
					.uniqueResult();
		} catch (HibernateException | XamplifyDataAccessException e) {
			throw new XamplifyDataAccessException(e);
		} catch (Exception ex) {
			throw new XamplifyDataAccessException(ex);
		}
	}

	@Override
	public boolean validateCompanyProfile(Integer companyId) {
		try {
			Session session = sessionFactory.getCurrentSession();
			String stringQuery = "select case when count(*)>0 then true else false end from xt_custom_skin where company_id = :id";

			SQLQuery query = session.createSQLQuery(stringQuery);
			query.setParameter("id", companyId);
			return (boolean) query.uniqueResult();
		} catch (HibernateException | XamplifyDataAccessException e) {
			throw new XamplifyDataAccessException(e);
		} catch (Exception ex) {
			throw new XamplifyDataAccessException(ex);
		}
	}

	@Override
	public int updateCustomSkin(CustomSkinDTO customSkinDTO1, Integer id) {
		Session session = sessionFactory.getCurrentSession();
		String stringQuery = "UPDATE CustomSkin" + " SET updatedUserId=:updatedUserId, "
				+ "	updatedTime=:updatedTime, moduleType= :moduleStatus, textColor=:textColor, buttonBorderColor=:buttonBorderColor, iconColor=:iconColor, iconBorderColor=:iconBorderColor, iconHoverColor=: iconHoverColor, "
				+ "	fontFamily=:fontFamily, textContent=:textContent, backgroundColor=:backgroundColor, buttonColor=:buttonColor, "
				+ "	buttonValueColor=:buttonValueColor, defaultSkin= :defaultSkin, "
				+ " showFooter=:showFooter, divBgColor =:divColor ,headerTextColor=:headerTextColor "
				+ "	WHERE id =:id";
		Query query = session.createQuery(stringQuery);
		query.setParameter("id", id);
		query.setParameter("updatedUserId", customSkinDTO1.getUpdatedBy());
		query.setParameter("updatedTime", new Date());
		query.setParameter("moduleStatus", CustomModule.valueOf(customSkinDTO1.getModuleTypeString()));
		query.setParameter("textColor", customSkinDTO1.getTextColor());
		query.setParameter("buttonBorderColor", customSkinDTO1.getButtonBorderColor());
		query.setParameter("iconColor", customSkinDTO1.getIconColor());
		query.setParameter("iconBorderColor", customSkinDTO1.getIconBorderColor());
		query.setParameter("iconHoverColor", customSkinDTO1.getIconHoverColor());
		query.setParameter("fontFamily", customSkinDTO1.getFontFamily());
		String textContent = customSkinDTO1.getTextContent();
		if (StringUtils.hasText(textContent)) {
			if (textContent.length() > 225) {
				query.setParameter("textContent", textContent.substring(0, textContent.length() - 1));
			} else {
				query.setParameter("textContent", textContent);
			}
		}

		query.setParameter("backgroundColor", customSkinDTO1.getBackgroundColor());
		query.setParameter("buttonColor", customSkinDTO1.getButtonColor());
		query.setParameter("buttonValueColor", customSkinDTO1.getButtonValueColor());
		query.setParameter("defaultSkin", customSkinDTO1.isDefaultSkin());
		query.setParameter("showFooter", customSkinDTO1.isShowFooter());
		query.setParameter("divColor", customSkinDTO1.getDivBgColor());
		query.setParameter("headerTextColor", customSkinDTO1.getHeaderTextColor());
		return query.executeUpdate();
	}

	@Override
	public int updateThemeProperties(ThemePropertiesDTO customSkinDTO1, Integer Id) {
		Session session = sessionFactory.getCurrentSession();
		String stringQuery = "UPDATE ThemeProperties" + "	SET updatedUserId=:updatedUserId, "
				+ "	updatedTime=:updatedDate, moduleType= :moduleStatus, textColor=:textColor, buttonBorderColor=:buttonBorderColor, iconColor=:iconColor, iconBorderColor=:iconBorderColor, iconHoverColor=:iconHoverColor,"
				+ " textContent=:textContent, backgroundColor=:backgroundColor,tableHeaderColor=:tableHeaderColor,tableBodyColor=:tableBodyColor, buttonColor=:buttonColor, "
				+ " showFooter=:showFooter, divBgColor =:divColor ,buttonValueColor=:buttonValueColor,buttonPrimaryBorderColor=:buttonPrimaryBorderColor,"
				+ " buttonSecondaryColor=:buttonSecondaryBackgroundColor,buttonSecondaryBorderColor=:buttonSecondaryBorderColor,"
				+ "	buttonSecondaryTextColor=:buttonSecondaryTextColor,gradiantColorOne=:gradiantColorOne,gradiantColorTwo=:gradiantColorTwo,  theme.id =:themeId "
				+ "	WHERE id =:id";
		Query query = session.createQuery(stringQuery);
		query.setParameter("id", Id);
		query.setParameter("updatedUserId", customSkinDTO1.getUpdatedBy());
		query.setParameter("updatedDate", new Date());
		query.setParameter("moduleStatus", CustomModule.valueOf(customSkinDTO1.getModuleTypeString()));
		query.setParameter("textColor", customSkinDTO1.getTextColor());
		query.setParameter("buttonBorderColor", customSkinDTO1.getButtonBorderColor());
		query.setParameter("iconColor", customSkinDTO1.getIconColor());
		query.setParameter("iconBorderColor", customSkinDTO1.getIconBorderColor());
		query.setParameter("iconHoverColor", customSkinDTO1.getIconHoverColor());
		query.setParameter("textContent", customSkinDTO1.getTextContent());
		query.setParameter("backgroundColor", customSkinDTO1.getBackgroundColor());
		query.setParameter("tableHeaderColor", customSkinDTO1.getTableHeaderColor());
		query.setParameter("tableBodyColor", customSkinDTO1.getTableBodyColor());
		query.setParameter("buttonColor", customSkinDTO1.getButtonColor());
		query.setParameter("buttonValueColor", customSkinDTO1.getButtonValueColor());
		query.setParameter("buttonPrimaryBorderColor", customSkinDTO1.getButtonPrimaryBorderColor());
		query.setParameter("buttonSecondaryBackgroundColor", customSkinDTO1.getButtonSecondaryColor());
		query.setParameter("buttonSecondaryTextColor", customSkinDTO1.getButtonSecondaryTextColor());
		query.setParameter("buttonSecondaryBorderColor", customSkinDTO1.getButtonSecondaryBorderColor());
		query.setParameter("divColor", customSkinDTO1.getDivBgColor());
		query.setParameter("themeId", customSkinDTO1.getThemeId());
		String textContent = customSkinDTO1.getTextContent();
		query.setParameter("textContent", textContent);
		query.setParameter("showFooter", customSkinDTO1.isShowFooter());
		query.setParameter("gradiantColorTwo", customSkinDTO1.getGradiantColorTwo());
		query.setParameter("gradiantColorOne", customSkinDTO1.getGradiantColorOne());
		return query.executeUpdate();
	}

	@Override
	public CustomSkinDTO getDefaultSkin(Integer companyId, String module, boolean isBoolean) {

		try {
			Session session = sessionFactory.getCurrentSession();
			CustomSkinDTO customSkinDTO = new CustomSkinDTO();
			// CompanyProfile companyName = userDao.getCompanyNameByCompanyId(companyId);
			// CompanyProfile companyProfile =
			// userDao.getCompanyProfileByCompanyName(companyName.getCompanyName());
			CompanyProfile companyProfile = genericDAO.get(CompanyProfile.class, companyId);
			CustomSkin customSkin = (CustomSkin) session.createCriteria(CustomSkin.class)
					.add(Restrictions.eq("companyProfile", companyProfile))
					.add(Restrictions.eq("moduleType", CustomModule.valueOf(module)))
					.add(Restrictions.eq("defaultSkin", isBoolean)).uniqueResult();
			customSkinDTO.setModuleTypeString(customSkin.getModuleType().name());
			BeanUtils.copyProperties(customSkin, customSkinDTO);

			return customSkinDTO;

		} catch (HibernateException | XamplifyDataAccessException e) {
			throw new XamplifyDataAccessException(e);
		} catch (Exception ex) {
			throw new XamplifyDataAccessException(ex);
		}

	}

	@Override
	public CustomSkinDTO updateDarkTheme(boolean isDarkTheme, boolean isDefault, Integer comapnyId) {
		List<CustomSkinDTO> listDtos = findByCompanyId(comapnyId);
		CustomSkinDTO darkCustomskin1 = new CustomSkinDTO();
		for (CustomSkinDTO darkCustomskin : listDtos) {
			darkCustomskin.setDarkTheme(isDarkTheme);
			darkCustomskin.setDefaultSkin(isDefault);
			darkCustomskin1 = darkCustomskin;
			updateCustomSkin(darkCustomskin, darkCustomskin.getId());
		}
		return darkCustomskin1;
	}

	@Override
	public CustomSkinDTO updateCustomTheme(boolean isDarkTheme, Integer comapnyId) {
		List<CustomSkinDTO> listDtos = findByCompanyId(comapnyId);
		CustomSkinDTO darkCustomskin1 = new CustomSkinDTO();
		for (CustomSkinDTO darkCustomskin : listDtos) {
			darkCustomskin.setDarkTheme(isDarkTheme);
			darkCustomskin1 = darkCustomskin;
			updateCustomSkin(darkCustomskin, darkCustomskin.getId());
		}
		return darkCustomskin1;
	}

	@Override
	public CustomSkinDTO updateDefaultSettings(CustomSkinDTO customSkinDTO) {
		Integer companyId = userDao.getCompanyIdByUserId(customSkinDTO.getCreatedBy());
		CustomSkin customSkinExstingUser = findByType(companyId, customSkinDTO.getModuleTypeString());
		if (customSkinDTO.isDefaultSkin()) {
			CustomSkin defaultCustomSkin = findByType(1, customSkinDTO.getModuleTypeString());
			customSkinDTO = findById(defaultCustomSkin.getId());
		}
		customSkinDTO.setId(customSkinExstingUser.getId());
		updateCustomSkin(customSkinDTO, customSkinExstingUser.getId());
		return customSkinDTO;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<ThemeDTO> getThemesByCompanyIdOne() {
		Session session = sessionFactory.getCurrentSession();
		Criteria criteria = session.createCriteria(Theme.class);
		return (List<ThemeDTO>) criteria
				.setProjection(Projections.distinct(Projections.projectionList().add(Projections.property("id"), "id")
						.add(Projections.property("name"), "name").add(Projections.property("parentId"), "parentId")
						.add(Projections.property("backgroundImage"), "backgroundImagePath")
						.add(Projections.property("parentThemeName"), "parentThemeName")
						.add(Projections.property("description"), "description")
						.add(Projections.property("defaultTheme"), "defaultTheme")))
				.setResultTransformer(Transformers.aliasToBean(ThemeDTO.class))
				.add(Restrictions.eq("companyProfile.id", 1)).addOrder(Order.asc("id")).list();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<ThemeDTO> getThemesByCompanyId(Integer companyId) {
		Session session = sessionFactory.getCurrentSession();
		List<ThemeDTO> dtosList = new ArrayList<ThemeDTO>();
		Criteria criteria = session.createCriteria(Theme.class);
		List<ThemeDTO> dtoList = criteria
				.setProjection(Projections.distinct(Projections.projectionList().add(Projections.property("id"), "id")
						.add(Projections.property("name"), "name").add(Projections.property("parentId"), "parentId")
						.add(Projections.property("backgroundImage"), "backgroundImagePath")
						.add(Projections.property("parentThemeName"), "parentThemeName")
						.add(Projections.property("description"), "description")
						.add(Projections.property("createdTime"), "createdDate")
						.add(Projections.property("defaultTheme"), "defaultTheme")
						.add(Projections.property("themeImagePath"), "themeImagePath")
						.add(Projections.property("companyProfile.id"), "companyId")))
				.setResultTransformer(Transformers.aliasToBean(ThemeDTO.class))
				.add(Restrictions.eq("companyProfile.id", companyId)).list();
		for (ThemeDTO themeDTO : dtoList) {
			dtosList.add(themeDTO);
		}
		return dtosList;
	}

	@Override
	public boolean checkThemeIdExsitOrNot(Integer themeId) {
		try {
			Session session = sessionFactory.getCurrentSession();
			String stringQuery = "select case when count(*)>0 then true else false end from xt_theme where id = :id";

			SQLQuery query = session.createSQLQuery(stringQuery);
			query.setParameter("id", themeId);
			return (boolean) query.uniqueResult();
		} catch (HibernateException | XamplifyDataAccessException e) {
			throw new XamplifyDataAccessException(e);
		} catch (Exception ex) {
			throw new XamplifyDataAccessException(ex);
		}
	}

	@Override
	public Integer getThemeIdByThemeName(String themeName) {
		Session session = sessionFactory.getCurrentSession();
		Criteria criteria = session.createCriteria(Theme.class);
		criteria.add(Restrictions.eq("name", themeName));
		Projection projection = Projections.property("id");
		criteria.setProjection(projection);
		return (Integer) criteria.uniqueResult();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<ThemePropertiesDTO> getThemesPropertiesByThemeId(Integer themeId) {

		String sqlQuery = "SELECT  c.id as \"id\",c.created_user_id as \"createdBy\", c.updated_user_id as \"updatedBy\","
				+ " c.created_time as \"createdDate\", c.updated_time as \"updatedDate\", cast(c.module_name  as text) as \"moduleTypeString\", c.text_color as \"textColor\", c.border_color as \"buttonBorderColor\", c.icon_border_color as \"iconBorderColor\", c.icon_hover_color as \"iconHoverColor\", c.icon_color as \"iconColor\","
				+ " c.text_content as \"textContent\", c.background_color as \"backgroundColor\", c.button_color as \"buttonColor\", c.button_value_color as \"buttonValueColor\","
				+ " c.is_show_footer as\"showFooter\",c.div_color as \"divBgColor\",c.btn_primary_border_color as\"buttonPrimaryBorderColor\",c.btn_secondary_background_color as \"buttonSecondaryColor\","
				+ " c.btn_secondary_border_color as\"buttonSecondaryBorderColor\",c.btn_secondary_text_color as \"buttonSecondaryTextColor\",c.button_gradiant_colorone as \"gradiantColorOne\", c.button_gradiant_colortwo as \"gradiantColorTwo\" FROM xt_theme_properties c where c.theme_id =:id";
		SQLQuery query = sessionFactory.getCurrentSession().createSQLQuery(sqlQuery);
		query.setParameter("id", themeId);
		return query.setResultTransformer(Transformers.aliasToBean(ThemePropertiesDTO.class)).list();

	}

	@Override
	public ThemePropertiesDTO getThemesPropertiesByThemeIdAndModuleName(Integer id) {
		String sqlQuery = "SELECT  c.id as \"id\" , c.created_user_id as \"createdBy\", c.updated_user_id as \"updatedBy\","
				+ " c.created_time as \"createdDate\", c.updated_time as \"updatedDate\", cast(c.module_name  as text) as \"moduleTypeString\", c.text_color as \"textColor\",c.table_header_color as \"tableHeaderColor\",c.table_body_color as \"tableBodyColor\", c.border_color as \"buttonBorderColor\", c.icon_border_color as \"iconBorderColor\",c.icon_hover_color as \"iconHoverColor\", c.icon_color as \"iconColor\","
				+ " c.text_content as \"textContent\", c.background_color as \"backgroundColor\", c.button_color as \"buttonColor\", c.button_value_color as \"buttonValueColor\","
				+ " c.is_show_footer as\"showFooter\",c.div_color as \"divBgColor\",c.btn_primary_border_color as \"buttonPrimaryBorderColor\",c.btn_secondary_background_color as \"buttonSecondaryColor\",c.btn_secondary_border_color as \"buttonSecondaryBorderColor\",c.btn_secondary_text_color as \"buttonSecondaryTextColor\",c.button_gradiant_colorone as \"gradiantColorOne\", c.button_gradiant_colortwo as \"gradiantColorTwo\" FROM xt_theme_properties c where c.id =:id";
		SQLQuery query = sessionFactory.getCurrentSession().createSQLQuery(sqlQuery);
		query.setParameter("id", id);
		return (ThemePropertiesDTO) query.setResultTransformer(Transformers.aliasToBean(ThemePropertiesDTO.class))
				.uniqueResult();
	}

	@Override
	public Theme getThemeById(Integer id) {
		Session session = sessionFactory.getCurrentSession();
		Criteria criteria = session.createCriteria(Theme.class);
		criteria.add(Restrictions.eq("id", id));
		return (Theme) criteria.uniqueResult();
	}

	@Override
	public String deleteThemeById(Integer id) {
		Session session = sessionFactory.getCurrentSession();
		String message = null;
		Theme theme = getThemeById(id);
		if (theme.getId() != null) {
			if (theme.isDefaultTheme() == true) {
				message = "You Can't delete this theme,its default theme.";
			} else {
				String sql = "delete from xt_theme where id = " + id;
				session.createSQLQuery(sql).executeUpdate();
				message = id + " has been deleted successfully";
			}

			return message;
		} else {
			return null;
		}

	}

	@Override
	public ThemeProperties getThemesPropertiesByThemeIdAndModuleName(Integer themeId, String modulename) {
		try {
			if (themeId != null && themeId > 0) {
				Session session = sessionFactory.getCurrentSession();
				return (ThemeProperties) session.createCriteria(ThemeProperties.class)
						.add(Restrictions.eq("theme.id", themeId))
						.add(Restrictions.eq("moduleType", CustomModule.valueOf(modulename))).uniqueResult();
			} else {
				return null;
			}

		} catch (HibernateException | XamplifyDataAccessException e) {
			throw new XamplifyDataAccessException(e);
		} catch (Exception ex) {
			throw new XamplifyDataAccessException(ex);
		}

	}

	@Override
	public CompanyThemeActiveDTO getActivateTheme(Integer companyId) {
		CompanyThemeActiveDTO activeDto = new CompanyThemeActiveDTO();
		try {
			if (companyId != null && companyId > 0) {
				Session session = sessionFactory.getCurrentSession();
				Criteria criteria = session.createCriteria(CompanyThemeActive.class);
				activeDto = (CompanyThemeActiveDTO) criteria
						.setProjection(
								Projections.distinct(Projections.projectionList().add(Projections.property("id"), "id")
										.add(Projections.property("companyProfile.id"), "companyId")
										.add(Projections.property("theme.id"), "themeId")
										.add(Projections.property("createdTime"), "createdDate")
										.add(Projections.property("updatedTime"), "updatedDate")))
						.setResultTransformer(Transformers.aliasToBean(CompanyThemeActiveDTO.class))
						.add(Restrictions.eq("companyProfile.id", companyId)).uniqueResult();

				return activeDto;
			} else {
				return null;
			}
		} catch (HibernateException e) {
			throw new XamplifyDataAccessException(e);
		}
	}

	@Override
	public int updateCompanyActivateTheme(CompanyThemeActiveDTO activeDto, Integer id) {
		String stringQuery = "update CompanyThemeActive set theme.id = :themeId,updatedTime=:updatedTime  where id = :id";
		Theme themeId = getThemeById(activeDto.getThemeId());
		Session session = sessionFactory.getCurrentSession();
		Query query = session.createQuery(stringQuery);
		query.setParameter("id", id);
		query.setParameter("themeId", themeId.getId());
		query.setParameter("updatedTime", new Date());
		return query.executeUpdate();
	}

	@Override
	public ThemeDTO getThemeDTOById(Integer id) {
		if (id != null && id > 0) {
			Session session = sessionFactory.getCurrentSession();
			Criteria criteria = session.createCriteria(Theme.class);
			return (ThemeDTO) criteria
					.setProjection(Projections.distinct(Projections.projectionList()
							.add(Projections.property("id"), "id").add(Projections.property("name"), "name")
							.add(Projections.property("parentId"), "parentId")
							.add(Projections.property("backgroundImage"), "backgroundImagePath")
							.add(Projections.property("parentThemeName"), "parentThemeName")
							.add(Projections.property("description"), "description")
							.add(Projections.property("createdTime"), "createdDate")
							.add(Projections.property("defaultTheme"), "defaultTheme")
							.add(Projections.property("companyProfile.id"), "companyId")))
					.setResultTransformer(Transformers.aliasToBean(ThemeDTO.class)).add(Restrictions.eq("id", id))
					.uniqueResult();
		} else {
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<String> getThemeNames(Integer companyId) {
		Session session = sessionFactory.getCurrentSession();
		Criteria criteria = session.createCriteria(Theme.class);

		List<Integer> companyIds = new ArrayList<>();
		companyIds.add(companyId);
		companyIds.add(1);

		return criteria
				.setProjection(
						Projections.distinct(Projections.projectionList().add(Projections.property("name"), "name")))
				.add(Restrictions.or(Restrictions.in("companyProfile.id", companyIds))).list();
	}

	@Override
	public boolean checkActiveTheme(Integer themeId) {
		try {
			Session session = sessionFactory.getCurrentSession();
			String stringQuery = "select case when count(*)>0 then true else false end from xt_company_theme_active where theme_id = :id";

			SQLQuery query = session.createSQLQuery(stringQuery);
			query.setParameter("id", themeId);
			return (boolean) query.uniqueResult();
		} catch (HibernateException | XamplifyDataAccessException e) {
			throw new XamplifyDataAccessException(e);
		} catch (Exception ex) {
			throw new XamplifyDataAccessException(ex);
		}
	}

	@Override
	public boolean checkActiveThemeByCompanyId(Integer companyId) {
		try {
			Session session = sessionFactory.getCurrentSession();
			String stringQuery = "select case when count(*)>0 then true else false end from xt_company_theme_active where company_id = :companyId";
			SQLQuery query = session.createSQLQuery(stringQuery);
			query.setParameter("companyId", companyId);
			return (boolean) query.uniqueResult();
		} catch (HibernateException | XamplifyDataAccessException e) {
			throw new XamplifyDataAccessException(e);
		} catch (Exception ex) {
			throw new XamplifyDataAccessException(ex);
		}
	}

	@Override
	public Integer updateThemeById(ThemeDTO themeDto, Integer themeId) {
		String stringQuery = "update Theme set name = :name,updatedUserId=:updatedUserId,updatedTime=:updatedDate,backgroundImage=:backgroundImage where id = :id";

		Session session = sessionFactory.getCurrentSession();
		Query query = session.createQuery(stringQuery);
		query.setParameter("id", themeId);
		query.setParameter("name", themeDto.getName());
		query.setParameter("updatedUserId", themeDto.getCreatedBy());
		query.setParameter("updatedDate", new Date());
		query.setParameter("backgroundImage", themeDto.getBackgroundImagePath());
		return query.executeUpdate();
	}

	@Override
	public boolean checkDuplicateName(String name, Integer companyId) {
		try {
			Session session = sessionFactory.getCurrentSession();
			String stringQuery = "select case when count(*)>0 then true else false end from xt_theme where lower(TRIM(name)) = :name and company_id =:companyId";

			SQLQuery query = session.createSQLQuery(stringQuery);
			query.setParameter("name", name.trim().toLowerCase());
			query.setParameter("companyId", companyId);
			return (boolean) query.uniqueResult();
		} catch (HibernateException | XamplifyDataAccessException e) {
			throw new XamplifyDataAccessException(e);
		} catch (Exception ex) {
			throw new XamplifyDataAccessException(ex);
		}
	}

	@Override
	public boolean checkNameForUpdate(String name, Integer companyId) {
		try {
			Session session = sessionFactory.getCurrentSession();
			String stringQuery = "select case when count(*)>1 then true else false end from xt_theme where name = :name and company_id =:companyId";

			SQLQuery query = session.createSQLQuery(stringQuery);
			query.setParameter("name", name);
			query.setParameter("companyId", companyId);
			return (boolean) query.uniqueResult();
		} catch (HibernateException | XamplifyDataAccessException e) {
			throw new XamplifyDataAccessException(e);
		} catch (Exception ex) {
			throw new XamplifyDataAccessException(ex);
		}

	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Integer> getCompanyIdsInCustomSkinTable() {
		try {
			Session session = sessionFactory.getCurrentSession();
			String stringQuery = "select distinct company_id from xt_custom_skin where company_id != 1 and updated_user_id != 1";
			SQLQuery query = session.createSQLQuery(stringQuery);
			return query.list();
		} catch (HibernateException | XamplifyDataAccessException e) {
			throw new XamplifyDataAccessException(e);
		} catch (Exception ex) {
			throw new XamplifyDataAccessException(ex);
		}
	}

	@Override
	public ThemeProperties getThemePropertyByThemeIdAndModule(Integer themeId, CustomModule moduleType) {
		if (themeId != null && themeId > 0) {
			Session session = sessionFactory.getCurrentSession();
			Criteria criteria = session.createCriteria(ThemeProperties.class, "TP");
			criteria.createAlias("TP.theme", "T", JoinType.INNER_JOIN);
			criteria.add(Restrictions.eq("T.id", themeId));
			criteria.add(Restrictions.eq("moduleType", moduleType));
			return (ThemeProperties) criteria.uniqueResult();
		} else {
			return null;
		}
	}

	/** XNFR-420 **/
	@Override
	public String getEnumType(Integer parentId) {
		String sqlQuery = "SELECT cast(parent_theme_name as text) from xt_theme where id = :id";
		SQLQuery query = sessionFactory.getCurrentSession().createSQLQuery(sqlQuery);
		query.setParameter("id", parentId);
		return (String) query.uniqueResult();
	}

	@Override
	public String getDefaultImagePath(String parentTheme, Integer themeId) {
		String sqlQuery = "select background_image from xt_theme where parent_theme_name = '" + parentTheme
				+ "' and id = " + themeId;
		SQLQuery query = sessionFactory.getCurrentSession().createSQLQuery(sqlQuery);
		String path = (String) query.uniqueResult();
		if (path == null || "".equals(path)) {
			sqlQuery = "select background_image from xt_theme where parent_theme_name = '" + parentTheme
					+ "' and created_user_id = 1 ";
			query = sessionFactory.getCurrentSession().createSQLQuery(sqlQuery);
			path = (String) query.uniqueResult();
		}
		return path;
	}

	@Override
	public Integer updateDefaultThemeImages(ThemeDTO themeDto) {
		String stringQuery = "update Theme set updatedTime=:updatedDate,backgroundImage=:backgroundImage where parentThemeName = '"
				+ themeDto.getParentThemeName().name() + "' and defaultTheme = true ";
		Session session = sessionFactory.getCurrentSession();
		Query query = session.createQuery(stringQuery);
		query.setParameter("updatedDate", new Date());
		query.setParameter("backgroundImage", themeDto.getBackgroundImagePath());
		return query.executeUpdate();
	}

	/**** 10-09-2024 ****/
	@Override
	public ThemeDTO fetchCompanyActiveTheme(Integer companyId) {
		try {
			String sql = "SELECT COALESCE((SELECT theme_id FROM xt_company_theme_active WHERE company_id = :companyId), 1) AS theme_id ";
			Session session = sessionFactory.getCurrentSession();
			SQLQuery query = session.createSQLQuery(sql);
			query.setParameter("companyId", companyId);
			Integer themeId = (Integer) query.uniqueResult();
			return findThemeDtoById(themeId, session);
		} catch (HibernateException | XamplifyDataAccessException e) {
			throw new XamplifyDataAccessException(e);
		} catch (Exception ex) {
			throw new XamplifyDataAccessException(ex);
		}
	}

	private ThemeDTO findThemeDtoById(Integer themeId, Session session) {
		if (XamplifyUtils.isValidInteger(themeId)) {
			Criteria criteria = session.createCriteria(Theme.class);
			criteria.setProjection(
					Projections.distinct(Projections.projectionList().add(Projections.property("id"), "id")
							.add(Projections.property("name"), "name").add(Projections.property("parentId"), "parentId")
							.add(Projections.property("backgroundImage"), "backgroundImagePath")
							.add(Projections.property("parentThemeName"), "parentThemeName")
							.add(Projections.property("description"), "description")
							.add(Projections.property("createdTime"), "createdDate")
							.add(Projections.property("defaultTheme"), "defaultTheme")
							.add(Projections.property("companyProfile.id"), "companyId")));
			criteria.add(Restrictions.eq("id", themeId));
			ThemeDTO themeDto = (ThemeDTO) criteria.setResultTransformer(Transformers.aliasToBean(ThemeDTO.class))
					.uniqueResult();
			// If backgroundImage is null, get it from another Theme based on
			// parentThemeName and created_user_id
			setDefaultBackgroundImage(themeDto, session);
			return themeDto;
		} else {
			return null;
		}
	}

	private void setDefaultBackgroundImage(ThemeDTO themeDto, Session session) {
		String parentThemeName = themeDto.getParentThemeName().name();
		boolean isGlassMorphismLight = Objects.equals(parentThemeName, ThemeStatus.GLASSMORPHISMLIGHT.name());
		boolean isGlassMorphismDark = Objects.equals(parentThemeName, ThemeStatus.GLASSMORPHISMDARK.name());
		String backgroundImagePath = themeDto.getBackgroundImagePath();
		boolean isBackgroundImagePath = backgroundImagePath == null || backgroundImagePath.trim().isEmpty();

		if (isBackgroundImagePath && (isGlassMorphismLight || isGlassMorphismDark)) {
			Criteria subCriteria = session.createCriteria(Theme.class);

			subCriteria.setProjection(Projections.property("backgroundImage"))
					.add(Restrictions.eq("parentThemeName", ThemeStatus.valueOf(parentThemeName)))
					.add(Restrictions.eq("createdUserId", 1)); // Use the proper variable or id for created_user_id

			String parentBackgroundImage = (String) subCriteria.uniqueResult();

			if (parentBackgroundImage != null) {
				themeDto.setBackgroundImagePath(parentBackgroundImage); // Update the background image in ThemeDTO
			}
		}
	}

}
