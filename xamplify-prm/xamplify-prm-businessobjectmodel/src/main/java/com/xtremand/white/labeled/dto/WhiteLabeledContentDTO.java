package com.xtremand.white.labeled.dto;

import java.io.Serializable;

import lombok.Data;

@Data
public class WhiteLabeledContentDTO implements Serializable {
	private static final long serialVersionUID = 6623345796728407293L;

	private String whiteLabeledContentSharedByVendorCompanyName;

	private boolean whiteLabeledContent;

}
