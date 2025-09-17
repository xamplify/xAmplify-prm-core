package com.xtremand.security.config;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

@Component
public class CustomSecurityFailureHandler extends
		SimpleUrlAuthenticationFailureHandler {
	Logger logger = LoggerFactory.getLogger(getClass());
	
	private RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();

	String targetUrl = "/failure";

	@Override
	public void onAuthenticationFailure(HttpServletRequest request,
			HttpServletResponse response, AuthenticationException exception)
			throws IOException, ServletException {
		logger.debug("CustomSecurityFailureHandler :: onAuthenticationFailure()");
		redirectStrategy.sendRedirect(request, response, targetUrl);
	}

	public void setRedirectStrategy(RedirectStrategy redirectStrategy) {
		this.redirectStrategy = redirectStrategy;
	}

	protected RedirectStrategy getRedirectStrategy() {
		return redirectStrategy;
	}

}
