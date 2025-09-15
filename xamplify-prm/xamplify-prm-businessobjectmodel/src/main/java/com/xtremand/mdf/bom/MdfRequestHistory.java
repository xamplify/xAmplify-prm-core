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

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name="xt_mdf_request_history")
@Getter
@Setter
public class MdfRequestHistory extends MdfRequestMappedSuperClass implements Serializable {
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -2016962116294288877L;
	
	
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "xt_mdf_request_history_id_seq")
	@SequenceGenerator(name = "xt_mdf_request_history_id_seq", sequenceName = "xt_mdf_request_history_id_seq", allocationSize = 1)
	@Column(name = "id")
	private Integer id;
	
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "request_id", referencedColumnName = "id")
	private MdfRequest mdfRequest;
	
	
}
