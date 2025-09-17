package com.xtremand.video.bom;

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

import com.xtremand.user.bom.User;

import lombok.Data;

@Entity
@Table(name = "xt_video_leads")
@Data
public class VideoLead implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6584097326854969819L;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "video_lead_id_seq")
	@SequenceGenerator(name = "video_lead_id_seq", sequenceName = "video_lead_id_seq", allocationSize = 1)
	private Integer id;

	@ManyToOne()
	@JoinColumn(name = "video_id", referencedColumnName = "id")
	private VideoFile videoFile;

	@ManyToOne()
	@JoinColumn(name = "user_id")
	private User user;

	@Column(name = "date", columnDefinition = "DATETIME")
	@Temporal(TemporalType.TIMESTAMP)
	private Date date;

	@Column(name = "firstname")
	private String firstName;

	@Column(name = "lastname")
	private String lastName;

	@Column(name = "session_id")
	private String sessionId;

}
