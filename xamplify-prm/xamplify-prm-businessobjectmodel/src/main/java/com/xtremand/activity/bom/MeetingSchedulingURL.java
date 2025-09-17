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

import com.xtremand.integration.bom.CalendarIntegration;
import com.xtremand.user.bom.User;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "xt_meeting_scheduling_url")
@Getter
@Setter
public class MeetingSchedulingURL {

	private static final String SEQUENCE = "xt_meeting_scheduling_url_sequence";
	
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = SEQUENCE)
	@SequenceGenerator(name = SEQUENCE, sequenceName = SEQUENCE, allocationSize = 1)
	private Integer id;
	
	@Column(name = "scheduling_url")
	private String schedulingUrl;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id")
	private User user;
	
	@Column(name = "created_time")
	private Date createdTime;
	
	@Column(name = "updated_time")
	private Date updatedTime;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "calendar_integration_id")
	private CalendarIntegration calendarIntegration;
	
}
