package com.xtremand.dashboard.buttons.bom;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "xt_dashboard_reference_url")
public class DashboardReferenceUrl implements Serializable{

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "dashboard_reference_url_id_seq")
	@SequenceGenerator(name = "dashboard_reference_url_id_seq", sequenceName = "dashboard_reference_url_id_seq", allocationSize = 1)
	@Column(name = "id")
	private Integer id;
	
	@JoinColumn(name = "url")
	private String url;
	
	@Fetch(FetchMode.SUBSELECT)
	@OneToMany(mappedBy = "dashboardReferenceUrl", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private List<DashboardAlternateUrl> dashboardAlternateUrls= new ArrayList<>();
	

	
}
