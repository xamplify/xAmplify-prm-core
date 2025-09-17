package com.xtremand.activity.bom;

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

import org.hibernate.annotations.Type;

import com.xtremand.user.bom.User;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "xt_note")
@Getter
@Setter
public class Note {
	
	private static final String SEQUENCE = "xt_note_sequence";
	
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = SEQUENCE)
	@SequenceGenerator(name = SEQUENCE, sequenceName = SEQUENCE, allocationSize = 1)
	private Integer id;

	@Column(name = "title")
	private String title;

	@Column(name = "content")
	private String content;

	@Column(name = "visibility")
	@Type(type = "com.xtremand.activity.bom.NoteVisibilityTypeType")
	private NoteVisibilityType visibility;
	
	@Column(name = "association_type")
	@Type(type = "com.xtremand.activity.bom.NoteAssociationTypeType")
	private NoteAssociationType associationType;	
	
	@Column(name = "company_id")
	private Integer companyId;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "contact_id", nullable = false)
	private User contact;
	
	@Column(name = "is_pinned")
	private boolean pinned;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "created_by", nullable = false)
	private User createdBy;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "updated_by", nullable = false)
	private User updatedBy;

	@Column(name = "created_time")
	private Date createdTime;

	@Column(name = "updated_time")
	private Date updatedTime;

}
