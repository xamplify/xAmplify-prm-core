package com.xtremand.high.level.analytics.dao;

import java.util.List;

import com.xtremand.formbeans.XtremandResponse;
import com.xtremand.highlevel.analytics.bom.DownloadRequest;
import com.xtremand.highlevel.analytics.bom.DownloadStatus;
import com.xtremand.highlevel.analytics.dto.HighLevelAnalyticsActivePartnersDto;
import com.xtremand.highlevel.analytics.dto.HighLevelAnalyticsDto;
import com.xtremand.highlevel.analytics.dto.HighLevelAnalyticsInactivePartnersDto;
import com.xtremand.highlevel.analytics.dto.HighLevelAnalyticsOnboardPartnersDto;
import com.xtremand.highlevel.analytics.dto.HighLevelAnalyticsPartnerAndTeamMemberDto;
import com.xtremand.highlevel.analytics.dto.HighLevelAnalyticsShareLeadsDto;
import com.xtremand.highlevel.analytics.dto.HighLevelAnalyticsTotalContactDto;
import com.xtremand.vanity.url.dto.VanityUrlDetailsDTO;

public interface HighLevelAnalyticsDao {

	/******************* High Level Anaytics ***********************/

	XtremandResponse getActiveAndInActivePartnersForDonutChart(VanityUrlDetailsDTO vanityUrlDetailsDto);

	Integer getHighLevelAnalyticsDetailReportsForOnBoardPartners(VanityUrlDetailsDTO vanityUrlDetailsDTO);

	Integer getHighLevelAnalyticsDetailReportsForActivePartners(VanityUrlDetailsDTO vanityUrlDetailsDTO);

	Integer getHighLevelAnalyticsDetailReportsForTotalPartners(VanityUrlDetailsDTO vanityUrlDetailsDto);

	Integer getHighLevelAnalyticsDetailReportsForInActivePartners(VanityUrlDetailsDTO vanityUrlDetailsDto);

	Integer getHighLevelAnalyticsDetailReportsForShareLeadsTile(VanityUrlDetailsDTO vanityUrlDetailsDto);

	Integer getHighLevelAnalyticsDetailReportsForTotalUsersTile(VanityUrlDetailsDTO vanityUrlDetailsDto);

	List<HighLevelAnalyticsDto> getHighLevelAnalyticsTotalUsersDetailReport(Integer userId,
			VanityUrlDetailsDTO vanityUrlDetailsDto);

	List<HighLevelAnalyticsOnboardPartnersDto> getOnboardPartners(Integer userId,
			VanityUrlDetailsDTO vanityUrlDetailsDto);

	List<HighLevelAnalyticsPartnerAndTeamMemberDto> getHighLevelAnalyticsPartnerWhichIncludesPartnerAndTeamMember(
			Integer userId, VanityUrlDetailsDTO vanityUrlDetailsDto);

	List<HighLevelAnalyticsInactivePartnersDto> getInactivePartners(Integer userId,
			VanityUrlDetailsDTO vanityUrlDetailsDto);

	List<HighLevelAnalyticsActivePartnersDto> getActivePartners(Integer userId,
			VanityUrlDetailsDTO vanityUrlDetailsDto);

	List<HighLevelAnalyticsTotalContactDto> getTotalContacts(VanityUrlDetailsDTO vanityUrlDetailsDto);

	List<HighLevelAnalyticsShareLeadsDto> getShareLeads(VanityUrlDetailsDTO vanityUrlDetailsDto);

	List<HighLevelAnalyticsShareLeadsDto> getShareLeadsForSecondDrill(VanityUrlDetailsDTO vanityUrlDetailsDto);

	List<HighLevelAnalyticsShareLeadsDto> getShareLeadsForThirdDrill(VanityUrlDetailsDTO vanityUrlDetailsDto);

	void updateDownloadRequestStatus(Integer id, DownloadStatus downloadStatus);

	void updateDownloadFilePath(Integer id, String awsPath);

	boolean isDownloadRequestExists(Integer userId);

	DownloadRequest findById(Integer id);

	List<DownloadRequest> findFailedRequests();

	Integer getPartnerCountInExcel(VanityUrlDetailsDTO vanityUrlDetailsDto);

	Integer getTeamMemberCountInExcel(VanityUrlDetailsDTO vanityUrlDetailsDto);

}
