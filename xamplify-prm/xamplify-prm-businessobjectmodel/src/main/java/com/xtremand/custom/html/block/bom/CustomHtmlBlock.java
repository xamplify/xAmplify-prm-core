package com.xtremand.custom.html.block.bom;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import com.xtremand.category.bom.XamplifyDefaultColumn;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "xt_custom_html_block")
@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
public class CustomHtmlBlock extends XamplifyDefaultColumn implements Serializable {

	private static final long serialVersionUID = 5518405906804305875L;

	private static final String SEQUENCE = "xt_custom_html_block_sequence";

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = SEQUENCE)
	@SequenceGenerator(name = SEQUENCE, sequenceName = SEQUENCE, allocationSize = 1)
	private Integer id;

	@Column(name = "title")
	private String title;

	@Column(name = "html_body")
	private String htmlBody;

	@Column(name = "left_html_body")
	private String leftHtmlBody;

	@Column(name = "right_html_body")
	private String rightHtmlBody;

	@Column(name = "is_selected")
	private boolean selected;

	@Column(name = "display_index")
	private Integer displayIndex;

	@Column(name = "layout_size")
	private String layoutSize;

	@Column(name = "is_title_visible")
	private boolean titleVisible;

}
