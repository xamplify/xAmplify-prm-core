package com.xtremand.partner.journey.bom;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import com.xtremand.common.bom.XamplifyTimeStamp;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "xt_trigger_component")
@Getter
@Setter
public class TriggerComponent extends XamplifyTimeStamp implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -7393952537967417568L;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "trigger_component_id_seq")
	@SequenceGenerator(name = "trigger_component_id_seq", sequenceName = "trigger_component_id_seq", allocationSize = 1)
	private Integer id;

	private String key;
	private String value;

	@org.hibernate.annotations.Type(type = "com.xtremand.partner.journey.bom.TriggerComponentTypeType")
	private TriggerComponentType type;

	@Column(name = "created_by")
	private Integer createdBy;

}
