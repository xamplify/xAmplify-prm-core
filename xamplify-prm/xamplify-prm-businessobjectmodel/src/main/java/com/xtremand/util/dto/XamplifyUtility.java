package com.xtremand.util.dto;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;

public class XamplifyUtility {

	private static final int MAX_STRING_LENGTH = 255;

	private static final int MAX_DESCRIPTION_LENGTH = 1000;

	public static String getTrimmedString(String message) {
		if (message != null && StringUtils.hasText(message) && message.trim().length() > MAX_STRING_LENGTH) {
			return message.substring(0, MAX_STRING_LENGTH - 1).trim();
		} else {
			return message != null ? message.trim() : message;
		}
	}

	public static String getTrimmedDescription(String description) {
		if (description != null && StringUtils.hasText(description)
				&& description.trim().length() > MAX_DESCRIPTION_LENGTH) {
			return description.substring(0, MAX_DESCRIPTION_LENGTH - 1).trim();
		} else {
			return description != null ? description.trim() : description;
		}

	}

	public static boolean isValidPattern(String regExpression, String input) {
		if (StringUtils.hasText(input)) {
			Pattern pattern = Pattern.compile(regExpression);
			Matcher matcher = pattern.matcher(input);
			return matcher.matches();
		} else {
			return false;

		}

	}

	public static Integer convertStringToInteger(String inputString) {
		if (inputString != null && StringUtils.hasText(inputString)) {
			Integer page;
			try {
				page = Integer.parseInt(inputString);
			} catch (NumberFormatException e) {
				page = 0;
			}
			return page;
		} else {
			return 0;
		}

	}

	public static void setRejectedValue(Errors errors, String parameter, String defaultErrorMessage) {
		errors.rejectValue(parameter, "", defaultErrorMessage);
	}

	public static String getEnvironment() {
		return System.getProperty("spring.profiles.active");
	}

	public static String replaceNullWithHyphen(String string) {
		if (string != null) {
			return string;
		} else {
			return "-";
		}
	}
	
	public static String getFullName(String firstName, String lastName) {
		String name = "";
		if (firstName != null) {
			name = firstName + " ";
		}
		if (lastName != null) {
			name = name + lastName;
		}
		return name;
	}

}
