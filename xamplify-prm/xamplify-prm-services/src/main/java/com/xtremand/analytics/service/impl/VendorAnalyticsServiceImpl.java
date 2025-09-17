package com.xtremand.analytics.service.impl;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.xtremand.analytics.dao.VendorAnalyticsDAO;
import com.xtremand.analytics.service.VendorAnalyticsService;
import com.xtremand.common.bom.Pagination;
import com.xtremand.dashboard.analytics.views.bom.DashboardModuleAnalyticsView;
import com.xtremand.dashboard.analytics.views.dao.DashboardAnalyticsViewsDao;
import com.xtremand.form.dao.FormDao;
import com.xtremand.formbeans.UserDTO;
import com.xtremand.formbeans.XtremandResponse;
import com.xtremand.user.bom.User;
import com.xtremand.user.dao.UserDAO;
import com.xtremand.user.service.UserService;
import com.xtremand.util.DateUtils;
import com.xtremand.util.XamplifyUtil;
import com.xtremand.util.XamplifyUtils;
import com.xtremand.util.dao.UtilDao;
import com.xtremand.util.service.UtilService;
import com.xtremand.vanity.url.dto.VanityUrlDetailsDTO;
import com.xtremand.vendor.bom.MyVendorCountsDTO;
import com.xtremand.vendor.bom.VendorDTO;

@Service("vendorAnalyticsService")
@Transactional
public class VendorAnalyticsServiceImpl implements VendorAnalyticsService {

	@Value("${server_path}")
	String serverPath;

	@Autowired
	VendorAnalyticsDAO vendorAnalyticsDAO;

	@Autowired
	UserService userService;

	@Autowired
	private UserDAO userDao;

	@Autowired
	private FormDao formDao;

	@Autowired
	private XamplifyUtil xamplifyUtil;

	@Autowired
	private UtilService utilService;

	@Autowired
	private DashboardAnalyticsViewsDao dashboardAnalyticsViewsDao;

	@Autowired
	private UtilDao utilDao;


	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Object> listVendors(Pagination pagination) {
		Map<String, Object> resultMap = new HashMap<>();
		List<UserDTO> userDTOsList = new ArrayList<>();
		Map<String, Object> map = vendorAnalyticsDAO.listVendors(pagination);
		List<Object[]> list = (List<Object[]>) map.get("data");
		Integer totalRecords = Integer.parseInt(map.get("totalRecords").toString());
		iterateListAndMapDtoObject(userDTOsList, list);
		resultMap.put("data", userDTOsList);
		resultMap.put("totalRecords", totalRecords);
		return resultMap;
	}

	private void iterateListAndMapDtoObject(List<UserDTO> userDTOsList, List<Object[]> list) {
		for (Object[] object : list) {
			UserDTO userDTO = new UserDTO();
			User user = (User) object[2];
			try {
				BeanUtils.copyProperties(userDTO, user);
			} catch (IllegalAccessException | InvocationTargetException e) {
			}
			userDTO.setId(user.getUserId());
			userDTO.setCompanyId(user.getCompanyProfile() != null ? user.getCompanyProfile().getId() : null);
			userDTO.setCompanyProfileName(
					user.getCompanyProfile() != null ? user.getCompanyProfile().getCompanyProfileName() : null);
			userDTO.setCompanyName(object[0] != null ? (String) object[0] : null);
			userDTO.setRoleId((Integer) object[1]);
			if (user.getSource() != null) {
				userDTO.setSource(user.getSource().name());
			}
			if (StringUtils.hasText(user.getAlias())) {
				userDTO.setAlias(user.getAlias());
			}
			userDTO.setUserStatus(user.getUserStatus().name());

			if (user.getCompanyProfile() != null) {
				userDTO.setEnableVanityURL(true);
			}

			userDTOsList.add(userDTO);
		}
	}

	@Override
	public Map<String, Object> listTop10Users() {
		List<UserDTO> datereg = vendorAnalyticsDAO.listTop10Users("datereg");
		List<UserDTO> datelastnav = vendorAnalyticsDAO.listTop10Users("datelastnav");
		Map<String, Object> map = new HashMap<>();
		map.put("datereg", datereg);
		map.put("datelastnav", datelastnav);
		return map;
	}

	@Override
	public Integer getVendorsCountByPartnerCompanyId(Integer partnerCompanyId) {
		return vendorAnalyticsDAO.getVendorsCountByPartnerCompanyId(partnerCompanyId);
	}

	@Override
	public UserDTO getUserDetailsByCompanyIdAndUserAlias(Integer companyId, String userAlias) {
		UserDTO userDto = vendorAnalyticsDAO.getUserDetailsByCompanyIdAndUserAlias(companyId, userAlias);
		Date mdfFormCreatedTime = formDao.getDefaultMdfFormCreatedDate(companyId);
		userDto.setDefaultMdfFormAvaible(mdfFormCreatedTime != null);
		if (mdfFormCreatedTime != null) {
			String dateInString = DateUtils.convertDateToStringWithOutSec(mdfFormCreatedTime);
			userDto.setMessage("Default MDF form is created on " + dateInString);
		} else {
			userDto.setMessage("Default MDF form is not available for this company");
		}
		return userDto;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Object> findAllVendors(Pagination pagination, Integer partnerId) {
		Map<String, Object> map = vendorAnalyticsDAO.findAllVendors(partnerId, pagination);
		List<VendorDTO> vendorDTOs = (List<VendorDTO>) map.get("data");
		for (VendorDTO vendorDTO : vendorDTOs) {
			vendorDTO.setCompanyLogo(serverPath + vendorDTO.getCompanyLogo());
		}
		return map;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Object> getVendors(Pagination pagination) {
		Map<String, Object> map = null;
		if (pagination != null && pagination.getPartnerId() != null) {
			VanityUrlDetailsDTO vanityUrlDetailsDTO = utilService.getVanityUrlFilteredData(pagination.getPartnerId(),
					pagination.isVanityUrlFilter(), utilDao.getPrmCompanyProfileName());
			/**** XNFR-252 ***/
			Integer loginAsUserId = pagination.getLoginAsUserId();
			boolean loginAsPartner = XamplifyUtils.isLoginAsPartner(loginAsUserId);
			if (vanityUrlDetailsDTO.isPartnerLoggedInThroughVanityUrl()) {
				pagination.setVendorCompanyId(vanityUrlDetailsDTO.getVendorCompanyId());
			} else if (loginAsPartner) {
				Integer loginAsUserCompanyId = userDao.getCompanyIdByUserId(loginAsUserId);
				pagination.setVendorCompanyId(loginAsUserCompanyId);
			}
			/**** XNFR-252 ***/
			map = vendorAnalyticsDAO.getVendors(pagination);
			List<VendorDTO> vendorDTOs = (List<VendorDTO>) map.get("data");
			for (VendorDTO vendorDTO : vendorDTOs) {
				vendorDTO.setCompanyLogo(serverPath + vendorDTO.getCompanyLogo());
				String completeImagePath = xamplifyUtil.getCompleteImagePath(vendorDTO.getVendorProfileImage());
				String displayName = utilService.getFullName(vendorDTO.getVendorFirstName(),
						vendorDTO.getVendorLastName());
				vendorDTO.setVendorProfileImage(completeImagePath);
				vendorDTO.setVendorDisplayName(displayName);

				completeImagePath = xamplifyUtil.getCompleteImagePath(vendorDTO.getPartnerProfileImage());
				displayName = utilService.getFullName(vendorDTO.getPartnerFirstName(), vendorDTO.getPartnerLastName());
				vendorDTO.setPartnerProfileImage(completeImagePath);
				vendorDTO.setPartnerDisplayName(displayName);

				completeImagePath = xamplifyUtil.getCompleteImagePath(vendorDTO.getVendorAdminProfileImage());
				displayName = utilService.getFullName(vendorDTO.getVendorAdminFirstName(),
						vendorDTO.getVendorAdminLastName());
				vendorDTO.setVendorAdminProfileImage(completeImagePath);
				vendorDTO.setVendorAdminDisplayName(displayName);

				completeImagePath = xamplifyUtil.getCompleteImagePath(vendorDTO.getPartnerAdminProfileImage());
				displayName = utilService.getFullName(vendorDTO.getPartnerAdminFirstName(),
						vendorDTO.getPartnerAdminLastName());
				vendorDTO.setPartnerAdminProfileImage(completeImagePath);
				vendorDTO.setPartnerAdminDisplayName(displayName);

				setCountsInVendorDTO(vendorDTO);

			}
		}
		return map;
	}

	private void setCountsInVendorDTO(VendorDTO vendorDTO) {

		List<MyVendorCountsDTO> myVendorCountsDTOs = new ArrayList<MyVendorCountsDTO>();

		if (!utilDao.isPrmByVendorCompanyId(vendorDTO.getCompanyId())) {
			MyVendorCountsDTO campaigns = new MyVendorCountsDTO();
			campaigns.setCount(vendorDTO.getCampaignsCount());
			campaigns.setName("Campaigns");
			myVendorCountsDTOs.add(campaigns);

			MyVendorCountsDTO pages = new MyVendorCountsDTO();
			pages.setCount(vendorDTO.getPagesCount());
			pages.setName("Pages");
			myVendorCountsDTOs.add(pages);
		}

		MyVendorCountsDTO playbooks = new MyVendorCountsDTO();
		playbooks.setCount(vendorDTO.getPlaybooksCount());
		playbooks.setName("Playbooks");
		myVendorCountsDTOs.add(playbooks);

		MyVendorCountsDTO assets = new MyVendorCountsDTO();
		assets.setCount(vendorDTO.getAssetsCount());
		assets.setName("Assets");
		myVendorCountsDTOs.add(assets);

		MyVendorCountsDTO tracks = new MyVendorCountsDTO();
		tracks.setCount(vendorDTO.getTracksCount());
		tracks.setName("Tracks");
		myVendorCountsDTOs.add(tracks);

		Collections.sort(myVendorCountsDTOs, Collections.reverseOrder());

		vendorDTO.setCounts(myVendorCountsDTOs);

	}

	@Override
	public XtremandResponse getVendorCount(VanityUrlDetailsDTO vanityUrlDetailsDTO) {
		XtremandResponse response = new XtremandResponse();
		String responseMessage = "Failed";
		Integer responseStatusCode = 400;
		response.setData(0);
		boolean isLoginAsPartner = XamplifyUtils.isLoginAsPartner(vanityUrlDetailsDTO.getLoginAsUserId());
		if (isLoginAsPartner) {
			response.setData(1);
			responseMessage = "SUCCESS";
			responseStatusCode = 200;
		} else {
			if (XamplifyUtils.isValidInteger(vanityUrlDetailsDTO.getUserId())) {
				vanityUrlDetailsDTO.setVanityUrlFilter(
						XamplifyUtils.isValidString(vanityUrlDetailsDTO.getVendorCompanyProfileName()));
				utilService.isVanityUrlFilterApplicable(vanityUrlDetailsDTO);
				if (vanityUrlDetailsDTO.isPartnerLoggedInThroughVanityUrl()) {
					response.setData(1);
				} else {
					DashboardModuleAnalyticsView dashboardAnalyticsView = dashboardAnalyticsViewsDao
							.getDashboardModuleViewByCompanyId(vanityUrlDetailsDTO);
					if (dashboardAnalyticsView != null && dashboardAnalyticsView.getVendors() != null) {
						response.setData(dashboardAnalyticsView.getVendors().intValue());
					}
				}
				responseMessage = "SUCCESS";
				responseStatusCode = 200;
			} else {
				responseMessage = "INVALID INPUT";
				responseStatusCode = 500;
			}

		}

		response.setMessage(responseMessage);
		response.setStatusCode(responseStatusCode);
		return response;
	}

}
