package com.xtremand.rest.config;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.xtremand.util.CustomValidatonException;

@Component("corsFilter")
@Order(Ordered.HIGHEST_PRECEDENCE)
public class SimpleCorsFilter implements Filter {

	private static final String ORIGIN = "Origin";

	public SimpleCorsFilter() {
		// Do nothing

	}

	@Override
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) {
		HttpServletResponse response = (HttpServletResponse) res;
		HttpServletRequest request = (HttpServletRequest) req;
		String origin = request.getHeader(ORIGIN);
		response.setHeader("Access-Control-Allow-Origin", origin);
		response.setHeader("Vary", ORIGIN);
		response.setHeader("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE");
		response.setHeader("Access-Control-Max-Age", "3600");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Expose-Header",
				"Access-Control-Allow-Origin,Access-Control-Allow-Credentials");
		response.setHeader("Access-Control-Allow-Headers", request.getHeader("Access-Control-Request-Headers"));
		if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
			response.setStatus(HttpServletResponse.SC_OK);
		} else {
			try {
				chain.doFilter(req, res);
			} catch (IOException | ServletException e) {
				throw new CustomValidatonException(e.getMessage());
			}
		}

	}

	@Override
	public void init(FilterConfig filterConfig) {
		// Do nothing
	}

	@Override
	public void destroy() {
		// Do nothing
	}
}
