package com.xtremand.highlevel.analytics.bom;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import com.xtremand.user.bom.User;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "xt_download_request")
@Data
@EqualsAndHashCode(callSuper = false)
public class DownloadRequest implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static final String DOWNLOAD_REQUEST_SEQUENCE = "xt_download_request_sequence";

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = DOWNLOAD_REQUEST_SEQUENCE)
	@SequenceGenerator(name = DOWNLOAD_REQUEST_SEQUENCE, sequenceName = DOWNLOAD_REQUEST_SEQUENCE, allocationSize = 1)
	@Column(name = "id", nullable = false)
	private Integer id;

	@JoinColumn(name = "requested_by", nullable = false)
	@ManyToOne
	private User requestedBy;

	@Column(name = "status", nullable = false)
	@org.hibernate.annotations.Type(type = "com.xtremand.highlevel.analytics.bom.DownloadStatusType")
	private DownloadStatus downloadStatus;

	@Column(name = "module_name", nullable = false)
	@org.hibernate.annotations.Type(type = "com.xtremand.highlevel.analytics.bom.DownloadModuleType")
	private DownloadModule downloadModule;

	@Column(name = "download_path")
	private String downloadPath;

	@Column(name = "requested_on", nullable = false, columnDefinition = "DATETIME")
	@Temporal(TemporalType.TIMESTAMP)
	private Date requestedOn;

	@Column(name = "updated_on", nullable = false, columnDefinition = "DATETIME")
	@Temporal(TemporalType.TIMESTAMP)
	private Date updatedOn;
	
	@Transient
	@Getter
	@Setter
	private String highLevelCdnPath;
}
