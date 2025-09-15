package com.xtremand.sf.cf.data.dao.hibernate;

import java.math.BigInteger;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.xtremand.deal.bom.Deal;
import com.xtremand.form.bom.FormLabel;
import com.xtremand.lead.bom.Lead;
import com.xtremand.salesforce.bom.SfCustomFieldsData;
import com.xtremand.sf.cf.data.dao.SfCustomFormDataDAO;
import com.xtremand.user.exception.UserDataAccessException;

@Repository
public class HibernateSfCustomFormDAO implements SfCustomFormDataDAO {

	private static final Logger logger = LoggerFactory.getLogger(HibernateSfCustomFormDAO.class);
	
	@Autowired
	SessionFactory sessionFactory;

	@Override
	public void saveSfCfData(SfCustomFieldsData sfCustomFieldsData) {
		sessionFactory.getCurrentSession().save(sfCustomFieldsData);
	}
	
	@Override
	public void updateSfCfData(SfCustomFieldsData sfCustomFieldsData) {
		sessionFactory.getCurrentSession().update(sfCustomFieldsData);
	}

	@Override
	public void deleteCustomFormLabelByFieldId(Integer labelId) {
		Session session = sessionFactory.getCurrentSession();
		String sql = "delete from xt_sf_cf_data where sf_cf_label_id = " + labelId + "";
		session.createSQLQuery(sql).executeUpdate();
	}
	
	@Override
	public void save(List<SfCustomFieldsData> sfCfFieldsData) {
		try {
			Session session = sessionFactory.getCurrentSession();
			for(int i=0;i<sfCfFieldsData.size();i++){
				session.save(sfCfFieldsData.get(i));
				if(i%30==0){
					session.flush();
					session.clear();
				}
			}
		} catch (HibernateException | UserDataAccessException e) {
			logger.error("Error In save("+sfCfFieldsData.toString()+")",e);
			throw new UserDataAccessException(e.getMessage());
		} catch (Exception e) {
			logger.error("Error In save("+sfCfFieldsData.toString()+")",e);
			throw new UserDataAccessException(e.getMessage());
		}
	}
	
	@Override
	public void update(List<SfCustomFieldsData> sfCfFieldsData) {
		try {
			Session session = sessionFactory.getCurrentSession();
			for(int i=0;i<sfCfFieldsData.size();i++){
				session.save(sfCfFieldsData.get(i));
				if(i%30==0){
					session.flush();
					session.clear();
				}
			}
		} catch (HibernateException | UserDataAccessException e) {
			logger.error("Error In save("+sfCfFieldsData.toString()+")",e);
			throw new UserDataAccessException(e.getMessage());
		} catch (Exception e) {
			logger.error("Error In save("+sfCfFieldsData.toString()+")",e);
			throw new UserDataAccessException(e.getMessage());
		}
	}


	@Override
	public SfCustomFieldsData getSfCustomFieldDataByDealIdAndLabelId(Deal deal, FormLabel formLabel) {
		Session session = sessionFactory.getCurrentSession();
		return (SfCustomFieldsData) session.createCriteria(SfCustomFieldsData.class).add(Restrictions.eq("deal", deal)).add(Restrictions.eq("formLabel", formLabel)).uniqueResult();
	}

	@Override
	public boolean checkIfRecordExists(Integer formLabelId) {
		Session session = sessionFactory.getCurrentSession();
		String sql = "select count(*) from xt_sf_cf_data where sf_cf_label_id = :formLabelId";
		BigInteger count = (BigInteger) session.createSQLQuery(sql).setParameter("formLabelId", formLabelId)
				.uniqueResult();
		return count.intValue() > 0;
	}
	
	//XNFR-615
	@Override
	public SfCustomFieldsData getSfCustomFieldDataByLeadIdAndLabelId(Lead lead, FormLabel formLabel) {
		Session session = sessionFactory.getCurrentSession();
		return (SfCustomFieldsData) session.createCriteria(SfCustomFieldsData.class).add(Restrictions.eq("lead", lead)).add(Restrictions.eq("formLabel", formLabel)).uniqueResult();
	}
}
