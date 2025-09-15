package com.xtremand.user.bom;

import java.util.HashSet;
import java.util.Set;

import com.xtremand.formbeans.UserDTO;
import com.xtremand.formbeans.UserListDTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserUserListWrapper {
	Set<UserDTO> users;
	UserListDTO userList;


	private Set<String> newlyAddedEmailIds = new HashSet<>();
}
