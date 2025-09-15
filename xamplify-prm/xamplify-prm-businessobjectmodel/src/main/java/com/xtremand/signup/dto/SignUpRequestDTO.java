package com.xtremand.signup.dto;

import java.io.Serializable;

import org.springframework.util.StringUtils;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;

@Data
public class SignUpRequestDTO implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4742959935247025465L;

	@Getter(value = AccessLevel.NONE)
	private String firstName;

	@Getter(value = AccessLevel.NONE)
	private String lastName;

	@Getter(value = AccessLevel.NONE)
	private String emailId;

	private Integer companyId;

	@Getter(value = AccessLevel.NONE)
	private String companyName;

	@Getter(value = AccessLevel.NONE)
	private String companyProfileName;

        private String password;

        @Getter(value = AccessLevel.NONE)
        private String confirmPassword;

	private boolean skipPassword;

	private boolean accessedFromVanityDomain;

	private boolean passwordExists;

	private boolean passwordUpdated;

	private boolean accountAlreadyExists;

	private String groupAlias;

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public String getFirstName() {
		if (StringUtils.hasText(firstName) && firstName.trim().length() > 255) {
			return firstName.trim().substring(0, 254);
		} else if (StringUtils.hasText(firstName)) {
			return firstName.trim();
		} else {
			return firstName;
		}

	}

	public String getLastName() {
		if (StringUtils.hasText(lastName) && lastName.trim().length() > 255) {
			return lastName.trim().substring(0, 254);
		} else if (StringUtils.hasText(lastName)) {
			return lastName.trim();
		} else {
			return lastName;
		}
	}

	public String getEmailId() {
		if (StringUtils.hasText(emailId) && emailId.trim().length() > 500) {
			return emailId.trim().substring(0, 499).toLowerCase();
		} else if (StringUtils.hasText(emailId)) {
			return emailId.toLowerCase().trim();
		} else {
			return emailId;
		}
	}

	public String getCompanyName() {
		if (StringUtils.hasText(companyName) && companyName.trim().length() > 250) {
			return companyName.trim().substring(0, 249);
		} else if (StringUtils.hasText(companyName)) {
			return companyName.trim();
		} else {
			return companyName;
		}
	}

        public String getCompanyProfileName() {
                if (StringUtils.hasText(companyProfileName)) {
                        return companyProfileName.trim();
                } else {
                        return companyProfileName;
                }

        }

        public String getConfirmPassword() {
                if (StringUtils.hasText(confirmPassword)) {
                        return confirmPassword.trim();
                } else {
                        return confirmPassword;
                }

        }

}
