package com.xtremand.form.emailtemplate.dao.hibernate;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.xtremand.form.emailtemplate.dao.FormEmailTemplateDao;
import com.xtremand.form.emailtemplate.dto.FormEmailTemplateDTO;

@Repository
public class HibernateFormEmailTemplateDao implements FormEmailTemplateDao {

	
	@Autowired
	private SessionFactory sessionFactory;
	
	@SuppressWarnings("unchecked")
	@Override
	public List<FormEmailTemplateDTO> listByEmailTemplateId(Integer emailTemplateId) {
		Session session =  sessionFactory.getCurrentSession();
		String sqlQuery = "select id,form_id from xt_form_email_template where email_template_id=:emailTemplateId";
		Query query = session.createSQLQuery(sqlQuery).setParameter("emailTemplateId", emailTemplateId);
		List<Object[]> list = query.list();
		List<FormEmailTemplateDTO> dtos = new ArrayList<>();
		for(Object[] object:list){
			FormEmailTemplateDTO formEmailTemplateDTO = new FormEmailTemplateDTO();
			formEmailTemplateDTO.setId((Integer) object[0]);
			formEmailTemplateDTO.setFormId((Integer) object[1]);
			formEmailTemplateDTO.setEmailTemplateId(emailTemplateId);
			dtos.add(formEmailTemplateDTO);
		}
		return dtos;
	}

}
