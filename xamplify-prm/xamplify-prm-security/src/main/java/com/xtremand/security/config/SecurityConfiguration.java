
package com.xtremand.security.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true)
@EnableWebSecurity
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

	@Autowired
	UserDetailsService userDetailsService;

	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		auth.userDetailsService(userDetailsService);
		auth.authenticationProvider(authenticationProvider());
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public DaoAuthenticationProvider authenticationProvider() {
		DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
		authenticationProvider.setUserDetailsService(userDetailsService);
		authenticationProvider.setPasswordEncoder(passwordEncoder());
		return authenticationProvider;
	}

	@Override
	@Bean
	public AuthenticationManager authenticationManagerBean() throws Exception {
		return super.authenticationManagerBean();
	}

	@Override
	public void configure(HttpSecurity http) throws Exception {
		http.csrf().disable().authorizeRequests().antMatchers(HttpMethod.OPTIONS, "/oauth/token").permitAll()
				.antMatchers(HttpMethod.GET, "/**/login").permitAll().antMatchers(HttpMethod.GET, "/logout/**")
				.permitAll().antMatchers(HttpMethod.POST, "/register/**").permitAll()
				.antMatchers(HttpMethod.GET, "/register/verifyemail/user").permitAll()
				.antMatchers(HttpMethod.GET, "/register/forgotpassword").permitAll()
				.antMatchers(HttpMethod.GET, "/**/videos/video-by-alias/**").permitAll()
				.antMatchers(HttpMethod.GET, "/user/video").permitAll()
				.antMatchers(HttpMethod.POST, "/videos/**/user/save-call-action-user").permitAll()
				.antMatchers(HttpMethod.POST, "/admin/video/increment_view").permitAll()
				.antMatchers(HttpMethod.POST, "/**/log/unsubscribe-user").permitAll()
				.antMatchers(HttpMethod.GET, "/**/videos/video-by-shortenerurlalias/**").permitAll()
				.antMatchers(HttpMethod.GET, "/register/resend/activationemail").permitAll()
				.antMatchers(HttpMethod.GET, "/user/{alias}").permitAll().antMatchers(HttpMethod.GET, "/getByFormAlias")
				.permitAll().antMatchers(HttpMethod.POST, "/form/submit/**").permitAll()
				.antMatchers(HttpMethod.POST, "/user/logVideoActionSms").permitAll()
				.antMatchers(HttpMethod.POST, "/admin/video/increment_view_sms").permitAll()
				.antMatchers(HttpMethod.GET, "/getUserByAlias/{alias}").permitAll()
				.antMatchers(HttpMethod.POST, "/accessAccount/updatePassword").permitAll()
				.antMatchers(HttpMethod.GET, "/v_url/**").permitAll()
				.antMatchers(HttpMethod.GET, "/addDefaultMdfCredit/**").permitAll()
				.antMatchers(HttpMethod.GET, "/**/api/pdf/proxy/**").permitAll()
				.antMatchers(HttpMethod.GET, "/validate-captcha/**").permitAll().antMatchers("/url/**").permitAll()
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
