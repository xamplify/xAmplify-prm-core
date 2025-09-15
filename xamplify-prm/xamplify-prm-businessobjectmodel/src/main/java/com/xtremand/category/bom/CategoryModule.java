package com.xtremand.category.bom;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import com.xtremand.lms.bom.LearningTrack;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "xt_category_module")
@Getter
@Setter
public class CategoryModule extends XamplifyDefaultColumn implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6020875437796430226L;

	private static final String SEQUENCE = "xt_category_module_sequence";
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = SEQUENCE)
	@SequenceGenerator(name = SEQUENCE, sequenceName = SEQUENCE, allocationSize = 1)
	@Column(name = "id")
	private Integer id;

	@Column(name = "category_id")
	private Integer categoryId;

	@Column(name = "email_template_id")
	private Integer emailTemplateId;

	@Column(name = "form_id")
	private Integer formId;

	@Column(name = "landing_page_id")
	private Integer landingPageId;

	@Column(name = "campaign_id")
	private Integer campaignId;

	@OneToOne
	@JoinColumn(name = "learning_track_id")
	private LearningTrack learningTrack;

	@Column(name = "dam_id")
	private Integer damId;

	@Column(name = "category_module_type")
	@org.hibernate.annotations.Type(type = "com.xtremand.category.bom.CategoryModuleEnumType")
	private CategoryModuleEnum categoryModuleEnum;

}
