package com.xtremand.category.service;

import java.util.List;

import com.xtremand.category.bom.CategoryModuleEnum;
import com.xtremand.category.dto.CategoryDTO;
import com.xtremand.category.dto.CategoryPostDTO;
import com.xtremand.common.bom.Pagination;
import com.xtremand.formbeans.XtremandResponse;

public interface CategoryService {

	public XtremandResponse findCategoriesByCompanyId(Pagination pagination);

	public XtremandResponse addDataToDefaultCategories(String type);

	public XtremandResponse save(CategoryDTO categoryDto);

	public XtremandResponse listAllCategoryNames(Integer companyId);

	public XtremandResponse getCategoryById(Integer categoryId);

	public XtremandResponse update(CategoryDTO categoryDto);

	public XtremandResponse deleteById(Integer categoryId, Integer userId);

	public XtremandResponse moveAndDeleteCategory(Integer categoryIdToDelete, Integer categoryIdToMove, Integer userId);

	public XtremandResponse addDefaultCategories(String type);

	public XtremandResponse listAllCategoryNamesByUserId(Integer userId);

	public List<CategoryDTO> listFoldersByType(Integer userId, CategoryModuleEnum categoryModuleEnum);

	public XtremandResponse getItemsCountDetailsByCategoryId(CategoryPostDTO categoryPostDto);

	public XtremandResponse findVendorCompanyCategoryNames(Integer userId, String domainName);

}
