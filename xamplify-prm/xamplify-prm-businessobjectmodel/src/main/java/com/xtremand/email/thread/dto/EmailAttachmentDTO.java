package com.xtremand.email.thread.dto;

import java.io.Serializable;

import lombok.Data;

@Data
public class EmailAttachmentDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String name;
    private String contentType;
    private long size;
    private String contentBytes;
    private boolean inline;
    private String contentId;
}