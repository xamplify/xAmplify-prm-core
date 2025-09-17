/**
 * 
 */
package com.xtremand.rest.config;

import java.util.Set;

import javax.servlet.FilterRegistration;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.web.session.HttpSessionEventPublisher;
import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.filter.DelegatingFilterProxy;
import org.springframework.web.servlet.DispatcherServlet;

import com.xtremand.security.config.AnnotationBasedSecurityConfig;
import com.xtremand.security.config.AuthorizationServerConfiguration;
import com.xtremand.security.config.GlobalAuthenticationConfig;
import com.xtremand.security.config.ResourceServerConfiguration;
import com.xtremand.security.config.SecurityConfiguration;

/**
 * @author Ramesh
 *
 */
public class WebAppInitializer implements WebApplicationInitializer {
	
	private  Logger logger = LoggerFactory.getLogger(getClass());

	@Override
	public void onStartup(ServletContext servletContext) throws ServletException {
		
		AnnotationConfigWebApplicationContext rootContext=new AnnotationConfigWebApplicationContext();
		rootContext.register(ApplicationRootConfig.class, HibernateConfig.class, AnnotationBasedSecurityConfig.class, 
				GlobalAuthenticationConfig.class,  SecurityConfiguration.class, AuthorizationServerConfiguration.class, ResourceServerConfiguration.class);

	    servletContext.addListener(new ContextLoaderListener(rootContext));
	    servletContext.setInitParameter("defaultHtmlEscape", "true");
		
		/*FilterRegistration.Dynamic encodingFilter=servletContext.addFilter("encoding-filter", new CharacterEncodingFilter());
		encodingFilter.setInitParameter("encoding", "UTF-8");
		encodingFilter.setInitParameter("forceEncoding", "true");
		encodingFilter.addMappingForServletNames(null, true, "/*");*/
		
		DelegatingFilterProxy corsFilter = new DelegatingFilterProxy("corsFilter");
		corsFilter.setServletContext(servletContext);
		corsFilter.setContextAttribute("org.springframework.web.servlet.FrameworkServlet.CONTEXT.dispatcher");
		FilterRegistration.Dynamic corsFilterDynamic = servletContext.addFilter("corsFilter", corsFilter);
		corsFilterDynamic.addMappingForUrlPatterns(null, false,"/*");
		
		DelegatingFilterProxy filter = new DelegatingFilterProxy("springSecurityFilterChain");
		filter.setServletContext(servletContext);
		filter.setContextAttribute("org.springframework.web.servlet.FrameworkServlet.CONTEXT.dispatcher");
		FilterRegistration.Dynamic securityFilter = servletContext.addFilter("securityFilter", filter);
		securityFilter.addMappingForUrlPatterns(null, false,"/*");
		
		servletContext.addListener(new HttpSessionEventPublisher());
		
		ServletRegistration.Dynamic dispatcher = servletContext.addServlet("dispatcher", new DispatcherServlet(rootContext));
		dispatcher.setAsyncSupported(true);
		dispatcher.setLoadOnStartup(1);
		Set<String> mappingConflicts = dispatcher.addMapping("/");
		if(!mappingConflicts.isEmpty()){
			for (String map : mappingConflicts) {
				logger.error("Mapping Conflict "+map);
			}
			throw new IllegalStateException("Dispatcher : cannot be mapped to '/' under Tomcat vesions <= 7.0.4");
		}
		rootContext.setServletContext(servletContext);
		rootContext.refresh();
	}

	
}
