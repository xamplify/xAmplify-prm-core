package com.xtremand.log.bom;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Immutable;

@Entity
@Table(name = "v_xt_xtremand_log")
@Immutable
public class XtremandLogView {
	
	@Id
	@Column(name = "id")
	private Integer id;
	
	@Column(name="user_id")
	private Integer userId;
	
	@Column(name="video_id")
	private Integer videoId;
	
	@Column(name="campaign_id")
	private Integer campaignId;
	
	@Column(name = "action_id")
	private Integer actionId;	
	
	@Column(name="start_time")
	private Date startTime;
	
	@Column(name="end_time")
	private Date endTime;
	
	@Column(name="device_type")
	private String deviceType;
	
	@Column(name="ip_address")
	private String ipAddress;
		
	@Column(name="country_code")
	private String countryCode;
	
	@Column(name="start_duration")
	private Integer startDuration;
	
	@Column(name="stop_duration")
	private Integer stopDuration;
	
	@Column(name="session_id")
	private String sessionId;
	
	@Column(name="open_count")
	private Integer openCount;
	
	@Column(name = "firstname")
	private String firstName;
	
	@Column(name = "lastname")
	private String lastName;
	
	@Column(name = "email_id")
	private String emailId;	
	
	@Column(name = "companyname")
	private String companyName;

	private String os;
	private String city;
	private String country;
	private String isp;
	private String state;
	private String zip;
	private String latitude;
	private String longitude;
	
	@Transient
	private String startTimeUtcString;
	
	@Transient
	private String endTimeUtcString;
	
	
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public Integer getUserId() {
		return userId;
	}
	public void setUserId(Integer userId) {
		this.userId = userId;
	}
	public Integer getVideoId() {
		return videoId;
	}
	public void setVideoId(Integer videoId) {
		this.videoId = videoId;
	}
	public Integer getCampaignId() {
		return campaignId;
	}
	public void setCampaignId(Integer campaignId) {
		this.campaignId = campaignId;
	}
	public Integer getActionId() {
		return actionId;
	}
	public void setActionId(Integer actionId) {
		this.actionId = actionId;
	}
	public Date getStartTime() {
		return startTime;
	}
	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}
	public Date getEndTime() {
		return endTime;
	}
	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}
	public String getDeviceType() {
		return deviceType;
	}
	public void setDeviceType(String deviceType) {
		this.deviceType = deviceType;
	}
	public String getIpAddress() {
		return ipAddress;
	}
	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}
	public String getCountryCode() {
		return countryCode;
	}
	public void setCountryCode(String countryCode) {
		this.countryCode = countryCode;
	}
	public Integer getStartDuration() {
		return startDuration;
	}
	public void setStartDuration(Integer startDuration) {
		this.startDuration = startDuration;
	}
	public Integer getStopDuration() {
		return stopDuration;
	}
	public void setStopDuration(Integer stopDuration) {
		this.stopDuration = stopDuration;
	}
	public String getSessionId() {
		return sessionId;
	}
	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}
	public Integer getOpenCount() {
		return openCount;
	}
	public void setOpenCount(Integer openCount) {
		this.openCount = openCount;
	}
	public String getFirstName() {
		return firstName;
	}
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}
	public String getLastName() {
		return lastName;
	}
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	public String getEmailId() {
		return emailId;
	}
	public void setEmailId(String emailId) {
		this.emailId = emailId;
	}
	public String getOs() {
		return os;
	}
	public void setOs(String os) {
		this.os = os;
	}
	public String getCity() {
		return city;
	}
	public void setCity(String city) {
		this.city = city;
	}
	public String getCountry() {
		return country;
	}
	public void setCountry(String country) {
		this.country = country;
	}
	public String getIsp() {
		return isp;
	}
	public void setIsp(String isp) {
		this.isp = isp;
	}
	public String getState() {
		return state;
	}
	public void setState(String state) {
		this.state = state;
	}
	public String getZip() {
		return zip;
	}
	public void setZip(String zip) {
		this.zip = zip;
	}
	public String getLatitude() {
		return latitude;
	}
	public void setLatitude(String latitude) {
		this.latitude = latitude;
	}
	public String getLongitude() {
		return longitude;
	}
	public void setLongitude(String longitude) {
		this.longitude = longitude;
	}
	public String getStartTimeUtcString() {
		return startTimeUtcString;
	}
	public void setStartTimeUtcString(String startTimeUtcString) {
		this.startTimeUtcString = startTimeUtcString;
	}
	public String getEndTimeUtcString() {
		return endTimeUtcString;
	}
	public void setEndTimeUtcString(String endTimeUtcString) {
		this.endTimeUtcString = endTimeUtcString;
	}
	public String getCompanyName() {
		return companyName;
	}
	public void setCompanyName(String companyName) {
		this.companyName = companyName;
	}
}
