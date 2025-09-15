package com.xtremand.user.bom;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.hibernate.annotations.Immutable;

import com.xtremand.user.bom.User.UserStatus;

import lombok.Data;

@Entity
@Immutable
@Table(name = "list_all_accounts_view")
@Data
public class AllAccountsView implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2384812798748805100L;

	@Id
	@Column(name = "user_id")
	private Integer userId;

	@Column(name = "company_name")
	private String companyName;

	@Column(name = "company_id")
	private Integer companyId;

	@Column(name = "company_profile_name")
	private String companyProfileName;

	@Column(name = "email_id")
	private String emailId;

	@Column(name = "firstname")
	private String firstName;

	@Column(name = "lastname")
	private String lastName;

	@Column(name = "status")
	@org.hibernate.annotations.Type(type = "com.xtremand.user.bom.UserStatusType")
	private UserStatus status;

	@Column(name = "role_id")
	private String roleIdsInString;

	@Column(name = "role_name")
	private String roleNamesInString;

	@Column(name = "created_time", columnDefinition = "DATETIME")
	@Temporal(TemporalType.TIMESTAMP)
	private Date createdTime;

	@Transient
	private String roleName;

	@Transient
	private String vanityUrlLink;

}
