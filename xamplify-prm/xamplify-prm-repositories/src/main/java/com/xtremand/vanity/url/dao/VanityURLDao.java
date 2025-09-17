package com.xtremand.vanity.url.dao;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.xtremand.common.bom.CompanyProfile;
import com.xtremand.common.bom.Pagination;
import com.xtremand.dashboard.buttons.bom.DashboardButton;
import com.xtremand.dashboard.buttons.dto.DashboardButtonsDTO;
import com.xtremand.formbeans.EmailTemplateDTO;
import com.xtremand.vanity.email.templates.bom.CustomDefaultEmailTemplate;
import com.xtremand.vanity.email.templates.bom.DefaultEmailTemplate;
import com.xtremand.vanity.email.templates.bom.DefaultEmailTemplateType;
import com.xtremand.vanity.url.dto.VanityUrlDetailsDTO;

public interface VanityURLDao {

	public CompanyProfile getCompanyProfileByCompanyProfileName(String companyProfileName);

	public boolean isUserBelongsToCompany(String emailId, Integer companyId);

	public void saveDashboardButton(DashboardButton dashboardButton);

	public void updateDashboardButton(DashboardButton dashboardButton);

	public List<DashboardButton> getVendorDashboardButtons(CompanyProfile companyProfile);

	public List<DashboardButton> getVendorDashboardButtons();

	public Map<String, Object> getVendorDashboardButtons(Pagination pagination, String searchKey);

	public DashboardButton getDashboardButtonById(Integer id);

	public void deleteDashboardButtonById(Integer id);

	public DefaultEmailTemplate getVanityDefaultEmailTemplateById(Integer id);

	public DefaultEmailTemplate getVanityDefaultEmailTemplateByType(DefaultEmailTemplateType vanityEmailTemplateType);

	public CustomDefaultEmailTemplate getVanityETByDefVanityETIdAndCompanyId(Integer defaultVanityETId,
			CompanyProfile companyProfile);

	public Map<String, Object> getVanityDefaultEmailTemplates(Pagination pagination,
			VanityUrlDetailsDTO vanityUrlDetailsDTO);

	public Map<String, Object> getVanityEmailTemplates(CompanyProfile companyProfile, Pagination pagination,
			VanityUrlDetailsDTO vanityUrlDetailsDTO);

	public void deleteVanityEmailTemplateById(Integer defaultEmailTemplateId);

	public boolean getPrmCustomDefaultCount(Integer companyId);

	/***** XNFR-571 ******/
	public List<String> findExistingDashboardButtonsByCompanyId(Integer vendorCompanyId);

	public boolean isDashboardButtonPublished(Integer dashboardButtonId);

	public List<Integer> findPublishedPartnerGroupIdsByDashboardButtonId(Integer dashboardButtonId);

	public List<Integer> findPublishedPartnershipIdsByDashboardButtonId(Integer dashboardButtonId);

	public List<Integer> findPublishedPartnerIdsByDashboardButtonId(Integer dashboardButtonId);

	public void updatePubishedStatusByDashboardButtonId(Integer dashboardButtonId);

	public void deletePartnerGroupIds(Set<Integer> publishedPartnerGroupIds, Integer dashboardButtonId);

	public void deletePartnerIds(Set<Integer> publishedPartnerIds, Integer dashboardButtonId);

	public Integer getMaxOrderIdByCompanyId(Integer vendorCompanyId);

	List<DashboardButtonsDTO> findDashboardButtonsForPartnerView(Integer vendorCompanyId, Integer loggedInUserId);

	/***** XNFR-571 ******/

	public CompanyProfile getCompanyProfileByCustomDomain(String customDomain);

	public String getCompanyProfileNameByCustomDomain(String customDomain);

	List<Integer> getDefaultTemplateIdsFromCustomTemplateByCompanyId(Integer companyId);

	public List<List<String>> listAllTemplateDuplicates(Integer companyId);

	public List<Object[]> getCustomTemplates(String name, Integer companyId);

	public List<Object[]> getCustomTemplatesAsc(Integer companyId);

	public List<Object[]> getCustomTemplatesDesc(Integer companyId);

	/*** XNFR-832 ***/
	public Integer getDefaultTemplateIdByType(String templateTypeInString);
	
	
	public Map<String, Object> getVanityDefaultEmailTemplatesForPartner(Pagination pagination,
			VanityUrlDetailsDTO vanityUrlDetailsDTO);
	
	public Map<String, Object> getVanityEmailTemplatesForPartner(CompanyProfile companyProfile, Pagination pagination,
			VanityUrlDetailsDTO vanityUrlDetailsDTO);
	
	public void updateCustomEmailTemplateImagePath(List<Integer> ids, String awsImagePath);
	
	public EmailTemplateDTO getDefaultEmailTemplateById(Integer templateId, Integer userId);
	
	public EmailTemplateDTO getCustomEmailTemplate(Integer templateId, Integer userId);
}
