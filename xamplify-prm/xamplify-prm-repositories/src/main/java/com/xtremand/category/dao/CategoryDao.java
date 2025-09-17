package com.xtremand.category.dao;

import java.util.List;

import com.xtremand.category.bom.Category;
import com.xtremand.category.bom.CategoryModule;
import com.xtremand.category.bom.CategoryModuleEnum;
import com.xtremand.category.bom.CategoryView;
import com.xtremand.category.dto.CategoryDTO;
import com.xtremand.category.dto.CategoryItemsCountDTO;
import com.xtremand.common.bom.Pagination;
import com.xtremand.dao.util.FinderDAO;

public interface CategoryDao extends FinderDAO<Category> {

	public List<CategoryView> findByCompanyId(Integer companyId);

	public List<Object[]> findAllCategories();

	public void save(Category category);

	public List<CategoryDTO> listAvailableCategories(Integer companyId);

	public List<CategoryDTO> listAvailableCategoriesByUserId(Integer userId);

	public CategoryView findById(Integer id);

	public void update(CategoryDTO categoryDTO);

	public void deleteById(Integer categoryId);

	public void moveAndDeleteCategory(Integer categoryIdToDelete, Integer categoryIdToMove);

	public List<Object[]> listAllCompanyDetails(String type);

	public Integer getUserIdByCompanyId(Integer companyId);

	public List<Integer> listEmailTemplateIdsByCompanyId(Integer companyId);

	public List<Integer> listLandingPagesByCompanyId(Integer companyId);

	public List<Integer> listCampaignIdsByCompanyId(Integer companyId);

	public List<Integer> listRedistributedCampaignIdsByCompanyId(Integer companyId);

	public void addCategoryModules(List<CategoryModule> categoryModules);

	public void saveCategoryModule(CategoryModule categoryModule);

	public Integer getCategoryIdByType(Integer itemId, String type);

	public void updateCategoryIdByType(Integer itemId, Integer categoryId, Integer updatedUserId, String type);

	public String getCategoryName(Integer inputId, CategoryModuleEnum categoryModuleEnum);

	public String getCategoryNameForLandingPage(Integer inputId, CategoryModuleEnum categoryModuleEnum);

	public List<Integer> listEmailTemplateIdsByCategoryId(Integer categoryId);

	public List<Integer> listEmailTemplateIdsByCategoryIds(List<Integer> categoryIds);

	public List<Integer> listFormIdsByCompanyId(Integer companyId);

	public List<Integer> listLandingPageIdsByCategoryId(Integer categoryId);

	public List<Integer> listCampaignIdsByCategoryId(Integer categoryId);

	public Integer getDefaultCategoryIdByCompanyId(Integer companyId);

	public List<Object[]> listFoldersByType(Integer userId, CategoryModuleEnum categoryModuleEnum);

	public CategoryItemsCountDTO getItemsCountDetailsByCategoryId(Integer categoryId);

	public Integer getDefaultCategoryIdByUserId(Integer userId);

	public List<Integer> getCampaignCategoryIdsByTeamMemberId(Integer teamMemberId);

	public Integer getCampaignItemsCountByCategoryIdAndUserId(Integer userId, Integer categoryId,
			CategoryModuleEnum categoryModuleEnum, boolean archived);

	public Integer getRedistributedCampaignItemsCountByCategoryIdAndPartnerCompanyId(Integer categoryId,
			Integer partnerCompanyId, CategoryModuleEnum categoryModuleEnum);

	public List<Integer> getSharedPageCategoryIdsByPartnerCompanyId(Integer partnerCompanyId);

	public Integer getPageItemsCountByCategoryIdAndPartnerCompanyId(Integer categoryId, Integer partnerCompanyId,
			CategoryModuleEnum categoryModuleEnum);

	public List<Integer> getRedistributedCampaignCategoryIdsByPartnerCompanyId(Integer partnerCompanyId);

	public CategoryDTO getCategoryIdAndNameByType(Integer moduleTypeId, String type);

	public Integer getCampaignCategoryCountByCampaignId(Integer campaignId);

	public Integer getPageCategoryCountByPageId(Integer pageId);

	public Integer getFormsCategroyCountByFormId(Integer formId);

	public Integer getEmailTemplateCategroyCountByEmailTemplateId(Integer emailTemplateId);

	public List<Integer> getVanityUrlFoldersIds(Integer vendorCompanyId, Integer partnerCompanyId);

	public List<Integer> getVanityUrlRedistributedCampaignCategoryIds(Integer vendorCompanyId,
			Integer partnerCompanyId);

	public List<Integer> getVanityUrlCampaignCategoryIdsByPartner(List<Integer> userIds, Integer vendorCompanyId);

	public Integer getCampaignItemsCountByCategoryIdAndVendorCompanyIdAndUserIds(List<Integer> userIds,
			Integer vendorCompanyId, Integer categoryId);

	public List<Integer> getCampaignCategoryIdsByTeamMemberIdForVanityUrl(Integer teamMemberId,
			Integer vendorCompanyId);

	public Integer getCampaignItemsCountByCategoryIdAndUserIdAndVendorCompanyId(Integer userId, Integer categoryId,
			CategoryModuleEnum categoryModuleEnum, Integer vendorCompanyId, boolean archived);

	public Integer getItemsCountExcludingRedistributedCampaigns(Integer categoryId, Integer companyId,
			List<Integer> campaignIds);

	public Integer getCampaignsCountExcludingRedistributedCampaigns(Integer categoryId, Integer companyId,
			List<Integer> campaignIds);

	public List<Integer> getCategoryIdsExcludingRedistributingCampaings(List<Integer> userIds);

	public Integer getCampaignItemsCountByCategoryIdAndTeamMemberId(Integer teamMemberId, Integer categoryId,
			boolean archived);

	public CategoryModule getCategoryModuleByLearningTrackId(Integer learningTrackId);

	public List<Integer> getSharedAssetsCategoryIdsByPartnerCompanyId(Integer partnerCompanyId, Integer partnerId);

	public Integer getSharedAssetsCountByCategoryIdAndPartnerCompanyId(Integer categoryId, Integer partnerCompanyId,
			CategoryModuleEnum categoryModuleEnum, Integer loggedInUserId);

	public List<Integer> getVanityUrlFoldersIdsForSharedAssets(Integer vendorCompanyId, Integer partnerCompanyId,
			Integer partnerId);

	public List<Integer> findCategoryModuleIdsForPlayBooksPatch();

	public List<Integer> getSharedTracksCategoryIdsByPartnerCompanyId(Integer partnerCompanyId, Integer partnerId);

	public Integer getSharedTracksCountByCategoryIdAndPartnerCompanyId(Integer categoryId, Integer partnerCompanyId,
			Integer loggedInUserId);

	public List<Integer> getVanityUrlFoldersIdsForSharedTracks(Integer vendorCompanyId, Integer partnerCompanyId,
			Integer partnerId);

	public List<Integer> getSharedPlayBooksCategoryIdsByPartnerCompanyId(Integer partnerCompanyId, Integer partnerId);

	public Integer getSharedPlayBooksCountByCategoryIdAndPartnerCompanyId(Integer categoryId, Integer partnerCompanyId,
			Integer loggedInUserId);

	public List<Integer> getVanityUrlFoldersIdsForSharedPlayBooks(Integer vendorCompanyId, Integer partnerCompanyId,
			Integer partnerId);

	public void updateHistoryTemplatesCategoryId(Integer categoryId, Integer damId, Integer updatedUserId);

	public List<Integer> getVendorCompanyCategoryIdsForLoginAsPartner(List<Integer> userIds);

	public Integer getVendorCompanyCampaignItemsCountByUserIdsAndCategoryId(List<Integer> userIds, Integer categoryId);

	public Integer isDefaultFolderExistsByCompanyId(Integer companyId);

	public Integer findEmailTemplateCategoryCount(Integer categoryId, Pagination pagination);

	public Integer findPartnerCreatedCampaignsCountByCategoryId(Integer vendorCompanyId, Integer categoryId);

	public CategoryItemsCountDTO findCategoryCountDetails(Integer categoryId, Integer vendorCompanyId,
			boolean hasPartnerAccess);

}
