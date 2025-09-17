package com.xtremand.custom.css.bom;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import com.xtremand.common.bom.CompanyProfile;
import com.xtremand.util.bom.XamplifyDefaultColumn;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode(callSuper=false)
@Entity
@Table(name="xt_custom_skin")
public class CustomSkin extends XamplifyDefaultColumn implements Serializable {

	
	/**
	 * 
	 */
	private static final long serialVersionUID = 2027693398302311817L;
	
	
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator="xt_custom_skin_sequence")
	@SequenceGenerator(
			name="xt_custom_skin_sequence",
			sequenceName="xt_custom_skin_sequence",
			allocationSize=1
			)
	@Column(name = "id")
	private Integer id;
	
	@Column(name = "text_color")
	private String textColor; 
	
	@Column(name = "icon_color")
	private String iconColor;
	
	@Column(name = "icon_border_color")
	private String iconBorderColor;
	
	@Column(name = "icon_hover_color")
	private String iconHoverColor;
	
	@Column(name = "background_color")
	private String backgroundColor;
	
	@Column(name = "button_border_color")
	private String buttonBorderColor;
	
	@Column(name = "font_family")
	private String fontFamily;
	
	@Column(name = "button_color")
	private String buttonColor;
	
	@Column(name = "button_value_color")
	private String buttonValueColor;
	
	@Column(name = "text_content")
	private String textContent;
	
	@ManyToOne
	@JoinColumn(name="company_id")
	private CompanyProfile companyProfile;
	
	@Column(name = "module_name", nullable = false)
	@org.hibernate.annotations.Type(type = "com.xtremand.custom.css.bom.CustomModuleType")
	private CustomModule moduleType;
	
	@Column(name = "is_default")
	private boolean defaultSkin;
	
	@Column(name = "is_show_footer")
	private boolean showFooter;
	
	@Column(name = "div_color")
	private String divBgColor;
	

	

}
