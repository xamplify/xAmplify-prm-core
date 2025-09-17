package com.xtremand.dashboard.layout.bom;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import com.xtremand.category.bom.XamplifyDefaultColumn;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "xt_dashboard_custom_layout")
@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
public class DashboardCustomLayout extends XamplifyDefaultColumn implements Serializable {

	private static final long serialVersionUID = 5518405906804305875L;

	private static final String SEQUENCE = "xt_dashboard_custom_layout_sequence";

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = SEQUENCE)
	@SequenceGenerator(name = SEQUENCE, sequenceName = SEQUENCE, allocationSize = 1)
	private Integer id;

	@JoinColumn(name = "dashboard_layout_id")
	@ManyToOne
	private DashboardLayout dashboardLayout;

	@Column(name = "display_index")
	private Integer displayIndex;

}
