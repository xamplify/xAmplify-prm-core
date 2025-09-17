package com.xtremand.url.authorization.dto;

import java.util.ArrayList;
import java.util.List;

import com.xtremand.vanity.url.dto.VanityUrlDetailsDTO;

import lombok.Data;

@Data
public class UrlAuthorizationDTO {

	private Integer loggedInUserCompanyId;
	private boolean vendorAdmin;
	private boolean partnerAdmin;
	private VanityUrlDetailsDTO vanityUrlDetailsDTO;
	private Integer loggedInUserId;
	private String routerUrl;
	private List<Integer> roleIds = new ArrayList<>();

}
