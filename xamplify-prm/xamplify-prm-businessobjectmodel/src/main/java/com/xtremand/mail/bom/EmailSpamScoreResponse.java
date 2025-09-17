package com.xtremand.mail.bom;

import lombok.Getter;

@Getter
public class EmailSpamScoreResponse {
	private String title;
	private Float mark;
	private String displayedMark;
	private String commentedMark;
	private String mailboxId;
	private int maxMark;
	private boolean status;
	private boolean redirect;
	private boolean access;
}
