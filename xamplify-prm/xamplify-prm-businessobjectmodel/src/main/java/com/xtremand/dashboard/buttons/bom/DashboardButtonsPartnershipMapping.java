package com.xtremand.dashboard.buttons.bom;

import java.io.Serializable;

import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;

import com.xtremand.partnership.bom.Partnership;

import lombok.Getter;
import lombok.Setter;

@MappedSuperclass
@Getter
@Setter
public class DashboardButtonsPartnershipMapping implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 968456800736483649L;

	@ManyToOne
	@JoinColumn(name = "dashboard_button_id")
	private DashboardButton dashboardButton;

	@ManyToOne
	@JoinColumn(name = "partnership_id")
	private Partnership partnership;

}
