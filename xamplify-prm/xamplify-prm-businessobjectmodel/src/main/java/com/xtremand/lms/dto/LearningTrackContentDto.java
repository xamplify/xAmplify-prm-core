package com.xtremand.lms.dto;

import com.xtremand.dam.dto.DamListDTO;
import com.xtremand.form.dto.FormDTO;

import lombok.Data;

@Data
public class LearningTrackContentDto {
	private Integer id;
	private Integer damId;
	private LearningTrackDto learningTrack;
	private DamListDTO dam;
	private boolean typeQuizId = false;
	private FormDTO quiz;
	private boolean finished;
	private boolean opened;
	private Integer quizId;
}
