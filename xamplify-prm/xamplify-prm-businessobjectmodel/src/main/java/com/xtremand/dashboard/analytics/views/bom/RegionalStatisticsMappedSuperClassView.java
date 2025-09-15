package com.xtremand.dashboard.analytics.views.bom;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

import lombok.Data;

@MappedSuperclass
@Data
public class RegionalStatisticsMappedSuperClassView {
	@Id
	@Column(name="row_number")
	private Integer rowNumber;
	
	@Column(name="sum")
	private BigDecimal wathedCount;
	
	@Column(name="country_code")
	private String countryCode;
	
	@Column(name="company_id")
	private Integer companyId;
}
