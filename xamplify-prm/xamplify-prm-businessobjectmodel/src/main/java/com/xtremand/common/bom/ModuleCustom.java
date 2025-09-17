package com.xtremand.common.bom;

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
import javax.persistence.Transient;

import com.xtremand.category.bom.XamplifyDefaultColumn;
import com.xtremand.partnership.bom.Partnership;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "xt_module_custom")
@EqualsAndHashCode(callSuper = false)
@Getter
@Setter
public class ModuleCustom extends XamplifyDefaultColumn implements Serializable {
	/**
	* 
	*/
	private static final long serialVersionUID = 354775800857035356L;

	private static final String MODULE_CUSTOM_SEQUENCE = "xt_module_custom_sequence";

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = MODULE_CUSTOM_SEQUENCE)
	@SequenceGenerator(name = MODULE_CUSTOM_SEQUENCE, sequenceName = MODULE_CUSTOM_SEQUENCE, allocationSize = 1)
	@Column(name = "id")
	private Integer id;

	@JoinColumn(name = "module_id")
	@ManyToOne
	private Module module;
	
	@JoinColumn(name = "partnership_id")
	@ManyToOne
	private Partnership partnership;

	@Column(name = "custom_name")
	private String customName;

	@Column(name = "display_index")
	private Integer displayIndex;
	
	@Column(name = "can_partner_access_module")
	private Boolean canPartnerAccessModule;

	@Column(name = "is_marketing_module")
	private Boolean marketingModule;

	@Transient
	private boolean hideMenu = false;

}
