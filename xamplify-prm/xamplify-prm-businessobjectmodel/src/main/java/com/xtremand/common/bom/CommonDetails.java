package com.xtremand.common.bom;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.xtremand.user.bom.User;

@MappedSuperclass
public  class CommonDetails {
	
	
	@Column(name = "created_time", columnDefinition="DATETIME")
	@Temporal(TemporalType.TIMESTAMP)
	private Date createdTime;
	
	
	@Column(name = "updated_time", columnDefinition="DATETIME",nullable=true)
	@Temporal(TemporalType.TIMESTAMP)
	private Date updatedTime;
	
	
	@ManyToOne
	@JoinColumn(name="created_by", referencedColumnName="user_id")
	private User createdUser;
	
	@ManyToOne
	@JoinColumn(name="updated_by", referencedColumnName="user_id",nullable=true)
	private User updatedUser;




	public Date getCreatedTime() {
		return createdTime;
	}


	public void setCreatedTime(Date createdTime) {
		this.createdTime = createdTime;
	}


	public Date getUpdatedTime() {
		return updatedTime;
	}


	public void setUpdatedTime(Date updatedTime) {
		this.updatedTime = updatedTime;
	}


	public User getCreatedUser() {
		return createdUser;
	}


	public void setCreatedUser(User createdUser) {
		this.createdUser = createdUser;
	}


	public User getUpdatedUser() {
		return updatedUser;
	}


	public void setUpdatedUser(User updatedUser) {
		this.updatedUser = updatedUser;
	}

	
	public void initializeTime(boolean isCreate,User user){
		if(isCreate){
			this.setCreatedTime(new Date());
			this.setCreatedUser(user);
		}else{
			this.setUpdatedTime(new Date());
			this.setUpdatedUser(user);
		}
			
	}
	


}
