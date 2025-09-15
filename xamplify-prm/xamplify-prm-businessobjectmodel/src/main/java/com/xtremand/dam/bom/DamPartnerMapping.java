package com.xtremand.dam.bom;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import lombok.Data;

@Entity
@Table(name = "xt_dam_partner_mapping")
@Data
public class DamPartnerMapping implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 5152855896345085061L;

	private static final String DAM_PARTNER_MAPPING_SEQUENCE = "xt_dam_partner_mapping_sequence";
	
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = DAM_PARTNER_MAPPING_SEQUENCE)
	@SequenceGenerator(name = DAM_PARTNER_MAPPING_SEQUENCE, sequenceName = DAM_PARTNER_MAPPING_SEQUENCE, allocationSize = 1)
	@Column(name = "id")
	private Integer id;
	
	@ManyToOne
	@JoinColumn(name = "dam_partner_id")
	private DamPartner damPartner;
	
	
	@Column(name = "partner_id")
	private Integer partnerId;
	
	@Column(name = "created_time", columnDefinition = "DATETIME")
	@Temporal(TemporalType.TIMESTAMP)
	private Date createdTime;

}
