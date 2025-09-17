package com.xtremand.dashboard.analytics.views.bom;

import java.io.Serializable;

import javax.persistence.Entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity(name = "vendor_email_stats_view")
@Data
@EqualsAndHashCode(callSuper = true)
public class VendorEmailStatsView   extends EmailStatsMappedSuperClassView implements Serializable{/**
	 * 
	 */
	private static final long serialVersionUID = 1485743235327371665L;

}
