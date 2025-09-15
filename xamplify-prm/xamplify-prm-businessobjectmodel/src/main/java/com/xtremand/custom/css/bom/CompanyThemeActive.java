package com.xtremand.custom.css.bom;

import java.io.Serializable;

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

import com.xtremand.common.bom.CompanyProfile;
import com.xtremand.util.bom.XamplifyDefaultColumn;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode(callSuper=false)
@Entity
@Table(name="xt_company_theme_active")
public class CompanyThemeActive extends XamplifyDefaultColumn implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator="xt_company_theme_active_sequence")
	@SequenceGenerator(
			name="xt_company_theme_active_sequence",
			sequenceName="xt_company_theme_active_sequence",
			allocationSize=1
			)
	@Column(name = "id")
	private Integer id;
	
	@OneToOne
	@JoinColumn(name="company_id")
	private CompanyProfile companyProfile;
	
	@ManyToOne
	@JoinColumn(name="theme_id")
	private Theme theme;
	

}
