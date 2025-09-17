package com.xtremand.security.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;

@Configuration
@EnableResourceServer
public class ResourceServerConfiguration extends ResourceServerConfigurerAdapter {

	private String resourceId = "rest_api";

	@Override
	public void configure(ResourceServerSecurityConfigurer resources) {
		// @formatter:off
		resources.resourceId(resourceId);
		// @formatter:on
	}

	@Override
	public void configure(HttpSecurity http) throws Exception {
		http.csrf().disable().authorizeRequests().antMatchers(HttpMethod.OPTIONS, "/oauth/token")
				.permitAll().antMatchers(HttpMethod.GET, "/**/login").permitAll()
				.antMatchers(HttpMethod.GET, "/logout/**").permitAll()
				.antMatchers(HttpMethod.POST, "/register/**").permitAll()
				.antMatchers(HttpMethod.GET, "/register/verifyemail/user").permitAll()
				.antMatchers(HttpMethod.GET, "/register/forgotpassword").permitAll()
				.antMatchers(HttpMethod.GET, "/**/videos/video-by-alias/**").permitAll()
				.antMatchers(HttpMethod.GET, "/user/video").permitAll()
				.antMatchers(HttpMethod.POST, "/videos/**/user/save-call-action-user").permitAll()
				.antMatchers(HttpMethod.POST, "/admin/video/increment_view").permitAll()
				.antMatchers(HttpMethod.POST, "/**/log/unsubscribe-user").permitAll()
				.antMatchers(HttpMethod.GET, "/**/videos/video-by-shortenerurlalias/**").permitAll()
				.antMatchers(HttpMethod.GET, "/register/resend/activationemail").permitAll()
				.antMatchers(HttpMethod.GET, "/user/{alias}")
				.permitAll()
				.antMatchers(HttpMethod.GET, "/getByFormAlias").permitAll()
				.antMatchers(HttpMethod.POST, "/form/submit/**").permitAll()
				.antMatchers(HttpMethod.POST, "/user/logVideoActionSms").permitAll()
				.antMatchers(HttpMethod.POST, "/admin/video/increment_view_sms").permitAll()
				.antMatchers(HttpMethod.GET, "/getUserByAlias/{alias}").permitAll()
				.antMatchers(HttpMethod.POST, "/accessAccount/updatePassword").permitAll()
				.antMatchers(HttpMethod.GET, "/v_url/**").permitAll()
				.antMatchers(HttpMethod.GET, "/addDefaultMdfCredit/**").permitAll()
				.antMatchers(HttpMethod.GET, "/validate-captcha/**").permitAll()
				.antMatchers("/url/**").permitAll()
				.antMatchers("/domain/**").permitAll().antMatchers(HttpMethod.GET, "/**/authorize").permitAll()
				.antMatchers(HttpMethod.GET, "/findCompanyDetails/**").permitAll()
                                .antMatchers(HttpMethod.POST, "/signUpAsTeamMember").permitAll()
                                .antMatchers(HttpMethod.POST, "/signUpAsPartner").permitAll()
                                .antMatchers(HttpMethod.POST, "/signUpAsPrm").permitAll()
                                .antMatchers(HttpMethod.GET, "/oauth/callback/**/**").permitAll()
				.antMatchers(HttpMethod.GET, "/authorize/**").permitAll().anyRequest().authenticated().and().formLogin()
				.permitAll();
	}
}