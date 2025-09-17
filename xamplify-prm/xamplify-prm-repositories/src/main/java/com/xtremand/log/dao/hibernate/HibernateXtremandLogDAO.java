package com.xtremand.log.dao.hibernate;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.transform.Transformers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.xtremand.campaign.dto.AutoResponseOption;
import com.xtremand.campaign.exception.XamplifyDataAccessException;
import com.xtremand.common.bom.Pagination;
import com.xtremand.dao.util.GenericDAO;
import com.xtremand.formbeans.EmailLogReport;
import com.xtremand.log.bom.EmailLogView;
import com.xtremand.log.bom.SMSLog;
import com.xtremand.log.bom.SMS_URLShortener;
import com.xtremand.log.bom.URLShortener;
import com.xtremand.log.bom.XtremandLog;
import com.xtremand.log.dao.XtremandLogDAO;
import com.xtremand.util.GenerateRandomPassword;
import com.xtremand.util.XamplifyUtils;
import com.xtremand.video.bom.VideoFile;

@Repository("xtremandLogDAO")
@Transactional
public class HibernateXtremandLogDAO implements XtremandLogDAO {
	private static final Logger logger = LoggerFactory.getLogger(HibernateXtremandLogDAO.class);

	@Resource
	SessionFactory sessionFactory;

	@Autowired
	GenericDAO genericDao;

	@Value("${show_shorten_url_log}")
	private boolean showShortenUrlLog;

	@SuppressWarnings("unchecked")
	@Override
	public List<Object[]> countrywiseUsersCount(Integer userId) {
		Session session = sessionFactory.getCurrentSession();
		String sql = "select count(distinct user_id) as count, country_code from xt_xtremand_log where campaign_id in (select campaign_id from xt_campaign where customer_id in "
				+ " (select user_id from xt_user_profile where company_id=(select company_id from xt_user_profile where user_id="
				+ userId + ")))  and country_code IS NOT NULL group by country_code " + " union "
				+ " select count(distinct ip_address) as count, country_code from xt_xtremand_log where user_id=0 and campaign_id=0 and video_id in(select id "
				+ " from xt_video_files where customer_id in (select user_id from xt_user_profile "
				+ " where company_id=(select company_id from xt_user_profile where user_id=" + userId
				+ "))) and country_code IS NOT NULL group by country_code";
		SQLQuery query = session.createSQLQuery(sql);
		return query.list();
	}

	@Override
	public List listEmailLogsByUserAndAction(List<Integer> userIdList, Integer actionId, Integer pageSize,
			Integer pageNumber) {
		Session session = sessionFactory.getCurrentSession();
		/*
		 * String sql =
		 * "SELECT el.email_id, el.firstname, el.lastname, c.campaign_name, el.time, el.subject "
		 * +" FROM v_xt_email_log el, xt_campaign c, xt_user_profile up "
		 * +" where el.campaign_id= c.campaign_id and c.customer_id in (:userIds) and el.action_id  ="
		 * +actionId+" and el.user_id=up.user_id order by el.time desc";
		 */

		String sql = "select  el.email_id, el.firstname, el.lastname, c.campaign_name, el.time,el.subject, cp.company_name from v_xt_email_log el "
				+ " inner join xt_campaign c on c.campaign_id = el.campaign_id inner join xt_user_profile up on up.user_id = el.user_id "
				+ " left join xt_company_profile cp on up.company_id = cp.company_id where c.customer_id in (:userIds) and el.action_id = "
				+ actionId + " order by el.time desc ";
		Query query = session.createSQLQuery(sql);
		query.setParameterList("userIds", userIdList);
		query.setFirstResult((pageNumber - 1) * pageSize);
		query.setMaxResults(pageSize);
		return query.list();
	}

	@Override
	public List listEmailGifClickedUrlClickedLogsByUser(List<Integer> userIdList, Integer pageSize,
			Integer pageNumber) {
		Session session = sessionFactory.getCurrentSession();
		String sql = "SELECT el.email_id, el.firstname, el.lastname, c.campaign_name, {el.*} "
				+ " FROM v_xt_email_log el, xt_campaign c, xt_user_profile up "
				+ " where el.campaign_id= c.campaign_id and c.customer_id in (:userIds) and el.action_id in (14,15) and el.user_id=up.user_id order by el.time desc";
		Query query = session.createSQLQuery(sql).addScalar("email_id").addScalar("firstname").addScalar("lastname")
				.addScalar("campaign_name").addEntity("el", EmailLogView.class);
		query.setParameterList("userIds", userIdList);
		query.setFirstResult((pageNumber - 1) * pageSize);
		query.setMaxResults(pageSize);
		return query.list();
	}

	@Override
	public Integer getEmailLogCountByUser(List<Integer> userIdList, Integer actionId) {
		logger.debug("entered in HibernateXtremandLogDAO getEmailLogCountByUser() mehtod");
		Session session = sessionFactory.getCurrentSession();
		Query query = session
				.createQuery("select count(*) from EmailLog e, User u where u.userId=e.userId and e.actionId="
						+ actionId + " and e.campaignId IN "
						+ "(select c.id from com.xtremand.campaign.bom.Campaign c where c.user.userId in (:userIds))");
		query.setParameterList("userIds", userIdList);
		Integer count = ((Long) query.uniqueResult()).intValue();
		return count;
	}

	@Override
	public List listWatchedUsersByUser(List<Integer> userIdList, Integer pageSize, Integer pageNumber) {
		logger.debug("entered in HibernateXtremandLogDAO listWatchedUsersByUser() mehtod");
		Session session = sessionFactory.getCurrentSession();
		String sql = "select a.* from ( "
				+ " (select  distinct el.session_id, el.firstname,el.lastname,el.email_id, min(el.start_time) as starttime,el.city,el.state,el.country,el.os, "
				+ " c.campaign_name, el.country_code from v_xt_xtremand_log el, xt_campaign c "
				+ " where  el.action_id=1 and el.campaign_id= c.campaign_id and c.customer_id in (:userIds) "
				+ " group by el.session_id,el.firstname,el.lastname,el.email_id,el.city,el.state,el.country,el.os, c.campaign_name, el.country_code) "
				+ " union all "
				+ " (select  distinct el.session_id, el.firstname,el.lastname,el.email_id, min(el.start_time) as starttime,el.city,el.state,el.country,el.os, "
				+ " c.campaign_name, el.country_code from v_xt_xtremand_log el, xt_campaign c "
				+ " where  el.action_id=10 and el.campaign_id= c.campaign_id and c.customer_id in (:userIds) "
				+ " group by el.session_id,el.firstname,el.lastname,el.email_id,el.city,el.state,el.country,el.os, c.campaign_name, el.country_code ) "
				+ " union all "
				+ " (SELECT distinct el.session_id, el.firstname,el.lastname,el.email_id, min(el.start_time) as starttime,el.city,el.state,el.country,el.os, null,  "
				+ " el.country_code FROM v_xt_xtremand_log el, xt_video_files v "
				+ " where el.action_id=1 and el.video_id=v.id and v.customer_id in (:userIds)  and  el.user_id=0 and el.campaign_id=0 "
				+ " group by el.session_id,el.firstname,el.lastname,el.email_id,el.city,el.state,el.country,el.os, el.country_code ) "
				+ " union all "
				+ " (SELECT distinct el.session_id, el.firstname,el.lastname,el.email_id, min(el.start_time) as starttime,el.city,el.state,el.country,el.os, null, "
				+ " el.country_code FROM v_xt_xtremand_log el, xt_video_files v "
				+ " where el.action_id=10 and el.video_id=v.id and v.customer_id in (:userIds)  and  el.user_id=0 and el.campaign_id=0 "
				+ " group by el.session_id,el.firstname,el.lastname,el.email_id,el.city,el.state,el.country,el.os, el.country_code ) "
				+ " ) a order by a.starttime desc";
		Query query = session.createSQLQuery(sql);
		query.setParameterList("userIds", userIdList);
		query.setFirstResult((pageNumber - 1) * pageSize);
		query.setMaxResults(pageSize);
		return query.list();
	}

	@Override
	public Integer getWatchedUsersCountByUser(List<Integer> userIdList) {
		logger.debug("entered in HibernateXtremandLogDAO getWatchedUsersCountByUser() mehtod");
		Session session = sessionFactory.getCurrentSession();
		String sql = "select  (a+b+c+d) as count " + " from (select "
				+ "  (select coalesce(COUNT(DISTINCT session_id),0) from xt_xtremand_log where action_id=1 and campaign_id in (select c.campaign_id from xt_campaign c where c.customer_id in (:userIds) )  ) as a, "
				+ " (select coalesce(SUM(CASE WHEN action_id = 10 THEN 1 ELSE 0 END),0) from xt_xtremand_log where campaign_id in (select c.campaign_id from xt_campaign c where c.customer_id in (:userIds) )   ) as b , "
				+ " (select coalesce(COUNT(DISTINCT session_id),0) from xt_xtremand_log where user_id=0 and campaign_id=0 and action_id=1 and video_id in (select id from xt_video_files where customer_id in (:userIds))) as c, "
				+ " (select coalesce(SUM(CASE WHEN action_id = 10 THEN 1 ELSE 0 END),0) from xt_xtremand_log where user_id=0 and campaign_id=0 and video_id in (select id from xt_video_files where customer_id in (:userIds))) as d "
				+ "  ) t";
		Query query = session.createSQLQuery(sql);
		query.setParameterList("userIds", userIdList);
		Integer count = query.uniqueResult() != null ? ((BigInteger) query.uniqueResult()).intValue() : 0;
		return count;
	}

	@Override
	public Integer getEmailLogCountByCampaign(Integer campaignId, String actionId) {
		Session session = sessionFactory.getCurrentSession();
		String sql = "select count(DISTINCT user_id) from xt_email_log where action_id IN " + actionId
				+ " and campaign_id=" + campaignId + " and reply_id is null and url_id is null";
		SQLQuery query = session.createSQLQuery(sql);
		Integer count = ((BigInteger) query.uniqueResult()).intValue();
		return count;
	}

	@Override
	public Integer getCampaignTotalViews(Integer campaignId, String date) {
		logger.debug("entered in HibernateXtremandLogDAO getCampaignTotalViews() mehtod");
		Session session = sessionFactory.getCurrentSession();
		String sql = "select count(distinct user_id)  from xt_xtremand_log where action_id=1 and campaign_id="
				+ campaignId + " and user_id in (select user_id from xt_campaign_user_userlist where campaign_id="
				+ campaignId + ") ";
		sql = date != null ? sql + " " + date : sql;
		SQLQuery query = session.createSQLQuery(sql);
		Integer count = ((BigInteger) query.uniqueResult()).intValue();
		return count;
	}

	@Override
	public Integer getCampaignTotalViewsCount(Integer campaignId) {
		logger.debug("entered in HibernateXtremandLogDAO getCampaignTotalViewsCount() mehtod");
		Session session = sessionFactory.getCurrentSession();
		String sql = "select( (select   count(*) from xt_xtremand_log xlv where xlv.campaign_id= " + campaignId
				+ " and xlv.action_id =10) +"
				+ " (select   count(distinct  (xlv.session_id)) from xt_xtremand_log xlv where xlv.campaign_id= "
				+ campaignId + ") )   ";
		SQLQuery query = session.createSQLQuery(sql);
		Integer count = ((BigInteger) query.uniqueResult()).intValue();
		return count;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<XtremandLog> listXtremandLogsBySessionId(String sessionId) {
		try {
			Session session = sessionFactory.getCurrentSession();
			Criteria criteria = session.createCriteria(XtremandLog.class);
			criteria.add(Restrictions.eq("sessionId", sessionId));

			criteria.addOrder(Order.asc("id"));
			criteria.addOrder(Order.asc("startTime"));

			return criteria.list();
		} catch (Exception exception) {
			throw new XamplifyDataAccessException(exception.getMessage());
		}
	}

	@Override
	public Integer campaignWatchedUsersCount(Integer campaignId) {
		logger.debug("entered in HibernateXtremandLogDAO campaignWatchedUsersCount() mehtod");
		Session session = sessionFactory.getCurrentSession();
		Criteria criteria = session.createCriteria(XtremandLog.class);
		criteria.add(Restrictions.eq("campaignId", campaignId));
		criteria.add(Restrictions.eq("actionId", 1));
		criteria.setProjection(Projections.countDistinct("userId"));
		int rowCount = ((Long) criteria.uniqueResult()).intValue();
		return rowCount;
	}

	@Override
	public Integer getUsersWatchedCountByUser(Integer userId, Integer actionId) {
		logger.debug("entered in HibernateXtremandLogDAO getUsersWatchedCountByUser() mehtod");
		Session session = sessionFactory.getCurrentSession();
		String hql = "select count(distinct userId) from XtremandLog where actionId=" + actionId + " and campaignId IN "
				+ "(select c.id from com.xtremand.campaign.bom.Campaign c where c.user.userId=" + userId + ")";
		Query query = session.createQuery(hql);
		Integer count = ((Long) query.uniqueResult()).intValue();
		return count;
	}

	@Override
	public Object getCountryWiseCampaignViews(Integer campaignId) {
		Session session = sessionFactory.getCurrentSession();

		/*
		 * 
		 * --Equivalent SQL Query
		 * 
		 * SELECT country_code, COUNT(DISTINCT session_id) AS count, SUM(CASE WHEN
		 * action_id = 10 THEN 1 ELSE 0 END) AS replay_count FROM xt_xtremand_log WHERE
		 * campaign_id= 686 AND country_code IS NOT NULL GROUP BY country_code
		 * 
		 */

		String sqlQuery = "SELECT countryCode, COUNT(DISTINCT sessionId) AS count,"
				+ "SUM(CASE WHEN actionId = 10 THEN 1 ELSE 0 END) AS replay_count FROM XtremandLog "
				+ "WHERE campaignId=" + campaignId + " AND country_code IS NOT NULL " + " GROUP BY countryCode";

		Query query = session.createQuery(sqlQuery);
		return query.list();
	}

	@Override
	public Integer getEmailLogClickedUrlsCountByUserId(Integer userId, String url, Integer campaignId) {
		try {
			logger.debug("In getEmailLogClickedUrlsCountByUserId() DAO");
			Session session = sessionFactory.getCurrentSession();
			String updatedUrl = "'" + url + "'";
			return (Integer) session.createSQLQuery(
					"select cast(count(*) as int) from xt_email_log where clicked_url=" + updatedUrl + " and user_id="
							+ userId + " and campaign_id=" + campaignId + " and reply_id is null and url_id is null")
					.uniqueResult();
		} catch (HibernateException | XamplifyDataAccessException e) {
			logger.error("Error In getEmailLogClickedUrlsCountByUserId() DAO", e);
			throw new XamplifyDataAccessException(e.getMessage());
		} catch (Exception ex) {
			logger.error("Error In getEmailLogClickedUrlsCountByUserId() DAO", ex);
			throw new XamplifyDataAccessException(ex.getMessage());
		}
	}

	@Override
	public Integer getOpenedEmailsCount(Integer userId, Integer videoId, Integer campaignId) {
		try {
			logger.debug("In getOpenedEmailsCount() DAO");
			Session session = sessionFactory.getCurrentSession();
			return ((BigInteger) session.createSQLQuery("select count(*) from xt_email_log where video_id=" + videoId
					+ " and user_id=" + userId + " and campaign_id=" + campaignId + " and action_id=13").uniqueResult())
							.intValue();
		} catch (HibernateException | XamplifyDataAccessException e) {
			logger.error("Error In getOpenedEmailsCount() DAO", e);
			throw new XamplifyDataAccessException(e.getMessage());
		} catch (Exception ex) {
			logger.error("Error In getOpenedEmailsCount() DAO", ex);
			throw new XamplifyDataAccessException(ex.getMessage());
		}
	}

	@Override
	public Date getOpenedEmailUserByMinTime(Integer userId, Integer campaignId) {
		try {
			logger.debug("In getOpenedEmailUserByMinTime() DAO::User Id=" + userId + ",Campaing Id=" + campaignId);
			Session session = sessionFactory.getCurrentSession();
			return (Date) session.createSQLQuery(
					"select  min(time) as time from xt_email_log where user_id=:userId  and campaign_id=:campaignId and action_id=:actionId and(reply_id IS NULL and url_id IS NULL)")
					.setParameter("userId", userId).setParameter("campaignId", campaignId).setParameter("actionId", 13)
					.uniqueResult();

		} catch (HibernateException | XamplifyDataAccessException e) {
			logger.error("Error In getOpenedEmailUserByMinTime() DAO", e);
			throw new XamplifyDataAccessException(e.getMessage());
		} catch (Exception ex) {
			logger.error("Error In getOpenedEmailUserByMinTime() DAO", ex);
			throw new XamplifyDataAccessException(ex.getMessage());
		}
	}

	@Override
	public Date getMinTimeOfClickedUrl(Integer campaignId, Integer userId, String clickedUrl) {
		try {
			logger.debug("In getMinTimeOfClickedUrl() DAO::User Id=" + userId + ",Campaign Id=" + campaignId
					+ "Clicked Url" + clickedUrl);
			Session session = sessionFactory.getCurrentSession();
			return (Date) session.createSQLQuery(
					"select  min(time) as time from xt_email_log where user_id=:userId  and campaign_id=:campaignId and action_id=:actionId and clicked_url=:url")
					.setParameter("userId", userId).setParameter("campaignId", campaignId).setParameter("actionId", 15)
					.setParameter("url", clickedUrl).uniqueResult();

		} catch (HibernateException | XamplifyDataAccessException e) {
			logger.error("Error In getMinTimeOfClickedUrl() DAO", e);
			throw new XamplifyDataAccessException(e.getMessage());
		} catch (Exception ex) {
			logger.error("Error In getMinTimeOfClickedUrl() DAO", ex);
			throw new XamplifyDataAccessException(ex.getMessage());
		}
	}

	@Override
	public Integer getWatchedVideosCount(Integer userId, Integer videoId, Integer campaignId) {
		try {
			logger.debug("In getWatchedVideosCount() DAO");
			Session session = sessionFactory.getCurrentSession();
			return (Integer) session
					.createSQLQuery("select cast(count(*) as int) from xt_xtremand_log where video_id=" + videoId
							+ " and user_id=" + userId + " and campaign_id=" + campaignId + " and action_id=1")
					.uniqueResult();
		} catch (HibernateException | XamplifyDataAccessException e) {
			logger.error("Error In getWatchedVideosCount() DAO", e);
			throw new XamplifyDataAccessException(e.getMessage());
		} catch (Exception ex) {
			logger.error("Error In getWatchedVideosCount() DAO", ex);
			throw new XamplifyDataAccessException(ex.getMessage());
		}
	}

	@Override
	public List listUniqueXtremandLogsByCampaignAndUser(Integer userId, Integer campaignId) {
		Session session = sessionFactory.getCurrentSession();

		String sqlQuery = "SELECT * FROM ( SELECT session_id, action_id, start_time, city, state, country, country_code, isp, "
				+ "row_number() over ( partition by session_id order by start_time ) as rn FROM xt_xtremand_log "
				+ "WHERE campaign_id = " + campaignId + " AND user_id = " + userId + ") as rs " + "where rs.rn = 1";

		Query query = session.createSQLQuery(sqlQuery);
		return query.list();
	}

	@Override
	public List listReplayLogsByCampaignAndUser(Integer userId, Integer campaignId) {
		Session session = sessionFactory.getCurrentSession();
		String sqlQuery = "SELECT session_id, action_id, start_time, city, state, country, country_code, isp "
				+ " FROM xt_xtremand_log WHERE campaign_id =:campaignId AND user_id = :userId AND action_id = 10";

		Query query = session.createSQLQuery(sqlQuery).setParameter("campaignId", campaignId).setParameter("userId",
				userId);
		return query.list();
	}

	@Override
	public Integer getEmailsSentCount(Integer campaignId) {
		Session session = sessionFactory.getCurrentSession();
		String sqlQuery = "SELECT count(*) FROM (SELECT DISTINCT campaign_id, user_id FROM xt_campaign_user_userlist WHERE campaign_id = "
				+ campaignId + ") A";
		return ((BigInteger) session.createSQLQuery(sqlQuery).uniqueResult()).intValue();
	}

	@Override
	public URLShortener getURLShortenerByAlias(String alias) {
		Session session = sessionFactory.getCurrentSession();
		Criteria criteria = session.createCriteria(URLShortener.class);
		criteria.add(Restrictions.eq("alias", alias));
		return (URLShortener) criteria.uniqueResult();
	}

	@Override
	@SuppressWarnings("unchecked")
	public URLShortener getURLShortenerByURL(String url) {
		Session session = sessionFactory.getCurrentSession();
		Criteria criteria = session.createCriteria(URLShortener.class);
		criteria.add(Restrictions.eq("url", url));
		List<URLShortener> urlShorteners = criteria.list();
		if (urlShorteners != null && !urlShorteners.isEmpty()) {
			return urlShorteners.get(0);
		} else {
			return null;
		}
	}

	@Override
	public String getURLShortenerAliasByURL(String aliasUrl) {
		GenerateRandomPassword password = new GenerateRandomPassword();
		String alias = password.getPassword();
		URLShortener shortenUrlLog = new URLShortener(aliasUrl, alias);
		genericDao.save(shortenUrlLog);
		return alias;
	}

	@Override
	public List<Object[]> getDashboardHeatMapData(Integer userId, String limit) {
		Session session = sessionFactory.getCurrentSession();
		String modifiedSql = null;
		String sql = "select  distinct a.campaign_id, a.campaign_name, count( distinct b.user_id) as email_oepned_count, "
				+ " count(cu.user_id) as users, a.launch_time, "
				+ " CASE WHEN COUNT(DISTINCT cu.user_id) = 0 THEN 0.00 ELSE ROUND(COUNT(DISTINCT b.user_id)*100.0/COUNT(DISTINCT cu.user_id),2) END AS percentage, a.campaign_type, "
				+ " a.is_channel_campaign, a.is_nurture_campaign "
				+ " from xt_campaign a inner join xt_campaign_user_userlist cu on cu.campaign_id=a.campaign_id left outer join xt_email_log b on a.campaign_id=b.campaign_id  "
				+ " left outer join xt_user_profile p on a.customer_id=p.user_id where  p.company_id in (select company_id from xt_user_profile where user_id="
				+ userId + ") " + " and b.action_id=13 and ";

		if (limit.equalsIgnoreCase("7") || limit.equalsIgnoreCase("14") || limit.equalsIgnoreCase("21")
				|| limit.equalsIgnoreCase("30")) {
			modifiedSql = sql
					+ " to_date(to_char(a.launch_time, 'YYYY/MM/DD'), 'YYYY/MM/DD') > (CURRENT_DATE - INTERVAL '"
					+ limit + " days') and a.launch_time is not null group by 1,2,5 ";
		} else if (limit.equalsIgnoreCase("quarter") || limit.equalsIgnoreCase("year")) {
			modifiedSql = sql + " extract(" + limit + " from a.launch_time)=extract(" + limit
					+ " from (current_date)) and a.launch_time is not null group by 1,2,5 ";
		}
		SQLQuery query = session.createSQLQuery(modifiedSql);
		return query.list();
	}

	@Override
	public List<Object[]> getCampaignEmailOpenedClickedCount(Integer userId, List<Integer> campaignIds) {
		Session session = sessionFactory.getCurrentSession();
		String sql = " select a.campaign_name, COUNT(DISTINCT case when action_id=13 then b.user_id end) as email_oepned_count, "
				+ " COUNT(DISTINCT case when action_id in(14,15) then b.user_id end) as clicked "
				+ " from xt_campaign a left outer join xt_email_log b on a.campaign_id=b.campaign_id "
				+ " inner join xt_user_profile p on a.customer_id=p.user_id  where  a.campaign_id in (:campaignIds) and "
				+ "  p.company_id in(select company_id from xt_user_profile where user_id=" + userId
				+ ")  group by a.campaign_name";
		SQLQuery query = session.createSQLQuery(sql);
		query.setParameterList("campaignIds", campaignIds);
		return query.list();
	}

	@Override
	public List<Object[]> getCampaignWatchedCount(Integer userId, List<Integer> campaignIds) {
		Session session = sessionFactory.getCurrentSession();
		String sql = " select a.campaign_name, count(distinct b.session_id)+  (SUM(CASE WHEN b.action_id = 10 THEN 1 ELSE 0 END)) from xt_campaign a left outer join xt_xtremand_log b on a.campaign_id=b.campaign_id  "
				+ " inner join xt_user_profile p on a.customer_id=p.user_id  where a.campaign_id in (:campaignIds) and  p.company_id "
				+ " in(select company_id from xt_user_profile where user_id=" + userId + ")  group by a.campaign_name";

		SQLQuery query = session.createSQLQuery(sql);
		query.setParameterList("campaignIds", campaignIds);
		return query.list();
	}

	@Override
	public List<Object[]> getDashboardViewsData(Integer userId, Integer daysInterval) {
		Session session = sessionFactory.getCurrentSession();
		String sql = "select (SELECT COUNT(DISTINCT  x.session_id )) +  (SUM(CASE WHEN x.action_id = 10 THEN 1 ELSE 0 END)) AS views, CAST(TRUNC(EXTRACT(DAY FROM   x.start_time )) AS INTEGER) AS  dy_start_time, "
				+ " to_date(to_char(x.start_time, 'YYYY/MM/DD'), 'YYYY/MM/DD') AS  tdy_start_time FROM  xt_xtremand_log x inner join xt_campaign c "
				+ " ON (x.campaign_id = c.campaign_id) inner join xt_user_profile p on (c.customer_id=p.user_id) WHERE ((  to_date(to_char(x.start_time,'YYYY-MM-DD'),'YYYY-MM-DD')  <= CURRENT_DATE) "
				+ " AND ( to_date(to_char(x.start_time,'YYYY-MM-DD'),'YYYY-MM-DD')  >= (CURRENT_DATE - INTERVAL '"
				+ daysInterval + " DAY'))) and p.company_id in(select company_id from xt_user_profile where user_id="
				+ userId + ") " + " GROUP BY 2, 3 order by 3 desc ";
		SQLQuery query = session.createSQLQuery(sql);
		return query.list();
	}

	@Override
	public List<Object[]> getDashboardMinutesWatchedData(Integer userId, Integer daysInterval) {
		Session session = sessionFactory.getCurrentSession();
		String sql = "SELECT round(cast((sum(extract(epoch from x.end_time)-extract(epoch from x.start_time))/60) as numeric),2) AS minutes_watched, "
				+ " CAST(TRUNC(EXTRACT(DAY FROM x.start_time)) AS INTEGER) AS dystart_time, to_date(to_char(x.start_time, 'YYYY/MM/DD'), 'YYYY/MM/DD') AS  tdy_start_time "
				+ " FROM xt_xtremand_log x  inner join xt_campaign c ON (x.campaign_id = c.campaign_id) inner join xt_user_profile p on (c.customer_id=p.user_id) "
				+ " WHERE (((to_date(to_char(x.start_time,'YYYY-MM-DD'),'YYYY-MM-DD') <= CURRENT_DATE) AND (to_date(to_char(x.start_time,'YYYY-MM-DD'),'YYYY-MM-DD') >= (CURRENT_DATE - INTERVAL '"
				+ daysInterval + " DAY'))) AND (x.action_id = 1)) "
				+ " and p.company_id in(select company_id from xt_user_profile where user_id=" + userId
				+ ") GROUP BY 2,  3 order by 3 desc ";
		SQLQuery query = session.createSQLQuery(sql);
		return query.list();
	}

	@Override
	public List<Object[]> getDashboardAverageDurationData(Integer userId, Integer daysInterval) {

		Session session = sessionFactory.getCurrentSession();
		String sql = "select round(cast((sum(extract(epoch from x.end_time)-extract(epoch from x.start_time))/60)/count(distinct x.video_id) as numeric),2) AS average, "
				+ " CAST(TRUNC(EXTRACT(DAY FROM   x.start_time )) AS INTEGER) AS  dy_start_time, to_date(to_char(x.start_time, 'YYYY/MM/DD'), 'YYYY/MM/DD') AS  tdy_start_time "
				+ " FROM  xt_xtremand_log x inner join xt_campaign c ON (x.campaign_id = c.campaign_id) inner join xt_user_profile p on (c.customer_id=p.user_id) "
				+ " WHERE (((to_date(to_char(x.start_time,'YYYY-MM-DD'),'YYYY-MM-DD') <= CURRENT_DATE) AND (to_date(to_char(x.start_time,'YYYY-MM-DD'),'YYYY-MM-DD') >= (CURRENT_DATE - INTERVAL '"
				+ daysInterval + " DAY'))) AND "
				+ " (x.action_id = 1)) and p.company_id in(select company_id from xt_user_profile where user_id="
				+ userId + ") GROUP BY 2,3 order by 3 desc ";
		SQLQuery query = session.createSQLQuery(sql);
		return query.list();
	}

	@Override
	public List<Object[]> listCampaignViewsDetialReport1(Integer userId, Integer daysInterval, Integer selectedDate) {
		Session session = sessionFactory.getCurrentSession();
		String sql = "SELECT x.video_id,vf.title,(SELECT COUNT(DISTINCT  x.session_id )) +  (SUM(CASE WHEN x.action_id = 10 THEN 1 ELSE 0 END)) AS views, CAST(TRUNC(EXTRACT(DAY FROM   x.start_time )) AS INTEGER) AS  dy_start_time, "
				+ " to_date(to_char(x.start_time, 'YYYY/MM/DD'), 'YYYY/MM/DD') AS  tdy_start_time FROM  xt_xtremand_log x inner join xt_campaign c ON (x.campaign_id = c.campaign_id) "
				+ " inner join xt_user_profile p on (c.customer_id=p.user_id) inner join xt_video_files vf on (x.video_id=vf.id) WHERE ((  to_date(to_char(x.start_time,'YYYY-MM-DD'),'YYYY-MM-DD')  <= CURRENT_DATE) "
				+ " AND ( to_date(to_char(x.start_time,'YYYY-MM-DD'),'YYYY-MM-DD') >= (CURRENT_DATE - INTERVAL '"
				+ daysInterval + " DAY'))) and CAST(TRUNC(EXTRACT(DAY FROM   x.start_time )) AS INTEGER)="
				+ selectedDate + " and p.company_id " + " in(select company_id from xt_user_profile where user_id="
				+ userId + ") GROUP BY x.video_id,vf.title, dy_start_time, tdy_start_time order by 3,4  desc ";
		SQLQuery query = session.createSQLQuery(sql);
		return query.list();
	}

	@Override
	public Map<String, Object> listCampaignViewsDetialReport2(Integer videoId, Integer daysInterval,
			Integer selectedDate, Pagination pagination) {
		Session session = sessionFactory.getCurrentSession();
		String sql = "SELECT x.video_id AS video_id,vf.title, x.email_id, x.firstname, x.lastname, MIN(x.start_time) AS start_time FROM  v_xt_xtremand_log x inner join xt_campaign c "
				+ " ON (x.campaign_id = c.campaign_id) inner join xt_user_profile p on (x.user_id=p.user_id) inner join xt_video_files vf on (x.video_id=vf.id) "
				+ " WHERE ((  to_date(to_char(x.start_time,'YYYY-MM-DD'),'YYYY-MM-DD')  <= CURRENT_DATE) AND ( to_date(to_char(x.start_time,'YYYY-MM-DD'),'YYYY-MM-DD')  >= (CURRENT_DATE - INTERVAL '"
				+ daysInterval + " DAY'))) and " + " CAST(TRUNC(EXTRACT(DAY FROM   x.start_time )) AS INTEGER)="
				+ selectedDate + " and x.video_id=" + videoId
				+ " GROUP BY x.email_id, x.video_id, x.session_id, vf.title, x.firstname, x.lastname union all"
				+ " SELECT x.video_id AS video_id,vf.title, x.email_id, x.firstname, x.lastname, MIN(x.start_time) AS start_time FROM  v_xt_xtremand_log x inner join xt_campaign c "
				+ " ON (x.campaign_id = c.campaign_id) inner join xt_user_profile p on (x.user_id=p.user_id) inner join xt_video_files vf on (x.video_id=vf.id) "
				+ " WHERE ((  to_date(to_char(x.start_time,'YYYY-MM-DD'),'YYYY-MM-DD')  <= CURRENT_DATE) AND ( to_date(to_char(x.start_time,'YYYY-MM-DD'),'YYYY-MM-DD')  >= (CURRENT_DATE - INTERVAL '"
				+ daysInterval + " DAY'))) and " + " CAST(TRUNC(EXTRACT(DAY FROM   x.start_time )) AS INTEGER)="
				+ selectedDate + " and x.video_id=" + videoId
				+ " and x.action_id=10 GROUP BY x.email_id, x.video_id, x.session_id, vf.title, x.firstname, x.lastname ";
		SQLQuery query = session.createSQLQuery(sql);
		ScrollableResults scrollableResults = query.scroll();
		scrollableResults.last();
		Integer totalRecords = scrollableResults.getRowNumber() + 1;
		query.setFirstResult((pagination.getPageIndex() - 1) * pagination.getMaxResults());
		query.setMaxResults(pagination.getMaxResults());
		List<Object[]> data = query.list();
		Map<String, Object> resultMap = new HashMap<String, Object>();
		resultMap.put("totalRecords", totalRecords);
		resultMap.put("data", data);
		return resultMap;
	}

	@Override
	public List<Object[]> listCampaignMinutesWatchedDetialReport1(Integer userId, Integer daysInterval,
			Integer selectedDate) {
		Session session = sessionFactory.getCurrentSession();
		String sql = "SELECT  x.video_id,vf.title, CAST(TRUNC(EXTRACT(DAY FROM   x.start_time )) AS INTEGER) AS  dy_start_time, to_date(to_char(x.start_time, 'YYYY/MM/DD'), 'YYYY/MM/DD') "
				+ " AS  tdy_start_time, round(cast((sum(extract(epoch from x.end_time)-extract(epoch from x.start_time))/60) as numeric),2) AS minutes_watched FROM  xt_xtremand_log x "
				+ " inner join xt_campaign c ON (x.campaign_id = c.campaign_id) inner join xt_user_profile p on (c.customer_id=p.user_id) inner join xt_video_files vf on (x.video_id=vf.id) "
				+ " WHERE (((to_date(to_char(x.start_time,'YYYY-MM-DD'),'YYYY-MM-DD') <= CURRENT_DATE) AND (to_date(to_char(x.start_time,'YYYY-MM-DD'),'YYYY-MM-DD') >= (CURRENT_DATE - INTERVAL '"
				+ daysInterval + " DAY'))) AND (x.action_id = 1)) and CAST(TRUNC(EXTRACT(DAY FROM x.start_time)) "
				+ " AS INTEGER)=" + selectedDate
				+ " and  p.company_id in(select company_id from xt_user_profile where user_id=" + userId
				+ ") group by x.video_id,CAST(TRUNC(EXTRACT(DAY FROM   x.start_time )) "
				+ " AS INTEGER),vf.title, tdy_start_time order by x.video_id desc ";
		SQLQuery query = session.createSQLQuery(sql);
		return query.list();
	}

	@Override
	public Map<String, Object> listCampaignMinutesWatchedDetialReport2(Pagination pagination, Integer videoId,
			Integer daysInterval, Integer selectedDate) {
		Session session = sessionFactory.getCurrentSession();
		String sql = "SELECT  x.video_id,vf.title,p.email_id, p.firstname, p.lastname, CAST(TRUNC(EXTRACT(DAY FROM   x.start_time )) AS INTEGER) AS  dy_start_time, MIN(x.start_time) AS start_time, "
				+ " round(cast((sum(extract(epoch from x.end_time)-extract(epoch from x.start_time))/60) as numeric),2) AS minutes_watched FROM  xt_xtremand_log x "
				+ " inner join xt_campaign c ON (x.campaign_id = c.campaign_id) inner join xt_user_profile p on (x.user_id=p.user_id) inner join xt_video_files vf "
				+ " on (x.video_id=vf.id) WHERE (((to_date(to_char(x.start_time,'YYYY-MM-DD'),'YYYY-MM-DD') <= CURRENT_DATE) AND (to_date(to_char(x.start_time,'YYYY-MM-DD'),'YYYY-MM-DD') >= (CURRENT_DATE - INTERVAL '"
				+ daysInterval + " DAY'))) AND (x.action_id = 1)) "
				+ " and CAST(TRUNC(EXTRACT(DAY FROM x.start_time)) AS INTEGER)=" + selectedDate + " and x.video_id="
				+ videoId + " group by x.video_id, CAST(TRUNC(EXTRACT(DAY FROM   x.start_time )) "
				+ " AS INTEGER),p.email_id,p.firstname, p.lastname, x.session_id,vf.title order by p.email_id desc ";
		SQLQuery query = session.createSQLQuery(sql);
		ScrollableResults scrollableResults = query.scroll();
		scrollableResults.last();
		Integer totalRecords = scrollableResults.getRowNumber() + 1;
		query.setFirstResult((pagination.getPageIndex() - 1) * pagination.getMaxResults());
		query.setMaxResults(pagination.getMaxResults());
		List<Object[]> data = query.list();
		Map<String, Object> resultMap = new HashMap<String, Object>();
		resultMap.put("totalRecords", totalRecords);
		resultMap.put("data", data);
		return resultMap;
	}

	@Override
	public List<Object[]> listTotalMunutesWatchedByTop10Users(Integer videoId) {
		Session session = sessionFactory.getCurrentSession();
		String sql = "select COALESCE(up.firstname, up.lastname,up.email_id) as name, up.email_id, round(cast((sum(extract(epoch from x.end_time)-extract(epoch from x.start_time))/60) as numeric),2) "
				+ " as minuteswatched from xt_xtremand_log x,xt_campaign ca ,xt_user_profile up where x.campaign_id=ca.campaign_id and up.user_id=x.user_id and x.video_id="
				+ videoId
				+ " group by name, up.email_id order by round(sum((extract(epoch from x.end_time)-extract(epoch from x.start_time))/60)) desc limit 10 ";
		SQLQuery query = session.createSQLQuery(sql);
		return query.list();
	}

	@Override
	public Integer totalVideoViewsCount(Integer videoId) {
		Session session = sessionFactory.getCurrentSession();
		String sql = "SELECT COUNT(DISTINCT x.session_id)+  (SUM(CASE WHEN x.action_id = 10 THEN 1 ELSE 0 END)) AS views from v_xt_xtremand_log x where video_id ="
				+ videoId;
		SQLQuery query = session.createSQLQuery(sql);
		Integer count = query.uniqueResult() != null ? ((BigInteger) query.uniqueResult()).intValue() : 0;
		return count;
	}

	@Override
	public List<Object[]> listCurrentMonthVideoViews(Integer videoId) {
		Session session = sessionFactory.getCurrentSession();
		String sql = "SELECT COUNT(DISTINCT x.session_id)+  (SUM(CASE WHEN x.action_id = 10 THEN 1 ELSE 0 END)) AS views, to_char(start_time, 'YYYY-MM-DD') as start_time FROM xt_xtremand_log x "
				+ " WHERE ((CAST(EXTRACT(MONTH FROM x.start_time) AS INTEGER) = CAST(EXTRACT(MONTH FROM CURRENT_DATE) AS INTEGER)) "
				+ " AND (CAST(EXTRACT(YEAR FROM CURRENT_DATE) AS INTEGER) = CAST(EXTRACT(YEAR FROM x.start_time) AS INTEGER)) AND (x.video_id = "
				+ videoId + ")) " + " GROUP BY  to_char(start_time, 'YYYY-MM-DD') ";
		SQLQuery query = session.createSQLQuery(sql);
		return query.list();
	}

	@Override
	public List<Object[]> listMonthWiseVideoViews(Integer videoId) {
		Session session = sessionFactory.getCurrentSession();
		String sql = " select views,starttime from "
				+ " (select count(distinct session_id)+  (SUM(CASE WHEN action_id = 10 THEN 1 ELSE 0 END)) as views, to_char(start_time,'Mon-yyyy') as starttime,"
				+ " max(start_time) ord from xt_xtremand_log where "
				+ " extract(month from start_time)>(EXTRACT(month FROM CURRENT_DATE)-12) and video_id=" + videoId
				+ " group by to_char(start_time,'Mon-yyyy') " + " )a " + " order by ord ";
		SQLQuery query = session.createSQLQuery(sql);
		return query.list();
	}

	@Override
	public List<Object[]> listQuarterlyVideoViews(Integer videoId) {
		Session session = sessionFactory.getCurrentSession();
		String sql = "select count(distinct session_id)+  (SUM(CASE WHEN action_id = 10 THEN 1 ELSE 0 END)) as views, 'Q'||to_char(start_time,'q-yyyy') as starttime from xt_xtremand_log where "
				+ " extract(quarter from start_time)>(EXTRACT(quarter FROM CURRENT_DATE)-4) and video_id=" + videoId
				+ " group by to_char(start_time,'q-yyyy') ";
		SQLQuery query = session.createSQLQuery(sql);
		return query.list();
	}

	@Override
	public List<Object[]> listYearlyVideoViews(Integer videoId) {
		Session session = sessionFactory.getCurrentSession();
		String sql = "select count(distinct session_id)+  (SUM(CASE WHEN action_id = 10 THEN 1 ELSE 0 END)) as views,to_char(start_time,'yyyy') as starttime from xt_xtremand_log where "
				+ " video_id=" + videoId
				+ " and to_char(start_time,'yyyy') is not null group by to_char(start_time,'yyyy') ";
		SQLQuery query = session.createSQLQuery(sql);
		return query.list();
	}

	@Override
	public List<Object[]> listVideoViewsMinutesWatchedByMonth(Integer videoId, String month) {
		Session session = sessionFactory.getCurrentSession();
		String sql = "select max(cuul.firstname) as fname, max(cuul.lastname) as lname,p.email_id, p.user_id as userId, count(distinct x.session_id)+  (SUM(CASE WHEN x.action_id = 10 THEN 1 ELSE 0 END)) as views, "
				+ " round(cast((sum(extract(epoch from x.end_time)-extract(epoch from x.start_time))/60) as numeric),2) AS minutes_watched "
				+ " FROM xt_xtremand_log x INNER JOIN xt_campaign c ON (x.campaign_id = c.campaign_id) INNER JOIN xt_video_files v ON (x.video_id = v.id) "
				+ " INNER JOIN xt_user_profile p ON (x.user_id = p.user_id) left join xt_campaign_user_userlist cuul on c.campaign_id=cuul.campaign_id and x.user_id=cuul.user_id WHERE  to_char(x.start_time,'Mon-yyyy')='"
				+ month + "' AND (x.video_id = " + videoId + ") "
				+ " GROUP BY p.email_id, userId order by count(distinct x.session_id) desc ";
		SQLQuery query = session.createSQLQuery(sql);
		return query.list();
	}

	@Override
	public List<Object[]> listVideoViewsMinutesWatchedByQuarter(Integer videoId, String quarter) {
		Session session = sessionFactory.getCurrentSession();
		String sql = "select max(cuul.firstname) as fname, max(cuul.lastname) as lname, p.email_id, p.user_id as userId, count(distinct x.session_id)+  (SUM(CASE WHEN x.action_id = 10 THEN 1 ELSE 0 END)) as views,  "
				+ " round(cast((sum(extract(epoch from x.end_time)-extract(epoch from x.start_time))/60) as numeric),2) AS minutes_watched  "
				+ " FROM xt_xtremand_log x INNER JOIN xt_campaign c ON (x.campaign_id = c.campaign_id) INNER JOIN xt_video_files v ON (x.video_id = v.id)  "
				+ " INNER JOIN xt_user_profile p ON (x.user_id = p.user_id) left join xt_campaign_user_userlist cuul on c.campaign_id=cuul.campaign_id and x.user_id=cuul.user_id WHERE  to_char(x.start_time,'q-yyyy')='"
				+ quarter + "' AND (x.video_id = " + videoId + ") GROUP BY  "
				+ " p.email_id, userId order by count(distinct x.session_id) desc";
		SQLQuery query = session.createSQLQuery(sql);
		return query.list();
	}

	@Override
	public List<Object[]> listTodayVideoViewsMinutesWatched(Integer videoId) {
		Session session = sessionFactory.getCurrentSession();
		String sql = "select max(cuul.firstname) as fname, max(cuul.lastname) as lname,p.email_id, p.user_id as userId, count(distinct x.session_id)+  (SUM(CASE WHEN x.action_id = 10 THEN 1 ELSE 0 END)) as views, "
				+ " round(cast((sum(extract(epoch from x.end_time)-extract(epoch from x.start_time))/60) as numeric),2) AS minutes_watched "
				+ " FROM xt_xtremand_log x INNER JOIN xt_campaign c ON (x.campaign_id = c.campaign_id) INNER JOIN xt_video_files v ON (x.video_id = v.id) "
				+ " INNER JOIN xt_user_profile p ON (x.user_id = p.user_id) left join xt_campaign_user_userlist cuul on c.campaign_id=cuul.campaign_id and x.user_id=cuul.user_id WHERE date(x.start_time) = current_date AND (x.video_id = "
				+ videoId + ") GROUP BY " + " p.email_id, userId order by count(distinct x.session_id) desc";
		SQLQuery query = session.createSQLQuery(sql);
		return query.list();
	}

	@Override
	public List<Object[]> listVideoViewsMinutesWatchedByYear(Integer videoId, String year) {
		Session session = sessionFactory.getCurrentSession();
		String sql = "select max(cuul.firstname) as fname, max(cuul.lastname) as lname,p.email_id,p.user_id as userId, count(distinct x.session_id)+  (SUM(CASE WHEN x.action_id = 10 THEN 1 ELSE 0 END)) as views, "
				+ " round(cast((sum(extract(epoch from x.end_time)-extract(epoch from x.start_time))/60) as numeric),2) AS minutes_watched "
				+ " FROM xt_xtremand_log x INNER JOIN xt_campaign c ON (x.campaign_id = c.campaign_id) INNER JOIN xt_video_files v ON (x.video_id = v.id) "
				+ " INNER JOIN xt_user_profile p ON (x.user_id = p.user_id) "
				+ " left join xt_campaign_user_userlist cuul on c.campaign_id=cuul.campaign_id and x.user_id=cuul.user_id WHERE extract(year from x.start_time)='"
				+ year + "' and (x.video_id = " + videoId + ") "
				+ " GROUP BY p.email_id,userId order by count(distinct x.session_id) desc";
		SQLQuery query = session.createSQLQuery(sql);
		return query.list();
	}

	@Override
	public List<Object[]> campaignBubbleChartData(Integer campaignId, String type) {
		Session session = sessionFactory.getCurrentSession();
		String sql = null;
		if (type.equalsIgnoreCase("VIDEO")) {
			sql = " select x.user_id, x.firstname, x.lastname, p.email_id ,\r\n"
					+ "sum(x.stop_duration - x.start_duration) AS minutes_watched from v_xt_xtremand_log x \r\n"
					+ "inner join xt_user_profile p on x.user_id=p.user_id where x.campaign_id=" + campaignId
					+ " and x.action_id=1 group by x.user_id, x.firstname, x.lastname, p.email_id order by 2 desc limit 10";
		} else if (type.equalsIgnoreCase("REGULAR")
				|| (type.equalsIgnoreCase("SOCIAL") || type.equalsIgnoreCase("SURVEY"))) {
			sql = "select e.user_id, e.firstname, e.lastname, p.email_id ,count(distinct (to_char(e.time, 'YYYY-MM-DD HH24:MI'))) as email_opened from v_xt_email_log e inner join xt_user_profile p on e.user_id=p.user_id\r\n"
					+ "where e.campaign_id=" + campaignId
					+ " and e.action_id=13 group by e.user_id, e.firstname, e.lastname, p.email_id order by  2 desc limit 10";
		}
		SQLQuery query = session.createSQLQuery(sql);
		return query.list();
	}

	@Override
	public List<Object[]> listCampaignLifeTimeViewsDetailReport(Integer campaignId, Pagination pagination) {
		Session session = sessionFactory.getCurrentSession();
		String searchSql = "";
		if (pagination.getSearchKey() != null) {
			searchSql = " and (Lower(u.firstname) like '%" + pagination.getSearchKey().toLowerCase()
					+ "%' or Lower(u.lastname) like '%" + pagination.getSearchKey().toLowerCase()
					+ "%' or Lower(xt.companyname) like " + " '%" + pagination.getSearchKey().toLowerCase()
					+ "%' or Lower(u.email_id) like '%" + pagination.getSearchKey().toLowerCase() + "%' ) ";
		}
		String sql = "select c.campaign_id, c.campaign_name, u.user_id, u.email_id, xt.city||', '||xt.state||', '||xt.country as location, min(xt.start_time) as played_time , xt.device_type as Device, xt.os as os, COUNT(distinct(case when  Action_Id <=9 THEN "
				+ "  Session_Id end)) +COUNT(distinct(case when  Action_Id =10 THEN session_id end)) as views_count, xt.companyname, u.firstname, u.lastname from v_xt_xtremand_log xt, xt_user_profile u, xt_campaign c where u.user_id=xt.user_id "
				+ "  and c.campaign_id=xt.campaign_id and  c.campaign_id=" + campaignId + searchSql
				+ " group by c.campaign_id, c.campaign_name, u.user_id, u.email_id, location, Device, os, xt.companyname";
		SQLQuery query = session.createSQLQuery(sql);
		query.setFirstResult((pagination.getPageIndex() - 1) * pagination.getMaxResults());
		query.setMaxResults(pagination.getMaxResults());
		return query.list();
	}

	@Override
	public List<Object[]> listCampaignCurrentMonthViewsDetailReport(Integer campaignId, Pagination pagination) {
		Session session = sessionFactory.getCurrentSession();
		String searchSql = "";
		if (pagination.getSearchKey() != null) {
			searchSql = " and (Lower(u.firstname) like '%" + pagination.getSearchKey().toLowerCase()
					+ "%' or Lower(u.lastname) like '%" + pagination.getSearchKey().toLowerCase()
					+ "%' or Lower(xt.companyname) like " + " '%" + pagination.getSearchKey().toLowerCase()
					+ "%' or Lower(u.email_id) like '%" + pagination.getSearchKey().toLowerCase() + "%' ) ";
		}
		String sql = "select c.campaign_id, c.campaign_name, u.user_id, u.email_id, xt.city||', '||xt.state||', '||xt.country as location, min(xt.start_time) as played_time ,xt.device_type as Device, xt.os as os,COUNT(distinct(case when  Action_Id <=9 THEN Session_Id end)) "
				+ "  +COUNT(distinct(case when  Action_Id =10 THEN session_id end)) as views_count, xt.companyname, u.firstname, u.lastname from v_xt_xtremand_log xt,xt_user_profile u,xt_campaign c where u.user_id=xt.user_id and "
				+ "  c.campaign_id=xt.campaign_id and  c.campaign_id=" + campaignId
				+ " and  extract(month from (xt.start_time))=extract(month from (current_date)) " + searchSql
				+ " group by c.campaign_id, c.campaign_name, u.user_id, u.email_id, location, Device, os, xt.companyname";
		SQLQuery query = session.createSQLQuery(sql);
		query.setFirstResult((pagination.getPageIndex() - 1) * pagination.getMaxResults());
		query.setMaxResults(pagination.getMaxResults());
		return query.list();
	}

	@Override
	public List<Object[]> listCampaignTodaysViewsDetailReport(Integer campaignId, Pagination pagination) {
		Session session = sessionFactory.getCurrentSession();
		String searchSql = "";
		if (pagination.getSearchKey() != null) {
			searchSql = " and (Lower(u.firstname) like '%" + pagination.getSearchKey().toLowerCase()
					+ "%' or Lower(u.lastname) like '%" + pagination.getSearchKey().toLowerCase()
					+ "%' or Lower(xt.companyname) like " + " '%" + pagination.getSearchKey().toLowerCase()
					+ "%' or Lower(u.email_id) like '%" + pagination.getSearchKey().toLowerCase() + "%' ) ";
		}
		String sql = "select c.campaign_id, c.campaign_name, u.user_id, u.email_id, xt.city||', '||xt.state||', '||xt.country as location, min(xt.start_time) as played_time , xt.device_type as Device, xt.os as os, COUNT(distinct(case when  Action_Id <=9 THEN Session_Id end)) "
				+ " +COUNT(distinct(case when  Action_Id =10 THEN session_id end)) as views_count, xt.companyname, u.firstname, u.lastname from v_xt_xtremand_log xt, xt_user_profile u, xt_campaign c where u.user_id=xt.user_id and  "
				+ " c.campaign_id=xt.campaign_id and  c.campaign_id=" + campaignId
				+ " and  to_date(to_char(xt.start_time,'YYYY-MM-DD'),'YYYY-MM-DD') =current_date " + searchSql
				+ " group by c.campaign_id, c.campaign_name, u.user_id, u.email_id, location, Device, os, xt.companyname";
		SQLQuery query = session.createSQLQuery(sql);
		query.setFirstResult((pagination.getPageIndex() - 1) * pagination.getMaxResults());
		query.setMaxResults(pagination.getMaxResults());
		return query.list();
	}

	@Override
	public Map<String, Object> listVideoViewsMinutesWatchedDetailReportByYear(Integer userId, Integer videoId,
			String year, Pagination pagination) {
		Session session = sessionFactory.getCurrentSession();
		String sql = "select distinct x.video_id, v.title, max(cuul.firstname) as firstname, max(cuul.lastname) as lastname,p.email_id,min(x.start_time), "
				+ "  round(cast((sum(extract(epoch from x.end_time)-extract(epoch from x.start_time))/60) as numeric),2) AS minutes_watched, "
				+ "  x.device_type,x.city as city, x.state as state, x.country as country, c.campaign_name FROM xt_xtremand_log x INNER JOIN xt_campaign c ON (x.campaign_id = c.campaign_id) "
				+ "  INNER JOIN xt_video_files v ON (x.video_id = v.id) INNER JOIN xt_user_profile p ON (x.user_id = p.user_id) left join xt_campaign_user_userlist cuul on c.campaign_id=cuul.campaign_id WHERE extract(year from x.start_time)='"
				+ year + "' " + "  AND (x.video_id = " + videoId + ") and p.user_id=" + userId
				+ " and cuul.user_id=x.user_id GROUP BY p.email_id,x.session_id,x.video_id,x.device_type,x.city,x.state,x.country, v.title, c.campaign_name ";
		SQLQuery query = session.createSQLQuery(sql);
		ScrollableResults scrollableResults = query.scroll();
		scrollableResults.last();
		Integer totalRecords = scrollableResults.getRowNumber() + 1;
		query.setFirstResult((pagination.getPageIndex() - 1) * pagination.getMaxResults());
		query.setMaxResults(pagination.getMaxResults());
		List<Object[]> data = query.list();
		Map<String, Object> resultMap = new HashMap<String, Object>();
		resultMap.put("totalRecords", totalRecords);
		resultMap.put("data", data);
		return resultMap;
	}

	@Override
	public Map<String, Object> listTodayVideoViewsMinutesWatchedDetailReport(Integer userId, Integer videoId,
			Pagination pagination) {
		Session session = sessionFactory.getCurrentSession();
		String sql = "select distinct x.video_id,v.title,  max(cuul.firstname) as firstname, max(cuul.lastname) as lastname,p.email_id,min(x.start_time), "
				+ " round(cast((sum(extract(epoch from x.end_time)-extract(epoch from x.start_time))/60) as numeric),2) AS minutes_watched, "
				+ " x.device_type,x.city as city, x.state as state, x.country as country, c.campaign_name FROM xt_xtremand_log x INNER JOIN xt_campaign c ON (x.campaign_id = c.campaign_id) "
				+ " INNER JOIN xt_video_files v ON (x.video_id = v.id) INNER JOIN xt_user_profile p ON (x.user_id = p.user_id) left join xt_campaign_user_userlist cuul on c.campaign_id=cuul.campaign_id WHERE date(x.start_time)=current_date "
				+ " AND (x.video_id = " + videoId + ") and p.user_id=" + userId
				+ " and cuul.user_id=x.user_id GROUP BY p.email_id,x.session_id,x.video_id,x.device_type,x.city,x.state,x.country, v.title, c.campaign_name";
		SQLQuery query = session.createSQLQuery(sql);
		ScrollableResults scrollableResults = query.scroll();
		scrollableResults.last();
		Integer totalRecords = scrollableResults.getRowNumber() + 1;
		query.setFirstResult((pagination.getPageIndex() - 1) * pagination.getMaxResults());
		query.setMaxResults(pagination.getMaxResults());
		List<Object[]> data = query.list();
		Map<String, Object> resultMap = new HashMap<String, Object>();
		resultMap.put("totalRecords", totalRecords);
		resultMap.put("data", data);
		return resultMap;
	}

	@Override
	public Map<String, Object> listVideoViewsMinutesWatchedDetailReportByMonth(Integer userId, Integer videoId,
			String month, Pagination pagination) {
		Session session = sessionFactory.getCurrentSession();
		String sql = "select distinct x.video_id,v.title,  max(cuul.firstname) as firstname, max(cuul.lastname) as lastname,p.email_id,min(x.start_time), "
				+ " round(cast((sum(extract(epoch from x.end_time)-extract(epoch from x.start_time))/60) as numeric),2) AS minutes_watched, "
				+ " x.device_type,x.city as city, x.state as state, x.country as country, c.campaign_name FROM xt_xtremand_log x INNER JOIN xt_campaign c ON (x.campaign_id = c.campaign_id) "
				+ " INNER JOIN xt_video_files v ON (x.video_id = v.id) INNER JOIN xt_user_profile p ON (x.user_id = p.user_id) left join xt_campaign_user_userlist cuul on c.campaign_id=cuul.campaign_id WHERE to_char(x.start_time,'Mon-yyyy')='"
				+ month + "' " + " AND (x.video_id = " + videoId + ") and p.user_id=" + userId
				+ " and cuul.user_id=x.user_id GROUP BY p.email_id,x.session_id,x.video_id,x.device_type,x.city,x.state,x.country, v.title, c.campaign_name";
		SQLQuery query = session.createSQLQuery(sql);
		ScrollableResults scrollableResults = query.scroll();
		scrollableResults.last();
		Integer totalRecords = scrollableResults.getRowNumber() + 1;
		query.setFirstResult((pagination.getPageIndex() - 1) * pagination.getMaxResults());
		query.setMaxResults(pagination.getMaxResults());
		List<Object[]> data = query.list();
		Map<String, Object> resultMap = new HashMap<String, Object>();
		resultMap.put("totalRecords", totalRecords);
		resultMap.put("data", data);
		return resultMap;
	}

	@Override
	public Map<String, Object> listVideoViewsMinutesWatchedDetailReportByQuarter(Integer userId, Integer videoId,
			String quarter, Pagination pagination) {
		Session session = sessionFactory.getCurrentSession();
		String sql = "select distinct x.video_id, v.title,  max(cuul.firstname) as firstname, max(cuul.lastname) as lastname,p.email_id,min(x.start_time), "
				+ "  round(cast((sum(extract(epoch from x.end_time)-extract(epoch from x.start_time))/60) as numeric),2) AS minutes_watched, x.device_type, "
				+ "  x.city as city, x.state as state, x.country as country, c.campaign_name FROM xt_xtremand_log x INNER JOIN xt_campaign c ON (x.campaign_id = c.campaign_id) INNER JOIN xt_video_files v "
				+ "  ON (x.video_id = v.id) INNER JOIN xt_user_profile p ON (x.user_id = p.user_id) left join xt_campaign_user_userlist cuul on c.campaign_id=cuul.campaign_id WHERE to_char(x.start_time,'q-yyyy')='"
				+ quarter + "' AND (x.video_id = " + videoId + ") " + "  and p.user_id=" + userId
				+ " and cuul.user_id=x.user_id GROUP BY p.email_id,x.session_id,x.video_id,x.device_type,x.city,x.state,x.country, v.title, c.campaign_name";
		SQLQuery query = session.createSQLQuery(sql);
		ScrollableResults scrollableResults = query.scroll();
		scrollableResults.last();
		Integer totalRecords = scrollableResults.getRowNumber() + 1;
		query.setFirstResult((pagination.getPageIndex() - 1) * pagination.getMaxResults());
		query.setMaxResults(pagination.getMaxResults());
		List<Object[]> data = query.list();
		Map<String, Object> resultMap = new HashMap<String, Object>();
		resultMap.put("totalRecords", totalRecords);
		resultMap.put("data", data);
		return resultMap;
	}

	@Override
	public Object[] listVideoViewsByYearDetailReport1(Integer videoId, String year) {
		Session session = sessionFactory.getCurrentSession();
		String sql = "select  CAST(extract(year from x.start_time) AS INTEGER) as years,x.video_id,v.title, COUNT(DISTINCT x.session_id) "
				+ " +(SUM(CASE WHEN x.action_id = 10 THEN 1 ELSE 0 END)) as views from xt_xtremand_log x,xt_video_files v "
				+ " where x.video_id=" + videoId + " and extract(year from x.start_time)=" + year
				+ " and v.id=x.video_id " + " group by  extract(year from x.start_time),x.video_id, v.title ";
		SQLQuery query = session.createSQLQuery(sql);
		return (Object[]) query.uniqueResult();
	}

	@Override
	public Map<String, Object> listVideoViewsByYearDetailReport2(Integer videoId, String year, Pagination pagination) {
		Session session = sessionFactory.getCurrentSession();
		String sql = " select CAST(extract(year from x.start_time) AS INTEGER) as years ,x.video_id, v.title, max(cuul.firstname) as firstname,max(cuul.lastname) as lastname,u.email_id,COUNT(DISTINCT x.session_id) "
				+ " +(SUM(CASE WHEN x.action_id = 10 THEN 1 ELSE 0 END)) as views,x.device_type,x.city as city, x.state as state, x.country as country from xt_xtremand_log x,xt_user_profile u,xt_campaign c,xt_campaign_user_userlist cuul, "
				+ " xt_video_files v where x.user_id=u.user_id and x.video_id=" + videoId
				+ " and extract(year from x.start_time)=" + year
				+ " and v.id=x.video_id and x.campaign_id=c.campaign_id and c.campaign_id=cuul.campaign_id and cuul.user_id=x.user_id group by years, "
				+ " u.email_id, x.video_id,x.device_type,x.city,x.state,x.country, v.title";
		SQLQuery query = session.createSQLQuery(sql);
		ScrollableResults scrollableResults = query.scroll();
		scrollableResults.last();
		Integer totalRecords = scrollableResults.getRowNumber() + 1;
		query.setFirstResult((pagination.getPageIndex() - 1) * pagination.getMaxResults());
		query.setMaxResults(pagination.getMaxResults());
		List<Object[]> data = query.list();
		Map<String, Object> resultMap = new HashMap<String, Object>();
		resultMap.put("totalRecords", totalRecords);
		resultMap.put("data", data);
		return resultMap;
	}

	@Override
	public Object[] listVideoViewsByQuarterDetailReport1(Integer videoId, String quarter) {
		Session session = sessionFactory.getCurrentSession();
		String sql = "select 'Q'||to_char(x.start_time,'q-yyyy') as quarter,x.video_id,v.title,COUNT(DISTINCT x.session_id) "
				+ " +(SUM(CASE WHEN x.action_id = 10 THEN 1 ELSE 0 END)) as views from xt_xtremand_log x,xt_video_files v "
				+ " where v.id=x.video_id and to_char(x.start_time,'q-yyyy')='" + quarter + "' and x.video_id="
				+ videoId + " group by to_char(x.start_time,'q-yyyy'),x.video_id,v.title ";
		SQLQuery query = session.createSQLQuery(sql);
		return (Object[]) query.uniqueResult();
	}

	@Override
	public Map<String, Object> listVideoViewsByQuarterDetailReport2(Integer videoId, String quarter,
			Pagination pagination) {
		Session session = sessionFactory.getCurrentSession();
		String sql = "select 'Q'||to_char(x.start_time,'q-yyyy') as quarter ,x.video_id,v.title, max(cuul.firstname) as firstname,max(cuul.lastname) as lastname,u.email_id,COUNT(DISTINCT x.session_id) "
				+ " +(SUM(CASE WHEN x.action_id = 10 THEN 1 ELSE 0 END)) as views,x.device_type,x.city as city, x.state as state, x.country as country from xt_xtremand_log x,xt_user_profile u , "
				+ " xt_video_files v,xt_campaign c,xt_campaign_user_userlist cuul where x.user_id=u.user_id and  to_char(x.start_time,'q-yyyy')='"
				+ quarter + "'  and x.video_id=" + videoId + " and v.id=x.video_id "
				+ " and x.campaign_id=c.campaign_id and c.campaign_id=cuul.campaign_id and cuul.user_id=x.user_id group by to_char(x.start_time,'q-yyyy'),u.email_id,x.video_id,x.device_type,x.city,x.state,x.country, v.title";
		SQLQuery query = session.createSQLQuery(sql);
		ScrollableResults scrollableResults = query.scroll();
		scrollableResults.last();
		Integer totalRecords = scrollableResults.getRowNumber() + 1;
		query.setFirstResult((pagination.getPageIndex() - 1) * pagination.getMaxResults());
		query.setMaxResults(pagination.getMaxResults());
		List<Object[]> data = query.list();
		Map<String, Object> resultMap = new HashMap<String, Object>();
		resultMap.put("totalRecords", totalRecords);
		resultMap.put("data", data);
		return resultMap;
	}

	@Override
	public Object[] listVideoViewsByMonthDetailReport1(Integer videoId, String month) {
		Session session = sessionFactory.getCurrentSession();
		String sql = " select to_char(x.start_time,'Mon-yyyy') as months,x.video_id,v.title,COUNT(DISTINCT x.session_id) "
				+ " +(SUM(CASE WHEN x.action_id = 10 THEN 1 ELSE 0 END)) as views from xt_xtremand_log x,xt_video_files v where v.id=x.video_id and x.video_id="
				+ videoId + "  and " + " to_char(x.start_time,'Mon-yyyy')='" + month
				+ "' group by to_char(x.start_time,'Mon-yyyy'),x.video_id,v.title ";
		SQLQuery query = session.createSQLQuery(sql);
		return (Object[]) query.uniqueResult();
	}

	@Override
	public Map<String, Object> listVideoViewsByMonthDetailReport2(Integer videoId, String month,
			Pagination pagination) {
		Session session = sessionFactory.getCurrentSession();
		String sql = "select to_char(x.start_time,'Mon-yyyy') as months,x.video_id,v.title,max(cuul.firstname) as firstname,max(cuul.lastname) as lastname,u.email_id,count(distinct x.session_id )"
				+ " +(SUM(CASE WHEN x.action_id = 10 THEN 1 ELSE 0 END)) as views,x.device_type,x.city as city, x.state as state, x.country as country from xt_xtremand_log x,xt_user_profile u , xt_video_files v, "
				+ " xt_campaign c,xt_campaign_user_userlist cuul where x.user_id=u.user_id and to_char(x.start_time,'Mon-yyyy')='"
				+ month + "' and x.video_id=" + videoId
				+ " and v.id=x.video_id and x.campaign_id=c.campaign_id and c.campaign_id=cuul.campaign_id and cuul.user_id=x.user_id group by"
				+ "  to_char(x.start_time,'Mon-yyyy') ,u.email_id,x.video_id,x.device_type,x.city,x.state,x.country, v.title";
		SQLQuery query = session.createSQLQuery(sql);
		ScrollableResults scrollableResults = query.scroll();
		scrollableResults.last();
		Integer totalRecords = scrollableResults.getRowNumber() + 1;
		query.setFirstResult((pagination.getPageIndex() - 1) * pagination.getMaxResults());
		query.setMaxResults(pagination.getMaxResults());
		List<Object[]> data = query.list();
		Map<String, Object> resultMap = new HashMap<String, Object>();
		resultMap.put("totalRecords", totalRecords);
		resultMap.put("data", data);
		return resultMap;
	}

	@Override
	public Object[] listCurrentMonthVideoViewsDetailReport1(Integer videoId, String date) {
		Session session = sessionFactory.getCurrentSession();
		String sql = "select to_char(x.start_time,'yyyy-mm-dd') as date,x.video_id,v.title,COUNT(DISTINCT x.session_id) "
				+ " + (SUM(CASE WHEN x.action_id = 10 THEN 1 ELSE 0 END)) as views  from xt_xtremand_log x,xt_video_files v "
				+ " where v.id=x.video_id and x.video_id=" + videoId + " and to_char(x.start_time,'yyyy-mm-dd')='"
				+ date + "'" + "  group by to_char(x.start_time,'yyyy-mm-dd'),x.video_id,v.title ";
		SQLQuery query = session.createSQLQuery(sql);
		return (Object[]) query.uniqueResult();
	}

	@Override
	public Map<String, Object> listCurrentMonthVideoViewsDetailReport2(Integer videoId, String date,
			Pagination pagination) {
		Session session = sessionFactory.getCurrentSession();
		String sql = "select to_char(x.start_time,'yyyy-mm-dd') as date ,x.video_id,v.title,max(cuul.firstname) as firstname,max(cuul.lastname) as lastname,u.email_id,count(distinct x.session_id) "
				+ " +(SUM(CASE WHEN x.action_id = 10 THEN 1 ELSE 0 END)) as views,x.device_type,x.city as city, x.state as state, x.country as country from xt_xtremand_log x,xt_user_profile u, "
				+ " xt_video_files v, xt_campaign c,xt_campaign_user_userlist cuul where x.user_id=u.user_id and to_char(x.start_time,'yyyy-mm-dd')='"
				+ date + "' and x.video_id=" + videoId + " and v.id=x.video_id "
				+ " and x.campaign_id=c.campaign_id and c.campaign_id=cuul.campaign_id and cuul.user_id=x.user_id group by date,u.email_id,x.video_id,x.device_type,x.city,x.state,x.country, v.title";
		SQLQuery query = session.createSQLQuery(sql);
		ScrollableResults scrollableResults = query.scroll();
		scrollableResults.last();
		Integer totalRecords = scrollableResults.getRowNumber() + 1;
		query.setFirstResult((pagination.getPageIndex() - 1) * pagination.getMaxResults());
		query.setMaxResults(pagination.getMaxResults());
		List<Object[]> data = query.list();
		Map<String, Object> resultMap = new HashMap<String, Object>();
		resultMap.put("totalRecords", totalRecords);
		resultMap.put("data", data);
		return resultMap;
	}

	@Override
	public List<Object[]> getVideoSkippedDurationData(Integer videoId) {
		Session session = sessionFactory.getCurrentSession();
		String sql = "SELECT  generate_series(start_duration,stop_duration) AS duration, "
				+ " COUNT(DISTINCT (CASE WHEN (action_id = 1  ) THEN session_id ELSE NULL END)) + SUM(CASE WHEN action_id = 10 THEN 1 ELSE 0 END) as views, "
				+ " -COUNT(DISTINCT (CASE WHEN (action_id = 8) THEN session_id ELSE NULL END)) as skipped "
				+ " FROM xt_xtremand_log  " + " WHERE (video_id = " + videoId + ") AND start_duration IS NOT NULL "
				+ " GROUP BY duration order by duration ";
		SQLQuery query = session.createSQLQuery(sql);
		return query.list();
	}

	@Override
	public Map<String, Object> getVideoWatchedFullyDetailReport(Integer videoId, Pagination pagination) {
		Session session = sessionFactory.getCurrentSession();
		String sql = "select distinct c.campaign_name,u.email_id, max(cuul.firstname) as firstName, max(cuul.lastname) as lastName, x.start_time,x.device_type,x.city as city, x.state as state, x.country as country from xt_xtremand_log x left join xt_user_profile u "
				+ "on x.user_id=u.user_id " + "left join xt_campaign c "
				+ "on c.campaign_id=x.campaign_id left join xt_campaign_user_userlist cuul on c.campaign_id=cuul.campaign_id and cuul.user_id=x.user_id where  x.start_duration>=0  and x.video_id="
				+ videoId + " and x.action_id=9 "
				+ " group by c.campaign_name,u.email_id,x.start_time,x.device_type,x.city,x.state,x.country";
		SQLQuery query = session.createSQLQuery(sql);
		ScrollableResults scrollableResults = query.scroll();
		scrollableResults.last();
		Integer totalRecords = scrollableResults.getRowNumber() + 1;
		query.setFirstResult((pagination.getPageIndex() - 1) * pagination.getMaxResults());
		query.setMaxResults(pagination.getMaxResults());
		List<Object[]> data = query.list();
		Map<String, Object> resultMap = new HashMap<String, Object>();
		resultMap.put("totalRecords", totalRecords);
		resultMap.put("data", data);
		return resultMap;
	}

	@Override
	public List<Object[]> listTotalMinutesWatchedByTop10UsersDetailReport(Integer videoId) {
		Session session = sessionFactory.getCurrentSession();
		String sql = "select max(cuul.firstname) as firstName, max(cuul.lastname) as lastName, up.email_id,max(x.start_time), round(cast((sum(extract(epoch from x.end_time)-extract(epoch from x.start_time))/60) as numeric),2) as minuteswatched , "
				+ " x.device_type,x.city as city, x.state as state, x.country as country from xt_xtremand_log x,xt_campaign ca ,xt_user_profile up, xt_campaign_user_userlist cuul where x.campaign_id=ca.campaign_id and  "
				+ "  up.user_id=x.user_id and x.video_id=" + videoId
				+ " and x.action_id not in (2,9) and ca.campaign_id=cuul.campaign_id and cuul.user_id=x.user_id group by up.email_id,x.device_type,x.city,x.state,x.country order by minuteswatched desc "
				+ " limit 10 ";
		SQLQuery query = session.createSQLQuery(sql);
		return query.list();
	}

	@Override
	public Map<String, Object> listVideoDurationPlayedUsers(Integer videoId, Pagination pagination) {
		Session session = sessionFactory.getCurrentSession();
		String sql = "select distinct x.start_duration,max(cuul.firstname) as firstName, max(cuul.lastname) as lastName, u.email_id, min(x.start_time), x.end_time,x.device_type,x.country from xt_xtremand_log x left join xt_user_profile u "
				+ " on x.user_id=u.user_id left join xt_campaign c on x.campaign_id=c.campaign_id left join xt_campaign_user_userlist cuul on c.campaign_id=cuul.campaign_id where x.video_id="
				+ videoId
				+ " and (x.action_id=1 or action_id=10) and cuul.user_id=x.user_id group by x.session_id,x.start_time, x.action_id, "
				+ " u.email_id,x.start_duration,x.device_type,x.country, x.end_time order by u.email_id, x.start_duration ";
		SQLQuery query = session.createSQLQuery(sql);
		ScrollableResults scrollableResults = query.scroll();
		scrollableResults.last();
		Integer totalRecords = scrollableResults.getRowNumber() + 1;
		query.setFirstResult((pagination.getPageIndex() - 1) * pagination.getMaxResults());
		query.setMaxResults(pagination.getMaxResults());
		List<Object[]> data = query.list();
		Map<String, Object> resultMap = new HashMap<String, Object>();
		resultMap.put("totalRecords", totalRecords);
		resultMap.put("data", data);
		return resultMap;
	}

	@Override
	public Map<String, Object> listVideoDurationSkippedUsers(Integer videoId, Pagination pagination) {
		Session session = sessionFactory.getCurrentSession();
		String sql = "select distinct x.start_duration,max(cuul.firstname) as firstName, max(cuul.lastname) as lastName, u.email_id, min(x.start_time),x.end_time,x.device_type,x.country from xt_xtremand_log x left join xt_user_profile u "
				+ " on x.user_id=u.user_id left join xt_campaign c on x.campaign_id=c.campaign_id left join xt_campaign_user_userlist cuul on c.campaign_id=cuul.campaign_id where x.video_id="
				+ videoId
				+ " and x.action_id=8 and cuul.user_id=x.user_id group by x.session_id,x.start_time, x.action_id,u.email_id, "
				+ "x.start_duration,x.device_type,x.country,x.end_time order by u.email_id, x.start_duration ";
		SQLQuery query = session.createSQLQuery(sql);
		ScrollableResults scrollableResults = query.scroll();
		scrollableResults.last();
		Integer totalRecords = scrollableResults.getRowNumber() + 1;
		query.setFirstResult((pagination.getPageIndex() - 1) * pagination.getMaxResults());
		query.setMaxResults(pagination.getMaxResults());
		List<Object[]> data = query.list();
		Map<String, Object> resultMap = new HashMap<String, Object>();
		resultMap.put("totalRecords", totalRecords);
		resultMap.put("data", data);
		return resultMap;
	}

	@Override
	public List<Number> getNAUsersVideoViewsMinutesWatched(Integer videoId) {
		Session session = sessionFactory.getCurrentSession();

		String sql = "select coalesce(COUNT(DISTINCT session_id),0) + coalesce((SUM(CASE WHEN action_id = 10 THEN 1 ELSE 0 END)),0) AS count  from xt_xtremand_log where user_id=0  and campaign_id=0 and video_id="
				+ videoId + " union all "
				+ " Select coalesce(round(cast((sum(extract(epoch from end_time)-extract(epoch from start_time))/60) as numeric),2),0) AS minutes_watched "
				+ " from xt_xtremand_log where user_id=0  and campaign_id=0 and video_id=" + videoId + "";
		SQLQuery query = session.createSQLQuery(sql);
		return query.list();
	}

	@Override
	public Map<String, Object> getDashboardWorldMapDetailReport(Pagination pagination, Integer userId,
			String countryCode) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		try {
			Session session = sessionFactory.getCurrentSession();
			String sql = " select x.firstname,x.lastname,x.email_id, min(start_time) as watched_time,x.device_type,x.city as city, x.state as state, x.country as country,x.os from v_xt_xtremand_log x,xt_user_profile u "
					+ " where u.user_id=x.user_id and campaign_id in (select campaign_id from xt_campaign where customer_id in "
					+ " (select user_id from xt_user_profile where company_id=(select company_id from xt_user_profile where user_id="
					+ userId + "))) and x.country_code='" + countryCode + "' "
					+ " and x.country_code IS NOT NULL group by x.firstname,x.lastname,x.email_id,x.city,x.state,x.country,x.user_id,x.device_type,x.os "
					+ " union all "
					+ " select  null,null,null,min(start_time) as watched_time,x.device_type,x.city as city,x.state as state,x.country as country,x.os from v_xt_xtremand_log x,xt_user_profile u where "
					+ "  x.user_id=0 and x.campaign_id=0 and x.video_id in(select id  from xt_video_files where customer_id in (select user_id from xt_user_profile  where "
					+ " company_id=(select company_id from xt_user_profile where user_id=" + userId
					+ "))) and x.country_code='" + countryCode + "' and x.country_code IS NOT NULL group by  "
					+ " x.city,x.state,x.country,x.device_type,x.city,x.state,x.os ";
			SQLQuery query = session.createSQLQuery(sql);
			ScrollableResults scrollableResults = query.scroll();
			scrollableResults.last();
			Integer totalRecords = scrollableResults.getRowNumber() + 1;
			query.setFirstResult((pagination.getPageIndex() - 1) * pagination.getMaxResults());
			query.setMaxResults(pagination.getMaxResults());
			List<Object[]> data = query.list();
			resultMap.put("totalRecords", totalRecords);
			resultMap.put("data", data);
		} catch (HibernateException e) {
			e.printStackTrace();
		}
		return resultMap;
	}

	@Override
	public Integer getTotalTimeSpentByCampaignUser(Integer userId, Integer campaignId) {
		Session session = sessionFactory.getCurrentSession();
		String sql = "SELECT sum(stop_duration - start_duration) as spent FROM xt_xtremand_log where user_id=" + userId
				+ " and action_id=1 and campaign_id=" + campaignId;
		SQLQuery query = session.createSQLQuery(sql);
		Integer count = query.uniqueResult() != null ? ((BigInteger) query.uniqueResult()).intValue() : 0;
		return count;
	}

	@Override
	public List<Object[]> getCampaignViewsCountByCountry(Integer campaignId) {
		Session session = sessionFactory.getCurrentSession();
		String sql = " select  count(distinct( case when action_id=1 then session_id end))+ count(distinct(case when action_id=10 then session_id end))AS  views,country_code "
				+ " from xt_xtremand_log  where campaign_id=" + campaignId
				+ " and country_code is not null group by country_code ";
		SQLQuery query = session.createSQLQuery(sql);
		return query.list();
	}

	@Override
	public Map<String, Object> listCountryWiseCampaignViewsDetailReport(Integer campaignId, String countryCode,
			Pagination pagination) {
		Session session = sessionFactory.getCurrentSession();
		String searchSQl = "";
		if (pagination.getSearchKey() != null) {
			searchSQl = " and (Lower(u.firstname) like '%" + pagination.getSearchKey().toLowerCase()
					+ "%' or Lower(u.lastname) like '%" + pagination.getSearchKey().toLowerCase() + "%' "
					+ "or Lower(x.companyname) like '%" + pagination.getSearchKey().toLowerCase()
					+ "%' or Lower(u.email_id) like '%" + pagination.getSearchKey().toLowerCase() + "%')";
		}
		String sql = "select u.user_id, u.email_id, min(x.start_time) as watched_time,x.device_type,x.city||','||x.state||','||x.country as location,x.country_code, x.companyname, u.firstname, u.lastname from v_xt_xtremand_log x,xt_user_profile u"
				+ " where u.user_id=x.user_id and x.campaign_id=" + campaignId
				+ " and x.action_id=1 and  x.country_code='" + countryCode + "' and x.country_code is not null "
				+ searchSQl
				+ " group by u.user_id, location,x.session_id,u.email_id,x.device_type,x.country_code, x.companyname"
				+ " union all"
				+ " select  u.user_id, u.email_id, min(x.start_time) as watched_time,x.device_type,x.city||','||x.state||','||x.country as location, x.country_code,  x.companyname, u.firstname, u.lastname from v_xt_xtremand_log x,xt_user_profile u "
				+ " where u.user_id=x.user_id and x.campaign_id=" + campaignId
				+ " and x.action_id=10 and  x.country_code='" + countryCode + "' and x.country_code is not null "
				+ searchSQl
				+ " group by u.user_id, location,x.session_id,u.email_id,x.device_type, x.country_code, x.companyname";
		SQLQuery query = session.createSQLQuery(sql);
		ScrollableResults scrollableResults = query.scroll();
		scrollableResults.last();
		Integer totalRecords = scrollableResults.getRowNumber() + 1;
		query.setFirstResult((pagination.getPageIndex() - 1) * pagination.getMaxResults());
		query.setMaxResults(pagination.getMaxResults());
		List<Object[]> data = query.list();
		Map<String, Object> resultMap = new HashMap<String, Object>();
		resultMap.put("totalRecords", totalRecords);
		resultMap.put("data", data);
		return resultMap;
	}

	@Override
	public List<Object[]> getVideoViewsCountByCountry(Integer videoId) {
		Session session = sessionFactory.getCurrentSession();
		String sql = "select count(distinct(case when action_id=1  then session_id end))+coalesce(SUM(CASE WHEN action_id = 10 THEN 1 ELSE 0 END),0) as views,country_code "
				+ " from xt_xtremand_log where video_id=" + videoId
				+ " and country_code is not null group by country_code ";
		SQLQuery query = session.createSQLQuery(sql);
		return query.list();
	}

	@Override
	public Map<String, Object> listCountryWiseVideoViewsDetailReport(Integer videoId, String countryCode,
			Pagination pagination) {
		logger.debug("entered in HibernateXtremandLogDAO listWatchedUsersByUser() mehtod");
		Session session = sessionFactory.getCurrentSession();
		String sql = "SELECT distinct on (el.session_id) up.email_id as \"emailId\", "
				+ " max(cuul.firstname) as \"firstName\",max(cuul.lastname) as \"lastName\", "
				+ " c.campaign_name as \"campaignName\",el.device_type as \"deviceType\", "
				+ " el.os as \"os\",el.city as \"city\", el.country as \"country\",el.isp as \"isp\",el.ip_address as "
				+ " \"ipAddress\",el.state as \"state\",el.zip as \"zip\",el.latitude as \"latitude\",el.longitude "
				+ " as \"longitude\",el.country_code as \"countryCode\",el.start_time as \"time\" "
				+ " FROM xt_xtremand_log el, xt_campaign c, xt_user_profile up, "
				+ " xt_video_files v, xt_campaign_user_userlist cuul "
				+ " where el.action_id=1 and el.campaign_id= c.campaign_id and el.video_id=v.id and el.user_id=up.user_id "
				+ " and v.id=" + videoId + " and el.country_code='" + countryCode + "'"
				+ " and c.campaign_id=cuul.campaign_id and cuul.user_id=el.user_id "
				+ " group by el.id,up.email_id,c.campaign_name,el.session_id,el.device_type, el.os, el.city, el.country, "
				+ " el.isp,el.ip_address,el.state,el.zip,el.latitude,el.longitude,el.country_code,el.start_time"
				+ " union all " + "  SELECT distinct on (el.session_id) up.email_id as \"emailId\", "
				+ " max(cuul.firstname) as \"firstName\",max(cuul.lastname) as \"lastName\", "
				+ " c.campaign_name as \"campaignName\",el.device_type as \"deviceType\", "
				+ " el.os as \"os\",el.city as \"city\", el.country as \"country\",el.isp as \"isp\",el.ip_address as "
				+ " \"ipAddress\",el.state as \"state\",el.zip as \"zip\",el.latitude as \"latitude\",el.longitude "
				+ " as \"longitude\",el.country_code as \"countryCode\",el.start_time as \"time\" "
				+ " FROM xt_xtremand_log el, xt_campaign c, xt_user_profile up, "
				+ " xt_video_files v, xt_campaign_user_userlist cuul "
				+ " where el.action_id=10 and el.campaign_id= c.campaign_id and el.video_id=v.id and el.user_id=up.user_id "
				+ " and v.id=" + videoId + " and el.country_code='" + countryCode + "'"
				+ " and c.campaign_id=cuul.campaign_id and cuul.user_id=el.user_id "
				+ " group by el.id,up.email_id,c.campaign_name,el.session_id,el.device_type, el.os, el.city, el.country, "
				+ " el.isp,el.ip_address,el.state,el.zip,el.latitude,el.longitude,el.country_code,el.start_time"
				+ " union all "
				+ " SELECT distinct on (el.session_id) null,null,null,null,el.device_type as \"deviceType\", "
				+ " el.os as \"os\",el.city as \"city\", el.country as \"country\",el.isp as \"isp\",el.ip_address as "
				+ " \"ipAddress\",el.state as \"state\",el.zip as \"zip\",el.latitude as \"latitude\",el.longitude "
				+ " as \"longitude\",el.country_code as \"countryCode\",el.start_time as \"time\" "
				+ " FROM xt_xtremand_log el, "
				+ " xt_video_files v where el.action_id=1 and el.video_id=v.id and el.video_id=" + videoId
				+ " and el.user_id=0 and el.campaign_id=0 and el.country_code='" + countryCode + "'" + " union all "
				+ " SELECT distinct on (el.session_id) null,null,null,null,el.device_type as \"deviceType\", "
				+ " el.os as \"os\",el.city as \"city\", el.country as \"country\",el.isp as \"isp\",el.ip_address as "
				+ " \"ipAddress\",el.state as \"state\",el.zip as \"zip\",el.latitude as \"latitude\",el.longitude "
				+ " as \"longitude\",el.country_code as \"countryCode\",el.start_time as \"time\" "
				+ " FROM xt_xtremand_log el, "
				+ " xt_video_files v where el.action_id=10 and el.video_id=v.id and el.video_id=" + videoId
				+ " and el.user_id=0 and el.campaign_id=0 and el.country_code='" + countryCode + "'";

		// Query query = session.createSQLQuery(sql).addEntity("el",
		// XtremandLog.class).addScalar("email_id").addScalar("firstname").addScalar("lastname").addScalar("campaign_name");
		Query query = session.createSQLQuery(sql);
		ScrollableResults scrollableResults = query.scroll();
		scrollableResults.last();
		Integer totalRecords = scrollableResults.getRowNumber() + 1;
		query.setFirstResult((pagination.getPageIndex() - 1) * pagination.getMaxResults());
		query.setMaxResults(pagination.getMaxResults());
		query.setResultTransformer(Transformers.aliasToBean(EmailLogReport.class));
		List<EmailLogReport> data = query.list();
		Map<String, Object> resultMap = new HashMap<String, Object>();
		resultMap.put("totalRecords", totalRecords);
		resultMap.put("data", data);
		return resultMap;
	}

	@Override
	public Integer getCampaignEmailOpenCountByUser(Integer campaignId, Integer userId) {
		Session session = sessionFactory.getCurrentSession();
		Query query = session.createQuery(
				"select count(*) from EmailLog where action_id=13 and campaignId=:campaignId and userId=:userId");
		query.setParameter("campaignId", campaignId);
		query.setParameter("userId", userId);
		Integer count = ((Long) query.uniqueResult()).intValue();
		return count;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Object[]> getVideoCoBrandingLogoEnableStatus(Integer videoId, Integer userId) {
		Session session = sessionFactory.getCurrentSession();
		String sql = "select distinct c.campaign_id , c.launch_time, c.enable_co_branding_logo from xt_campaign_videos cv, xt_campaign_user_userlist cu, xt_campaign c where cv.video_id = :videoId and cu.user_id=:userId  "
				+ " and c.campaign_id=cv.campaign_id and cu.campaign_id=c.campaign_id and c.is_launched=true order by c.launch_time desc ";
		SQLQuery query = session.createSQLQuery(sql);
		query.setParameter("videoId", videoId);
		query.setParameter("userId", userId);
		return query.list();
	}

	@Override
	public Integer getVideoNotOpenedEmailNotificationCount(Integer userId, Integer videoId, Integer campaignId,
			Integer replyId) {
		try {
			logger.debug("In getVideoNotOpenedEmailNotificationCount() DAO");
			Session session = sessionFactory.getCurrentSession();
			return ((Integer) session.createSQLQuery(
					"select cast(count(*) as int) from xt_email_log where video_id=" + videoId + " and user_id="
							+ userId + " and campaign_id=" + campaignId + " and action_id=18 and reply_id=" + replyId)
					.uniqueResult());
		} catch (HibernateException | XamplifyDataAccessException e) {
			logger.error("Error In getVideoNotOpenedEmailNotificationCount() DAO", e);
			throw new XamplifyDataAccessException(e.getMessage());
		} catch (Exception ex) {
			logger.error("Error In getVideoNotOpenedEmailNotificationCount() DAO", ex);
			throw new XamplifyDataAccessException(ex.getMessage());
		}
	}

	@Override
	public Integer getEmailActionCountByCampaignIdAndUserIdAndActionType(Integer campaignId, Integer userId,
			String actionType) {
		Session session = sessionFactory.getCurrentSession();
		String actionQuery = null;
		if ("open".equalsIgnoreCase(actionType))
			actionQuery = " = 13";
		else if ("click".equalsIgnoreCase(actionType))
			actionQuery = " IN (14, 15)";
		Query query = session.createSQLQuery("select count(1) from xt_email_log where user_id = " + userId
				+ " and campaign_id=" + campaignId + "  and  action_id" + actionQuery);
		Integer count = ((BigInteger) query.uniqueResult()).intValue();
		return count;
	}

	@Override
	public List<EmailLogView> listEmailLogsByCampaignIdUserIdActionType(Integer campaignId, Integer userId,
			String actionType) {
		Session session = sessionFactory.getCurrentSession();
		String actionQuery = null;
		String sql = null;
		if ("open".equalsIgnoreCase(actionType)) {
			actionQuery = " = 13 and reply_id is null and url_id is null ";
			sql = "select distinct on (v.email_id, to_char(v.time, 'YYYY-MM-DD HH24:MI')) * from v_xt_email_log v where v.user_id = "
					+ userId + " and v.campaign_id=" + campaignId + "  and  v.action_id" + actionQuery;
		} else if ("click".equalsIgnoreCase(actionType)) {
			actionQuery = " IN (14, 15) and reply_id is null and url_id is null ";
			sql = "select  * from v_xt_email_log v where v.user_id = " + userId + " and v.campaign_id=" + campaignId
					+ "  and  v.action_id" + actionQuery;
		}

		Query query = session.createSQLQuery(sql).addEntity(EmailLogView.class);
		/*
		 * Query query =
		 * session.createSQLQuery("select * from v_xt_email_log v where v.user_id = " +
		 * userId+ " and v.campaign_id=" + campaignId + "  and  v.action_id" +
		 * actionQuery)
		 */
		return query.list();
	}

	@Override
	public List<String> listClickedUrlsByCampaignIdUserId(Integer campaignId, Integer userId) {
		Session session = sessionFactory.getCurrentSession();
		String sql = "select clicked_url from v_xt_email_log v where v.user_id = " + userId + " and v.campaign_id="
				+ campaignId + "  and  v.action_id IN (14, 15)";
		Query query = session.createSQLQuery(sql);
		return query.list();
	}

	@Override
	public Map<String, Object> campaignInteractiveViews(Pagination pagination) {
		Session session = sessionFactory.getCurrentSession();
		String sqlQuery = null;
		String searchSql = "";
		switch (pagination.getCampaignType()) {
		case "REGULAR":
		case "LANDINGPAGE":
		case "SURVEY":
		case "SOCIAL":
			if (pagination.getSearchKey() != null) {
				searchSql = " and (Lower(xel.email_id) like '%" + pagination.getSearchKey().toLowerCase() + "%' or "
						+ " Lower(xel.firstname) like '%" + pagination.getSearchKey().toLowerCase() + "%' or "
						+ " Lower(xel.lastname) like '%" + pagination.getSearchKey().toLowerCase() + "%' ) ";
			}
			sqlQuery = "select distinct xc.campaign_id, xc.campaign_name, xel.user_id, xel.email_id, xel.firstname, xel.lastname, max(xel.time), "
					+ " cast(count(distinct case when xel.action_id = 13 and xel.url_id is null and xel.reply_id is null and "
					+ " (xel.video_id is null or xel.video_id=0) then date_trunc('minute',xel.time) end ) as integer) as emailOpenedCount, "
					+ " cast(count(distinct case when (xel.action_id = 14 or xel.action_id = 15) and xel.url_id is null and "
					+ " xel.reply_id is null then xel.id end) as integer) as clickedUrlsCount" + " from "
					+ " xt_campaign xc " + " left join v_xt_email_log xel on xel.campaign_id = xc.campaign_id "
					+ " where xel.campaign_id=:campaignId " + searchSql + " group by 1,2,3,4,5,6 ";
			if (pagination.getSortcolumn() != null) {
				String sortColumn = pagination.getSortcolumn();
				if ("urls_clicked_count".equals(pagination.getSortcolumn())) {
					sortColumn = "clickedUrlsCount";
				} else if ("email_opened_count".equals(pagination.getSortcolumn())) {
					sortColumn = "emailOpenedCount";
				}
				sqlQuery = sqlQuery + " order by " + sortColumn + " " + pagination.getSortingOrder();
			}
			break;
		case "SMS":
			sqlQuery = "SELECT c.campaign_id, c.campaign_name, sl.user_id, u.email_id, u.firstname, u.lastname,u.mobile_number sl.time\r\n"
					+ "FROM xt_sms_log sl \r\n" + "INNER JOIN xt_campaign c ON c.campaign_id = sl.campaign_id\r\n"
					+ "WHERE sl.id IN (SELECT max(id) FROM xt_sms_log WHERE campaign_id = :campaignId AND action_id = 13 GROUP BY user_id)"
					+ " and u.user_id=sl.user_id\r\n";
			break;
		case "VIDEO":
			if (pagination.getSearchKey() != null) {
				searchSql = " and (Lower(xel.email_id) like '%" + pagination.getSearchKey().toLowerCase() + "%' or "
						+ " Lower(xel.firstname) like '%" + pagination.getSearchKey().toLowerCase() + "%' or "
						+ " Lower(xel.lastname) like '%" + pagination.getSearchKey().toLowerCase() + "%' ) ";
			}
			sqlQuery = "select xc.campaign_id, xc.campaign_name, xel.user_id, xel.email_id, xel.firstname, xel.lastname, max(xxl.start_time), "
					+ " cast(count(distinct case when xel.action_id = 13 and xel.url_id is null and xel.reply_id is null and "
					+ " xel.video_id is not null then date_trunc('minute',xel.time) end ) as integer) as emailOpenedCount , "
					+ " cast(count(distinct case when (xel.action_id = 14 or xel.action_id = 15) and xel.url_id is null and "
					+ " xel.reply_id is null then xel.id end) as integer) as clickedUrlsCount, "
					+ " cast(count(DISTINCT xxl.session_id) + count(distinct CASE WHEN xxl.action_id = 10 THEN xxl.id END) as integer) AS  views "
					+ " from " + " xt_campaign xc "
					+ " left join v_xt_email_log xel on xel.campaign_id = xc.campaign_id "
					+ " left join v_xt_xtremand_log xxl ON (xc.campaign_id = xxl.campaign_id) and xxl.user_id=xel.user_id "
					+ " where xel.campaign_id=:campaignId " + searchSql + " group by 1,2,3,4,5,6 ";
			if (pagination.getSortcolumn() != null) {
				String sortColumn = pagination.getSortcolumn();
				if ("urls_clicked_count".equals(pagination.getSortcolumn())) {
					sortColumn = "clickedUrlsCount";
				} else if ("email_opened_count".equals(pagination.getSortcolumn())) {
					sortColumn = "emailOpenedCount";
				} else if ("views_count".equals(pagination.getSortcolumn())) {
					sortColumn = "views";
				}
				sqlQuery = sqlQuery + " order by " + sortColumn + " " + pagination.getSortingOrder();
			}
			break;
		case "EVENT":
			if (pagination.getSearchKey() != null) {
				searchSql = " and (Lower(up.email_id) like '%" + pagination.getSearchKey().toLowerCase() + "%' or "
						+ " Lower(cuul.firstname) like '%" + pagination.getSearchKey().toLowerCase() + "%' or "
						+ " Lower(cuul.lastname) like '%" + pagination.getSearchKey().toLowerCase() + "%' ) ";
			}
			sqlQuery = " SELECT c.campaign_id, c.campaign_name, cuul.user_id, up.email_id,  cuul.firstname, cuul.lastname,cuul.companyname, cer.rsvp_type, "
					+ " cer.additional_count FROM  xt_campaign_user_userlist cuul, xt_campaign c, xt_campaign_event_rsvp cer,xt_user_profile up where "
					+ " cuul.campaign_id=c.campaign_id and cuul.user_id = up.user_id and cuul.user_id=cer.user_id and cuul.campaign_id=cer.campaign_id and "
					+ " cer.rsvp_time in "
					+ " (select max(rsvp_time) from xt_campaign_event_rsvp where campaign_id = :campaignId group by user_id) "
					+ searchSql;
			break;
		}

		Query query = session.createSQLQuery(sqlQuery);
		query.setParameter("campaignId", pagination.getCampaignId());
		ScrollableResults scrollableResults = query.scroll();
		scrollableResults.last();
		Integer totalRecords = scrollableResults.getRowNumber() + 1;
		query.setFirstResult((pagination.getPageIndex() - 1) * pagination.getMaxResults());
		query.setMaxResults(pagination.getMaxResults());
		List<Object[]> data = query.list();
		Map<String, Object> resultMap = new HashMap<String, Object>();
		resultMap.put("totalRecords", totalRecords);
		resultMap.put("data", data);
		return resultMap;
	}

	/**
	 * 
	 * 
	 * SMS SERVICES
	 */
	@Override
	public SMS_URLShortener getSMS_URLShortenerByAlias(String alias) {
		Session session = sessionFactory.getCurrentSession();
		Criteria criteria = session.createCriteria(SMS_URLShortener.class);
		criteria.add(Restrictions.eq("alias", alias));
		return (SMS_URLShortener) criteria.uniqueResult();
	}

	@Override
	public Integer getSMSSentCount(Integer campaignId) {
		Session session = sessionFactory.getCurrentSession();
		String sqlQuery = "SELECT cast(count(*) as int) FROM (SELECT DISTINCT campaign_id, user_id FROM xt_campaign_sms_history WHERE campaign_id = "
				+ campaignId + ") A";
		return (Integer) session.createSQLQuery(sqlQuery).uniqueResult();
	}

	@Override
	public Integer getSMSSentSuccessCount(Integer campaignId) {
		Session session = sessionFactory.getCurrentSession();
		String sqlQuery = "SELECT cast(count(*) as int) FROM (SELECT DISTINCT campaign_id, user_id FROM xt_campaign_sms_history WHERE campaign_id = "
				+ campaignId + " and status='success') A";
		return (Integer) session.createSQLQuery(sqlQuery).uniqueResult();
	}

	@Override
	public Integer getSMSSentFailureCount(Integer campaignId) {
		Session session = sessionFactory.getCurrentSession();
		String sqlQuery = "SELECT count(*) FROM (SELECT DISTINCT campaign_id, user_id FROM xt_campaign_sms_history WHERE campaign_id = "
				+ campaignId + " and status='failure') A";
		return ((BigInteger) session.createSQLQuery(sqlQuery).uniqueResult()).intValue();
	}

	@Override
	public List<Object> listSMSLogsByCampaignIdUserIdActionType(Integer campaignId, Integer userId, String actionType) {
		Session session = sessionFactory.getCurrentSession();
		String actionQuery = null;
		if ("open".equalsIgnoreCase(actionType))
			actionQuery = " = 13";
		else if ("click".equalsIgnoreCase(actionType))
			actionQuery = " IN (14, 15)";
		Query query = session
				.createSQLQuery("select distinct on ((select u.mobile_number from xt_user_profile u where u.user_id = "
						+ userId + "), to_char(s.time, 'YYYY-MM-DD HH24:MI')) * from xt_sms_log s where s.user_id = "
						+ userId + " and s.campaign_id=" + campaignId + "  and  s.action_id" + actionQuery);
		return query.list();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<SMSLog> listSMSLogsByCampaignAndUser(Integer userId, Integer campaignId) {
		try {
			Session session = sessionFactory.getCurrentSession();
			String sql = "select distinct on (s.action_id, to_char(s.time, 'YYYY-MM-DD HH24:MI')) * from xt_sms_log s where s.user_id = "
					+ userId + "  and s.campaign_id=" + campaignId + " group by s.time, s.id";
			Query query = session.createSQLQuery(sql).addEntity(SMSLog.class);
			return query.list();

		} catch (Exception exception) {
			throw new XamplifyDataAccessException(exception.getMessage());
		}
	}

	@Override
	public Integer getSMS_LogCountByCampaign(Integer campaignId, String actionId) {
		Session session = sessionFactory.getCurrentSession();
		String sql = "select count(DISTINCT user_id) from xt_sms_log where action_id IN " + actionId
				+ " and campaign_id=" + campaignId;
		SQLQuery query = session.createSQLQuery(sql);
		Integer count = ((BigInteger) query.uniqueResult()).intValue();
		return count;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<EmailLogView> listSMS_LogsByAction(Integer campaignId, String actionType, Pagination pagination)
			throws XamplifyDataAccessException {
		try {
			Session session = sessionFactory.getCurrentSession();
			String q = null;
			if ("open".equalsIgnoreCase(actionType))
				q = " = 13";
			else if ("click".equalsIgnoreCase(actionType))
				q = " IN (14,15)";
			String sortQuery = "";
			if (StringUtils.hasText(pagination.getSortcolumn())) {
				switch (pagination.getSortcolumn()) {
				case "time":
					sortQuery = " ORDER BY b.time " + pagination.getSortingOrder();
					break;
				case "name":
					sortQuery = " ORDER BY b.firstName " + pagination.getSortingOrder() + "," + "b.lastName "
							+ pagination.getSortingOrder();
					break;
				case "subject":
					sortQuery = " ORDER BY b.subject " + pagination.getSortingOrder();
					break;
				default:
					break;
				}
			}

			String hql = "SELECT b.*\r\n" + "FROM\r\n" + "(\r\n"
					+ "SELECT user_id, max(time) as latest FROM v_xt_email_log \r\n" + "WHERE campaign_id = "
					+ campaignId + " AND action_id " + q + "\r\n" + " GROUP BY user_id\r\n" + ") a,\r\n" + "\r\n"
					+ "(SELECT * FROM v_xt_email_log \r\n" + "WHERE campaign_id = " + campaignId + " AND action_id " + q
					+ ") b\r\n" + "WHERE a.user_id = b.user_id AND a.latest = b.time" + sortQuery;
			Query query = session.createSQLQuery(hql).addEntity(EmailLogView.class);
			query.setFirstResult((pagination.getPageIndex() - 1) * pagination.getMaxResults());
			query.setMaxResults(pagination.getMaxResults());
			return query.list();
		} catch (Exception ex) {
			throw new XamplifyDataAccessException(ex.getMessage());
		}
	}

	@Override
	public Map<String, Object> listLeadsDetails(Integer videoId, Pagination pagination) {
		logger.debug("entered into HibernateXtremandLogDAO listLeadsDetails() mehtod");
		Session session = sessionFactory.getCurrentSession();
		/*
		 * String sql =
		 * "select {xl.*}, up.email_id, vl.firstname,  vl.lastname from xt_xtremand_log xl, xt_video_leads vl, xt_user_profile up "
		 * + " where vl.session_id =xl.session_id and vl.video_id= "
		 * +videoId+" and xl.campaign_id=0 " +
		 * " and xl.action_id=1 and vl.user_id=up.user_id and vl.user_id=xl.user_id order by xl.start_time desc"
		 * ;
		 */
		String sql = " select xl.device_type, xl.os, xl.city, xl.country, xl.isp, xl.ip_address, xl.state, xl.zip, xl.latitude, "
				+ " xl.longitude, xl.country_code, min(xl.start_time) as starttime, up.email_id, vl.firstname,  vl.lastname from "
				+ " xt_xtremand_log xl, xt_video_leads vl, xt_user_profile up where vl.session_id =xl.session_id and vl.video_id= "
				+ videoId + " and xl.campaign_id=0  and vl.user_id=up.user_id and vl.user_id=xl.user_id "
				+ " group by xl.device_type, xl.os, xl.city, xl.country, xl.isp, xl.ip_address, xl.state, xl.zip, xl.latitude, "
				+ " xl.longitude, xl.country_code, up.email_id, vl.firstname,  vl.lastname "
				+ " order by starttime desc";
		Query query = session.createSQLQuery(sql);
		ScrollableResults scrollableResults = query.scroll();
		scrollableResults.last();
		Integer totalRecords = scrollableResults.getRowNumber() + 1;
		query.setFirstResult((pagination.getPageIndex() - 1) * pagination.getMaxResults());
		query.setMaxResults(pagination.getMaxResults());
		List<Object[]> data = query.list();
		Map<String, Object> resultMap = new HashMap<String, Object>();
		resultMap.put("totalRecords", totalRecords);
		resultMap.put("data", data);
		return resultMap;
	}

	@Override
	public Integer getDataShareClickedUrlsCountForVendor(Integer campaignId, String actionId) {
		Session session = sessionFactory.getCurrentSession();
		String sql = "select count(*) from xt_email_log where action_id IN " + actionId + " and campaign_id="
				+ campaignId;
		SQLQuery query = session.createSQLQuery(sql);
		return query.uniqueResult() != null ? ((BigInteger) query.uniqueResult()).intValue() : 0;
	}

	@Override
	public List<Integer> updateAliasesUserIds() {
		Session session = sessionFactory.getCurrentSession();
		String sql = "(select  user_id from xt_user_profile up where up.alias in\n" + " (select alias\n"
				+ " from xt_user_profile\n" + " group by  1\n" + " having count(alias) >1\n"
				+ " ) order by up.alias ASC)\n" + " EXCEPT\n"
				+ " select  distinct user_id from xt_campaign_user_userlist where user_id  in\n" + " (\n"
				+ " select  up.user_id from xt_user_profile up where up.alias in\n" + " (select alias\n"
				+ " from xt_user_profile\n" + " group by  1\n" + " having count(alias) >1\n"
				+ " ) order by up.alias asc\n" + " )\n" + " EXCEPT\n"
				+ " (select distinct up.user_id  from xt_user_profile up, xt_user_role ur  \n"
				+ " where up.user_id = ur.user_id\n" + " and up.user_id IN\n" + " (\n"
				+ " select up.user_id from xt_user_profile up where up.alias in\n" + " (select alias\n"
				+ " from xt_user_profile\n" + " group by  1\n" + " having count(alias) >1\n"
				+ " ) order by up.alias asc  \n" + " )\n"
				+ " and ur.role_id in (1,2,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26) \n"
				+ " ) \n";
		SQLQuery query = session.createSQLQuery(sql);
		return query.list();
	}

	public void updateAlias(String alias, Integer userId) {
		Session session = sessionFactory.getCurrentSession();
		String sql = "update xt_user_profile set alias= '" + alias + "' where user_id=" + userId;
		SQLQuery query = session.createSQLQuery(sql);
		query.executeUpdate();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Integer> updateAliasesUserIds3() {
		Session session = sessionFactory.getCurrentSession();
		String sql = "select up.user_id from xt_user_profile up where up.alias in " + " (select alias "
				+ " from xt_user_profile " + " group by  1 " + " having count(alias) >1 "
				+ " ) order by up.alias asc   ";

		SQLQuery query = session.createSQLQuery(sql);
		return query.list();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Integer> findVideoIsNotPlayedEmailNotificationOpenedUserIdsByCampaignIdAndVideoIdAndReplyId(
			Integer campaignId, Integer videoId, Integer replyId) {
		List<Integer> userIds = new ArrayList<Integer>();
		Session session = sessionFactory.getCurrentSession();
		String queryString = "select distinct user_id from xt_email_log where video_id = :videoId  and campaign_id=:campaignId "
				+ " and action_id=:actionId and reply_id=:replyId";
		SQLQuery query = session.createSQLQuery(queryString);
		query.setParameter("videoId", videoId);
		query.setParameter("campaignId", campaignId);
		query.setParameter("videoId", videoId);
		query.setParameter("replyId", replyId);
		query.setParameter("actionId", AutoResponseOption.VIDEO_IS_NOT_PLAYED);
		userIds = query.list();
		return (List<Integer>) XamplifyUtils.returnQueryList(userIds);

	}

	private VideoFile findVideoFileByAlias(String alias) {
		Session session = sessionFactory.getCurrentSession();
		Criteria criteria = session.createCriteria(VideoFile.class);
		criteria.add(Restrictions.eq("alias", alias));
		return (VideoFile) criteria.uniqueResult();
	}

}