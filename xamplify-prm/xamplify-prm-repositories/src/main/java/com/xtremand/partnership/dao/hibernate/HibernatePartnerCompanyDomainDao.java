package com.xtremand.partnership.dao.hibernate;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.xtremand.partnership.dao.PartnerCompanyDomainDao;


@Repository("PartnerCompanyDomainDao")
@Transactional
public class HibernatePartnerCompanyDomainDao implements PartnerCompanyDomainDao{
	
	@Autowired
	private SessionFactory sessionFactory;
}
