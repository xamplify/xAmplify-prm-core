package com.xtremand.mdf.bom;

import java.io.Serializable;

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

import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Table(name="xt_mdf_details_history")
@Data
@EqualsAndHashCode(callSuper=false)
public class MdfDetailsHistory extends MdfDetailsMappedSuperClass implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -5765666287388228425L;
	
	private static final String SEQUENCE = "xt_mdf_details_history_id_seq";

	
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = SEQUENCE)
	@SequenceGenerator(name = SEQUENCE, sequenceName = SEQUENCE, allocationSize = 1)
	@Column(name = "id")
	private Integer id;
	
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name="mdf_details_id",referencedColumnName = "id")
	private MdfDetails marketDevelopementFundsDetails;
	

	

}
