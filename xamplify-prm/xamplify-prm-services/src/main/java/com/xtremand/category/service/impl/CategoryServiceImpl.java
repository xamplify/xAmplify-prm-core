package com.xtremand.category.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.xtremand.category.bom.Category;
import com.xtremand.category.bom.CategoryMappedView;
import com.xtremand.category.bom.CategoryModule;
import com.xtremand.category.bom.CategoryModuleEnum;
import com.xtremand.category.bom.CategoryView;
import com.xtremand.category.dao.CategoryDao;
import com.xtremand.category.dto.CategoryDTO;
import com.xtremand.category.dto.CategoryItemsCountDTO;
import com.xtremand.category.dto.CategoryItemsDTO;
import com.xtremand.category.dto.CategoryPostDTO;
import com.xtremand.category.service.CategoryService;
import com.xtremand.common.bom.Criteria;
import com.xtremand.common.bom.FindLevel;
import com.xtremand.common.bom.Pagination;
import com.xtremand.exception.CategoryDataAccessException;
import com.xtremand.exception.DuplicateEntryException;
import com.xtremand.formbeans.XtremandResponse;
import com.xtremand.team.dao.TeamDao;
import com.xtremand.user.bom.Role;
import com.xtremand.user.dao.UserDAO;
import com.xtremand.util.DateUtils;
import com.xtremand.util.XamplifyUtils;
import com.xtremand.util.dao.UtilDao;
import com.xtremand.util.service.UtilService;
import com.xtremand.vanity.url.dto.VanityUrlDetailsDTO;

@Service
@Transactional
public class CategoryServiceImpl implements CategoryService {

	@Value("${fa.folder.open}")
	private String faIcon;

	@Value("${default-company-folder-suffix}")
	private String defaultFolderSuffix;

	@Autowired
	private CategoryDao categoryDao;

        @Autowired
        private UserDAO userDao;

        @Autowired
        private UtilService utilService;

        @Autowired
        private UtilDao utilDao;

	@Value("${form.module.name}")
	private String formModule;

	@Value("${form.icon}")
	private String formIcon;

	@Value("${page.module.name}")
	private String pageModule;

	@Value("${page.icon}")
	private String pageIcon;

	@Value("${campaign.module.name}")
	private String campaignModule;

	@Value("${campaign.icon}")
	private String campaignIcon;

	@Value("${dam.module.name}")
	private String damModule;

	@Value("${dam.icon}")
	private String damIcon;

	@Value("${lms.module.name}")
	private String lmsModule;

	@Value("${lms.icon}")
	private String lmsIcon;

	@Value("${playbook.module.name}")
	private String playbookModule;

	@Value("${playbook.icon}")
	private String playbookIcon;


	@Autowired
	private TeamDao teamDao;

	@SuppressWarnings("unchecked")
	@Override
	public XtremandResponse findCategoriesByCompanyId(Pagination pagination) {
		try {
			List<Criteria> criterias = new ArrayList<>();
			XtremandResponse response = new XtremandResponse();
			utilService.setVanityUrlFilter(pagination);
			Integer loggedInUserId = pagination.getUserId();
			Integer companyId = userDao.getCompanyIdByUserId(loggedInUserId);
			pagination.setCompanyId(companyId);
			Map<String, Object> map = categoryDao.find(criterias, new FindLevel[] { FindLevel.SHALLOW }, pagination);
			Map<String, Object> updatedMap = new HashMap<>();
			List<CategoryMappedView> categoryViews = (List<CategoryMappedView>) map.get("list");
			List<Integer> userIds = userDao.listAllUserIdsByLoggedInUserId(loggedInUserId);
			List<CategoryDTO> dtos = convertDtoList(categoryViews, pagination, userIds);
			Integer totalRecords = (Integer) map.get("totalRecords");
			updatedMap.put("categories", dtos);
			updatedMap.put("totalRecords", totalRecords);
			if (totalRecords != null && totalRecords > 0) {
				List<String> listRolesByUserId = userDao.listRolesByUserId(loggedInUserId);
				boolean hasTeamAccess = teamDao.hasMarketingModulesAccessToTeamMember(loggedInUserId);
				boolean hasPreviewAccess = listRolesByUserId.indexOf(Role.COMPANY_PARTNER.getRoleName()) > -1
						|| listRolesByUserId.indexOf(Role.PRM_ROLE.getRoleName()) > -1
						|| listRolesByUserId.indexOf(Role.DAM.getRoleName()) > -1
						|| listRolesByUserId.indexOf(Role.LEARNING_TRACK.getRoleName()) > -1
						|| listRolesByUserId.indexOf(Role.PLAY_BOOK.getRoleName()) > -1 || hasTeamAccess;
				updatedMap.put("previewAccess", hasPreviewAccess);
			}
			response.setData(updatedMap);
			response.setStatusCode(200);
			return response;
		} catch (BeansException e) {
			throw new CategoryDataAccessException("Unable to copy from source to target");
		} catch (CategoryDataAccessException ge) {
			throw new CategoryDataAccessException(ge);
		} catch (Exception ex) {
			throw new CategoryDataAccessException(ex);
		}

	}

	private List<CategoryDTO> convertDtoList(List<CategoryMappedView> categoryViews, Pagination pagination,
			List<Integer> userIds) {
		List<CategoryDTO> dtos = new ArrayList<>();
		VanityUrlDetailsDTO vanityUrlDetailsDto = utilService.getVanityUrlFilteredData(pagination.getUserId(),
				pagination.isVanityUrlFilter(), utilDao.getPrmCompanyProfileName());
		boolean isPartnerLoggedInThroughVanityUrl = vanityUrlDetailsDto.isPartnerLoggedInThroughVanityUrl();
		boolean isVendorLoggedInThroughVanityUrl = vanityUrlDetailsDto.isVendorLoggedInThroughOwnVanityUrl();
		Integer vendorCompanyId = pagination.getVendorCompanyId();
		for (CategoryMappedView categoryView : categoryViews) {
			CategoryDTO dto = new CategoryDTO();
			BeanUtils.copyProperties(categoryView, dto);
			dto.setCreatedTimeInString(DateUtils.getUtcString(categoryView.getCreatedTime()));
			dto.setCreatedBy(XamplifyUtils.setDisplayName(categoryView.getFirstName(), categoryView.getLastName(),
					categoryView.getEmailId()));
			Integer teamMemberId = pagination.getTeamMemberId();
			Integer partnerCompanyId = pagination.getPartnerCompanyId();
			Integer categoryId = dto.getId();
			dto.setCreatedUserId(userDao.getUserIdByEmail(categoryView.getEmailId()));
			if (XamplifyUtils.isValidInteger(teamMemberId)) {
				setTeamMembersCampaignCount(pagination, dto, teamMemberId, categoryId);
			} else if (XamplifyUtils.isValidInteger(partnerCompanyId)) {
				setCountByType(pagination, dto, partnerCompanyId, categoryId);
			} else {
				setVanityFilterCount(userIds, isPartnerLoggedInThroughVanityUrl, isVendorLoggedInThroughVanityUrl,
						vendorCompanyId, categoryView, dto, pagination);
			}
			dto.setExpanded(false);
			dtos.add(dto);
		}
		return dtos;
	}

	private void setCountByType(Pagination pagination, CategoryDTO dto, Integer partnerCompanyId, Integer categoryId) {
		String categoryType = pagination.getCategoryType();
		if ("c".equalsIgnoreCase(categoryType) || CategoryModuleEnum.CAMPAIGN.name().equalsIgnoreCase(categoryType)) {
			dto.setCount(categoryDao.getRedistributedCampaignItemsCountByCategoryIdAndPartnerCompanyId(categoryId,
					partnerCompanyId, CategoryModuleEnum.CAMPAIGN));
		} else if ("l".equalsIgnoreCase(categoryType)
				|| CategoryModuleEnum.LANDING_PAGE.name().equalsIgnoreCase(categoryType)) {
			dto.setCount(categoryDao.getPageItemsCountByCategoryIdAndPartnerCompanyId(categoryId, partnerCompanyId,
					CategoryModuleEnum.LANDING_PAGE));
		} else if (CategoryModuleEnum.DAM.name().equalsIgnoreCase(categoryType)) {
			dto.setCount(categoryDao.getSharedAssetsCountByCategoryIdAndPartnerCompanyId(categoryId, partnerCompanyId,
					CategoryModuleEnum.DAM, pagination.getUserId()));
		} else if (CategoryModuleEnum.LEARNING_TRACK.name().equalsIgnoreCase(categoryType)) {
			dto.setCount(categoryDao.getSharedTracksCountByCategoryIdAndPartnerCompanyId(categoryId, partnerCompanyId,
					pagination.getUserId()));
		} else if (CategoryModuleEnum.PLAY_BOOK.name().equalsIgnoreCase(categoryType)) {
			dto.setCount(categoryDao.getSharedPlayBooksCountByCategoryIdAndPartnerCompanyId(categoryId,
					partnerCompanyId, pagination.getUserId()));
		}
	}

	private void setVanityFilterCount(List<Integer> userIds, boolean isPartnerLoggedInThroughVanityUrl,
			boolean isVendorLoggedInThroughVanityUrl, Integer vendorCompanyId, CategoryMappedView categoryView,
			CategoryDTO dto, Pagination pagination) {
		String categoryType = pagination.getCategoryType();
		if (isPartnerLoggedInThroughVanityUrl) {
			setCategoryCountForPartner(userIds, vendorCompanyId, dto, pagination, categoryType);
		} else if (isVendorLoggedInThroughVanityUrl) {
			setCategoryCountForOwnVanity(userIds, categoryView, dto, pagination, categoryType);
		} else {
			setCategoryCountForNonVanity(userIds, vendorCompanyId, dto, pagination, categoryType);
		}
	}

	private void setCategoryCountForNonVanity(List<Integer> userIds, Integer vendorCompanyId, CategoryDTO dto,
			Pagination pagination, String categoryType) {
		if ("c".equals(categoryType)) {
			boolean loginAsPartner = XamplifyUtils.isLoginAsPartner(pagination.getLoginAsUserId());
			if (loginAsPartner) {
				Integer redistributedCampaignCount = categoryDao
						.getCampaignItemsCountByCategoryIdAndVendorCompanyIdAndUserIds(userIds, vendorCompanyId,
								dto.getId());
				Integer vendorCompanyCampaignCount = categoryDao
						.getVendorCompanyCampaignItemsCountByUserIdsAndCategoryId(userIds, dto.getId());
				dto.setCount(redistributedCampaignCount + vendorCompanyCampaignCount);
			} else if (pagination.isShowPartnerCreatedCampaigns()) {
				dto.setCount(categoryDao.findPartnerCreatedCampaignsCountByCategoryId(pagination.getCompanyId(),
						dto.getId()));
			}
		} else if (CategoryModuleEnum.EMAIL_TEMPLATE.name().equalsIgnoreCase(categoryType)) {
			dto.setCount(categoryDao.findEmailTemplateCategoryCount(dto.getId(), pagination));
		} else if (!XamplifyUtils.isValidString(categoryType)) {
			CategoryItemsCountDTO categoryItemsCountDTO = categoryDao.findCategoryCountDetails(dto.getId(), 0, false);
			dto.setCount(categoryItemsCountDTO.getTotalCount());
		}
	}

	private void setCategoryCountForOwnVanity(List<Integer> userIds, CategoryMappedView categoryView, CategoryDTO dto,
			Pagination pagination, String categoryType) {
		if (CategoryModuleEnum.EMAIL_TEMPLATE.name().equalsIgnoreCase(categoryType)) {
			dto.setCount(categoryDao.findEmailTemplateCategoryCount(dto.getId(), pagination));
		} else if (!XamplifyUtils.isValidString(categoryType)) {
			CategoryItemsCountDTO categoryItemsCountDTO = categoryDao.findCategoryCountDetails(dto.getId(), 0, false);
			dto.setCount(categoryItemsCountDTO.getTotalCount());
		}
	}

	private void setCategoryCountForPartner(List<Integer> userIds, Integer vendorCompanyId, CategoryDTO dto,
			Pagination pagination, String categoryType) {
		if (CategoryModuleEnum.EMAIL_TEMPLATE.name().equalsIgnoreCase(categoryType)) {
			dto.setCount(categoryDao.findEmailTemplateCategoryCount(dto.getId(), pagination));
		}
	}

	private void setTeamMembersCampaignCount(Pagination pagination, CategoryDTO dto, Integer teamMemberId,
			Integer categoryId) {
		if (pagination.isVanityUrlFilterApplicable()) {
			dto.setCount(categoryDao.getCampaignItemsCountByCategoryIdAndUserIdAndVendorCompanyId(teamMemberId,
					categoryId, CategoryModuleEnum.CAMPAIGN, pagination.getVendorCompanyId(), pagination.isArchived()));
		} else if (pagination.isVanityUrlFilter()) {
			dto.setCount(categoryDao.getCampaignItemsCountByCategoryIdAndTeamMemberId(teamMemberId, categoryId,
					pagination.isArchived()));
		} else {
			dto.setCount(categoryDao.getCampaignItemsCountByCategoryIdAndUserId(teamMemberId, categoryId,
					CategoryModuleEnum.CAMPAIGN, pagination.isArchived()));
		}
	}

	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public XtremandResponse save(CategoryDTO categoryDto) {
		try {
			XtremandResponse response = new XtremandResponse();
			Integer userId = categoryDto.getCreatedUserId();
			response.setAccess(hasFolderAccess(userId));
			if (response.isAccess()) {
				Category category = new Category();
				BeanUtils.copyProperties(categoryDto, category);
				String updatedCategoryName = XamplifyUtils.removeExtraSpace(categoryDto.getName());
				category.setName(updatedCategoryName);
				category.setCreatedTime(new Date());
				category.setIcon(faIcon);
				Integer companyId = userDao.getCompanyIdByUserId(userId);
				category.setCompanyId(companyId);
				categoryDao.save(category);
				response.setStatusCode(200);
				response.setMessage("Folder Created Successfully");
			}
			return response;
		} catch (BeansException e) {
			throw new CategoryDataAccessException("Unable to copy from source to taget");
		} catch (CategoryDataAccessException cde) {
			throw new CategoryDataAccessException(cde);
		} catch (Exception exception) {
			throw new CategoryDataAccessException(exception);
		}
	}

	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public XtremandResponse update(CategoryDTO categoryDto) {
		try {
			XtremandResponse response = new XtremandResponse();
			response.setAccess(hasFolderAccess(categoryDto.getCreatedUserId()));
			if (response.isAccess()) {
				String updatedCategoryName = XamplifyUtils.removeExtraSpace(categoryDto.getName());
				categoryDto.setName(updatedCategoryName);
				categoryDao.update(categoryDto);
				response.setStatusCode(200);
				response.setMessage("Folder Updated Successfully");
			}
			return response;
		} catch (DuplicateEntryException exception) {
			throw new DuplicateEntryException(exception.getMessage());
		} catch (BeansException e) {
			throw new CategoryDataAccessException("Unable to copy from source to taget");
		} catch (CategoryDataAccessException cde) {
			throw new CategoryDataAccessException(cde);
		} catch (Exception exception) {
			throw new CategoryDataAccessException(exception);
		}

	}

	@Override
	public XtremandResponse listAllCategoryNames(Integer companyId) {
		try {
			XtremandResponse response = new XtremandResponse();
			response.setStatusCode(200);
			response.setData(categoryDao.listAvailableCategories(companyId));
			return response;
		} catch (CategoryDataAccessException cde) {
			throw new CategoryDataAccessException(cde);
		} catch (Exception exception) {
			throw new CategoryDataAccessException(exception);
		}
	}

	@Override
	public XtremandResponse listAllCategoryNamesByUserId(Integer userId) {
		try {
			XtremandResponse response = new XtremandResponse();
			response.setStatusCode(200);
			response.setData(categoryDao.listAvailableCategoriesByUserId(userId));
			return response;
		} catch (CategoryDataAccessException cde) {
			throw new CategoryDataAccessException(cde);
		} catch (Exception exception) {
			throw new CategoryDataAccessException(exception);
		}

	}

	@Override
	public XtremandResponse getCategoryById(Integer categoryId) {
		try {
			XtremandResponse response = new XtremandResponse();
			CategoryView categoryView = categoryDao.findById(categoryId);
			if (categoryView != null) {
				CategoryDTO categoryDTO = new CategoryDTO();
				BeanUtils.copyProperties(categoryView, categoryDTO);
				response.setData(categoryDTO);
				response.setStatusCode(200);
			} else {
				setEmptyMessage(response);
			}
			return response;
		} catch (BeansException e) {
			throw new CategoryDataAccessException("Unable to copy from source to target");
		} catch (CategoryDataAccessException ge) {
			throw new CategoryDataAccessException(ge);
		} catch (Exception ex) {
			throw new CategoryDataAccessException(ex);
		}

	}

	@Override

	public XtremandResponse deleteById(Integer categoryId, Integer userId) {
		try {
			XtremandResponse response = new XtremandResponse();
			response.setAccess(hasFolderAccess(userId));
			if (response.isAccess()) {
				/*** XBI-1784 ***/
				Integer defaultCategoryId = categoryDao.getDefaultCategoryIdByUserId(userId);
				if (!defaultCategoryId.equals(categoryId)) {
					categoryDao.moveAndDeleteCategory(categoryId, defaultCategoryId);
					response.setStatusCode(200);
					response.setMessage("Folder Deleted Successfully");
				} else {
					response.setAccess(false);
				}
			}
			return response;

		} catch (CategoryDataAccessException cde) {
			throw new CategoryDataAccessException(cde);
		} catch (Exception exception) {
			throw new CategoryDataAccessException(exception);
		}

	}

	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public XtremandResponse moveAndDeleteCategory(Integer categoryIdToDelete, Integer categoryIdToMove,
			Integer userId) {
		try {
			XtremandResponse response = new XtremandResponse();
			response.setAccess(hasFolderAccess(userId));
			if (response.isAccess()) {
				categoryDao.moveAndDeleteCategory(categoryIdToDelete, categoryIdToMove);
				response.setStatusCode(200);
				response.setMessage("Items are moved and folder is deleted successfully");
			}
			return response;
		} catch (CategoryDataAccessException cde) {
			throw new CategoryDataAccessException(cde);
		} catch (Exception exception) {
			throw new CategoryDataAccessException(exception);
		}

	}

	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public XtremandResponse addDefaultCategories(String type) {
		try {
			XtremandResponse response = new XtremandResponse();
			response.setStatusCode(200);
			List<Object[]> listAllCompanies = categoryDao.listAllCompanyDetails(type);
			return addDefaultFoldersForCompanies(response, listAllCompanies);
		} catch (CategoryDataAccessException cde) {
			throw new CategoryDataAccessException(cde);
		} catch (Exception exception) {
			throw new CategoryDataAccessException(exception);
		}

	}

	private XtremandResponse addDefaultFoldersForCompanies(XtremandResponse response, List<Object[]> listAllCompanies) {
		for (Object[] company : listAllCompanies) {
			Integer companyId = (Integer) company[0];
			String companyProfileName = (String) company[1];
			Category category = new Category();
			category.setCompanyId(companyId);
			String defaultCategoryName = companyProfileName + defaultFolderSuffix;
			category.setName(defaultCategoryName);
			category.setDescription(defaultCategoryName);
			category.setDefaultCategory(true);
			category.setCreatedTime(new Date());
			category.setCreatedUserId(categoryDao.getUserIdByCompanyId(companyId));
			category.setIcon(faIcon);
			categoryDao.save(category);
		}
		if (listAllCompanies.isEmpty()) {
			response.setMessage("Already Added For All Companies");
		} else {
			response.setMessage("Default Folder has been added successfully");
		}

		return response;
	}

	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public XtremandResponse addDataToDefaultCategories(String type) {
		try {
			XtremandResponse response = new XtremandResponse();
			response.setStatusCode(200);
			List<Object[]> listAllCategories = categoryDao.findAllCategories();
			List<CategoryModule> categoryModules = new ArrayList<>();
			for (Object[] category : listAllCategories) {
				Integer categoryId = (Integer) category[0];
				Integer companyId = (Integer) category[1];
				Integer createdUserId = (Integer) category[2];
				Date createdTime = (Date) category[3];
				if (type.equals("F")) {
					addFormsToDefaultCategory(categoryModules, categoryId, companyId, createdUserId, createdTime);
				}
			}
			categoryDao.addCategoryModules(categoryModules);
			response.setMessage(type + " Are Added To Default Folder Successfully");
			return response;
		} catch (CategoryDataAccessException cde) {
			throw new CategoryDataAccessException(cde);
		} catch (Exception exception) {
			throw new CategoryDataAccessException(exception);
		}

	}

	private void addFormsToDefaultCategory(List<CategoryModule> categoryModules, Integer categoryId, Integer companyId,
			Integer createdUserId, Date createdTime) {
		List<Integer> formIds = categoryDao.listFormIdsByCompanyId(companyId);
		for (Integer formId : formIds) {
			Integer formIdsCount = categoryDao.getFormsCategroyCountByFormId(formId);
			if (formIdsCount == 0) {
				CategoryModule categoryModule = new CategoryModule();
				categoryModule.setCategoryId(categoryId);
				categoryModule.setCompanyId(companyId);
				categoryModule.setCategoryModuleEnum(CategoryModuleEnum.FORM);
				categoryModule.setFormId(formId);
				categoryModule.setCreatedTime(createdTime);
				categoryModule.setCreatedUserId(createdUserId);
				categoryModules.add(categoryModule);
			}

		}
	}

	@Override
	public List<CategoryDTO> listFoldersByType(Integer userId, CategoryModuleEnum categoryModuleEnum) {
		List<Object[]> folders = categoryDao.listFoldersByType(userId, categoryModuleEnum);
		List<CategoryDTO> categoryDtos = new ArrayList<>();
		for (Object[] folder : folders) {
			Integer id = (Integer) folder[0];
			String name = (String) folder[1];
			CategoryDTO categoryDTO = new CategoryDTO();
			categoryDTO.setId(id);
			categoryDTO.setName(name);
			categoryDtos.add(categoryDTO);
		}
		return categoryDtos;
	}

	@Override
	public XtremandResponse getItemsCountDetailsByCategoryId(CategoryPostDTO categoryPostDTO) {
		XtremandResponse response = new XtremandResponse();
		Integer categoryId = categoryPostDTO.getCategoryId();
		Integer userId = categoryPostDTO.getLoggedInUserId();
		boolean isVanityUrlFilter = categoryPostDTO.isVanityUrlFilter();
		String companyProfileName = categoryPostDTO.getVendorCompanyProfileName();
		VanityUrlDetailsDTO vanityUrlDetailsDTO = new VanityUrlDetailsDTO();
		vanityUrlDetailsDTO.setUserId(userId);
		vanityUrlDetailsDTO.setVanityUrlFilter(isVanityUrlFilter);
		vanityUrlDetailsDTO.setVendorCompanyProfileName(companyProfileName);
		utilService.isVanityUrlFilterApplicable(vanityUrlDetailsDTO);
		if (isVanityUrlFilter) {
			setItemsCountForVanityLogin(response, categoryId, vanityUrlDetailsDTO);
		} else {
			setItemsCountForNonVanityLogin(response, categoryId, vanityUrlDetailsDTO);
		}
		return response;
	}

	private void setItemsCountForVanityLogin(XtremandResponse response, Integer categoryId,
			VanityUrlDetailsDTO vanityUrlDetailsDTO) {
		List<Integer> userIds = userDao.listAllUserIdsByLoggedInUserId(vanityUrlDetailsDTO.getUserId());
		if (vanityUrlDetailsDTO.isVendorLoggedInThroughOwnVanityUrl()) {
			setItemsCountForOwnVanity(response, categoryId, vanityUrlDetailsDTO, userIds);
		} else if (vanityUrlDetailsDTO.isPartnerLoggedInThroughVanityUrl()) {
			setItemsCountForPartner(response, categoryId, vanityUrlDetailsDTO, userIds);
		}
	}

	private void setItemsCountForNonVanityLogin(XtremandResponse response, Integer categoryId,
			VanityUrlDetailsDTO vanityUrlDetailsDTO) {
		CategoryItemsCountDTO categoryItemsCountDTO = categoryDao.findCategoryCountDetails(categoryId, 0, false);
		if (categoryItemsCountDTO.getTotalCount().equals(0)) {
			setEmptyMessage(response);
		} else {
			setItemsCountDto(response, categoryItemsCountDTO.getEmailTemplatesCount(),
					categoryItemsCountDTO.getFormsCount(), categoryItemsCountDTO.getPagesCount(),
					categoryItemsCountDTO.getCampaignsCount(), categoryItemsCountDTO.getDamCount(),
					categoryItemsCountDTO.getLmsCount(), categoryItemsCountDTO.getPlayBooksCount(),
					vanityUrlDetailsDTO);
		}
	}

	private void setEmptyMessage(XtremandResponse response) {
		response.setStatusCode(404);
		response.setMessage("No data found");
	}

	private void setItemsCountForOwnVanity(XtremandResponse response, Integer categoryId,
			VanityUrlDetailsDTO vanityUrlDetailsDTO, List<Integer> userIds) {
		CategoryItemsCountDTO categoryItemsCountDTO = categoryDao.findCategoryCountDetails(categoryId, 0, false);
		if (categoryItemsCountDTO.getTotalCount().equals(0)) {
			setEmptyMessage(response);
		} else {
			setItemsCountDto(response, categoryItemsCountDTO.getEmailTemplatesCount(),
					categoryItemsCountDTO.getFormsCount(), categoryItemsCountDTO.getPagesCount(),
					categoryItemsCountDTO.getCampaignsCount(), categoryItemsCountDTO.getDamCount(),
					categoryItemsCountDTO.getLmsCount(), categoryItemsCountDTO.getPlayBooksCount(),
					vanityUrlDetailsDTO);
		}
	}

	private void setItemsCountForPartner(XtremandResponse response, Integer categoryId,
			VanityUrlDetailsDTO vanityUrlDetailsDTO, List<Integer> userIds) {
		setItemsCountDto(response, 0, 0, 0, 0, 0, 0, 0, vanityUrlDetailsDTO);
	}

	private void setItemsCountDto(XtremandResponse response, int emailTemplateCount, int formCount, int pageCount,
			int campaignCount, int damCount, int lmsCount, int playbooksCount,
			VanityUrlDetailsDTO vanityUrlDetailsDTO) {
		List<CategoryItemsDTO> dtos = new ArrayList<>();
		Integer loggedInUserId = vanityUrlDetailsDTO.getUserId();
		List<String> listRolesByUserId = userDao.listRolesByUserId(loggedInUserId);
		boolean isOnlyPartner = Role.isOnlyPartnerCompanyByRoleNames(listRolesByUserId);
		boolean companyPartnerRole = listRolesByUserId.indexOf(Role.COMPANY_PARTNER.getRoleName()) > -1;
		boolean prmRole = listRolesByUserId.indexOf(Role.PRM_ROLE.getRoleName()) > -1;
		boolean damRole = listRolesByUserId.indexOf(Role.DAM.getRoleName()) > -1;
		boolean lmsRole = listRolesByUserId.indexOf(Role.LEARNING_TRACK.getRoleName()) > -1;
		boolean playBooksRole = listRolesByUserId.indexOf(Role.PLAY_BOOK.getRoleName()) > -1;

		boolean campaignCountPreviewAccess = companyPartnerRole;
//		CategoryItemsDTO campaignDto = setDtoByType(campaignIcon, campaignCount, campaignModule,
//				campaignCountPreviewAccess);
//		dtos.add(campaignDto);

		boolean designCountPreviewAccess = companyPartnerRole;

		if (!isOnlyPartner && !vanityUrlDetailsDTO.isPartnerLoggedInThroughVanityUrl()) {
			CategoryItemsDTO formDto = setDtoByType(formIcon, formCount, formModule,
					designCountPreviewAccess || prmRole);
//			CategoryItemsDTO pageDto = setDtoByType(pageIcon, pageCount, pageModule, designCountPreviewAccess);
			dtos.add(formDto);
//			dtos.add(pageDto);
		}

		/********* XNFR-169 *************/
		boolean damCountPreviewAccess = damRole || prmRole || companyPartnerRole;

		boolean lmsCountPreviewAccess = lmsRole || prmRole;

		boolean playBooksCountPreviewAccess = playBooksRole || prmRole;
		CategoryItemsDTO damDto = setDtoByType(damIcon, damCount, damModule, damCountPreviewAccess);
		dtos.add(damDto);

		CategoryItemsDTO lmsDto = setDtoByType(lmsIcon, lmsCount, lmsModule, lmsCountPreviewAccess);
		dtos.add(lmsDto);

		CategoryItemsDTO playBookDto = setDtoByType(playbookIcon, playbooksCount, playbookModule,
				playBooksCountPreviewAccess);
		dtos.add(playBookDto);

		response.setStatusCode(200);
		response.setData(dtos);
	}

	private CategoryItemsDTO setDtoByType(String iconPath, int itemsCount, String moduleName, boolean previewAccess) {
		CategoryItemsDTO campaignDto = new CategoryItemsDTO();
		campaignDto.setModuleIcon(iconPath);
		campaignDto.setModuleItemsCount(itemsCount);
		campaignDto.setModuleName(moduleName);
		campaignDto.setPreviewAccess(previewAccess);
		return campaignDto;
	}

	private boolean hasFolderAccess(Integer userId) {
		return utilService.hasFolderAccess(userId);
	}

	@Override
	public XtremandResponse findVendorCompanyCategoryNames(Integer userId, String domainName) {
		return new XtremandResponse();
	}

}
