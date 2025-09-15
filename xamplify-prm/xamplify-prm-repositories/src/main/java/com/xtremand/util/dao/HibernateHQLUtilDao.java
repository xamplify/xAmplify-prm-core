package com.xtremand.util.dao;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public class HibernateHQLUtilDao {

	@Autowired
	private SessionFactory sessionFactory;

	void delete() {
		Session session = sessionFactory.getCurrentSession();
		String hqlString = "delete  from Workflow where id=:id";
		Query query = session.createQuery(hqlString);
		query.executeUpdate();

	}

}
