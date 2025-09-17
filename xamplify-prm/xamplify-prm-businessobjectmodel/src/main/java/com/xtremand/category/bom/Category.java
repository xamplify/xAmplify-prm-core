package com.xtremand.category.bom;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Table(name="xt_category")
@Data
@EqualsAndHashCode(callSuper=true)
public class Category extends XamplifyDefaultColumn implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5173377588646924936L;
	
	
	private static final String SEQUENCE = "xt_category_sequence";

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = SEQUENCE)
	@SequenceGenerator(name = SEQUENCE, sequenceName = SEQUENCE, allocationSize = 1)
	@Column(name = "id")
	private Integer id;
	
	@Column(name = "name")
	private String name;
	
	@Column(name = "description")
	private String description;
	
	@Column(name = "icon")
	private String icon;
	
	@Column(name = "is_default")
	private boolean defaultCategory;
	
}
