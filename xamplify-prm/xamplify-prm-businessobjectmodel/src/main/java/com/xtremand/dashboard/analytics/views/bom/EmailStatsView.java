package com.xtremand.dashboard.analytics.views.bom;

import java.io.Serializable;

import javax.persistence.Entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity(name="email_stats_view")
@Data
@EqualsAndHashCode(callSuper = true)
public class EmailStatsView extends EmailStatsMappedSuperClassView implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -906291132576440750L;

	
}
