package com.xtremand.dam.bom;

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
import com.xtremand.tag.bom.Tag;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "xt_dam_tag")
@Getter
@Setter
public class DamTag extends XamplifyTimeStamp {
	private static final long serialVersionUID = 1L;

	private static final String DAM_TAG_SEQUENCE = "xt_dam_tag_sequence";

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = DAM_TAG_SEQUENCE)
	@SequenceGenerator(name = DAM_TAG_SEQUENCE, sequenceName = DAM_TAG_SEQUENCE, allocationSize = 1)
	private Integer id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "dam_id")
	private Dam dam;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "tag_id")
	private Tag tag;

	@Column(name = "created_by")
	private Integer createdBy;
}