package com.xtremand.mail.bom;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import com.xtremand.user.bom.User;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "xt_email_spam_score")
public class EmailSpamScore implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 4236113719734341155L;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "email_spam_score_id_seq")
	@SequenceGenerator(name = "email_spam_score_id_seq", sequenceName = "email_spam_score_id_seq", allocationSize = 1)
	Integer id;

	@Column(name = "score")
	private String score;

	@Column(name = "subject")
	private String subject;

	@OneToOne
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@Column(name = "to_email", unique = true)
	private String toEmail;

	@ManyToOne
	@JoinColumn(name = "email_template_id", nullable = false)
	private EmailTemplate emailTemplate;

}
