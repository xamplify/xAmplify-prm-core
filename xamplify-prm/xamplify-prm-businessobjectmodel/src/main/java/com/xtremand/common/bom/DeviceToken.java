package com.xtremand.common.bom;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.xtremand.user.bom.User;

@Entity
@Table(name="xt_device_token")
public class DeviceToken extends XamplifyTimeStamp{
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	Integer id;
	@Column(name="token_id")
	String tokenId;
	@Column(name="device_type")
	@Enumerated(EnumType.STRING)
	DEVICE_TYPE deviceType;
	
	@ManyToOne
	@JoinColumn(name="user_id")
	User user;
	
	public enum DEVICE_TYPE{
		WEB, ANDROID,IOS 
	}
	
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	
	public String getTokenId() {
		return tokenId;
	}
	public void setTokenId(String tokenId) {
		this.tokenId = tokenId;
	}
 
	public DEVICE_TYPE getDeviceType() {
		return deviceType;
	}
	public void setDeviceType(DEVICE_TYPE deviceType) {
		this.deviceType = deviceType;
	}
	public User getUser() {
		return user;
	}
	public void setUser(User user) {
		this.user = user;
	}
	
}
