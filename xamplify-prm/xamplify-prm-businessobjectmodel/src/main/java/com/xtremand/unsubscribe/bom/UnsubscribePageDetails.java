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
@Table(name = "xt_unsubscribe_page_details")
@Data
@EqualsAndHashCode(callSuper = false)
public class UnsubscribePageDetails  extends XamplifyDefaultColumn implements Serializable {
	
	private static final long serialVersionUID = 1181671069259706195L;

	private static final String UNSUBSCRIBE_PAGE_DETAILS_SEQUENCE = "xt_unsubscribe_page_details_sequence";

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = UNSUBSCRIBE_PAGE_DETAILS_SEQUENCE)
	@SequenceGenerator(name = UNSUBSCRIBE_PAGE_DETAILS_SEQUENCE, sequenceName = UNSUBSCRIBE_PAGE_DETAILS_SEQUENCE, allocationSize = 1)
	@Column(name = "id")
	private Integer id;
	
	@Column(name="header_text")
	private String headerText;
	
	@Column(name="footer_text")
	private String footerText;
	
	@Column(name="is_hide_header_text")
	private boolean hideHeaderText;
	
	@Column(name="is_hide_footer_text")
	private boolean hideFooterText;

}
