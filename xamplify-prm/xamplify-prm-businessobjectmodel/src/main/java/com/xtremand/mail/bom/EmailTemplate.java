package com.xtremand.mail.bom;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.springframework.util.StringUtils;

import com.xtremand.common.bom.Template;
import com.xtremand.common.bom.XamplifyTimeStamp;
import com.xtremand.user.bom.User;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(exclude = { "body", "user", "jsonBody", "vendor", "emailSpamScores" })
@Entity
@Table(name = "xt_email_templates")
public class EmailTemplate extends XamplifyTimeStamp implements Template, Serializable {
	private static final long serialVersionUID = 1L;
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "email_temp_id_seq")
	@SequenceGenerator(name = "email_temp_id_seq", sequenceName = "email_temp_id_seq", initialValue = 60, allocationSize = 1)
	Integer id;

	@Setter(value = AccessLevel.NONE)
	String name;
	String subject;

	@Setter(value = AccessLevel.NONE)
	@Getter(value = AccessLevel.NONE)
	String body;

	@Column(name = "describe")
	String desc;

	@Column(name = "langid")
	Integer langId;

	@OneToOne(cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	@JoinColumn(name = "user_id", nullable = true)
	private User user;

	@Column(name = "is_user_defined")
	private boolean userDefined;

	@Column(name = "json_body")
	String jsonBody;

	@Column(name = "is_default")
	private boolean defaultTemplate;

	@Column(name = "is_regular_template")
	private boolean regularTemplate;

	@Column(name = "is_video_template")
	private boolean videoTemplate;

	@Column(name = "type")
	@org.hibernate.annotations.Type(type = "com.xtremand.mail.bom.EmailTemplateTypeEnum")
	private EmailTemplateType type;

	@org.hibernate.annotations.Type(type = "com.xtremand.mail.bom.EmailTemplateSourceType")
	private EmailTemplateSource source;

	@Transient
	private String createdBy;

	@OneToOne(cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	@JoinColumn(name = "vendor_id", nullable = true)
	private User vendor;

	@Column(name = "parent_id")
	private Integer parentId;

	@Column(name = "is_draft")
	private boolean draft;

	@Column(name = "image_sync")
	private boolean imageSync;

	@Column(name = "image_sync_time")
	private Date imageSyncTime;

	@Transient
	private Integer videoId;

	@Transient
	private boolean onDestroy;

	@OneToMany(mappedBy = "emailTemplate", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	private Set<EmailSpamScore> emailSpamScores = new HashSet<>();

	@Column(name = "spam_score")
	private String spamScore;

	@Transient
	private Integer categoryId;

	@Transient
	private String category;

	@Transient
	private String domainName;


	@Transient
	private Integer vendorCompanyId;

	@Transient
	private Integer partnerCompanyId;

	@Transient
	private boolean whiteLabeledTemplate;

	/****** XNFR-330 *******/

	@Transient
	@Getter
	@Setter
	private String vendorCompanyLogoPath;

	@Transient
	@Getter
	@Setter
	private String partnerCompanyLogoPath;

	/****** XNFR-330 *******/

	public void setName(String name) {
		this.name = name;
	}

	public String getBody() {
		if (StringUtils.hasText(body)) {
			return body;
		} else {
			return "";
		}

	}

	public void setBody(String body) {
		if (StringUtils.hasText(body)) {
			this.body = body;
		} else {
			this.body = "";
		}

	}

	/**
	 * @return the serialversionuid
	 */
	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	@Transient
	private String cdnSubject;

	@Column(name = "created_for_company_id")
	private Integer createdForCompanyId;

	@Transient
	private String vendorOrganizationName;

}