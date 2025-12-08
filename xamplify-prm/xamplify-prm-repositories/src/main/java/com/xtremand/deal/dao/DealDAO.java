package com.xtremand.deal.dao;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;

import com.xtremand.activity.dto.ContactOpportunityRequestDTO;
import com.xtremand.common.bom.Pagination;
import com.xtremand.deal.bom.Deal;
import com.xtremand.deal.dto.DealCountsResponseDTO;
import com.xtremand.deal.dto.DealDto;
import com.xtremand.integration.bom.Integration.IntegrationType;
import com.xtremand.lead.bom.Pipeline;

public interface DealDAO {

	Map<String, Object> getDealsForPartner(Pagination pagination);

	Map<String, Object> getDealsForVendor(Pagination pagination);

	DealCountsResponseDTO getCountsForVendor(Integer companyId, boolean applyFilter);

	DealCountsResponseDTO getCountsForPartner(Integer companyId);

	DealCountsResponseDTO getCountsForPartnerInVanity(Integer id, Integer vendorCompanyId);

	BigInteger getUnReadChatCount(Integer dealId, Integer loggedInUserId);

	BigInteger getUnReadPropertyChatCount(Integer propertyId, Integer loggedInUserId);

	DealCountsResponseDTO findDealsCountByFilter(String sqlQuery, Integer companyId, List<Integer> partnerCompanyIds);

	List<String> getStageNamesForVendor(Integer loggedInUserId, String activeCRM);

	List<String> getStageNamesForPartner(Integer loggedInUserId);

	List<String> getStageNamesForVendorInCampaign(Integer loggedInUserId);

	List<String> getStageNamesForPartnerCompanyId(Integer companyId);

	List<Deal> getDealsForVendorByType(Integer companyId, IntegrationType type);

	List<Deal> getDealsWithoutLeadAndWithSfDealIdForVendor(Integer companyId);

	Integer getDefaultDealOrLeadPipeLineIdByNameAndTypeAndCompanyId(Pipeline pipeLine);

	boolean isDefaultStageNameExistsByStageNameAndPipeLineId(String stageName, Integer pipeLineId);

	void updateDealTitle(Integer formId, String labelId);

	void updateDealAmount(Integer formId, String labelId);

	void updateDealCloseDate(Integer formId, String labelId);

	List<Deal> getDealsForSyncByType(Integer companyId, IntegrationType type);

	List<Deal> getDealsWithoutLeadAndWithSfDealId(Integer companyId);

	Integer deleteDealById(Integer dealId);

	Map<String, Object> queryDealsForPartner(Pagination pagination);

	Map<String, Object> queryDealsForVendor(Pagination pagination);

	/** XNFR-553 **/
	Map<String, Object> findDealsAndCountByContactId(ContactOpportunityRequestDTO contactOpportunityRequestDTO);

	/** XNFR-848 **/
	Map<String, Object> fetchDealsForCompanyJourney(Pagination pagination);

	Double fetchTotalDealAmount(ContactOpportunityRequestDTO contactOpportunityRequestDTO);

	/** XNFR-892 **/
	DealDto fetchMergeTagsDataForPartnerMailNotification(Integer dealId);

	/** XNFR-1012 **/
	List<DealDto> fetchDealsForContactReport(String dynamicQueryCondition, Integer createdByCompanyId,
			Integer createdForCompanyId, Integer limit);

	List<DealDto> fetchDealsForContact(Integer contactId, Integer userListId, Integer createdByCompanyId,
			Integer createdForCompanyId);

	List<DealDto> fetchDealsForPartnerReport(Integer createdByCompanyId, Integer createdForCompanyId,
			Integer userListId, Integer queryLimit);

	void getUserUserDetailsForDeal(DealDto dealDto, Integer partnerId, Integer vendorId);
	
	List<Deal> findDealsByCreatedForCompanyId(Integer createdForCompanyId);

}
