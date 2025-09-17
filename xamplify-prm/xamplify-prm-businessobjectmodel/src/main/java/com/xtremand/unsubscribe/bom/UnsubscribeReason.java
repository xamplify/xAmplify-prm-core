package com.xtremand.unsubscribe.bom;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import com.xtremand.category.bom.XamplifyDefaultColumn;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "xt_unsubscribe_reasons")
@Data
@EqualsAndHashCode(callSuper = false)
public class UnsubscribeReason extends XamplifyDefaultColumn implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1181671069259706195L;

	private static final String UNSUBSCRIBE_REASON_SEQUENCE = "xt_unsubscribe_reasons_sequence";

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = UNSUBSCRIBE_REASON_SEQUENCE)
	@SequenceGenerator(name = UNSUBSCRIBE_REASON_SEQUENCE, sequenceName = UNSUBSCRIBE_REASON_SEQUENCE, allocationSize = 1)
	@Column(name = "id")
	private Integer id;

	@Column(name = "reason")
	private String reason;

	@Column(name = "is_custom_reason")
	private boolean customReason;

}
