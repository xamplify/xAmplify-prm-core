package com.xtremand.deal.bom;

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
import com.xtremand.lead.bom.PipelineStage;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name="xt_deal_pipeline_stage")
@Getter @Setter
public class DealStage extends XamplifyTimeStamp{
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "deal_pipeline_stage_id_seq")
	@SequenceGenerator(name = "deal_pipeline_stage_id_seq", sequenceName = "deal_pipeline_stage_id_seq", allocationSize = 1)
	private Integer id;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name = "deal_id")
	private Deal deal;

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name = "pipeline_stage_id")
	private PipelineStage pipelineStage;
	
	@Column(name="created_by")
	Integer createdBy;

}
