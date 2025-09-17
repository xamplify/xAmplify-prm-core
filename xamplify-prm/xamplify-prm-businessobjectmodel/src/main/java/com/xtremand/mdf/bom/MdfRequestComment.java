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

import org.springframework.util.StringUtils;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;

@Data
@Entity
@Table(name = "xt_mdf_request_comments")
public class MdfRequestComment implements Serializable {/**
	 * 
	 */
	private static final long serialVersionUID = 7361350702145547391L;
	
	private static final String SEQUENCE = "xt_mdf_request_comments_sequence";

	
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = SEQUENCE)
	@SequenceGenerator(name = SEQUENCE, sequenceName = SEQUENCE, allocationSize = 1)
	@Column(name = "id")
	private Integer id;
	
	@JoinColumn(name = "request_id")
	@ManyToOne
	private MdfRequest mdfRequest;
	
	@Column(name = "company_id")
	private Integer companyId;
	
	@Column(name="comment")
	@Setter(value = AccessLevel.NONE)
	private String comment;
	
	@Column(name = "commented_by")
	private Integer commentedBy;
	

	@Column(name = "created_time", columnDefinition = "DATETIME")
	@Temporal(TemporalType.TIMESTAMP)
	private Date createdTime;


	public void setComment(String comment) {
		if(StringUtils.hasText(comment)) {
			if(comment.length()>1000) {
				this.comment = comment.trim().substring(0,999);
			}else {
				this.comment = comment.trim();
			}
		}else {
			this.comment = comment;
		}
		
	}


}
