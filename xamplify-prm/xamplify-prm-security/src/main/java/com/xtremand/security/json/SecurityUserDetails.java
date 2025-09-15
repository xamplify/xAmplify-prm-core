package com.xtremand.security.json;

import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.StringUtils;

import com.xtremand.user.bom.User;
import com.xtremand.user.bom.User.UserStatus;

public class SecurityUserDetails implements UserDetails {
	private static final long serialVersionUID = 1L;
	
	private Integer userId;
	private String username;
	private String password;
	private Collection<? extends GrantedAuthority> authorities;
	private UserStatus userStatus;

	public SecurityUserDetails(User user) {
		this.userId = user.getUserId();
		String userName = user.getUserName();
		if (StringUtils.hasText(userName) && user.getEmailId().equals(userName)) {
			this.username = user.getUserName();
		} else {
			this.username = user.getEmailId();
		}
		this.password = user.getPassword();
		this.authorities = new SecurityRole(user.getRoles()).getAuthorities();
		this.userStatus = user.getUserStatus();
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return authorities;
	}

	@Override
	public String getPassword() {
		return password;
	}

	@Override
	public String getUsername() {
		return username;
	}
	

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return UserStatus.APPROVED.equals(userStatus);
	}

	public Integer getUserId() {
		return userId;
	}
}
