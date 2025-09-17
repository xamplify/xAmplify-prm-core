package com.xtremand.category.dto;

import java.io.Serializable;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;

@Data
public class CategoryItemsCountDTO implements Serializable {
	/**
	* 
	*/
	private static final long serialVersionUID = -3557719725341507048L;

	private Integer emailTemplatesCount;

	private Integer formsCount;

	private Integer pagesCount;

	private Integer campaignsCount;

	private Integer damCount;

	private Integer lmsCount;

	private Integer playBooksCount;

	@Getter(value = AccessLevel.NONE)
	private Integer totalCount;

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public Integer getTotalCount() {
		return emailTemplatesCount + formsCount + pagesCount + campaignsCount + damCount + lmsCount + playBooksCount;
	}

}
