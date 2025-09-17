package com.xtremand.form.submit.dto;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import lombok.Data;

@Data
public class FormSubmitAnswerDTO {

	private Integer labelId;

	private Object submittedAnswer;

	private Set<Integer> submittedAnswers = new HashSet<>();

	private Set<Integer> correctAnswers = new HashSet<>();

	private boolean skipped = false;

	private boolean submittedAnswerCorrect = false;

	private Integer score;

	private Integer maxScore;

	Map<Integer, FormSubmitAnswerDTO> submittedData = new HashMap<>();

	public void checkSubmittedAnswers() {
		if (this.submittedAnswers == null || this.submittedAnswers.isEmpty()) {
			this.skipped = true;
		} else {
			if (this.submittedAnswers.equals(this.correctAnswers)) {
				this.submittedAnswerCorrect = true;
				this.skipped = false;
			}
		}
		if (this.submittedAnswer == null) {
			this.submittedAnswer = this.submittedAnswers;
		}
	}
}
