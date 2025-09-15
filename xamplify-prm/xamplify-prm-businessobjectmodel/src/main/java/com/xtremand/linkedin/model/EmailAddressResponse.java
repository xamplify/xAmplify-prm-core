package com.xtremand.linkedin.model;

import java.util.List;

import lombok.Data;

@Data
public class EmailAddressResponse {
	private List<EmailAddressHandle> elements;
}