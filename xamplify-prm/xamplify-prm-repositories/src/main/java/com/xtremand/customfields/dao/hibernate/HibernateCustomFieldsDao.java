package com.xtremand.customfields.dao.hibernate;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.xtremand.customfields.dao.CustomFieldsDao;
import com.xtremand.form.bom.FormLabel;
import com.xtremand.form.bom.FormTypeEnum;
import com.xtremand.salesforce.dto.OpportunityFormFieldsDTO;
import com.xtremand.util.PaginationUtil;

@Repository
@Transactional
public class HibernateCustomFieldsDao implements CustomFieldsDao {


	@Autowired
	SessionFactory sessionFactory;

	@Autowired
	private PaginationUtil paginationUtil;

	@Override
	public boolean checkFormExistByFormType(Integer companyId, FormTypeEnum formType) {
		Session session = sessionFactory.getCurrentSession();
		String sqlQuery = "select exists (select 1 from xt_form where company_id = :companyId and form_type = '"
				+ formType + "' and id is not null) as hasFrom";
		Query query = session.createSQLQuery(sqlQuery);
		query.setParameter("companyId", companyId);
		return (boolean) query.uniqueResult();
	}

	@Override
	public void updateFormFields(Integer labelId) {
		Session session = sessionFactory.getCurrentSession();
		String sql = "update xt_form_label set is_active = 'false',is_required = 'false' where  id = :labelId";
		Query query = session.createSQLQuery(sql);
		query.setParameter("labelId", labelId);
		query.executeUpdate();
	}

	@Override
	public void deleteCustomField(Integer id) {
		Session session = sessionFactory.getCurrentSession();
		String sql = "delete from xt_form_label where id = :id";
		Query query = session.createSQLQuery(sql);
		query.setParameter("id", id);
		query.executeUpdate();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<OpportunityFormFieldsDTO> getDefaultLeadFilds() {
		String sql = "SELECT id AS \"id\", " + "label_name AS \"label\", " + "label_id AS \"name\", "
				+ "label_type AS \"type\", " + "true AS \"selected\", " + "CASE "
				+ "WHEN label_id IN ('Last_Name', 'Email', 'Company') THEN true " + "    ELSE false "
				+ "END AS \"required\", " + "CASE " + "WHEN label_id IN ('Last_Name', 'Email', 'Company') THEN false "
				+ "ELSE true " + "END AS \"canEditRequired\", " + "CASE "
				+ "WHEN label_id IN ('Last_Name', 'Email', 'Company') THEN false " + "    ELSE true "
				+ "END AS \"canUnselect\", " + "true AS \"isActive\", " + "false AS \"nonInteractive\", "
				+ "false AS \"isPrivate\", " + "label_name AS \"displayName\" " + "FROM xt_lead_fields "
				+ "ORDER BY id";
		Session session = sessionFactory.getCurrentSession();
		Query query = session.createSQLQuery(sql);
		List<OpportunityFormFieldsDTO> fieldsList = (List<OpportunityFormFieldsDTO>) paginationUtil
				.getListDTO(OpportunityFormFieldsDTO.class, query);
		for (int i = 0; i < fieldsList.size(); i++) {
			OpportunityFormFieldsDTO field = fieldsList.get(i);
			field.setOrder(i + 1);
		}
		return fieldsList;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<FormLabel> getFormLabelsByFormId(Integer formId) {
		Session session = sessionFactory.getCurrentSession();
		Criteria criteria = session.createCriteria(FormLabel.class);
		criteria.add(Restrictions.eq("form.id", formId));
		return criteria.list();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<FormLabel> getSelectedFormLabelsByFormId(Integer formId) {
		Session session = sessionFactory.getCurrentSession();
		Criteria criteria = session.createCriteria(FormLabel.class);
		criteria.add(Restrictions.eq("form.id", formId));
		criteria.add(Restrictions.eq("isActive", true));
		criteria.addOrder(Order.asc("order"));
		return criteria.list();
	}

	@Override
	public Integer getLeadCountForCustomField(Integer customFieldId) {
		Session session = sessionFactory.getCurrentSession();
		String sql = "SELECT cast (COUNT(*) as integer) FROM xt_sf_cf_data WHERE sf_cf_label_id = :customFieldId and value is not null";
		Query query = session.createSQLQuery(sql);
		query.setParameter("customFieldId", customFieldId);
		return (Integer) query.uniqueResult();
	}

	@Override
	public List<OpportunityFormFieldsDTO> getDealDefaultCustomFieldsDto() {
		String sql = "SELECT id AS \"id\", " + "label_name AS \"label\", " + "label_id AS \"name\", "
				+ "label_type AS \"type\", " + "true AS \"selected\", " + "CASE "
				+ "WHEN label_id IN ('Deal_Name', 'Amount', 'Close_Date') THEN true " + "    ELSE false "
				+ "END AS \"required\", " + "CASE " + "WHEN label_id IN ('Deal_Name', 'Amount', 'Close_Date') THEN false "
				+ "ELSE true " + "END AS \"canEditRequired\", " + "CASE "
				+ "WHEN label_id IN ('Deal_Name', 'Amount', 'Close_Date') THEN false " + "    ELSE true "
				+ "END AS \"canUnselect\", " + "true AS \"isActive\", " + "false AS \"nonInteractive\", "
				+ "false AS \"isPrivate\", " + "label_name AS \"displayName\" " + "FROM xt_deal_fields "
				+ "ORDER BY id";
		Session session = sessionFactory.getCurrentSession();
		Query query = session.createSQLQuery(sql);
		List<OpportunityFormFieldsDTO> fieldsList = (List<OpportunityFormFieldsDTO>) paginationUtil
				.getListDTO(OpportunityFormFieldsDTO.class, query);
		for (int i = 0; i < fieldsList.size(); i++) {
			OpportunityFormFieldsDTO field = fieldsList.get(i);
			field.setOrder(i + 1);
		}
		return fieldsList;
	}

}
