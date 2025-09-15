package com.xtremand.email.thread.dto;

import java.io.Serializable;
import java.util.List;

import lombok.Data;

@Data
public class EmailRequestDTO implements Serializable {

	private static final long serialVersionUID = 1L;

	private String from;

	private List<String> toEmailIds;

	private String subject;

	private String bodyHtml;

	private String bodyText;

	private String threadId;

	private String messageId;

	private List<String> cc;

	private List<String> bcc;

	private String type;
	
	private String accessToken;
	
	private List<String> inReplyTo;

	
}
