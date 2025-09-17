package com.xtremand.custom.css.service.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.HibernateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.xtremand.category.dao.CategoryDao;
import com.xtremand.common.bom.CompanyProfile;
import com.xtremand.custom.css.bom.CompanyThemeActive;
import com.xtremand.custom.css.bom.CustomModule;
import com.xtremand.custom.css.bom.CustomSkin;
import com.xtremand.custom.css.bom.Theme;
import com.xtremand.custom.css.bom.Theme.ThemeStatus;
import com.xtremand.custom.css.bom.ThemeProperties;
import com.xtremand.custom.css.dao.hibernate.HibernateCustomSkinDao;
import com.xtremand.custom.css.dto.CompanyThemeActiveDTO;
import com.xtremand.custom.css.dto.CustomSkinDTO;
import com.xtremand.custom.css.dto.ThemeDTO;
import com.xtremand.custom.css.dto.ThemePropertiesDTO;
import com.xtremand.custom.css.dto.ThemeThemePropertiesListWrapper;
import com.xtremand.custom.css.service.CustomSkinService;
import com.xtremand.dao.util.GenericDAO;
import com.xtremand.formbeans.XtremandResponse;
import com.xtremand.user.dao.UserDAO;
import com.xtremand.user.exception.UserDataAccessException;
import com.xtremand.util.XamplifyUtils;
import com.xtremand.vanity.url.dto.VanityUrlDetailsDTO;

@Service
@Transactional
public class CustomSkinServiceImpl implements CustomSkinService {
	private static final Logger logger = LoggerFactory.getLogger(CustomSkinServiceImpl.class);

	@Value("${separator}")
	String sep;

	@Value("${specialCharacters}")
	String regex;

	@Value("${images_path}")
	String images_path;

	@Autowired
	private GenericDAO genericDao;

	@Autowired
	private UserDAO userDao;

	@Autowired
	private HibernateCustomSkinDao hibernateCustomSkinDao;

	@Autowired
	private CategoryDao categoryDao;

	@Override
	public XtremandResponse save(CustomSkinDTO customSkinDTO) {
		XtremandResponse response = new XtremandResponse();
		Integer companyId = userDao.getCompanyIdByUserId(customSkinDTO.getCreatedBy());
		CustomSkin customSkinExstingUser = hibernateCustomSkinDao.findByType(companyId,
				customSkinDTO.getModuleTypeString());
		CustomSkin customSkin = new CustomSkin();

		if (customSkinExstingUser != null) {
			CustomSkinDTO customSkinDTOExstingUser = hibernateCustomSkinDao.findById(customSkinExstingUser.getId());
			if (customSkinDTOExstingUser.getId() != null) {
				Integer id = customSkinExstingUser.getId();
				customSkinDTO.setId(id);
				hibernateCustomSkinDao.updateCustomSkin(customSkinDTO, id);
			}
		} else {
			customSkin.setBackgroundColor(customSkinDTO.getBackgroundColor());
			customSkin.setButtonColor(customSkinDTO.getButtonColor());
			customSkin.setButtonValueColor(customSkinDTO.getButtonValueColor());
			customSkin.setTextColor(customSkinDTO.getTextColor());
			String textContent = customSkinDTO.getTextContent();
			if (StringUtils.hasText(textContent)) {
				if (textContent.length() > 225) {
					customSkin.setTextContent((textContent).substring(0, textContent.length() - 1));
				} else {
					customSkin.setTextContent(textContent);
				}
			}
			customSkin.setIconColor(customSkinDTO.getIconColor());
			customSkin.setIconBorderColor(customSkinDTO.getIconBorderColor());
			customSkin.setIconHoverColor(customSkinDTO.getIconHoverColor());
			customSkin.setButtonBorderColor(customSkinDTO.getButtonBorderColor());
			customSkin.setFontFamily(customSkinDTO.getFontFamily());
			customSkin.setModuleType(CustomModule.valueOf(customSkinDTO.getModuleTypeString()));
			// CompanyProfile comapnyName = userDao.getCompanyNameByCompanyId(companyId);
			// CompanyProfile companyProfile =
			// userDao.getCompanyProfileByCompanyName(comapnyName.getCompanyName());
			CompanyProfile companyProfile = genericDao.get(CompanyProfile.class, companyId);
			customSkin.setCompanyProfile(companyProfile);
			customSkin.setCreatedUserId(customSkinDTO.getCreatedBy());
			customSkin.setUpdatedUserId(customSkinDTO.getUpdatedBy());
			customSkin.setCreatedTime(new Date());
			customSkin.setUpdatedTime(new Date());
			customSkin.setBackgroundColor(customSkinDTO.getBackgroundColor());
			customSkin.setDefaultSkin(customSkinDTO.isDefaultSkin());
			customSkin.setShowFooter(customSkinDTO.isShowFooter());
			customSkin.setDivBgColor(customSkinDTO.getDivBgColor());
			// customSkin.setDarkTheme(customSkinDTO.isDarkTheme());
			genericDao.save(customSkin);
			response.setData(customSkin);
			response.setStatusCode(200);
		}
		return response;
	}

	@Override
	public XtremandResponse findByType(Integer userId, String type) {
		XtremandResponse response = new XtremandResponse();
		CustomSkin customSkin = new CustomSkin();
		Integer companyId = userDao.getCompanyIdByUserId(userId);
		customSkin = hibernateCustomSkinDao.findByType(companyId, type);
		setCustomDTO(customSkin, new CustomSkinDTO(), response);
		response.setStatusCode(200);
		return response;
	}

	/** XNFR-420 **/
	@Override
	public XtremandResponse getEnumTypeByParentId1(Integer parentId) {
		XtremandResponse response = new XtremandResponse();
		response.setData(hibernateCustomSkinDao.getEnumType(parentId));
		response.setStatusCode(200);
		return response;
	}

	private void setCustomDTO(CustomSkin customSkin, CustomSkinDTO customSkinDTO, XtremandResponse response) {
		if (customSkin != null) {
			customSkinDTO.setId(customSkin.getId());
			customSkinDTO.setBackgroundColor(customSkin.getBackgroundColor());
			customSkinDTO.setButtonColor(customSkin.getButtonColor());
			customSkinDTO.setButtonValueColor(customSkin.getButtonValueColor());
			customSkinDTO.setCompanyId(customSkin.getCompanyProfile().getId());
			customSkinDTO.setCreatedBy(customSkin.getCreatedUserId());
			customSkinDTO.setUpdatedBy(customSkin.getUpdatedUserId());
			customSkinDTO.setCreatedDate(customSkin.getCreatedTime());
			customSkinDTO.setUpdatedDate(customSkin.getUpdatedTime());
			customSkinDTO.setTextColor(customSkin.getTextColor());
			customSkinDTO.setIconColor(customSkin.getIconColor());
			customSkinDTO.setButtonBorderColor(customSkin.getButtonBorderColor());
			customSkinDTO.setFontFamily(customSkin.getFontFamily());
			customSkinDTO.setModuleTypeString(customSkin.getModuleType().name());
			customSkinDTO.setDefaultSkin(customSkin.isDefaultSkin());
			customSkinDTO.setShowFooter(customSkin.isShowFooter());
			customSkinDTO.setDivBgColor(customSkin.getDivBgColor());
			// customSkinDTO.setDarkTheme(customSkin.isDarkTheme());
			response.setStatusCode(200);
			response.setData(customSkinDTO);
		}
	}

	@Override
	public XtremandResponse findByCompanyId(VanityUrlDetailsDTO vanityUrlDetailsDTO) {
		XtremandResponse response = new XtremandResponse();
		Integer companyId;
		if (vanityUrlDetailsDTO.isVanityUrlFilter()
				&& StringUtils.hasText(vanityUrlDetailsDTO.getVendorCompanyProfileName())) {
			companyId = userDao.getCompanyIdByProfileName(vanityUrlDetailsDTO.getVendorCompanyProfileName());
		} else {
			companyId = userDao.getCompanyIdByUserId(vanityUrlDetailsDTO.getUserId());
		}
		Map<String, Object> map = new HashMap<String, Object>();
		CustomSkinDTO left = new CustomSkinDTO();
		CustomSkin leftSkin = new CustomSkin();
		CustomSkin topSkin = new CustomSkin();
		CustomSkinDTO top = new CustomSkinDTO();
		CustomSkin footer = new CustomSkin();
		CustomSkinDTO footerDto = new CustomSkinDTO();
		CustomSkin mainContent = new CustomSkin();
		CustomSkinDTO mainContentDto = new CustomSkinDTO();
		leftSkin = hibernateCustomSkinDao.findByType(companyId, CustomModule.LEFT_SIDE_MENU.name());
		if (leftSkin == null) {
			left = hibernateCustomSkinDao.getDefaultSkin(1, CustomModule.LEFT_SIDE_MENU.getModuleName(), true);
		} else {
			left = hibernateCustomSkinDao.findById(leftSkin.getId());
		}
		topSkin = hibernateCustomSkinDao.findByType(companyId, CustomModule.TOP_NAVIGATION_BAR.name());
		if (topSkin == null) {
			top = hibernateCustomSkinDao.getDefaultSkin(1, CustomModule.TOP_NAVIGATION_BAR.getModuleName(), true);
		} else {
			top = hibernateCustomSkinDao.findById(topSkin.getId());
		}

		footer = hibernateCustomSkinDao.findByType(companyId, CustomModule.FOOTER.name());
		if (footer == null) {
			footerDto = hibernateCustomSkinDao.getDefaultSkin(1, CustomModule.FOOTER.getModuleName(), true);
		} else {
			footerDto = hibernateCustomSkinDao.findById(footer.getId());

		}
		mainContent = hibernateCustomSkinDao.findByType(companyId, CustomModule.MAIN_CONTENT.name());
		if (mainContent == null) {
			mainContentDto = hibernateCustomSkinDao.getDefaultSkin(1, CustomModule.MAIN_CONTENT.getModuleName(), true);
		} else {
			mainContentDto = hibernateCustomSkinDao.findById(mainContent.getId());

		}
		map.put("TOP_NAVIGATION_BAR", top);
		map.put("LEFT_SIDE_MENU", left);
		map.put("FOOTER", footerDto);
		map.put("MAIN_CONTENT", mainContentDto);
		response.setData(map);
		response.setStatusCode(200);
		return response;

	}

	@Override
	public XtremandResponse findById(Integer id) {
		XtremandResponse response = new XtremandResponse();
		response.setData(hibernateCustomSkinDao.findById(id));
		response.setStatusCode(200);
		return response;
	}

	@Override
	public XtremandResponse findDefaultSkin(Integer userId) {
		XtremandResponse response = new XtremandResponse();
		Integer companyId = userDao.getCompanyIdByUserId(userId);
		response.setData(
				hibernateCustomSkinDao.getDefaultSkin(companyId, CustomModule.TOP_NAVIGATION_BAR.name(), true));
		response.setStatusCode(200);
		return response;
	}

	@Override
	public XtremandResponse updateDarkTheme(CustomSkinDTO customSkinDTO) {
		XtremandResponse response = new XtremandResponse();
		Integer companyId = userDao.getCompanyIdByUserId(customSkinDTO.getCreatedBy());
		List<CustomSkinDTO> listDtos = hibernateCustomSkinDao.findByCompanyId(companyId);
		if (!listDtos.isEmpty()) {
			response.setData(hibernateCustomSkinDao.updateDarkTheme(customSkinDTO.isDarkTheme(),
					customSkinDTO.isDefaultSkin(), companyId));
		} else {
			listDtos = hibernateCustomSkinDao.findByCompanyId(1);
			for (CustomSkinDTO dto : listDtos) {
				dto.setDarkTheme(customSkinDTO.isDarkTheme());
				dto.setDefaultSkin(customSkinDTO.isDefaultSkin());
				dto.setCompanyId(companyId);
				dto.setCreatedBy(customSkinDTO.getCreatedBy());
				response.setData(save(dto));
			}
		}
		response.setStatusCode(200);
		return response;
	}

	@Override
	public XtremandResponse updateCustomTheme(CustomSkinDTO customSkinDTO) {
		XtremandResponse response = new XtremandResponse();
		Integer companyId = userDao.getCompanyIdByUserId(customSkinDTO.getCreatedBy());
		List<CustomSkinDTO> listDtos = hibernateCustomSkinDao.findByCompanyId(companyId);
		if (!listDtos.isEmpty()) {
			response.setData(hibernateCustomSkinDao.updateCustomTheme(customSkinDTO.isDarkTheme(), companyId));
		} else {
			listDtos = hibernateCustomSkinDao.findByCompanyId(1);
			for (CustomSkinDTO dto : listDtos) {
				dto.setDarkTheme(customSkinDTO.isDarkTheme());
				dto.setCompanyId(companyId);
				dto.setCreatedBy(customSkinDTO.getCreatedBy());
				response.setData(save(dto));
			}
		}
		response.setStatusCode(200);
		return response;
	}

	@Override
	public XtremandResponse updateUserDefaultSettings(CustomSkinDTO customSkinDTO) {
		XtremandResponse response = new XtremandResponse();
		response.setStatusCode(200);
		response.setData(hibernateCustomSkinDao.updateDefaultSettings(customSkinDTO));
		return response;
	}

	/*************** new Changes(26-04) ************/

	@Override
	public XtremandResponse saveTheme(ThemeThemePropertiesListWrapper themeThemePropertiesListWrapper) {
		XtremandResponse response = new XtremandResponse();
		ThemeDTO themeDto = themeThemePropertiesListWrapper.getThemeDto();
		Theme themes = new Theme();
		Integer loggedCompanyId = userDao.getCompanyIdByUserId(themeDto.getCreatedBy());
		boolean isExists = hibernateCustomSkinDao.checkDuplicateName(themeDto.getName(), loggedCompanyId);
		if (themeDto.getName() != null) {
			if (!isExists) {
				themes.setName(themeDto.getName());
				themes.setDescription(themeDto.getDescription());
				themes.setDefaultTheme(themeDto.isDefaultTheme());
				themes.setParentId(themeDto.getParentId());
				themes.setParentThemeName(themeDto.getParentThemeName());
				String imageCopyThemeImagePath = customThemeImagePath(themeDto.getParentThemeName());
				themes.setThemeImagePath(imageCopyThemeImagePath);
				Integer companyId = userDao.getCompanyIdByUserId(themeDto.getCreatedBy());
				CompanyProfile companyProfile = genericDao.get(CompanyProfile.class, companyId);
				themes.setCompanyProfile(companyProfile);
				themes.setBackgroundImage(themeDto.getBackgroundImagePath());
				themes.setCreatedUserId(themeDto.getCreatedBy());
				themes.setUpdatedUserId(themeDto.getCreatedBy());
				themes.setCreatedTime(new Date());
				themes.setUpdatedTime(new Date());
				genericDao.save(themes);
				Set<ThemeProperties> propertiesList = new HashSet<ThemeProperties>();

				for (ThemePropertiesDTO td : themeThemePropertiesListWrapper.getPropertiesList()) {
					ThemeProperties tp = new ThemeProperties();
					tp.setModuleType(CustomModule.valueOf(td.getModuleTypeString()));
					tp.setBackgroundColor(td.getBackgroundColor());
					tp.setDivBgColor(td.getDivBgColor());
					tp.setTableHeaderColor(td.getTableHeaderColor());
					tp.setTableBodyColor(td.getTableBodyColor());
					tp.setTextColor(td.getTextColor());
					tp.setButtonBorderColor(td.getButtonBorderColor());
					tp.setButtonColor(td.getButtonColor());
					tp.setButtonValueColor(td.getButtonValueColor());
					tp.setButtonPrimaryBorderColor(td.getButtonPrimaryBorderColor());
					tp.setButtonSecondaryBorderColor(td.getButtonSecondaryBorderColor());
					tp.setButtonSecondaryColor(td.getButtonSecondaryColor());
					tp.setButtonSecondaryTextColor(td.getButtonSecondaryTextColor());
					tp.setTextContent(td.getTextContent());
					tp.setIconColor(td.getIconColor());
					tp.setIconBorderColor(td.getIconBorderColor());
					tp.setIconHoverColor(td.getIconHoverColor());
					tp.setGradiantColorOne(td.getGradiantColorOne());
					tp.setGradiantColorTwo(td.getGradiantColorTwo());
					tp.setShowFooter(td.isShowFooter());
					tp.setCreatedUserId(td.getCreatedBy());
					tp.setUpdatedUserId(td.getCreatedBy());
					tp.setCreatedTime(new Date());
					tp.setUpdatedTime(new Date());
					tp.setTheme(themes);
					propertiesList.add(tp);
					genericDao.save(tp);
				}
				response.setData(themes);
				response.setStatusCode(200);
				response.setMessage("Successfully Created Theme.");
			} else {
				response.setStatusCode(409);
				response.setMessage("Name Already Exists.");
			}
		} else {
			response.setStatusCode(402);
			response.setMessage("Theme Name is Required.");
		}

		return response;
	}

	private String customThemeImagePath(ThemeStatus parentThemeName) {
		String imagePath = "";
		if (ThemeStatus.LIGHT == parentThemeName) {
			imagePath = "assets/images/theme/Final/light-theme-custom.webp";
		} else if (ThemeStatus.DARK == parentThemeName) {
			imagePath = "assets/images/theme/Final/dark-theme-custom.webp";
		} else if (ThemeStatus.NEUMORPHISMLIGHT == parentThemeName) {
			imagePath = "assets/images/theme/Final/Custom_Neumorphism_Light.webp";
		} else if (ThemeStatus.NEUMORPHISMDARK == parentThemeName) {
			imagePath = "assets/images/theme/Final/Custom_Neumorphism_Dark.webp";
		} else if (ThemeStatus.GLASSMORPHISMLIGHT == parentThemeName) {
			imagePath = "assets/images/theme/Final/Custom_Glassomorphism_Light.webp";
		} else if (ThemeStatus.GLASSMORPHISMDARK == parentThemeName) {
			imagePath = "assets/images/theme/Final/Custom_Glassomorphism_Dark.webp";
		}
		return imagePath;
	}

	@Override
	public XtremandResponse getThemesByUserId(Integer userId) {
		XtremandResponse response = new XtremandResponse();
		Integer companyId = userDao.getCompanyIdByUserId(userId);
		List<ThemeDTO> themes = hibernateCustomSkinDao.getThemesByCompanyId(companyId);
		response.setData(themes);
		response.setStatusCode(200);
		response.setMessage("Theme Retrived Successfully..");
		return response;
	}

	@Override
	public XtremandResponse getThemeIdByThemeName(String themeName) {
		XtremandResponse response = new XtremandResponse();
		Integer themeId = hibernateCustomSkinDao.getThemeIdByThemeName(themeName);
		response.setData(themeId);
		response.setStatusCode(200);
		response.setMessage("Theme ID was :" + themeId);
		return response;
	}

	@Override
	public XtremandResponse getThemesPropertiesByThemeName(String themeName) {
		XtremandResponse response = new XtremandResponse();
		Integer themeId = hibernateCustomSkinDao.getThemeIdByThemeName(themeName);
		List<ThemePropertiesDTO> themesProperties = hibernateCustomSkinDao.getThemesPropertiesByThemeId(themeId);
		response.setData(themesProperties);
		response.setStatusCode(200);
		response.setMessage("Successfully Fetced Data.");
		return response;
	}

	@Override
	public XtremandResponse setThemeForCompany(CompanyThemeActiveDTO activeDTO) {
		XtremandResponse response = new XtremandResponse();
		CompanyThemeActive cTA = new CompanyThemeActive();
		Integer loggedInUserId = activeDTO.getCreatedBy();
		Integer companyId = userDao.getCompanyIdByUserId(loggedInUserId);
		Integer company_id = null;
		if (loggedInUserId != null && loggedInUserId > 0 && companyId != null && companyId > 0) {
			company_id = companyId;
		} else {
			company_id = 1;
		}
		CompanyProfile companyProfile = genericDao.get(CompanyProfile.class, companyId);
		CompanyThemeActiveDTO activeDto = hibernateCustomSkinDao.getActivateTheme(companyId);
		if (activeDto != null) {
			if (activeDto.getId() != null) {
				Integer activeId = activeDto.getId();
				hibernateCustomSkinDao.updateCompanyActivateTheme(activeDTO, activeId);
				response.setMessage("Theme Updated Successfully.");
			}
		} else {
			Theme theme2 = hibernateCustomSkinDao.getThemeById(activeDTO.getThemeId());
			cTA.setTheme(theme2);
			cTA.setCreatedUserId(loggedInUserId);
			cTA.setUpdatedUserId(loggedInUserId);
			cTA.setCreatedTime(new Date());
			cTA.setUpdatedTime(new Date());
			cTA.setCompanyProfile(companyProfile);
			response.setData(genericDao.save(cTA));
			response.setMessage("Theme Activated Successfully.");
		}
		response.setStatusCode(200);
		return response;
	}

	@Override
	public XtremandResponse deleteThemeById(Integer id) {
		XtremandResponse response = new XtremandResponse();
		String message = null;
		Integer statusCode = null;
		boolean result = hibernateCustomSkinDao.checkThemeIdExsitOrNot(id);
		if (result) {
			Theme theme = hibernateCustomSkinDao.getThemeById(id);
			if (theme.getId() != null) {
				boolean isExists = hibernateCustomSkinDao.checkActiveTheme(id);
				if (isExists) {
					message = "You Can't delet this theme.";
					statusCode = 405;
				} else {
					message = hibernateCustomSkinDao.deleteThemeById(id);
					statusCode = 200;
				}
			}
		} else {
			statusCode = 404;
			message = "Record Not Found";
		}
		response.setData(message);
		response.setStatusCode(statusCode);
		response.setMessage(message);
		return response;
	}

	@Override
	public XtremandResponse getThemesPropertiesByThemeIdAndModuleName(Integer themeId) {
		XtremandResponse response = new XtremandResponse();
		Map<String, Object> map = new HashMap<>();
		ThemeProperties left = new ThemeProperties();
		ThemePropertiesDTO leftSkinDto = new ThemePropertiesDTO();
		ThemeProperties topSkin = new ThemeProperties();
		ThemePropertiesDTO topDto = new ThemePropertiesDTO();
		ThemeProperties footer = new ThemeProperties();
		ThemePropertiesDTO footerDto = new ThemePropertiesDTO();
		ThemeProperties mainContent = new ThemeProperties();
		ThemePropertiesDTO mainContentDto = new ThemePropertiesDTO();
		ThemePropertiesDTO buttonCustomizationDto = new ThemePropertiesDTO();
		ThemeProperties buttonCustomization = new ThemeProperties();
		left = hibernateCustomSkinDao.getThemesPropertiesByThemeIdAndModuleName(themeId,
				CustomModule.LEFT_SIDE_MENU.name());
		if (left != null) {
			leftSkinDto = hibernateCustomSkinDao.getThemesPropertiesByThemeIdAndModuleName(left.getId());
		}
		topSkin = hibernateCustomSkinDao.getThemesPropertiesByThemeIdAndModuleName(themeId,
				CustomModule.TOP_NAVIGATION_BAR.name());

		if (topSkin != null) {
			topDto = hibernateCustomSkinDao.getThemesPropertiesByThemeIdAndModuleName(topSkin.getId());
		}

		footer = hibernateCustomSkinDao.getThemesPropertiesByThemeIdAndModuleName(themeId, CustomModule.FOOTER.name());
		if (footer != null) {
			footerDto = hibernateCustomSkinDao.getThemesPropertiesByThemeIdAndModuleName(footer.getId());
		}

		mainContent = hibernateCustomSkinDao.getThemesPropertiesByThemeIdAndModuleName(themeId,
				CustomModule.MAIN_CONTENT.name());
		if (mainContent != null) {
			mainContentDto = hibernateCustomSkinDao.getThemesPropertiesByThemeIdAndModuleName(mainContent.getId());
		}

		buttonCustomization = hibernateCustomSkinDao.getThemesPropertiesByThemeIdAndModuleName(themeId,
				CustomModule.BUTTON_CUSTOMIZE.name());
		if (buttonCustomization != null) {
			buttonCustomizationDto = hibernateCustomSkinDao
					.getThemesPropertiesByThemeIdAndModuleName(buttonCustomization.getId());
		}
		map.put("TOP_NAVIGATION_BAR", topDto);
		map.put("LEFT_SIDE_MENU", leftSkinDto);
		map.put("FOOTER", footerDto);
		map.put("MAIN_CONTENT", mainContentDto);
		map.put("BUTTON_CUSTOMIZE", buttonCustomizationDto);
		response.setData(map);
		response.setStatusCode(200);
		return response;
	}

	@Override
	public XtremandResponse getAtivatedTheme(VanityUrlDetailsDTO vanityUrlDetailsDTO) {
		XtremandResponse response = new XtremandResponse();
		CompanyThemeActiveDTO activeDto = getActiveThemeID(vanityUrlDetailsDTO);
		response.setData(activeDto);
		response.setStatusCode(200);
		return response;
	}

	public CompanyThemeActiveDTO getActiveThemeID(VanityUrlDetailsDTO vanityUrlDetailsDTO) {
		Integer companyId;
		if (vanityUrlDetailsDTO.isVanityUrlFilter()
				&& StringUtils.hasText(vanityUrlDetailsDTO.getVendorCompanyProfileName())) {
			companyId = userDao.getCompanyIdByProfileName(vanityUrlDetailsDTO.getVendorCompanyProfileName());
		} else if (vanityUrlDetailsDTO.getUserId() != null) {
			companyId = userDao.getCompanyIdByUserId(vanityUrlDetailsDTO.getUserId());
		} else {
			companyId = null;
		}
		CompanyThemeActiveDTO activeDto = new CompanyThemeActiveDTO();
		Integer loggedInUserCompanyId = 0;
		if (companyId != null && companyId > 0) {
			loggedInUserCompanyId = companyId;
		}
		boolean result = hibernateCustomSkinDao.checkActiveThemeByCompanyId(loggedInUserCompanyId);
		if (result) {
			activeDto = hibernateCustomSkinDao.getActivateTheme(companyId);
		} else {
			activeDto.setCompanyId(companyId);
			activeDto.setThemeId(1);
			activeDto.setUpdatedDate(new Date());
		}
		return activeDto;
	}

//	@Override
//	public XtremandResponse updateActivateTheme(ThemePropertiesDTO dto ,Integer id) {
//		XtremandResponse response = new XtremandResponse();
//
//		int result1 = hibernateCustomSkinDao.updateCompanyActivateTheme(dto, id);
//		response.setData(result1);
//		response.setStatusCode(200);
//		return response;
//		
//	}

	@Override
	public XtremandResponse updateProperties(Integer themeId, String moduleName,
			ThemePropertiesDTO themePropertiesDto) {
		XtremandResponse response = new XtremandResponse();
		ThemeProperties themeProperties = new ThemeProperties();

		themeProperties = hibernateCustomSkinDao.getThemesPropertiesByThemeIdAndModuleName(themeId, moduleName);
		hibernateCustomSkinDao.updateThemeProperties(themePropertiesDto, themeProperties.getId());

		response.setStatusCode(200);
		response.setMessage("updated sucessfully");
		return response;
	}

	@Override
	public XtremandResponse getThemeDTOById(Integer id) {
		XtremandResponse response = new XtremandResponse();
		ThemeDTO themeDto = hibernateCustomSkinDao.getThemeDTOById(id);
		response.setData(themeDto);
		response.setStatusCode(200);
		response.setMessage("updated sucessfully");
		return response;
	}

	@Override
	public XtremandResponse getThemeNames(Integer userId) {
		XtremandResponse response = new XtremandResponse();
		Integer loggedCompanyId = userDao.getCompanyIdByUserId(userId);

		List<String> names = hibernateCustomSkinDao.getThemeNames(loggedCompanyId);
		response.setData(names);
		response.setStatusCode(200);
		return response;
	}

	@Override
	public XtremandResponse updateThemeByID(Integer themeId, ThemeDTO themeDto) {
		XtremandResponse response = new XtremandResponse();

		if (themeDto != null && themeId != null && themeId > 4 && !themeDto.isDefaultTheme()) {
			if (themeDto.getName() != null) {
				hibernateCustomSkinDao.updateThemeById(themeDto, themeId);

				for (ThemePropertiesDTO td : themeDto.getThemesProperties()) {
					ThemeProperties themeProperties = new ThemeProperties();

					themeProperties = hibernateCustomSkinDao.getThemesPropertiesByThemeIdAndModuleName(themeId,
							td.getModuleTypeString());
					hibernateCustomSkinDao.updateThemeProperties(td, themeProperties.getId());

				}
				response.setMessage("Updated Sucessfully.");
				response.setStatusCode(200);
			} else {
				response.setMessage("Theme Name is Required.");
				response.setStatusCode(402);
			}

		} else {
			response.setMessage("Update Failed");
			response.setStatusCode(400);
		}

		return response;
	}

	@Override
	public XtremandResponse getThemesByCompanyIdOne() {
		XtremandResponse response = new XtremandResponse();
		List<ThemeDTO> themeDtoByOne = hibernateCustomSkinDao.getThemesByCompanyIdOne();
		response.setData(themeDtoByOne);
		response.setStatusCode(200);
		return response;
	}

	@Override
	public XtremandResponse getCompanyIdsInCustomSkinTable() {
		XtremandResponse response = new XtremandResponse();
		List<Integer> companyIdsList = hibernateCustomSkinDao.getCompanyIdsInCustomSkinTable();
		response.setData(companyIdsList);
		response.setStatusCode(200);
		return response;
	}

	/**
	 * Migrating Custom SKin Table Data Into Theme ,ThemeProperties and
	 * CompanyThemeActive Tables
	 **/
	@Override
	public XtremandResponse insertDataIntoThemeAndThemePropertiesTable() {
		XtremandResponse response = new XtremandResponse();
		List<Integer> companyIdsList = hibernateCustomSkinDao.getCompanyIdsInCustomSkinTable();
		Integer count = 0;
		Integer totalCompanies = companyIdsList.size();
		for (Integer companyId : companyIdsList) {
			List<ThemePropertiesDTO> themePropertiesDTOs = new ArrayList<ThemePropertiesDTO>();
			ThemePropertiesDTO topPropertiesDto = new ThemePropertiesDTO();
			ThemePropertiesDTO leftPropertiesDto = new ThemePropertiesDTO();
			ThemePropertiesDTO footerPropertiesDto = new ThemePropertiesDTO();
			ThemePropertiesDTO mainPropertiesDto = new ThemePropertiesDTO();
			topPropertiesDto.setModuleTypeString(CustomModule.TOP_NAVIGATION_BAR.name());
			leftPropertiesDto.setModuleTypeString(CustomModule.LEFT_SIDE_MENU.name());
			footerPropertiesDto.setModuleTypeString(CustomModule.FOOTER.name());
			mainPropertiesDto.setModuleTypeString(CustomModule.MAIN_CONTENT.name());
			Integer loggedUserId = categoryDao.getUserIdByCompanyId(companyId);
			/** TOPNAVIGATION **/
			CustomSkin topCustomSkin = hibernateCustomSkinDao.findByType(companyId,
					CustomModule.TOP_NAVIGATION_BAR.name());
			CustomSkin leftCustomSkin = hibernateCustomSkinDao.findByType(companyId,
					CustomModule.LEFT_SIDE_MENU.name());
			CustomSkin footerCustomSkin = hibernateCustomSkinDao.findByType(companyId, CustomModule.FOOTER.name());
			CustomSkin mainCustomSkin = hibernateCustomSkinDao.findByType(companyId, CustomModule.MAIN_CONTENT.name());

			if (topCustomSkin != null) {
				topPropertiesDto.setBackgroundColor(topCustomSkin.getBackgroundColor());
				topPropertiesDto.setButtonBorderColor(topCustomSkin.getButtonBorderColor());
				topPropertiesDto.setButtonColor(topCustomSkin.getButtonColor());
				topPropertiesDto.setButtonValueColor(topCustomSkin.getButtonValueColor());
				topPropertiesDto.setIconColor(topCustomSkin.getIconColor());
				themePropertiesDTOs.add(topPropertiesDto);

			} else {
				ThemeProperties properties = hibernateCustomSkinDao.getThemesPropertiesByThemeIdAndModuleName(1,
						CustomModule.TOP_NAVIGATION_BAR.name());
				topPropertiesDto.setBackgroundColor(properties.getBackgroundColor());
				topPropertiesDto.setButtonBorderColor(properties.getButtonBorderColor());
				topPropertiesDto.setButtonColor(properties.getButtonColor());
				topPropertiesDto.setButtonValueColor(properties.getButtonValueColor());
				topPropertiesDto.setIconColor(properties.getIconColor());
				themePropertiesDTOs.add(topPropertiesDto);
			}
			if (leftCustomSkin != null) {
				leftPropertiesDto.setBackgroundColor(leftCustomSkin.getBackgroundColor());
				leftPropertiesDto.setIconColor(leftCustomSkin.getIconColor());
				leftPropertiesDto.setTextColor(leftCustomSkin.getTextColor());
				leftPropertiesDto.setButtonBorderColor(leftCustomSkin.getButtonBorderColor());
				themePropertiesDTOs.add(leftPropertiesDto);
			} else {
				ThemeProperties leftProperties = hibernateCustomSkinDao.getThemesPropertiesByThemeIdAndModuleName(1,
						CustomModule.LEFT_SIDE_MENU.name());
				leftPropertiesDto.setBackgroundColor(leftProperties.getBackgroundColor());
				leftPropertiesDto.setIconColor(leftProperties.getIconColor());
				leftPropertiesDto.setTextColor(leftProperties.getTextColor());
				leftPropertiesDto.setButtonBorderColor(leftProperties.getButtonBorderColor());
				themePropertiesDTOs.add(leftPropertiesDto);
			}
			if (footerCustomSkin != null) {
				footerPropertiesDto.setBackgroundColor(footerCustomSkin.getBackgroundColor());
				footerPropertiesDto.setTextColor(footerCustomSkin.getTextColor());
				footerPropertiesDto.setTextContent(footerCustomSkin.getTextContent());
				footerPropertiesDto.setShowFooter(footerCustomSkin.isShowFooter());
				themePropertiesDTOs.add(footerPropertiesDto);
			} else {
				ThemeProperties footerProperties = hibernateCustomSkinDao.getThemesPropertiesByThemeIdAndModuleName(1,
						CustomModule.FOOTER.name());
				footerPropertiesDto.setBackgroundColor(footerProperties.getBackgroundColor());
				footerPropertiesDto.setTextColor(footerProperties.getTextColor());
				footerPropertiesDto.setTextContent(footerProperties.getTextContent());
				footerPropertiesDto.setShowFooter(footerProperties.isShowFooter());
				themePropertiesDTOs.add(footerPropertiesDto);
			}

			if (mainCustomSkin != null) {
				mainPropertiesDto.setBackgroundColor(mainCustomSkin.getBackgroundColor());
				mainPropertiesDto.setDivBgColor(mainCustomSkin.getDivBgColor());
				if (mainCustomSkin.getTextColor().equalsIgnoreCase(mainCustomSkin.getDivBgColor())) {
					mainPropertiesDto.setTextColor("#000");
					mainPropertiesDto.setButtonBorderColor("#ddd");
				} else {
					mainPropertiesDto.setTextColor(mainCustomSkin.getTextColor());
					mainPropertiesDto.setButtonBorderColor(mainCustomSkin.getButtonBorderColor());
				}
				themePropertiesDTOs.add(mainPropertiesDto);
			} else {
				ThemeProperties mainProperties = hibernateCustomSkinDao.getThemesPropertiesByThemeIdAndModuleName(1,
						CustomModule.MAIN_CONTENT.name());
				mainPropertiesDto.setBackgroundColor(mainProperties.getBackgroundColor());
				mainPropertiesDto.setDivBgColor(mainProperties.getDivBgColor());
				mainPropertiesDto.setTextColor(mainProperties.getTextColor());
				mainPropertiesDto.setButtonBorderColor(mainProperties.getButtonBorderColor());
				themePropertiesDTOs.add(mainPropertiesDto);

			}

			/** Save Theme **/
			Theme theme = new Theme();
			theme.setName("Light_copy");
			theme.setCreatedUserId(loggedUserId);
			theme.setUpdatedUserId(loggedUserId);
			theme.setCreatedTime(new Date());
			theme.setUpdatedTime(new Date());
			CompanyProfile companyProfile = new CompanyProfile();
			companyProfile.setId(companyId);
			theme.setCompanyProfile(companyProfile);
			theme.setDefaultTheme(false);
			theme.setDescription("Light_copy");
			genericDao.save(theme);
			/** End Save Theme **/
			for (ThemePropertiesDTO td : themePropertiesDTOs) {
				ThemeProperties tp = new ThemeProperties();
				tp.setModuleType(CustomModule.valueOf(td.getModuleTypeString()));
				tp.setBackgroundColor(td.getBackgroundColor());
				tp.setDivBgColor(td.getDivBgColor());
				tp.setTextColor(td.getTextColor());
				tp.setButtonBorderColor(td.getButtonBorderColor());
				tp.setButtonColor(td.getButtonColor());
				tp.setButtonValueColor(td.getButtonValueColor());
				tp.setTextContent(td.getTextContent());
				tp.setIconColor(td.getIconColor());
				tp.setShowFooter(td.isShowFooter());
				tp.setCreatedUserId(loggedUserId);
				tp.setUpdatedUserId(loggedUserId);
				tp.setCreatedTime(new Date());
				tp.setUpdatedTime(new Date());
				tp.setTheme(theme);
				genericDao.save(tp);
			}
			CompanyThemeActive themeActivated = new CompanyThemeActive();
			themeActivated.setTheme(theme);
			themeActivated.setCreatedUserId(loggedUserId);
			themeActivated.setUpdatedUserId(loggedUserId);
			themeActivated.setCreatedTime(new Date());
			themeActivated.setUpdatedTime(new Date());
			themeActivated.setCompanyProfile(companyProfile);
			genericDao.save(themeActivated);
			count++;
			int itemsLeft = totalCompanies - count;
		}
		response.setData(count);
		response.setStatusCode(200);
		response.setMessage("Successfully Inserted Data.");
		return response;
	}

	@Override
	public XtremandResponse getThemeBgImagePath(Integer userId, MultipartFile file) {
		XtremandResponse xRes = new XtremandResponse();
		xRes.setStatusCode(400);
		try {
			xRes.setData(returnImagePath(file, userId, "theme-background-image"));
			xRes.setStatusCode(200);
		} catch (Exception e) {
			String errorMessage = "Error in saving theme Bg Image file :: " + e.getMessage();
			logger.error(errorMessage);
		}
		return xRes;
	}

	private String returnImagePath(MultipartFile imageFile, Integer userId, String type) {
		try {
			final String path = new SimpleDateFormat("ddMMyyyy").format(new Date());
			String themeBgPath = null;
			if (imageFile != null && imageFile.getOriginalFilename() != null
					&& !imageFile.getOriginalFilename().trim().isEmpty()) {
				String imagePath = images_path + userId + sep + path + sep + type + sep;
				File imageDir = new File(imagePath);
				if (!imageDir.exists()) {
					imageDir.mkdirs();
				}
				String imageFilePath = imagePath + sep + imageFile.getOriginalFilename().replaceAll(regex, "");
				File newImageFile = new File(imageFilePath);

				if (!newImageFile.exists()) {
					FileOutputStream fileOutputStream = new FileOutputStream(newImageFile);
					fileOutputStream.write(imageFile.getBytes());
					fileOutputStream.flush();
					fileOutputStream.close();
				}
				themeBgPath = "images" + sep + userId + sep + path + sep + type + sep
						+ imageFile.getOriginalFilename().replaceAll(regex, "");

			}
			return themeBgPath;
		} catch (HibernateException | UserDataAccessException e) {
			logger.error("Error In  returnImagePath()", e);
			throw new UserDataAccessException(e.getMessage());
		} catch (Exception ex) {
			logger.error("Error In  returnImagePath()", ex);
			throw new UserDataAccessException(ex.getMessage());
		}

	}
//	@Override
//	public XtremandResponse insertDataIntoCompanyActiveThemeTable() {
//		XtremandResponse response = new XtremandResponse();
//		List<Integer> companyIds = hibernateCustomSkinDao.getCompanyIdsInCustomSkinTable();
//		for (Integer companyId : companyIds) {
//			Integer loggedInUserId = categoryDao.getUserIdByCompanyId(companyId);
//			List<ThemeDTO> dtos = hibernateCustomSkinDao.getThemesByCompanyId(companyId);
//			for (ThemeDTO dto : dtos) {
//				Theme theme2 = hibernateCustomSkinDao.getThemeById(dto.getId());
//				CompanyThemeActive themeActivated = new CompanyThemeActive();
//				themeActivated.setTheme(theme2);
//				themeActivated.setCreatedUserId(loggedInUserId);
//				themeActivated.setUpdatedUserId(loggedInUserId);
//				themeActivated.setCreatedTime(new Date());
//				themeActivated.setUpdatedTime(new Date());
//				CompanyProfile companyProfileName = userDao.getCompanyNameByCompanyId(companyId);
//				CompanyProfile companyProfile = userDao
//						.getCompanyProfileByCompanyName(companyProfileName.getCompanyName());
//				themeActivated.setCompanyProfile(companyProfile);
//				genericDao.save(themeActivated);
//			}
//		}
//		response.setStatusCode(200);
//		response.setMessage("Successfully Inserted Data In The Table.");
//		return response;
//	}
//	

	@Override
	public XtremandResponse getDefaultImagePath(String parentThemeName, Integer themeId) {
		XtremandResponse response = new XtremandResponse();
		response.setStatusCode(400);
		if (themeId != null && parentThemeName != null) {
			response.setData(hibernateCustomSkinDao.getDefaultImagePath(parentThemeName, themeId));
			response.setMessage("Success");
			response.setStatusCode(200);
		} else {
			response.setStatusCode(500);
			response.setMessage("Invalid Inputs");
		}
		return response;
	}

	@Override
	public XtremandResponse updateDefaultThemeImages(ThemeDTO themeDto) {
		XtremandResponse response = new XtremandResponse();
		String responseMessage = "Failed";
		Integer responseStatusCode = 400;
		if (themeDto != null) {
			response.setData(hibernateCustomSkinDao.updateDefaultThemeImages(themeDto));
			responseMessage = "Updated Sucessfully.";
			responseStatusCode = 200;
		}
		response.setMessage(responseMessage);
		response.setStatusCode(responseStatusCode);
		return response;
	}

	/**** 10-09-2024 ****/
	@Override
	public XtremandResponse fetchCompanyActiveTheme(VanityUrlDetailsDTO vanityUrlDetailsDTO) {
		XtremandResponse response = new XtremandResponse();
		String responseMessage = "Failed";
		Integer responseStatusCode = 400;

		Integer companyId = 0;
		Integer userId = vanityUrlDetailsDTO.getUserId();
		String vendorComapnyProfileName = vanityUrlDetailsDTO.getVendorCompanyProfileName();

		if (vanityUrlDetailsDTO.isVanityUrlFilter() && StringUtils.hasText(vendorComapnyProfileName)) {
			companyId = userDao.getCompanyIdByProfileName(vendorComapnyProfileName);
		} else if (XamplifyUtils.isValidInteger(userId)) {
			companyId = userDao.getCompanyIdByUserId(userId);
		}
		if (XamplifyUtils.isValidInteger(companyId)) {
			response.setData(hibernateCustomSkinDao.fetchCompanyActiveTheme(companyId));
			responseStatusCode = 200;
			responseMessage = "Sucess";
		}
		response.setMessage(responseMessage);
		response.setStatusCode(responseStatusCode);
		return response;
	}
}
