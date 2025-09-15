package com.xtremand.selectedfields.dao.hibernate;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.xtremand.form.bom.FormTypeEnum;
import com.xtremand.form.bom.SelectedFields;
import com.xtremand.form.dto.SelectedFieldsDTO;
import com.xtremand.selectedfields.dao.SelectedFieldsDAO;
import com.xtremand.util.PaginationUtil;

@Repository
public class HibernateSelectedFieldsDAO implements SelectedFieldsDAO {

	private static final String FORMID = "formId";
	private static final String OPPORTUNITY_TYPE = "opportunityType";
	@Autowired
	private SessionFactory sessionFactory;

	@Autowired
	private PaginationUtil paginationUtil;

	@SuppressWarnings("unchecked")
	@Override
	public List<SelectedFieldsDTO> getAllSelectedFieldsByLoggedInuserIdAndOpportunity(Integer loggedInUserId,
			Integer formId, String opportunityType) {
		Session session = sessionFactory.getCurrentSession();
		String sql = "SELECT id as \"id\", label_name as \"labelName\", display_name as \"displayName\", label_id as \"labelId\", form_id as \"formId\", integration_id as \"integrationId\", column_order as \"columnOrder\", is_selected_column as \"selectedColumn\",my_preferences_enabled as \"myPreferencesEnabled\", is_default_column as \"defaultColumn\"  "
				+ "FROM public.xt_selected_fields where created_user_id=:loggedInUserId and form_id =:formId and opportunity_type=CAST(:opportunityType AS opportunity_type) order by column_order ";
		Query query = session.createSQLQuery(sql);
		query.setParameter("loggedInUserId", loggedInUserId);
		query.setParameter(FORMID, formId);
		query.setParameter(OPPORTUNITY_TYPE, opportunityType);
		return (List<SelectedFieldsDTO>) paginationUtil.getListDTO(SelectedFieldsDTO.class, query);
	}

	@Override
	public SelectedFields getSelectedFieldByLableId(String lableId,Integer loggedInUserId, Integer formId, String opportunityType) {
		Session session = sessionFactory.getCurrentSession();
		Criteria criteria = session.createCriteria(SelectedFields.class);
		criteria.add(Restrictions.eq("labelId", lableId));
		criteria.add(Restrictions.eq("form.id", formId));
		criteria.add(Restrictions.eq("createdBy.id", loggedInUserId));
		SelectedFields result = (SelectedFields) criteria.uniqueResult();
		return result != null ? result : new SelectedFields();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<SelectedFieldsDTO> getFormFieldsByFormId(Integer formId, String userType, FormTypeEnum formType) {
		Session session = sessionFactory.getCurrentSession();
		String sql = frameFormLabelDtoQueryString();
		sql += userType.equals("v") ? " " : "and xfl.is_private = false";
		sql += FormTypeEnum.XAMPLIFY_LEAD_CUSTOM_FORM == formType || FormTypeEnum.XAMPLIFY_DEAL_CUSTOM_FORM == formType
				? " and  xfl.is_active = true  "
				: " ";
		Query query = session.createSQLQuery(sql).setParameter(FORMID, formId);
		return (List<SelectedFieldsDTO>) paginationUtil.getListDTO(SelectedFieldsDTO.class, query);
	}

	private String frameFormLabelDtoQueryString() {
		return " SELECT  xfl.label_name AS \"labelName\", xfl.label_id AS \"labelId\", "
				+ " xfl.display_name AS \"displayName\",xfl.form_id AS \"formId\",0 AS \"integrationId\", true as \"selectedColumn\", column_order as \"columnOrder\",  xfl.form_default_field_type as  \"formDefaultFieldType\"  "
				+ " from xt_form_label xfl JOIN  xt_form_label_type xft ON xfl.label_type = xft.id where xfl.form_id = :formId ";
	}

	@Override
	public boolean isMyPreferances(Integer userId, Integer formId, String opportunityType) {
		Session session = sessionFactory.getCurrentSession();
		String sql = "select case when count(*)>0 then true else false end from xt_selected_fields where created_user_id =:userId and my_preferences_enabled = true and form_id=:formId and opportunity_type=CAST(:opportunityType AS opportunity_type) ; ";
		Query query = session.createSQLQuery(sql).setParameter("userId", userId).setParameter(FORMID, formId)
				.setParameter(OPPORTUNITY_TYPE, opportunityType);
		return (boolean) query.uniqueResult();
	}

	@Override
	public Integer deleteUnSelectedFieldsByIds(List<Integer> unselectedIds) {
		String sqlQuery = "DELETE FROM  xt_selected_fields WHERE id in (:unselectedIds);";
		Session session = sessionFactory.getCurrentSession();
		Query query = session.createSQLQuery(sqlQuery);
		query.setParameterList("unselectedIds", unselectedIds);
		return query.executeUpdate();

	}

	@Override
	public boolean isFieldsByFormId(Integer formId, String opportunityType) {
		Session session = sessionFactory.getCurrentSession();
		String sql = "select case when count(*)>0 then true else false end from xt_selected_fields where form_id=:formId and opportunity_type=CAST(:opportunityType AS opportunity_type) ; ";
		Query query = session.createSQLQuery(sql).setParameter(FORMID, formId).setParameter(OPPORTUNITY_TYPE,
				opportunityType);
		return (boolean) query.uniqueResult();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<SelectedFieldsDTO> getSelectedFieldsByFormIdAndOpportunityType(Integer formId, String opportunityType) {
		Session session = sessionFactory.getCurrentSession();
		String sql = "SELECT id as \"id\", label_name as \"labelName\", display_name as \"displayName\", label_id as \"labelId\", form_id as \"formId\", integration_id as \"integrationId\", column_order as \"columnOrder\", is_selected_column as \"selectedColumn\",my_preferences_enabled as \"myPreferencesEnabled\", is_default_column as \"defaultColumn\",created_user_id as \"loggedInUserId\"  "
				+ "FROM public.xt_selected_fields where  form_id =:formId and opportunity_type=CAST(:opportunityType AS opportunity_type) order by column_order ";
		SQLQuery query = session.createSQLQuery(sql);
		query.setParameter(FORMID, formId);
		query.setParameter(OPPORTUNITY_TYPE, opportunityType);
		return (List<SelectedFieldsDTO>) paginationUtil.getListDTO(SelectedFieldsDTO.class, query);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<String> getPrivateFormLableIdsFormFormId(Integer formId,String userType,FormTypeEnum formType){
		Session session = sessionFactory.getCurrentSession();
		String sql = " SELECT xfl.label_id  from xt_form_label xfl JOIN  xt_form_label_type xft ON xfl.label_type = xft.id where xfl.form_id = :formId and xfl.is_private = true ";
		Query query = session.createSQLQuery(sql).setParameter(FORMID, formId);
		return query.list();
	}

}
