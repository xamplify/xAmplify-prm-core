package com.xtremand.user.bom;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import lombok.Data;

@Entity
@Table(name = "xt_user_list_upload_history")
@Data
public class UserListUploadHistory {

	private static final String SEQUENCE = "xt_user_list_upload_history_sequence";

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = SEQUENCE)
	@SequenceGenerator(name = SEQUENCE, sequenceName = SEQUENCE, allocationSize = 1)
	private Integer id;

	@Column(name = "user_list_id")
	private Integer userListId;

	@Column(name = "csv_rows_count")
	private Integer csvRowsCount;

	@Column(name = "created_time", columnDefinition = "DATETIME")
	@Temporal(TemporalType.TIMESTAMP)
	private Date createdTime;

	@Column(name = "csv_path")
	private String csvPath;

	@Column(name = "inserted_count")
	private Integer insertedCount;

}
