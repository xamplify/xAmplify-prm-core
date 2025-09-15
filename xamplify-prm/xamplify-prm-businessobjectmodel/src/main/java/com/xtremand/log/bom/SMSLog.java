package com.xtremand.log.bom;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

@Entity
@Table(name = "xt_sms_log")
public class SMSLog {
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sms_log_id_seq")
	@SequenceGenerator(
			name = "sms_log_id_seq",
			sequenceName = "sms_log_id_seq",
			allocationSize = 1
			)
	@Column(name = "id")
	private Integer id;

	@Column(name = "user_id")
	private Integer userId;

	@Column(name = "video_id")
	private Integer videoId;

	@Column(name = "action_id")
	private Integer actionId;

	@Column(name = "campaign_id")
	private Integer campaignId;

	@Column(name = "time", columnDefinition = "DATETIME")
	@Temporal(TemporalType.TIMESTAMP)
	private Date time;

	@Column(name = "clicked_url")
	private String url;

	@Column(name = "device_type")
	private String deviceType;

	private String os;
	private String city;
	private String country;
	private String isp;

	@Column(name = "ip_address")
	private String ipAddress;

	private String state;
	private String zip;
	private String latitude;
	private String longitude;

	@Column(name = "country_code")
	private String countryCode;

	@Column(name = "reply_id")
	private Integer replyId;

	@Column(name = "url_id")
	private Integer urlId;
	
	private String subject;

	@Transient private String firstName;
	@Transient private String lastName;
	@Transient private String userAlias;
	@Transient private String campaignAlias;
	@Transient private String emailId;
	@Transient private String userEmailId;
	@Transient private String campaign;
	@Transient private String videoAlias;
	@Transient private Integer templateId;
	@Transient private String alias;
	@Transient private String utcTimeString;
	@Transient private String companyName;


	@Column(name = "open_count")
	private Integer openCount;

	@Transient private String sessionId;

	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getVideoId() {
		return videoId;
	}

	public void setVideoId(Integer videoId) {
		this.videoId = videoId;
	}

	public Integer getActionId() {
		return actionId;
	}

	public void setActionId(Integer actionId) {
		this.actionId = actionId;
	}

	public Integer getCampaignId() {
		return campaignId;
	}

	public void setCampaignId(Integer campaignId) {
		this.campaignId = campaignId;
	}

	public Date getTime() {
		return time;
	}

	public void setTime(Date time) {
		this.time = time;
	}

	public Integer getUserId() {
		return userId;
	}

	public void setUserId(Integer userId) {
		this.userId = userId;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
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

	public String getCampaignAlias() {
		return campaignAlias;
	}

	public void setCampaignAlias(String campaignAlias) {
		this.campaignAlias = campaignAlias;
	}

	public String getEmailId() {
		return emailId;
	}

	public void setEmailId(String emailId) {
		this.emailId = emailId;
	}

	public String getUserEmailId() {
		return userEmailId;
	}

	public void setUserEmailId(String userEmailId) {
		this.userEmailId = userEmailId;
	}

	public String getCampaign() {
		return campaign;
	}

	public void setCampaign(String campaign) {
		this.campaign = campaign;
	}

	public Integer getOpenCount() {
		return openCount;
	}

	public void setOpenCount(Integer openCount) {
		this.openCount = openCount;
	}

	public String getVideoAlias() {
		return videoAlias;
	}

	public void setVideoAlias(String videoAlias) {
		this.videoAlias = videoAlias;
	}

	public Integer getTemplateId() {
		return templateId;
	}

	public void setTemplateId(Integer templateId) {
		this.templateId = templateId;
	}

	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

	public Integer getReplyId() {
		return replyId;
	}

	public void setReplyId(Integer replyId) {
		this.replyId = replyId;
	}

	public Integer getUrlId() {
		return urlId;
	}

	public void setUrlId(Integer urlId) {
		this.urlId = urlId;
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

	public String getUtcTimeString() {
		return utcTimeString;
	}

	public void setUtcTimeString(String utcTimeString) {
		this.utcTimeString = utcTimeString;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	@Override
	public String toString() {
		return "SMSLog [id=" + id + ", userId=" + userId + ", videoId=" + videoId + ", actionId=" + actionId
				+ ", campaignId=" + campaignId + ", time=" + time + ", url=" + url + ", deviceType=" + deviceType
				+ ", os=" + os + ", city=" + city + ", country=" + country + ", isp=" + isp + ", ipAddress=" + ipAddress
				+ ", state=" + state + ", zip=" + zip + ", latitude=" + latitude + ", longitude=" + longitude
				+ ", countryCode=" + countryCode + ", replyId=" + replyId + ", urlId=" + urlId + ", subject=" + subject
				+ ", firstName=" + firstName + ", lastName=" + lastName + ", userAlias=" + userAlias
				+ ", campaignAlias=" + campaignAlias + ", emailId=" + emailId + ", userEmailId=" + userEmailId
				+ ", campaign=" + campaign + ", videoAlias=" + videoAlias + ", templateId=" + templateId + ", alias="
				+ alias + ", utcTimeString=" + utcTimeString + ", openCount=" + openCount + ", sessionId=" + sessionId
				+ ", getSessionId()=" + getSessionId() + ", getId()=" + getId() + ", getVideoId()=" + getVideoId()
				+ ", getActionId()=" + getActionId() + ", getCampaignId()=" + getCampaignId() + ", getTime()="
				+ getTime() + ", getUserId()=" + getUserId() + ", getUrl()=" + getUrl() + ", getDeviceType()="
				+ getDeviceType() + ", getOs()=" + getOs() + ", getCity()=" + getCity() + ", getCountry()="
				+ getCountry() + ", getIsp()=" + getIsp() + ", getIpAddress()=" + getIpAddress() + ", getState()="
				+ getState() + ", getZip()=" + getZip() + ", getLatitude()=" + getLatitude() + ", getLongitude()="
				+ getLongitude() + ", getCountryCode()=" + getCountryCode() + ", getUserAlias()=" + getUserAlias()
				+ ", getCampaignAlias()=" + getCampaignAlias() + ", getEmailId()=" + getEmailId()
				+ ", getUserEmailId()=" + getUserEmailId() + ", getCampaign()=" + getCampaign() + ", getOpenCount()="
				+ getOpenCount() + ", getVideoAlias()=" + getVideoAlias() + ", getTemplateId()=" + getTemplateId()
				+ ", getAlias()=" + getAlias() + ", getReplyId()=" + getReplyId() + ", getUrlId()=" + getUrlId()
				+ ", getFirstName()=" + getFirstName() + ", getLastName()=" + getLastName() + ", getUtcTimeString()="
				+ getUtcTimeString() + ", getSubject()=" + getSubject() + ", getClass()=" + getClass() + ", hashCode()="
				+ hashCode() + ", toString()=" + super.toString() + "]";
	}
}
