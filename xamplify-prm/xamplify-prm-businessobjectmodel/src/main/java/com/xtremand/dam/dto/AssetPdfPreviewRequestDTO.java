package com.xtremand.dam.dto;

import java.io.Serializable;

import lombok.Data;

@Data
public class AssetPdfPreviewRequestDTO implements Serializable {
	/**
	* 
	*/
	private static final long serialVersionUID = 3335930541215373587L;

	private Integer id;

	private Integer userId;

	private boolean partnerView;

	private boolean trackOrPlayBookPdfPreview;
	
	private Integer learningTrackContentMappingId;

}
