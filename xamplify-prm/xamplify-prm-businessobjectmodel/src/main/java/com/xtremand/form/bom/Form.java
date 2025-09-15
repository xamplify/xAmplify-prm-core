package com.xtremand.form.bom;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.springframework.util.StringUtils;

import com.xtremand.common.bom.CompanyProfile;
import com.xtremand.form.submit.bom.FormSubmit;
import com.xtremand.util.bom.XamplifyDefaultColumn;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "xt_form")
public class Form extends XamplifyDefaultColumn implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -9062789549778036798L;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "xt_form_sequence")
	@SequenceGenerator(name = "xt_form_sequence", sequenceName = "xt_form_sequence", allocationSize = 1)
	@Column(name = "id")
	private Integer id;

	@Column(name = "form_name")
	private String formName;

	private String description;

	private String alias;

	@Column(name = "is_default")
	private boolean defaultForm;

	@Column(name = "background_color")
	private String backgroundColor;

	@Column(name = "label_color")
	private String labelColor;

	@Column(name = "button_value")
	private String buttonValue;

	@Column(name = "button_value_color")
	private String buttonValueColor;

	@Column(name = "button_color")
	private String buttonColor;

	@Column(name = "form_submit_message")
	private String formSubmitMessage;

	@Column(name = "background_image")
	private String backgroundImage;

	@Column(name = "company_logo")
	private String companyLogo;

	@Column(name = "show_company_logo")
	private boolean showCompanyLogo;

	@Column(name = "footer")
	private String footer;

	@Column(name = "show_footer")
	private boolean showFooter;

	@Column(name = "title_color")
	private String titleColor;

	@Column(name = "border_color")
	private String borderColor;

	@Column(name = "page_background_color")
	private String pageBackgroundColor;

	@Column(name = "show_background_image")
	private boolean showBackgroundImage;

	@Column(name = "show_captcha")
	private boolean showCaptcha;

	@Column(name = "is_quiz_form")
	private boolean quizForm;

	@Column(name = "form_type")
	@org.hibernate.annotations.Type(type = "com.xtremand.form.bom.FormType")
	private FormTypeEnum formTypeEnum;

	@Column(name = "form_sub_type")
	@org.hibernate.annotations.Type(type = "com.xtremand.form.bom.FormSubType")
	private FormSubTypeEnum formSubTypeEnum;

	@Fetch(FetchMode.SUBSELECT)
	@OneToMany(mappedBy = "form", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private List<FormLabel> formLabels = new ArrayList<>();

	@Fetch(FetchMode.SUBSELECT)
	@OneToMany(mappedBy = "form", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private List<FormSubmit> formSubmits = new ArrayList<>();

	@ManyToOne
	@JoinColumn(name = "company_id")
	private CompanyProfile companyProfile;

	@Column(name = "open_link_in_new_tab")
	@Setter(value = AccessLevel.NONE)
	private boolean openLinkInNewTab;

	@Column(name = "form_submission_url")
	@Setter(value = AccessLevel.NONE)
	@Getter(value = AccessLevel.NONE)
	private String formSubmissionUrl;

	@Column(name = "max_score")
	private Integer maxScore;

	@Column(name = "thumbnail_image")
	private String thumbnailImage;

	@OneToMany(mappedBy = "form", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private Set<FormTeamGroupMapping> formTeamGroupMappings;

	@Column(name = "show_title_header")
	private boolean showTitleHeader;

	@Column(name = "description_color")
	private String descriptionColor;

	// XNFR-611
	@Column(name = "crm_deal_form_description")
	private String dealFormHeader;

	@Transient
	private String vendorCompanyFormAlias;

	@Transient
	private String requestTitle;

	@Transient
	private String requestAmount;

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public void setFormSubmissionUrl(String formSubmissionUrl) {
		if (StringUtils.hasText(formSubmissionUrl) && formSubmissionUrl.length() > 2082) {
			this.formSubmissionUrl = formSubmissionUrl.substring(0, 2082);
		} else {
			this.formSubmissionUrl = formSubmissionUrl;
		}

	}

	public String getFormSubmissionUrl() {
		if (StringUtils.hasText(formSubmissionUrl)) {
			return formSubmissionUrl;
		} else {
			return "";
		}

	}

	public void setOpenLinkInNewTab(boolean openLinkInNewTab) {
		if (!StringUtils.hasText(formSubmissionUrl)) {
			this.openLinkInNewTab = false;
		} else {
			this.openLinkInNewTab = openLinkInNewTab;
		}

	}

}
