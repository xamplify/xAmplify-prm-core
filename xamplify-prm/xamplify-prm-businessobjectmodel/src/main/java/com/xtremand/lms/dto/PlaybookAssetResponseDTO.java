package com.xtremand.lms.dto;

import lombok.Data;

@Data
public class PlaybookAssetResponseDTO {
	
	private Integer id;
	private boolean finished;
	private boolean opened;	
	private String assetName;
	private String assetType;
	private String assetPath;
	private String thumbnailPath;
	private boolean published;
	private String categoryName;
	private Integer categoryId;
	private Integer videoId;
	private boolean beeTemplate;
	private Integer learningTrackContentMappingId;
	private boolean imageFileType;
	private boolean textFileType;
	private boolean contentPreviewType;
	private String assetProxyPath;
	private String description;

}
