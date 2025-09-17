package com.xtremand.util.dto;

import java.io.Serializable;
import java.util.List;

import lombok.Data;

@Data
public class ModuleCustomDTO implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -6655940902532270778L;

	private Integer id;
	
	private Integer moduleId;
	
	private String moduleName;
	
	private String customName;
	
	private Integer displayIndex;

	
	private String angularPath;
	
	private String angularIcon;
	
	private String mergeTag;
	
	private boolean showSubMenu = false;
	
	private List<ModuleCustomDTO> subModules;
	
	private boolean partnerAccessModule;

	private boolean marketingModule;

}
