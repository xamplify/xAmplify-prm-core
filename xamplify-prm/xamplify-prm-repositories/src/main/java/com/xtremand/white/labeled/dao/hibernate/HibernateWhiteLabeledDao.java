package com.xtremand.white.labeled.dao.hibernate;

import java.util.Collections;
import java.util.List;

import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.xtremand.util.PaginationUtil;
import com.xtremand.white.labeled.dao.WhiteLabeledDao;
import com.xtremand.white.labeled.dto.WhiteLabeledContentDTO;

@Repository
@Transactional
public class HibernateWhiteLabeledDao implements WhiteLabeledDao {

	@Autowired
	private SessionFactory sessionFactory;

	@Autowired
	private PaginationUtil paginationUtil;

	@Override
	public WhiteLabeledContentDTO findSharedByVendorCompanyNameByContentId(Integer id, String tableName,
			String columnName) {
		Session session = sessionFactory.getCurrentSession();
		String queryString = "select c.company_name as \"whiteLabeledContentSharedByVendorCompanyName\" " + "from "
				+ tableName + " e,xt_company_profile c\r\n" + "where e.shared_by_vendor_company_id = c.company_id and "
				+ "e." + columnName + " = :id ";
		SQLQuery query = session.createSQLQuery(queryString);
		query.setParameter("id", id);
		return (WhiteLabeledContentDTO) paginationUtil.getDto(WhiteLabeledContentDTO.class, query);
	}

	@Override
	public boolean isVendorCompanyContentWhiteLabeledAndSharedWithPartners(Integer id, String tableName,
			String columnName) {
		Session session = sessionFactory.getCurrentSession();
		SQLQuery query = session.createSQLQuery("select case when count(*)>0 then true else false end from " + tableName
				+ " where " + columnName + " =:id");
		query.setParameter("id", id);
		return (boolean) query.uniqueResult();
	}

	@Override
	public boolean isWhiteLabeledContentSharedWithPartnerCompany(Integer contentId, Integer vendorCompanyId,
			Integer partnerCompanyId, String tableName, String vendorCompanyEmailTemplateIdColumnName) {
		Session session = sessionFactory.getCurrentSession();
		SQLQuery query = session.createSQLQuery("select case when count(*)>0 then true else false end from " + tableName
				+ " where " + vendorCompanyEmailTemplateIdColumnName
				+ " =:id and shared_by_vendor_company_id  = :vendorCompanyId and shared_with_partner_company_id = :partnerCompanyId");
		query.setParameter("id", contentId);
		query.setParameter("vendorCompanyId", vendorCompanyId);
		query.setParameter("partnerCompanyId", partnerCompanyId);
		return (boolean) query.uniqueResult();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Integer> findPartnerCompanyIdsByContentId(Integer assetId, String tableName, String columnName) {
		Session session = sessionFactory.getCurrentSession();
		String sqlString = "select distinct shared_with_partner_company_id from "+tableName+" where "+columnName+" = :assetId";
		SQLQuery query = session.createSQLQuery(sqlString);
		query.setParameter("assetId", assetId);
		return  query.list()!=null ? (List<Integer>) query.list():Collections.emptyList();
	}

}
