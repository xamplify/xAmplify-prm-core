package com.xtremand.form.emailtemplate.dao;

import java.util.List;

import com.xtremand.form.emailtemplate.dto.FormEmailTemplateDTO;

public interface FormEmailTemplateDao {
	
	List<FormEmailTemplateDTO> listByEmailTemplateId(Integer emailTemplateId);

}
