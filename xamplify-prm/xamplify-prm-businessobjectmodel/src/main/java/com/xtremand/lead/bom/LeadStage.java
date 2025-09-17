package com.xtremand.lead.bom;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import com.xtremand.common.bom.XamplifyTimeStamp;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "xt_lead_pipeline_stage")
@Getter
@Setter
public class LeadStage extends XamplifyTimeStamp {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "lead_pipeline_stage_id_seq")
	@SequenceGenerator(name = "lead_pipeline_stage_id_seq", sequenceName = "lead_pipeline_stage_id_seq", allocationSize = 1)
	private Integer id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "lead_id")
	private Lead lead;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "pipeline_stage_id")
	private PipelineStage pipelineStage;

	@Column(name = "created_by")
	Integer createdBy;
}
