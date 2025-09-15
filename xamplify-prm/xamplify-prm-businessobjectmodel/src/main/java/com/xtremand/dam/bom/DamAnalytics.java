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

import org.hibernate.annotations.Type;

import lombok.Data;

@Entity
@Table(name = "xt_dam_analytics")
@Data
public class DamAnalytics implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3698562427642328807L;

	private static final String DAM_ANALYTICS_SEQUENCE = "xt_dam_analytics_sequence";

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = DAM_ANALYTICS_SEQUENCE)
	@SequenceGenerator(name = DAM_ANALYTICS_SEQUENCE, sequenceName = DAM_ANALYTICS_SEQUENCE, allocationSize = 1)
	@Column(name = "id")
	private Integer id;

	@ManyToOne
	@JoinColumn(name = "dam_partner_mapping_fk_id")
	private DamPartnerMapping damPartnerMapping;

	@Column(name = "action_type")
	@Type(type = "com.xtremand.dam.bom.DamAnalyticsActionEnumerator")
	private DamAnalyticsActionEnum damAnalyticsActionEnum;

	@Column(name = "action_time", columnDefinition = "DATETIME")
	@Temporal(TemporalType.TIMESTAMP)
	private Date actionTime;

	@Column(name = "action_performed_by")
	private Integer actionPerformedBy;

	@ManyToOne
	@JoinColumn(name = "dam_partner_fk_id")
	private DamPartner damPartner;

}
