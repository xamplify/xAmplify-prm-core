package com.xtremand.activity.bom;

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
@Table(name = "xt_activity_attachment")
@Setter
@Getter
public class ActivityAttachment {
	
	private static final String SEQUENCE = "xt_email_recipient_sequence";

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = SEQUENCE)
	@SequenceGenerator(name = SEQUENCE, sequenceName = SEQUENCE, allocationSize = 1)
	private Integer id;
	
	@Column(name = "file_name")
	private String fileName;
	
	@Column(name = "file_path")
	private String filePath;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "email_activity_id")
	private EmailActivity emailActivity;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "task_activity_id")
	private TaskActivity taskActivity;
	
	@Column(name = "temporary_file_path")
	private String temporaryFilePath;
	
	@Column(name = "file_type")
	private String fileType;
	
	@Column(name = "size")
	private Long size;
	
}
