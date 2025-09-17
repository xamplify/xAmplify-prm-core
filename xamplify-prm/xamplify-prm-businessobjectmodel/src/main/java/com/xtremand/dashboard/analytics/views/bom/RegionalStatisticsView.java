package com.xtremand.dashboard.analytics.views.bom;

import java.io.Serializable;

import javax.persistence.Entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity(name="regional_statistics_view")
@Data
@EqualsAndHashCode(callSuper=true)
public class RegionalStatisticsView extends RegionalStatisticsMappedSuperClassView implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1887704907745135327L;

	

}
