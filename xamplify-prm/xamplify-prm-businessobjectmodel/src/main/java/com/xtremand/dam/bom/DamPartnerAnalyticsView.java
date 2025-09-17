package com.xtremand.dam.bom;

import javax.persistence.Entity;
import javax.persistence.Table;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@Entity
@EqualsAndHashCode(callSuper=false)
@Table(name="v_dam_partner_analytics")
public class DamPartnerAnalyticsView extends DamAnalyticsMappedSuperClass {/**
	 * 
	 */
	private static final long serialVersionUID = 7655953210400592527L;

	


}
