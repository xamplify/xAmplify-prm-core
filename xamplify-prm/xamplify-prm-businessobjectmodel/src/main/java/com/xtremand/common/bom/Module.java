package com.xtremand.common.bom;

import java.util.List;

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

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "xt_module")
@Getter
@Setter
public class Module {

	private static final String MODULE_SEQUENCE = "xt_module_sequence";

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = MODULE_SEQUENCE)
	@SequenceGenerator(name = MODULE_SEQUENCE, sequenceName = MODULE_SEQUENCE, allocationSize = 1)
	@Column(name = "id")
	private Integer id;

	@Column(name = "module_id")
	private Integer moduleId;

	@Column(name = "module_name")
	private String moduleName;
	
	@Column(name = "angular_path")
	private String angularPath;
	
	@Column(name = "angular_icon")
	private String angularIcon;
	
	@Column(name = "merge_tag")
	private String mergeTag;
	
	@JoinColumn(name = "parent_module_id")
	@ManyToOne
	private Module parentModule;
	
	@OneToMany(mappedBy = "parentModule", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private List<Module> subModules;

}
