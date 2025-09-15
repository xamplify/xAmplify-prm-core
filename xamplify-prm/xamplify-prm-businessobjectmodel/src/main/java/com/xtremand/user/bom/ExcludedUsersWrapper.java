package com.xtremand.user.bom;

import java.util.List;

import com.xtremand.formbeans.UserDTO;

import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class ExcludedUsersWrapper {
	List<UserDTO> users;
	List<String> domainNames;
}
