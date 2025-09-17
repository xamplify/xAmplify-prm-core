package com.xtremand.security.json;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.security.core.GrantedAuthority;

import com.xtremand.user.bom.Role;

public class SecurityRole{
	Set<Role> roles;
	
	public SecurityRole(Set<Role> set) {
		this.roles = set;
	}
	
	public List<GrantedAuthority> getAuthorities(){
		List<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
		for(Role role : this.roles){
			authorities.add(new SecRole(role));
		}
		return authorities;
	}

	private static class SecRole implements GrantedAuthority{

		private static final long serialVersionUID = 1L;
		
		private String authority;

		SecRole(Role role) {
			this.authority = role.getRoleName();
		}
		@Override
		public String getAuthority() {
			return authority;
		}
	}
}