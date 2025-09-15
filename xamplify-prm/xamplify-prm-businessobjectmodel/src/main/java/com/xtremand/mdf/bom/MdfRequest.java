package com.xtremand.mdf.bom;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name="xt_mdf_request")
@Getter
@Setter
public class MdfRequest extends MdfRequestMappedSuperClass implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "xt_mdf_request_id_seq")
	@SequenceGenerator(name = "xt_mdf_request_id_seq", sequenceName = "xt_mdf_request_id_seq", allocationSize = 1)
	@Column(name = "id")
	private Integer id;
	
	@Transient
	private Integer vendorCompanyId;
	
	@Transient
	private Integer partnerCompanyId;
	
	
	
	
	
	
	

}

