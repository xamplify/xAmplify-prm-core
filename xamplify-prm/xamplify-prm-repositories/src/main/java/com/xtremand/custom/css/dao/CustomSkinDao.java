package com.xtremand.custom.css.dao;

import java.util.List;

import com.xtremand.custom.css.bom.CustomModule;
import com.xtremand.custom.css.bom.CustomSkin;
import com.xtremand.custom.css.bom.Theme;
import com.xtremand.custom.css.bom.ThemeProperties;
import com.xtremand.custom.css.dto.CompanyThemeActiveDTO;
import com.xtremand.custom.css.dto.CustomSkinDTO;
import com.xtremand.custom.css.dto.ThemeDTO;
import com.xtremand.custom.css.dto.ThemePropertiesDTO;



public interface CustomSkinDao {

	CustomSkin findByType(Integer userId ,String type);
	
	String getEnumType(Integer parentId);

	List<CustomSkinDTO> findByCompanyId(Integer id);

	boolean validateCompanyProfile(Integer companyId);

	CustomSkinDTO findById(Integer id);

	int updateCustomSkin(CustomSkinDTO customSkinDTO, Integer id);
	
	int updateThemeProperties(ThemePropertiesDTO customSkinDTO1, Integer themeId);

	CustomSkinDTO getDefaultSkin(Integer companyId, String module, boolean isBoolean);
	
	/****** Dark Theme ********/
	public CustomSkinDTO updateDarkTheme(boolean isDarkTheme,boolean isDefault,Integer comapnyId);
	
	/********** Custom Theme *********/
	public CustomSkinDTO updateCustomTheme(boolean isDarkTheme, Integer comapnyId);
	
	public CustomSkinDTO updateDefaultSettings(CustomSkinDTO customSkinDTO);
	
	
	/**************** New Changes*************/
	public List<ThemeDTO> getThemesByCompanyId(Integer CompanyId);
	
	public Integer getThemeIdByThemeName(String themeName);
	
	public List<ThemePropertiesDTO> getThemesPropertiesByThemeId(Integer themeId);
	
	public Theme getThemeById(Integer id);
	
	public String deleteThemeById(Integer id);
	
	public ThemeProperties getThemesPropertiesByThemeIdAndModuleName(Integer themeId,String modulename);

	
	public ThemePropertiesDTO getThemesPropertiesByThemeIdAndModuleName(Integer themeId);
	
	public CompanyThemeActiveDTO getActivateTheme(Integer companyId);
	
	public int updateCompanyActivateTheme(CompanyThemeActiveDTO activeDto,Integer id);

	public ThemeDTO getThemeDTOById(Integer id);
	
	public List<String> getThemeNames(Integer companyId);
    
	boolean checkActiveTheme(Integer themeId);
	
	boolean checkThemeIdExsitOrNot(Integer themeId);
	
	Integer updateThemeById(ThemeDTO themeDto,Integer themeId);
	
	public List<ThemeDTO> getThemesByCompanyIdOne();
	
	public boolean checkActiveThemeByCompanyId(Integer companyId);
	
	public boolean checkDuplicateName(String name, Integer companyId);
	
	public boolean checkNameForUpdate(String name,Integer companyId);
	
	
	/*** fetch data from existing data ***/
	List<Integer> getCompanyIdsInCustomSkinTable();
	
	
	public ThemeProperties getThemePropertyByThemeIdAndModule(Integer themeId, CustomModule moduleType);

	String getDefaultImagePath(String parentThemeName, Integer themeId);

	Integer updateDefaultThemeImages(ThemeDTO themeDto);

	ThemeDTO fetchCompanyActiveTheme(Integer companyId);
	


}
