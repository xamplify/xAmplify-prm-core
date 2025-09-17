package com.xtremand.analytics.service.impl;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.xtremand.analytics.dao.TeamMemberAnalyticsDAO;
import com.xtremand.analytics.service.TeamMemberAnalyticsService;
import com.xtremand.common.bom.Pagination;
import com.xtremand.deal.dto.DealDto;
import com.xtremand.formbeans.XtremandResponse;
import com.xtremand.lead.dto.LeadDto;
import com.xtremand.mdf.dto.MdfRequestDeatailsDTO;
import com.xtremand.team.member.dto.TeamMemberDTO;
import com.xtremand.user.bom.ShareLeadsDTO;
import com.xtremand.user.dao.UserDAO;
import com.xtremand.user.list.dto.ContactsDetailsDTO;
import com.xtremand.util.DateUtils;
import com.xtremand.util.XamplifyUtil;
import com.xtremand.util.XamplifyUtils;
import com.xtremand.util.dao.UtilDao;
import com.xtremand.util.dto.AllPartnersDetailsDTO;
import com.xtremand.util.dto.BarChartDualAxesDTO;
import com.xtremand.util.dto.PartnerJourneyTrackDetailsDTO;
import com.xtremand.util.dto.TeamMemberAnalyticsDTO;
import com.xtremand.util.dto.TeamMemberAnalyticsRequestDTO;
import com.xtremand.util.service.UtilService;

@Service("teamMemberAnalyticsService")
@Transactional
public class TeamMemberAnalyticsServiceImpl implements TeamMemberAnalyticsService {

	@Autowired
	UserDAO userDao;

	@Autowired
	UtilDao utilDAO;

	@Autowired
	private XamplifyUtil xamplifyUtil;

	@Autowired
	TeamMemberAnalyticsDAO teamMemberAnalyticsDao;

	@Autowired
	UtilService utilService;

	@Value("${partnerJourneyInteracted}")
	private String partnerJourneyInteracted;

	@Value("${partnerJourneyNotInteracted}")
	private String partnerJourneyNotInteracted;

	private static final String TEAM_MEMBER_NAME = "TEAM MEMBER NAME";

	private static final String TEAM_MEMBER_EMAIL_ID = "TEAM MEMBER EMAIL ID";

	private static final String VENDOR_TEAM_MEMBER_EMAIL_ID = "VENDOR TEAM MEMBER EMAIL ID";

	private static final String PARTNER_TEAM_MEMBER_EMAIL_ID = "PARTNER TEAM MEMBER EMAIL ID";

	private static final String VENDOR_TEAM_MEMBER_NAME = "VENDOR TEAM MEMBER NAME";

	private static final String VENDOR_COMPANY = "VENDOR COMPANY NAME";

	private static final String VENDOR_EMAIL_ID = "VENDOR EMAIL ID";

	private static final String TRACK_TITLE = "TRACK TITLE";

	private static final String ASSET_TITLE = "ASSET NAME";

	private static final String ASSET_TYPE = "ASSET TYPE";

	private static final String PUBLISHED_TIME = "PUBLISHED TIME(PST)";

	private static final String AVG_PROGRESS = "AVERAGE TRACK PROGRESS";

	private static final String ASSET_CREATED_TIME = "ASSET CREATED TIME(PST)";

	private static final String OPENED_COUNT = "OPENED COUNT";

	private static final String CREATED_ON = "CREATED ON(PST)";

	private static final String CREATED_BY_NAME = "CREATED BY NAME";

	private static final String CREATED_BY_EMAIL_ID = "CREATED BY EMAIL ID";

	private static final String CREATED_FOR_COMPANY_NAME = "CREATED FOR COMPANY NAME";

	private static final String LIST_NAME = "LIST NAME";

	private static final String CONTACTS_COUNT = "CONTACTS COUNT";

	@Override
	public XtremandResponse getTeamMemberJourneyCounts(TeamMemberAnalyticsRequestDTO teamMemberJourneyRequestDTO) {
		XtremandResponse response = new XtremandResponse();
		response.setStatusCode(400);
		if (teamMemberJourneyRequestDTO.getLoggedInUserId() != null
				&& teamMemberJourneyRequestDTO.getLoggedInUserId() > 0) {
			Integer partnerUserId = teamMemberJourneyRequestDTO.getLoggedInUserId();
			Integer partnerCompanyId = userDao.getCompanyIdByUserId(teamMemberJourneyRequestDTO.getLoggedInUserId());
			teamMemberJourneyRequestDTO.setPartnerCompanyId(partnerCompanyId);
			if (partnerCompanyId != null && partnerCompanyId > 0) {
				TeamMemberAnalyticsDTO teamMemberAnalytics = new TeamMemberAnalyticsDTO();
				utilService.setDateFiltersForTeamMemberAnalytics(teamMemberJourneyRequestDTO);

				if (utilDAO.sharedLeadsAccessForPartner(partnerUserId)) {
					String shareLeadCount = teamMemberAnalyticsDao
							.getTeamMembersShareLeadCount(teamMemberJourneyRequestDTO);
					teamMemberAnalytics.setShareLeadCount(shareLeadCount);
					teamMemberAnalytics.setShowShareLeadCount(true);
				}

				if (utilDAO.enableLeadsForPartner(partnerUserId)) {
					String leadCount = teamMemberAnalyticsDao.getTeamMembersLeadCount(teamMemberJourneyRequestDTO);
					String dealCount = teamMemberAnalyticsDao.getTeamMembersDealCount(teamMemberJourneyRequestDTO);
					teamMemberAnalytics.setLeadCount(leadCount);
					teamMemberAnalytics.setDealCount(dealCount);
					teamMemberAnalytics.setShowLeadCount(true);
					teamMemberAnalytics.setShowDealCount(true);
				}

				if (utilDAO.damAccessForPartner(partnerUserId)) {
					String assetCount = teamMemberAnalyticsDao.getTeamMembersAssetCount(teamMemberJourneyRequestDTO);
					teamMemberAnalytics.setAssetCount(assetCount);
					teamMemberAnalytics.setShowAssetCount(true);
				}

				if (utilDAO.lmsAccessForPartner(partnerUserId) || utilDAO.playbookAccessForPartner(partnerUserId)) {
					TeamMemberAnalyticsDTO trackAndPlaybookCountDto = teamMemberAnalyticsDao
							.getTeamMembersTrackAndPlaybookCount(teamMemberJourneyRequestDTO);
					if (trackAndPlaybookCountDto != null) {
						if (utilDAO.lmsAccessForPartner(partnerUserId)) {
							teamMemberAnalytics.setTrackCount(trackAndPlaybookCountDto.getTrackCount());
							teamMemberAnalytics.setShowTrackCount(true);
						}
						if (utilDAO.playbookAccessForPartner(partnerUserId)) {
							teamMemberAnalytics.setPlaybookCount(trackAndPlaybookCountDto.getPlaybookCount());
							teamMemberAnalytics.setShowPlaybookCount(true);
						}
					}
				}

				if (!utilDAO.isPartnershipEstablishedOnlyWithPrm(teamMemberJourneyRequestDTO.getLoggedInUserId())) {
					String contactCount = teamMemberAnalyticsDao
							.getTeamMembersContactCount(teamMemberJourneyRequestDTO);
					teamMemberAnalytics.setContactCount(contactCount);
					teamMemberAnalytics.setShowContactCount(true);
				}

				if (!utilDAO.isPartnershipEstablishedOnlyWithPrm(teamMemberJourneyRequestDTO.getLoggedInUserId())) {
					String companyCount = teamMemberAnalyticsDao
							.getTeamMembersCompanyCount(teamMemberJourneyRequestDTO);
					teamMemberAnalytics.setCompanyCount(companyCount);
					teamMemberAnalytics.setShowCompanyCount(true);
				}

				if (utilDAO.lmsAccessForPartner(partnerUserId)) {
					String trackAssetCount = teamMemberAnalyticsDao
							.getTeamMembersTrackAssetCount(teamMemberJourneyRequestDTO);
					teamMemberAnalytics.setTrackAssetCount(trackAssetCount);
					teamMemberAnalytics.setShowTrackAssetCount(true);
				}

				response.setData(teamMemberAnalytics);
				response.setStatusCode(200);
			}
		}
		return response;
	}

	@Override
	public XtremandResponse getTeamMemberTrackCountsByInteraction(
			TeamMemberAnalyticsRequestDTO teamMemberJourneyRequestDTO) {
		XtremandResponse response = new XtremandResponse();
		response.setStatusCode(400);
		if (teamMemberJourneyRequestDTO.getLoggedInUserId() != null
				&& teamMemberJourneyRequestDTO.getLoggedInUserId() > 0) {
			Integer partnerCompanyId = userDao.getCompanyIdByUserId(teamMemberJourneyRequestDTO.getLoggedInUserId());
			teamMemberJourneyRequestDTO.setPartnerCompanyId(partnerCompanyId);
			if (partnerCompanyId != null && partnerCompanyId > 0) {
				utilService.setDateFiltersForTeamMemberAnalytics(teamMemberJourneyRequestDTO);
				List<List<Object>> trackCounts = new ArrayList<List<Object>>();
				PartnerJourneyTrackDetailsDTO partnerJourneyTrackDetailsDTO = teamMemberAnalyticsDao
						.getTeamMemberTrackCountsByInteraction(teamMemberJourneyRequestDTO);
				if (partnerJourneyTrackDetailsDTO != null) {
					xamplifyUtil.addItemsToArrayList(trackCounts, partnerJourneyInteracted,
							partnerJourneyTrackDetailsDTO.getOpenedCount());
					xamplifyUtil.addItemsToArrayList(trackCounts, partnerJourneyNotInteracted,
							partnerJourneyTrackDetailsDTO.getNotOpenedCount());
				}
				response.setData(trackCounts);
				response.setStatusCode(200);
			}
		}
		return response;
	}

	@Override
	public XtremandResponse getVendorTeamMemberTrackCountsByInteraction(
			TeamMemberAnalyticsRequestDTO teamMemberJourneyRequestDTO) {
		XtremandResponse response = new XtremandResponse();
		response.setStatusCode(400);
		if (teamMemberJourneyRequestDTO.getLoggedInUserId() != null
				&& teamMemberJourneyRequestDTO.getLoggedInUserId() > 0) {
			Integer vendorCompanyId = userDao.getCompanyIdByUserId(teamMemberJourneyRequestDTO.getLoggedInUserId());
			teamMemberJourneyRequestDTO.setVendorCompanyId(vendorCompanyId);
			if (vendorCompanyId != null && vendorCompanyId > 0) {
				utilService.setDateFiltersForTeamMemberAnalytics(teamMemberJourneyRequestDTO);
				List<List<Object>> trackCounts = new ArrayList<List<Object>>();
				PartnerJourneyTrackDetailsDTO partnerJourneyTrackDetailsDTO = teamMemberAnalyticsDao
						.getTeamMemberTrackCountsByInteractionForVendorTeamMember(teamMemberJourneyRequestDTO);
				if (partnerJourneyTrackDetailsDTO != null) {
					xamplifyUtil.addItemsToArrayList(trackCounts, partnerJourneyInteracted,
							partnerJourneyTrackDetailsDTO.getOpenedCount());
					xamplifyUtil.addItemsToArrayList(trackCounts, partnerJourneyNotInteracted,
							partnerJourneyTrackDetailsDTO.getNotOpenedCount());
				}
				response.setData(trackCounts);
				response.setStatusCode(200);
			}
		}
		return response;
	}

	@Override
	public XtremandResponse getTeamMemberTrackDetailsByInteraction(Pagination pagination) {
		XtremandResponse response = new XtremandResponse();
		response.setStatusCode(400);
		if (pagination.getUserId() != null && pagination.getUserId() > 0) {
			Integer partnerCompanyId = userDao.getCompanyIdByUserId(pagination.getUserId());
			pagination.setPartnerCompanyId(partnerCompanyId);
			if (partnerCompanyId != null && partnerCompanyId > 0) {
				utilService.setDateFilters(pagination);
				response.setData(teamMemberAnalyticsDao.getTeamMemberTrackDetailsByInteraction(pagination));
				response.setStatusCode(200);
			}
		}
		return response;
	}

	@Override
	public XtremandResponse getVendorTeamMemberTrackDetailsByInteraction(Pagination pagination) {
		XtremandResponse response = new XtremandResponse();
		response.setStatusCode(400);
		if (pagination.getUserId() != null && pagination.getUserId() > 0) {
			Integer vendorCompanyId = userDao.getCompanyIdByUserId(pagination.getUserId());
			pagination.setVendorCompanyId(vendorCompanyId);
			if (vendorCompanyId != null && vendorCompanyId > 0) {
				utilService.setDateFilters(pagination);
				response.setData(teamMemberAnalyticsDao.getTeamMemberTrackDetailsByInteractionForVendor(pagination));
				response.setStatusCode(200);
			}
		}
		return response;
	}

	@Override
	public XtremandResponse getTeamMemberTrackCountsByType(TeamMemberAnalyticsRequestDTO teamMemberJourneyRequestDTO) {
		XtremandResponse response = new XtremandResponse();
		response.setStatusCode(400);
		if (teamMemberJourneyRequestDTO.getLoggedInUserId() != null
				&& teamMemberJourneyRequestDTO.getLoggedInUserId() > 0) {
			Integer partnerCompanyId = userDao.getCompanyIdByUserId(teamMemberJourneyRequestDTO.getLoggedInUserId());
			teamMemberJourneyRequestDTO.setPartnerCompanyId(partnerCompanyId);
			if (partnerCompanyId != null && partnerCompanyId > 0) {
				utilService.setDateFiltersForTeamMemberAnalytics(teamMemberJourneyRequestDTO);
				List<List<Object>> trackCounts = new ArrayList<List<Object>>();
				PartnerJourneyTrackDetailsDTO partnerJourneyTrackDetailsDTO = teamMemberAnalyticsDao
						.getTeamMemberTrackCountsByType(teamMemberJourneyRequestDTO);
				if (partnerJourneyTrackDetailsDTO != null) {
					xamplifyUtil.addItemsToArrayList(trackCounts, "Opened",
							partnerJourneyTrackDetailsDTO.getOpenedCount());
					xamplifyUtil.addItemsToArrayList(trackCounts, "Not Opened",
							partnerJourneyTrackDetailsDTO.getNotOpenedCount());
					xamplifyUtil.addItemsToArrayList(trackCounts, "Viewed",
							partnerJourneyTrackDetailsDTO.getViewedCount());
					xamplifyUtil.addItemsToArrayList(trackCounts, "Downloaded",
							partnerJourneyTrackDetailsDTO.getDownloadedCount());
					xamplifyUtil.addItemsToArrayList(trackCounts, "Submitted",
							partnerJourneyTrackDetailsDTO.getSubmittedCount());
				}
				response.setData(trackCounts);
				response.setStatusCode(200);
			}
		}
		return response;
	}

	@Override
	public XtremandResponse getVendorTeamMemberTrackCountsByType(
			TeamMemberAnalyticsRequestDTO teamMemberJourneyRequestDTO) {
		XtremandResponse response = new XtremandResponse();
		response.setStatusCode(400);
		if (teamMemberJourneyRequestDTO.getLoggedInUserId() != null
				&& teamMemberJourneyRequestDTO.getLoggedInUserId() > 0) {
			Integer vendorCompanyId = userDao.getCompanyIdByUserId(teamMemberJourneyRequestDTO.getLoggedInUserId());
			teamMemberJourneyRequestDTO.setVendorCompanyId(vendorCompanyId);
			if (vendorCompanyId != null && vendorCompanyId > 0) {
				utilService.setDateFiltersForTeamMemberAnalytics(teamMemberJourneyRequestDTO);
				List<List<Object>> trackCounts = new ArrayList<List<Object>>();
				PartnerJourneyTrackDetailsDTO partnerJourneyTrackDetailsDTO = teamMemberAnalyticsDao
						.getTeamMemberTrackCountsByTypeForVendor(teamMemberJourneyRequestDTO);
				if (partnerJourneyTrackDetailsDTO != null) {
					xamplifyUtil.addItemsToArrayList(trackCounts, "Opened",
							partnerJourneyTrackDetailsDTO.getOpenedCount());
					xamplifyUtil.addItemsToArrayList(trackCounts, "Not Opened",
							partnerJourneyTrackDetailsDTO.getNotOpenedCount());
					xamplifyUtil.addItemsToArrayList(trackCounts, "Viewed",
							partnerJourneyTrackDetailsDTO.getViewedCount());
					xamplifyUtil.addItemsToArrayList(trackCounts, "Downloaded",
							partnerJourneyTrackDetailsDTO.getDownloadedCount());
					xamplifyUtil.addItemsToArrayList(trackCounts, "Submitted",
							partnerJourneyTrackDetailsDTO.getSubmittedCount());
				}
				response.setData(trackCounts);
				response.setStatusCode(200);
			}
		}
		return response;
	}

	@Override
	public XtremandResponse getTeamMemberTrackAssetDetailsByType(Pagination pagination) {
		XtremandResponse response = new XtremandResponse();
		response.setStatusCode(400);
		if (pagination.getUserId() != null && pagination.getUserId() > 0) {
			Integer partnerCompanyId = userDao.getCompanyIdByUserId(pagination.getUserId());
			pagination.setPartnerCompanyId(partnerCompanyId);
			if (partnerCompanyId != null && partnerCompanyId > 0) {
				utilService.setDateFilters(pagination);
				response.setData(teamMemberAnalyticsDao.getTeamMemberTrackAssetDetailsByType(pagination));
				response.setStatusCode(200);
			}
		}
		return response;
	}

	@Override
	public XtremandResponse getVendorTeamMemberTrackAssetDetailsByType(Pagination pagination) {
		XtremandResponse response = new XtremandResponse();
		response.setStatusCode(400);
		if (pagination.getUserId() != null && pagination.getUserId() > 0) {
			Integer vendorCompanyId = userDao.getCompanyIdByUserId(pagination.getUserId());
			pagination.setVendorCompanyId(vendorCompanyId);
			if (vendorCompanyId != null && vendorCompanyId > 0) {
				utilService.setDateFilters(pagination);
				response.setData(
						teamMemberAnalyticsDao.getTeamMemberTrackAssetDetailsByTypeForVendorTeamMember(pagination));
				response.setStatusCode(200);
			}
		}
		return response;
	}

	@Override
	public XtremandResponse getTeamMemberTracksCount(Pagination pagination) {
		XtremandResponse response = new XtremandResponse();
		response.setStatusCode(400);
		if (pagination.getUserId() != null && pagination.getUserId() > 0) {
			Integer partnerCompanyId = userDao.getCompanyIdByUserId(pagination.getUserId());
			pagination.setPartnerCompanyId(partnerCompanyId);
			if (partnerCompanyId != null && partnerCompanyId > 0) {
				utilService.setDateFilters(pagination);
				response.setData(teamMemberAnalyticsDao.getTeamMemberTracksCount(pagination));
				response.setStatusCode(200);
			}
		}
		return response;
	}

	@Override
	public XtremandResponse getVendorTeamMemberTracksCount(Pagination pagination) {
		XtremandResponse response = new XtremandResponse();
		response.setStatusCode(400);
		if (pagination.getUserId() != null && pagination.getUserId() > 0) {
			Integer vendorCompanyId = userDao.getCompanyIdByUserId(pagination.getUserId());
			pagination.setVendorCompanyId(vendorCompanyId);
			if (vendorCompanyId != null && vendorCompanyId > 0) {
				utilService.setDateFilters(pagination);
				response.setData(teamMemberAnalyticsDao.getTeamMemberTracksCountForVendorTeamMember(pagination));
				response.setStatusCode(200);
			}
		}
		return response;
	}

	@Override
	public XtremandResponse getTeamMemberWisePlaybooksCount(Pagination pagination) {
		XtremandResponse response = new XtremandResponse();
		response.setStatusCode(400);
		if (pagination.getUserId() != null && pagination.getUserId() > 0) {
			Integer partnerCompanyId = userDao.getCompanyIdByUserId(pagination.getUserId());
			pagination.setPartnerCompanyId(partnerCompanyId);
			if (partnerCompanyId != null && partnerCompanyId > 0) {
				utilService.setDateFilters(pagination);
				response.setData(teamMemberAnalyticsDao.getTeamMemberWisePlaybooksCount(pagination));
				response.setStatusCode(200);
			}
		}
		return response;
	}

	@Override
	public XtremandResponse getVendorTeamMemberWisePlaybooksCount(Pagination pagination) {
		XtremandResponse response = new XtremandResponse();
		response.setStatusCode(400);
		if (pagination.getUserId() != null && pagination.getUserId() > 0) {
			Integer vendorCompanyId = userDao.getCompanyIdByUserId(pagination.getUserId());
			pagination.setVendorCompanyId(vendorCompanyId);
			if (vendorCompanyId != null && vendorCompanyId > 0) {
				utilService.setDateFilters(pagination);
				response.setData(teamMemberAnalyticsDao.getTeamMemberWisePlaybooksCountForVendorTeamMember(pagination));
				response.setStatusCode(200);
			}
		}
		return response;
	}

	@Override
	public XtremandResponse getTeamMemberTrackDetails(Pagination pagination) {
		XtremandResponse response = new XtremandResponse();
		response.setStatusCode(400);
		if (pagination.getUserId() != null && pagination.getUserId() > 0) {
			Integer partnerCompanyId = userDao.getCompanyIdByUserId(pagination.getUserId());
			pagination.setPartnerCompanyId(partnerCompanyId);
			if (partnerCompanyId != null && partnerCompanyId > 0) {
				utilService.setDateFilters(pagination);
				response.setData(teamMemberAnalyticsDao.getTeamMemberTrackDetails(pagination));
				response.setStatusCode(200);
			}
		}
		return response;
	}

	@Override
	public XtremandResponse getVendorTeamMemberTrackDetails(Pagination pagination) {
		XtremandResponse response = new XtremandResponse();
		response.setStatusCode(400);
		if (pagination.getUserId() != null && pagination.getUserId() > 0) {
			Integer vendorCompanyId = userDao.getCompanyIdByUserId(pagination.getUserId());
			pagination.setVendorCompanyId(vendorCompanyId);
			if (vendorCompanyId != null && vendorCompanyId > 0) {
				utilService.setDateFilters(pagination);
				response.setData(teamMemberAnalyticsDao.getVendorTeamMemberTrackDetails(pagination));
				response.setStatusCode(200);
			}
		}
		return response;
	}

	@Override
	public XtremandResponse getTeamMemberTrackAssetDetails(Pagination pagination) {
		XtremandResponse response = new XtremandResponse();
		response.setStatusCode(400);
		if (pagination.getUserId() != null && pagination.getUserId() > 0) {
			Integer partnerCompanyId = userDao.getCompanyIdByUserId(pagination.getUserId());
			pagination.setPartnerCompanyId(partnerCompanyId);
			if (partnerCompanyId != null && partnerCompanyId > 0) {
				utilService.setDateFilters(pagination);
				response.setData(teamMemberAnalyticsDao.getTeamMemberTrackAssetDetails(pagination));
				response.setStatusCode(200);
			}
		}
		return response;
	}

	@Override
	public XtremandResponse getVendorTeamMemberTrackAssetDetails(Pagination pagination) {
		XtremandResponse response = new XtremandResponse();
		response.setStatusCode(400);
		if (pagination.getUserId() != null && pagination.getUserId() > 0) {
			Integer vendorCompanyId = userDao.getCompanyIdByUserId(pagination.getUserId());
			pagination.setVendorCompanyId(vendorCompanyId);
			if (vendorCompanyId != null && vendorCompanyId > 0) {
				utilService.setDateFilters(pagination);
				response.setData(teamMemberAnalyticsDao.getTeamMemberTrackAssetDetailsForVendorTeamMember(pagination));
				response.setStatusCode(200);
			}
		}
		return response;
	}

	@Override
	public XtremandResponse getTeamMemberPlaybookAssetDetails(Pagination pagination) {
		XtremandResponse response = new XtremandResponse();
		response.setStatusCode(400);
		if (pagination.getUserId() != null && pagination.getUserId() > 0) {
			Integer partnerCompanyId = userDao.getCompanyIdByUserId(pagination.getUserId());
			pagination.setPartnerCompanyId(partnerCompanyId);
			if (partnerCompanyId != null && partnerCompanyId > 0) {
				utilService.setDateFilters(pagination);
				response.setData(teamMemberAnalyticsDao.getTeamMemberPlaybookAssetDetails(pagination));
				response.setStatusCode(200);
			}
		}
		return response;
	}

	@Override
	public XtremandResponse getVendorTeamMemberPlaybookAssetDetails(Pagination pagination) {
		XtremandResponse response = new XtremandResponse();
		response.setStatusCode(400);
		if (pagination.getUserId() != null && pagination.getUserId() > 0) {
			Integer vendorCompanyId = userDao.getCompanyIdByUserId(pagination.getUserId());
			pagination.setVendorCompanyId(vendorCompanyId);
			if (vendorCompanyId != null && vendorCompanyId > 0) {
				utilService.setDateFilters(pagination);
				response.setData(
						teamMemberAnalyticsDao.getTeamMemberPlaybookAssetDetailsForVendorTeamMember(pagination));
				response.setStatusCode(200);
			}
		}
		return response;
	}

	@Override
	public XtremandResponse getTeamMemberShareLeadDetails(Pagination pagination) {
		XtremandResponse response = new XtremandResponse();
		response.setStatusCode(400);
		if (pagination.getUserId() != null && pagination.getUserId() > 0) {
			Integer partnerCompanyId = userDao.getCompanyIdByUserId(pagination.getUserId());
			pagination.setPartnerCompanyId(partnerCompanyId);
			if (partnerCompanyId != null && partnerCompanyId > 0) {
				utilService.setDateFilters(pagination);
				response.setData(teamMemberAnalyticsDao.getTeamMemberShareLeadDetails(pagination));
				response.setStatusCode(200);
			}
		}
		return response;
	}

	@Override
	public XtremandResponse getVendorTeamMemberShareLeadDetails(Pagination pagination) {
		XtremandResponse response = new XtremandResponse();
		response.setStatusCode(400);
		if (pagination.getUserId() != null && pagination.getUserId() > 0) {
			Integer vendorCompanyId = userDao.getCompanyIdByUserId(pagination.getUserId());
			pagination.setVendorCompanyId(vendorCompanyId);
			if (vendorCompanyId != null && vendorCompanyId > 0) {
				utilService.setDateFilters(pagination);
				response.setData(teamMemberAnalyticsDao.getTeamMemberShareLeadDetailsForVendorTeamMember(pagination));
				response.setStatusCode(200);
			}
		}
		return response;
	}

	@Override
	public XtremandResponse getTeamMemberRedistributedCampaignDetails(Pagination pagination) {
		XtremandResponse response = new XtremandResponse();
		response.setStatusCode(400);
		if (pagination.getUserId() != null && pagination.getUserId() > 0) {
			Integer partnerCompanyId = userDao.getCompanyIdByUserId(pagination.getUserId());
			pagination.setPartnerCompanyId(partnerCompanyId);
			if (partnerCompanyId != null && partnerCompanyId > 0) {
				utilService.setDateFilters(pagination);
				response.setData(teamMemberAnalyticsDao.getTeamMemberRedistributedCampaignDetails(pagination));
				response.setStatusCode(200);
			}
		}
		return response;
	}

	@Override
	public XtremandResponse getVendorTeamMemberLaunchedCampaignDetails(Pagination pagination) {
		XtremandResponse response = new XtremandResponse();
		response.setStatusCode(400);
		if (pagination.getUserId() != null && pagination.getUserId() > 0) {
			Integer vendorCompanyId = userDao.getCompanyIdByUserId(pagination.getUserId());
			pagination.setVendorCompanyId(vendorCompanyId);
			if (vendorCompanyId != null && vendorCompanyId > 0) {
				utilService.setDateFilters(pagination);
				response.setData(teamMemberAnalyticsDao.getVendorTeamMemberLaunchedCampaignDetails(pagination));
				response.setStatusCode(200);
			}
		}
		return response;
	}

	@Override
	public XtremandResponse getTeamMemberLeadDetails(Pagination pagination) {
		XtremandResponse response = new XtremandResponse();
		response.setStatusCode(400);
		if (pagination.getUserId() != null && pagination.getUserId() > 0) {
			Integer partnerCompanyId = userDao.getCompanyIdByUserId(pagination.getUserId());
			pagination.setPartnerCompanyId(partnerCompanyId);
			if (partnerCompanyId != null && partnerCompanyId > 0) {
				utilService.setDateFilters(pagination);
				response.setData(teamMemberAnalyticsDao.getTeamMemberLeadDetails(pagination));
				response.setStatusCode(200);
			}
		}
		return response;
	}

	@Override
	public XtremandResponse getTeamMemberDealDetails(Pagination pagination) {
		XtremandResponse response = new XtremandResponse();
		response.setStatusCode(400);
		if (pagination.getUserId() != null && pagination.getUserId() > 0) {
			Integer partnerCompanyId = userDao.getCompanyIdByUserId(pagination.getUserId());
			pagination.setPartnerCompanyId(partnerCompanyId);
			if (partnerCompanyId != null && partnerCompanyId > 0) {
				utilService.setDateFilters(pagination);
				response.setData(teamMemberAnalyticsDao.getTeamMemberDealDetails(pagination));
				response.setStatusCode(200);
			}
		}
		return response;
	}

	@Override
	public XtremandResponse getVendorTeamMemberLeadDetails(Pagination pagination) {
		XtremandResponse response = new XtremandResponse();
		response.setStatusCode(400);
		if (pagination.getUserId() != null && pagination.getUserId() > 0) {
			Integer vendorCompanyId = userDao.getCompanyIdByUserId(pagination.getUserId());
			pagination.setVendorCompanyId(vendorCompanyId);
			if (vendorCompanyId != null && vendorCompanyId > 0) {
				utilService.setDateFilters(pagination);
				response.setData(teamMemberAnalyticsDao.getVendorTeamMemberLeadDetails(pagination));
				response.setStatusCode(200);
			}
		}
		return response;
	}

	@Override
	public XtremandResponse getVendorTeamMemberDealDetails(Pagination pagination) {
		XtremandResponse response = new XtremandResponse();
		response.setStatusCode(400);
		if (pagination.getUserId() != null && pagination.getUserId() > 0) {
			Integer vendorCompanyId = userDao.getCompanyIdByUserId(pagination.getUserId());
			pagination.setVendorCompanyId(vendorCompanyId);
			if (vendorCompanyId != null && vendorCompanyId > 0) {
				utilService.setDateFilters(pagination);
				response.setData(teamMemberAnalyticsDao.getVendorTeamMemberDealDetails(pagination));
				response.setStatusCode(200);
			}
		}
		return response;
	}

	@Override
	public XtremandResponse getVendorInfoForFilter(Pagination pagination) {
		XtremandResponse response = new XtremandResponse();
		response.setStatusCode(400);
		if (pagination.getUserId() != null && pagination.getUserId() > 0) {
			Integer partnerCompanyId = userDao.getCompanyIdByUserId(pagination.getUserId());
			pagination.setPartnerCompanyId(partnerCompanyId);
			if (partnerCompanyId != null && partnerCompanyId > 0) {
				response.setData(teamMemberAnalyticsDao.getVendorInfoForFilter(pagination));
				response.setStatusCode(200);
			}
		}
		return response;
	}

	@Override
	public XtremandResponse getTeamMemberInfoForFilter(Pagination pagination) {
		XtremandResponse response = new XtremandResponse();
		response.setStatusCode(400);
		if (pagination.getUserId() != null && pagination.getUserId() > 0) {
			Integer partnerCompanyId = userDao.getCompanyIdByUserId(pagination.getUserId());
			pagination.setPartnerCompanyId(partnerCompanyId);
			if (partnerCompanyId != null && partnerCompanyId > 0) {
				response.setData(teamMemberAnalyticsDao.getTeamMemberInfoForFilter(pagination));
				response.setStatusCode(200);
			}
		}
		return response;
	}

	@Override
	public XtremandResponse getTeamMemberMdfDetails(Pagination pagination) {
		XtremandResponse response = new XtremandResponse();
		response.setStatusCode(400);
		if (pagination.getUserId() != null && pagination.getUserId() > 0) {
			Integer partnerCompanyId = userDao.getCompanyIdByUserId(pagination.getUserId());
			pagination.setPartnerCompanyId(partnerCompanyId);
			if (partnerCompanyId != null && partnerCompanyId > 0) {
				utilService.setDateFilters(pagination);
				response.setData(teamMemberAnalyticsDao.getTeamMemberMdfDetails(pagination));
				response.setStatusCode(200);
			}
		}
		return response;
	}

	@Override
	public XtremandResponse getVendorTeamMemberMdfDetails(Pagination pagination) {
		XtremandResponse response = new XtremandResponse();
		response.setStatusCode(400);
		if (pagination.getUserId() != null && pagination.getUserId() > 0) {
			Integer vendorCompanyId = userDao.getCompanyIdByUserId(pagination.getUserId());
			pagination.setVendorCompanyId(vendorCompanyId);
			if (vendorCompanyId != null && vendorCompanyId > 0) {
				utilService.setDateFilters(pagination);
				response.setData(teamMemberAnalyticsDao.getVendorTeamMemberMdfDetails(pagination));
				response.setStatusCode(200);
			}
		}
		return response;
	}

	@Override
	public XtremandResponse getTeamMemberJourneyCountsForVendor(
			TeamMemberAnalyticsRequestDTO teamMemberJourneyRequestDTO) {
		XtremandResponse response = new XtremandResponse();
		response.setStatusCode(400);
		if (teamMemberJourneyRequestDTO.getLoggedInUserId() != null
				&& teamMemberJourneyRequestDTO.getLoggedInUserId() > 0) {
			Integer vendorCompanyId = userDao.getCompanyIdByUserId(teamMemberJourneyRequestDTO.getLoggedInUserId());
			teamMemberJourneyRequestDTO.setVendorCompanyId(vendorCompanyId);
			if (vendorCompanyId != null && vendorCompanyId > 0) {
				utilService.setDateFiltersForTeamMemberAnalytics(teamMemberJourneyRequestDTO);
				TeamMemberAnalyticsDTO teamMemberAnalytics = new TeamMemberAnalyticsDTO();

				String allPartnersCount = teamMemberAnalyticsDao
						.getAllPartnersCountForVendorTeamMember(teamMemberJourneyRequestDTO);
				teamMemberAnalytics.setShowAllPartnersCount(true);
				teamMemberAnalytics.setAllPartnersCount(allPartnersCount);

				if (utilDAO.hasShareLeadsAccessByCompanyId(vendorCompanyId)) {
					String shareLeadCount = teamMemberAnalyticsDao
							.getShareLeadCountForVendorTeamMember(teamMemberJourneyRequestDTO);
					teamMemberAnalytics.setShareLeadCount(shareLeadCount);
					teamMemberAnalytics.setShowShareLeadCount(true);
				}

				if (utilDAO.hasEnableLeadsAccessByCompanyId(vendorCompanyId)) {
					String leadCount = teamMemberAnalyticsDao
							.getTeamMembersLeadCountForVendorTeamMember(teamMemberJourneyRequestDTO);
					String dealCount = teamMemberAnalyticsDao
							.getTeamMembersDealCountForVendorTeamMember(teamMemberJourneyRequestDTO);
					teamMemberAnalytics.setLeadCount(leadCount);
					teamMemberAnalytics.setDealCount(dealCount);
					teamMemberAnalytics.setShowLeadCount(true);
					teamMemberAnalytics.setShowDealCount(true);
				}

				if (utilDAO.hasDamAccessByCompanyId(vendorCompanyId)) {
					String assetCount = teamMemberAnalyticsDao
							.getTeamMembersAssetCountForVendorTeamMember(teamMemberJourneyRequestDTO);
					teamMemberAnalytics.setAssetCount(assetCount);
					teamMemberAnalytics.setShowAssetCount(true);
				}

				if (utilDAO.hasLmsAccessByCompanyId(vendorCompanyId)
						|| utilDAO.hasPlaybookAccessByCompanyId(vendorCompanyId)) {
					TeamMemberAnalyticsDTO trackAndPlaybookCountDto = teamMemberAnalyticsDao
							.getTeamMembersTrackAndPlaybookCountForVendorTeamMember(teamMemberJourneyRequestDTO);
					if (trackAndPlaybookCountDto != null) {
						if (utilDAO.hasLmsAccessByCompanyId(vendorCompanyId)) {
							teamMemberAnalytics.setTrackCount(trackAndPlaybookCountDto.getTrackCount());
							teamMemberAnalytics.setShowTrackCount(true);
						}
						if (utilDAO.hasPlaybookAccessByCompanyId(vendorCompanyId)) {
							teamMemberAnalytics.setPlaybookCount(trackAndPlaybookCountDto.getPlaybookCount());
							teamMemberAnalytics.setShowPlaybookCount(true);
						}
					}
				}

				if (utilDAO.hasLmsAccessByCompanyId(vendorCompanyId)) {
					String trackAssetCount = teamMemberAnalyticsDao
							.getVendorTeamMembersTrackAssetCount(teamMemberJourneyRequestDTO);
					teamMemberAnalytics.setTrackAssetCount(trackAssetCount);
					teamMemberAnalytics.setShowTrackAssetCount(true);
				}

				response.setData(teamMemberAnalytics);
				response.setStatusCode(200);
			}
		}
		return response;
	}

	@Override
	public XtremandResponse getVendorTeamMemberContactsDetails(Pagination pagination) {
		XtremandResponse response = new XtremandResponse();
		response.setStatusCode(400);
		if (pagination.getUserId() != null && pagination.getUserId() > 0) {
			Integer vendorCompanyId = userDao.getCompanyIdByUserId(pagination.getUserId());
			pagination.setVendorCompanyId(vendorCompanyId);
			if (vendorCompanyId != null && vendorCompanyId > 0) {
				utilService.setDateFilters(pagination);
				response.setData(teamMemberAnalyticsDao.getVendorTeamMemberContactDetails(pagination));
				response.setStatusCode(200);
			}
		}
		return response;
	}

	@Override
	public XtremandResponse getVendorTeamMemberallPartnersDetails(Pagination pagination) {
		XtremandResponse response = new XtremandResponse();
		response.setStatusCode(400);
		if (pagination.getUserId() != null && pagination.getUserId() > 0) {
			Integer vendorCompanyId = userDao.getCompanyIdByUserId(pagination.getUserId());
			pagination.setVendorCompanyId(vendorCompanyId);
			if (vendorCompanyId != null && vendorCompanyId > 0) {
				utilService.setDateFilters(pagination);
				response.setData(teamMemberAnalyticsDao.getallPartnersDetailsForVendor(pagination));
				response.setStatusCode(200);
			}
		}
		return response;
	}

	private void setDataNotFound(XtremandResponse response) {
		response.setStatusCode(404);
		response.setMessage("No Data Found.");
	}

	@Override
	public XtremandResponse findLeadsAndDealsCountForTeamMember(
			TeamMemberAnalyticsRequestDTO teamMemberJourneyRequestDTO, String filterType) {
		XtremandResponse response = new XtremandResponse();
		utilService.setDateFiltersForTeamMemberAnalytics(teamMemberJourneyRequestDTO);
		List<Object[]> list = teamMemberAnalyticsDao.findLeadsAndDealsCountForTeamMember(teamMemberJourneyRequestDTO,
				filterType);
		if (list != null && !list.isEmpty()) {
			setDTOData(response, list);
		} else {
			setDataNotFound(response);
		}
		return response;
	}

	private void setDTOData(XtremandResponse response, List<Object[]> list) {
		BarChartDualAxesDTO barChartDualAxesDTO = new BarChartDualAxesDTO();
		List<String> teamMemberemailIds = new ArrayList<>();
		List<Integer> leadsCount = new ArrayList<>();
		List<Integer> dealsCount = new ArrayList<>();
		for (Object[] object : list) {
			teamMemberemailIds.add((String) object[0]);
			BigDecimal leadCountInBigInt = (BigDecimal) object[1];
			leadsCount.add(leadCountInBigInt != null ? leadCountInBigInt.intValue() : 0);
			BigDecimal dealCountInBigInt = (BigDecimal) object[2];
			dealsCount.add(dealCountInBigInt != null ? dealCountInBigInt.intValue() : 0);
		}
		barChartDualAxesDTO.setXaxis(teamMemberemailIds);
		barChartDualAxesDTO.setYaxis1(dealsCount);
		barChartDualAxesDTO.setYaxis2(leadsCount);
		response.setStatusCode(200);
		response.setData(barChartDualAxesDTO);
	}

	@Override
	public XtremandResponse findAllLeadsAndDealsCountForTeamMember(
			TeamMemberAnalyticsRequestDTO teamMemberJourneyRequestDTO, String filterType) {
		XtremandResponse response = new XtremandResponse();
		utilService.setDateFiltersForTeamMemberAnalytics(teamMemberJourneyRequestDTO);
		List<Object[]> list = teamMemberAnalyticsDao.findAllLeadsAndDealsCountForTeamMember(teamMemberJourneyRequestDTO,
				filterType);
		if (list != null && !list.isEmpty()) {
			setDTOData(response, list);
		} else {
			setDataNotFound(response);
		}
		return response;
	}

	@Override
	public XtremandResponse getContactsDetailsForTeamMember(Pagination pagination) {
		XtremandResponse response = new XtremandResponse();
		response.setStatusCode(400);
		if (pagination.getUserId() != null && pagination.getUserId() > 0) {
			Integer partnerCompanyId = userDao.getCompanyIdByUserId(pagination.getUserId());
			pagination.setPartnerCompanyId(partnerCompanyId);
			if (partnerCompanyId != null && partnerCompanyId > 0) {
				utilService.setDateFilters(pagination);
				response.setData(teamMemberAnalyticsDao.getContactsDetailsForTeamMember(pagination));
				response.setStatusCode(200);
			}
		}
		return response;
	}

	@Override
	public XtremandResponse getVendorTeamMemberAssetsCount(Pagination pagination) {
		XtremandResponse response = new XtremandResponse();
		response.setStatusCode(400);
		if (pagination.getUserId() != null && pagination.getUserId() > 0) {
			Integer vendorCompanyId = userDao.getCompanyIdByUserId(pagination.getUserId());
			pagination.setVendorCompanyId(vendorCompanyId);
			if (vendorCompanyId != null && vendorCompanyId > 0) {
				utilService.setDateFilters(pagination);
				response.setData(teamMemberAnalyticsDao.getVendorTeamMemberAssetsCount(pagination));
				response.setStatusCode(200);
			}
		}
		return response;
	}

	@Override
	public XtremandResponse getVendorTeamMemberAssetsDetails(Pagination pagination) {
		XtremandResponse response = new XtremandResponse();
		response.setStatusCode(400);
		if (pagination.getUserId() != null && pagination.getUserId() > 0) {
			Integer vendorCompanyId = userDao.getCompanyIdByUserId(pagination.getUserId());
			pagination.setVendorCompanyId(vendorCompanyId);
			if (vendorCompanyId != null && vendorCompanyId > 0) {
				utilService.setDateFilters(pagination);
				response.setData(teamMemberAnalyticsDao.getVendorTeamMemberAssetsDetails(pagination));
				response.setStatusCode(200);
			}
		}
		return response;
	}

	@Override
	public XtremandResponse getCompanyDetailsForTeamMember(Pagination pagination) {
		XtremandResponse response = new XtremandResponse();
		response.setStatusCode(400);
		if (pagination.getUserId() != null && pagination.getUserId() > 0) {
			Integer partnerCompanyId = userDao.getCompanyIdByUserId(pagination.getUserId());
			pagination.setPartnerCompanyId(partnerCompanyId);
			if (partnerCompanyId != null && partnerCompanyId > 0) {
				utilService.setDateFilters(pagination);
				response.setData(teamMemberAnalyticsDao.getCompanyDetailsForTeamMember(pagination));
				response.setStatusCode(200);
			}
		}
		return response;
	}

	@SuppressWarnings("unchecked")
	@Override
	public HttpServletResponse downloadTrackInteractionAndNonInteractionReport(
			TeamMemberAnalyticsRequestDTO teamMemberJourneyRequestDTO, HttpServletResponse response) {
		String fileName = "Interacted-And-Not-Interacted-Tracks-Report";
		Pagination pagination = new Pagination();
		pagination.setExcludeLimit(true);
		framePaginationValues(teamMemberJourneyRequestDTO, pagination);
		if (pagination.getUserId() != null && pagination.getUserId() > 0) {
			utilService.setDateFilters(pagination);
			Map<String, Object> result = null;
			if (teamMemberJourneyRequestDTO.isVendorVersion()) {
				Integer vendorCompanyId = userDao.getCompanyIdByUserId(pagination.getUserId());
				pagination.setVendorCompanyId(vendorCompanyId);
				if (XamplifyUtils.isValidInteger(vendorCompanyId)) {
					result = teamMemberAnalyticsDao.getTeamMemberTrackDetailsByInteractionForVendor(pagination);
				}
			} else {
				Integer partnerCompanyId = userDao.getCompanyIdByUserId(pagination.getUserId());
				pagination.setPartnerCompanyId(partnerCompanyId);
				if (XamplifyUtils.isValidInteger(partnerCompanyId)) {
					result = teamMemberAnalyticsDao.getTeamMemberTrackDetailsByInteraction(pagination);
				}
			}
			if (result != null) {
				List<PartnerJourneyTrackDetailsDTO> trackData = (List<PartnerJourneyTrackDetailsDTO>) result
						.get("list");
				return frameCSVDataForTrackInteractionReport(response, fileName, teamMemberJourneyRequestDTO,
						trackData);
			}
		}
		return response;
	}

	private HttpServletResponse frameCSVDataForTrackInteractionReport(HttpServletResponse response, String fileName,
			TeamMemberAnalyticsRequestDTO teamMemberJourneyRequestDTO, List<PartnerJourneyTrackDetailsDTO> trackData) {
		try {
			List<String[]> row = new ArrayList<>();
			List<String> headerList = new ArrayList<>();
			if (teamMemberJourneyRequestDTO.isVendorVersion()) {
				headerList.add(VENDOR_TEAM_MEMBER_EMAIL_ID);
				headerList.add(PARTNER_TEAM_MEMBER_EMAIL_ID);
			} else {
				headerList.add(VENDOR_COMPANY);
				headerList.add(VENDOR_EMAIL_ID);
				headerList.add(PARTNER_TEAM_MEMBER_EMAIL_ID);
			}
			headerList.add(TRACK_TITLE);
			headerList.add(AVG_PROGRESS);
			row.add(headerList.toArray(new String[0]));
			for (PartnerJourneyTrackDetailsDTO track : trackData) {
				List<String> dataList = new ArrayList<>();
				if (teamMemberJourneyRequestDTO.isVendorVersion()) {
					dataList.add(track.getEmailId());
					dataList.add(track.getPartnerEmailId());
				} else {
					dataList.add(track.getVendorCompany());
					dataList.add(track.getVendorTeamMemberEmailId());
					dataList.add(track.getEmailId());
				}
				dataList.add(track.getTitle());
				dataList.add(track.getProgress().toString() + "%");
				row.add(dataList.toArray(new String[0]));
			}
			return XamplifyUtils.generateCSV(fileName, response, row);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return response;
	}

	private void framePaginationValues(TeamMemberAnalyticsRequestDTO teamMemberJourneyRequestDTO,
			Pagination pagination) {
		pagination.setUserId(teamMemberJourneyRequestDTO.getLoggedInUserId());
		pagination.setTrackTypeFilter(teamMemberJourneyRequestDTO.getTrackTypeFilter());
		pagination.setSelectedTeamMemberIds(teamMemberJourneyRequestDTO.getSelectedTeamMemberIds());
		pagination.setSelectedVendorCompanyIds(teamMemberJourneyRequestDTO.getSelectedVendorCompanyIds());
		pagination.setSearchKey(teamMemberJourneyRequestDTO.getSearchKey());
		pagination.setAssetTypeFilter(teamMemberJourneyRequestDTO.getAssetType());
		pagination.setCampaignTypeFilter(teamMemberJourneyRequestDTO.getCampaignTypeFilter());
		pagination.setPartnerCompanyId(teamMemberJourneyRequestDTO.getPartnerCompanyId());
		pagination.setTeamMemberId(teamMemberJourneyRequestDTO.getTeamMemberUserId());
		pagination.setFromDateFilterString(teamMemberJourneyRequestDTO.getFromDateFilterInString());
		pagination.setToDateFilterString(teamMemberJourneyRequestDTO.getToDateFilterInString());
		pagination.setTimeZone(teamMemberJourneyRequestDTO.getTimeZone());
		pagination.setVanityUrlFilter(teamMemberJourneyRequestDTO.isVanityUrlFilter());
		pagination.setVendorCompanyProfileName(teamMemberJourneyRequestDTO.getVendorCompanyProfileName());
	}

	@SuppressWarnings("unchecked")
	@Override
	public HttpServletResponse downloadTrackAsserDetailsReport(
			TeamMemberAnalyticsRequestDTO teamMemberJourneyRequestDTO, HttpServletResponse response) {
		String fileName = "Track-Asset-Details-Report";
		Pagination pagination = new Pagination();
		pagination.setExcludeLimit(true);
		framePaginationValues(teamMemberJourneyRequestDTO, pagination);
		if (pagination.getUserId() != null && pagination.getUserId() > 0) {
			utilService.setDateFilters(pagination);
			Map<String, Object> result = null;
			if (teamMemberJourneyRequestDTO.isVendorVersion()) {
				Integer vendorCompanyId = userDao.getCompanyIdByUserId(pagination.getUserId());
				pagination.setVendorCompanyId(vendorCompanyId);
				if (XamplifyUtils.isValidInteger(vendorCompanyId)) {
					result = teamMemberAnalyticsDao.getTeamMemberTrackAssetDetailsByTypeForVendorTeamMember(pagination);
				}
			} else {
				Integer partnerCompanyId = userDao.getCompanyIdByUserId(pagination.getUserId());
				pagination.setPartnerCompanyId(partnerCompanyId);
				if (XamplifyUtils.isValidInteger(partnerCompanyId)) {
					result = teamMemberAnalyticsDao.getTeamMemberTrackAssetDetailsByType(pagination);
				}
			}
			if (result != null) {
				List<PartnerJourneyTrackDetailsDTO> trackData = (List<PartnerJourneyTrackDetailsDTO>) result
						.get("list");
				return frameCSVDataForTypeWiseTrackContentReport(response, fileName, teamMemberJourneyRequestDTO,
						trackData);
			}
		}
		return response;
	}

	private HttpServletResponse frameCSVDataForTypeWiseTrackContentReport(HttpServletResponse response, String fileName,
			TeamMemberAnalyticsRequestDTO teamMemberJourneyRequestDTO, List<PartnerJourneyTrackDetailsDTO> trackData) {
		try {
			List<String[]> row = new ArrayList<>();
			List<String> headerList = new ArrayList<>();
			if (teamMemberJourneyRequestDTO.isVendorVersion()) {
				headerList.add(VENDOR_TEAM_MEMBER_NAME);
				headerList.add(VENDOR_TEAM_MEMBER_EMAIL_ID);
				headerList.add(PARTNER_TEAM_MEMBER_EMAIL_ID);
			} else {
				headerList.add(VENDOR_COMPANY);
				headerList.add(VENDOR_EMAIL_ID);
				headerList.add(PARTNER_TEAM_MEMBER_EMAIL_ID);
			}
			headerList.add(ASSET_TITLE);
			headerList.add(ASSET_TYPE);
			headerList.add(TRACK_TITLE);
			headerList.add("STATUS");
			row.add(headerList.toArray(new String[0]));
			for (PartnerJourneyTrackDetailsDTO track : trackData) {
				List<String> dataList = new ArrayList<>();
				if (teamMemberJourneyRequestDTO.isVendorVersion()) {
					dataList.add(track.getFullName());
					dataList.add(track.getEmailId());
					dataList.add(track.getPartnerEmailId());
				} else {
					dataList.add(track.getVendorCompany());
					dataList.add(track.getVendorTeamMemberEmailId());
					dataList.add(track.getEmailId());
				}
				dataList.add(track.getAssetName());
				dataList.add(track.getAssetType());
				dataList.add(track.getTitle());
				dataList.add(track.getStatus());
				row.add(dataList.toArray(new String[0]));
			}
			return XamplifyUtils.generateCSV(fileName, response, row);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return response;
	}

	@SuppressWarnings("unchecked")
	@Override
	public HttpServletResponse downloadUserWiseTrackCountReport(
			TeamMemberAnalyticsRequestDTO teamMemberJourneyRequestDTO, HttpServletResponse response) {
		String fileName = "Track-Counts-Report";
		String lmsType = "TRACK";
		Pagination pagination = new Pagination();
		pagination.setExcludeLimit(true);
		framePaginationValues(teamMemberJourneyRequestDTO, pagination);
		if (pagination.getUserId() != null && pagination.getUserId() > 0) {
			utilService.setDateFilters(pagination);
			Map<String, Object> result = null;
			if (teamMemberJourneyRequestDTO.isVendorVersion()) {
				Integer vendorCompanyId = userDao.getCompanyIdByUserId(pagination.getUserId());
				pagination.setVendorCompanyId(vendorCompanyId);
				if (XamplifyUtils.isValidInteger(vendorCompanyId)) {
					result = teamMemberAnalyticsDao.getTeamMemberTracksCountForVendorTeamMember(pagination);
				}
			} else {
				Integer partnerCompanyId = userDao.getCompanyIdByUserId(pagination.getUserId());
				pagination.setPartnerCompanyId(partnerCompanyId);
				if (XamplifyUtils.isValidInteger(partnerCompanyId)) {
					result = teamMemberAnalyticsDao.getTeamMemberTracksCount(pagination);
				}
			}
			if (result != null) {
				List<TeamMemberDTO> trackData = (List<TeamMemberDTO>) result.get("list");
				return frameCSVDataForTrackandPlaybookCountsReport(response, fileName, teamMemberJourneyRequestDTO,
						trackData, lmsType);
			}
		}
		return response;
	}

	private HttpServletResponse frameCSVDataForTrackandPlaybookCountsReport(HttpServletResponse response,
			String fileName, TeamMemberAnalyticsRequestDTO teamMemberJourneyRequestDTO, List<TeamMemberDTO> trackData,
			String lmsType) {
		try {
			List<String[]> row = new ArrayList<>();
			List<String> headerList = new ArrayList<>();
			if (teamMemberJourneyRequestDTO.isVendorVersion()) {
				headerList.add(TEAM_MEMBER_NAME);
				headerList.add(TEAM_MEMBER_EMAIL_ID);
			} else {
				headerList.add(VENDOR_COMPANY);
				headerList.add(VENDOR_EMAIL_ID);
				headerList.add(PARTNER_TEAM_MEMBER_EMAIL_ID);
			}
			headerList.add(lmsType + " COUNT");
			row.add(headerList.toArray(new String[0]));
			for (TeamMemberDTO track : trackData) {
				List<String> dataList = new ArrayList<>();
				if (teamMemberJourneyRequestDTO.isVendorVersion()) {
					dataList.add(track.getFullName());
					dataList.add(track.getEmailId());
				} else {
					dataList.add(track.getVendorCompanyProfileName());
					dataList.add(track.getVendorTeamMemberEmailId());
					dataList.add(track.getEmailId());
				}
				dataList.add(track.getTrackCount());
				row.add(dataList.toArray(new String[0]));
			}
			return XamplifyUtils.generateCSV(fileName, response, row);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return response;
	}

	@SuppressWarnings("unchecked")
	@Override
	public HttpServletResponse downloadUserWisePlayBookCountReport(
			TeamMemberAnalyticsRequestDTO teamMemberJourneyRequestDTO, HttpServletResponse response) {
		String fileName = "Playbook-Counts-Report";
		String lmsType = "PLAYBOOK";
		Pagination pagination = new Pagination();
		pagination.setExcludeLimit(true);
		framePaginationValues(teamMemberJourneyRequestDTO, pagination);
		if (pagination.getUserId() != null && pagination.getUserId() > 0) {
			utilService.setDateFilters(pagination);
			Map<String, Object> result = null;
			if (teamMemberJourneyRequestDTO.isVendorVersion()) {
				Integer vendorCompanyId = userDao.getCompanyIdByUserId(pagination.getUserId());
				pagination.setVendorCompanyId(vendorCompanyId);
				if (XamplifyUtils.isValidInteger(vendorCompanyId)) {
					result = teamMemberAnalyticsDao.getTeamMemberWisePlaybooksCountForVendorTeamMember(pagination);
				}
			} else {
				Integer partnerCompanyId = userDao.getCompanyIdByUserId(pagination.getUserId());
				pagination.setPartnerCompanyId(partnerCompanyId);
				if (XamplifyUtils.isValidInteger(partnerCompanyId)) {
					result = teamMemberAnalyticsDao.getTeamMemberWisePlaybooksCount(pagination);
				}
			}
			if (result != null) {
				List<TeamMemberDTO> trackData = (List<TeamMemberDTO>) result.get("list");
				return frameCSVDataForTrackandPlaybookCountsReport(response, fileName, teamMemberJourneyRequestDTO,
						trackData, lmsType);
			}
		}
		return response;
	}

	@SuppressWarnings("unchecked")
	@Override
	public HttpServletResponse downloadTrackAssetsDetailedReport(
			TeamMemberAnalyticsRequestDTO teamMemberJourneyRequestDTO, HttpServletResponse response) {
		String fileName = "Track-Assets-Detailed-Report";
		Pagination pagination = new Pagination();
		pagination.setExcludeLimit(true);
		framePaginationValues(teamMemberJourneyRequestDTO, pagination);
		if (pagination.getUserId() != null && pagination.getUserId() > 0) {
			utilService.setDateFilters(pagination);
			Map<String, Object> result = null;
			if (teamMemberJourneyRequestDTO.isVendorVersion()) {
				Integer vendorCompanyId = userDao.getCompanyIdByUserId(pagination.getUserId());
				pagination.setVendorCompanyId(vendorCompanyId);
				if (XamplifyUtils.isValidInteger(vendorCompanyId)) {
					result = teamMemberAnalyticsDao.getVendorTeamMemberTrackDetails(pagination);
				}
			} else {
				Integer partnerCompanyId = userDao.getCompanyIdByUserId(pagination.getUserId());
				pagination.setPartnerCompanyId(partnerCompanyId);
				if (XamplifyUtils.isValidInteger(partnerCompanyId)) {
					result = teamMemberAnalyticsDao.getTeamMemberTrackDetails(pagination);
				}
			}
			if (result != null) {
				List<PartnerJourneyTrackDetailsDTO> trackData = (List<PartnerJourneyTrackDetailsDTO>) result
						.get("list");
				return frameCSVDataForTrackDetailedReport(response, fileName, teamMemberJourneyRequestDTO, trackData);
			}
		}
		return response;
	}

	private HttpServletResponse frameCSVDataForTrackDetailedReport(HttpServletResponse response, String fileName,
			TeamMemberAnalyticsRequestDTO teamMemberJourneyRequestDTO, List<PartnerJourneyTrackDetailsDTO> trackData) {
		try {
			List<String[]> row = new ArrayList<>();
			List<String> headerList = new ArrayList<>();
			headerList.add(TEAM_MEMBER_NAME);
			headerList.add(TEAM_MEMBER_EMAIL_ID);
			headerList.add(TRACK_TITLE);
			headerList.add("ASSETS COUNT");
			headerList.add(PUBLISHED_TIME);
			if (!teamMemberJourneyRequestDTO.isVendorVersion()) {
				headerList.add(OPENED_COUNT);
				headerList.add("VIEWED COUNT");
				headerList.add("DOWNLOADED COUNT");
			}
			headerList.add(AVG_PROGRESS);
			headerList.add("QUIZ COUNT");
			if (!teamMemberJourneyRequestDTO.isVendorVersion()) {
				headerList.add("SCORE");
			}
			row.add(headerList.toArray(new String[0]));
			for (PartnerJourneyTrackDetailsDTO track : trackData) {
				List<String> dataList = new ArrayList<>();
				String publishedDate = checkIfDateIsNull(track.getPublishedOn());
				String quizCount = track.getQuizCount().toString();
				dataList.add(track.getFullName());
				dataList.add(track.getEmailId());
				dataList.add(track.getTitle());
				dataList.add(track.getAssetCount().toString());
				dataList.add(publishedDate);
				if (!teamMemberJourneyRequestDTO.isVendorVersion()) {
					dataList.add(track.getOpenedCount().toString());
					dataList.add(track.getViewedCount().toString());
					dataList.add(track.getDownloadedCount().toString());
				}
				dataList.add(track.getProgress().toString() + "%");
				dataList.add(quizCount);
				if (!teamMemberJourneyRequestDTO.isVendorVersion()) {
					String score = "0".equals(quizCount) ? "Quiz Not Available" : track.getScore();
					dataList.add(score);
				}
				row.add(dataList.toArray(new String[0]));
			}
			return XamplifyUtils.generateCSV(fileName, response, row);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return response;
	}

	private String checkIfDateIsNull(Date date) {
		String dateToString = "";
		if (date != null) {
			dateToString = DateUtils.convertDateToString(date);
		}
		return dateToString;
	}

	@SuppressWarnings("unchecked")
	@Override
	public HttpServletResponse downloadTrackAssetsDetailsReport(
			TeamMemberAnalyticsRequestDTO teamMemberJourneyRequestDTO, HttpServletResponse response) {
		String fileName = "Track-Asset-Details-Report";
		String lmsType = "TRACK";
		Pagination pagination = new Pagination();
		pagination.setExcludeLimit(true);
		framePaginationValues(teamMemberJourneyRequestDTO, pagination);
		if (pagination.getUserId() != null && pagination.getUserId() > 0) {
			utilService.setDateFilters(pagination);
			Map<String, Object> result = null;
			if (teamMemberJourneyRequestDTO.isVendorVersion()) {
				Integer vendorCompanyId = userDao.getCompanyIdByUserId(pagination.getUserId());
				pagination.setVendorCompanyId(vendorCompanyId);
				if (XamplifyUtils.isValidInteger(vendorCompanyId)) {
					result = teamMemberAnalyticsDao.getTeamMemberTrackAssetDetailsForVendorTeamMember(pagination);
				}
			} else {
				Integer partnerCompanyId = userDao.getCompanyIdByUserId(pagination.getUserId());
				pagination.setPartnerCompanyId(partnerCompanyId);
				if (XamplifyUtils.isValidInteger(partnerCompanyId)) {
					result = teamMemberAnalyticsDao.getTeamMemberTrackAssetDetails(pagination);
				}
			}
			if (result != null) {
				List<PartnerJourneyTrackDetailsDTO> trackData = (List<PartnerJourneyTrackDetailsDTO>) result
						.get("list");
				return frameCSVDataForTrackandPlayBookAssetDetailsReport(response, fileName,
						teamMemberJourneyRequestDTO, trackData, lmsType);
			}
		}
		return response;
	}

	@SuppressWarnings("unchecked")
	@Override
	public HttpServletResponse downloadPlayBookAssetsDetailsReport(
			TeamMemberAnalyticsRequestDTO teamMemberJourneyRequestDTO, HttpServletResponse response) {
		String fileName = "PlayBook-Asset-Details-Report";
		String lmsType = "PLAYBOOK";
		Pagination pagination = new Pagination();
		pagination.setExcludeLimit(true);
		framePaginationValues(teamMemberJourneyRequestDTO, pagination);
		if (pagination.getUserId() != null && pagination.getUserId() > 0) {
			utilService.setDateFilters(pagination);
			Map<String, Object> result = null;
			if (teamMemberJourneyRequestDTO.isVendorVersion()) {
				Integer vendorCompanyId = userDao.getCompanyIdByUserId(pagination.getUserId());
				pagination.setVendorCompanyId(vendorCompanyId);
				if (XamplifyUtils.isValidInteger(vendorCompanyId)) {
					result = teamMemberAnalyticsDao.getTeamMemberPlaybookAssetDetailsForVendorTeamMember(pagination);
				}
			} else {
				Integer partnerCompanyId = userDao.getCompanyIdByUserId(pagination.getUserId());
				pagination.setPartnerCompanyId(partnerCompanyId);
				if (XamplifyUtils.isValidInteger(partnerCompanyId)) {
					result = teamMemberAnalyticsDao.getTeamMemberPlaybookAssetDetails(pagination);
				}
			}
			if (result != null) {
				List<PartnerJourneyTrackDetailsDTO> trackData = (List<PartnerJourneyTrackDetailsDTO>) result
						.get("list");
				return frameCSVDataForTrackandPlayBookAssetDetailsReport(response, fileName,
						teamMemberJourneyRequestDTO, trackData, lmsType);
			}
		}
		return response;
	}

	private HttpServletResponse frameCSVDataForTrackandPlayBookAssetDetailsReport(HttpServletResponse response,
			String fileName, TeamMemberAnalyticsRequestDTO teamMemberJourneyRequestDTO,
			List<PartnerJourneyTrackDetailsDTO> trackData, String lmsType) {
		try {
			List<String[]> row = new ArrayList<>();
			List<String> headerList = new ArrayList<>();
			headerList.add(lmsType + " TITLE");
			headerList.add(ASSET_TITLE);
			headerList.add(ASSET_TYPE);
			headerList.add(ASSET_CREATED_TIME);
			if (!teamMemberJourneyRequestDTO.isVendorVersion()) {
				headerList.add(OPENED_COUNT);
			}
			row.add(headerList.toArray(new String[0]));
			for (PartnerJourneyTrackDetailsDTO track : trackData) {
				List<String> dataList = new ArrayList<>();
				String createdTime = checkIfDateIsNull(track.getAssetCreatedTime());
				dataList.add(track.getTitle());
				dataList.add(track.getAssetName());
				dataList.add(track.getAssetType());
				dataList.add(createdTime);
				if (!teamMemberJourneyRequestDTO.isVendorVersion()) {
					dataList.add(track.getOpenedCount().toString());
				}
				row.add(dataList.toArray(new String[0]));
			}
			return XamplifyUtils.generateCSV(fileName, response, row);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return response;
	}

	@SuppressWarnings("unchecked")
	@Override
	public HttpServletResponse downloadShareLeadsDetailsReport(
			TeamMemberAnalyticsRequestDTO teamMemberJourneyRequestDTO, HttpServletResponse response) {
		String fileName = "Share-Leads-Details-Report";
		Pagination pagination = new Pagination();
		pagination.setExcludeLimit(true);
		framePaginationValues(teamMemberJourneyRequestDTO, pagination);
		if (pagination.getUserId() != null && pagination.getUserId() > 0) {
			utilService.setDateFilters(pagination);
			Map<String, Object> result = null;
			if (teamMemberJourneyRequestDTO.isVendorVersion()) {
				Integer vendorCompanyId = userDao.getCompanyIdByUserId(pagination.getUserId());
				pagination.setVendorCompanyId(vendorCompanyId);
				if (XamplifyUtils.isValidInteger(vendorCompanyId)) {
					result = teamMemberAnalyticsDao.getTeamMemberShareLeadDetailsForVendorTeamMember(pagination);
				}
			} else {
				Integer partnerCompanyId = userDao.getCompanyIdByUserId(pagination.getUserId());
				pagination.setPartnerCompanyId(partnerCompanyId);
				if (XamplifyUtils.isValidInteger(partnerCompanyId)) {
					result = teamMemberAnalyticsDao.getTeamMemberShareLeadDetails(pagination);
				}
			}
			if (result != null) {
				List<ShareLeadsDTO> shareLeadData = (List<ShareLeadsDTO>) result.get("list");
				return frameCSVDataForShareLeadsDetailsReport(response, teamMemberJourneyRequestDTO, fileName,
						shareLeadData);
			}
		}
		return response;
	}

	private HttpServletResponse frameCSVDataForShareLeadsDetailsReport(HttpServletResponse response,
			TeamMemberAnalyticsRequestDTO teamMemberJourneyRequestDTO, String fileName,
			List<ShareLeadsDTO> shareLeadData) {
		try {
			List<String[]> row = new ArrayList<>();
			List<String> headerList = new ArrayList<>();
			if (teamMemberJourneyRequestDTO.isVendorVersion()) {
				headerList.add(TEAM_MEMBER_NAME);
				headerList.add(TEAM_MEMBER_EMAIL_ID);
			} else {
				headerList.add(VENDOR_COMPANY);
				headerList.add(VENDOR_EMAIL_ID);
				headerList.add(PARTNER_TEAM_MEMBER_EMAIL_ID);
			}
			headerList.add(LIST_NAME);
			headerList.add(CREATED_ON);
			headerList.add("ASSIGNED ON(PST)");
			headerList.add("SHARE LEADS COUNT");
			row.add(headerList.toArray(new String[0]));
			for (ShareLeadsDTO shareLead : shareLeadData) {
				List<String> dataList = new ArrayList<>();
				String createdTime = checkIfDateIsNull(shareLead.getCreatedTime());
				String assignedTime = checkIfDateIsNull(shareLead.getAssignedDate());
				if (teamMemberJourneyRequestDTO.isVendorVersion()) {
					dataList.add(shareLead.getFullName());
					dataList.add(shareLead.getEmailId());
				} else {
					dataList.add(shareLead.getVendorCompany());
					dataList.add(shareLead.getVendorTeamMemberEmailId());
					dataList.add(shareLead.getEmailId());
				}
				dataList.add(shareLead.getListName());
				dataList.add(createdTime);
				dataList.add(assignedTime);
				dataList.add(shareLead.getShareLeadCount().toString());
				row.add(dataList.toArray(new String[0]));
			}
			return XamplifyUtils.generateCSV(fileName, response, row);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return response;
	}

	@SuppressWarnings("unchecked")
	@Override
	public HttpServletResponse downloadLeadsDetailsReport(TeamMemberAnalyticsRequestDTO teamMemberJourneyRequestDTO,
			HttpServletResponse response) {
		String fileName = "Lead-Details-Report";
		Pagination pagination = new Pagination();
		pagination.setExcludeLimit(true);
		framePaginationValues(teamMemberJourneyRequestDTO, pagination);
		if (pagination.getUserId() != null && pagination.getUserId() > 0) {
			utilService.setDateFilters(pagination);
			Map<String, Object> result = null;
			if (teamMemberJourneyRequestDTO.isVendorVersion()) {
				Integer vendorCompanyId = userDao.getCompanyIdByUserId(pagination.getUserId());
				pagination.setVendorCompanyId(vendorCompanyId);
				if (XamplifyUtils.isValidInteger(vendorCompanyId)) {
					result = teamMemberAnalyticsDao.getVendorTeamMemberLeadDetails(pagination);
				}
			} else {
				Integer partnerCompanyId = userDao.getCompanyIdByUserId(pagination.getUserId());
				pagination.setPartnerCompanyId(partnerCompanyId);
				if (XamplifyUtils.isValidInteger(partnerCompanyId)) {
					result = teamMemberAnalyticsDao.getTeamMemberLeadDetails(pagination);
				}
			}
			if (result != null) {
				List<LeadDto> leadData = (List<LeadDto>) result.get("list");
				return frameCSVDataForLeadsDetailsReport(response, teamMemberJourneyRequestDTO, fileName, leadData);
			}
		}
		return response;
	}

	private HttpServletResponse frameCSVDataForLeadsDetailsReport(HttpServletResponse response,
			TeamMemberAnalyticsRequestDTO teamMemberJourneyRequestDTO, String fileName, List<LeadDto> leadData) {
		try {
			List<String[]> row = new ArrayList<>();
			List<String> headerList = new ArrayList<>();
			headerList.add(CREATED_BY_NAME);
			headerList.add(CREATED_BY_EMAIL_ID);
			if (!teamMemberJourneyRequestDTO.isVendorVersion()) {
				headerList.add(CREATED_FOR_COMPANY_NAME);
			}
			headerList.add("LEAD COMPANY NAME");
			headerList.add("LEAD NAME");
			headerList.add("LEAD EMAIL ID");
			headerList.add("STAGE");
			headerList.add(CREATED_ON);
			row.add(headerList.toArray(new String[0]));
			for (LeadDto lead : leadData) {
				List<String> dataList = new ArrayList<>();
				String createdTime = checkIfDateIsNull(lead.getCreatedDate());
				dataList.add(lead.getCreatedByName());
				dataList.add(lead.getCreatedByEmail());
				if (!teamMemberJourneyRequestDTO.isVendorVersion()) {
					dataList.add(lead.getCreatedForCompanyName());
				}
				dataList.add(lead.getCompany());
				dataList.add(lead.getFullName());
				dataList.add(lead.getEmail());
				dataList.add(lead.getCurrentStageName());
				dataList.add(createdTime);
				row.add(dataList.toArray(new String[0]));
			}
			return XamplifyUtils.generateCSV(fileName, response, row);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return response;
	}

	@SuppressWarnings("unchecked")
	@Override
	public HttpServletResponse downloadDealDetailsReport(TeamMemberAnalyticsRequestDTO teamMemberJourneyRequestDTO,
			HttpServletResponse response) {
		String fileName = "Deal-Details-Report";
		Pagination pagination = new Pagination();
		pagination.setExcludeLimit(true);
		framePaginationValues(teamMemberJourneyRequestDTO, pagination);
		if (pagination.getUserId() != null && pagination.getUserId() > 0) {
			utilService.setDateFilters(pagination);
			Map<String, Object> result = null;
			if (teamMemberJourneyRequestDTO.isVendorVersion()) {
				Integer vendorCompanyId = userDao.getCompanyIdByUserId(pagination.getUserId());
				pagination.setVendorCompanyId(vendorCompanyId);
				if (XamplifyUtils.isValidInteger(vendorCompanyId)) {
					result = teamMemberAnalyticsDao.getVendorTeamMemberDealDetails(pagination);
				}
			} else {
				Integer partnerCompanyId = userDao.getCompanyIdByUserId(pagination.getUserId());
				pagination.setPartnerCompanyId(partnerCompanyId);
				if (XamplifyUtils.isValidInteger(partnerCompanyId)) {
					result = teamMemberAnalyticsDao.getTeamMemberDealDetails(pagination);
				}
			}
			if (result != null) {
				List<DealDto> dealData = (List<DealDto>) result.get("list");
				return frameCSVDataForDealDetailsReport(response, teamMemberJourneyRequestDTO, fileName, dealData);
			}
		}
		return response;
	}

	private HttpServletResponse frameCSVDataForDealDetailsReport(HttpServletResponse response,
			TeamMemberAnalyticsRequestDTO teamMemberJourneyRequestDTO, String fileName, List<DealDto> dealData) {
		try {
			List<String[]> row = new ArrayList<>();
			List<String> headerList = new ArrayList<>();
			headerList.add(CREATED_BY_NAME);
			headerList.add(CREATED_BY_EMAIL_ID);
			if (!teamMemberJourneyRequestDTO.isVendorVersion()) {
				headerList.add(CREATED_FOR_COMPANY_NAME);
			}
			headerList.add("DEAL TITLE");
			if (teamMemberJourneyRequestDTO.isVendorVersion()) {
				headerList.add("DEAL TYPE");
			}
			headerList.add("AMOUNT");
			headerList.add(CREATED_ON);
			row.add(headerList.toArray(new String[0]));
			for (DealDto deal : dealData) {
				List<String> dataList = new ArrayList<>();
				String createdTime = checkIfDateIsNull(deal.getCreatedDate());
				String dealAmount = deal.getAmount() != null ? "$" + deal.getAmount().toString() : "";
				dataList.add(deal.getCreatedByName());
				dataList.add(deal.getCreatedByEmail());
				if (!teamMemberJourneyRequestDTO.isVendorVersion()) {
					dataList.add(deal.getCreatedForCompanyName());
				}
				dataList.add(deal.getTitle());
				if (teamMemberJourneyRequestDTO.isVendorVersion()) {
					dataList.add(deal.getDealType());
				}
				dataList.add(dealAmount);
				dataList.add(createdTime);
				row.add(dataList.toArray(new String[0]));
			}
			return XamplifyUtils.generateCSV(fileName, response, row);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return response;
	}

	@Override
	public HttpServletResponse downloadMDFDetailsReport(TeamMemberAnalyticsRequestDTO teamMemberJourneyRequestDTO,
			HttpServletResponse response) {
		String fileName = "MDF-Details-Report";
		Pagination pagination = new Pagination();
		pagination.setExcludeLimit(true);
		framePaginationValues(teamMemberJourneyRequestDTO, pagination);
		if (pagination.getUserId() != null && pagination.getUserId() > 0) {
			utilService.setDateFilters(pagination);
			Map<String, Object> result = null;
			Integer partnerCompanyId = userDao.getCompanyIdByUserId(pagination.getUserId());
			pagination.setPartnerCompanyId(partnerCompanyId);
			if (XamplifyUtils.isValidInteger(partnerCompanyId)) {
				result = teamMemberAnalyticsDao.getTeamMemberMdfDetails(pagination);
			}
			if (result != null) {
				@SuppressWarnings("unchecked")
				List<MdfRequestDeatailsDTO> mdfData = (List<MdfRequestDeatailsDTO>) result.get("list");
				return frameCSVDataForMDFDetailsReport(response, fileName, mdfData);
			}
		}
		return response;
	}

	private HttpServletResponse frameCSVDataForMDFDetailsReport(HttpServletResponse response, String fileName,
			List<MdfRequestDeatailsDTO> mdfData) {
		try {
			List<String[]> row = new ArrayList<>();
			List<String> headerList = new ArrayList<>();
			headerList.add(VENDOR_COMPANY);
			headerList.add(VENDOR_EMAIL_ID);
			headerList.add(PARTNER_TEAM_MEMBER_EMAIL_ID);
			headerList.add("TOTAL REQUESTS");
			headerList.add("AVERAGE REQUEST SIZE");
			headerList.add("TOTAL VALUE");
			headerList.add("CREATED TIME");
			headerList.add("ALLOCATION TIME");
			row.add(headerList.toArray(new String[0]));
			for (MdfRequestDeatailsDTO mdf : mdfData) {
				List<String> dataList = new ArrayList<>();
				String createdTime = checkIfDateIsNull(mdf.getCreatedTime());
				String allocationTime = checkIfDateIsNull(mdf.getAllocationTime());
				dataList.add(mdf.getCompanyName());
				dataList.add(mdf.getEmailId());
				dataList.add(mdf.getPartnerEmailId());
				dataList.add(mdf.getTotalRequestsInString());
				dataList.add(mdf.getAverageRequestSizeInString());
				dataList.add(mdf.getTotalValueInString());
				dataList.add(createdTime);
				dataList.add(allocationTime);
				row.add(dataList.toArray(new String[0]));
			}
			return XamplifyUtils.generateCSV(fileName, response, row);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return response;
	}

	@SuppressWarnings("unchecked")
	@Override
	public HttpServletResponse downloadContactDetailsReport(TeamMemberAnalyticsRequestDTO teamMemberJourneyRequestDTO,
			HttpServletResponse response) {
		String fileName = "Contact-Details-Report";
		Pagination pagination = new Pagination();
		pagination.setExcludeLimit(true);
		framePaginationValues(teamMemberJourneyRequestDTO, pagination);
		if (pagination.getUserId() != null && pagination.getUserId() > 0) {
			utilService.setDateFilters(pagination);
			Map<String, Object> result = null;
			if (teamMemberJourneyRequestDTO.isVendorVersion()) {
				Integer vendorCompanyId = userDao.getCompanyIdByUserId(pagination.getUserId());
				pagination.setVendorCompanyId(vendorCompanyId);
				if (XamplifyUtils.isValidInteger(vendorCompanyId)) {
					result = teamMemberAnalyticsDao.getVendorTeamMemberContactDetails(pagination);
				}
			} else {
				Integer partnerCompanyId = userDao.getCompanyIdByUserId(pagination.getUserId());
				pagination.setPartnerCompanyId(partnerCompanyId);
				if (XamplifyUtils.isValidInteger(partnerCompanyId)) {
					result = teamMemberAnalyticsDao.getContactsDetailsForTeamMember(pagination);
				}
			}
			if (result != null) {
				List<ContactsDetailsDTO> contactData = (List<ContactsDetailsDTO>) result.get("list");
				return frameCSVDataForContactDetailsReport(response, fileName, contactData);
			}
		}
		return response;
	}

	private HttpServletResponse frameCSVDataForContactDetailsReport(HttpServletResponse response, String fileName,
			List<ContactsDetailsDTO> contactData) {
		try {
			List<String[]> row = new ArrayList<>();
			List<String> headerList = new ArrayList<>();
			headerList.add(TEAM_MEMBER_NAME);
			headerList.add(TEAM_MEMBER_EMAIL_ID);
			headerList.add(LIST_NAME);
			headerList.add(CREATED_ON);
			headerList.add(CONTACTS_COUNT);
			row.add(headerList.toArray(new String[0]));
			for (ContactsDetailsDTO contact : contactData) {
				List<String> dataList = new ArrayList<>();
				String createdTime = checkIfDateIsNull(contact.getCreatedTime());
				dataList.add(contact.getFullName());
				dataList.add(contact.getEmailId());
				dataList.add(contact.getUserListName());
				dataList.add(createdTime);
				dataList.add(contact.getContacts());
				row.add(dataList.toArray(new String[0]));
			}
			return XamplifyUtils.generateCSV(fileName, response, row);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return response;
	}

	@SuppressWarnings("unchecked")
	@Override
	public HttpServletResponse downloadCompanyDetailsReport(TeamMemberAnalyticsRequestDTO teamMemberJourneyRequestDTO,
			HttpServletResponse response) {
		String fileName = "Company-Details-Report";
		Pagination pagination = new Pagination();
		pagination.setExcludeLimit(true);
		framePaginationValues(teamMemberJourneyRequestDTO, pagination);
		if (pagination.getUserId() != null && pagination.getUserId() > 0) {
			utilService.setDateFilters(pagination);
			Map<String, Object> result = null;
			Integer partnerCompanyId = userDao.getCompanyIdByUserId(pagination.getUserId());
			pagination.setPartnerCompanyId(partnerCompanyId);
			if (XamplifyUtils.isValidInteger(partnerCompanyId)) {
				result = teamMemberAnalyticsDao.getCompanyDetailsForTeamMember(pagination);
			}
			if (result != null) {
				List<ContactsDetailsDTO> companyData = (List<ContactsDetailsDTO>) result.get("list");
				return frameCSVDataForCompanyDetailsReport(response, fileName, companyData);
			}
		}
		return response;
	}

	private HttpServletResponse frameCSVDataForCompanyDetailsReport(HttpServletResponse response, String fileName,
			List<ContactsDetailsDTO> companyData) {
		try {
			List<String[]> row = new ArrayList<>();
			List<String> headerList = new ArrayList<>();
			headerList.add(TEAM_MEMBER_NAME);
			headerList.add(TEAM_MEMBER_EMAIL_ID);
			headerList.add("COMPANY NAME");
			headerList.add("COMPANY LIST NAME");
			headerList.add(CONTACTS_COUNT);
			row.add(headerList.toArray(new String[0]));
			for (ContactsDetailsDTO company : companyData) {
				List<String> dataList = new ArrayList<>();
				dataList.add(company.getFullName());
				dataList.add(company.getEmailId());
				dataList.add(company.getCompany());
				dataList.add(company.getUserListName());
				dataList.add(company.getContacts());
				row.add(dataList.toArray(new String[0]));
			}
			return XamplifyUtils.generateCSV(fileName, response, row);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return response;
	}

	@SuppressWarnings("unchecked")
	@Override
	public HttpServletResponse downloadAssetCountReport(TeamMemberAnalyticsRequestDTO teamMemberJourneyRequestDTO,
			HttpServletResponse response) {
		String fileName = "Assets-Details-Report";
		Pagination pagination = new Pagination();
		pagination.setExcludeLimit(true);
		framePaginationValues(teamMemberJourneyRequestDTO, pagination);
		if (pagination.getUserId() != null && pagination.getUserId() > 0) {
			utilService.setDateFilters(pagination);
			Map<String, Object> result = null;
			Integer vendorCompanyId = userDao.getCompanyIdByUserId(pagination.getUserId());
			pagination.setVendorCompanyId(vendorCompanyId);
			if (XamplifyUtils.isValidInteger(vendorCompanyId)) {
				result = teamMemberAnalyticsDao.getVendorTeamMemberAssetsCount(pagination);
			}
			if (result != null) {
				List<PartnerJourneyTrackDetailsDTO> trackData = (List<PartnerJourneyTrackDetailsDTO>) result
						.get("list");
				return frameCSVDataForAssetCountReport(response, fileName, trackData);
			}
		}
		return response;
	}

	private HttpServletResponse frameCSVDataForAssetCountReport(HttpServletResponse response, String fileName,
			List<PartnerJourneyTrackDetailsDTO> trackData) {
		try {
			List<String[]> row = new ArrayList<>();
			List<String> headerList = new ArrayList<>();
			headerList.add(TEAM_MEMBER_NAME);
			headerList.add(TEAM_MEMBER_EMAIL_ID);
			headerList.add("ASSET COUNT");
			row.add(headerList.toArray(new String[0]));
			for (PartnerJourneyTrackDetailsDTO track : trackData) {
				List<String> dataList = new ArrayList<>();
				dataList.add(track.getFullName());
				dataList.add(track.getEmailId());
				dataList.add(track.getAssetCount().toString());
				row.add(dataList.toArray(new String[0]));
			}
			return XamplifyUtils.generateCSV(fileName, response, row);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return response;
	}

	@SuppressWarnings("unchecked")
	@Override
	public HttpServletResponse downloadAssetDetailsReport(TeamMemberAnalyticsRequestDTO teamMemberJourneyRequestDTO,
			HttpServletResponse response) {
		String fileName = "Assets-Detailed-Report";
		Pagination pagination = new Pagination();
		pagination.setExcludeLimit(true);
		framePaginationValues(teamMemberJourneyRequestDTO, pagination);
		if (pagination.getUserId() != null && pagination.getUserId() > 0) {
			utilService.setDateFilters(pagination);
			Map<String, Object> result = null;
			Integer vendorCompanyId = userDao.getCompanyIdByUserId(pagination.getUserId());
			pagination.setVendorCompanyId(vendorCompanyId);
			if (XamplifyUtils.isValidInteger(vendorCompanyId)) {
				result = teamMemberAnalyticsDao.getVendorTeamMemberAssetsDetails(pagination);
			}
			if (result != null) {
				List<PartnerJourneyTrackDetailsDTO> trackData = (List<PartnerJourneyTrackDetailsDTO>) result
						.get("list");
				return frameCSVDataForAssetDetailsReport(response, fileName, trackData);
			}
		}
		return response;
	}

	private HttpServletResponse frameCSVDataForAssetDetailsReport(HttpServletResponse response, String fileName,
			List<PartnerJourneyTrackDetailsDTO> trackData) {
		try {
			List<String[]> row = new ArrayList<>();
			List<String> headerList = new ArrayList<>();
			headerList.add(TEAM_MEMBER_EMAIL_ID);
			headerList.add(ASSET_TITLE);
			headerList.add(ASSET_TYPE);
			headerList.add(ASSET_CREATED_TIME);
			headerList.add("ASSET PUBLISHED TIME(PST)");
			row.add(headerList.toArray(new String[0]));
			for (PartnerJourneyTrackDetailsDTO track : trackData) {
				List<String> dataList = new ArrayList<>();
				String createdTime = checkIfDateIsNull(track.getAssetCreatedTime());
				String publishedTime = checkIfDateIsNull(track.getPublishedOn());
				dataList.add(track.getEmailId());
				dataList.add(track.getAssetName());
				dataList.add(track.getAssetType());
				dataList.add(createdTime);
				dataList.add(publishedTime);
				row.add(dataList.toArray(new String[0]));
			}
			return XamplifyUtils.generateCSV(fileName, response, row);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return response;
	}

	@SuppressWarnings("unchecked")
	@Override
	public HttpServletResponse downloadAllPartnersDetailsReport(
			TeamMemberAnalyticsRequestDTO teamMemberJourneyRequestDTO, HttpServletResponse response) {
		String fileName = "All-Partners-Details-Report";
		Pagination pagination = new Pagination();
		pagination.setExcludeLimit(true);
		framePaginationValues(teamMemberJourneyRequestDTO, pagination);
		if (pagination.getUserId() != null && pagination.getUserId() > 0) {
			utilService.setDateFilters(pagination);
			Map<String, Object> result = null;
			Integer vendorCompanyId = userDao.getCompanyIdByUserId(pagination.getUserId());
			pagination.setVendorCompanyId(vendorCompanyId);
			if (XamplifyUtils.isValidInteger(vendorCompanyId)) {
				result = teamMemberAnalyticsDao.getallPartnersDetailsForVendor(pagination);
			}
			if (result != null) {
				List<AllPartnersDetailsDTO> partnerData = (List<AllPartnersDetailsDTO>) result.get("list");
				return frameCSVDataForAllPartnersDetailsReport(response, fileName, partnerData);
			}
		}
		return response;
	}

	private HttpServletResponse frameCSVDataForAllPartnersDetailsReport(HttpServletResponse response, String fileName,
			List<AllPartnersDetailsDTO> partnerData) {
		try {
			List<String[]> row = new ArrayList<>();
			List<String> headerList = new ArrayList<>();
			headerList.add(TEAM_MEMBER_NAME);
			headerList.add(TEAM_MEMBER_EMAIL_ID);
			headerList.add("PARTNER COMPANY NAME");
			headerList.add("PARTNER NAME");
			headerList.add("PARTNER EMAIL ID");
			headerList.add("STATUS");
			headerList.add("ONBOARDED TIME(PST)");
			row.add(headerList.toArray(new String[0]));
			for (AllPartnersDetailsDTO partner : partnerData) {
				List<String> dataList = new ArrayList<>();
				String onboardTime = checkIfDateIsNull(partner.getOnboardTime());
				dataList.add(partner.getFullName());
				dataList.add(partner.getEmailId());
				dataList.add(partner.getPartnerCompany());
				dataList.add(partner.getPartnerName());
				dataList.add(partner.getPartnerEmailId());
				dataList.add(partner.getUserStatus());
				dataList.add(onboardTime);
				row.add(dataList.toArray(new String[0]));
			}
			return XamplifyUtils.generateCSV(fileName, response, row);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return response;
	}

	@Override
	public XtremandResponse getTeamMemberAssetsDetails(TeamMemberAnalyticsRequestDTO teamMemberJourneyRequestDTO) {
		XtremandResponse response = new XtremandResponse();
		response.setStatusCode(400);
		Integer partnerCompanyId = userDao.getCompanyIdByUserId(teamMemberJourneyRequestDTO.getLoggedInUserId());
		if (XamplifyUtils.isValidInteger(partnerCompanyId)) {
			Pagination pagination = new Pagination();
			framePaginationValues(teamMemberJourneyRequestDTO, pagination);
			pagination.setPartnerCompanyId(partnerCompanyId);
			pagination.setPageIndex(teamMemberJourneyRequestDTO.getPageNumber());
			pagination.setMaxResults(teamMemberJourneyRequestDTO.getLimit());
			utilService.setDateFilters(pagination);
			response.setData(teamMemberAnalyticsDao.getTeamMemberAssetsDetails(pagination));
			response.setStatusCode(200);
		}
		return response;
	}

	@SuppressWarnings("unchecked")
	@Override
	public HttpServletResponse downloadTeamMemberAssetDetailsReport(
			TeamMemberAnalyticsRequestDTO teamMemberJourneyRequestDTO, HttpServletResponse response) {
		String fileName = "Asset-Details-Report";
		Pagination pagination = new Pagination();
		pagination.setExcludeLimit(true);
		framePaginationValues(teamMemberJourneyRequestDTO, pagination);
		if (pagination.getUserId() != null && pagination.getUserId() > 0) {
			utilService.setDateFilters(pagination);
			Map<String, Object> result = null;
			Integer partnerCompanyId = userDao.getCompanyIdByUserId(pagination.getUserId());
			pagination.setPartnerCompanyId(partnerCompanyId);
			if (XamplifyUtils.isValidInteger(partnerCompanyId)) {
				result = teamMemberAnalyticsDao.getTeamMemberAssetsDetails(pagination);
			}
			if (result != null) {
				List<PartnerJourneyTrackDetailsDTO> trackData = (List<PartnerJourneyTrackDetailsDTO>) result
						.get("list");
				return frameCSVDataForTeamMemberAssetDetailsReport(response, fileName, trackData);
			}
		}
		return response;
	}

	private HttpServletResponse frameCSVDataForTeamMemberAssetDetailsReport(HttpServletResponse response,
			String fileName, List<PartnerJourneyTrackDetailsDTO> trackData) {
		try {
			List<String[]> row = new ArrayList<>();
			List<String> headerList = new ArrayList<>();
			headerList.add(VENDOR_COMPANY);
			headerList.add(VENDOR_EMAIL_ID);
			headerList.add(PARTNER_TEAM_MEMBER_EMAIL_ID);
			headerList.add(ASSET_TITLE);
			headerList.add(ASSET_TYPE);
			headerList.add("PUBLISHED ON(PST)");
			row.add(headerList.toArray(new String[0]));
			for (PartnerJourneyTrackDetailsDTO track : trackData) {
				List<String> dataList = new ArrayList<>();
				String publishedDate = checkIfDateIsNull(track.getPublishedOn());
				dataList.add(track.getVendorCompany());
				dataList.add(track.getVendorTeamMemberEmailId());
				dataList.add(track.getEmailId());
				dataList.add(track.getAssetName());
				dataList.add(track.getAssetType());
				dataList.add(publishedDate);
				row.add(dataList.toArray(new String[0]));
			}
			return XamplifyUtils.generateCSV(fileName, response, row);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return response;
	}

}
