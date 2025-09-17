package com.xtremand.util.dto;

import java.io.Serializable;
import java.util.Date;

import org.springframework.util.StringUtils;

import com.xtremand.user.bom.ModulesDisplayType;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;

@Data
public class ViewTypePatchRequestDTO implements Serializable {
	/**
	* 
	*/
	private static final long serialVersionUID = -9032633089381684419L;

	private Integer userId;

	private Integer teamMemberId;

	@Getter(value = AccessLevel.NONE)
	private String viewType;

	@Getter(value = AccessLevel.NONE)
	private ModulesDisplayType modulesDisplayType;

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public String getViewType() {
		if (StringUtils.hasText(viewType)) {
			return viewType;
		} else {
			return ModulesDisplayType.LIST.name();
		}

	}

	public ModulesDisplayType getModulesDisplayType() {
		if (ModulesDisplayType.LIST.name().equals(viewType)) {
			return ModulesDisplayType.LIST;
		} else if (ModulesDisplayType.GRID.name().equals(viewType)) {
			return ModulesDisplayType.GRID;
		} else if (ModulesDisplayType.FOLDER_LIST.name().equals(viewType)) {
			return ModulesDisplayType.FOLDER_LIST;
		} else if (ModulesDisplayType.FOLDER_GRID.name().equals(viewType)) {
			return ModulesDisplayType.FOLDER_GRID;
		} else {
			return ModulesDisplayType.LIST;
		}

	}
	
	private Integer partnershipId;
	
	private boolean viewUpdated;
	
	private Date createdTime;
	
	private Date updatedTime;

}
