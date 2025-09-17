package com.xtremand.custom.css.bom;

import java.io.Serializable;
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
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.hibernate.annotations.Type;

import com.xtremand.common.bom.CompanyProfile;
import com.xtremand.util.bom.XamplifyDefaultColumn;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "xt_theme")
public class Theme extends XamplifyDefaultColumn implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2027693398302311817L;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "xt_theme_sequence")
	@SequenceGenerator(name = "xt_theme_sequence", sequenceName = "xt_theme_sequence", allocationSize = 1)
	@Column(name = "id")
	private Integer id;

	@Column(name = "name")
	private String name;
	
	@ManyToOne
	@JoinColumn(name="company_id")
	private CompanyProfile companyProfile;

	@Column(name = "description")
	private String description;
	
	@Column(name = "is_default")
	private boolean defaultTheme;
	
	/******Start XNFR-420 ****/
	@Column(name = "theme_image_path")
	private String themeImagePath;
	
	@Column(name = "parent_id")
	private Integer parentId;
	
	@Column(name = "background_image")
	private String backgroundImage;
	
	@Column(name = "parent_theme_name")
	@Type(type="com.xtremand.custom.css.bom.ThemeStatusType")
	private ThemeStatus parentThemeName;

	public enum ThemeStatus {
		LIGHT("LIGHT"), DARK("DARK"),NEUMORPHISMLIGHT("NEUMORPHISMLIGHT"),NEUMORPHISMDARK("NEUMORPHISMDARK"),
		GLASSMORPHISMLIGHT("GLASSMORPHISMLIGHT"),GLASSMORPHISMDARK("GLASSMORPHISMDARK");
		
		protected String status;
		
		private ThemeStatus(String status) {
			this.status = status;
		}
		public String getStatus() {
			return  status;
		}
		
	}	
	/*****End XNFR-420****/
	
	//private Set<ThemesProperties> themesProperties;
	
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "theme", cascade = CascadeType.ALL)
	private Set<ThemeProperties> themeProperties=new HashSet<ThemeProperties>();
	
}
