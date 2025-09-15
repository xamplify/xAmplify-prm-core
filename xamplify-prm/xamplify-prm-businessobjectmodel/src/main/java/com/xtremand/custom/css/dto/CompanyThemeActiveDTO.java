package com.xtremand.custom.css.dto;





import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class CompanyThemeActiveDTO {
	

private Integer id;

private Integer companyId;

private Integer themeId;

private Integer createdBy;

private Date createdDate;

private Date updatedDate;

}
