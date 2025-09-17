package com.xtremand.dam.bom;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

import lombok.Data;

@MappedSuperclass
@Data
public class DamMappedSuperClass implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7734979632625849911L;



	@Column(name = "alias")
	private String alias;

	@Column(name = "json_body")
	private String jsonBody;

	@Column(name = "html_body")
	private String htmlBody;

	

}
