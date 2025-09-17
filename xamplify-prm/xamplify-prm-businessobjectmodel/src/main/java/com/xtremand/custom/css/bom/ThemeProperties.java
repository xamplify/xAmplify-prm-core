package com.xtremand.custom.css.bom;

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

import com.xtremand.util.bom.XamplifyDefaultColumn;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode(callSuper=false)
@Entity
@Table(name="xt_theme_properties")
public class ThemeProperties  extends XamplifyDefaultColumn implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2027693398302311817L;
	
	
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator="xt_theme_properties_sequence")
	@SequenceGenerator(
			name="xt_theme_properties_sequence",
			sequenceName="xt_theme_properties_sequence",
			allocationSize=1
			)
	@Column(name = "id")
	private Integer id;
	
  
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name="theme_id",referencedColumnName = "id")
	private Theme theme;

	
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
	
	@Column(name = "table_header_color")
	private String tableHeaderColor;
	
	@Column(name = "table_body_color")
	private String tableBodyColor;
	
	@Column(name = "border_color")
	private String buttonBorderColor;
	
	
	@Column(name = "button_color")
	private String buttonColor;
	
	@Column(name = "button_value_color")
	private String buttonValueColor;
	
	@Column(name = "btn_primary_border_color")
	private String buttonPrimaryBorderColor;
	
	@Column(name = "btn_secondary_background_color")
	private String buttonSecondaryColor;
	
	@Column(name = "btn_secondary_border_color")
	private String buttonSecondaryBorderColor;
	
	@Column(name = "btn_secondary_text_color")
	private String buttonSecondaryTextColor;
	
	@Column(name = "text_content")
	private String textContent;
	
	@Column(name = "module_name", nullable = false)
	@org.hibernate.annotations.Type(type = "com.xtremand.custom.css.bom.CustomModuleType")
	private CustomModule moduleType;
	
	
	@Column(name = "is_show_footer")
	private boolean showFooter;
	
	@Column(name = "div_color")
	private String divBgColor;
	
	@Column(name = "button_gradiant_colorone")
	private String gradiantColorOne;
	
	@Column(name = "button_gradiant_colortwo")
	private String gradiantColorTwo;
	 
}
