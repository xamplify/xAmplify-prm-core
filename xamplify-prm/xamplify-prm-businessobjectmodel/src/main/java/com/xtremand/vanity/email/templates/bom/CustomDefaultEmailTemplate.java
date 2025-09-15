package com.xtremand.vanity.email.templates.bom;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import com.xtremand.common.bom.CompanyProfile;
import com.xtremand.user.bom.User;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name="xt_custom_default_templates")
public class CustomDefaultEmailTemplate implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "xt_custom_default_templates_sequence")
	@SequenceGenerator(name = "xt_custom_default_templates_sequence", sequenceName = "xt_custom_default_templates_sequence", allocationSize = 1)
	@Column(name="id")
	private Integer id;
	
	@ManyToOne
	@JoinColumn(name = "default_email_template_id")
	private DefaultEmailTemplate defaultEmailTemplate;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "company_id", referencedColumnName="company_id")
	private CompanyProfile companyProfile;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "created_user_id", referencedColumnName="user_id")
	private User createdUser;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "updated_user_id", referencedColumnName="user_id")
	private User updatedUser;
	
	@Column(name = "created_time", columnDefinition = "DATETIME")
	@Temporal(TemporalType.TIMESTAMP)
	private Date createdTimestamp;
	
	@Column(name = "updated_time", columnDefinition = "DATETIME")
	@Temporal(TemporalType.TIMESTAMP)
	private Date updatedTimestamp;
	
	@Column(name="json_body")
	private String jsonBody;	

	@Column(name="html_body")
	private String htmlBody;

	@Column(name="image_path")
	private String imagePath;
	
	@Column(name="spam_score")
	private String spamScore;

	@Column(name="image_sync")
	private Boolean imageSync;

	@Column(name = "image_sync_time", columnDefinition = "DATETIME")
	@Temporal(TemporalType.TIMESTAMP)
	private Date imageSyncTime;
	
	@Column(name="subject")
	private String subject;	
	
	@Transient
	private String cdnImagePath;


}
