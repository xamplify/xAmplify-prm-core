package com.xtremand.analytics.dao.hibernate;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.Hibernate;
import org.hibernate.SQLQuery;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.transform.Transformers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.xtremand.analytics.dao.VendorAnalyticsDAO;
import com.xtremand.common.bom.Pagination;
import com.xtremand.common.bom.Pagination.SORTINGORDER;
import com.xtremand.formbeans.UserDTO;
import com.xtremand.user.bom.Role;
import com.xtremand.user.bom.User;
import com.xtremand.user.bom.UserSource;
import com.xtremand.user.dao.UserDAO;
import com.xtremand.util.DateUtils;
import com.xtremand.util.XamplifyUtils;
import com.xtremand.vendor.bom.VendorDTO;

@Repository("vendorAnalyticsDAO")
@Transactional
public class HibernateVendorAnalyticsDAO implements VendorAnalyticsDAO {

	@Autowired
	private SessionFactory sessionFactory;

	@Value("${vendors.count}")
	private String vendorsCountQuery;

	@Value("${userDetailsByAlias}")
	private String userDetailsByAlias;

	@Autowired
	private UserDAO userDao;

	@Value("${server_path}")
	String serverPath;

	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Object> findAllVendors(Integer partnerId, Pagination pagination) {
		Session session = sessionFactory.getCurrentSession();
		Integer partnerCompanyId = userDao.getCompanyIdByUserId(partnerId);
		Integer loginAsUserId = pagination.getLoginAsUserId();
		boolean isLoginAsUser = XamplifyUtils.isLoginAsPartner(loginAsUserId);
		String sql = " select c.company_id as \"companyId\",c.company_name as \"companyName\",c.company_logo as \"companyLogo\",cast(count(distinct cp.id) as integer) as \"campaignsCount\" from "
				+ " xt_partnership p join xt_company_profile c on p.vendor_company_id = c.company_id "
				+ " left join xt_campaign_partner cp on c.company_id = cp.company_id  "
				+ " and cp.partner_company_id = p.partner_company_id and cp.email_template_id is not null and cp.company_id is not null where p.status = 'approved'"
				+ " and p.partner_company_id = :partnerCompanyId {{vendorCompanyIdFilterPlaceHolder}} group by c.company_id,c.company_name,c.company_logo";
		if (isLoginAsUser) {
			sql = sql.replace("{{vendorCompanyIdFilterPlaceHolder}}", " and p.vendor_company_id = :vendorCompanyId");
		} else {
			sql = sql.replace("{{vendorCompanyIdFilterPlaceHolder}}", "");
		}
		SQLQuery query = session.createSQLQuery(sql);
		query.setParameter("partnerCompanyId", partnerCompanyId);
		if (isLoginAsUser) {
			Integer loginAsUserCompanyId = userDao.getCompanyIdByUserId(loginAsUserId);
			query.setParameter("vendorCompanyId", loginAsUserCompanyId);
		}
		ScrollableResults scrollableResults = query.scroll();
		scrollableResults.last();
		Integer totalRecords = scrollableResults.getRowNumber() + 1;
		query.setFirstResult((pagination.getPageIndex() - 1) * pagination.getMaxResults());
		query.setMaxResults(pagination.getMaxResults());
		List<VendorDTO> list = query.setResultTransformer(Transformers.aliasToBean(VendorDTO.class)).list();
		Map<String, Object> resultMap = new HashMap<>();
		resultMap.put("totalRecords", totalRecords);
		resultMap.put("data", list);
		return resultMap;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Object[]> getDistributedCampaignsCount(List<Integer> companyIdsList, Integer partnerCompanyId) {
		Session session = sessionFactory.getCurrentSession();
		String sql = " select c.customer_id, count(distinct c.campaign_id) from xt_campaign c, xt_campaign_partner p  where c.customer_id in "
				+ " (select up.user_id from xt_user_profile up,xt_user_role ur where up.company_id in (:companyIds) and up.user_id=ur.user_id) "
				+ " and c.is_channel_campaign=true and c.is_launched=true  "
				+ " and p.campaign_id = c.campaign_id and p.company_id >0 and p.partner_company_id = "
				+ partnerCompanyId + " group by c.customer_id  ";
		SQLQuery query = session.createSQLQuery(sql);
		query.setParameterList("companyIds", companyIdsList);
		return query.list();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Object> listVendors(Pagination pagination) {
		Session session = sessionFactory.getCurrentSession();
		StringBuilder builder = new StringBuilder();
		builder.append(
				"SELECT distinct cp.company_name, ur.role_id, {up.*} FROM xt_user_role ur INNER JOIN xt_user_profile up ON "
						+ "ur.user_id = up.user_id INNER  join xt_company_profile cp on cp.company_id = up.company_id and (ur.role_id = "
						+ " OR ur.role_id = " + Role.PRM_ROLE.getRoleId() + ")");
		if (pagination.getSearchKey() != null && pagination.getSearchKey().length() > 0) {
			builder.append(" where lower(cp.company_name) like '%" + pagination.getSearchKey().toLowerCase()
					+ "%' or lower(up.email_id) like '%" + pagination.getSearchKey().toLowerCase()
					+ "%' or lower(up.firstname) like  '%" + pagination.getSearchKey().toLowerCase()
					+ "%' or lower(up.lastname) like  '%" + pagination.getSearchKey().toLowerCase() + "%'");
		}
		if (pagination.getSortcolumn() != null) {
			if (pagination.getSortcolumn().equalsIgnoreCase("companyName")) {
				if (SORTINGORDER.ASC == SORTINGORDER.valueOf(pagination.getSortingOrder())) {
					builder.append(" order by cp.company_name asc");
				} else if (SORTINGORDER.DESC == SORTINGORDER.valueOf(pagination.getSortingOrder())) {
					builder.append(" order by cp.company_name desc nulls last");

				}
			} else if (pagination.getSortcolumn().equalsIgnoreCase("dateLastLogin")) {
				if (SORTINGORDER.ASC == SORTINGORDER.valueOf(pagination.getSortingOrder())) {
					builder.append(" order by up.datelastlogin asc ");
				} else if (SORTINGORDER.DESC == SORTINGORDER.valueOf(pagination.getSortingOrder())) {
					builder.append(" order by up.datelastlogin desc ");

				}
			}
		}
		if (pagination.getSortcolumn() == null) {
			builder.append(" order by ur.role_id");
		}
		SQLQuery query = session.createSQLQuery(builder.toString()).addScalar("company_name").addScalar("role_id")
				.addEntity("up", User.class);
		ScrollableResults scrollableResults = query.scroll();
		scrollableResults.last();
		Integer totalRecords = scrollableResults.getRowNumber() + 1;
		query.setFirstResult((pagination.getPageIndex() - 1) * pagination.getMaxResults());
		query.setMaxResults(pagination.getMaxResults());
		List<Object[]> list = query.list();
		loadUserRoles(list);
		Map<String, Object> resultMap = new HashMap<>();
		resultMap.put("totalRecords", totalRecords);
		resultMap.put("data", list);
		return resultMap;
	}

	private void loadUserRoles(List<Object[]> list) {
		for (Object[] object : list) {
			User user = (User) object[2];
			Hibernate.initialize(user.getRoles());
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<UserDTO> listTop10Users(String columnName) {
		Session session = sessionFactory.getCurrentSession();
		String sql = "SELECT user_id, cp.company_id, cp.company_name, up.email_id, firstname, lastname, datereg, datelastnav "
				+ " FROM xt_user_profile up \r\n"
				+ "inner JOIN xt_company_profile cp ON up.company_id = cp.company_id\r\n"
				+ "WHERE up.company_id IS NOT NULL ORDER BY " + columnName + " DESC LIMIT 10";
		SQLQuery query = session.createSQLQuery(sql);
		List<Object[]> rows = query.list();
		List<UserDTO> list = new ArrayList<>();
		for (Object[] row : rows) {
			UserDTO userDTO = new UserDTO();
			userDTO.setId(Integer.parseInt(row[0].toString()));
			userDTO.setCompanyId(Integer.parseInt(row[1].toString()));
			userDTO.setCompanyName(row[2].toString());
			userDTO.setEmailId(row[3].toString());
			userDTO.setFirstName(row[4] != null ? String.valueOf(row[4]) : null);
			userDTO.setLastName(row[5] != null ? String.valueOf(row[5]) : null);
			userDTO.setDateReg(String.valueOf(row[6]));
			userDTO.setDateLastNav(String.valueOf(row[7]));

			list.add(userDTO);
		}
		return list;
	}

	@Override
	public Integer getVendorsCountByPartnerCompanyId(Integer partnerCompanyId) {
		Session session = sessionFactory.getCurrentSession();
		SQLQuery query = session.createSQLQuery(vendorsCountQuery + " and status = 'approved'");
		query.setParameter("partnerCompanyId", partnerCompanyId);
		return query.uniqueResult() != null ? ((BigInteger) query.uniqueResult()).intValue() : 0;
	}

	@SuppressWarnings("unchecked")
	@Override
	public UserDTO getUserDetailsByCompanyIdAndUserAlias(Integer companyId, String userAlias) {
		Session session = sessionFactory.getCurrentSession();
		SQLQuery query = session.createSQLQuery(userDetailsByAlias);
		query.setParameter("userAlias", userAlias);
		query.setParameter("companyId", companyId);
		List<Object[]> list = query.list();
		UserDTO userDto = new UserDTO();
		for (Object[] object : list) {
			userDto.setEmailId((String) object[0]);
			userDto.setFirstName((String) object[1]);
			userDto.setLastName((String) object[2]);
			userDto.setCompanyName((String) object[3]);
			userDto.setRoleName((String) object[4]);
			userDto.setUserStatus((String) object[5]);
			userDto.setDateReg(DateUtils.convertDateToString((Date) object[6]));
			userDto.setDateLastLogin(DateUtils.convertDateToString((Date) object[7]));
			userDto.setUpdatedTime(DateUtils.convertDateToString((Date) object[8]));
			userDto.setRoleId((Integer) object[9]);
			userDto.setId((Integer) object[10]);
			String source = (String) object[11];
			userDto.setAllBoundSource(UserSource.ALLBOUND.name().equals(source));
			String companyProfileName = (String) object[12];
			userDto.setCompanyProfileName(companyProfileName);
		}
		return userDto;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Object> getVendors(Pagination pagination) {
		Session session = sessionFactory.getCurrentSession();
		Integer partnerId = pagination.getPartnerId();
		Integer partnerCompanyId = userDao.getCompanyIdByUserId(partnerId);
		String vanityFilter = "";
		if (pagination.getVendorCompanyId() != null && pagination.getVendorCompanyId() > 0) {
			vanityFilter = " and p.vendor_company_id = " + pagination.getVendorCompanyId() + " ";
		}
		String sql = "with a as\r\n"
				+ "(select distinct c.company_id as \"companyId\",c.company_name as \"companyName\",\r\n"
				+ "c.company_logo as \"companyLogo\",\r\n"
				+ "cast(count(distinct cp.id) as integer) as \"campaignsCount\",\r\n"
				+ "vup.firstname as \"vendorFirstName\", vup.lastname as \"vendorLastName\", vup.email_id as \"vendorEmailId\",\r\n"
				+ "vup.profile_image as \"vendorProfileImage\", vup.mobile_number as \"vendorPhone\",\r\n"
				+ "pup.firstname as \"partnerFirstName\", pup.lastname as \"partnerLastName\", pup.email_id as \"partnerEmailId\",\r\n"
				+ "pup.profile_image as \"partnerProfileImage\", pup.mobile_number as \"partnerPhone\"\r\n"
				+ "from xt_partnership p join xt_company_profile c on p.vendor_company_id = c.company_id\r\n"
				+ "left join xt_user_profile xup1 on c.company_id=xup1.company_id\r\n"
				+ "left join xt_campaign xc on xc.customer_id=xup1.user_id\r\n"
				+ "left join xt_user_profile vup on p.vendor_id = vup.user_id\r\n"
				+ "left join xt_user_profile pup on p.partner_id = pup.user_id\r\n"
				+ "left join xt_campaign_partner cp on c.company_id = cp.company_id\r\n"
				+ " and cp.partner_company_id = p.partner_company_id and cp.campaign_id =xc.campaign_id and\r\n"
				+ "(cp.email_template_id is not null or xc.campaign_type in('EMAIL','VIDEO','SURVEY','EVENT', 'SOCIAL'))"
				+ " and cp.company_id is not null where p.status = 'approved'\r\n" + " and p.partner_company_id = "
				+ partnerCompanyId + "  " + vanityFilter + " group by c.company_id,c.company_name,c.company_logo,\r\n"
				+ "vup.firstname, vup.email_id, vup.lastname, vup.profile_image, vup.mobile_number, pup.firstname, pup.email_id,\r\n"
				+ "pup.lastname, pup.profile_image, pup.mobile_number),\r\n"
				+ "b as ( select distinct c.company_id as \"vendorCompanyId\",\r\n"
				+ "xup.firstname as \"vendorAdminFirstName\", xup.lastname as \"vendorAdminLastName\",\r\n"
				+ "xup.email_id as \"vendorAdminEmailId\", xup.profile_image as \"vendorAdminProfileImage\", xup.mobile_number as \"vendorAdminPhone\",\r\n"
				+ "xup1.firstname as \"partnerAdminFirstName\", xup1.lastname as \"partnerAdminLastName\",\r\n"
				+ "xup1.email_id as \"partnerAdminEmailId\", xup1.profile_image as \"partnerAdminProfileImage\", xup1.mobile_number as \"partnerAdminPhone\"\r\n"
				+ "from xt_partnership p join xt_company_profile c on p.vendor_company_id = c.company_id\r\n"
				+ "left join xt_user_profile xup on p.vendor_company_id=xup.company_id\r\n"
				+ "left join xt_company_profile xcp on xup.company_id=xcp.company_id\r\n"
				+ "left join xt_user_profile xup1 on p.partner_company_id=xup1.company_id\r\n"
				+ "left join xt_company_profile xcp1 on xup1.company_id=xcp1.company_id\r\n"
				+ "left join xt_user_role xur on xur.user_id=xup.user_id\r\n" + "where p.partner_company_id="
				+ partnerCompanyId + " " + vanityFilter + " and xup.email_id is not null\r\n"
				+ " and role_id in (2,3,12,13,20)\r\n"
				+ " and xup.user_id not in (select team_member_id from xt_team_member )\r\n"
				+ " and xup1.user_id not in (select team_member_id from xt_team_member) ),\r\n" + "c as\r\n"
				+ "(select distinct xlt.company_id as \"vendorCompanyIdC\" , c.company_name as \"companyNameC\" ,\r\n"
				+ "cast(count(distinct case when xlt.type = 'TRACK' and xlt.is_published = true then xltv.id end) as int) as \"tracksCount\",\r\n"
				+ "cast(count(distinct case when xlt.type = 'PLAYBOOK' and xlt.is_published = true then xltv.id end) as int) as \"playbooksCount\"\r\n"
				+ "from\r\n" + "xt_partnership p join xt_company_profile c on p.vendor_company_id = c.company_id\r\n"
				+ "left join xt_learning_track xlt on p.vendor_company_id=xlt.company_id\r\n"
				+ "left join xt_learning_track_visibility xltv on xltv.partnership_id =p.id and xltv.learning_track_id = xlt.id\r\n"
				+ "left join xt_user_profile xup1 on xup1.user_id=xltv.user_id\r\n" + "where xup1.user_id=" + partnerId
				+ "\r\n" + " group by 1,2),\r\n" + "d as\r\n"
				+ " (select foo.\"vendorCompanyId\" as \"vendorCompanyIdA\",cast(sum(foo.\"assetsCount\") as int) as \"assetsCount\" from\r\n"
				+ " (select distinct  xp.vendor_company_id as \"vendorCompanyId\",xcp.company_name as \"companyName\","
				+ "count(distinct xdp.dam_id) as \"assetsCount\"\r\n" + "from\r\n" + "xt_dam_partner xdp\r\n"
				+ "left join xt_dam xd on xd.id=xdp.dam_id\r\n"
				+ "left join xt_dam_partner_mapping xdpm on xdpm.dam_partner_id=xdp.id\r\n"
				+ "left join xt_partnership xp on xp.id=xdp.partnership_id and xd.company_id =xp.vendor_company_id\r\n"
				+ "join xt_company_profile xcp on xcp.company_id=xp.vendor_company_id\r\n" + "where xdpm.partner_id="
				+ partnerId + "\r\n" + " group by 1,2\r\n" + " union \r\n"
				+ "SELECT distinct p.vendor_company_id as \"vendorCompanyId\" , c.company_name as \"companyName\",count(distinct dp.dam_id) as \"assetsCount\"\r\n"
				+ "FROM xt_dam_partner dp\r\n"
				+ " JOIN xt_dam d on dp.dam_id = d.id JOIN xt_partnership p on p.id = dp.partnership_id\r\n"
				+ " JOIN xt_company_profile c on p.vendor_company_id = c.company_id\r\n"
				+ " JOIN xt_dam_partner_group_mapping dpgm on dp.id = dpgm.dam_partner_id\r\n"
				+ "WHERE p.partner_company_id = " + partnerCompanyId + " " + vanityFilter
				+ " group by 1,2)foo group by 1),\r\n"
				+ " e as(select xlp.company_id as \"vendorCompanyIdP\",cast(count(distinct xlp.id) as integer)as \"pagesCount\"\r\n"
				+ "from xt_partnership p left join xt_partner_landing_page xpl\r\n"
				+ "on xpl.partner_ship_fkey_id =p.id\r\n"
				+ "left join xt_landing_page xlp on xlp.id=xpl.landing_page_fkey_id\r\n"
				+ "where p.partner_company_id= " + partnerCompanyId + " " + vanityFilter + " group by 1)\r\n"
				+ "select distinct a.*, b.* ,c.* ,d.* ,e.* from a left join b on a.\"companyId\"=\"vendorCompanyId\"\r\n"
				+ "left join c on a.\"companyId\"=c.\"vendorCompanyIdC\"\r\n"
				+ "left join d on a.\"companyId\"=d.\"vendorCompanyIdA\"\r\n"
				+ "left join e on a.\"companyId\"=e.\"vendorCompanyIdP\"";

		SQLQuery query = session.createSQLQuery(sql);
		ScrollableResults scrollableResults = query.scroll();
		scrollableResults.last();
		Integer totalRecords = scrollableResults.getRowNumber() + 1;
		query.setFirstResult((pagination.getPageIndex() - 1) * pagination.getMaxResults());
		query.setMaxResults(pagination.getMaxResults());
		List<VendorDTO> list = query.setResultTransformer(Transformers.aliasToBean(VendorDTO.class)).list();
		Map<String, Object> resultMap = new HashMap<>();
		resultMap.put("totalRecords", totalRecords);
		resultMap.put("data", list);
		return resultMap;
	}
}
