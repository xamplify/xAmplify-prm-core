package com.xtremand.util.dto;

import com.xtremand.vanity.url.dto.VanityUrlDetailsDTO;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class DownloadRequestPostDTO extends VanityUrlDetailsDTO {

	private boolean allowDuplicateRequest;

}
