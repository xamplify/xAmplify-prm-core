package com.xtremand.notification.dao;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.xtremand.common.bom.Notification;

@Repository("notificationDAO")
@Transactional
public class NotificationDAO {

	static final Logger logger = LoggerFactory.getLogger(NotificationDAO.class);

	@Autowired
	SessionFactory sessionFactory;

	@SuppressWarnings("unchecked")
	public List<Notification> listNotifications(Integer userId) {
		try {
			Session session = sessionFactory.getCurrentSession();
			Criteria criteria = session.createCriteria(Notification.class);
			criteria.add(Restrictions.eq("userId", userId));
			criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
			criteria.addOrder(Order.desc("createdTime"));
			return criteria.list();
		} catch (Exception exception) {
			logger.error(exception.getMessage());
			throw exception;
		}
	}

	public void markAllAsRead(Integer userId) {
		Session session = sessionFactory.getCurrentSession();
		String hql = "UPDATE Notification SET isRead = :isRead WHERE userId = :userId";
		Query query = session.createQuery(hql);
		query.setParameter("userId", userId);
		query.setParameter("isRead", true);
		query.executeUpdate();
	}

	public Integer unreadCount(Integer userId) {
		Session session = sessionFactory.getCurrentSession();
		Criteria criteria = session.createCriteria(Notification.class);
		criteria.add(Restrictions.eq("userId", userId));
		criteria.add(Restrictions.eq("isRead", false));
		criteria.setProjection(Projections.rowCount());
		return criteria.uniqueResult() != null ? ((Long) criteria.uniqueResult()).intValue() : 0;
	}

	public void markAsRead(Integer id) {
		Session session = sessionFactory.getCurrentSession();
		String hql = "UPDATE Notification SET isRead = :isRead WHERE id = :id";
		Query query = session.createQuery(hql);
		query.setParameter("id", id);
		query.setParameter("isRead", true);
		query.executeUpdate();
	}

}
