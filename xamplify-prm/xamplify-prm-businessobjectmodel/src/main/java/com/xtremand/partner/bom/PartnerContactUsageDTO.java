package com.xtremand.partner.bom;

import java.io.Serializable;

import lombok.Data;

@Data
public class PartnerContactUsageDTO  implements Serializable{
	
	private static final long serialVersionUID = 272901525159373482L;
	
	private Integer companyId;
    private String companyName;
    private String emailId;
    private String firstName;
    private String lastName;
    private Integer contactUploadLimit;
    private Integer contactsUploadedCount;
    private Integer exceededContactUploadLimit;
    private Integer unusedContactUploadLimit;
    
}
