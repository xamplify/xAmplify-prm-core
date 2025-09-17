package com.xtremand.dashboard.buttons.bom;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "xt_dashboard_alternate_url")
public class DashboardAlternateUrl implements Serializable{
	
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "dashboard_alternate_url_id_seq")
	@SequenceGenerator(name = "dashboard_alternate_url_id_seq", sequenceName = "dashboard_alternate_url_id_seq", allocationSize = 1)
	@Column(name = "id")
	private Integer id;

	@JoinColumn(name = "alternate_url")
	private String alternateUrl;
	
	@JoinColumn(name = "title")
	private String title;
	
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "reference_url_id", referencedColumnName = "id")
	private DashboardReferenceUrl dashboardReferenceUrl;


}
