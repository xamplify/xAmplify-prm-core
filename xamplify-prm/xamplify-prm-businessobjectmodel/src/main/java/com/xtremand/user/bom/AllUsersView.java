package com.xtremand.user.bom;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;

@Entity
@Table(name="v_all_users")
@Data
public class AllUsersView implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 4168812355068371775L;

	@Id
	@Column(name="user_id")
	private Integer userId;
	
	@Column(name="email_id")
	private String emailId;
	
	@Column(name="first_name")
	private String firstName;
	
	@Column(name="last_name")
	private String lastName;
	
	@Column(name="roles_in_string")
	@JsonIgnore
	private String rolesInString;
	
	@Column(name="roles_count")
	@JsonIgnore
	private BigInteger rolesCount;
	
	@Column(name="is_team_member")
	private boolean teamMember;
	
	@Column(name="org_admin_id")
	@JsonIgnore
	private Integer orgAdminId;
	
	@Transient
	private String roleName;
	
	@Transient
	@Getter(value = AccessLevel.NONE)
	@JsonIgnore
	private List<Integer> roleIds;

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public List<Integer> getRoleIds() {
		List<String> roleIdsArray = Arrays.asList(rolesInString.split(","));
		List<Integer> roleIdsInInteger = new ArrayList<>(roleIdsArray.size());
		for (String role : roleIdsArray) {
			roleIdsInInteger.add(Integer.valueOf(role));
		}
		return roleIdsInInteger;
	}
	
	
	

}
