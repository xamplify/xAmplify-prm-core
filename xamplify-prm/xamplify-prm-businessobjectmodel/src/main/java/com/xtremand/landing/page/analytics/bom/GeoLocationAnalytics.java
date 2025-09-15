package com.xtremand.landing.page.analytics.bom;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import com.xtremand.dam.bom.DamAnalytics;
import com.xtremand.form.submit.bom.FormSubmit;

import lombok.Data;
@Entity
@Table(name="xt_geo_location_analytics")
@Data
public class GeoLocationAnalytics implements Serializable {
	
	private static final long serialVersionUID = 947287291145715178L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "xt_geo_location_analytics_sequence")
	@SequenceGenerator(name = "xt_geo_location_analytics_sequence", sequenceName = "xt_geo_location_analytics_sequence", allocationSize = 1)
	@Column(name = "id")
	private Integer id;
	
	
	@Column(name="device_type")
	private String deviceType;
	
	private String os;
	
	private String city;
	
	private String state;
	
	private String zip;
	
	private String country;
	
	private String isp;
	
	@Column(name="ip_address")
	private String ipAddress;
	
	private String latitude;
	
	private String longitude;
	
	@Column(name="country_code")
	private String countryCode;
	
	@Column(name="time_zone")
	private String timezone;
	
	@Column(name="opened_time")
	private Date openedTime;
	
	@Column(name="campaign_id")
	private Integer campaignId;
	
	@Column(name="user_id")
	private Integer userId;
	
	@Column(name="form_id")
	private Integer formId;
	
	@Column(name="landing_page_id")
	private Integer landingPageId;
	
	@Column(name="form_landing_page_id")
	private Integer formLandingPageId;
	
	@Column(name="partner_company_id")
	private Integer partnerCompanyId;
	
	@Column(name="url")
	private String url;
	
	@ManyToOne
	@JoinColumn(name="dam_analytics_id")
	private DamAnalytics damAnalytics;
	
	@OneToOne
    @JoinColumn(name = "form_submit_id", referencedColumnName = "id")
    private FormSubmit formSubmit;
	
	@Column(name="geo_location_analytics_type")
	@org.hibernate.annotations.Type(type = "com.xtremand.landing.page.analytics.bom.GeoLocationAnalyticsType")
	private GeoLocationAnalyticsEnum analyticsType;



}
