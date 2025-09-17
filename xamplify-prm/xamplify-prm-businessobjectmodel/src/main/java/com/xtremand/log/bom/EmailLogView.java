package com.xtremand.log.bom;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.hibernate.annotations.Immutable;

import lombok.Data;

@Entity
@Table(name = "v_xt_email_log")
@Immutable
@Data
public class EmailLogView {
	@Id
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

	@Column(name = "ip_address")
	private String ipAddress;

	@Column(name = "country_code")
	private String countryCode;

	@Column(name = "reply_id")
	private Integer replyId;

	@Column(name = "url_id")
	private Integer urlId;

	@Column(name = "open_count")
	private Integer openCount;

	@Column(name = "firstname")
	private String firstName;

	@Column(name = "lastname")
	private String lastName;

	@Column(name = "email_id")
	private String emailId;

	private String os;
	private String city;
	private String country;
	private String isp;
	private String state;
	private String zip;
	private String latitude;
	private String longitude;
	private String subject;
	
	@Transient
	private Integer emailActionCount;
	
	@Transient
	private String utcTimeString;
	
	@Column(name = "companyname")
	private String companyName;

}