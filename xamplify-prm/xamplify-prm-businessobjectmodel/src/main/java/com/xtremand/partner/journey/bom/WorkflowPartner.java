package com.xtremand.partner.journey.bom;

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

import com.xtremand.campaign.bom.WorkflowsStatusEnum;
import com.xtremand.common.bom.XamplifyTimeStamp;
import com.xtremand.user.bom.User;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "xt_workflow_partner")
@Getter
@Setter
public class WorkflowPartner extends XamplifyTimeStamp implements Serializable{

	private static final long serialVersionUID = -4793435140561980237L;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "workflow_partner_id_seq")
	@SequenceGenerator(name = "workflow_partner_id_seq", sequenceName = "workflow_partner_id_seq", allocationSize = 1)
	private Integer id;

	@ManyToOne
	@JoinColumn(name = "workflow_id")
	private Workflow workflow;

	@ManyToOne
	@JoinColumn(name = "partner_id")
	private User partner;

	@Column(name = "created_by")
	private Integer createdBy;
	
	@Column(name = "status")
	@org.hibernate.annotations.Type(type = "com.xtremand.campaign.bom.WorkflowsEnumType")
	private WorkflowsStatusEnum status;

}
