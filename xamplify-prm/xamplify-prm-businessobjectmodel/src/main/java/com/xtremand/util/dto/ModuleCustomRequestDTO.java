package com.xtremand.util.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class ModuleCustomRequestDTO implements Serializable {


	/**
	 * 
	 */
	private static final long serialVersionUID = -3038224730296745522L;
	
	private Integer userId;
	private List<ModuleCustomDTO> menuItems = new ArrayList<ModuleCustomDTO>();

}
