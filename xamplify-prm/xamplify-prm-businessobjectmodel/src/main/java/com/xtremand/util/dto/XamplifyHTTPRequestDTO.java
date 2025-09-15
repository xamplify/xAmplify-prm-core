package com.xtremand.util.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class XamplifyHTTPRequestDTO implements Serializable {
	private static final long serialVersionUID = -3153322741241544687L;

	private Integer id;

	private List<Integer> ids = new ArrayList<>();

}
