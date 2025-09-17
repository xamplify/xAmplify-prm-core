package com.xtremand.email.thread.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import lombok.Data;

@Data
public class EmailMessageDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String from;
    private String sender;
    private String subject;
    private String body;
    private Date receivedDate;
    private String timestamp;
    private String bodyContent;
    private List<String> labelIds;
    private boolean read;
    
    private List<EmailAttachmentDTO> attachments = new ArrayList<>();
    private String toEmailIds;
    private String ccEmailIds;
    private String bccEmailIds;
    private String threadId;
    private String messageId;
}
