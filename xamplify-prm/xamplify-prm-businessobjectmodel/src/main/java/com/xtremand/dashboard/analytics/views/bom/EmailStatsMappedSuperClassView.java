package com.xtremand.dashboard.analytics.views.bom;

import java.io.Serializable;
import java.math.BigInteger;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

import lombok.Data;

@MappedSuperclass
@Data
public class EmailStatsMappedSuperClassView implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -4182449931282918283L;

	@Id
	@Column(name = "company_id")
	private Integer companyId;
	
	private BigInteger opened;
	
	private BigInteger clicked;
	
	private BigInteger views;

}
