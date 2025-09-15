package com.xtremand.superadmin.validator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import com.xtremand.superadmin.dto.ChangeEmailAddressRequestDTO;
import com.xtremand.user.dao.UserDAO;
import com.xtremand.util.XamplifyUtils;
import com.xtremand.util.dto.XamplifyConstants;
import com.xtremand.util.dto.XamplifyUtilValidator;

@Component
public class UpdateEmailAddressValidator implements Validator {

	private static final String EXISTING_EMAIL_ADDRESS_PARAMETER = "existingEmailAddress";

	private static final String UPDATED_EMAIL_ADDRESS_PARAMETER = "updatedEmailAddress";

	@Autowired
	private XamplifyUtilValidator xamplifyUtilValidator;

	@Autowired
	private UserDAO userDao;

	@Override
	public boolean supports(Class<?> clazz) {
		return ChangeEmailAddressRequestDTO.class.isAssignableFrom(clazz);
	}

	@Override
	public void validate(Object target, Errors errors) {
		ChangeEmailAddressRequestDTO changeEmailAddressRequestDTO = (ChangeEmailAddressRequestDTO) target;
		String existingEmailAddress = changeEmailAddressRequestDTO.getExistingEmailAddress();
		validateExistingEmailAddressParameter(errors, existingEmailAddress);
		String updatedEmailAddress = changeEmailAddressRequestDTO.getUpdatedEmailAddress();
		validateUpdateEmailAddressParameter(errors, updatedEmailAddress);
		validateExistingAndUpdateEmailAddressValues(existingEmailAddress, updatedEmailAddress, errors);

	}

	private void validateExistingAndUpdateEmailAddressValues(String existingEmailAddress, String updatedEmailAddress,
			Errors errors) {
		boolean isValidExistingEmailAddress = XamplifyUtils.isValidString(existingEmailAddress);
		boolean isValidUpdatedEmailAddress = XamplifyUtils.isValidString(updatedEmailAddress);
		boolean isValidEmailAddresses = isValidExistingEmailAddress && isValidUpdatedEmailAddress;
		if (isValidEmailAddresses) {
			boolean isBothEmailAddressMatched = existingEmailAddress.equalsIgnoreCase(updatedEmailAddress);
			if (isBothEmailAddressMatched) {
				xamplifyUtilValidator.addFieldError(errors, UPDATED_EMAIL_ADDRESS_PARAMETER,
						"Existing Email Address And Updated Email Address Cannot Be Same");
			}
		}
	}

	private void validateUpdateEmailAddressParameter(Errors errors, String updatedEmailAddress) {
		if (updatedEmailAddress == null) {
			xamplifyUtilValidator.addFieldError(errors, UPDATED_EMAIL_ADDRESS_PARAMETER,
					UPDATED_EMAIL_ADDRESS_PARAMETER + XamplifyConstants.PARAMETER_IS_MISSING);
		} else if (!XamplifyUtils.isValidString(updatedEmailAddress)) {
			xamplifyUtilValidator.addFieldError(errors, UPDATED_EMAIL_ADDRESS_PARAMETER,
					"Please enter Updated Email Address");
		} else if (XamplifyUtils.isValidString(updatedEmailAddress)) {
			boolean isEmailAddressExists = userDao.isEmailAddressExists(updatedEmailAddress);
			if (isEmailAddressExists) {
				xamplifyUtilValidator.addFieldError(errors, UPDATED_EMAIL_ADDRESS_PARAMETER,
						"Above Email Address Already Exists");
			}
		}
	}

	private void validateExistingEmailAddressParameter(Errors errors, String existingEmailAddress) {
		if (existingEmailAddress == null) {
			xamplifyUtilValidator.addFieldError(errors, EXISTING_EMAIL_ADDRESS_PARAMETER,
					EXISTING_EMAIL_ADDRESS_PARAMETER + XamplifyConstants.PARAMETER_IS_MISSING);
		} else if (!StringUtils.hasText(existingEmailAddress)) {
			xamplifyUtilValidator.addFieldError(errors, EXISTING_EMAIL_ADDRESS_PARAMETER,
					"Please enter Existing Email Address");
		} else if (StringUtils.hasText(existingEmailAddress)) {
			boolean isEmailAddressExists = userDao.isEmailAddressExists(existingEmailAddress);
			if (!isEmailAddressExists) {
				xamplifyUtilValidator.addFieldError(errors, EXISTING_EMAIL_ADDRESS_PARAMETER,
						"Entered Email Address does not exist");
			} else if ("test@test.com".equalsIgnoreCase(existingEmailAddress)) {
				xamplifyUtilValidator.addFieldError(errors, EXISTING_EMAIL_ADDRESS_PARAMETER, "Entered Email Address Cannot Be Changed");
			}
		}
	}

}
