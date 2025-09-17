package com.xtremand.user.bom;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.Immutable;

import lombok.Data;

@Entity
@Table(name="v_user_list_users_count")
@Immutable
@Data
public class UserListUsersCount {	
	@Id
	@Column(name="user_list_id")
	private Integer userListId;
	
	@Column(name="company_id")
	private Integer companyId;
	
	@Column(name="all")
	private Integer allUsersCount;
	
	@Column(name="active")
	private Integer activeUsersCount;
	
	@Column(name="nonactive")
	private Integer nonActiveUsersCount;
	
	@Column(name="invalid")
	private Integer invalidUsersCount;
	
	@Column(name="unsubscribe")
	private Integer unsubscribedUsersCount;	
}
