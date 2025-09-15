package com.xtremand.views;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.xtremand.util.dto.DateInString;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;

@Entity
@Table(name = "v_registered_and_recent_logged_in_users")
@Data
public class RegisteredAndRecentLoggedInUsersView {
	
	
	@Id
	@Column(name="user_id")
	private Integer userId;
	
	@Column(name="company_id")
	private Integer companyId;
	
	@Column(name="company_name")
	private String companyName;
	
	@Column(name="email_id")
	private String emailId;
	
	@Column(name="first_name")
	private String firstName;
	
	@Column(name="last_name")
	private String lastName;
	
	@JsonIgnore
	@Column(name="datereg")
	private Date registeredOn;
	
	
	@JsonIgnore
	@Column(name="datelastnav")
	private Date lastLoginOn;
	
	@Getter(value = AccessLevel.NONE)
	@Transient
	private String registeredOnInUTCString;
	
	@Getter(value = AccessLevel.NONE)
	@Transient
	private String lastLoginInUTCString;

	public String getRegisteredOnInUTCString() {
		if(registeredOn!=null) {
			return DateInString.getUtcString(registeredOn);
		}else {
			return "";
		}
	}

	public String getLastLoginInUTCString() {
		if(lastLoginOn!=null) {
			return DateInString.getUtcString(lastLoginOn);
		}else {
			return "";
		}
	}

}
