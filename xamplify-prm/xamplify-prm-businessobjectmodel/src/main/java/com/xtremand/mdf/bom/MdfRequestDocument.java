package com.xtremand.mdf.bom;

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

import lombok.Data;

@Entity
@Table(name="xt_mdf_request_documents")
@Data
public class MdfRequestDocument implements Serializable {/**
	 * 
	 */
	private static final long serialVersionUID = -6658448918426852494L;
	
	
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "xt_mdf_request_documents_sequence")
	@SequenceGenerator(name = "xt_mdf_request_documents_sequence", sequenceName = "xt_mdf_request_documents_sequence", allocationSize = 1)
	@Column(name = "id")
	private Integer id;
	
	
	@ManyToOne
	@JoinColumn(name="request_id")
	private MdfRequest mdfRequest;
	
	
	@Column(name="file_name")
	private String fileName;
	
	@Column(name="file_path")
	private String filePath;
	
	@Column(name="file_path_alias")
	private String filePathAlias;
	
	@Column(name="description")
	private String description;
	
	@Column(name="uploaded_by")
	private Integer uploadedBy;
	
	
	@Column(name = "uploaded_time", columnDefinition = "DATETIME")
	@Temporal(TemporalType.TIMESTAMP)
	private Date uploadedTime;
	
	

}
