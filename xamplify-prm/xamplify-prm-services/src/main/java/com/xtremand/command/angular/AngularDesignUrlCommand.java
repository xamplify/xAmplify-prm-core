package com.xtremand.command.angular;

import com.xtremand.team.member.dto.RoleDisplayDTO;
import com.xtremand.util.dto.AngularUrl;

public class AngularDesignUrlCommand implements AngularUrlCommand {
	
	private RoleDisplayDTO roleDisplayDTO;
	
	private AngularUrl angularUrl;


	@Override
	public boolean isAuthorizedUrl() {
		boolean isAuthorizedUrl = false;
		boolean isLoggedInThroughOwnVanityUrl = angularUrl.isVendorLoggedInThroughOwnVanityUrl();
		boolean isVanityUrlFilter = angularUrl.isVanityUrlFilter();
		if (isVanityUrlFilter) {
			if (isLoggedInThroughOwnVanityUrl) {
				isAuthorizedUrl = roleDisplayDTO.hasDesignAccess();
			}
		} else {
			isAuthorizedUrl = roleDisplayDTO.hasDesignAccess();
		}
		return isAuthorizedUrl;
	}

	public AngularDesignUrlCommand(RoleDisplayDTO roleDisplayDTO, AngularUrl angularUrl) {
		super();
		this.roleDisplayDTO = roleDisplayDTO;
		this.angularUrl = angularUrl;
	}

	

}
