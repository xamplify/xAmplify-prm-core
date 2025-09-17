package com.xtremand.dashboard.analytics.views.bom;

import javax.persistence.Entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity(name="vendor_regional_statistics_view")
public class VendorRegionalStatisticsView extends RegionalStatisticsMappedSuperClassView {

}
