package com.xtremand.url.authorization.service;

import com.xtremand.url.authorization.dto.UrlAuthorizationDTO;

public interface UrlAuthorizationStrategy {
	void authorize(UrlAuthorizationDTO urlAuthorizationDTO);

	boolean isModuleIdMatched(Integer moduleId);

}
