package com.xtremand.formbeans;

import java.util.List;

import lombok.Data;

@Data
public class EmailDTO {
	public List<String> emailIds;
	public String subject;
	public String message;
	public String alias;
	public boolean enableVanityURL;
	public String vanityURL;
}
