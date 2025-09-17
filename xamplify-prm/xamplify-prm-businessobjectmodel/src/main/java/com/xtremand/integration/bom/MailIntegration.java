package com.xtremand.integration.bom;

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

import org.hibernate.annotations.Type;

import com.xtremand.user.bom.User;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name="xt_mail_integration")
public class MailIntegration {
	
	private static final String SEQUENCE = "xt_mail_integration_sequence";
	
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = SEQUENCE)
	@SequenceGenerator(name = SEQUENCE, sequenceName = SEQUENCE, allocationSize = 1)
	private Integer id;
	
	@Column(name = "type")
	@Type(type = "com.xtremand.integration.bom.MailIntegrationTypeType")
	private MailIntegrationTypeEnum type;
	
	@Column(name = "access_token")
	private String accessToken;
	
	@Column(name = "refresh_token")
	private String refreshToken;
	
	@Temporal(TemporalType.TIMESTAMP)
	private Date expiry;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "created_by")
	private User createdBy;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "updated_by")
	private User updatedBy;
	
	@Column(name = "active")
	private Boolean active;
	
	@Column(name = "external_email_id")
	private String externalEmailId;
	
	@Column(name = "external_name")
	private String externalName;
	
	@Column(name = "created_time", columnDefinition = "DATETIME")
	@Temporal(TemporalType.TIMESTAMP)
	private Date createdTime;

	@Column(name = "updated_time", columnDefinition = "DATETIME")
	@Temporal(TemporalType.TIMESTAMP)
	private Date updatedTime;
}
