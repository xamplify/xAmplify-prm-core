package com.xtremand.custome.css.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.xtremand.campaign.exception.XamplifyDataAccessException;
import com.xtremand.custom.css.dto.CompanyThemeActiveDTO;
import com.xtremand.custom.css.dto.CustomSkinDTO;
import com.xtremand.custom.css.dto.ThemeDTO;
import com.xtremand.custom.css.dto.ThemePropertiesDTO;
import com.xtremand.custom.css.dto.ThemeThemePropertiesListWrapper;
import com.xtremand.custom.css.service.CustomSkinService;
import com.xtremand.exception.DuplicateEntryException;
import com.xtremand.formbeans.XtremandResponse;
import com.xtremand.vanity.url.dto.VanityUrlDetailsDTO;

@RestController
@RequestMapping("/custom/skin/")
public class CustomSkinController {

	@Autowired
	private CustomSkinService customSkinService;

	@PostMapping(value = "/save")
	public ResponseEntity<XtremandResponse> save(@RequestBody CustomSkinDTO customSkinDTO) {
		return ResponseEntity.ok(customSkinService.save(customSkinDTO));
	}

	@GetMapping(value = "/get/{userId}/{type}")
	public ResponseEntity<XtremandResponse> findByuserId(@PathVariable Integer userId, @PathVariable String type) {
		return ResponseEntity.ok(customSkinService.findByType(userId, type));
	}

	@PostMapping(value = "/find")
	public ResponseEntity<XtremandResponse> findById(@RequestBody VanityUrlDetailsDTO vanityUrlDetailsDTO) {
		return ResponseEntity.ok(customSkinService.findByCompanyId(vanityUrlDetailsDTO));
	}

	@GetMapping(value = "/defaultSkin/{userId}")
	public ResponseEntity<XtremandResponse> findDefaultSkin(@PathVariable Integer userId) {
		return ResponseEntity.ok(customSkinService.findDefaultSkin(userId));
	}

	@PostMapping(value = "/dark")
	public ResponseEntity<XtremandResponse> updateDarkTheme(@RequestBody CustomSkinDTO customSkinDTO) {
		return ResponseEntity.ok(customSkinService.updateDarkTheme(customSkinDTO));
	}

	@PostMapping(value = "/light")
	public ResponseEntity<XtremandResponse> updateCustomTheme(@RequestBody CustomSkinDTO customSkinDTO) {
		return ResponseEntity.ok(customSkinService.updateCustomTheme(customSkinDTO));
	}
	
	
	@PostMapping(value = "/default")
	public ResponseEntity<XtremandResponse> updateUserDefaultSettings(@RequestBody CustomSkinDTO customSkinDTO) {
		return ResponseEntity.ok(customSkinService.updateUserDefaultSettings(customSkinDTO));
	}
	
	@PostMapping(value = "/savetheme")
	public ResponseEntity<XtremandResponse> saveNewChanges(
			@RequestBody ThemeThemePropertiesListWrapper themePropertiesListWrapper) {
		try {
			return ResponseEntity.ok(customSkinService.saveTheme(themePropertiesListWrapper));
		} catch (DataIntegrityViolationException e) {
			if (e.getMessage().indexOf("xt_theme_name_unique_constraint") > -1) {
				throw new DuplicateEntryException(
						themePropertiesListWrapper.getThemeDto().getName() + " Already Exists.");
			} else {
				throw new XamplifyDataAccessException(e);
			}
		} catch (Exception e) {
			throw new XamplifyDataAccessException(e);
		}
	}
	
	@GetMapping(value = "/theme/{userId}")
	public ResponseEntity<XtremandResponse> getThemesByUserId(@PathVariable Integer userId) {
		return ResponseEntity.ok(customSkinService.getThemesByUserId(userId));
	}
	@GetMapping(value = "/defaultThemes")
	public ResponseEntity<XtremandResponse> getThemesByCompanyIdOne() {
		return ResponseEntity.ok(customSkinService.getThemesByCompanyIdOne());
	}

	@GetMapping(value = "/theme/name/{themeName}")
	public ResponseEntity<XtremandResponse> getThemeIdByThemeName(@PathVariable String themeName) {
		return ResponseEntity.ok(customSkinService.getThemeIdByThemeName(themeName));
	}

	@GetMapping(value = "/getProperties/{themeName}")
	public ResponseEntity<XtremandResponse> getThemesPropertiesByThemeName(@PathVariable String themeName) {
		return ResponseEntity.ok(customSkinService.getThemesPropertiesByThemeName(themeName));
	}

	@PostMapping(value = "/activateTheme")
	public ResponseEntity<XtremandResponse> SetThemeActivated(
			@RequestBody CompanyThemeActiveDTO companyThemeActiveDTO) {
	    try {
			return ResponseEntity.ok(customSkinService.setThemeForCompany(companyThemeActiveDTO));
		} catch (DataIntegrityViolationException e) {
			if (e.getMessage().indexOf("xt_company_theme_active_unique_constra1nt") > -1) {
				throw new DuplicateEntryException(companyThemeActiveDTO.getThemeId() + " Already Exists.");
			} else {
				throw new XamplifyDataAccessException(e);
			}
		} catch (Exception e) {
			throw new XamplifyDataAccessException(e);
		}
	}
	
	@DeleteMapping(value = "/delete/{id}")
	public ResponseEntity<XtremandResponse> deleteThemeById(@PathVariable Integer id){
		return ResponseEntity.ok(customSkinService.deleteThemeById(id));

	}
	@GetMapping(value = "/getProperties/themeId/{id}")
	public ResponseEntity<XtremandResponse> getThemesPropertiesByThemeIdAndModuleName(@PathVariable Integer id){
		return ResponseEntity.ok(customSkinService.getThemesPropertiesByThemeIdAndModuleName(id));
	}
	
	@PostMapping(value = "/getactiveTheme/")
	public ResponseEntity<XtremandResponse> getActivateTheme(@RequestBody VanityUrlDetailsDTO vanityUrlDetailsDTO){
		return ResponseEntity.ok(customSkinService.getAtivatedTheme(vanityUrlDetailsDTO));
	}

	
	@PutMapping("/update/{teamId}/{moduleName}")
	public ResponseEntity<XtremandResponse> updateThemeProperties(@PathVariable Integer teamId, @PathVariable String moduleName, @RequestBody ThemePropertiesDTO themePropertiesDto){
		return ResponseEntity.ok(customSkinService.updateProperties(teamId, moduleName, themePropertiesDto));
	}
	
	@GetMapping(value = "/getThemeDto/{id}")
	public ResponseEntity<XtremandResponse> getThemeDTOById(@PathVariable Integer id){
		return ResponseEntity.ok(customSkinService.getThemeDTOById(id));
	}
	
	@GetMapping(value = "/getNames/{userId}")
	public ResponseEntity<XtremandResponse> getNames(@PathVariable Integer userId){
		return ResponseEntity.ok(customSkinService.getThemeNames(userId));
	}
	
	@PostMapping(value = "/updatThemDto/{id}")
	public ResponseEntity<XtremandResponse> updateThemeProperties(@PathVariable Integer id, @RequestBody ThemeDTO themeDto){
		return ResponseEntity.ok(customSkinService.updateThemeByID(id, themeDto));
	}
	

	
	/** Migrating Custom SKin Table Data Into Theme ,ThemeProperties and CompanyThemeActive Tables **/
	@GetMapping(value = "/insertData")
	public ResponseEntity<XtremandResponse> insertDataIntoThemeAndThemePropertiesTable() {
		return ResponseEntity.ok(customSkinService.insertDataIntoThemeAndThemePropertiesTable());
	}
	
	/** XNFR-420 **/
	@GetMapping(value = "/getEnum/{parentId}")
	public ResponseEntity<XtremandResponse> getEnumTypeByParentId1(@PathVariable Integer parentId){
		return ResponseEntity.ok(customSkinService.getEnumTypeByParentId1(parentId));
	}
	
	@PostMapping("/backgroundImage/saveBgImage/{userId}")
    public  ResponseEntity<XtremandResponse> uploadBgImageFile(@PathVariable Integer userId,
            @RequestParam("bgImageFile") MultipartFile file) {
        return ResponseEntity.ok(customSkinService.getThemeBgImagePath(userId, file));
    }
	
	@GetMapping(value = "/getDefaultImagePath/{parentThemeName}/{themeId}")
	public ResponseEntity<XtremandResponse>  getDefaultImagePath(@PathVariable String parentThemeName,@PathVariable Integer themeId) {
		return ResponseEntity.ok(customSkinService.getDefaultImagePath(parentThemeName, themeId));
	}
	
	@PostMapping("/updateBgImagePath")
	public XtremandResponse updateDefaultThemeImages(@RequestBody ThemeDTO themeDto) {
		return customSkinService.updateDefaultThemeImages(themeDto);
	}
	
	/**** 10-09-2024****/
	@GetMapping("/fetchCompanyActiveTheme") 
	public XtremandResponse fetchCompanyActiveTheme(VanityUrlDetailsDTO detailsDTO) {
		return customSkinService.fetchCompanyActiveTheme(detailsDTO);
	}

}

