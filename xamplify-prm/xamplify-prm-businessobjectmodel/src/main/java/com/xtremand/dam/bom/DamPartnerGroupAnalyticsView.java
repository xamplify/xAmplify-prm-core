package com.xtremand.dam.bom;

import javax.persistence.Entity;
import javax.persistence.Table;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@Entity
@EqualsAndHashCode(callSuper=false)
@Table(name="v_dam_partner_group_analytics")
public class DamPartnerGroupAnalyticsView extends DamAnalyticsMappedSuperClass  {/**
	 * 
	 */
	private static final long serialVersionUID = -2171507402990175434L;

}
