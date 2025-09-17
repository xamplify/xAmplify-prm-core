package com.xtremand.dam.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.util.StringUtils;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;

@Data
public class PublishedContentIdAndUserListIdDetailsDTO implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6793797630202220430L;

	private Integer id;

	private String userIdsAsString;


	@Getter(value = AccessLevel.NONE)
	private List<Integer> userIds;

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public List<Integer> getUserIds() {
		if (StringUtils.hasText(userIdsAsString)) {
			List<String> userIdsStringArray = Arrays.asList(userIdsAsString.split(","));
			List<Integer> updatedUserIds = new ArrayList<>(userIdsStringArray.size());
			for (String role : userIdsStringArray) {
				updatedUserIds.add(Integer.valueOf(role));
			}
			return updatedUserIds;
		} else {
			return new ArrayList<>();
		}

	}

}
