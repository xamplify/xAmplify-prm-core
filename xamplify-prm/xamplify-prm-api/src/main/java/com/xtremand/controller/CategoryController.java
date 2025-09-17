package com.xtremand.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.xtremand.category.bom.CategoryModuleEnum;
import com.xtremand.category.dto.CategoryDTO;
import com.xtremand.category.dto.CategoryPostDTO;
import com.xtremand.category.service.CategoryService;
import com.xtremand.common.bom.Pagination;
import com.xtremand.exception.CategoryDataAccessException;
import com.xtremand.exception.DuplicateEntryException;
import com.xtremand.formbeans.XtremandResponse;

@RestController
@RequestMapping(value = "/category/")
public class CategoryController {

	@Autowired
	private CategoryService categoryService;

	@PostMapping(value = "listAll")
	public ResponseEntity<XtremandResponse> findCategories(@RequestBody Pagination pagination) {
		try {
			return ResponseEntity.ok(categoryService.findCategoriesByCompanyId(pagination));
		} catch (Exception e) {
			throw new CategoryDataAccessException(e);
		}
	}

	@GetMapping(value = "listAllCategoryNames/{companyId}")
	public ResponseEntity<XtremandResponse> listAllCategoryNames(@PathVariable Integer companyId) {
		try {
			return ResponseEntity.ok(categoryService.listAllCategoryNames(companyId));
		} catch (Exception e) {
			throw new CategoryDataAccessException(e);
		}
	}

	@GetMapping(value = "listAllCategoryNamesByLoggedInUserId/{userId}")
	public ResponseEntity<XtremandResponse> listAllCategoryNamesByLoggedInUserId(@PathVariable Integer userId) {
		try {
			return ResponseEntity.ok(categoryService.listAllCategoryNamesByUserId(userId));
		} catch (Exception e) {
			throw new CategoryDataAccessException(e);
		}
	}

	@GetMapping(value = "getById/{categoryId}")
	public ResponseEntity<XtremandResponse> getCategoryById(@PathVariable Integer categoryId) {
		try {
			return ResponseEntity.ok(categoryService.getCategoryById(categoryId));
		} catch (Exception e) {
			throw new CategoryDataAccessException(e);
		}
	}

	@PostMapping(value = "getItemsCountDetailsByCategoryId")
	public ResponseEntity<XtremandResponse> getItemsCountDetailsByCategoryId(
			@RequestBody CategoryPostDTO categoryPostDTO) {
		try {
			return ResponseEntity.ok(categoryService.getItemsCountDetailsByCategoryId(categoryPostDTO));
		} catch (Exception e) {
			throw new CategoryDataAccessException(e);
		}
	}

	@PostMapping(value = "save")
	public ResponseEntity<XtremandResponse> save(@RequestBody CategoryDTO categoryDto) {
		try {
			return ResponseEntity.ok(categoryService.save(categoryDto));
		} catch (DataIntegrityViolationException e) {
			if (e.getMessage().indexOf("xt_category_unique_key") > -1) {
				throw new DuplicateEntryException("Folder Already Exists.");
			} else {
				throw new CategoryDataAccessException(e);
			}
		} catch (Exception e) {
			throw new CategoryDataAccessException(e);
		}
	}

	@PostMapping(value = "update")
	public ResponseEntity<XtremandResponse> update(@RequestBody CategoryDTO categoryDto) {
		try {
			return ResponseEntity.ok(categoryService.update(categoryDto));
		} catch (DuplicateEntryException exception) {
			throw new DuplicateEntryException(exception.getMessage());
		} catch (Exception e) {
			throw new CategoryDataAccessException(e);
		}
	}

	@GetMapping(value = "deleteById/{categoryId}/{userId}")
	public ResponseEntity<XtremandResponse> deleteById(@PathVariable Integer categoryId, @PathVariable Integer userId) {
		try {
			return ResponseEntity.ok(categoryService.deleteById(categoryId, userId));
		} catch (Exception e) {
			throw new CategoryDataAccessException(e);
		}
	}

	@GetMapping(value = "moveAndDeleteCategory/{categoryIdToDelete}/{categoryIdToMove}/{userId}")
	public ResponseEntity<XtremandResponse> moveAndDeleteCategory(@PathVariable Integer categoryIdToDelete,
			@PathVariable Integer categoryIdToMove, @PathVariable Integer userId) {
		try {
			return ResponseEntity
					.ok(categoryService.moveAndDeleteCategory(categoryIdToDelete, categoryIdToMove, userId));
		} catch (Exception e) {
			throw new CategoryDataAccessException(e);
		}
	}

	@GetMapping(value = "addDefaultCategories/{type}")
	public ResponseEntity<XtremandResponse> addDefaultCategories(@PathVariable String type) {
		try {
			return ResponseEntity.ok(categoryService.addDefaultCategories(type));
		} catch (Exception e) {
			throw new CategoryDataAccessException(e);
		}
	}

	@GetMapping(value = "addDataToDefaultCategories/{type}")
	public ResponseEntity<XtremandResponse> addDataToDefaultCategories(@PathVariable String type) {
		try {
			return ResponseEntity.ok(categoryService.addDataToDefaultCategories(type));
		} catch (Exception e) {
			throw new CategoryDataAccessException(e);
		}
	}

	@GetMapping(value = "listEmailTemplateCategories/{userId}")
	public ResponseEntity<List<CategoryDTO>> listEmailTemplateCategories(@PathVariable Integer userId) {
		try {
			return ResponseEntity.ok(categoryService.listFoldersByType(userId, CategoryModuleEnum.EMAIL_TEMPLATE));
		} catch (Exception e) {
			throw new CategoryDataAccessException(e);
		}
	}

	@GetMapping(value = "listLandingPageCategories/{userId}")
	public ResponseEntity<List<CategoryDTO>> listLandingPageCategories(@PathVariable Integer userId) {
		try {
			return ResponseEntity.ok(categoryService.listFoldersByType(userId, CategoryModuleEnum.LANDING_PAGE));
		} catch (Exception e) {
			throw new CategoryDataAccessException(e);
		}
	}

	/*************** XNFR-83 **********/
	@GetMapping(value = "listAllCategoryNamesByLoggedInUserId/{userId}/domainName/{domainName}")
	public ResponseEntity<XtremandResponse> findVendorCompanyCategoryNames(@PathVariable Integer userId,
			@PathVariable String domainName) {
		return ResponseEntity.ok(categoryService.findVendorCompanyCategoryNames(userId, domainName));
	}

}
