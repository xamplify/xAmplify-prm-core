package com.xtremand.drip.email.hibernate;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.SQLQuery;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.xtremand.drip.email.dao.VendroDripEmailDAO;
import com.xtremand.drip.email.exception.DripEmailException;
import com.xtremand.user.bom.Role;
import com.xtremand.user.exception.UserDataAccessException;
import com.xtremand.util.DripEmailConstants;

@Repository("vendorDripEmailDAO")
public class HibernateVendorDripEmailDAO implements VendroDripEmailDAO {

	private static final Logger logger = LoggerFactory.getLogger(HibernateVendorDripEmailDAO.class);

	private static final String ADMIN_ROLES = " (" + Role.PRM_ROLE.getRoleId() + ")";

	private static final String APPORVE_STATUS = "'APPROVE'";

	@Autowired
	SessionFactory sessionFactory;

	@SuppressWarnings("unchecked")
	@Override
	public List<Object[]> listAllIntroEmailIds(Timestamp presentTs) {
		try {
			String userStatus = APPORVE_STATUS;
			String sqlQuery = "select distinct v_up.email_id,v_up.user_id from xt_user_profile v_up,xt_user_role r,xt_drip_email_history v_deh where v_up.user_id not in (select user_id from xt_drip_email_history  where action_id= "
					+ DripEmailConstants.VENDOR_CSM_INTRO_AT + ") and DATE_PART('day', :presentTs - v_deh.sent_time)= "
					+ DripEmailConstants.VENDOR_INTRO_EMAIL_DAYS + " and r.role_id in  " + ADMIN_ROLES
					+ " and v_up.user_id = r.user_id and v_up.user_id = v_deh.user_id and v_deh.action_id="
					+ DripEmailConstants.VENDOR_WELCOME_AT + " and v_up.status=" + userStatus + "";
			SQLQuery query = sessionFactory.getCurrentSession().createSQLQuery(sqlQuery);
			query.setTimestamp("presentTs", presentTs);
			return query.list();
		} catch (HibernateException | DripEmailException e) {
			logger.error("Error In listAllIntroEmailIds()", e);
			throw new UserDataAccessException(e.getMessage());
		} catch (Exception ex) {
			logger.error("Error In listAllIntroEmailIds()", ex);
			throw new DripEmailException(ex.getMessage());
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Object[]> listAllKickOffEmailIds(Timestamp presentTs) {
		try {
			String sqlQuery = "select distinct v_up.email_id,v_deh.user_id from  xt_user_profile v_up,xt_user_role v_r,xt_drip_email_history v_deh where v_deh.user_id not in (select user_id from xt_drip_email_history  where action_id= "
					+ DripEmailConstants.VENDOR_KICK_OFF_AT + ") and DATE_PART('day', :presentTs - v_deh.sent_time)="
					+ DripEmailConstants.VENDOR_KICK_OFF_EMAIL_DAYS + " and v_r.role_id in " + ADMIN_ROLES
					+ " and v_up.user_id = v_r.user_id and v_deh.user_id = v_up.user_id and v_deh.action_id="
					+ DripEmailConstants.VENDOR_CSM_INTRO_AT + "";
			SQLQuery query = sessionFactory.getCurrentSession().createSQLQuery(sqlQuery);
			query.setTimestamp("presentTs", presentTs);
			return query.list();
		} catch (HibernateException | DripEmailException e) {
			logger.error("Error In listAllKickOffEmailIds()", e);
			throw new UserDataAccessException(e.getMessage());
		} catch (Exception ex) {
			logger.error("Error In listAllKickOffEmailIds()", ex);
			throw new DripEmailException(ex.getMessage());
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Object[]> listAllInCompleteProfileEmailIds(Timestamp presentTs) {
		try {
			String userStatus = APPORVE_STATUS;
			String sqlQuery = "select v_up.email_id, v_up.user_id from xt_user_profile v_up, xt_user_role v_r,xt_drip_email_history v_deh where DATE_PART('day', :presentTs - v_deh.sent_time)="
					+ DripEmailConstants.VENDOR_IN_COMPLETE_PROFILE_EMAIL_DAYS + " and v_r.role_id in" + ADMIN_ROLES
					+ " and v_up.status=" + userStatus
					+ " and v_up.company_id IS NULL and v_up.user_id = v_r.user_id and v_up.user_id = v_deh.user_id and v_deh.action_id="
					+ DripEmailConstants.VENDOR_WELCOME_AT + " group by 1,2";
			SQLQuery query = sessionFactory.getCurrentSession().createSQLQuery(sqlQuery);
			query.setTimestamp("presentTs", presentTs);
			return query.list();
		} catch (HibernateException | DripEmailException e) {
			logger.error("Error In listAllInCompleteProfileEmailIds()", e);
			throw new UserDataAccessException(e.getMessage());
		} catch (Exception ex) {
			logger.error("Error In listAllInCompleteProfileEmailIds()", ex);
			throw new DripEmailException(ex.getMessage());
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Object[]> listAllInCompleteProfileEmailIdsOfWeek(Timestamp presentTs) {
		try {
			String userStatus = APPORVE_STATUS;
			String sqlQuery = "select v_up.email_id,deh.user_id from (select v_deh.user_id user_id, max(v_deh.sent_time) sent_time,count(v_deh.sent_time) from xt_drip_email_history v_deh where v_deh.action_id = "
					+ DripEmailConstants.VENDOR_INCOMPLETE_PROFILE_AT
					+ " group by 1 having count(v_deh.sent_time) < 5) deh, xt_user_profile v_up, xt_user_role v_r where DATE_PART('day', :presentTs - deh.sent_time)= "
					+ DripEmailConstants.ONE_WEEK_DAYS + " and v_r.role_id in " + ADMIN_ROLES + " and v_up.status="
					+ userStatus
					+ " and v_up.company_id IS NULL and v_up.user_id = v_r.user_id and v_up.user_id = deh.user_id order by 2";
			SQLQuery query = sessionFactory.getCurrentSession().createSQLQuery(sqlQuery);
			query.setTimestamp("presentTs", presentTs);
			return query.list();
		} catch (HibernateException | DripEmailException e) {
			logger.error("Error In listAllInCompleteProfileEmailIdsOfWeek()", e);
			throw new UserDataAccessException(e.getMessage());
		} catch (Exception ex) {
			logger.error("Error In listAllInCompleteProfileEmailIdsOfWeek()", ex);
			throw new DripEmailException(ex.getMessage());
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Object[]> listAllInActiveProfileEmailIdsOfWeek(Timestamp presentTs, Integer weeksCount) {
		try {
			String userStatus = APPORVE_STATUS;
			String sqlQuery = "select distinct v_up.email_id,v_up.user_id from xt_user_profile v_up, xt_user_role v_r,xt_drip_email_history v_deh where DATE_PART('day', :presentTs - v_deh.sent_time)="
					+ DripEmailConstants.ONE_WEEK_DAYS * weeksCount + " and v_r.role_id in " + ADMIN_ROLES
					+ " and v_up.status=" + userStatus
					+ " and v_up.company_id IS NULL and v_up.user_id = v_r.user_id and v_up.user_id = v_deh.user_id and v_deh.action_id="
					+ DripEmailConstants.VENDOR_WELCOME_AT + "";
			SQLQuery query = sessionFactory.getCurrentSession().createSQLQuery(sqlQuery);
			query.setTimestamp("presentTs", presentTs);
			return query.list();
		} catch (HibernateException | DripEmailException e) {
			logger.error("Error In listAllInActiveProfileEmailIdsOfWeek()", e);
			throw new UserDataAccessException(e.getMessage());
		} catch (Exception ex) {
			logger.error("Error In listAllInActiveProfileEmailIdsOfWeek()", ex);
			throw new DripEmailException(ex.getMessage());
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Object[]> updateAllInActiveProfileEmailIds(Timestamp presentTs, Integer weekCount) {
		try {
			String userStatus = APPORVE_STATUS;
			String sqlQuery = "(select distinct v_up.email_id,v_up.user_id from xt_user_profile v_up , xt_user_role v_r,xt_drip_email_history v_deh where v_up.email_id IS NOT NULL and v_up.status ="
					+ userStatus + " and v_up.company_id IS NULL and v_r.role_id in " + ADMIN_ROLES
					+ " and v_up.user_id = v_r.user_id and v_up.user_id=v_deh.user_id and v_deh.action_id ="
					+ DripEmailConstants.VENDOR_WELCOME_AT + " and DATE_PART('day',:presentTs - v_deh.sent_time) = "
					+ DripEmailConstants.ONE_WEEK_DAYS * weekCount + ")";
			SQLQuery query = sessionFactory.getCurrentSession().createSQLQuery(sqlQuery);
			query.setTimestamp("presentTs", presentTs);
			List<Object[]> usersList = query.list();
			if (!usersList.isEmpty()) {
				List<Integer> userIds = new ArrayList<>();
				for (Object[] user : usersList) {
					userIds.add(Integer.parseInt(String.valueOf(user[1])));
				}
				String sqlQuery1 = "update xt_user_profile set status='DISABLED' where user_id in (:userIds)";
				query = sessionFactory.getCurrentSession().createSQLQuery(sqlQuery1);
				query.setParameterList("userIds", userIds);
				query.executeUpdate();
			}
			return usersList;
		} catch (HibernateException | DripEmailException e) {
			logger.error("Error In updateAllInActiveProfileEmailIds()", e);
			throw new UserDataAccessException(e.getMessage());
		} catch (Exception ex) {
			logger.error("Error In updateAllInActiveProfileEmailIds()", ex);
			throw new DripEmailException(ex.getMessage());
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Object[]> listAllNotOnBoardedPartnersEmailIds(Timestamp presentTs, Integer actionType,
			Integer noOfDays) {
		try {
			String userStatus = APPORVE_STATUS;
			String sqlQuery = "select distinct v_up.email_id,v_up.user_id from xt_user_profile v_up, xt_user_role v_r,xt_drip_email_history v_deh where DATE_PART('day', :presentTs - v_deh.sent_time)="
					+ noOfDays + " and v_r.role_id NOT IN (" + Role.COMPANY_PARTNER.getRoleId() + ","
					+ Role.USER_ROLE.getRoleId() + ") and v_up.status=" + userStatus
					+ " and v_up.company_id IS NOT NULL and v_up.user_id = v_r.user_id and v_up.user_id = v_deh.user_id and v_deh.action_id="
					+ actionType
					+ " and v_up.company_id NOT IN (select vendor_company_id from xt_partnership  where vendor_company_id IS NOT NULL)";
			SQLQuery query = sessionFactory.getCurrentSession().createSQLQuery(sqlQuery);
			query.setTimestamp("presentTs", presentTs);
			return query.list();
		} catch (HibernateException | DripEmailException e) {
			logger.error("Error In listAllNotOnBoardedPartnersEmailIds()", e);
			throw new UserDataAccessException(e.getMessage());
		} catch (Exception ex) {
			logger.error("Error In listAllNotOnBoardedPartnersEmailIds()", ex);
			throw new DripEmailException(ex.getMessage());
		}
	}
}
