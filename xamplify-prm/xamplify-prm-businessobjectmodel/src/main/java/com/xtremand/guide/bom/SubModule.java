package com.xtremand.guide.bom;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import com.xtremand.common.bom.Module;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "xt_sub_module")
public class SubModule {
	/**
	 * 
	 */
	@SuppressWarnings("unused")
	private static final long serialVersionUID = 2027693398302311817L;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "xt_sub_module_id_sequence")
	@SequenceGenerator(name = "xt_sub_module_id_sequence", sequenceName = "xt_sub_module_id_sequence", allocationSize = 1)
	@Column(name = "id")
	private Integer id;
	
	@Column(name = "name")
	private String name;
	
	@ManyToOne
	@JoinColumn(name = "module_id")
	private Module moduleId;
}

