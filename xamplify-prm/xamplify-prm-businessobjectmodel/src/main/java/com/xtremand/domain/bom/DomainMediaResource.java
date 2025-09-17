package com.xtremand.domain.bom;

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

@Entity
@Table(name = "xt_domain_media_resource")
@Setter
@Getter
public class DomainMediaResource {

	private static final String SEQUENCE = "xt_domain_media_resource_sequence";
	
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = SEQUENCE)
	@SequenceGenerator(name = SEQUENCE, sequenceName = SEQUENCE, allocationSize = 1)
	private Integer id;
	
	@Column(name = "file_name")
	private String fileName;
	
	@Column(name = "file_path")
	private String filePath;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "domain_metadata_id")
	private  DomainMetadata domainMetadata;
	
	@Column(name = "temporary_file_path")
	private String temporaryFilePath;
	
	@Column(name = "image_type")
	private String imageType;
	
	@Column(name = "theme")
	private String theme;
	
	@Column(name = "file_type")
	private String fileType;
	
}
