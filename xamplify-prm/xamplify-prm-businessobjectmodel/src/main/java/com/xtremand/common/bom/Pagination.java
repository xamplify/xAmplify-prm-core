package com.xtremand.common.bom;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.springframework.util.StringUtils;

import com.xtremand.form.dto.SelectedFieldsDTO;
import com.xtremand.formbeans.UserDTO;
import com.xtremand.lms.bom.LearningTrackType;

import lombok.Getter;
import lombok.Setter;

public class Pagination {

	private Integer pageIndex;
	private Integer offset;
	private Integer maxResults;
	private String sortcolumn;
	private String sortingOrder;
	private String searchKey;
	private String filterBy;
	private String filterKey;
	private Object filterValue;
	private boolean editCampaign;
	private Integer[] campaignUserListIds;
	private Integer userId;
	private boolean campaignDefaultTemplate;
	private String campaignType;
	private Integer campaignId;
	private Boolean isEmailTemplateSearchedFromCampaign;
	private Criteria[] criterias;
	private boolean coBrandedEmailTemplateSearch;
	private boolean throughPartnerAnalytics;
	private boolean reDistributedPartnerAnalytics;
	private Integer companyId;
	private boolean throughPartner;
	private Boolean showDraftContent;
	private boolean campaignForm;
	private boolean defaultLandingPage;
	private boolean landingPageForm;
	private boolean landingPageCampaignForm;
	private Integer landingPageId;
	private String landingPageAlias;
	private boolean partnerLandingPageForm;
	private int formId;// used for assetId too
	private Integer partnerCompanyId;
	private boolean publicEventLeads;
	private boolean addingMoreLists;
	private Integer partnerId;
	private boolean totalLeads;
	private boolean eventCampaign;
	private boolean exportToExcel;
	private boolean totalAttendees;
	private boolean totalPartnerLeads;
	private boolean selectedPartnerLeads;
	private boolean checkInLeads;
	private Integer userListId;
	private boolean teamMemberAnalytics;
	private Integer teamMemberId;
	private String categoryType;
	private Integer categoryId;
	private List<Integer> categoryIds;
	private boolean categoryFilter;
	private List<Integer> filtertedEmailTempalteIds;
	private boolean foldersForPartners;
	private boolean partnerView;
	private String formName;
	private String landingPageName;
	private String campaignName;
	private boolean loggedInAsTeamMember;
	/******* Vanity Url variables *******/
	private boolean vanityUrlFilter;
	private String vendorCompanyProfileName;
	private boolean vanityUrlFilterApplicable;
	private Integer vendorCompanyId;
	private boolean redistributingCampaign;
	private boolean sharedLeads;

	private Integer feedId;
	private Integer collectionId;

	private boolean mdfForm;
	private Integer partnershipId;

	private String pipelineType;
	private String type;
	private Integer learningTrackId;
	private boolean excludeLimit;

	private boolean ignoreSearch = false;

	private boolean surveyCampaignForm = false;
	private Date fromDateFilter;
	private Date toDateFilter;
	private String fromDateFilterString;
	private String toDateFilterString;

	private String filterFromDateString;
	private String filterToDateString;

	public String getFilterFromDateString() {
		return filterFromDateString;
	}

	public void setFilterFromDateString(String filterFromDateString) {
		this.filterFromDateString = filterFromDateString;
	}

	public String getFilterToDateString() {
		return filterToDateString;
	}

	public void setFilterToDateString(String filterToDateString) {
		this.filterToDateString = filterToDateString;
	}

	private boolean partnerTeamMemberGroupFilter;
	private boolean forCampaignAnalytics;
	private boolean archived = false;
	private String timeZone;
	private String stageFilter;
	private String companyNameFilter;
	private boolean oneClickLaunch;
	private boolean previewSelectedSharedLeads;
	@Getter
	@Setter
	private List<String> selectedRegionIds;

	@Getter
	@Setter
	private List<String> selectedStatusIds;
	@Getter
	@Setter
	private boolean isAdmin = false;

	@Getter
	@Setter
	private Integer[] selectedPartnerIds;

	private boolean singleMail;

	/*** XNFR-553 ***/
	@Getter
	@Setter
	private Integer contactId;
	/*** XNFR-553 ***/

	@Getter
	@Setter
	private boolean detailedAnalytics;
	@Getter
	@Setter
	private List<Integer> selectedPartnerCompanyIds;

	/************ user guides **********/
	@Getter
	@Setter
	private String moduleName;
	@Getter
	@Setter
	private String slug;
	@Getter
	@Setter
	private String guideTitle;
	/****** User Guides *******/

	@Getter
	@Setter
	private String customKey;

	@Getter
	@Setter
	private Integer loginAsUserId;

	@Getter
	@Setter
	private String trackTypeFilter;

	@Getter
	@Setter
	private String assetTypeFilter;

	@Getter
	@Setter
	private String campaignTypeFilter;

	@Getter
	@Setter
	private Integer selectedEmailTempalteId;

	@Getter
	@Setter
	private Integer selectedVideoId;

	private boolean searchWithModuleName = false;

	@Getter
	@Setter
	private boolean partnerJourneyFilter;

	@Getter
	@Setter
	private List<Integer> ids;

	/*** XNFR-409 *****/
	@Getter
	@Setter
	boolean filterOptionEnable = false;
	@Getter
	@Setter
	boolean dateFilterOpionEnable = false;
	@Getter
	@Setter
	boolean customFilterOption = false;
	/*** XNFR-409 *****/

	@Getter
	@Setter
	private Integer totalRecords;

	@Getter
	@Setter
	private String userType;

	@Getter
	@Setter
	private String source;

	@Getter
	@Setter
	private String campaignViewType;

	/*** XNFR-427 *****/
	private boolean ignoreSelfLeadsOrDeals = true;

	/*** XNFR-476 ***/
	private boolean showLeadsForAttachingLead;

	private boolean isLeadApprovalFeatureEnabledForVendorCompany = false;

	@Getter
	@Setter
	private List<Integer> selectedVendorCompanyIds;

	@Getter
	@Setter
	private List<Integer> selectedTeamMemberIds;

	@Getter
	@Setter
	private Integer id;

	@Getter
	@Setter
	private Integer registeredByCompanyId;

	@Getter
	@Setter
	private Integer registeredByUserId;

	/*** XNFR-522 *****/
	@Getter
	@Setter
	private boolean vendorJourneyOnly;

	/*** XNFR-583 *****/
	@Getter
	@Setter
	private boolean vendorJourney;

	@Getter
	@Setter
	private boolean masterLandingPage;

	@Getter
	@Setter
	private Integer masterLandingPageId;

	@Getter
	@Setter
	private boolean campaignAnalyticsSettingsOptionEnabled;

	@Getter
	@Setter
	private boolean vendorPages;

	@Getter
	@Setter
	private List<Integer> invalidVendorIds;

	@Getter
	@Setter
	private boolean masterLandingPageAnalytics;

	@Getter
	@Setter
	private Integer vendorLandingPageId;

	@Getter
	@Setter
	private boolean loggedInThroughOwnVanityUrl;

	@Getter
	@Setter
	private boolean loggedInThroughVendorVanityUrl;

	@Getter
	@Setter
	private boolean isVendorPreviewingDealsThroughShowRedistributedCampaigns;

	@Getter
	@Setter
	private boolean partnerJourneyPage;

	@Getter
	@Setter
	private boolean vendorMarketplacePage;

	@Getter
	@Setter
	private Integer vendorMarketplacePageId;

	@Getter
	@Setter
	private boolean vendorPartnerJourneyPage;

	@Getter
	@Setter
	private boolean vendorMarketplacePageAnalytics;

	@Getter
	@Setter
	private Integer userUserListId;

	@Getter
	@Setter
	private String selectedApprovalStatusCategory;

	/** XNFR-848 **/
	@Getter
	@Setter
	private Boolean isCompanyJourney;

	private Boolean vanityAccessAvailable;

	@Getter
	@Setter
	private boolean selectedType;

	@Getter
	@Setter
	private List<UserDTO> partners;

	@Getter
	@Setter
	private Integer damId;

	@Getter
	@Setter
	private String partnerSignatureType;

	@Getter
	@Setter
	private String selectedTab;

	@Getter
	@Setter
	private List<Integer> companyIds;

	public Boolean getIsVanityAccessAvailable() {
		return vanityAccessAvailable;
	}

	public void setIsVanityAccessAvailable(Boolean isVanityAccessAvailable) {
		this.vanityAccessAvailable = isVanityAccessAvailable;
	}

	/*** XNFR-839 ***/
	@Getter
	@Setter
	private List<SelectedFieldsDTO> selectedExcelFormFields;
	/*** XNFR-839 ***/

	@Getter
	@Setter
	private String regionFilter;

	@Getter
	@Setter
	private boolean fromPartnerAnalytics;

	@Getter
	@Setter
	private String category;

	@Getter
	@Setter
	private List<Integer> selectedCompanyIds;

	@Getter
	@Setter
	private List<String> selectedAssetNames;

	@Getter
	@Setter
	private List<String> selectedEmailIds;

	@Getter
	@Setter
	private List<String> selectedPlaybookNames;

	@Getter
	@Setter
	private List<Integer> assetIds;

	/*** XNFR-1006 ***/
	@Getter
	@Setter
	private String partnershipStatus;
	@Getter
	@Setter
	private String assetName;
	@Getter
	@Setter
	private String assetType;
	@Getter
	@Setter
	private String createdBy;
	@Getter
	@Setter
	private Date publishedOn;
	@Getter
	@Setter
	private String categoryName;
	@Getter
	@Setter
	private String partnerName;
	@Getter
	@Setter
	private String emailId;
	@Getter
	@Setter
	private boolean publishedFilter;

	public boolean isSearchWithModuleName() {
		return searchWithModuleName;
	}

	public boolean isPreviewSelectedSharedLeads() {
		return previewSelectedSharedLeads;
	}

	public void setPreviewSelectedSharedLeads(boolean previewSelectedSharedLeads) {
		this.previewSelectedSharedLeads = previewSelectedSharedLeads;
	}

	public boolean isOneClickLaunch() {
		return oneClickLaunch;
	}

	public void setOneClickLaunch(boolean oneClickLaunch) {
		this.oneClickLaunch = oneClickLaunch;
	}

	public boolean isPartnerTeamMemberGroupFilter() {
		return partnerTeamMemberGroupFilter;
	}

	public void setPartnerTeamMemberGroupFilter(boolean partnerTeamMemberGroupFilter) {
		this.partnerTeamMemberGroupFilter = partnerTeamMemberGroupFilter;
	}

	public String getFromDateFilterString() {
		return fromDateFilterString;
	}

	public void setFromDateFilterString(String fromDateFilterString) {
		this.fromDateFilterString = fromDateFilterString;
	}

	public String getToDateFilterString() {
		return toDateFilterString;
	}

	public void setToDateFilterString(String toDateFilterString) {
		this.toDateFilterString = toDateFilterString;
	}

	public Date getFromDateFilter() {
		return fromDateFilter;
	}

	public void setFromDateFilter(Date fromDateFilter) {
		this.fromDateFilter = fromDateFilter;
	}

	public Date getToDateFilter() {
		return toDateFilter;
	}

	public void setToDateFilter(Date toDateFilter) {
		this.toDateFilter = toDateFilter;
	}

	public boolean isIgnoreSearch() {
		return ignoreSearch;
	}

	public void setIgnoreSearch(boolean ignoreSearch) {
		this.ignoreSearch = ignoreSearch;
	}

	private LearningTrackType lmsType = LearningTrackType.TRACK;

	private boolean channelCampaign;



	public LearningTrackType getLmsType() {
		return lmsType;
	}

	public void setLmsType(LearningTrackType lmsType) {
		this.lmsType = lmsType;
	}

	public boolean isExcludeLimit() {
		return excludeLimit;
	}

	public void setExcludeLimit(boolean excludeLimit) {
		this.excludeLimit = excludeLimit;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Integer getLearningTrackId() {
		return learningTrackId;
	}

	public void setLearningTrackId(Integer learningTrackId) {
		this.learningTrackId = learningTrackId;
	}

	public String getType() {
		return type;
	}

	public void setLeadType(String type) {
		this.type = type;
	}

	public String getPipelineType() {
		return pipelineType;
	}

	private Integer parentCampaignId;

	public void setPipelineType(String pipelineType) {
		this.pipelineType = pipelineType;
	}

	public Integer getPartnershipId() {
		return partnershipId;
	}

	public void setPartnershipId(Integer partnershipId) {
		this.partnershipId = partnershipId;
	}

	public boolean isMdfForm() {
		return mdfForm;
	}

	public void setMdfForm(boolean mdfForm) {
		this.mdfForm = mdfForm;
	}

	public Integer getCollectionId() {
		return collectionId;
	}

	public void setCollectionId(Integer collectionId) {
		this.collectionId = collectionId;
	}

	public Integer getFeedId() {
		return feedId;
	}

	public void setFeedId(Integer feedId) {
		this.feedId = feedId;
	}

	public boolean isCategoryFilter() {
		return categoryFilter;
	}

	public void setCategoryFilter(boolean categoryFilter) {
		this.categoryFilter = categoryFilter;
	}

	public List<Integer> getCategoryIds() {
		return categoryIds;
	}

	public void setCategoryIds(List<Integer> categoryIds) {
		this.categoryIds = categoryIds;
	}

	public Integer getCategoryId() {
		return categoryId;
	}

	public void setCategoryId(Integer categoryId) {
		this.categoryId = categoryId;
	}

	public String getCategoryType() {
		return categoryType;
	}

	public void setCategoryType(String categoryType) {
		this.categoryType = categoryType;
	}

	public Integer getVendorCompanyId() {
		return vendorCompanyId;
	}

	public void setVendorCompanyId(Integer vendorCompanyId) {
		this.vendorCompanyId = vendorCompanyId;

	}

	public Integer getPartnerCompanyId() {
		return partnerCompanyId;
	}

	public void setPartnerCompanyId(Integer partnerCompanyId) {
		this.partnerCompanyId = partnerCompanyId;
	}

	public int getFormId() {
		return formId;
	}

	public void setFormId(int formId) {
		this.formId = formId;
	}

	public boolean isPartnerLandingPageForm() {
		return partnerLandingPageForm;
	}

	public void setPartnerLandingPageForm(boolean partnerLandingPageForm) {
		this.partnerLandingPageForm = partnerLandingPageForm;
	}

	public String getLandingPageAlias() {
		return landingPageAlias;
	}

	public void setLandingPageAlias(String landingPageAlias) {
		this.landingPageAlias = landingPageAlias;
	}

	public boolean isThroughPartner() {
		return throughPartner;
	}

	public void setThroughPartner(boolean throughPartner) {
		this.throughPartner = throughPartner;
	}

	public enum SORTINGORDER {
		ASC("ASC"), DESC("DESC");

		@SuppressWarnings("unused")
		private String sortOrder;

		private SORTINGORDER(String sortOrder) {
			this.sortOrder = sortOrder;
		}
	}

	public Integer getPageIndex() {
		return pageIndex;
	}

	public void setPageIndex(Integer pageIndex) {
		this.pageIndex = pageIndex;
	}

	public Integer getMaxResults() {
		return maxResults;
	}

	public void setMaxResults(Integer maxResults) {
		this.maxResults = maxResults;
	}

	public String getSortcolumn() {
		return sortcolumn;
	}

	public void setSortcolumn(String sortcolumn) {
		this.sortcolumn = sortcolumn;
	}

	public String getSortingOrder() {
		return sortingOrder;
	}

	public void setSortingOrder(String sortingOrder) {
		this.sortingOrder = sortingOrder;
	}

	public String getSearchKey() {
		return searchKey;
	}

	public void setSearchKey(String searchKey) {
		if (StringUtils.hasText(searchKey)) {
			this.searchKey = searchKey.trim().replace('\'', '"');
		} else {
			this.searchKey = searchKey;
		}

	}

	public String getFilterBy() {
		return filterBy;
	}

	public void setFilterBy(String filterBy) {
		this.filterBy = filterBy;
	}

	public boolean isEditCampaign() {
		return editCampaign;
	}

	public void setEditCampaign(boolean editCampaign) {
		this.editCampaign = editCampaign;
	}

	public Integer[] getCampaignUserListIds() {
		return campaignUserListIds;
	}

	public void setCampaignUserListIds(Integer[] campaignUserListIds) {
		this.campaignUserListIds = campaignUserListIds;
	}

	public Integer getUserId() {
		return userId;
	}

	public void setUserId(Integer userId) {
		this.userId = userId;
	}

	public boolean isCampaignDefaultTemplate() {
		return campaignDefaultTemplate;
	}

	public void setCampaignDefaultTemplate(boolean campaignDefaultTemplate) {
		this.campaignDefaultTemplate = campaignDefaultTemplate;
	}

	public Integer getCampaignId() {
		return campaignId;

	}

	public void setCampaignId(Integer campaignId) {
		this.campaignId = campaignId;
	}

	public Boolean getIsEmailTemplateSearchedFromCampaign() {
		return isEmailTemplateSearchedFromCampaign;
	}

	public void setIsEmailTemplateSearchedFromCampaign(Boolean isEmailTemplateSearchedFromCampaign) {
		this.isEmailTemplateSearchedFromCampaign = isEmailTemplateSearchedFromCampaign;
	}

	public String getFilterKey() {
		return filterKey;
	}

	public void setFilterKey(String filterKey) {
		this.filterKey = filterKey;
	}

	public Object getFilterValue() {
		return filterValue;
	}

	public void setFilterValue(Object filterValue) {
		this.filterValue = filterValue;
	}

	public Criteria[] getCriterias() {
		return criterias;
	}

	public void setCriterias(Criteria[] criterias) {
		this.criterias = criterias;
	}

	public boolean isCoBrandedEmailTemplateSearch() {
		return coBrandedEmailTemplateSearch;
	}

	public void setCoBrandedEmailTemplateSearch(boolean coBrandedEmailTemplateSearch) {
		this.coBrandedEmailTemplateSearch = coBrandedEmailTemplateSearch;
	}

	public String getCampaignType() {
		return campaignType;
	}

	public void setCampaignType(String campaignType) {
		this.campaignType = campaignType;
	}

	public Integer getCompanyId() {
		return companyId;
	}

	public void setCompanyId(Integer companyId) {
		this.companyId = companyId;
	}

	public boolean isThroughPartnerAnalytics() {
		return throughPartnerAnalytics;
	}

	public void setThroughPartnerAnalytics(boolean throughPartnerAnalytics) {
		this.throughPartnerAnalytics = throughPartnerAnalytics;
	}

	public boolean isReDistributedPartnerAnalytics() {
		return reDistributedPartnerAnalytics;
	}

	public void setReDistributedPartnerAnalytics(boolean reDistributedPartnerAnalytics) {
		this.reDistributedPartnerAnalytics = reDistributedPartnerAnalytics;
	}

	public Boolean getShowDraftContent() {
		return showDraftContent;
	}

	public void setShowDraftContent(Boolean showDraftContent) {
		this.showDraftContent = showDraftContent;
	}

	@Override
	public String toString() {
		return "Pagination [pageIndex=" + pageIndex + ", maxResults=" + maxResults + ", sortcolumn=" + sortcolumn
				+ ", sortingOrder=" + sortingOrder + ", searchKey=" + searchKey + ", filterBy=" + filterBy
				+ ", filterKey=" + filterKey + ", filterValue=" + filterValue + ", editCampaign=" + editCampaign
				+ ", campaignUserListIds=" + Arrays.toString(campaignUserListIds) + ", userId=" + userId
				+ ", campaignDefaultTemplate=" + campaignDefaultTemplate + ", campaignType=" + campaignType
				+ ", campaignId=" + campaignId + ", isEmailTemplateSearchedFromCampaign="
				+ isEmailTemplateSearchedFromCampaign + ", criterias=" + Arrays.toString(criterias)
				+ ", coBrandedEmailTemplateSearch=" + coBrandedEmailTemplateSearch + ", throughPartnerAnalytics="
				+ throughPartnerAnalytics + ", reDistributedPartnerAnalytics=" + reDistributedPartnerAnalytics
				+ ", companyId=" + companyId + "]";
	}

	/**
	 * @return the campaignForm
	 */
	public boolean isCampaignForm() {
		return campaignForm;
	}

	/**
	 * @param campaignForm the campaignForm to set
	 */
	public void setCampaignForm(boolean campaignForm) {
		this.campaignForm = campaignForm;
	}

	/**
	 * @return the defaultLandingPage
	 */
	public boolean isDefaultLandingPage() {
		return defaultLandingPage;
	}

	/**
	 * @param defaultLandingPage the defaultLandingPage to set
	 */
	public void setDefaultLandingPage(boolean defaultLandingPage) {
		this.defaultLandingPage = defaultLandingPage;
	}

	/**
	 * @return the landingPageForm
	 */
	public boolean isLandingPageForm() {
		return landingPageForm;
	}

	/**
	 * @param landingPageForm the landingPageForm to set
	 */
	public void setLandingPageForm(boolean landingPageForm) {
		this.landingPageForm = landingPageForm;
	}

	/**
	 * @return the landingPageId
	 */
	public Integer getLandingPageId() {
		return landingPageId;
	}

	/**
	 * @param landingPageId the landingPageId to set
	 */
	public void setLandingPageId(Integer landingPageId) {
		this.landingPageId = landingPageId;
	}

	/**
	 * @return the landingPageCampaignForm
	 */
	public boolean isLandingPageCampaignForm() {
		return landingPageCampaignForm;
	}

	/**
	 * @param landingPageCampaignForm the landingPageCampaignForm to set
	 */
	public void setLandingPageCampaignForm(boolean landingPageCampaignForm) {
		this.landingPageCampaignForm = landingPageCampaignForm;
	}

	public boolean isPublicEventLeads() {
		return publicEventLeads;
	}

	public void setPublicEventLeads(boolean publicEventLeads) {
		this.publicEventLeads = publicEventLeads;
	}

	public Integer getOffset() {
		return offset;
	}

	public void setOffset(Integer offset) {
		this.offset = offset;
	}

	public boolean isAddingMoreLists() {
		return addingMoreLists;
	}

	public void setAddingMoreLists(boolean addingMoreLists) {
		this.addingMoreLists = addingMoreLists;
	}

	public Integer getPartnerId() {
		return partnerId;
	}

	public void setPartnerId(Integer partnerId) {
		this.partnerId = partnerId;
	}

	public boolean isTotalLeads() {
		return totalLeads;
	}

	public void setTotalLeads(boolean totalLeads) {
		this.totalLeads = totalLeads;
	}

	public boolean isEventCampaign() {
		return eventCampaign;
	}

	public void setEventCampaign(boolean eventCampaign) {
		this.eventCampaign = eventCampaign;
	}

	public boolean isExportToExcel() {
		return exportToExcel;
	}

	public void setExportToExcel(boolean exportToExcel) {
		this.exportToExcel = exportToExcel;
	}

	public boolean isTotalAttendees() {
		return totalAttendees;
	}

	public void setTotalAttendees(boolean totalAttendees) {
		this.totalAttendees = totalAttendees;
	}

	public boolean isTotalPartnerLeads() {
		return totalPartnerLeads;
	}

	public void setTotalPartnerLeads(boolean totalPartnerLeads) {
		this.totalPartnerLeads = totalPartnerLeads;
	}

	public boolean isCheckInLeads() {
		return checkInLeads;
	}

	public void setCheckInLeads(boolean checkInLeads) {
		this.checkInLeads = checkInLeads;
	}

	public Integer getUserListId() {
		return userListId;
	}

	public void setUserListId(Integer userListId) {
		this.userListId = userListId;
	}

	public boolean isTeamMemberAnalytics() {
		return teamMemberAnalytics;
	}

	public void setTeamMemberAnalytics(boolean teamMemberAnalytics) {
		this.teamMemberAnalytics = teamMemberAnalytics;
	}

	public Integer getTeamMemberId() {
		return teamMemberId;
	}

	public void setTeamMemberId(Integer teamMemberId) {
		this.teamMemberId = teamMemberId;
	}

	public List<Integer> getFiltertedEmailTempalteIds() {
		return filtertedEmailTempalteIds;
	}

	public void setFiltertedEmailTempalteIds(List<Integer> filtertedEmailTempalteIds) {
		this.filtertedEmailTempalteIds = filtertedEmailTempalteIds;
	}

	public boolean isFoldersForPartners() {
		return foldersForPartners;
	}

	public void setFoldersForPartners(boolean foldersForPartners) {
		this.foldersForPartners = foldersForPartners;
	}

	public boolean isPartnerView() {
		return partnerView;
	}

	public void setPartnerView(boolean partnerView) {
		this.partnerView = partnerView;
	}

	public String getFormName() {
		return formName;
	}

	public void setFormName(String formName) {
		this.formName = formName;
	}

	public String getLandingPageName() {
		return landingPageName;
	}

	public void setLandingPageName(String landingPageName) {
		this.landingPageName = landingPageName;
	}

	public String getCampaignName() {
		return campaignName;
	}

	public void setCampaignName(String campaignName) {
		this.campaignName = campaignName;
	}

	public boolean isSelectedPartnerLeads() {
		return selectedPartnerLeads;
	}

	public void setSelectedPartnerLeads(boolean selectedPartnerLeads) {
		this.selectedPartnerLeads = selectedPartnerLeads;
	}

	public boolean isLoggedInAsTeamMember() {
		return loggedInAsTeamMember;
	}

	public void setLoggedInAsTeamMember(boolean loggedInAsTeamMember) {
		this.loggedInAsTeamMember = loggedInAsTeamMember;
	}

	public boolean isVanityUrlFilter() {
		return vanityUrlFilter;
	}

	public void setVanityUrlFilter(boolean vanityUrlFilter) {
		this.vanityUrlFilter = vanityUrlFilter;
	}

	public String getVendorCompanyProfileName() {
		return vendorCompanyProfileName;
	}

	public void setVendorCompanyProfileName(String vendorCompanyProfileName) {
		this.vendorCompanyProfileName = vendorCompanyProfileName;
	}

	public boolean isVanityUrlFilterApplicable() {
		return vanityUrlFilterApplicable;
	}

	public void setVanityUrlFilterApplicable(boolean vanityUrlFilterApplicable) {
		this.vanityUrlFilterApplicable = vanityUrlFilterApplicable;
	}

	public boolean isRedistributingCampaign() {
		return redistributingCampaign;
	}

	public void setRedistributingCampaign(boolean redistributingCampaign) {
		this.redistributingCampaign = redistributingCampaign;
	}

	public Integer getParentCampaignId() {
		return parentCampaignId;
	}

	public void setParentCampaignId(Integer parentCampaignId) {
		this.parentCampaignId = parentCampaignId;
	}

	public boolean isSharedLeads() {
		return sharedLeads;
	}

	public void setSharedLeads(boolean sharedLeads) {
		this.sharedLeads = sharedLeads;
	}

	public boolean isChannelCampaign() {
		return channelCampaign;
	}

	public void setChannelCampaign(boolean channelCampaign) {
		this.channelCampaign = channelCampaign;
	}

	public boolean isSurveyCampaignForm() {
		return surveyCampaignForm;
	}

	public void setSurveyCampaignForm(boolean surveyCampaignForm) {
		this.surveyCampaignForm = surveyCampaignForm;
	}

	public boolean isForCampaignAnalytics() {
		return forCampaignAnalytics;
	}

	public void setForCampaignAnalytics(boolean forCampaignAnalytics) {
		this.forCampaignAnalytics = forCampaignAnalytics;
	}

	public boolean isArchived() {
		return archived;
	}

	public void setArchived(boolean archived) {
		this.archived = archived;
	}

	public String getTimeZone() {
		return timeZone;
	}

	public void setTimeZone(String timeZone) {
		this.timeZone = timeZone;
	}

	public String getStageFilter() {
		return stageFilter;
	}

	public void setStageFilter(String stageFilter) {
		this.stageFilter = stageFilter;
	}

	public String getCompanyNameFilter() {
		return companyNameFilter;
	}

	public void setCompanyNameFilter(String companyNameFilter) {
		this.companyNameFilter = companyNameFilter;
	}

	public boolean isIgnoreSelfLeadsOrDeals() {
		return ignoreSelfLeadsOrDeals;
	}

	public void setIgnoreSelfLeadsOrDeals(boolean ignoreSelfLeadsOrDeals) {
		this.ignoreSelfLeadsOrDeals = ignoreSelfLeadsOrDeals;
	}

	/*** XNFR-476 ***/
	public boolean isShowLeadsForAttachingLead() {
		return showLeadsForAttachingLead;
	}

	public void setShowLeadsForAttachingLead(boolean showLeadsForAttachingLead) {
		this.showLeadsForAttachingLead = showLeadsForAttachingLead;
	}

	public boolean isLeadApprovalFeatureEnabledForVendorCompany() {
		return isLeadApprovalFeatureEnabledForVendorCompany;
	}

	public void setLeadApprovalFeatureEnabledForVendorCompany(boolean isLeadApprovalFeatureEnabledForVendorCompany) {
		this.isLeadApprovalFeatureEnabledForVendorCompany = isLeadApprovalFeatureEnabledForVendorCompany;
	}

	public boolean isSingleMail() {
		return singleMail;
	}

	public void setSingleMail(boolean singleMail) {
		this.singleMail = singleMail;
	}

	@Getter
	@Setter
	private boolean partnerMarketingCompany;

	@Getter
	@Setter
	private boolean showPartnerCreatedCampaigns;

	@Getter
	@Setter
	private boolean vendorPreviewingDealsThroughShowPartnerCreatedCampaigns;

}
