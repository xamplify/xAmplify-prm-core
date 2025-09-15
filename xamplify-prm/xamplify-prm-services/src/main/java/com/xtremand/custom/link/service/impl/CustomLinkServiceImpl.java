package com.xtremand.custom.link.service.impl;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartFile;

import com.custom.link.dto.CustomLinkRequestDTO;
import com.custom.link.dto.CustomLinkResponseDTO;
import com.xtremand.aws.AmazonWebModel;
import com.xtremand.aws.AmazonWebService;
import com.xtremand.common.bom.CompanyProfile;
import com.xtremand.common.bom.Pagination;
import com.xtremand.custom.link.bom.CustomLink;
import com.xtremand.custom.link.bom.CustomLinkType;
import com.xtremand.custom.link.dao.CustomLinkDao;
import com.xtremand.custom.link.service.CustomLinkService;
import com.xtremand.custom.link.validator.CustomLinkValidator;
import com.xtremand.dao.util.GenericDAO;
import com.xtremand.formbeans.XtremandResponse;
import com.xtremand.util.FileUtil;
import com.xtremand.util.XamplifyUtil;
import com.xtremand.util.XamplifyUtils;
import com.xtremand.util.dto.Pageable;
import com.xtremand.util.dto.XamplifyConstants;
import com.xtremand.util.dto.XamplifyUtilValidator;
import com.xtremand.util.service.UtilService;
import com.xtremand.vanity.url.dto.VanityUrlDetailsDTO;

@Service
@Transactional
public class CustomLinkServiceImpl implements CustomLinkService {

	@Autowired
	private CustomLinkDao customLinkDao;

	@Autowired
	private GenericDAO genericDao;

	@Autowired
	private CustomLinkValidator customLinkValidator;

	@Autowired
	private XamplifyUtilValidator xamplifyUtilValidator;

	@Autowired
	private UtilService utilService;

	@Autowired
	private FileUtil fileUtil;

	@Autowired
	private AmazonWebService amazonWebService;

	@Autowired
	private XamplifyUtil xamplifyUtil;
	
	@Value("${server_path}")
	String serverPath;

	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public XtremandResponse save(CustomLinkRequestDTO customLinkRequestDTO, BindingResult result,
			MultipartFile dashboardBannerImage) {
		XtremandResponse response = new XtremandResponse();
		customLinkValidator.validate(customLinkRequestDTO, result);
		if (result.hasErrors()) {
			xamplifyUtilValidator.addErrorResponse(result, response);
		} else {
			CustomLink customLink = new CustomLink();
			BeanUtils.copyProperties(customLinkRequestDTO, customLink);
			customLink.setSubTitle(customLink.getTitle());
			CompanyProfile companyProfile = new CompanyProfile();
			companyProfile.setId(customLinkRequestDTO.getCompanyId());
			customLink.setCompany(companyProfile);
			customLink.setCustomLinkType(customLinkRequestDTO.getCustomLinkType());
			customLink.setCreatedTime(new Date());
			customLink.setUpdatedTime(customLink.getCreatedTime());
			Integer loggedInUserId = customLinkRequestDTO.getLoggedInUserId();
			customLink.setCreatedUserId(loggedInUserId);
			customLink.setUpdatedUserId(loggedInUserId);
			String typeInString = getTypeInString(customLink);
			if (CustomLinkType.DASHBOARD_BANNERS.equals(customLink.getCustomLinkType())) {
				boolean isMaximumDashboardBannersUploaded = customLinkDao
						.isMaximumDashboardBannersUploaded(customLinkRequestDTO.getCompanyId());
				if (isMaximumDashboardBannersUploaded) {
					throw new AccessDeniedException(
							"You have reached the limit of displaying up to 7 dashboard banners. You cannot add any more banners.");
				}
			}
			genericDao.save(customLink);
			uploadDashboardBannerImage(customLinkRequestDTO, dashboardBannerImage, customLink);
			XamplifyUtils.addSuccessStatusWithMessage(response, typeInString + " created successfully");
		}
		return response;
	}

	private void uploadDashboardBannerImage(CustomLinkRequestDTO customLinkRequestDTO,
			MultipartFile dashboardBannerImage, CustomLink customLink) {
		if (CustomLinkType.DASHBOARD_BANNERS.equals(customLink.getCustomLinkType()) && dashboardBannerImage != null) {
			String updatedFileName = fileUtil
					.updateFileNameWithTimeStampAndRemoveSpecialCharacters(dashboardBannerImage.getOriginalFilename());
			String uploadedFilePath = fileUtil.uploadFileToXamplifyServerAndGetPath(dashboardBannerImage,
					XamplifyConstants.DASHBOARD_BANNERS, updatedFileName, customLink.getId());
			AmazonWebModel amazonWebModel = new AmazonWebModel();
			amazonWebModel.setFilePath(uploadedFilePath);
			amazonWebModel.setFileName(updatedFileName);
			amazonWebModel.setCategory(XamplifyConstants.DASHBOARD_BANNERS);
			amazonWebModel.setCompanyId(customLinkRequestDTO.getCompanyId());
			amazonWebModel.setId(customLink.getId());
			String dashboardBannerImagePath = amazonWebService.uploadFileToAWS(amazonWebModel);
			customLink.setBannerImagePath(dashboardBannerImagePath);
		}
	}


	private String getTypeInString(CustomLink customLink) {
		String typeInString = "";
		if (CustomLinkType.NEWS.equals(customLink.getCustomLinkType())) {
			typeInString = XamplifyConstants.NEWS_TEXT;
			customLink.setIcon("fa-newspaper-o");
		} else if (CustomLinkType.ANNOUNCEMENTS.equals(customLink.getCustomLinkType())) {
			typeInString = XamplifyConstants.ANNOUNCEMENT_TEXT;
			customLink.setIcon("fa-bullhorn");
		} else if (CustomLinkType.DASHBOARD_BANNERS.equals(customLink.getCustomLinkType())) {
			typeInString = XamplifyConstants.DASHBOARD_BANNERS_TEXT;
		}
		return typeInString;
	}

	@Override
	public XtremandResponse findAll(Pageable pageable, BindingResult result, Integer loggedInUserId, List<String> types,
			String vendorCompanyProfileName) {
		XtremandResponse response = new XtremandResponse();
		customLinkValidator.validatePagableParameters(pageable, result);
		if (result.hasErrors()) {
			xamplifyUtilValidator.addErrorResponse(result, response);
		} else {
			Pagination pagination = utilService.setPageableParameters(pageable, loggedInUserId);
			VanityUrlDetailsDTO vanityUrlDetailsDTO = new VanityUrlDetailsDTO();
			vanityUrlDetailsDTO.setVanityUrlFilter(true);
			vanityUrlDetailsDTO.setUserId(loggedInUserId);
			vanityUrlDetailsDTO.setVendorCompanyProfileName(vendorCompanyProfileName);
			utilService.isVanityUrlFilterApplicable(vanityUrlDetailsDTO);
			if (vanityUrlDetailsDTO.isPartnerLoggedInThroughVanityUrl()) {
				pagination.setCompanyId(vanityUrlDetailsDTO.getVendorCompanyId());
			} else if (vanityUrlDetailsDTO.isVendorLoggedInThroughOwnVanityUrl()) {
				pagination.setCompanyId(vanityUrlDetailsDTO.getLoggedInUserCompanyId());
			}
			Map<String, Object> map = customLinkDao.findAll(pagination, pageable.getSearch(), types);
			 @SuppressWarnings("unchecked")
		        List<CustomLinkResponseDTO> bannerList = (List<CustomLinkResponseDTO>) map.get("list");
		        bannerList.forEach(item -> {
		            if (XamplifyUtils.isValidString(item.getBannerImagePath())) {
		                String updatedCdnUrl = xamplifyUtil.replaceS3WithCloudfrontViceVersa(item.getBannerImagePath());
		                item.setCdnBannerImagePath(updatedCdnUrl);
		            }
		        });

		        map.put("list", bannerList); 
			
			response.setData(map);
			XamplifyUtils.addSuccessStatus(response);
		}
		return response;
	}

	@Override
	public XtremandResponse getById(Integer id, Integer loggedInUserId) {
		XtremandResponse response = new XtremandResponse();
		customLinkValidator.validateId(id, loggedInUserId, false);
		response.setData(customLinkDao.findById(id));
		XamplifyUtils.addSuccessStatus(response);
		return response;
	}

	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public XtremandResponse update(CustomLinkRequestDTO customLinkRequestDto, BindingResult result,
			MultipartFile dashboardBannerImage) {
		XtremandResponse response = new XtremandResponse();
		Integer id = customLinkRequestDto.getId();
		try {
			customLinkValidator.validateCustomLinkPropertiesForUpdateRequest(customLinkRequestDto, result);
		} catch (AccessDeniedException a) {
			throw new AccessDeniedException(a.getMessage());
		}
		if (result.hasErrors()) {
			xamplifyUtilValidator.addErrorResponse(result, response);
		} else {
			CustomLink customLink = genericDao.get(CustomLink.class, id);
			customLink.setTitle(customLinkRequestDto.getTitle());
			customLink.setIcon(customLinkRequestDto.getIcon());
			customLink.setDescription(customLinkRequestDto.getDescription());
			customLink.setLink(customLinkRequestDto.getLink());
			customLink.setUpdatedTime(new Date());
			customLink.setOpenLinkInNewTab(customLinkRequestDto.isOpenLinkInNewTab());
			customLink.setUpdatedUserId(customLinkRequestDto.getLoggedInUserId());
			customLink.setDisplayTitle(customLinkRequestDto.isDisplayTitle());
			customLink.setButtonText(customLinkRequestDto.getButtonText());
			String typeInString = getTypeInString(customLink);
			uploadDashboardBannerImage(customLinkRequestDto, dashboardBannerImage, customLink);
			XamplifyUtils.addSuccessStatusWithMessage(response, typeInString + " updated successfully");
		}
		return response;
	}

	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public XtremandResponse delete(Integer id, Integer loggedInUserId) {
		XtremandResponse response = new XtremandResponse();
		customLinkValidator.validateId(id, loggedInUserId, true);
		customLinkDao.delete(id);
		XamplifyUtils.addSuccessStatusWithMessage(response, "Deleted successfully");
		return response;
	}

}
