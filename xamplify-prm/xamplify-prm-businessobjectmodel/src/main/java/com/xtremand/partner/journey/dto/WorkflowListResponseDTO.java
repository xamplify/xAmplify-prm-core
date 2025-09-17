package com.xtremand.partner.journey.dto;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.xtremand.util.dto.CreatedTimeConverter;

import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Data
@EqualsAndHashCode(callSuper = false)
public class WorkflowListResponseDTO extends CreatedTimeConverter implements Serializable {
	/**
	* 
	*/
	private static final long serialVersionUID = 6211231784624701000L;

	private Integer id;

	private String title;

	@Getter(value = AccessLevel.NONE)
	private String triggerDescription;

	private String status;

	private String createdBy;

	@JsonIgnore
	private String subjectValue;

	@JsonIgnore
	private String actionValue;

	@JsonIgnore
	private String timePhraseKey;

	@JsonIgnore
	private String timePhraseValue;

	private Integer customTriggerDays;

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public String getTriggerDescription() {
		if ("custom".equals(timePhraseKey)) {
			triggerDescription = subjectValue + " " + actionValue + " In the past " + customTriggerDays + " days";
		} else {
			triggerDescription = subjectValue + " " + actionValue + " " + timePhraseValue;
		}
		return triggerDescription;
	}

}
