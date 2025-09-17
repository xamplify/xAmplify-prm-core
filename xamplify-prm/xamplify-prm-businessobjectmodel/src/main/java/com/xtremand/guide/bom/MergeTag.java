package com.xtremand.guide.bom;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import com.xtremand.util.bom.XamplifyDefaultColumn;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "xt_merge_tag")
public class MergeTag extends XamplifyDefaultColumn implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2027693398302311817L;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "xt_merge_tag_id_sequence")
	@SequenceGenerator(name = "xt_merge_tag_id_sequence", sequenceName = "xt_merge_tag_id_sequence", allocationSize = 1)
	@Column(name = "id")
	private Integer id;
	
	@Column(name = "name")
	private String name;
}

