package com.xtremand.godaddy.dao.hibernate;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.xtremand.godaddy.dao.GoDaddyConfigurationDao;

@Repository
@Transactional
public class HibernateGodaddyConfigurationDao implements GoDaddyConfigurationDao{
	
	@Autowired
	SessionFactory sessionFactory;
	
	@Override
	public void updateGodaddyConfiguration(Integer companyId,boolean isConnected, String domainName) {
		Session session = sessionFactory.getCurrentSession();
		String sql = "update xt_company_profile set is_domain_connected = :isConnected,godaddy_domain_name = :domainName where company_id = :companyId";
		Query query = session.createSQLQuery(sql);
		query.setParameter("companyId", companyId);
		query.setParameter("domainName", domainName);
		query.setParameter("isConnected", isConnected);
		//query.setParameter("isSpfConfigured", isConnected);
		query.executeUpdate();
	}

	@Override
	public boolean isGodaddyConfigured(Integer companyId) {
		Session session = sessionFactory.getCurrentSession();
		String sql = "select is_domain_connected from xt_company_profile where company_id=:companyId";
		return (boolean) session.createSQLQuery(sql).setParameter("companyId", companyId).uniqueResult();

	}
	
	@Override
	public String getDomainName(Integer companyId) {
		Session session = sessionFactory.getCurrentSession();
		String sql = "select godaddy_domain_name from xt_company_profile where company_id=:companyId";
		return (String) session.createSQLQuery(sql).setParameter("companyId", companyId).uniqueResult();
	}

}
