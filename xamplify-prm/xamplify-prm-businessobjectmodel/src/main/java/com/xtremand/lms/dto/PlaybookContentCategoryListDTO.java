package com.xtremand.lms.dto;


import java.util.List;

import lombok.Data;

@Data
public class PlaybookContentCategoryListDTO {
	
	private String categoryName;
	
	private String damIds;
	
	private Integer count;
	
	private List<PlaybookAssetResponseDTO> dam;
	
}
