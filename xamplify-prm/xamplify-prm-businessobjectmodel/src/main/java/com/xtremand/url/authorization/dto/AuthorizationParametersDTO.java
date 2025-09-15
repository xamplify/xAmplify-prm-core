package com.xtremand.url.authorization.dto;

import com.xtremand.vanity.url.dto.VanityUrlDetailsDTO;

public class AuthorizationParametersDTO {

	private final VanityUrlDetailsDTO vanityUrlDetailsDTO;
	private final String routerUrl;

	public AuthorizationParametersDTO(VanityUrlDetailsDTO vanityUrlDetailsDTO, String routerUrl) {
		this.vanityUrlDetailsDTO = vanityUrlDetailsDTO;
		this.routerUrl = routerUrl;
	}

	public boolean isLoggedInThroughVanity() {
		return vanityUrlDetailsDTO.isVanityUrlFilter();
	}

	public boolean isLoggedInThroughOwnVanityUrl() {
		return vanityUrlDetailsDTO.isVendorLoggedInThroughOwnVanityUrl();
	}

	public boolean isPartnerLoggedInThroughVanityUrl() {
		return vanityUrlDetailsDTO.isPartnerLoggedInThroughVanityUrl();
	}

	public String getRouterUrl() {
		return routerUrl;
	}

}
