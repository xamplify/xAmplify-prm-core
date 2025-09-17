package com.xtremand.lead.bom;

import java.util.List;

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

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import com.xtremand.common.bom.XamplifyTimeStamp;
import com.xtremand.deal.bom.Deal;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "xt_pipeline_stage")
@Getter
@Setter
public class PipelineStage extends XamplifyTimeStamp {
	/**
	 * 
	 */
	private static final long serialVersionUID = 377385198830965747L;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "pipeline_stage_id_seq")
	@SequenceGenerator(name = "pipeline_stage_id_seq", sequenceName = "pipeline_stage_id_seq", allocationSize = 1)
	private Integer id;

	@ManyToOne
	@JoinColumn(name = "pipeline_id")
	private Pipeline pipeline;

	@Column(name = "stage_name")
	private String stageName;

	@Column(name = "display_index")
	private Integer displayIndex;

	@Column(name = "is_default")
	private boolean defaultStage;

	@Column(name = "is_won")
	private boolean won;

	@Column(name = "is_lost")
	private boolean lost;

	@Column(name = "created_by")
	Integer createdBy;

	@Fetch(FetchMode.SUBSELECT)
	@OneToMany(mappedBy = "currentStage", fetch = FetchType.LAZY)
	private List<Lead> leads;

	@Fetch(FetchMode.SUBSELECT)
	@OneToMany(mappedBy = "currentStage", fetch = FetchType.LAZY)
	private List<Deal> deals;

	@Column(name = "external_pipeline_stage_id")
	private String externalPipelineStageId;

	@Column(name = "is_non_interactive")
	private boolean nonInteractive;

	@Column(name = "is_private")
	private boolean isPrivate;

	@Column(name = "external_pipeline_step_id")
	private Long externalPipelineStepId;

}
