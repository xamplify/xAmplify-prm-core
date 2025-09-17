package com.xtremand.email.thread.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class EmailThreadDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String threadId;
    private String subject;
    private List<EmailMessageDTO> messages = new ArrayList<>();
    private int replyCount;
    private boolean read;
    private List<String> labels;
	private String lastReceivedDate;
}
