package com.xtremand.dashboard.layout.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import lombok.Data;

@Data
public class DashboardLayoutRequestDTO implements Serializable {

	private static final long serialVersionUID = -3038224730296745522L;

	private Integer userId;

	private Set<Integer> ids;

	private String companyProfileName;

	private List<DashboardLayoutDTO> dashboardLayoutDTOs = new ArrayList<>();

}
