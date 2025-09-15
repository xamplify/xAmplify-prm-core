package com.xtremand.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

@Component
public class CustomEmailValidator {
        private static final String EMAIL_REGEX =
            "^(([a-zA-Z0-9.!#$&'*+/=?_`{|}~-]+(\\.[a-zA-Z0-9.!#$&'*+/=?_`{|}~-]+)*)|(\".+\"))"
          + "@((\\[[0-9]{1,3}(\\.[0-9]{1,3}){3}\\])"
          + "|(([a-zA-Z\\-0-9]+\\.)+[a-zA-Z]{2,}))$";

        private static final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX);

	public boolean validate(String emailAddress) {
		if (emailAddress == null || emailAddress.trim().isEmpty()) {
			return false;
		}
		Matcher matcher = EMAIL_PATTERN.matcher(emailAddress);
		if (!matcher.matches()) {
			return false;
		}
		return true;
	}
}
