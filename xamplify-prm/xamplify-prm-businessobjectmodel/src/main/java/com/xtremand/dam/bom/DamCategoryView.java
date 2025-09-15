package com.xtremand.dam.bom;

import javax.persistence.Column;
import javax.persistence.Entity;

import com.xtremand.category.bom.CategoryMappedView;

@Entity(name = "xt_dam_category_view")
public class DamCategoryView extends CategoryMappedView {

	@Column(name = "created_for_company")
	private Integer createdForCompany;

}
