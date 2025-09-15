package com.xtremand.gdpr.setting.dao.hibernate;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Disjunction;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.xtremand.gdpr.setting.bom.GdprSetting;
import com.xtremand.gdpr.setting.bom.GdprSettingView;
import com.xtremand.gdpr.setting.dao.GdprSettingDao;
import com.xtremand.gdpr.setting.exception.GdprSettingDataAccessException;
import com.xtremand.user.bom.LegalBasis;

@Repository
@Transactional
public class HibernateGdprSettingDao implements GdprSettingDao {
	
	@Autowired
	private SessionFactory sessionFactory;

	@Override
	public void save(GdprSetting gdprSetting) {
		try {
			sessionFactory.getCurrentSession().save(gdprSetting);
		} catch (HibernateException e) {
			throw new GdprSettingDataAccessException(e);
		}catch (Exception ex) {
			throw new GdprSettingDataAccessException(ex);
		}
	}

	@Override
	public GdprSettingView getByCompanyId(Integer companyId) {
		try {
			Session session = sessionFactory.getCurrentSession();
			return (GdprSettingView) session.createCriteria(GdprSettingView.class).add(Restrictions.eq("companyId", companyId)).uniqueResult();
		} catch (HibernateException e) {
			throw new GdprSettingDataAccessException(e);
		}catch (Exception ex) {
			throw new GdprSettingDataAccessException(ex);
		}
	}

	@Override
	public GdprSetting getSettingByCompanyId(Integer companyId) {
		try {
			Session session = sessionFactory.getCurrentSession();
			return (GdprSetting) session.createCriteria(GdprSetting.class).add(Restrictions.eq("companyProfile.id", companyId)).uniqueResult();
		} catch (HibernateException e) {
			throw new GdprSettingDataAccessException(e);
		}catch (Exception ex) {
			throw new GdprSettingDataAccessException(ex);
		}
	}

	@Override
	public List<LegalBasis> getLegalBasisAddedByCompanyId(Integer companyId) {
		try {
			Session session = sessionFactory.getCurrentSession();
			Criteria criteria = session.createCriteria(LegalBasis.class);
			criteria.add(Restrictions.eq("company.id", companyId));
			return (List<LegalBasis>) criteria.list();
		} catch (HibernateException e) {
			throw new GdprSettingDataAccessException(e);
		}catch (Exception ex) {
			throw new GdprSettingDataAccessException(ex);
		}
	}
	
	// This method returns Default legal basis along with legal basis added by company
	@Override
	public List<LegalBasis> getLegalBasisListForCompany(Integer companyId) {
		try {
			Session session = sessionFactory.getCurrentSession();
			Criteria criteria = session.createCriteria(LegalBasis.class);
			Disjunction disjunction = Restrictions.disjunction();
			disjunction.add(Restrictions.eq("isDefault",true));
			disjunction.add(Restrictions.eq("company.id",companyId));
			return (List<LegalBasis>) criteria.add(disjunction).list();
		} catch (HibernateException e) {
			throw new GdprSettingDataAccessException(e);
		}catch (Exception ex) {
			throw new GdprSettingDataAccessException(ex);
		}
	}

	@Override
	public void deleteLegalBasis(LegalBasis legalbasis) {
		Session session = sessionFactory.getCurrentSession();
		session.delete(legalbasis);
	}

	@Override
	public List<LegalBasis> getSelectByDefaultLegalBasis() {
		Session session = sessionFactory.getCurrentSession();
		Criteria criteria = session.createCriteria(LegalBasis.class);
		criteria.add(Restrictions.eq("isDefault", true));
		criteria.add(Restrictions.eq("isSelectByDefault", true));
		return (List<LegalBasis>) criteria.list();
	}
	
	@Override
	public void removeLegalBasis(List<Integer> userIdsList, List<Integer> userListIds){
		Session session = sessionFactory.getCurrentSession();
		Query query = session.createSQLQuery("delete from xt_user_legal_basis where user_id in (:userIds) and user_list_id in (:userListIds)");
		query.setParameterList("userIds", userIdsList);
		query.setParameterList("userListIds", userListIds);
		query.executeUpdate();
	}

}
