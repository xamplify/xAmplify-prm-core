package com.xtremand.user.validation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.xtremand.formbeans.UserDTO;
import com.xtremand.formbeans.XtremandResponse;
import com.xtremand.user.bom.User;
import com.xtremand.user.bom.UserList;
import com.xtremand.user.exception.UserDataAccessException;
import com.xtremand.userlist.exception.UserListException;
import com.xtremand.util.CustomValidatonException;
import com.xtremand.util.EmailValidatorUtil;

import au.com.bytecode.opencsv.CSV;
import au.com.bytecode.opencsv.CSVReader;

@Component
public class UserListValidator {

	@Autowired
	EmailValidatorUtil emailValidator;

	private static final CSV csv = CSV.separator(',').quote('\'').skipLines(1).charset("UTF-8").create();

	public boolean validate(MultipartFile file, String userListName, List<UserList> userLists)
			throws UserDataAccessException {
		try {
			List<String> invalidEmailIds = new ArrayList<>();
			CSVReader reader = csv.reader(file.getInputStream());
			List<String[]> data = reader.readAll();
			boolean status = true;
			int counter = 0;
			for (int i = 0; i < data.size(); i++) {
				if (!emailValidator.validate(data.get(i)[0].trim())) {
					invalidEmailIds.add(data.get(i)[0] + ":" + (i + 1));
					counter++;
					status = false;
				}
			}

			boolean isDuplicate = false;
			for (UserList list : userLists) {
				if (list.getName().equalsIgnoreCase(userListName)) {
					isDuplicate = true;
					status = false;
					break;
				}
			}
			if (counter == 0 && !isDuplicate) {
				return status;
			} else if (counter != 0 && isDuplicate) {
				throw new UserDataAccessException(
						"email addresses in your contact list that aren't formatted properly & User List name already exists");
			} else if (isDuplicate) {
				throw new UserDataAccessException("User List name already exists");
			} else {
				throw new UserDataAccessException(
						"email addresses in your contact list that aren't formatted properly");
			}
		} catch (IOException e) {
			throw new UserDataAccessException(e.getMessage());
		}
	}

	public boolean validate(Set<User> users, String userListName, List<UserList> userLists) throws UserListException {
		try {
			List<String> invalidEmailIds = new ArrayList<>();

			boolean status = true;
			int counter = 0;
			for (User user  : users) {
				if (!emailValidator.validate(user.getEmailId().trim())) {
					invalidEmailIds.add(user.getEmailId());
					counter++;
					status = false;
				}
			}

			boolean isDuplicate = false;
			for (UserList list : userLists) {
				if (list.getName().equalsIgnoreCase(userListName)) {
					isDuplicate = true;
					status = false;
					break;
				}
			}
			if (counter == 0 && !isDuplicate) {
				return status;
			} else if (counter != 0 && isDuplicate) {
				throw new CustomValidatonException(
						"email addresses in your contact list that aren't formatted properly & User List name already exists");
			} else if (isDuplicate) {
				throw new CustomValidatonException("User List name already exists");
			} else {
				throw new CustomValidatonException(
						"email addresses in your contact list that aren't formatted properly " + invalidEmailIds);
			}
		} catch (Exception e) {
			throw new UserListException(e.getMessage());
		}
	}

	public boolean validate(List<User> users) throws UserListException {
		try {
			List<String> invalidEmailIds = new ArrayList<>();

			boolean status = true;
			int counter = 0;
			for (int i = 0; i < users.size(); i++) {
				User user = users.get(i);
				if (!emailValidator.validate(user.getEmailId().trim())) {
					invalidEmailIds.add(user.getEmailId());
					counter++;
					status = false;
				}
			}

			if (counter == 0) {
				return status;
			} else {
				throw new CustomValidatonException(
						"email addresses in your contact list that aren't formatted properly " + invalidEmailIds);
			}
		} catch (Exception e) {
			throw new UserListException(e.getMessage());
		}
	}

	public Map<String, Object> validateEmailIds(Set<UserDTO> users) {
		String key = "statusCode";
		Map<String, Object> map = new HashMap<>();
		List<String> invalidEmailIds = new ArrayList<>();
		for (UserDTO user : users) {
			if (user != null && StringUtils.isNoneBlank(user.getEmailId().trim())) {
				if (!emailValidator.validate(user.getEmailId().trim())) {
					invalidEmailIds.add(user.getEmailId());
				}
			}			
		}
		if (invalidEmailIds.isEmpty()) {
			map.put(key, 200);
		} else {
			map.put("errorMessage", "Following email address(es)'s are invalid ");
			map.put("emailAddresses", invalidEmailIds);
			map.put(key, 409);
		}
		return map;
	}
	
	
	public Map<String, Object> validateEmailIds(List<User> users) {
		String key = "statusCode";
		Map<String, Object> map = new HashMap<>();
		List<String> invalidEmailIds = new ArrayList<>();
		int counter = 0;
		for (User user : users) {
			if (!emailValidator.validate(user.getEmailId().trim())) {
				invalidEmailIds.add(user.getEmailId());
				counter++;
			}
		}
		if (counter == 0) {
			map.put(key, 200);
		} else {
			map.put("errorMessage", "Following email address(es)'s are invalid ");
			map.put("emailAddresses", invalidEmailIds);
			map.put(key, 409);
		}
		return map;
	}
	
	public XtremandResponse validateList(Set<UserDTO> users, String userListName, List<String> names , XtremandResponse xtremandResponse) {
		List<String> invalidEmailIds = new ArrayList<>();

		int counter = 0;
		for (UserDTO user : users) {
			if (!emailValidator.validate(user.getEmailId().trim())) {
				invalidEmailIds.add(user.getEmailId());
				counter++;
			}
		}

		boolean isDuplicate = false;
		for (String existinguserListName : names) {
			if (existinguserListName.equalsIgnoreCase(userListName)) {
				isDuplicate = true;
				break;
			}
		}
		if (counter == 0 && !isDuplicate) {
			xtremandResponse.setStatusCode(200);
		}else if(isDuplicate) {
			xtremandResponse.setStatusCode(401);
			xtremandResponse.setMessage("list name already exists");
		}else if (counter > 0){
			xtremandResponse.setStatusCode(402);
			xtremandResponse.setMessage("Email addresses in your list that aren't formatted properly");
			xtremandResponse.setData(invalidEmailIds);
		}
		return xtremandResponse;
	}

}
