package com.xtremand.campaign.bom;

import javax.persistence.Column;
import javax.persistence.Entity;

import com.xtremand.category.bom.CategoryMappedView;
@Entity(name="xt_form_category_view")
public class FormCategoryView extends CategoryMappedView {

	@Column(name = "created_for_company")
	private Integer createdForCompany;

}
