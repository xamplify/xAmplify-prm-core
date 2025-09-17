package com.xtremand.integration.bom;


import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name="xt_crm_custom_region")
public class CustomRegion {
	
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "xt_crm_custom_region_sequence")
	@SequenceGenerator(name = "xt_crm_custom_region_sequence", sequenceName = "xt_crm_custom_region_sequence", allocationSize = 1)
	private Integer id;
	
	
	@Column(name = "choice_name")
	private String choiceName;

	@Column(name = "choice_id")
	private String choiceId;
	
	
}
