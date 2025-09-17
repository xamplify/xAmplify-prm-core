package com.xtremand.deal.bom;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "xt_deal_fields")
@Getter
@Setter
public class DealField {
	
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "xt_deal_fields_sequence")
	@SequenceGenerator(name = "xt_deal_fields_sequence", sequenceName = "xt_deal_fields_sequence", allocationSize = 1)
	private Integer id;
	
	@Column(name = "label_name")
	private String labelName;
	
	@Column(name = "label_id")
	private String labelId;
	
	@Column(name = "label_type")
	private String labelType;
	
}
