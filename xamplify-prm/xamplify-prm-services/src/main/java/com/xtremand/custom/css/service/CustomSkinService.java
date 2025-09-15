package com.xtremand.custom.css.service;



import org.springframework.web.multipart.MultipartFile;

import com.xtremand.custom.css.dto.CompanyThemeActiveDTO;
import com.xtremand.custom.css.dto.CustomSkinDTO;
import com.xtremand.custom.css.dto.ThemeDTO;
import com.xtremand.custom.css.dto.ThemePropertiesDTO;
import com.xtremand.custom.css.dto.ThemeThemePropertiesListWrapper;
import com.xtremand.formbeans.XtremandResponse;
import com.xtremand.vanity.url.dto.VanityUrlDetailsDTO;

public interface CustomSkinService {

	XtremandResponse save(CustomSkinDTO customSkinDTO);

	XtremandResponse findByType(Integer userId, String type);
	
	//XtremandResponse getEnumTypeByParentId(Integer parentId, Integer userId);

	XtremandResponse findByCompanyId(VanityUrlDetailsDTO vanityUrlDetailsDTO);

	XtremandResponse findById(Integer id);

	XtremandResponse findDefaultSkin(Integer userId);

	/***** Dark Theme *****/
	XtremandResponse updateDarkTheme(CustomSkinDTO customSkinDTO);

	/*********** Custom Theme *********/
	XtremandResponse updateCustomTheme(CustomSkinDTO customSkinDTO);

	XtremandResponse updateUserDefaultSettings(CustomSkinDTO customSkinDTO);

	/********** NEw Changes ***********/
	XtremandResponse saveTheme(ThemeThemePropertiesListWrapper themeThemePropertiesListWrapper);

	XtremandResponse getThemesByUserId(Integer userId);

	XtremandResponse getThemeIdByThemeName(String themeName);

	XtremandResponse getThemesPropertiesByThemeName(String themeName);
	
	XtremandResponse setThemeForCompany(CompanyThemeActiveDTO activeDTO);
	
	XtremandResponse deleteThemeById(Integer id);
	
	XtremandResponse getThemesPropertiesByThemeIdAndModuleName(Integer themeId);
	
	XtremandResponse getAtivatedTheme(VanityUrlDetailsDTO vanityUrlDetailsDTO);
	
//	public XtremandResponse updateActivateTheme(CompanyThemeActiveDTO dto ,Integer id ,BindingResult result) ;

	public XtremandResponse updateProperties(Integer themeId, String moduleName, ThemePropertiesDTO themePropertiesDto);
	
	XtremandResponse getThemeDTOById(Integer id);
	
	XtremandResponse getThemeNames(Integer userId);
	
	//XtremandResponse getEnumTypeByParentId(Integer parentId);
	
	public XtremandResponse updateThemeByID(Integer themeId, ThemeDTO themeDto);
	
	XtremandResponse getThemesByCompanyIdOne();
	
	/*** fetch data from existing data ***/
	XtremandResponse getCompanyIdsInCustomSkinTable();
	XtremandResponse insertDataIntoThemeAndThemePropertiesTable();
	//XtremandResponse insertDataIntoCompanyActiveThemeTable();

	/** XNFR-420 **/
	XtremandResponse getEnumTypeByParentId1(Integer parentId);

	XtremandResponse getThemeBgImagePath(Integer userId, MultipartFile file);

	XtremandResponse getDefaultImagePath(String parentThemeName, Integer themeId);

	XtremandResponse updateDefaultThemeImages(ThemeDTO themeDto);

	XtremandResponse fetchCompanyActiveTheme(VanityUrlDetailsDTO detailsDTO);

	
}
