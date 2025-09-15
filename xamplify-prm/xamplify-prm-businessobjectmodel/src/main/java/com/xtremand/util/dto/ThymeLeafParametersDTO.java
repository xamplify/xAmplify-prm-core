package com.xtremand.util.dto;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import lombok.Data;

@Data
public class ThymeLeafParametersDTO implements Serializable {
	/**
	* 
	*/
	private static final long serialVersionUID = -2358968517650635935L;

	private Map<String, Object> map = new HashMap<String, Object>();

	private Set<String> receiverEmailIds = new HashSet<String>();

	private String htmlName;

	private String subject;

}
