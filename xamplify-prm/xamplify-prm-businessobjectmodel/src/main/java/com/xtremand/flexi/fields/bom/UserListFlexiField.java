package com.xtremand.flexi.fields.bom;

import java.io.Serializable;
import java.util.Date;

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
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.xtremand.user.bom.UserUserList;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "xt_user_list_flexi_field_entries")
@Getter
@Setter
public class UserListFlexiField implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7222255610691767638L;

	private static final String SEQUENCE = "xt_user_list_flexi_field_entries_sequence";

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = SEQUENCE)
	@SequenceGenerator(name = SEQUENCE, sequenceName = SEQUENCE, allocationSize = 1)
	private Integer id;

	@Column(name = "flexi_field_value", nullable = false)
	private String flexiFieldValue;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_user_list_id", nullable = false)
	private UserUserList userUserList;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "flexi_field_id", nullable = false)
	private FlexiField flexiField;

	@Column(name = "created_time", columnDefinition = "DATETIME", nullable = false)
	@Temporal(TemporalType.TIMESTAMP)
	private Date createdTime;

}
