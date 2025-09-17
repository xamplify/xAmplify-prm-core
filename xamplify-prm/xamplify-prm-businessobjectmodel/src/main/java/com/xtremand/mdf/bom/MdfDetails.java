package com.xtremand.mdf.bom;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Table(name="xt_mdf_details")
@Data
@EqualsAndHashCode(callSuper=false)
public class MdfDetails extends MdfDetailsMappedSuperClass implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 244702804898217478L;
	
	
	private static final String SEQUENCE = "xt_mdf_details_id_seq";

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = SEQUENCE)
	@SequenceGenerator(name = SEQUENCE, sequenceName = SEQUENCE, allocationSize = 1)
	@Column(name = "id")
	private Integer id;
	



}
