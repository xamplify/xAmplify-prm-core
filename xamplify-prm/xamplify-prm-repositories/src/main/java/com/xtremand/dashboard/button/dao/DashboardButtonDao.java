package com.xtremand.dashboard.button.dao;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.xtremand.common.bom.Pagination;
import com.xtremand.dashboard.buttons.dto.DashboardAlternateUrlDTO;
import com.xtremand.dashboard.buttons.dto.DashboardButtonsPartnersDTO;
import com.xtremand.dashboard.buttons.dto.PublishedDashboardButtonDetailsDTO;

public interface DashboardButtonDao {

	List<Integer> findPublishedPartnerGroupPartnerIds(Integer userListId, Integer dashboardButtonId);

	List<DashboardButtonsPartnersDTO> findUserListIdAndPartnerIdAndPartnershipIds(Integer userListId,
			Integer dashboardButtonId);

	List<PublishedDashboardButtonDetailsDTO> findPublishedDashboardButtonIdAndTitlesByPartnerListIdAndCompanyId(
			Set<Integer> partnerGroupIds, Integer companyId);

	public List<DashboardButtonsPartnersDTO> findUserListIdsAndPartnerIdsAndPartnershipIdsByPartnerGroupIdAndPartnerIds(
			Integer partnerGroupId, Set<Integer> partnerIds);

	void updateStatus(Set<Integer> dashboardButtonIds, boolean isPublishingInProgress);
	
	void updateStatus(Integer dashboardButtonId, boolean isPublishingInProgress);

	boolean isPublishingInProgress(Integer dashboardButtonId);

	List<Integer> findUnPublishedDashboardButtonIdsByCompanyId(Integer companyId);

	Map<String, Object> findAllPublishedAndUnPublished(Pagination pagination, String search);

	boolean isDashboardButtonPublished(Integer dashboardButtonId, Integer partnershipId, Integer userListId,
			Integer partnerUserId);

	void publishDashboardButtonToPartnerCompanyByPartnerId(Integer dashboardButtonId, Integer partnershipId,
			Integer userUserListId, Integer loggedInUserId);
	
	public Set<String> saveDashboardButtonsMapping(List<DashboardButtonsPartnersDTO> dashboardButtonsPartnerDTOs,Integer dashboardId, Integer vendorId, List<Integer> publishedPartnerIds);
	
	public Set<String> savePartnerCompanyDashboardbuttons(Integer dashboardId,
			Integer vendorId, DashboardButtonsPartnersDTO dashboardButtonsPartnerDTO);
	
	 public List<Integer> findPublishedPartnershipIds(Integer dashboardButtonsId);
	
	 public List<DashboardAlternateUrlDTO> findAlternateUrls(String refarenceUrl);

}
