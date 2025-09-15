package com.xtremand.custom.link.bom;

import java.io.Serializable;

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
import javax.persistence.Transient;

import com.xtremand.common.bom.CompanyProfile;
import com.xtremand.util.bom.XamplifyDefaultColumn;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "xt_custom_link")
@Data
@EqualsAndHashCode(callSuper = false)
public class CustomLink extends XamplifyDefaultColumn implements Serializable {
	/**
	* 
	*/
	private static final long serialVersionUID = -8103897608205002773L;

	private static final String SEQUENCE = "xt_custom_link_sequence";

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = SEQUENCE)
	@SequenceGenerator(name = SEQUENCE, sequenceName = SEQUENCE, allocationSize = 1)
	private Integer id;

	@Column(name = "title", nullable = false)
	private String title;

	@Column(name = "sub_title")
	private String subTitle;

	@Column(name = "link", nullable = false)
	private String link;

	private String icon;

	@Column(name = "open_link_in_new_tab")
	private boolean openLinkInNewTab;

	@Column(name = "banner_image_path")
	private String bannerImagePath;

	private String description;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "company_id", nullable = false)
	private CompanyProfile company;

	@Column(name = "custom_link_type", nullable = false)
	@org.hibernate.annotations.Type(type = "com.xtremand.custom.link.bom.CustomLinkTypeEnum")
	private CustomLinkType customLinkType;

	/**** XNFR-532 ***/
	@Column(name = "is_display_title")
	private boolean displayTitle;

	@Column(name = "button_text")
	private String buttonText;
	/**** XNFR-532 ***/

	@Transient
	@Getter
	@Setter
	private String cdnBannerImagePath;

}
