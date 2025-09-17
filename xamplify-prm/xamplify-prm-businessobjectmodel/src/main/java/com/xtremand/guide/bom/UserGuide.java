package com.xtremand.guide.bom;

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

import com.xtremand.common.bom.Module;
import com.xtremand.util.bom.XamplifyDefaultColumn;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "xt_user_guide")
public class UserGuide extends XamplifyDefaultColumn implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2027693398302311817L;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "xt_user_guide_id_sequence")
	@SequenceGenerator(name = "xt_user_guide_id_sequence", sequenceName = "xt_user_guide_id_sequence", allocationSize = 1)
	@Column(name = "id")
	private Integer id;

	@Column(name = "title")
	private String title;

	@Column(name = "description")
	private String description;

	@Column(name = "link")
	private String link;

	@Column(name = "slug")
	private String slug;
	
	@ManyToOne
	@JoinColumn(name = "module_id")
	private Module module;
    
	@ManyToOne
	@JoinColumn(name = "sub_module_id")
	private SubModule subModule;
	
	@OneToOne
	@JoinColumn(name = "merge_tag_id")
	private MergeTag mergeTag;
}

