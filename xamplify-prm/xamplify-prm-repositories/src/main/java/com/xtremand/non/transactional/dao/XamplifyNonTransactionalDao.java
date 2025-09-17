package com.xtremand.non.transactional.dao;

import java.util.Set;

import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.xtremand.util.XamplifyUtils;

@Repository
public class XamplifyNonTransactionalDao {

	@Autowired
	private SessionFactory sessionFactory;

	public void updateIsPublishedToPartnerListByIds(Set<Integer> damIds, boolean value) {
		if (XamplifyUtils.isNotEmptySet(damIds)) {
			String queryString = "update xt_dam set is_publishing_to_partner_list = :value where id in(:ids)";
			Session session = sessionFactory.getCurrentSession();
			SQLQuery query = session.createSQLQuery(queryString);
			query.setParameter("value", value);
			query.setParameterList("ids", damIds);
			query.executeUpdate();
		}

	}

}
