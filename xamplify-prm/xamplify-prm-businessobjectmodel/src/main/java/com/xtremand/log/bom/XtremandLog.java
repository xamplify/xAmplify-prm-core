package com.xtremand.log.bom;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;

@Entity
@Table(name="xt_xtremand_log")
public class XtremandLog {
	
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator="xtremand_log_id_seq")
	@SequenceGenerator(
			name="xtremand_log_id_seq",
			sequenceName="xtremand_log_id_seq",
			allocationSize=1
			)
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
	
	private String os;
	private String city;
	private String country;
	private String isp;
	
	@Column(name="ip_address")
	private String ipAddress;
	
	private String state;
	private String zip;
	private String latitude;
	private String longitude;
	
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
	
	
//	@Column(name="message_service_type ")
//	private XtremandLog.MESSAGESERVICETYPE messageServiceType;
//	
//	
//	
//	
//
//	public XtremandLog.MESSAGESERVICETYPE getMessageServiceType() {
//		return messageServiceType;
//	}
//
//	public void setMessageServiceType(XtremandLog.MESSAGESERVICETYPE messageServiceType) {
//		this.messageServiceType = messageServiceType;
//	}
	
	
	public enum MESSAGESERVICETYPE {
		EMAIL("EMAIL"), SMS("SMS");

		protected String messageServiceType;

		private MESSAGESERVICETYPE(String messageServiceType) {
			this.messageServiceType = messageServiceType;
		}

		public String getMessageServiceType() {
			return messageServiceType;
		}

	}

	
	@Transient
	private String firstName;
	@Transient
	private String lastName;
	@Transient
	private String userAlias;
	@Transient
	private String videoAlias;
	@Transient
	private String campaignAlias;
	@Transient
	private String emailId;
	
	@Transient
	private String campaignName;
	
	@Transient
	private String userEmailId;
	
	@Transient
	private String startDurationHHMMSS;

	@Transient
	private String stopDurationHHMMSS;
	
	@Transient
	private String videoTitle;
	
	@Transient
	private Integer previousId;
	
	@Transient
	private String alias;
	
	
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

	public String getOs() {
		return os;
	}

	public void setOs(String os) {
		this.os = os;
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

	public String getIpAddress() {
		return ipAddress;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
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

	public String getCountryCode() {
		return countryCode;
	}

	public void setCountryCode(String countryCode) {
		this.countryCode = countryCode;
	}

	public String getUserAlias() {
		return userAlias;
	}

	public void setUserAlias(String userAlias) {
		this.userAlias = userAlias;
	}

	public String getVideoAlias() {
		return videoAlias;
	}

	public void setVideoAlias(String videoAlias) {
		this.videoAlias = videoAlias;
	}

	public String getCampaignAlias() {
		return campaignAlias;
	}

	public void setCampaignAlias(String campaignAlias) {
		this.campaignAlias = campaignAlias;
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

	public String getStartDurationHHMMSS() {
		return startDurationHHMMSS;
	}

	public void setStartDurationHHMMSS(String startDurationHHMMSS) {
		this.startDurationHHMMSS = startDurationHHMMSS;
	}

	public String getStopDurationHHMMSS() {
		return stopDurationHHMMSS;
	}

	public void setStopDurationHHMMSS(String stopDurationHHMMSS) {
		this.stopDurationHHMMSS = stopDurationHHMMSS;
	}

	public String getEmailId() {
		return emailId;
	}

	public void setEmailId(String emailId) {
		this.emailId = emailId;
	}

	

	public String getCampaignName() {
		return campaignName;
	}

	public void setCampaignName(String campaignName) {
		this.campaignName = campaignName;
	}

	public String getUserEmailId() {
		return userEmailId;
	}

	public void setUserEmailId(String userEmailId) {
		this.userEmailId = userEmailId;
	}

	public Integer getOpenCount() {
		return openCount;
	}

	public void setOpenCount(Integer openCount) {
		this.openCount = openCount;
	}

	public String getVideoTitle() {
		return videoTitle;
	}

	public void setVideoTitle(String videoTitle) {
		this.videoTitle = videoTitle;
	}

	public Integer getPreviousId() {
		return previousId;
	}

	public void setPreviousId(Integer previousId) {
		this.previousId = previousId;
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

	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

	@Override
	public String toString() {
		return "XtremandLog [id=" + id + ", userId=" + userId + ", videoId=" + videoId + ", campaignId=" + campaignId
				+ ", actionId=" + actionId + ", startTime=" + startTime + ", endTime=" + endTime + ", deviceType="
				+ deviceType + ", os=" + os + ", city=" + city + ", country=" + country + ", isp=" + isp
				+ ", ipAddress=" + ipAddress + ", state=" + state + ", zip=" + zip + ", latitude=" + latitude
				+ ", longitude=" + longitude + ", countryCode=" + countryCode + ", startDuration=" + startDuration
				+ ", stopDuration=" + stopDuration + ", sessionId=" + sessionId + ", openCount=" + openCount
				+ ", firstName=" + firstName + ", lastName=" + lastName + ", userAlias=" + userAlias + ", videoAlias="
				+ videoAlias + ", campaignAlias=" + campaignAlias + ", emailId=" + emailId + ", campaignName="
				+ campaignName + ", userEmailId=" + userEmailId + ", startDurationHHMMSS=" + startDurationHHMMSS
				+ ", stopDurationHHMMSS=" + stopDurationHHMMSS + ", videoTitle=" + videoTitle + ", previousId="
				+ previousId + ", alias=" + alias + "]";
	}
}
