package com.xtremand.dashboard.layout.dto;

import java.io.Serializable;

import lombok.Data;

@Data
public class DashboardLayoutDTO implements Serializable {

	private static final long serialVersionUID = 5435050001560008540L;

	private Integer divId;

	private String divName;

	private String title;

	private String htmlBody;

	private String leftHtmlBody;

	private String rightHtmlBody;

	private Integer customHtmlBlockId;

	private Integer displayIndex;

	private boolean titleVisible;

}
