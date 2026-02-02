package com.xtremand.analytics.service.impl;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.text.WordUtils;
import org.apache.poi.common.usermodel.HyperlinkType;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Hyperlink;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.neovisionaries.i18n.CountryCode;
import com.xtremand.analytics.dao.PartnerAnalyticsDAO;
import com.xtremand.analytics.dao.VendorAnalyticsDAO;
import com.xtremand.analytics.service.PartnerAnalyticsService;
import com.xtremand.common.bom.CompanyProfile;
import com.xtremand.common.bom.FindLevel;
import com.xtremand.common.bom.Pagination;
import com.xtremand.dao.util.GenericDAO;
import com.xtremand.deal.dto.DealDto;
import com.xtremand.formbeans.UserDTO;
import com.xtremand.formbeans.XtremandResponse;
import com.xtremand.lead.dto.LeadDto;
import com.xtremand.mail.service.MailService;
import com.xtremand.mdf.dto.MdfAmountTilesDTO;
import com.xtremand.partner.bom.PartnerContactUsageDTO;
import com.xtremand.partner.bom.PartnerDTO;
import com.xtremand.partnership.dao.PartnershipDAO;
import com.xtremand.team.dao.TeamDao;
import com.xtremand.team.member.dto.TeamMemberDTO;
import com.xtremand.team.member.dto.TeamMemberListDTO;
import com.xtremand.user.bom.ShareLeadsDTO;
import com.xtremand.user.dao.UserDAO;
import com.xtremand.user.service.UserService;
import com.xtremand.userlist.dao.UserListDAO;
import com.xtremand.userlist.service.UserListService;
import com.xtremand.util.DateUtils;
import com.xtremand.util.XamplifyUtil;
import com.xtremand.util.XamplifyUtils;
import com.xtremand.util.dao.UtilDao;
import com.xtremand.util.dto.BarChartDualAxesDTO;
import com.xtremand.util.dto.NumberFormatterString;
import com.xtremand.util.dto.Pageable;
import com.xtremand.util.dto.PaginatedDTO;
import com.xtremand.util.dto.PartnerCompanyDTO;
import com.xtremand.util.dto.PartnerJourneyAnalyticsDTO;
import com.xtremand.util.dto.PartnerJourneyRequestDTO;
import com.xtremand.util.dto.PartnerJourneyTrackDetailsDTO;
import com.xtremand.util.dto.TeamMemberFilterDTO;
import com.xtremand.util.dto.XamplifyConstants;
import com.xtremand.util.service.UtilService;

@Service("partnerAnalyticsService")
@Transactional
public class PartnerAnalyticsServiceImpl implements PartnerAnalyticsService {

	private static final String INVALID_DATA = "Invalid Input Parameters";

	@Autowired
	PartnerAnalyticsDAO partnerAnalyticsDAO;

	@Autowired
	GenericDAO genericDao;

	@Autowired
	MailService mailService;

	@Autowired
	UserService userService;

	@Autowired
	UserListService userListService;

	@Autowired
	UserListDAO userListDAO;

	@Autowired
	UserDAO userDao;

	@Autowired
	VendorAnalyticsDAO vendorAnalyticsDAO;

	@Autowired
	PartnershipDAO partnershipDAO;

	@Autowired
	TeamDao teamDao;

	// XNFR-316
	@Autowired
	private XamplifyUtil xamplifyUtil;

	@Autowired
	private UtilDao utilDao;

	@Autowired
	UtilDao utilDAO;

	@Autowired
	UtilService utilService;

	@Value("${partnerJourneyInteracted}")
	private String partnerJourneyInteracted;

	@Value("${partnerJourneyNotInteracted}")
	private String partnerJourneyNotInteracted;

	@Value("${server_path}")
	String server_path;

	@Value("${replace.company.logo}")
	private String replaceCompanyLogo;

	private static final String PARTNER_COMPANY = "PARTNER COMPANY";

	private static final String TRACK_TITLE = "TRACK TITLE";

	private static final String TEAM_MEMBER_NAME = "TEAM MEMBER NAME";

	private static final String TEAM_MEMBER_EMAIL_ID = "TEAM MEMBER EMAIL ID";

	/**** XNFR-835 ****/
	private static final String NAME = "NAME";

	private static final String COMPANY_NAME = "COMPANY " + NAME;

	private static final String EMAIL_ID = "EMAIL ID";

	private static final String MOBILE_NUMBER = "MOBILE NUMBER";

	private static final String JOB_TITLE = "JOB TITLE";

	private static final String STATUS = "STATUS";

	// XNFR-1026
	private static final String LAST_REMINDER_SENT = "LAST REMINDER SENT";

	@Override
	public Integer getCompanyPartnersCount(Integer companyId) {
		return partnerAnalyticsDAO.getCompanyPartnersCount(companyId);
	}

	@Override
	public Map<String, Object> listCountrywisePartnersCount(Integer userId, Integer companyId) {
		Map<String, Object> resultMap = new HashMap<>();
		List<Object[]> list = partnerAnalyticsDAO.listCountrywisePartnersCount(userId, companyId);
		JSONArray countrywisePartnersJsonArray = new JSONArray();
		for (int i = 0; i < list.size(); i++) {
			Object[] row = list.get(i);
			JSONArray json = new JSONArray();
			String code;

			String countryName = ((String) row[0]).equalsIgnoreCase("USA") ? "United States"
					: WordUtils.capitalizeFully((String) row[0]);
			if (StringUtils.hasText(countryName) && !countryName.equalsIgnoreCase("Select Country")
					&& !countryName.equalsIgnoreCase("Country")) {
				if (!CountryCode.findByName(countryName).isEmpty()) {
					code = CountryCode.findByName(countryName).get(0).name();
					json.put(code.toLowerCase());
					json.put(row[1] != null ? ((java.math.BigInteger) row[1]).intValue() : null);
					countrywisePartnersJsonArray.put(json);
				}

			}
		}
		resultMap.put("countrywisepartners", countrywisePartnersJsonArray);
		return resultMap;
	}

	@Override
	public XtremandResponse sendPartnerReminder(UserDTO userDto, Integer vendorId) {
		return new XtremandResponse();
	}

	private void setDataNotFound(XtremandResponse response) {
		response.setStatusCode(404);
		response.setMessage("No Data Found.");
	}

	@Override
	public XtremandResponse findPartnerCompanyNamesAndLeadsAndDealsCount(
			PartnerJourneyRequestDTO partnerJourneyRequestDTO) {
		XtremandResponse response = new XtremandResponse();
		utilService.setDateFiltersForPartnerJourney(partnerJourneyRequestDTO);
		Integer userId = partnerJourneyRequestDTO.getLoggedInUserId();
		if (XamplifyUtils.isValidInteger(userId)) {
			Integer companyId = userDao.getCompanyIdByUserId(userId);
			List<Object[]> list = partnerAnalyticsDAO
					.findPartnerCompanyNamesAndLeadsAndDealsCount(partnerJourneyRequestDTO, companyId);
			if (list != null && !list.isEmpty()) {
				setDTOData(response, list);
			} else {
				setDataNotFound(response);
			}
		}
		return response;
	}

	private void setDTOData(XtremandResponse response, List<Object[]> list) {
		BarChartDualAxesDTO barChartDualAxesDTO = new BarChartDualAxesDTO();
		List<String> partnerCompanyNames = new ArrayList<>();
		List<Integer> leadsCount = new ArrayList<>();
		List<Integer> dealsCount = new ArrayList<>();
		for (Object[] object : list) {
			partnerCompanyNames.add((String) object[0]);
			BigDecimal leadCountInBigInt = (BigDecimal) object[1];
			leadsCount.add(leadCountInBigInt != null ? leadCountInBigInt.intValue() : 0);
			BigDecimal dealCountInBigInt = (BigDecimal) object[2];
			dealsCount.add(dealCountInBigInt != null ? dealCountInBigInt.intValue() : 0);
		}
		barChartDualAxesDTO.setXaxis(partnerCompanyNames);
		barChartDualAxesDTO.setYaxis1(dealsCount);
		barChartDualAxesDTO.setYaxis2(leadsCount);
		response.setStatusCode(200);
		response.setData(barChartDualAxesDTO);
	}

	private void extractValuesandSetDTOData(XtremandResponse response, List<Object[]> list) {
		BarChartDualAxesDTO barChartDualAxesDTO = new BarChartDualAxesDTO();
		List<String> partnerCompanyNames = new ArrayList<>();
		List<Integer> leadsCount = new ArrayList<>();
		List<Integer> contactsCount = new ArrayList<>();
		for (Object[] object : list) {
			partnerCompanyNames.add((String) object[1]);
			BigDecimal leadCountInBigInt = (BigDecimal) object[3];
			leadsCount.add(leadCountInBigInt != null ? leadCountInBigInt.intValue() : 0);
			BigDecimal contactsCountInBigInt = (BigDecimal) object[2];
			contactsCount.add(contactsCountInBigInt != null ? contactsCountInBigInt.intValue() : 0);
		}
		barChartDualAxesDTO.setXaxis(partnerCompanyNames);
		barChartDualAxesDTO.setYaxis1(leadsCount);
		barChartDualAxesDTO.setYaxis2(contactsCount);
		response.setStatusCode(200);
		response.setData(barChartDualAxesDTO);
	}

	@Override
	public XtremandResponse findAllLeadsAndDealsCount(PartnerJourneyRequestDTO partnerJourneyRequestDTO) {
		XtremandResponse response = new XtremandResponse();
		utilService.setDateFiltersForPartnerJourney(partnerJourneyRequestDTO);
		Integer userId = partnerJourneyRequestDTO.getLoggedInUserId();
		if (XamplifyUtils.isValidInteger(userId)) {
			Integer companyId = userDao.getCompanyIdByUserId(userId);
			List<Object[]> list = partnerAnalyticsDAO
					.findAllPartnerCompanyNamesAndLeadsAndDealsCount(partnerJourneyRequestDTO, companyId);
			if (list != null && !list.isEmpty()) {
				setDTOData(response, list);
			} else {
				setDataNotFound(response);
			}
		}
		return response;
	}

	@Override
	public XtremandResponse findLeadsToDealsConversionPercentageAsText(Integer companyId, Integer userId,
			boolean applyFilter) {
		XtremandResponse response = new XtremandResponse();
		String percentageAsText = partnerAnalyticsDAO.findLeadsToDealsConversionPercentageAsText(companyId, userId,
				applyFilter);
		if (StringUtils.hasText(percentageAsText)) {
			response.setData(percentageAsText);
		} else {
			response.setData("0.00%");
		}
		return response;
	}

	@Override
	public XtremandResponse findOpportunityAmountAsText(Integer companyId, Integer userId, boolean applyFilter) {
		XtremandResponse response = new XtremandResponse();
		Double opportunityAmount = partnerAnalyticsDAO.findOpportunityAmount(companyId, userId, applyFilter);
		response.setData("$ " + NumberFormatterString.formatValueInTrillionsOrBillions(opportunityAmount));
		return response;
	}

	@Override
	public XtremandResponse findChannelCampaigns(Pagination pagination) {
		XtremandResponse response = new XtremandResponse();
		response.setData(partnerAnalyticsDAO.findChannelCampaigns(pagination));
		response.setStatusCode(4000);
		return response;
	}

	@Override
	public XtremandResponse findRedistributedCampaigns(Pagination pagination) {
		XtremandResponse response = new XtremandResponse();
		response.setData(partnerAnalyticsDAO.findRedistributedCampaigns(pagination));
		response.setStatusCode(4000);
		return response;
	}

	@Override
	public Map<String, Object> countrywisePartnersCount(Integer userId, boolean applyFilter) {
		Integer companyId = userDao.getCompanyIdByUserId(userId);
		return listCountrywisePartnersCount(userId, companyId);
	}

	@Override
	public Map<String, Object> findRedistributedCampaignsCount(Integer userId, boolean applyFilter) {
		Map<String, Object> map = new HashMap<String, Object>();
		Integer companyId = userDao.getCompanyIdByUserId(userId);
		Integer totalRecords = partnerAnalyticsDAO.partnersRedistributedCampaignsCount(companyId, userId, applyFilter);
		map.put("redistributedCampaignsCount", totalRecords);
		return map;
	}

	@Override
	public Map<String, Object> findActivePartnersCount(Integer userId, boolean applyFilter) {
		Map<String, Object> map = new HashMap<>();
		Integer companyId = userDao.getCompanyIdByUserId(userId);
		map.put("activePartnersCount", partnerAnalyticsDAO.getActivePartnersCount(companyId, applyFilter, userId));
		return map;
	}

	@Override
	public Map<String, Object> findThroughPartnerCampaignsCount(Integer userId, boolean applyFilter) {
		Map<String, Object> map = new HashMap<>();
		Integer companyId = userDao.getCompanyIdByUserId(userId);
		map.put("throughPartnerCampaignsCount",
				partnerAnalyticsDAO.getThroughPartnerCampaignsCount(companyId, applyFilter, userId));
		return map;
	}

	@Override
	public Map<String, Object> findInActivePartnersCount(Integer userId, boolean applyFilter) {
		Map<String, Object> map = new HashMap<String, Object>();
		Integer companyId = userDao.getCompanyIdByUserId(userId);
		map.put("inActivePartnersCount", partnerAnalyticsDAO.getInactivePartnersCount(companyId, applyFilter, userId));
		return map;
	}

	@Override
	public Map<String, Object> findApprovePartnersCount(Integer userId, boolean applyFilter) {
		Map<String, Object> map = new HashMap<String, Object>();
		CompanyProfile companyProfile = userDao.findByPrimaryKey(userId, new FindLevel[] { FindLevel.COMPANY_PROFILE })
				.getCompanyProfile();
		map.put("approvePartnersCount", partnershipDAO.getApprovePartnersCount(companyProfile, userId, applyFilter));
		return map;
	}

	@Override
	public XtremandResponse findAllPartnerCompanies(Pagination pagination) {
		XtremandResponse response = new XtremandResponse();
		Integer vendorCompanyId = userDao.getCompanyIdByUserId(pagination.getUserId());
		pagination.setVendorCompanyId(vendorCompanyId);
		response.setData(partnershipDAO.findAllPartnerCompanies(pagination));
		response.setStatusCode(200);
		return response;
	}

	@Override
	public XtremandResponse findJourney(Integer partnershipId) {
		XtremandResponse response = new XtremandResponse();
		response.setData(partnershipDAO.findJourney(partnershipId));
		response.setStatusCode(200);
		return response;
	}

	/**** XNFR-316 ***/
	@Override
	public XtremandResponse getActivePartnerCompanies(Pagination pagination) {
		XtremandResponse response = new XtremandResponse();
		response.setStatusCode(400);
		if (pagination != null && pagination.getUserId() != null && pagination.getUserId() > 0) {
			Integer vendorCompanyId = userDao.getCompanyIdByUserId(pagination.getUserId());
			if (vendorCompanyId != null && vendorCompanyId > 0) {
				pagination.setVendorCompanyId(vendorCompanyId);
				response.setData(partnershipDAO.getActivePartnerCompanies(pagination));
				response.setStatusCode(200);
			}
		}
		return response;
	}

	@Override
	public XtremandResponse getPartnerJourneyCompanyInfo(Integer partnerCompanyId, Integer loggedInUserId) {
		XtremandResponse response = new XtremandResponse();
		response.setStatusCode(400);
		if (XamplifyUtils.isValidInteger(partnerCompanyId) && XamplifyUtils.isValidInteger(loggedInUserId)) {
			Integer vendorCompanyId = userDao.getCompanyIdByUserId(loggedInUserId);
			if (XamplifyUtils.isValidInteger(vendorCompanyId)) {
				PartnerCompanyDTO companyInfo = partnerAnalyticsDAO.getPartnerJourneyCompanyInfo(vendorCompanyId,
						partnerCompanyId);
				response.setData(companyInfo);
				response.setStatusCode(200);
			}
		}
		return response;
	}

	@Override
	public XtremandResponse getPartnerJourneyTeamInfo(Pagination pagination) {
		XtremandResponse response = new XtremandResponse();
		response.setStatusCode(400);
		if (pagination.getUserId() != null && pagination.getUserId() > 0) {
			Integer vendorCompanyId = userDao.getCompanyIdByUserId(pagination.getUserId());
			pagination.setVendorCompanyId(vendorCompanyId);
			if (vendorCompanyId != null && vendorCompanyId > 0) {
				utilService.setDateFilters(pagination);
				response.setData(partnerAnalyticsDAO.getPartnerJourneyTeamInfo(pagination));
				response.setStatusCode(200);
			}
		}
		return response;
	}

	@Override
	public XtremandResponse getPartnerJourneyCounts(PartnerJourneyRequestDTO partnerJourneyRequestDTO) {
		XtremandResponse response = new XtremandResponse();
		response.setStatusCode(400);
		if (partnerJourneyRequestDTO.getLoggedInUserId() != null && partnerJourneyRequestDTO.getLoggedInUserId() > 0) {
			Integer vendorCompanyId = userDao.getCompanyIdByUserId(partnerJourneyRequestDTO.getLoggedInUserId());
			partnerJourneyRequestDTO.setVendorCompanyId(vendorCompanyId);
			if (vendorCompanyId != null && vendorCompanyId > 0) {
				PartnerJourneyAnalyticsDTO partnerJourneyAnalytics = new PartnerJourneyAnalyticsDTO();
				utilService.setDateFiltersForPartnerJourney(partnerJourneyRequestDTO);
				if (!utilDAO.isPrmByVendorCompanyId(vendorCompanyId)) {
					String redistributedCampaignCount = partnerAnalyticsDAO
							.getPartnerJourneyRedistributedCampaignCount(partnerJourneyRequestDTO);
					partnerJourneyAnalytics.setShowRedistributedCampaignCount(true);
					partnerJourneyAnalytics.setRedistributedCampaignCount(redistributedCampaignCount);
				}
				if (utilDAO.hasShareLeadsAccessByCompanyId(vendorCompanyId)) {
					String shareLeadCount = partnerAnalyticsDAO
							.getPartnerJourneyShareLeadCount(partnerJourneyRequestDTO);
					partnerJourneyAnalytics.setShareLeadCount(shareLeadCount);
					partnerJourneyAnalytics.setShowShareLeadCount(true);
				}
				String teamMemberCount = partnerAnalyticsDAO.getPartnerJourneyTeamMemberCount(partnerJourneyRequestDTO);
				partnerJourneyAnalytics.setTeamMemberCount(teamMemberCount);
				partnerJourneyAnalytics.setShowTeamMemberCount(true);
				if (utilDAO.hasEnableLeadsAccessByCompanyId(vendorCompanyId)) {
					String leadCount = partnerAnalyticsDAO.getPartnerJourneyLeadCount(partnerJourneyRequestDTO);
					String dealCount = partnerAnalyticsDAO.getPartnerJourneyDealCount(partnerJourneyRequestDTO);
					partnerJourneyAnalytics.setLeadCount(leadCount);
					partnerJourneyAnalytics.setDealCount(dealCount);
					partnerJourneyAnalytics.setShowLeadCount(true);
					partnerJourneyAnalytics.setShowDealCount(true);
				}
				if (utilDAO.hasMdfAccessByCompanyId(vendorCompanyId)) {
					String mdfAmount = partnerAnalyticsDAO.getPartnerJourneyMdfAmount(partnerJourneyRequestDTO);
					partnerJourneyAnalytics.setMdfAmount(mdfAmount);
					partnerJourneyAnalytics.setShowMDFAmount(true);
				}
				if (utilDAO.hasDamAccessByCompanyId(vendorCompanyId)) {
					String assetCount = partnerAnalyticsDAO.getPartnerJourneyAssetCount(partnerJourneyRequestDTO);
					partnerJourneyAnalytics.setAssetCount(assetCount);
					partnerJourneyAnalytics.setShowAssetCount(true);
				}
				if (!utilDAO.isPrmByVendorCompanyId(vendorCompanyId)) {
					String contactCount = partnerAnalyticsDAO.getPartnerJourneyContactCount(partnerJourneyRequestDTO);
					partnerJourneyAnalytics.setContactCount(contactCount);
					partnerJourneyAnalytics.setShowContactCount(true);
				}
				if (utilDAO.hasLmsAccessByCompanyId(vendorCompanyId)
						|| utilDAO.hasPlaybookAccessByCompanyId(vendorCompanyId)) {
					PartnerJourneyAnalyticsDTO trackAndPlaybookCountDto = partnerAnalyticsDAO
							.getPartnerJourneyTrackAndPlaybookCount(partnerJourneyRequestDTO);
					if (trackAndPlaybookCountDto != null) {
						if (utilDAO.hasLmsAccessByCompanyId(vendorCompanyId)) {
							partnerJourneyAnalytics.setTrackCount(trackAndPlaybookCountDto.getTrackCount());
							partnerJourneyAnalytics.setShowTrackCount(true);
						}
						if (utilDAO.hasPlaybookAccessByCompanyId(vendorCompanyId)) {
							partnerJourneyAnalytics.setPlaybookCount(trackAndPlaybookCountDto.getPlaybookCount());
							partnerJourneyAnalytics.setShowPlaybookCount(true);
						}
					}

					if (utilDAO.hasLmsAccessByCompanyId(vendorCompanyId)) {
						String trackAssetCount = partnerAnalyticsDAO
								.getPartnerJourneyTrackAssetCount(partnerJourneyRequestDTO);
						partnerJourneyAnalytics.setTrackAssetCount(trackAssetCount);
						partnerJourneyAnalytics.setShowTrackAssetCount(true);
					}

				}
				response.setData(partnerJourneyAnalytics);
				response.setStatusCode(200);
			}
		}
		return response;
	}

	@Override
	public XtremandResponse getPartnerLeadToDealCounts(PartnerJourneyRequestDTO partnerJourneyRequestDTO) {
		XtremandResponse response = new XtremandResponse();
		response.setStatusCode(400);
		if (partnerJourneyRequestDTO.getPartnerCompanyId() != null && partnerJourneyRequestDTO.getPartnerCompanyId() > 0
				&& partnerJourneyRequestDTO.getLoggedInUserId() != null
				&& partnerJourneyRequestDTO.getLoggedInUserId() > 0) {
			Integer vendorCompanyId = userDao.getCompanyIdByUserId(partnerJourneyRequestDTO.getLoggedInUserId());
			partnerJourneyRequestDTO.setVendorCompanyId(vendorCompanyId);
			if (vendorCompanyId != null && vendorCompanyId > 0) {
				utilService.setDateFiltersForPartnerJourney(partnerJourneyRequestDTO);
				List<Object[]> list = partnerAnalyticsDAO.getPartnerLeadToDealCounts(partnerJourneyRequestDTO);
				if (list != null && !list.isEmpty()) {
					setDTOData(response, list);
				} else {
					setDataNotFound(response);
				}
			}
		}
		return response;
	}

	@Override
	public XtremandResponse getPartnerCampaignToLeadCounts(PartnerJourneyRequestDTO partnerJourneyRequestDTO) {
		XtremandResponse response = new XtremandResponse();
		response.setStatusCode(400);
		if (partnerJourneyRequestDTO.getPartnerCompanyId() != null && partnerJourneyRequestDTO.getPartnerCompanyId() > 0
				&& partnerJourneyRequestDTO.getLoggedInUserId() != null
				&& partnerJourneyRequestDTO.getLoggedInUserId() > 0) {
			Integer vendorCompanyId = userDao.getCompanyIdByUserId(partnerJourneyRequestDTO.getLoggedInUserId());
			partnerJourneyRequestDTO.setVendorCompanyId(vendorCompanyId);
			if (vendorCompanyId != null && vendorCompanyId > 0) {
				utilService.setDateFiltersForPartnerJourney(partnerJourneyRequestDTO);
				List<Object[]> list = partnerAnalyticsDAO.getPartnerCampaignToLeadCounts(partnerJourneyRequestDTO);
				if (list != null && !list.isEmpty()) {
					extractValuesandSetDTOData(response, list);
				} else {
					setDataNotFound(response);
				}
			}
		}
		return response;
	}

	@Override
	public XtremandResponse getPartnerJourneyTrackDetailsByInteraction(Pagination pagination) {
		XtremandResponse response = new XtremandResponse();
		response.setStatusCode(400);
		if (pagination.getUserId() != null && pagination.getUserId() > 0) {
			Integer vendorCompanyId = userDao.getCompanyIdByUserId(pagination.getUserId());
			pagination.setVendorCompanyId(vendorCompanyId);
			if (vendorCompanyId != null && vendorCompanyId > 0) {
				utilService.setDateFilters(pagination);
				response.setData(partnerAnalyticsDAO.getPartnerJourneyTrackDetailsByInteraction(pagination));
				response.setStatusCode(200);
			}
		}
		return response;
	}

	@Override
	public XtremandResponse getPartnerJourneyTrackAssetDetailsByType(Pagination pagination) {
		XtremandResponse response = new XtremandResponse();
		response.setStatusCode(400);
		if (pagination.getUserId() != null && pagination.getUserId() > 0) {
			Integer vendorCompanyId = userDao.getCompanyIdByUserId(pagination.getUserId());
			pagination.setVendorCompanyId(vendorCompanyId);
			if (vendorCompanyId != null && vendorCompanyId > 0) {
				utilService.setDateFilters(pagination);
				response.setData(partnerAnalyticsDAO.getPartnerJourneyTrackAssetDetailsByType(pagination));
				response.setStatusCode(200);
			}
		}
		return response;
	}

	@Override
	public XtremandResponse getPartnerJourneyTracksByUser(Pagination pagination) {
		XtremandResponse response = new XtremandResponse();
		response.setStatusCode(400);
		if (pagination.getUserId() != null && pagination.getUserId() > 0) {
			Integer vendorCompanyId = userDao.getCompanyIdByUserId(pagination.getUserId());
			pagination.setVendorCompanyId(vendorCompanyId);
			if (vendorCompanyId != null && vendorCompanyId > 0) {
				utilService.setDateFilters(pagination);
				response.setData(partnerAnalyticsDAO.getPartnerJourneyTracksByUser(pagination));
				response.setStatusCode(200);
			}
		}
		return response;
	}

	@Override
	public XtremandResponse getPartnerJourneyTrackDetailsByUser(Pagination pagination) {
		XtremandResponse response = new XtremandResponse();
		response.setStatusCode(400);
		if (pagination.getUserId() != null && pagination.getUserId() > 0) {
			Integer vendorCompanyId = userDao.getCompanyIdByUserId(pagination.getUserId());
			pagination.setVendorCompanyId(vendorCompanyId);
			if (vendorCompanyId != null && vendorCompanyId > 0) {
				utilService.setDateFilters(pagination);
				response.setData(partnerAnalyticsDAO.getPartnerJourneyTrackDetailsByUser(pagination));
				response.setStatusCode(200);
			}
		}
		return response;
	}

	@Override
	public XtremandResponse getPartnerJourneyTrackAssetDetails(Pagination pagination) {
		XtremandResponse response = new XtremandResponse();
		response.setStatusCode(400);
		if (pagination.getUserId() != null && pagination.getUserId() > 0) {
			Integer vendorCompanyId = userDao.getCompanyIdByUserId(pagination.getUserId());
			pagination.setVendorCompanyId(vendorCompanyId);
			if (vendorCompanyId != null && vendorCompanyId > 0) {
				utilService.setDateFilters(pagination);
				response.setData(partnerAnalyticsDAO.getPartnerJourneyTrackAssetDetails(pagination));
				response.setStatusCode(200);
			}
		}
		return response;
	}

	@Override
	public XtremandResponse getPartnerJourneyPlaybookAssetDetails(Pagination pagination) {
		XtremandResponse response = new XtremandResponse();
		response.setStatusCode(400);
		if (pagination.getUserId() != null && pagination.getUserId() > 0) {
			Integer vendorCompanyId = userDao.getCompanyIdByUserId(pagination.getUserId());
			pagination.setVendorCompanyId(vendorCompanyId);
			if (vendorCompanyId != null && vendorCompanyId > 0) {
				utilService.setDateFilters(pagination);
				response.setData(partnerAnalyticsDAO.getPartnerJourneyPlaybookAssetDetails(pagination));
				response.setStatusCode(200);
			}
		}
		return response;
	}

	@Override
	public XtremandResponse getPartnerJourneyShareLeadDetails(Pagination pagination) {
		XtremandResponse response = new XtremandResponse();
		response.setStatusCode(400);
		if (pagination.getUserId() != null && pagination.getUserId() > 0) {
			Integer vendorCompanyId = userDao.getCompanyIdByUserId(pagination.getUserId());
			pagination.setVendorCompanyId(vendorCompanyId);
			if (vendorCompanyId != null && vendorCompanyId > 0) {
				utilService.setDateFilters(pagination);
				response.setData(partnerAnalyticsDAO.getPartnerJourneyShareLeadDetails(pagination));
				response.setStatusCode(200);
			}
		}
		return response;
	}

	@Override
	public XtremandResponse getPartnerJourneyRedistributedCampaignDetails(Pagination pagination) {
		XtremandResponse response = new XtremandResponse();
		response.setStatusCode(400);
		if (pagination.getUserId() != null && pagination.getUserId() > 0) {
			Integer vendorCompanyId = userDao.getCompanyIdByUserId(pagination.getUserId());
			pagination.setVendorCompanyId(vendorCompanyId);
			if (vendorCompanyId != null && vendorCompanyId > 0) {
				utilService.setDateFilters(pagination);
				response.setData(partnerAnalyticsDAO.getPartnerJourneyRedistributedCampaignDetails(pagination));
				response.setStatusCode(200);
			}
		}
		return response;
	}

	@Override
	public XtremandResponse getPartnerJourneyLeadDetails(Pagination pagination) {
		XtremandResponse response = new XtremandResponse();
		response.setStatusCode(400);
		if (pagination.getUserId() != null && pagination.getUserId() > 0) {
			Integer vendorCompanyId = userDao.getCompanyIdByUserId(pagination.getUserId());
			pagination.setVendorCompanyId(vendorCompanyId);
			if (vendorCompanyId != null && vendorCompanyId > 0) {
				utilService.setDateFilters(pagination);
				response.setData(partnerAnalyticsDAO.getPartnerJourneyLeadDetails(pagination));
				response.setStatusCode(200);
			}
		}
		return response;
	}

	@Override
	public XtremandResponse getPartnerJourneyDealDetails(Pagination pagination) {
		XtremandResponse response = new XtremandResponse();
		response.setStatusCode(400);
		if (pagination.getUserId() != null && pagination.getUserId() > 0) {
			Integer vendorCompanyId = userDao.getCompanyIdByUserId(pagination.getUserId());
			pagination.setVendorCompanyId(vendorCompanyId);
			if (vendorCompanyId != null && vendorCompanyId > 0) {
				utilService.setDateFilters(pagination);
				response.setData(partnerAnalyticsDAO.getPartnerJourneyDealDetails(pagination));
				response.setStatusCode(200);
			}
		}
		return response;
	}

	@Override
	public XtremandResponse getPartnerJourneyContactDetails(Pagination pagination) {
		XtremandResponse response = new XtremandResponse();
		response.setStatusCode(400);
		if (pagination.getPartnerCompanyId() != null && pagination.getPartnerCompanyId() > 0
				&& pagination.getUserId() != null && pagination.getUserId() > 0) {
			Integer vendorCompanyId = userDao.getCompanyIdByUserId(pagination.getUserId());
			pagination.setVendorCompanyId(vendorCompanyId);
			if (vendorCompanyId != null && vendorCompanyId > 0) {
				response.setData(partnerAnalyticsDAO.getPartnerJourneyContactDetails(pagination));
				response.setStatusCode(200);
			}
		}
		return response;
	}

	@Override
	public XtremandResponse getPartnerJourneyMdfDetails(Pagination pagination) {
		XtremandResponse response = new XtremandResponse();
		response.setStatusCode(400);
		if (pagination.getUserId() != null && pagination.getUserId() > 0) {
			Integer vendorCompanyId = userDao.getCompanyIdByUserId(pagination.getUserId());
			pagination.setVendorCompanyId(vendorCompanyId);
			if (vendorCompanyId != null && vendorCompanyId > 0) {
				utilService.setDateFilters(pagination);
				response.setData(partnerAnalyticsDAO.getPartnerJourneyMdfDetails(pagination));
				response.setStatusCode(200);
			}
		}
		return response;
	}

	@Override
	public XtremandResponse getPartnerJourneyTrackCountsByInteraction(
			PartnerJourneyRequestDTO partnerJourneyRequestDTO) {
		XtremandResponse response = new XtremandResponse();
		response.setStatusCode(400);
		if (partnerJourneyRequestDTO.getLoggedInUserId() != null && partnerJourneyRequestDTO.getLoggedInUserId() > 0) {
			Integer vendorCompanyId = userDao.getCompanyIdByUserId(partnerJourneyRequestDTO.getLoggedInUserId());
			partnerJourneyRequestDTO.setVendorCompanyId(vendorCompanyId);
			if (vendorCompanyId != null && vendorCompanyId > 0) {
				utilService.setDateFiltersForPartnerJourney(partnerJourneyRequestDTO);
				List<List<Object>> trackCounts = new ArrayList<>();
				PartnerJourneyTrackDetailsDTO partnerJourneyTrackDetailsDTO = partnerAnalyticsDAO
						.getPartnerJourneyTrackCountsByInteraction(partnerJourneyRequestDTO);
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
	public XtremandResponse getPartnerJourneyTrackCountsByType(PartnerJourneyRequestDTO partnerJourneyRequestDTO) {
		XtremandResponse response = new XtremandResponse();
		response.setStatusCode(400);
		if (partnerJourneyRequestDTO.getLoggedInUserId() != null && partnerJourneyRequestDTO.getLoggedInUserId() > 0) {
			Integer vendorCompanyId = userDao.getCompanyIdByUserId(partnerJourneyRequestDTO.getLoggedInUserId());
			partnerJourneyRequestDTO.setVendorCompanyId(vendorCompanyId);
			if (vendorCompanyId != null && vendorCompanyId > 0) {
				utilService.setDateFiltersForPartnerJourney(partnerJourneyRequestDTO);
				List<List<Object>> trackCounts = new ArrayList<List<Object>>();
				PartnerJourneyTrackDetailsDTO partnerJourneyTrackDetailsDTO = partnerAnalyticsDAO
						.getPartnerJourneyTrackCountsByType(partnerJourneyRequestDTO);
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
	public XtremandResponse getPartnerJourneyTeamEmails(PartnerJourneyRequestDTO partnerJourneyRequestDTO) {
		XtremandResponse response = new XtremandResponse();
		response.setStatusCode(400);
		if (partnerJourneyRequestDTO.getPartnerCompanyId() != null && partnerJourneyRequestDTO.getPartnerCompanyId() > 0
				&& partnerJourneyRequestDTO.getLoggedInUserId() != null
				&& partnerJourneyRequestDTO.getLoggedInUserId() > 0) {
			Integer vendorCompanyId = userDao.getCompanyIdByUserId(partnerJourneyRequestDTO.getLoggedInUserId());
			partnerJourneyRequestDTO.setVendorCompanyId(vendorCompanyId);
			if (vendorCompanyId != null && vendorCompanyId > 0) {
				utilService.setDateFiltersForPartnerJourney(partnerJourneyRequestDTO);
				List<TeamMemberDTO> list = partnerAnalyticsDAO.getPartnerJourneyTeamEmails(partnerJourneyRequestDTO);
				response.setStatusCode(200);
				response.setData(list);
			}
		}
		return response;
	}

	@Override
	public XtremandResponse getPartnerJourneyPlaybooksByUser(Pagination pagination) {
		XtremandResponse response = new XtremandResponse();
		response.setStatusCode(400);
		if (pagination.getUserId() != null && pagination.getUserId() > 0) {
			Integer vendorCompanyId = userDao.getCompanyIdByUserId(pagination.getUserId());
			pagination.setVendorCompanyId(vendorCompanyId);
			if (vendorCompanyId != null && vendorCompanyId > 0) {
				utilService.setDateFilters(pagination);
				response.setData(partnerAnalyticsDAO.getPartnerJourneyPlaybooksByUser(pagination));
				response.setStatusCode(200);
			}
		}
		return response;
	}

	@Override
	public XtremandResponse getPartnerJourneyCompanyInfoForFilter(Pagination pagination) {
		XtremandResponse response = new XtremandResponse();
		response.setStatusCode(400);
		if (pagination.getUserId() != null && pagination.getUserId() > 0) {
			Integer vendorCompanyId = userDao.getCompanyIdByUserId(pagination.getUserId());
			if (vendorCompanyId != null && vendorCompanyId > 0) {
				response.setData(partnerAnalyticsDAO.getPartnerJourneyCompanyInfoForFilter(pagination));
				response.setStatusCode(200);
			}
		}
		return response;
	}

	@Override
	public XtremandResponse getAllPartnersRegionNamesForFilter(Pagination pagination) {
		XtremandResponse response = new XtremandResponse();
		response.setStatusCode(400);
		if (pagination.getUserId() != null && pagination.getUserId() > 0) {
			Integer vendorCompanyId = userDao.getCompanyIdByUserId(pagination.getUserId());
			if (vendorCompanyId != null && vendorCompanyId > 0) {
				response.setData(partnerAnalyticsDAO.getAllPartnersRegionNamesForFilter(pagination));
				response.setStatusCode(200);
			}
		}
		return response;
	}

	@Override
	public Map<String, Object> findPendingSignupAndCompanyProfileIncompletePartnersCount(Integer userId,
			boolean applyFilter) {
		Map<String, Object> map = new HashMap<>();
		Integer companyId = userDao.getCompanyIdByUserId(userId);
		TeamMemberFilterDTO teamMemberFilterDTO = utilDAO.applyFilterConditions(userId, applyFilter, false);
		boolean applyTeamMemberFilter = teamMemberFilterDTO.isApplyTeamMemberFilter()
				|| teamMemberFilterDTO.isEmptyFilter();
		map.put("PendingSignupPartnersCount",
				partnerAnalyticsDAO.getPendingSignupAndCompanyProfileIncompletePartnersCount(companyId,
						applyTeamMemberFilter, userId, XamplifyConstants.PARTNER_SIGNUP_URL_PREFIX));
		map.put("CompanyProfileIncompletePartnersCount",
				partnerAnalyticsDAO.getPendingSignupAndCompanyProfileIncompletePartnersCount(companyId,
						applyTeamMemberFilter, userId, XamplifyConstants.INCOMPLETE_COMPANY_PROFILE));
		return map;
	}

	@Override
	public Map<String, Object> getPendingSignupAndCompanyProfileIncompletePartnersCount(Pagination pagination) {
		Integer companyId = userDao.getCompanyIdByUserId(pagination.getUserId());
		return partnerAnalyticsDAO.getPendingSignupAndCompanyProfileIncompletePartners(companyId, pagination);
	}

	@Override
	public XtremandResponse sendSingupIncompleteCompanyprofilEmail(Pagination pagination) {

		pagination.setUserListId(partnerAnalyticsDAO.getDefaultPartnerListidByUserid(pagination.getUserId()));

		XtremandResponse xtremandResponse = new XtremandResponse();
		try {
			String responseMessage = null;

			if (pagination.isSingleMail()) {

				xtremandResponse = userListService.sendPartnerMail(pagination);

			} else if (pagination.getSelectedPartnerIds().length > 0) {
				for (int userId : pagination.getSelectedPartnerIds()) {
					pagination.setPartnerId(userId);
					xtremandResponse = userListService.sendPartnerMail(pagination);
					if (xtremandResponse.getStatusCode() != 400) {
						responseMessage = xtremandResponse.getMessage();
					}
					if (xtremandResponse.getStatusCode() == 400) {
						if (responseMessage != null) {
							xtremandResponse.setMessage(responseMessage + " and " + xtremandResponse.getMessage());
						} else {
							xtremandResponse.setMessage(xtremandResponse.getMessage());
						}
					}
				}
			} else {
				xtremandResponse = userListService.sendPartnerMail(pagination);
			}
		} catch (Exception e) {
			e.printStackTrace();
			xtremandResponse.setAccess(false);
		}

		return xtremandResponse;
	}

	@SuppressWarnings("unchecked")
	@Override
	public HttpServletResponse downloadPartnerAnalyticsTeamMembers(Pagination pagination,
			HttpServletResponse response) {
		XtremandResponse teamResponse = new XtremandResponse();
		pagination.setPageIndex(1);
		pagination.setMaxResults(pagination.getTotalRecords());
		List<String[]> data = new ArrayList<>();
		try {
			data.add(new String[] { "COMPANY NAME", "NAME", "EMAIL ID", "STATUS", "RECENT LOGIN", "GROUP" });
			teamResponse = getPartnerJourneyTeamInfo(pagination);
			Map<String, Object> result = (Map<String, Object>) teamResponse.getData();
			List<TeamMemberListDTO> list = (List<TeamMemberListDTO>) result.get("list");

			for (TeamMemberListDTO finalResult : list) {
				String fullName = (Objects.toString(finalResult.getFirstName(), "") + " "
						+ Objects.toString(finalResult.getLastName(), "")).trim();
				String groupName = (finalResult.getTeamMemberGroupName() == null
						|| finalResult.getTeamMemberGroupName().isEmpty()) ? "NA"
								: finalResult.getTeamMemberGroupName();
				String status = (finalResult.getStatus() != null) ? finalResult.getStatus().trim().toUpperCase() : "";

				String recentLogin = ("UNAPPROVED".equals(status) || finalResult.getLogInTime() == null) ? "-"
						: DateUtils.dateToString(finalResult.getLogInTime());
				data.add(new String[] { finalResult.getCompanyName(), fullName, finalResult.getEmailId(),
						finalResult.getStatus(), recentLogin, groupName });
			}

			return XamplifyUtils.generateCSV("Team_Members_List", response, data);

		} catch (IOException e) {
			e.printStackTrace();
		}
		return response;

	}

	@SuppressWarnings("unchecked")
	@Override
	public HttpServletResponse downloadInteractedAndNonInteractedTracksReport(
			PartnerJourneyRequestDTO partnerJourneyRequestDTO, HttpServletResponse response) {
		String fileName = "Interacted-And-Not-Interacted-Track-Details-Report";
		Integer userId = partnerJourneyRequestDTO.getLoggedInUserId();
		Integer vendorCompanyId = userDao.getCompanyIdByUserId(partnerJourneyRequestDTO.getLoggedInUserId());
		Pagination pagination = new Pagination();
		framePaginationValues(partnerJourneyRequestDTO, userId, vendorCompanyId, pagination);
		if (XamplifyUtils.isValidInteger(vendorCompanyId)) {
			utilService.setDateFilters(pagination);
			Map<String, Object> result = partnerAnalyticsDAO.getPartnerJourneyTrackDetailsByInteraction(pagination);
			if (result != null) {
				List<PartnerJourneyTrackDetailsDTO> trackData = (List<PartnerJourneyTrackDetailsDTO>) result
						.get("list");
				return frameCSVDataForInteractedAndNonInteractedTracksReport(response, fileName, pagination, trackData);
			}
		}
		return response;
	}

	private HttpServletResponse frameCSVDataForInteractedAndNonInteractedTracksReport(HttpServletResponse response,
			String fileName, Pagination pagination, List<PartnerJourneyTrackDetailsDTO> trackData) {
		try {
			List<String[]> row = new ArrayList<>();
			List<String> headerList = new ArrayList<>();
			if (pagination.isDetailedAnalytics()) {
				headerList.add(TEAM_MEMBER_NAME);
				headerList.add(TEAM_MEMBER_EMAIL_ID);
			} else {
				headerList.add(PARTNER_COMPANY);
			}
			headerList.add(TRACK_TITLE);
			headerList.add("TRACK PROGRESS");
			row.add(headerList.toArray(new String[0]));
			for (PartnerJourneyTrackDetailsDTO track : trackData) {
				List<String> dataList = new ArrayList<>();
				String fullName = getFullName(track.getFirstName(), track.getLastName());
				if (pagination.isDetailedAnalytics()) {
					dataList.add(fullName);
					dataList.add(track.getEmailId());
				} else {
					dataList.add(track.getCompanyName());
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

	@SuppressWarnings("unchecked")
	@Override
	public HttpServletResponse downloadTypeWiseTrackContentReport(PartnerJourneyRequestDTO partnerJourneyRequestDTO,
			HttpServletResponse response) {
		String fileName = "Track-Asset-Details-Report";
		Integer userId = partnerJourneyRequestDTO.getLoggedInUserId();
		Integer vendorCompanyId = userDao.getCompanyIdByUserId(partnerJourneyRequestDTO.getLoggedInUserId());
		Pagination pagination = new Pagination();
		pagination.setExcludeLimit(true);
		framePaginationValues(partnerJourneyRequestDTO, userId, vendorCompanyId, pagination);
		if (XamplifyUtils.isValidInteger(vendorCompanyId)) {
			utilService.setDateFilters(pagination);
			Map<String, Object> result = partnerAnalyticsDAO.getPartnerJourneyTrackAssetDetailsByType(pagination);
			if (result != null) {
				List<PartnerJourneyTrackDetailsDTO> trackData = (List<PartnerJourneyTrackDetailsDTO>) result
						.get("list");
				return frameCSVDataForTypeWiseTrackContentReport(response, fileName, pagination, trackData);
			}
		}
		return response;
	}

	private HttpServletResponse frameCSVDataForTypeWiseTrackContentReport(HttpServletResponse response, String fileName,
			Pagination pagination, List<PartnerJourneyTrackDetailsDTO> trackData) {
		try {
			List<String[]> row = new ArrayList<>();
			List<String> headerList = new ArrayList<>();
			if (pagination.isDetailedAnalytics()) {
				headerList.add(TEAM_MEMBER_NAME);
				headerList.add(TEAM_MEMBER_EMAIL_ID);
			} else {
				headerList.add(PARTNER_COMPANY);
			}
			headerList.add("ASSET NAME");
			headerList.add("ASSET TYPE");
			headerList.add(TRACK_TITLE);
			headerList.add("STATUS");
			row.add(headerList.toArray(new String[0]));
			for (PartnerJourneyTrackDetailsDTO track : trackData) {
				List<String> dataList = new ArrayList<>();
				String fullName = getFullName(track.getFirstName(), track.getLastName());
				if (pagination.isDetailedAnalytics()) {
					dataList.add(fullName);
					dataList.add(track.getEmailId());
				} else {
					dataList.add(track.getCompanyName());
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

	private void framePaginationValues(PartnerJourneyRequestDTO partnerJourneyRequestDTO, Integer userId,
			Integer vendorCompanyId, Pagination pagination) {
		pagination.setCompanyId(vendorCompanyId);
		pagination.setVendorCompanyId(vendorCompanyId);
		pagination.setUserId(userId);
		pagination.setTrackTypeFilter(partnerJourneyRequestDTO.getTrackTypeFilter());
		pagination.setSelectedPartnerCompanyIds(partnerJourneyRequestDTO.getSelectedPartnerCompanyIds());
		pagination.setSearchKey(partnerJourneyRequestDTO.getSearchKey());
		pagination.setAssetTypeFilter(partnerJourneyRequestDTO.getAssetType());
		pagination.setCampaignTypeFilter(partnerJourneyRequestDTO.getCampaignTypeFilter());
		pagination.setDetailedAnalytics(partnerJourneyRequestDTO.isDetailedAnalytics());
		pagination.setPartnerCompanyId(partnerJourneyRequestDTO.getPartnerCompanyId());
		pagination.setPartnerTeamMemberGroupFilter(partnerJourneyRequestDTO.isPartnerTeamMemberGroupFilter());
		pagination.setTeamMemberId(partnerJourneyRequestDTO.getTeamMemberUserId());
		pagination.setFromDateFilterString(partnerJourneyRequestDTO.getFromDateFilterInString());
		pagination.setToDateFilterString(partnerJourneyRequestDTO.getToDateFilterInString());
		pagination.setTimeZone(partnerJourneyRequestDTO.getTimeZone());
		pagination.setPageIndex(partnerJourneyRequestDTO.getPageNumber());
		pagination.setMaxResults(partnerJourneyRequestDTO.getLimit());
		pagination.setSortcolumn(partnerJourneyRequestDTO.getSortcolumn());
		pagination.setSortingOrder(partnerJourneyRequestDTO.getSortingOrder());
		pagination.setModuleName(partnerJourneyRequestDTO.getModuleName());
		pagination.setSelectedAssetNames(partnerJourneyRequestDTO.getAssetNames());
		pagination.setSelectedCompanyIds(partnerJourneyRequestDTO.getCompanyIds());
		pagination.setSelectedEmailIds(partnerJourneyRequestDTO.getEmailIds());
		// pagination.setTeamMemberId(partnerJourneyRequestDTO.getLoggedInUserId());
		// pagination.setSelectedPlaybookNames(partnerJourneyRequestDTO.getPlaybookNames());
		pagination.setPartnershipStatus(partnerJourneyRequestDTO.getPartnershipStatus());
	}

	@SuppressWarnings("unchecked")
	@Override
	public HttpServletResponse downloadUserWiseTrackCountReport(PartnerJourneyRequestDTO partnerJourneyRequestDTO,
			HttpServletResponse response) {
		String fileName = "Track-Counts-Report";
		String lmsType = "TRACK";
		Integer userId = partnerJourneyRequestDTO.getLoggedInUserId();
		Integer vendorCompanyId = userDao.getCompanyIdByUserId(partnerJourneyRequestDTO.getLoggedInUserId());
		Pagination pagination = new Pagination();
		pagination.setExcludeLimit(true);
		framePaginationValues(partnerJourneyRequestDTO, userId, vendorCompanyId, pagination);
		if (XamplifyUtils.isValidInteger(vendorCompanyId)) {
			utilService.setDateFilters(pagination);
			Map<String, Object> result = partnerAnalyticsDAO.getPartnerJourneyTracksByUser(pagination);
			if (result != null) {
				List<TeamMemberDTO> data = (List<TeamMemberDTO>) result.get("list");
				return frameUserWiseTracksAndPlaybookCountData(response, fileName, pagination, data, lmsType);
			}
		}
		return response;
	}

	private HttpServletResponse frameUserWiseTracksAndPlaybookCountData(HttpServletResponse response, String fileName,
			Pagination pagination, List<TeamMemberDTO> data, String lmsType) {
		try {
			List<String[]> row = new ArrayList<>();
			List<String> headerList = new ArrayList<>();
			if (pagination.isDetailedAnalytics()) {
				headerList.add(TEAM_MEMBER_NAME);
				headerList.add(TEAM_MEMBER_EMAIL_ID);
			} else {
				headerList.add(PARTNER_COMPANY);
			}
			headerList.add(lmsType + " COUNT");
			row.add(headerList.toArray(new String[0]));
			for (TeamMemberDTO track : data) {
				List<String> dataList = new ArrayList<>();
				String fullName = getFullName(track.getFirstName(), track.getLastName());
				if (pagination.isDetailedAnalytics()) {
					dataList.add(fullName);
					dataList.add(track.getEmailId());
				} else {
					dataList.add(track.getCompanyName());
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

	private String getFullName(String firstName, String lastName) {
		String fullName = "";
		if (XamplifyUtils.isValidString(firstName) || XamplifyUtils.isValidString(lastName)) {
			fullName = firstName + " " + lastName;
		}
		return fullName;
	}

	@SuppressWarnings("unchecked")
	@Override
	public HttpServletResponse downloadUserWisePlayBookCountReport(PartnerJourneyRequestDTO partnerJourneyRequestDTO,
			HttpServletResponse response) {
		String fileName = "Playbook-Counts-Report";
		String lmsType = "PLAYBOOK";
		Integer userId = partnerJourneyRequestDTO.getLoggedInUserId();
		Integer vendorCompanyId = userDao.getCompanyIdByUserId(partnerJourneyRequestDTO.getLoggedInUserId());
		Pagination pagination = new Pagination();
		pagination.setExcludeLimit(true);
		framePaginationValues(partnerJourneyRequestDTO, userId, vendorCompanyId, pagination);
		if (XamplifyUtils.isValidInteger(vendorCompanyId)) {
			utilService.setDateFilters(pagination);
			Map<String, Object> result = partnerAnalyticsDAO.getPartnerJourneyPlaybooksByUser(pagination);
			if (result != null) {
				List<TeamMemberDTO> data = (List<TeamMemberDTO>) result.get("list");
				return frameUserWiseTracksAndPlaybookCountData(response, fileName, pagination, data, lmsType);
			}
		}
		return response;
	}

	@SuppressWarnings("unchecked")
	@Override
	public HttpServletResponse downloadTrackAssetsDetailedReport(PartnerJourneyRequestDTO partnerJourneyRequestDTO,
			HttpServletResponse response) {
		String fileName = "Track-Assets-Detailed-Report";
		Integer userId = partnerJourneyRequestDTO.getLoggedInUserId();
		Integer vendorCompanyId = userDao.getCompanyIdByUserId(partnerJourneyRequestDTO.getLoggedInUserId());
		Pagination pagination = new Pagination();
		pagination.setExcludeLimit(true);
		framePaginationValues(partnerJourneyRequestDTO, userId, vendorCompanyId, pagination);
		if (XamplifyUtils.isValidInteger(vendorCompanyId)) {
			utilService.setDateFilters(pagination);
			Map<String, Object> result = partnerAnalyticsDAO.getPartnerJourneyTrackDetailsByUser(pagination);
			if (result != null) {
				List<PartnerJourneyTrackDetailsDTO> trackData = (List<PartnerJourneyTrackDetailsDTO>) result
						.get("list");
				return frameCSVDataForTrackAssetsDetailedReport(response, fileName, pagination, trackData);
			}
		}
		return response;
	}

	private HttpServletResponse frameCSVDataForTrackAssetsDetailedReport(HttpServletResponse response, String fileName,
			Pagination pagination, List<PartnerJourneyTrackDetailsDTO> trackData) {
		try {
			List<String[]> row = new ArrayList<>();
			List<String> headerList = new ArrayList<>();
			headerList.add(PARTNER_COMPANY);
			if (pagination.isDetailedAnalytics()) {
				headerList.add(TEAM_MEMBER_EMAIL_ID);
			}
			headerList.add(TRACK_TITLE);
			headerList.add("ASSETS COUNT");
			headerList.add("PUBLISHED TIME(PST)");
			headerList.add("OPENED COUNT");
			headerList.add("VIEWED COUNT");
			headerList.add("DOWNLOADED COUNT");
			headerList.add("AVERAGE PROGRESS");
			headerList.add("QUIZ COUNT");
			headerList.add("SCORE");
			row.add(headerList.toArray(new String[0]));
			for (PartnerJourneyTrackDetailsDTO track : trackData) {
				List<String> dataList = new ArrayList<>();
				String publishedDate = checkIfDateIsNull(track.getPublishedOn());
				String quizCount = track.getQuizCount().toString();
				String score = "0".equals(quizCount) ? "Quiz Not Available" : track.getScore();
				dataList.add(track.getCompanyName());
				if (pagination.isDetailedAnalytics()) {
					dataList.add(track.getEmailId());
				}
				dataList.add(track.getTitle());
				dataList.add(track.getAssetCount().toString());
				dataList.add(publishedDate);
				dataList.add(track.getOpenedCount().toString());
				dataList.add(track.getViewedCount().toString());
				dataList.add(track.getDownloadedCount().toString());
				dataList.add(track.getProgress().toString() + "%");
				dataList.add(quizCount);
				dataList.add(score);
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
	public HttpServletResponse downloadPlayBookAssetsDetailsReport(PartnerJourneyRequestDTO partnerJourneyRequestDTO,
			HttpServletResponse response) {
		String fileName = "Playbook-Asset-Details-Report";
		String lmsType = "PlayBook";
		Integer userId = partnerJourneyRequestDTO.getLoggedInUserId();
		Integer vendorCompanyId = userDao.getCompanyIdByUserId(partnerJourneyRequestDTO.getLoggedInUserId());
		Pagination pagination = new Pagination();
		pagination.setExcludeLimit(true);
		framePaginationValues(partnerJourneyRequestDTO, userId, vendorCompanyId, pagination);
		if (XamplifyUtils.isValidInteger(vendorCompanyId)) {
			utilService.setDateFilters(pagination);
			Map<String, Object> result = partnerAnalyticsDAO.getPartnerJourneyPlaybookAssetDetails(pagination);
			if (result != null) {
				List<PartnerJourneyTrackDetailsDTO> trackAssetsData = (List<PartnerJourneyTrackDetailsDTO>) result
						.get("list");
				return frameCSVData(response, fileName, trackAssetsData, lmsType);
			}
		}
		return response;
	}

	@SuppressWarnings("unchecked")
	@Override
	public HttpServletResponse downloadTrackAssetsDetailsReport(PartnerJourneyRequestDTO partnerJourneyRequestDTO,
			HttpServletResponse response) {
		String fileName = "Track-Asset-Details-Report";
		String lmsType = "Track";
		Integer userId = partnerJourneyRequestDTO.getLoggedInUserId();
		Integer vendorCompanyId = userDao.getCompanyIdByUserId(partnerJourneyRequestDTO.getLoggedInUserId());
		Pagination pagination = new Pagination();
		pagination.setExcludeLimit(true);
		framePaginationValues(partnerJourneyRequestDTO, userId, vendorCompanyId, pagination);
		if (XamplifyUtils.isValidInteger(vendorCompanyId)) {
			utilService.setDateFilters(pagination);
			Map<String, Object> result = partnerAnalyticsDAO.getPartnerJourneyTrackAssetDetails(pagination);
			if (result != null) {
				List<PartnerJourneyTrackDetailsDTO> trackAssetsData = (List<PartnerJourneyTrackDetailsDTO>) result
						.get("list");
				return frameCSVData(response, fileName, trackAssetsData, lmsType);
			}
		}
		return response;
	}

	private HttpServletResponse frameCSVData(HttpServletResponse response, String fileName,
			List<PartnerJourneyTrackDetailsDTO> trackData, String lmsType) {
		try {
			String createdDate;
			List<String[]> data = new ArrayList<>();
			data.add(new String[] { lmsType + " TITLE ", " ASSET NAME ", "ASSET TYPE", "ASSET CREATED TIME(PST)",
					"OPENED COUNT" });
			for (PartnerJourneyTrackDetailsDTO track : trackData) {
				createdDate = checkIfDateIsNull(track.getAssetCreatedTime());
				data.add(new String[] { track.getTitle(), track.getAssetName(), track.getAssetType(), createdDate,
						track.getOpenedCount().toString() });
			}
			return XamplifyUtils.generateCSV(fileName, response, data);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return response;
	}

	@SuppressWarnings("unchecked")
	@Override
	public HttpServletResponse downloadShareLeadsDetailsReport(PartnerJourneyRequestDTO partnerJourneyRequestDTO,
			HttpServletResponse response) {
		String fileName = "Share-Leads-Details-Report";
		Integer userId = partnerJourneyRequestDTO.getLoggedInUserId();
		Integer vendorCompanyId = userDao.getCompanyIdByUserId(partnerJourneyRequestDTO.getLoggedInUserId());
		Pagination pagination = new Pagination();
		pagination.setExcludeLimit(true);
		framePaginationValues(partnerJourneyRequestDTO, userId, vendorCompanyId, pagination);
		if (XamplifyUtils.isValidInteger(vendorCompanyId)) {
			utilService.setDateFilters(pagination);
			Map<String, Object> result = partnerAnalyticsDAO.getPartnerJourneyShareLeadDetails(pagination);
			if (result != null) {
				List<ShareLeadsDTO> shareLeadData = (List<ShareLeadsDTO>) result.get("list");
				return frameCSVDataForShareLeadsDetailsReport(response, fileName, pagination, shareLeadData);
			}
		}
		return response;
	}

	private HttpServletResponse frameCSVDataForShareLeadsDetailsReport(HttpServletResponse response, String fileName,
			Pagination pagination, List<ShareLeadsDTO> shareLeadData) {
		try {
			List<String[]> row = new ArrayList<>();
			List<String> headerList = new ArrayList<>();
			if (pagination.isDetailedAnalytics()) {
				headerList.add(TEAM_MEMBER_NAME);
				headerList.add(TEAM_MEMBER_EMAIL_ID);
			} else {
				headerList.add(PARTNER_COMPANY);
			}
			headerList.add("LIST NAME");
			headerList.add("CREATED ON(PST)");
			headerList.add("ASSIGNED ON(PST)");
			headerList.add("SHARE LEADS COUNT");
			row.add(headerList.toArray(new String[0]));
			for (ShareLeadsDTO shareLead : shareLeadData) {
				List<String> dataList = new ArrayList<>();
				String fullName = getFullName(shareLead.getFirstName(), shareLead.getLastName());
				String createdDate = checkIfDateIsNull(shareLead.getCreatedTime());
				String assignedDate = checkIfDateIsNull(shareLead.getAssignedDate());
				if (pagination.isDetailedAnalytics()) {
					dataList.add(fullName);
					dataList.add(shareLead.getEmailId());
				} else {
					dataList.add(shareLead.getCompanyName());
				}
				dataList.add(shareLead.getListName());
				dataList.add(createdDate);
				dataList.add(assignedDate);
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
	public HttpServletResponse downloadLeadsDetailsReport(PartnerJourneyRequestDTO partnerJourneyRequestDTO,
			HttpServletResponse response) {
		String fileName = "Lead-Details-Report";
		Integer userId = partnerJourneyRequestDTO.getLoggedInUserId();
		Integer vendorCompanyId = userDao.getCompanyIdByUserId(partnerJourneyRequestDTO.getLoggedInUserId());
		Pagination pagination = new Pagination();
		pagination.setExcludeLimit(true);
		framePaginationValues(partnerJourneyRequestDTO, userId, vendorCompanyId, pagination);
		if (XamplifyUtils.isValidInteger(vendorCompanyId)) {
			utilService.setDateFilters(pagination);
			Map<String, Object> result = partnerAnalyticsDAO.getPartnerJourneyLeadDetails(pagination);
			if (result != null) {
				List<LeadDto> leadData = (List<LeadDto>) result.get("list");
				return frameCSVDataForLeadsDetailsReport(response, fileName, pagination, leadData);
			}
		}
		return response;
	}

	private HttpServletResponse frameCSVDataForLeadsDetailsReport(HttpServletResponse response, String fileName,
			Pagination pagination, List<LeadDto> leadData) {
		try {
			List<String[]> row = new ArrayList<>();
			List<String> headerList = new ArrayList<>();
			if (pagination.isDetailedAnalytics()) {
				headerList.add(TEAM_MEMBER_NAME);
				headerList.add(TEAM_MEMBER_EMAIL_ID);
			} else {
				headerList.add(PARTNER_COMPANY);
			}
			headerList.add("LEAD NAME");
			headerList.add("LEAD EMAIL ID");
			headerList.add("LEAD COMPANY");
			headerList.add("LEAD PHONE");
			headerList.add("LEAD STAGE");
			headerList.add("LEAD CREATED ON(PST)");
			row.add(headerList.toArray(new String[0]));
			for (LeadDto lead : leadData) {
				List<String> dataList = new ArrayList<>();
				String createdDate = checkIfDateIsNull(lead.getCreatedDate());
				if (pagination.isDetailedAnalytics()) {
					dataList.add(lead.getCreatedByName());
					dataList.add(lead.getCreatedByEmail());
				} else {
					dataList.add(lead.getCreatedByCompanyName());
				}
				dataList.add(lead.getFullName());
				dataList.add(lead.getEmail());
				dataList.add(lead.getCompany());
				dataList.add(lead.getPhone());
				dataList.add(lead.getCurrentStageName());
				dataList.add(createdDate);
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
	public HttpServletResponse downloadDealsDetailsReport(PartnerJourneyRequestDTO partnerJourneyRequestDTO,
			HttpServletResponse response) {
		String fileName = "Deal-Details-Report";
		Integer userId = partnerJourneyRequestDTO.getLoggedInUserId();
		Integer vendorCompanyId = userDao.getCompanyIdByUserId(partnerJourneyRequestDTO.getLoggedInUserId());
		Pagination pagination = new Pagination();
		pagination.setExcludeLimit(true);
		framePaginationValues(partnerJourneyRequestDTO, userId, vendorCompanyId, pagination);
		if (XamplifyUtils.isValidInteger(vendorCompanyId)) {
			utilService.setDateFilters(pagination);
			Map<String, Object> result = partnerAnalyticsDAO.getPartnerJourneyDealDetails(pagination);
			if (result != null) {
				List<DealDto> dealData = (List<DealDto>) result.get("list");
				return frameCSVDataForDealDetailsReport(response, fileName, pagination, dealData);
			}
		}
		return response;
	}

	private HttpServletResponse frameCSVDataForDealDetailsReport(HttpServletResponse response, String fileName,
			Pagination pagination, List<DealDto> dealData) {
		try {
			List<String[]> row = new ArrayList<>();
			List<String> headerList = new ArrayList<>();
			if (pagination.isDetailedAnalytics()) {
				headerList.add(TEAM_MEMBER_NAME);
				headerList.add(TEAM_MEMBER_EMAIL_ID);
			} else {
				headerList.add(PARTNER_COMPANY);
			}
			headerList.add("DEAL TITLE");
			headerList.add("DEAL TYPE");
			headerList.add("DEAL AMOUNT");
			headerList.add("DEAL CREATED ON(PST)");
			row.add(headerList.toArray(new String[0]));
			for (DealDto deal : dealData) {
				List<String> dataList = new ArrayList<>();
				String createdDate = checkIfDateIsNull(deal.getCreatedDate());
				String dealAmount = deal.getAmount() != null ? deal.getAmount().toString() : "";
				if (pagination.isDetailedAnalytics()) {
					dataList.add(deal.getCreatedByName());
					dataList.add(deal.getCreatedByEmail());
				} else {
					dataList.add(deal.getCreatedByCompanyName());
				}
				dataList.add(deal.getTitle());
				dataList.add(deal.getDealType());
				dataList.add("$ " + dealAmount);
				dataList.add(createdDate);
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
	public HttpServletResponse downloadMDFDetailsReport(PartnerJourneyRequestDTO partnerJourneyRequestDTO,
			HttpServletResponse response) {
		String fileName = "MDF-Details-Report";
		Integer userId = partnerJourneyRequestDTO.getLoggedInUserId();
		Integer vendorCompanyId = userDao.getCompanyIdByUserId(partnerJourneyRequestDTO.getLoggedInUserId());
		Pagination pagination = new Pagination();
		pagination.setExcludeLimit(true);
		framePaginationValues(partnerJourneyRequestDTO, userId, vendorCompanyId, pagination);
		if (XamplifyUtils.isValidInteger(vendorCompanyId)) {
			utilService.setDateFilters(pagination);
			Map<String, Object> result = partnerAnalyticsDAO.getPartnerJourneyMdfDetails(pagination);
			if (result != null) {
				List<MdfAmountTilesDTO> mdfData = (List<MdfAmountTilesDTO>) result.get("list");
				try {
					List<String[]> data = new ArrayList<>();
					data.add(new String[] { PARTNER_COMPANY, "TOTAL BALANCE", "USED BALANCE", "AVAILABLE BALANCE" });
					for (MdfAmountTilesDTO mdf : mdfData) {
						data.add(new String[] { mdf.getCompanyName(), mdf.getTotalBalanceInString(),
								mdf.getUsedBalanceInString(), mdf.getAvailableBalanceInString() });
					}
					return XamplifyUtils.generateCSV(fileName, response, data);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return response;
	}

	@Override
	public XtremandResponse findTotalPartnersCount(Integer userId, boolean applyFilter) {
		XtremandResponse response = new XtremandResponse();
		response.setStatusCode(400);
		Integer companyId = userDao.getCompanyIdByUserId(userId);
		if (XamplifyUtils.isValidInteger(companyId)) {
			Map<String, Object> resultMap = partnerAnalyticsDAO.getTotalPartnersCount(companyId, applyFilter, userId);
			response.setStatusCode(200);
			response.setData(resultMap);
		}
		return response;
	}

	@Override
	public HttpServletResponse downloadTeamMembersReport(PartnerJourneyRequestDTO partnerJourneyRequestDTO,
			HttpServletResponse response) {
		String fileName = "Team-Members-Report";
		Integer userId = partnerJourneyRequestDTO.getLoggedInUserId();
		Integer vendorCompanyId = userDao.getCompanyIdByUserId(partnerJourneyRequestDTO.getLoggedInUserId());
		Pagination pagination = new Pagination();
		pagination.setCompanyId(vendorCompanyId);
		pagination.setFilterKey(partnerJourneyRequestDTO.getFilterType());
		pagination.setExcludeLimit(true);
		framePaginationValues(partnerJourneyRequestDTO, userId, vendorCompanyId, pagination);
		Integer adminId = utilDAO.findAdminIdByCompanyId(partnerJourneyRequestDTO.getPartnerCompanyId());
		pagination.setUserId(adminId);
		pagination.setPartnerJourneyFilter(true);
		if (XamplifyUtils.isValidInteger(vendorCompanyId)) {
			utilService.setDateFilters(pagination);
			Map<String, Object> result = teamDao.findAll(pagination);
			if (result != null) {
				@SuppressWarnings("unchecked")
				List<TeamMemberListDTO> teamMemberData = (List<TeamMemberListDTO>) result.get("list");
				try {
					List<String[]> data = new ArrayList<>();
					data.add(new String[] { TEAM_MEMBER_NAME, "RECENT LOGIN", TEAM_MEMBER_EMAIL_ID, "GROUP NAME" });
					for (TeamMemberListDTO teamMember : teamMemberData) {
						String fullName = getFullName(teamMember.getFirstName(), teamMember.getLastName());
						String groupName = XamplifyUtils.isValidString(teamMember.getTeamMemberGroupName())
								? teamMember.getTeamMemberGroupName()
								: "NA";
						String status = (teamMember.getStatus() != null) ? teamMember.getStatus().trim().toUpperCase()
								: "";

						String recentLogin = ("UNAPPROVED".equals(status) || teamMember.getLogInTime() == null) ? "-"
								: DateUtils.dateToString(teamMember.getLogInTime());

						data.add(new String[] { fullName, recentLogin, teamMember.getEmailId(), groupName });
					}
					return XamplifyUtils.generateCSV(fileName, response, data);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return response;
	}

	@Override
	public XtremandResponse getPartnerJourneyAssetsDetails(PartnerJourneyRequestDTO partnerJourneyRequestDTO) {
		XtremandResponse response = new XtremandResponse();
		response.setStatusCode(400);
		Integer userId = partnerJourneyRequestDTO.getLoggedInUserId();
		Integer vendorCompanyId = userDao.getCompanyIdByUserId(partnerJourneyRequestDTO.getLoggedInUserId());
		if (XamplifyUtils.isValidInteger(vendorCompanyId)) {
			Pagination pagination = new Pagination();
			framePaginationValuesForAssets(partnerJourneyRequestDTO, userId, vendorCompanyId, pagination);
			pagination.setVendorCompanyId(vendorCompanyId);
			pagination.setSelectedCompanyIds(partnerJourneyRequestDTO.getCompanyIds());
			pagination.setAssetIds(partnerJourneyRequestDTO.getAssetIds());
			pagination.setSelectedEmailIds(partnerJourneyRequestDTO.getEmailIds());
			pagination.setTeamMemberId(partnerJourneyRequestDTO.getLoggedInUserId());
			utilService.setDateFilters(pagination);
			response.setData(partnerAnalyticsDAO.getPartnerJourneyAssetDetails(pagination));
			response.setStatusCode(200);
		}
		return response;
	}

	@SuppressWarnings("unchecked")
	@Override
	public HttpServletResponse downloadAssetDetailsReport(PartnerJourneyRequestDTO partnerJourneyRequestDTO,
			HttpServletResponse response) {
		String fileName = "Assets-Details-Report";
		Integer userId = partnerJourneyRequestDTO.getLoggedInUserId();
		Integer vendorCompanyId = userDao.getCompanyIdByUserId(partnerJourneyRequestDTO.getLoggedInUserId());
		Pagination pagination = new Pagination();
		pagination.setExcludeLimit(true);
		framePaginationValuesForAssets(partnerJourneyRequestDTO, userId, vendorCompanyId, pagination);
		if (XamplifyUtils.isValidInteger(vendorCompanyId)) {
			utilService.setDateFilters(pagination);
			Map<String, Object> result = partnerAnalyticsDAO.getPartnerJourneyAssetDetails(pagination);
			Map<String, Object> assetResult = partnerAnalyticsDAO.getPartnerJourneyAssetInteractionDetails(pagination);
			if (result != null) {
				List<PartnerJourneyTrackDetailsDTO> trackAssetsData = (List<PartnerJourneyTrackDetailsDTO>) result
						.get("list");
				List<PartnerJourneyTrackDetailsDTO> assetData = (List<PartnerJourneyTrackDetailsDTO>) assetResult
						.get("list");
				return frameCSVData(response, fileName, trackAssetsData, assetData, pagination);
			}
		}
		return response;
	}

	private void framePaginationValuesForAssets(PartnerJourneyRequestDTO partnerJourneyRequestDTO, Integer userId,
			Integer vendorCompanyId, Pagination pagination) {
		pagination.setCompanyId(vendorCompanyId);
		pagination.setVendorCompanyId(vendorCompanyId);
		pagination.setUserId(userId);
		pagination.setTrackTypeFilter(partnerJourneyRequestDTO.getTrackTypeFilter());
		pagination.setSelectedPartnerCompanyIds(partnerJourneyRequestDTO.getSelectedPartnerCompanyIds());
		pagination.setSearchKey(partnerJourneyRequestDTO.getSearchKey());
		pagination.setAssetTypeFilter(partnerJourneyRequestDTO.getAssetType());
		pagination.setCampaignTypeFilter(partnerJourneyRequestDTO.getCampaignTypeFilter());
		pagination.setDetailedAnalytics(partnerJourneyRequestDTO.isDetailedAnalytics());
		pagination.setPartnerCompanyId(partnerJourneyRequestDTO.getPartnerCompanyId());
		pagination.setPartnerTeamMemberGroupFilter(partnerJourneyRequestDTO.isPartnerTeamMemberGroupFilter());
		pagination.setFilterFromDateString(partnerJourneyRequestDTO.getFilterFromDateString());
		pagination.setFilterToDateString(partnerJourneyRequestDTO.getFilterToDateString());
		pagination.setFromDateFilterString(partnerJourneyRequestDTO.getFromDateFilterInString());
		pagination.setToDateFilterString(partnerJourneyRequestDTO.getToDateFilterInString());
		pagination.setTimeZone(partnerJourneyRequestDTO.getTimeZone());
		pagination.setPageIndex(partnerJourneyRequestDTO.getPageNumber());
		pagination.setMaxResults(partnerJourneyRequestDTO.getLimit());
		pagination.setSortcolumn(partnerJourneyRequestDTO.getSortcolumn());
		pagination.setSortingOrder(partnerJourneyRequestDTO.getSortingOrder());
		pagination.setModuleName(partnerJourneyRequestDTO.getModuleName());
		pagination.setAssetIds(partnerJourneyRequestDTO.getAssetIds());
		pagination.setSelectedCompanyIds(partnerJourneyRequestDTO.getCompanyIds());
		pagination.setSelectedEmailIds(partnerJourneyRequestDTO.getEmailIds());
		pagination.setTeamMemberId(partnerJourneyRequestDTO.getLoggedInUserId());
		pagination.setPartnershipStatus(partnerJourneyRequestDTO.getPartnershipStatus());
	}

	private HttpServletResponse frameCSVData(HttpServletResponse response, String fileName,
			List<PartnerJourneyTrackDetailsDTO> trackAssetsData, List<PartnerJourneyTrackDetailsDTO> assetData,
			Pagination pagination) {
		try {
			XSSFWorkbook workbook = createWorkbookWithAssetData(trackAssetsData, assetData, pagination);
			response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
			response.setHeader("Content-Disposition", "attachment; filename=" + fileName + ".xlsx");
			workbook.write(response.getOutputStream());
			workbook.close();
			return response;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return response;
	}

	private XSSFWorkbook createWorkbookWithAssetData(List<PartnerJourneyTrackDetailsDTO> trackAssetsData,
			List<PartnerJourneyTrackDetailsDTO> assetData, Pagination pagination) {
		List<String[]> row = new ArrayList<>();
		List<String> headerList = new ArrayList<>();
		if (pagination.isDetailedAnalytics()) {
			headerList.add(TEAM_MEMBER_NAME);
			headerList.add(TEAM_MEMBER_EMAIL_ID);
		} else {
			headerList.add(PARTNER_COMPANY);
		}
		headerList.add("ASSET NAME");
		headerList.add("ASSET TYPE");
		headerList.add("CREATED BY");
		headerList.add("VIEW COUNT");
		headerList.add("DOWNLOAD COUNT");
		headerList.add("PUBLISHED ON(PST)");
		row.add(headerList.toArray(new String[0]));
		for (PartnerJourneyTrackDetailsDTO track : trackAssetsData) {
			List<String> dataList = new ArrayList<>();
			String publishedDate = checkIfDateIsNull(track.getPublishedOn());
			if (pagination.isDetailedAnalytics()) {
				String fullName = getFullName(track.getFirstName(), track.getLastName());
				dataList.add(fullName);
				dataList.add(track.getEmailId());
			} else {
				dataList.add(track.getCompanyName());
			}
			dataList.add(track.getAssetName());
			dataList.add(track.getAssetType());
			dataList.add(track.getCreatedByName());
			dataList.add(track.getViewedCount() != null ? track.getViewedCount().toString() : "0");
			dataList.add(track.getDownloadedCount() != null ? track.getDownloadedCount().toString() : "0");
			dataList.add(publishedDate);
			row.add(dataList.toArray(new String[0]));
		}

		XSSFWorkbook workbook = new XSSFWorkbook();
		Sheet sheet = workbook.createSheet("Asset Details");
		CreationHelper createHelper = workbook.getCreationHelper();

		CellStyle hyperlinkStyle = workbook.createCellStyle();
		Font linkFont = workbook.createFont();
		linkFont.setUnderline(Font.U_SINGLE);
		linkFont.setColor(IndexedColors.BLUE.getIndex());
		hyperlinkStyle.setFont(linkFont);

		Row headerRow = sheet.createRow(0);
		for (int i = 0; i < row.get(0).length; i++) {
			headerRow.createCell(i).setCellValue(row.get(0)[i]);
		}
		if (!pagination.isDetailedAnalytics()) {
			sheet.setAutoFilter(new CellRangeAddress(0, 0, 0, headerRow.getLastCellNum() - 1));
		}

		int viewCountColumnIndex = -1;
		int downloadedCountColumnIndex = -1;
		for (int i = 0; i < row.get(0).length; i++) {
			if ("VIEW COUNT".equalsIgnoreCase(row.get(0)[i])) {
				viewCountColumnIndex = i;
			}
			if ("DOWNLOAD COUNT".equalsIgnoreCase(row.get(0)[i])) {
				downloadedCountColumnIndex = i;
			}
		}

		for (int i = 1; i < row.size(); i++) {
			Row dataRow = sheet.createRow(i);
			String[] data = row.get(i);

			for (int j = 0; j < data.length; j++) {
				Cell cell = dataRow.createCell(j);
				boolean isViewOrDownloadColumn = (j == viewCountColumnIndex || j == downloadedCountColumnIndex);
				if (!pagination.isDetailedAnalytics() && isViewOrDownloadColumn) {
					cell.setCellValue(Double.parseDouble(data[j].trim()));
					Hyperlink link = createHelper.createHyperlink(HyperlinkType.DOCUMENT);
					link.setAddress("'All Partner Interaction Details'!A1");
					cell.setHyperlink(link);
					cell.setCellStyle(hyperlinkStyle);
				} else {
					cell.setCellValue(data[j]);
				}
			}
		}
		if (!pagination.isDetailedAnalytics()) {
			Sheet viewDetailSheet = workbook.createSheet("All Partner Interaction Details");
			Row drillHeader = viewDetailSheet.createRow(0);
			drillHeader.createCell(0).setCellValue("ASSET  NAME");
			drillHeader.createCell(1).setCellValue("ASSET TYPE");
			drillHeader.createCell(2).setCellValue("CREATED BY");
			drillHeader.createCell(3).setCellValue("PARTNER COMPANY NAME");
			drillHeader.createCell(4).setCellValue("PARTNER EMAIL ID");
			drillHeader.createCell(5).setCellValue("PARTNER NAME");
			drillHeader.createCell(6).setCellValue("VIEW COUNT");
			drillHeader.createCell(7).setCellValue("DOWNLOADED COUNT");
			drillHeader.createCell(8).setCellValue("PUBLISHED ON(PST)");

			viewDetailSheet.setAutoFilter(new CellRangeAddress(0, 0, 0, 7));

			CellStyle lightYellowPercent = workbook.createCellStyle();
			lightYellowPercent.setDataFormat(workbook.createDataFormat().getFormat("0%"));
			lightYellowPercent.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
			lightYellowPercent.setFillPattern(FillPatternType.SOLID_FOREGROUND);

			CellStyle lightBluePercent = workbook.createCellStyle();
			lightBluePercent.setDataFormat(workbook.createDataFormat().getFormat("0%"));
			lightBluePercent.setFillForegroundColor(IndexedColors.PALE_BLUE.getIndex());
			lightBluePercent.setFillPattern(FillPatternType.SOLID_FOREGROUND);

			CellStyle lightGreenPercent = workbook.createCellStyle();
			lightGreenPercent.setDataFormat(workbook.createDataFormat().getFormat("0%"));
			lightGreenPercent.setFillForegroundColor(IndexedColors.SEA_GREEN.getIndex());
			lightGreenPercent.setFillPattern(FillPatternType.SOLID_FOREGROUND);

			int drillRowNum = 1;
			for (PartnerJourneyTrackDetailsDTO detail : assetData) {
				Row r = viewDetailSheet.createRow(drillRowNum++);

				r.createCell(0).setCellValue(detail.getAssetName());
				r.createCell(1).setCellValue(detail.getAssetType());
				r.createCell(2).setCellValue(detail.getCreatedByName());
				r.createCell(3).setCellValue(detail.getCompanyName());
				r.createCell(4).setCellValue(detail.getPartnerEmailId());
				r.createCell(5).setCellValue(detail.getPartnerName());
				BigInteger viewedCountBI = detail.getViewedCount();
				BigInteger completedCountBI = detail.getDownloadedCount();

				int viewedCount = (viewedCountBI != null) ? viewedCountBI.intValue() : 0;
				int downloadedCount = (completedCountBI != null) ? completedCountBI.intValue() : 0;

				r.createCell(6).setCellValue(viewedCount);
				r.createCell(7).setCellValue(downloadedCount);
				r.createCell(8).setCellValue(checkIfDateIsNull(detail.getPublishedOn()));
			}
		}
		return workbook;
	}

	public static String checkIfFullNameIsNull(String firstName, String lastName) {
		StringBuilder fullName = new StringBuilder();

		if (firstName != null && !firstName.trim().isEmpty()) {
			fullName.append(firstName.trim());
		}

		if (lastName != null && !lastName.trim().isEmpty()) {
			if (fullName.length() > 0) {
				fullName.append(" ");
			}
			fullName.append(lastName.trim());
		}

		return fullName.toString();
	}

	@Override
	public HttpServletResponse downloadActivePartnersReport(PartnerJourneyRequestDTO partnerJourneyRequestDTO,
			HttpServletResponse response) {
		String fileName = "Active-Partners-Report";
		Integer userId = partnerJourneyRequestDTO.getLoggedInUserId();
		Integer vendorCompanyId = userDao.getCompanyIdByUserId(partnerJourneyRequestDTO.getLoggedInUserId());
		Pagination pagination = new Pagination();
		pagination.setExcludeLimit(true);
		framePaginationValues(partnerJourneyRequestDTO, userId, vendorCompanyId, pagination);
		if (XamplifyUtils.isValidInteger(vendorCompanyId)) {
			utilService.setDateFilters(pagination);
			Map<String, Object> result = partnershipDAO.getActivePartnerCompanies(pagination);
			if (result != null) {
				@SuppressWarnings("unchecked")
				List<PartnerDTO> activePartnerData = (List<PartnerDTO>) result.get("list");
				try {
					List<String[]> data = new ArrayList<>();
					data.add(new String[] { COMPANY_NAME, NAME, JOB_TITLE, EMAIL_ID, MOBILE_NUMBER });
					for (PartnerDTO activePartner : activePartnerData) {
						String fullName = (Objects.toString(activePartner.getFirstName(), "") + " "
								+ Objects.toString(activePartner.getLastName(), "")).trim();
						data.add(new String[] { activePartner.getCompanyName(), fullName, activePartner.getJobTitle(),
								activePartner.getEmailId(), activePartner.getMobileNumber() });
					}
					return XamplifyUtils.generateCSV(fileName, response, data);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return response;
	}

	@Override
	public HttpServletResponse downloadInActivePartnersReport(PartnerJourneyRequestDTO partnerJourneyRequestDTO,
			HttpServletResponse response) {
		String fileName = "Dormant-Partners-Report";
		Integer userId = partnerJourneyRequestDTO.getLoggedInUserId();
		Integer vendorCompanyId = userDao.getCompanyIdByUserId(partnerJourneyRequestDTO.getLoggedInUserId());
		Pagination pagination = new Pagination();
		pagination.setCompanyId(vendorCompanyId);
		pagination.setExcludeLimit(true);
		framePaginationValues(partnerJourneyRequestDTO, userId, vendorCompanyId, pagination);

		if (XamplifyUtils.isValidInteger(vendorCompanyId)) {
			utilService.setDateFilters(pagination);
			Map<String, Object> result = partnerAnalyticsDAO.listInActiveCampaignPartners(pagination);

			if (result != null) {
				@SuppressWarnings("unchecked")
				List<Map<String, Object>> inActivePartnerData = (List<Map<String, Object>>) result
						.get("inactivePartnerList");

				try {
					List<String[]> data = new ArrayList<>();
					// Header row
					data.add(new String[] { COMPANY_NAME, NAME, JOB_TITLE, EMAIL_ID, MOBILE_NUMBER,
							"LAST REMINDER SENT" });

					for (Map<String, Object> row : inActivePartnerData) {
						String firstName = Objects.toString(row.get("firstName"), "");
						String lastName = Objects.toString(row.get("lastName"), "");
						String fullName = (firstName + " " + lastName).trim();

						String companyName = Objects.toString(row.get("companyName"), "");
						String jobTitle = Objects.toString(row.get("jobTitle"), "");
						String emailId = Objects.toString(row.get("emailId"), "");
						String mobileNumber = Objects.toString(row.get("mobileNumber"), "");
						String time = Objects.toString(row.get("time"), "");

						// Convert string -> date -> formatted string
						Date date = convertStringToDate(time);
						String publishedDate = checkIfDateIsNull(date);

						// Add row to CSV
						data.add(
								new String[] { companyName, fullName, jobTitle, emailId, mobileNumber, publishedDate });
					}

					// Generate and return CSV
					return XamplifyUtils.generateCSV(fileName, response, data);

				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return response;
	}

	private Date convertStringToDate(String dateString) {
		if (!XamplifyUtils.isValidString(dateString)) {
			return null; // Return null or handle it as needed
		}
		SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		Date date = null;
		try {
			date = inputFormat.parse(dateString);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return date;
	}

	@Override
	public HttpServletResponse downloadCompanyProfileIncompletePartnersReport(
			PartnerJourneyRequestDTO partnerJourneyRequestDTO, HttpServletResponse response) {
		String fileName = "-Partners-Report";
		Integer userId = partnerJourneyRequestDTO.getLoggedInUserId();
		Integer vendorCompanyId = userDao.getCompanyIdByUserId(partnerJourneyRequestDTO.getLoggedInUserId());
		Pagination pagination = new Pagination();
		pagination.setCompanyId(vendorCompanyId);
		pagination.setExcludeLimit(true);
		framePaginationValues(partnerJourneyRequestDTO, userId, vendorCompanyId, pagination);
		if (pagination.getModuleName().equalsIgnoreCase(XamplifyConstants.PARTNER_SIGNUP_URL_PREFIX)) {
			fileName = "Pending-Signup" + fileName;
		} else if (pagination.getModuleName().equalsIgnoreCase(XamplifyConstants.INCOMPLETE_COMPANY_PROFILE)) {
			fileName = "Company-Profile-Incomplete" + fileName;
		}

		if (XamplifyUtils.isValidInteger(vendorCompanyId)) {
			utilService.setDateFilters(pagination);
			Map<String, Object> result = partnerAnalyticsDAO
					.getPendingSignupAndCompanyProfileIncompletePartners(vendorCompanyId, pagination);
			if (result != null) {
				@SuppressWarnings("unchecked")
				List<PartnerDTO> inAactivePartnerData = (List<PartnerDTO>) result.get("list");
				try {
					List<String[]> data = new ArrayList<>();
					data.add(new String[] { COMPANY_NAME, NAME, EMAIL_ID, LAST_REMINDER_SENT });
					for (PartnerDTO inActivePartner : inAactivePartnerData) {
						String fullName = (Objects.toString(inActivePartner.getFirstName(), "") + " "
								+ Objects.toString(inActivePartner.getLastName(), "")).trim();
						String remainderSentOn = checkIfDateIsNull(inActivePartner.getSentOn());
						data.add(new String[] { inActivePartner.getCompanyName(), fullName,
								inActivePartner.getEmailId(), remainderSentOn });
					}
					return XamplifyUtils.generateCSV(fileName, response, data);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return response;
	}

	@Override
	public XtremandResponse findAllPartnerDetails(PartnerJourneyRequestDTO partnerJourneyRequestDTO) {
		XtremandResponse response = new XtremandResponse();
		response.setStatusCode(400);
		if (partnerJourneyRequestDTO.getLoggedInUserId() != null && partnerJourneyRequestDTO.getLoggedInUserId() > 0) {
			Integer vendorCompanyId = userDao.getCompanyIdByUserId(partnerJourneyRequestDTO.getLoggedInUserId());
			partnerJourneyRequestDTO.setVendorCompanyId(vendorCompanyId);
			if (vendorCompanyId != null && vendorCompanyId > 0) {
				List<PartnerJourneyTrackDetailsDTO> partnerJourneyTrackDetailsList = partnerAnalyticsDAO
						.getAllPartnerRegionSDetailsCount(partnerJourneyRequestDTO);
				List<List<Object>> regionPartnerCounts = new ArrayList<>();
				if (partnerJourneyTrackDetailsList != null && !partnerJourneyTrackDetailsList.isEmpty()) {
					for (PartnerJourneyTrackDetailsDTO dto : partnerJourneyTrackDetailsList) {
						xamplifyUtil.addItemsToArrayList(regionPartnerCounts, dto.getRegion(), dto.getPartnersCount());
					}
				}
				response.setData(regionPartnerCounts);
				response.setStatusCode(200);
			}
		}
		return response;
	}

	@Override
	public XtremandResponse getAllPartnersDetailsList(Pagination pagination) {
		XtremandResponse response = new XtremandResponse();
		response.setStatusCode(400);
		if (pagination.getUserId() != null && pagination.getUserId() > 0) {
			Integer vendorCompanyId = userDao.getCompanyIdByUserId(pagination.getUserId());
			pagination.setVendorCompanyId(vendorCompanyId);
			if (vendorCompanyId != null && vendorCompanyId > 0) {
				response.setData(partnerAnalyticsDAO.getAllPartnersDetailsList(pagination));
				response.setStatusCode(200);
			}
		}
		return response;
	}

	@Override
	public void allpartnersDownloadCsv(Integer userId, String regionFilter, String sortColumn,
			List<String> selectedRegionIdList, List<String> selectedStatusIdList, boolean partnerTeamMemberGroupFilter,
			Pageable pageable, HttpServletResponse response) {

		Pagination pagination = utilService.setPageableParameters(pageable, userId);
		pagination.setPageIndex(1);
		pagination.setRegionFilter(regionFilter);
		pagination.setSortcolumn(sortColumn);
		pagination.setSelectedRegionIds(selectedRegionIdList);
		pagination.setSelectedStatusIds(selectedStatusIdList);
		pagination.setPartnerTeamMemberGroupFilter(partnerTeamMemberGroupFilter);
		pagination.setFromDateFilterString(pageable.getFromDateFilterString());
		pagination.setToDateFilterString(pageable.getToDateFilterString());
		Map<String, Object> resultMap = partnerAnalyticsDAO.getAllPartnersDetailsList(pagination);
		@SuppressWarnings("unchecked")
		List<PartnerJourneyTrackDetailsDTO> partnerList = (List<PartnerJourneyTrackDetailsDTO>) resultMap.get("list");
		Set<PartnerJourneyTrackDetailsDTO> allPartners = new LinkedHashSet<>(partnerList);

		List<String[]> data = new ArrayList<>();
		data.add(new String[] { "FIRSTNAME", "EMAIL ID", "COMPANY NAME", "ONBOARDED ON", "REGION", "STATUS",
				"RECENT LOGIN" });
		for (PartnerJourneyTrackDetailsDTO partner : allPartners) {
			String onBoardedOn = partner.getOnboardedOn() != null ? DateUtils.dateToString(partner.getOnboardedOn())
					: "";
			String lastLogin = "";
			Date lastLoginDate = partner.getDateLastLogin();
			if (lastLoginDate == null) {
				lastLogin = "-";
			} else {
				lastLogin = partner.getDateLastLogin() != null ? DateUtils.dateToString(partner.getDateLastLogin())
						: "";
			}
			String fullName = checkIfFullNameIsNull(partner.getFirstName(), partner.getLastName());
			data.add(new String[] { fullName, partner.getEmailId(), partner.getCompanyName(), onBoardedOn,
					partner.getRegion(), partner.getStatus(), lastLogin });
		}
		String fileName = "all-partners.csv";
		try {
			XamplifyUtils.generateCSV(fileName, response, data);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/** XNFR-952 **/
	@Override
	public XtremandResponse listAllPartnersForContactUploadManagementSettings(Pageable pageable,
			Integer loggedInUserId) {
		XtremandResponse response = new XtremandResponse();
		if (!XamplifyUtils.isValidInteger(loggedInUserId)) {
			XamplifyUtils.addErorMessageWithStatusCode(response, INVALID_DATA, 400);
			return response;
		}
		Pagination pagination = utilService.setPageableParameters(pageable, loggedInUserId);
		PaginatedDTO paginatedDTO = partnerAnalyticsDAO.listAllPartnersForContactUploadManagementSettings(pagination,
				pageable.getSearch());
		XamplifyUtils.addPaginatedDTO(response, paginatedDTO);
		XamplifyUtils.addSuccessStatus(response);
		return response;
	}

	/** XNFR-952 **/
	@Override
	public XtremandResponse saveOrUpdateContactUploadSManagementSettings(Integer loggedInUserId,
			List<PartnerContactUsageDTO> partnerContactUsageDTOs) {
		XtremandResponse response = new XtremandResponse();
		if (!XamplifyUtils.isValidInteger(loggedInUserId)) {
			XamplifyUtils.addErorMessageWithStatusCode(response, INVALID_DATA, 400);
			return response;
		}
		Integer vendorCompanyId = userDao.getCompanyIdByUserId(loggedInUserId);
		if (XamplifyUtils.isValidInteger(vendorCompanyId)) {
			for (PartnerContactUsageDTO partnerContactUsageDTO : partnerContactUsageDTOs) {
				partnerAnalyticsDAO.updatePartnerContactUploadLimit(partnerContactUsageDTO.getCompanyId(),
						vendorCompanyId, partnerContactUsageDTO.getContactUploadLimit());
			}
			XamplifyUtils.addSuccessStatus(response);
		} else {
			XamplifyUtils.addErorMessageWithStatusCode(response, INVALID_DATA, 400);
		}
		return response;
	}

	/** XNFR-952 **/
	@Override
	public XtremandResponse fetchTotalNumberOfContactsAddedForCompany(Integer loggedInUserId) {
		XtremandResponse response = new XtremandResponse();
		if (!XamplifyUtils.isValidInteger(loggedInUserId)) {
			XamplifyUtils.addErorMessageWithStatusCode(response, INVALID_DATA, 400);
			return response;
		}
		Integer companyId = userDao.getCompanyIdByUserId(loggedInUserId);
		if (XamplifyUtils.isValidInteger(companyId)) {
			Integer contactsCount = partnerAnalyticsDAO.fetchNumberOfContactsAddedByCompanyId(companyId);
			response.setData(contactsCount);
			XamplifyUtils.addSuccessStatus(response);
		}
		return response;
	}

	/** XNFR-952 **/
	@Override
	public XtremandResponse loadContactsUploadedCountByAllPartners(Integer loggedInUserId) {
		XtremandResponse response = new XtremandResponse();
		if (!XamplifyUtils.isValidInteger(loggedInUserId)) {
			XamplifyUtils.addErorMessageWithStatusCode(response, INVALID_DATA, 400);
			return response;
		}
		Integer companyId = userDao.getCompanyIdByUserId(loggedInUserId);
		if (XamplifyUtils.isValidInteger(companyId)) {
			Integer contactsCount = partnerAnalyticsDAO.getContactsUploadedCountByAllPartnersForCompanyById(companyId);
			response.setData(contactsCount);
			XamplifyUtils.addSuccessStatus(response);
		}
		return response;
	}

	/** XNFR-952 **/
	@Override
	public XtremandResponse loadContactUploadSubscriptionLimitForCompany(Integer loggedInUserId) {
		XtremandResponse response = new XtremandResponse();
		if (!XamplifyUtils.isValidInteger(loggedInUserId)) {
			XamplifyUtils.addErorMessageWithStatusCode(response, INVALID_DATA, 400);
			return response;
		}
		Integer companyId = userDao.getCompanyIdByUserId(loggedInUserId);
		if (XamplifyUtils.isValidInteger(companyId)) {
			Integer contactsCount = partnerAnalyticsDAO.getContactUploadSubscriptionLimitByCompanyId(companyId);
			response.setData(contactsCount);
			XamplifyUtils.addSuccessStatus(response);
		}
		return response;
	}

	/** XNFR-952 **/
	@Override
	public XtremandResponse getNumberOfContactSubscriptionUsedByCompany(Integer loggedInUserId) {
		XtremandResponse response = new XtremandResponse();
		if (!XamplifyUtils.isValidInteger(loggedInUserId)) {
			XamplifyUtils.addErorMessageWithStatusCode(response, INVALID_DATA, 400);
			return response;
		}
		Integer companyId = userDao.getCompanyIdByUserId(loggedInUserId);
		if (XamplifyUtils.isValidInteger(companyId)) {
			Integer contactsCount = utilDao.getTotalContactSubscriptionUsedByCompanyAndPartners(companyId);
			response.setData(contactsCount);
			XamplifyUtils.addSuccessStatus(response);
		}
		return response;
	}

	@Override
	public XtremandResponse getAllAssetNamesForFilter(Pagination pagination) {
		XtremandResponse response = new XtremandResponse();
		response.setStatusCode(400);
		if (pagination.getUserId() != null && pagination.getUserId() > 0) {
			Integer vendorCompanyId = userDao.getCompanyIdByUserId(pagination.getUserId());
			if (vendorCompanyId != null && vendorCompanyId > 0) {
				pagination.setVendorCompanyId(vendorCompanyId);
				response.setData(partnerAnalyticsDAO.getAllAssetNamesForFilter(pagination));
				response.setStatusCode(200);
			}
		}
		return response;
	}

	@Override
	public XtremandResponse getAllEmailIdsForFilter(Pagination pagination) {
		XtremandResponse response = new XtremandResponse();
		response.setStatusCode(400);

		if (pagination.getUserId() != null && pagination.getUserId() > 0) {
			Integer vendorCompanyId = userDao.getCompanyIdByUserId(pagination.getUserId());
			if (vendorCompanyId != null && vendorCompanyId > 0) {
				pagination.setVendorCompanyId(vendorCompanyId);
				response.setData(partnerAnalyticsDAO.getAllEmailIdsForFilter(pagination));
				response.setStatusCode(200);
			}
		}
		return response;
	}

	// XNFR - 989
	@Override
	public XtremandResponse getAssetJourneyAssetsDetails(Pagination pagination) {
		XtremandResponse response = new XtremandResponse();
		response.setStatusCode(400);
		Integer userId = pagination.getUserId();
		Integer vendorCompanyId = userDao.getCompanyIdByUserId(userId);
		if (vendorCompanyId != null && vendorCompanyId > 0) {
			pagination.setVendorCompanyId(vendorCompanyId);
			utilService.setDateFilters(pagination);
			response.setData(partnerAnalyticsDAO.getAssetJourneyAssetsDetails(pagination));
			response.setStatusCode(200);
		}
		return response;
	}

	@SuppressWarnings("unchecked")
	@Override
	public HttpServletResponse downloadAssetJourneyAssetDetailsReport(PartnerJourneyRequestDTO partnerJourneyRequestDTO,
			HttpServletResponse response) {
		String fileName = "Assets-Interaction-Details-Report";
		Integer userId = partnerJourneyRequestDTO.getLoggedInUserId();
		Integer vendorCompanyId = userDao.getCompanyIdByUserId(partnerJourneyRequestDTO.getLoggedInUserId());
		Pagination pagination = new Pagination();
		pagination.setExcludeLimit(true);
		framePaginationValuesForAssets(partnerJourneyRequestDTO, userId, vendorCompanyId, pagination);
		if (XamplifyUtils.isValidInteger(vendorCompanyId)) {
			pagination.setVendorCompanyId(vendorCompanyId);
			utilService.setDateFilters(pagination);
			Map<String, Object> result = partnerAnalyticsDAO.getAssetJourneyAssetsDetails(pagination);
			Map<String, Object> AssetInteractionresult = partnerAnalyticsDAO
					.getPartnerAssetDetailsInteraction(pagination);
			if (result != null) {
				List<PartnerJourneyTrackDetailsDTO> trackAssetsData = (List<PartnerJourneyTrackDetailsDTO>) result
						.get("list");
				List<PartnerJourneyTrackDetailsDTO> assetInteractionData = (List<PartnerJourneyTrackDetailsDTO>) AssetInteractionresult
						.get("list");
				return frameAssetInteractionCSVData(response, fileName, trackAssetsData, assetInteractionData,
						pagination);
			}
		}
		return response;
	}

	private HttpServletResponse frameAssetInteractionCSVData(HttpServletResponse response, String fileName,
			List<PartnerJourneyTrackDetailsDTO> trackAssetsData,
			List<PartnerJourneyTrackDetailsDTO> assetInteractionData, Pagination pagination) {
		try {
			XSSFWorkbook workbook = createWorkbookWithAssetInteractionDetails(trackAssetsData, assetInteractionData,
					pagination);
			response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
			response.setHeader("Content-Disposition", "attachment; filename=" + fileName + ".xlsx");
			workbook.write(response.getOutputStream());
			workbook.close();
			return response;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return response;
	}

	private XSSFWorkbook createWorkbookWithAssetInteractionDetails(List<PartnerJourneyTrackDetailsDTO> trackAssetsData,
			List<PartnerJourneyTrackDetailsDTO> assetInteractionData, Pagination pagination) {
		List<String[]> row = new ArrayList<>();
		List<String> headerList = new ArrayList<>();
		headerList.add("ASSET NAME");
		headerList.add("ASSET TYPE");
		headerList.add("CREATED BY");
		headerList.add("VIEW COUNT");
		headerList.add("DOWNLOAD COUNT");
		headerList.add("PUBLISHED ON(PST)");
		row.add(headerList.toArray(new String[0]));
		for (PartnerJourneyTrackDetailsDTO track : trackAssetsData) {
			List<String> dataList = new ArrayList<>();
			String publishedDate = checkIfDateIsNull(track.getPublishedOn());
			dataList.add(track.getAssetName());
			dataList.add(track.getAssetType());
			dataList.add(track.getCreatedByName());
			dataList.add(track.getViewedCount() != null ? track.getViewedCount().toString() : "0");
			dataList.add(track.getDownloadedCount() != null ? track.getDownloadedCount().toString() : "0");
			dataList.add(publishedDate);
			row.add(dataList.toArray(new String[0]));
		}

		XSSFWorkbook workbook = new XSSFWorkbook();
		Sheet sheet = workbook.createSheet("Asset Interaction Details");
		CreationHelper createHelper = workbook.getCreationHelper();

		CellStyle hyperlinkStyle = workbook.createCellStyle();
		Font linkFont = workbook.createFont();
		linkFont.setUnderline(Font.U_SINGLE);
		linkFont.setColor(IndexedColors.BLUE.getIndex());
		hyperlinkStyle.setFont(linkFont);

		Row headerRow = sheet.createRow(0);
		for (int i = 0; i < row.get(0).length; i++) {
			headerRow.createCell(i).setCellValue(row.get(0)[i]);
		}
		if (!pagination.isDetailedAnalytics()) {
			sheet.setAutoFilter(new CellRangeAddress(0, 0, 0, headerRow.getLastCellNum() - 1));
		}

		int viewCountColumnIndex = -1;
		int completedCountColumnIndex = -1;
		for (int i = 0; i < row.get(0).length; i++) {
			if ("VIEW COUNT".equalsIgnoreCase(row.get(0)[i])) {
				viewCountColumnIndex = i;
			}
			if ("DOWNLOAD COUNT".equalsIgnoreCase(row.get(0)[i])) {
				completedCountColumnIndex = i;
			}
		}

		for (int i = 1; i < row.size(); i++) {
			Row dataRow = sheet.createRow(i);
			String[] data = row.get(i);

			for (int j = 0; j < data.length; j++) {
				Cell cell = dataRow.createCell(j);
				if (j == viewCountColumnIndex) {
					cell.setCellValue(data[j]);
					Hyperlink link = createHelper.createHyperlink(HyperlinkType.DOCUMENT);
					link.setAddress("'All Partner Interaction Details'!A1");
					cell.setHyperlink(link);
					cell.setCellStyle(hyperlinkStyle);
				} else if (j == completedCountColumnIndex) {
					cell.setCellValue(data[j]);
					Hyperlink link = createHelper.createHyperlink(HyperlinkType.DOCUMENT);
					link.setAddress("'All Partner Interaction Details'!A1");
					cell.setHyperlink(link);
					cell.setCellStyle(hyperlinkStyle);
				} else {
					cell.setCellValue(data[j]);
				}
			}
		}

		if (!pagination.isDetailedAnalytics()) {
			Sheet viewDetailSheet = workbook.createSheet("All Partner Interaction Details");
			Row drillHeader = viewDetailSheet.createRow(0);
			drillHeader.createCell(0).setCellValue("ASSET  NAME");
			drillHeader.createCell(1).setCellValue("ASSET TYPE");
			drillHeader.createCell(2).setCellValue("CREATED BY");
			drillHeader.createCell(3).setCellValue("PARTNER COMPANY NAME");
			drillHeader.createCell(4).setCellValue("PARTNER EMAIL ID");
			drillHeader.createCell(5).setCellValue("PARTNER NAME");
			drillHeader.createCell(6).setCellValue("VIEW COUNT");
			drillHeader.createCell(7).setCellValue("DOWNLOAD COUNT");
			drillHeader.createCell(8).setCellValue("PUBLISHED ON(PST)");

			viewDetailSheet.setAutoFilter(new CellRangeAddress(0, 0, 0, 7));

			CellStyle lightYellowPercent = workbook.createCellStyle();
			lightYellowPercent.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
			lightYellowPercent.setFillPattern(FillPatternType.SOLID_FOREGROUND);

			CellStyle lightBluePercent = workbook.createCellStyle();
			lightBluePercent.setFillForegroundColor(IndexedColors.PALE_BLUE.getIndex());
			lightBluePercent.setFillPattern(FillPatternType.SOLID_FOREGROUND);

			CellStyle lightGreenPercent = workbook.createCellStyle();
			lightGreenPercent.setFillForegroundColor(IndexedColors.SEA_GREEN.getIndex());
			lightGreenPercent.setFillPattern(FillPatternType.SOLID_FOREGROUND);

			int drillRowNum = 1;
			for (PartnerJourneyTrackDetailsDTO detail : assetInteractionData) {
				Row r = viewDetailSheet.createRow(drillRowNum++);

				r.createCell(0).setCellValue(detail.getAssetName());
				r.createCell(1).setCellValue(detail.getAssetType());
				r.createCell(2).setCellValue(detail.getCreatedByName());
				r.createCell(3).setCellValue(detail.getCompanyName());
				r.createCell(4).setCellValue(detail.getPartnerEmailId());
				r.createCell(5).setCellValue(detail.getPartnerName());
				BigInteger viewedCountBI = detail.getViewedCount();
				BigInteger completedCountBI = detail.getDownloadedCount();

				int viewedCount = (viewedCountBI != null) ? viewedCountBI.intValue() : 0;
				int downloadedCount = (completedCountBI != null) ? completedCountBI.intValue() : 0;

				r.createCell(6).setCellValue(viewedCount);
				r.createCell(7).setCellValue(downloadedCount);
				r.createCell(8).setCellValue(checkIfDateIsNull(detail.getPublishedOn()));
			}
		}
		return workbook;
	}

//	private HttpServletResponse frameAssetInteractionCSVData(HttpServletResponse response, String fileName,
//			List<PartnerJourneyTrackDetailsDTO> trackAssetsData, Pagination pagination) {
//		try {
//			List<String[]> row = new ArrayList<>();
//			List<String> headerList = new ArrayList<>();
//			headerList.add("ASSET NAME");
//			headerList.add("ASSET TYPE");
//			headerList.add("CREATED BY");
//			headerList.add("VIEW COUNT");
//			headerList.add("DOWNLOAD COUNT");
//			headerList.add("PUBLISHED ON(PST)");
//			row.add(headerList.toArray(new String[0]));
//			for (PartnerJourneyTrackDetailsDTO track : trackAssetsData) {
//				List<String> dataList = new ArrayList<>();
//				String publishedDate = checkIfDateIsNull(track.getPublishedOn());
//				dataList.add(track.getAssetName());
//				dataList.add(track.getAssetType());
//				dataList.add(track.getCreatedByName());
//				dataList.add(track.getViewedCount() != null ? track.getViewedCount().toString() : "0");
//				dataList.add(track.getDownloadedCount() != null ? track.getDownloadedCount().toString() : "0");
//				dataList.add(publishedDate);
//				row.add(dataList.toArray(new String[0]));
//			}
//			return XtremandUtils.generateCSV(fileName, response, row);
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		return response;
//	}

	@Override
	public XtremandResponse getPlaybookJourneyInteractionDetails(Pagination pagination) {
		XtremandResponse response = new XtremandResponse();
		response.setStatusCode(400);
		Integer userId = pagination.getUserId();
		Integer vendorCompanyId = userDao.getCompanyIdByUserId(userId);
		if (vendorCompanyId != null && vendorCompanyId > 0) {
			pagination.setVendorCompanyId(vendorCompanyId);
			utilService.setDateFilters(pagination);
			response.setData(partnerAnalyticsDAO.getPlaybookJourneyInteractionDetails(pagination));
			response.setStatusCode(200);
		}
		return response;
	}

	@Override
	public XtremandResponse findTotalDeactivatePartnersCount(Integer userId, boolean applyFilter) {
		XtremandResponse response = new XtremandResponse();
		response.setStatusCode(400);
		Integer companyId = userDao.getCompanyIdByUserId(userId);
		if (XamplifyUtils.isValidInteger(companyId)) {
			Map<String, Object> resultMap = partnerAnalyticsDAO.findTotalDeactivatePartnersCount(companyId, applyFilter,
					userId);
			response.setStatusCode(200);
			response.setData(resultMap);
		}
		return response;
	}

	@Override
	public HttpServletResponse downloadPlaybookJourneyInteractionDetailsReport(
			PartnerJourneyRequestDTO partnerJourneyRequestDTO, HttpServletResponse response) {
		String fileName = "Playbook-Interaction-Details-Report";
		Integer userId = partnerJourneyRequestDTO.getLoggedInUserId();
		Integer vendorCompanyId = userDao.getCompanyIdByUserId(partnerJourneyRequestDTO.getLoggedInUserId());
		Pagination pagination = new Pagination();
		pagination.setExcludeLimit(true);
		framePaginationValuesForPlaybookInteraction(partnerJourneyRequestDTO, userId, vendorCompanyId, pagination);
		if (XamplifyUtils.isValidInteger(vendorCompanyId)) {
			pagination.setVendorCompanyId(vendorCompanyId);
			utilService.setDateFilters(pagination);
			Map<String, Object> result = partnerAnalyticsDAO.getPlaybookJourneyInteractionDetails(pagination);
			Map<String, Object> totalPartnerInteraction = partnerAnalyticsDAO
					.getTotalPartnerInteractionDetails(pagination);
			if (result != null) {
				@SuppressWarnings("unchecked")
				List<PartnerJourneyTrackDetailsDTO> trackAssetsData = (List<PartnerJourneyTrackDetailsDTO>) result
						.get("list");
				@SuppressWarnings("unchecked")
				List<PartnerJourneyTrackDetailsDTO> playbookTotalPartnerInteraction = (List<PartnerJourneyTrackDetailsDTO>) totalPartnerInteraction
						.get("list");
				return framePlaybookInteractionCSVData(response, fileName, trackAssetsData,
						playbookTotalPartnerInteraction, pagination);
			}
		}
		return response;
	}

	private void framePaginationValuesForPlaybookInteraction(PartnerJourneyRequestDTO partnerJourneyRequestDTO,
			Integer userId, Integer vendorCompanyId, Pagination pagination) {
		pagination.setCompanyId(vendorCompanyId);
		pagination.setVendorCompanyId(vendorCompanyId);
		pagination.setUserId(userId);
		pagination.setTrackTypeFilter(partnerJourneyRequestDTO.getTrackTypeFilter());
		pagination.setSelectedPartnerCompanyIds(partnerJourneyRequestDTO.getSelectedPartnerCompanyIds());
		pagination.setSearchKey(partnerJourneyRequestDTO.getSearchKey());
		pagination.setAssetTypeFilter(partnerJourneyRequestDTO.getAssetType());
		pagination.setCampaignTypeFilter(partnerJourneyRequestDTO.getCampaignTypeFilter());
		pagination.setDetailedAnalytics(partnerJourneyRequestDTO.isDetailedAnalytics());
		pagination.setPartnerCompanyId(partnerJourneyRequestDTO.getPartnerCompanyId());
		pagination.setPartnerTeamMemberGroupFilter(partnerJourneyRequestDTO.isPartnerTeamMemberGroupFilter());
		pagination.setTeamMemberId(partnerJourneyRequestDTO.getTeamMemberUserId());
		pagination.setFromDateFilterString(partnerJourneyRequestDTO.getFromDateFilterInString());
		pagination.setToDateFilterString(partnerJourneyRequestDTO.getToDateFilterInString());
		pagination.setTimeZone(partnerJourneyRequestDTO.getTimeZone());
		pagination.setPageIndex(partnerJourneyRequestDTO.getPageNumber());
		pagination.setMaxResults(partnerJourneyRequestDTO.getLimit());
		pagination.setSortcolumn(partnerJourneyRequestDTO.getSortcolumn());
		pagination.setSortingOrder(partnerJourneyRequestDTO.getSortingOrder());
		pagination.setModuleName(partnerJourneyRequestDTO.getModuleName());
		pagination.setSelectedCompanyIds(partnerJourneyRequestDTO.getCompanyIds());
		pagination.setSelectedPlaybookNames(partnerJourneyRequestDTO.getPlaybookNames());
		pagination.setPartnershipStatus(partnerJourneyRequestDTO.getPartnershipStatus());
	}

	private HttpServletResponse framePlaybookInteractionCSVData(HttpServletResponse response, String fileName,
			List<PartnerJourneyTrackDetailsDTO> trackAssetsData,
			List<PartnerJourneyTrackDetailsDTO> totalPartnerInteraction, Pagination pagination) {
		try {
			XSSFWorkbook workbook = createWorkbookWithPlaybookData(trackAssetsData, totalPartnerInteraction,
					pagination);
			response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
			response.setHeader("Content-Disposition", "attachment; filename=" + fileName + ".xlsx");
			workbook.write(response.getOutputStream());
			workbook.close();
			return response;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return response;
	}

	private XSSFWorkbook createWorkbookWithPlaybookData(List<PartnerJourneyTrackDetailsDTO> trackAssetsData,
			List<PartnerJourneyTrackDetailsDTO> totalPartnerInteraction, Pagination pagination) {
		List<String[]> row = new ArrayList<>();
		List<String> headerList = new ArrayList<>();
		if (pagination.isDetailedAnalytics()) {
			headerList.add(TEAM_MEMBER_NAME);
			headerList.add(TEAM_MEMBER_EMAIL_ID);
		}
		headerList.add("PLAYBOOK NAME");
		headerList.add("CREATED BY");
		if (pagination.isDetailedAnalytics()) {
			headerList.add("PROGRESS");
		} else {
			headerList.add("VIEW COUNT");
			headerList.add("COMPLETED COUNT");
		}
		headerList.add("PUBLISHED ON(PST)");

		row.add(headerList.toArray(new String[0]));
		for (PartnerJourneyTrackDetailsDTO track : trackAssetsData) {
			List<String> dataList = new ArrayList<>();
			String publishedDate = checkIfDateIsNull(track.getPublishedOn());
			if (pagination.isDetailedAnalytics()) {
				dataList.add(track.getFirstName());
				dataList.add(track.getEmailId());
			}
			dataList.add(track.getPlaybookName());
			dataList.add(track.getCreatedByName());
			if (pagination.isDetailedAnalytics()) {
				dataList.add(track.getProgress() != null ? track.getProgress().toString() + "%" : "0%");
			} else {
				dataList.add(track.getViewedCount() != null ? track.getViewedCount().toString() : "0");
				dataList.add(track.getCompletedCount() != null ? track.getCompletedCount().toString() : "0");
			}
			dataList.add(publishedDate);
			row.add(dataList.toArray(new String[0]));
		}

		XSSFWorkbook workbook = new XSSFWorkbook();
		Sheet sheet = workbook.createSheet("Playbook Interaction Details");
		CreationHelper createHelper = workbook.getCreationHelper();

		CellStyle hyperlinkStyle = workbook.createCellStyle();
		Font linkFont = workbook.createFont();
		linkFont.setUnderline(Font.U_SINGLE);
		linkFont.setColor(IndexedColors.BLUE.getIndex());
		hyperlinkStyle.setFont(linkFont);

		Row headerRow = sheet.createRow(0);
		for (int i = 0; i < row.get(0).length; i++) {
			headerRow.createCell(i).setCellValue(row.get(0)[i]);
		}
		if (!pagination.isDetailedAnalytics()) {
			sheet.setAutoFilter(new CellRangeAddress(0, 0, 0, headerRow.getLastCellNum() - 1));
		}

		int viewCountColumnIndex = -1;
		int completedCountColumnIndex = -1;
		for (int i = 0; i < row.get(0).length; i++) {
			if ("VIEW COUNT".equalsIgnoreCase(row.get(0)[i])) {
				viewCountColumnIndex = i;
			}
			if ("COMPLETED COUNT".equalsIgnoreCase(row.get(0)[i])) {
				completedCountColumnIndex = i;
			}
		}

		for (int i = 1; i < row.size(); i++) {
			Row dataRow = sheet.createRow(i);
			String[] data = row.get(i);

			for (int j = 0; j < data.length; j++) {
				Cell cell = dataRow.createCell(j);
				if (j == viewCountColumnIndex) {
					cell.setCellValue(data[j]);
					Hyperlink link = createHelper.createHyperlink(HyperlinkType.DOCUMENT);
					link.setAddress("'All Partner Interaction Details'!A1");
					cell.setHyperlink(link);
					cell.setCellStyle(hyperlinkStyle);
				} else if (j == completedCountColumnIndex) {
					cell.setCellValue(data[j]);
					Hyperlink link = createHelper.createHyperlink(HyperlinkType.DOCUMENT);
					link.setAddress("'All Partner Interaction Details'!A1");
					cell.setHyperlink(link);
					cell.setCellStyle(hyperlinkStyle);
				} else {
					cell.setCellValue(data[j]);
				}
			}
		}

		if (!pagination.isDetailedAnalytics()) {
			Sheet viewDetailSheet = workbook.createSheet("All Partner Interaction Details");
			Row drillHeader = viewDetailSheet.createRow(0);
			drillHeader.createCell(0).setCellValue("PLAYBOOK  NAME");
			drillHeader.createCell(1).setCellValue("CREATED BY");
			drillHeader.createCell(2).setCellValue("PARTNER COMPANY NAME");
			drillHeader.createCell(3).setCellValue("PARTNER EMAIL ID");
			drillHeader.createCell(4).setCellValue("PARTNER FULL NAME");
			drillHeader.createCell(5).setCellValue("VIEW COUNT");
			drillHeader.createCell(6).setCellValue("COMPLETED COUNT");
			drillHeader.createCell(7).setCellValue("PROGRESS");
			drillHeader.createCell(8).setCellValue("PUBLISHED ON(PST)");

			viewDetailSheet.setAutoFilter(new CellRangeAddress(0, 0, 0, 8));

			CellStyle lightYellowPercent = workbook.createCellStyle();
			lightYellowPercent.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
			lightYellowPercent.setFillPattern(FillPatternType.SOLID_FOREGROUND);

			CellStyle lightBluePercent = workbook.createCellStyle();
			lightBluePercent.setFillForegroundColor(IndexedColors.PALE_BLUE.getIndex());
			lightBluePercent.setFillPattern(FillPatternType.SOLID_FOREGROUND);

			CellStyle lightGreenPercent = workbook.createCellStyle();
			lightGreenPercent.setFillForegroundColor(IndexedColors.SEA_GREEN.getIndex());
			lightGreenPercent.setFillPattern(FillPatternType.SOLID_FOREGROUND);

			int drillRowNum = 1;
			for (PartnerJourneyTrackDetailsDTO detail : totalPartnerInteraction) {
				Row r = viewDetailSheet.createRow(drillRowNum++);

				r.createCell(0).setCellValue(detail.getPlaybookName());
				r.createCell(1).setCellValue(detail.getCreatedByName());
				r.createCell(2).setCellValue(detail.getPartnerCompanyName());
				r.createCell(3).setCellValue(detail.getPartnerEmailId());
				r.createCell(4).setCellValue(detail.getPartnerFullName());
				BigInteger viewedCountBI = detail.getViewedCount();
				BigInteger completedCountBI = detail.getCompletedCount();

				int viewedCount = (viewedCountBI != null) ? viewedCountBI.intValue() : 0;
				int completedCount = (completedCountBI != null) ? completedCountBI.intValue() : 0;

				r.createCell(5).setCellValue(viewedCount);
				r.createCell(6).setCellValue(completedCount);
				r.createCell(8).setCellValue(checkIfDateIsNull(detail.getPublishedOn()));

				Cell progressCell = r.createCell(7);
				Integer progress = detail.getProgress();
				String progressValue = (progress != null ? progress + "%" : "0%");
				progressCell.setCellValue(progressValue);

				if ("0%".equals(progressValue)) {
					progressCell.setCellStyle(lightYellowPercent);
				} else if ("100%".equals(progressValue)) {
					progressCell.setCellStyle(lightGreenPercent);
				} else {
					progressCell.setCellStyle(lightBluePercent);
				}
			}
		}
		return workbook;
	}

	@Override
	public XtremandResponse getAllPlaybookNamesForFilter(Pagination pagination) {
		XtremandResponse response = new XtremandResponse();
		response.setStatusCode(400);
		if (pagination.getUserId() != null && pagination.getUserId() > 0) {
			Integer vendorCompanyId = userDao.getCompanyIdByUserId(pagination.getUserId());
			if (vendorCompanyId != null && vendorCompanyId > 0) {
				pagination.setVendorCompanyId(vendorCompanyId);
				response.setData(partnerAnalyticsDAO.getAllPlaybookNamesForFilter(pagination));
				response.setStatusCode(200);
			}
		}
		return response;
	}

	@Override
	public XtremandResponse getDeactivePartnerCompanies(Pagination pagination) {
		XtremandResponse response = new XtremandResponse();
		response.setStatusCode(400);
		if (pagination != null && pagination.getUserId() != null && pagination.getUserId() > 0) {
			Integer vendorCompanyId = userDao.getCompanyIdByUserId(pagination.getUserId());
			if (vendorCompanyId != null && vendorCompanyId > 0) {
				pagination.setVendorCompanyId(vendorCompanyId);
				response.setData(partnershipDAO.getdeactivePartnerCompanies(pagination));
				response.setStatusCode(200);
			}
		}
		return response;
	}

	@Override
	public HttpServletResponse downloadDeactivePartnersReport(PartnerJourneyRequestDTO partnerJourneyRequestDTO,
			HttpServletResponse response) {
		String fileName = "Deactivated-Partners-Report";
		Integer userId = partnerJourneyRequestDTO.getLoggedInUserId();
		Integer vendorCompanyId = userDao.getCompanyIdByUserId(partnerJourneyRequestDTO.getLoggedInUserId());
		Pagination pagination = new Pagination();
		pagination.setExcludeLimit(true);
		framePaginationValues(partnerJourneyRequestDTO, userId, vendorCompanyId, pagination);
		if (XamplifyUtils.isValidInteger(vendorCompanyId)) {
			utilService.setDateFilters(pagination);
			Map<String, Object> result = partnershipDAO.getdeactivePartnerCompanies(pagination);
			if (result != null) {
				@SuppressWarnings("unchecked")
				List<PartnerDTO> activePartnerData = (List<PartnerDTO>) result.get("list");
				try {
					List<String[]> data = new ArrayList<>();
					data.add(new String[] { COMPANY_NAME, NAME, JOB_TITLE, EMAIL_ID, MOBILE_NUMBER });
					for (PartnerDTO deactivePartner : activePartnerData) {
						String fullName = (Objects.toString(deactivePartner.getFirstName(), "") + " "
								+ Objects.toString(deactivePartner.getLastName(), "")).trim();
						data.add(new String[] { deactivePartner.getCompanyName(), fullName,
								deactivePartner.getJobTitle(), deactivePartner.getEmailId(),
								deactivePartner.getMobileNumber() });
					}
					return XamplifyUtils.generateCSV(fileName, response, data);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return response;
	}

	@Override
	public XtremandResponse getAllPartnerCompaniesDeatails(Pagination pagination) {
		XtremandResponse response = new XtremandResponse();
		response.setStatusCode(400);
		if (pagination != null && pagination.getUserId() != null && pagination.getUserId() > 0) {
			Integer vendorCompanyId = userDao.getCompanyIdByUserId(pagination.getUserId());
			if (vendorCompanyId != null && vendorCompanyId > 0) {
				pagination.setVendorCompanyId(vendorCompanyId);
				response.setData(partnershipDAO.getAllPartnerCompaniesDetails(pagination));
				response.setStatusCode(200);
			}
		}
		return response;
	}

	@Override
	public HttpServletResponse downloadAllPartnersDetailsReport(PartnerJourneyRequestDTO partnerJourneyRequestDTO,
			HttpServletResponse response) {
		String fileName = "All-Partners-Report";
		Integer userId = partnerJourneyRequestDTO.getLoggedInUserId();
		Integer vendorCompanyId = userDao.getCompanyIdByUserId(partnerJourneyRequestDTO.getLoggedInUserId());
		Pagination pagination = new Pagination();
		pagination.setExcludeLimit(true);
		framePaginationValues(partnerJourneyRequestDTO, userId, vendorCompanyId, pagination);
		if (XamplifyUtils.isValidInteger(vendorCompanyId)) {
			utilService.setDateFilters(pagination);
			Map<String, Object> result = partnershipDAO.getAllPartnerCompaniesDetails(pagination);
			if (result != null) {
				@SuppressWarnings("unchecked")
				List<PartnerDTO> activePartnerData = (List<PartnerDTO>) result.get("list");
				try {
					List<String[]> data = new ArrayList<>();
					data.add(new String[] { COMPANY_NAME, NAME, EMAIL_ID, STATUS });
					for (PartnerDTO partner : activePartnerData) {
						String fullName = (Objects.toString(partner.getFirstName(), "") + " "
								+ Objects.toString(partner.getLastName(), "")).trim();
						data.add(new String[] { partner.getCompanyName(), fullName, partner.getEmailId(),
								partner.getStatus() });
					}
					return XamplifyUtils.generateCSV(fileName, response, data);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return response;
	}

}
