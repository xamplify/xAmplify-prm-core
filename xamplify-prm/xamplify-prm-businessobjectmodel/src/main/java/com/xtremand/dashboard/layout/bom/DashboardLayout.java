package com.xtremand.dashboard.layout.bom;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "xt_dashboard_layout")
@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
public class DashboardLayout implements Serializable {

	public enum LayoutType {
		ACCOUNT_DASHBOARD("Account Dashboard");

		protected String status;

		private LayoutType(String status) {
			this.status = status;
		}

		public String getStatus() {
			return status;
		}
	}

	private static final long serialVersionUID = 5518405906804305875L;

	private static final String SEQUENCE = "xt_dashboard_layout_sequence";

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = SEQUENCE)
	@SequenceGenerator(name = SEQUENCE, sequenceName = SEQUENCE, allocationSize = 1)
	private Integer id;

	@Column(name = "div_id", nullable = false)
	private String divId;

	@Column(name = "div_name", nullable = false)
	private String divName;

	@Column(name = "layout_type", nullable = false)
	private LayoutType layoutType;

}
