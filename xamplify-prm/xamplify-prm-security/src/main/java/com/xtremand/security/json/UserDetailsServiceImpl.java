package com.xtremand.security.json;

import org.hibernate.Hibernate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.xtremand.user.bom.User;
import com.xtremand.user.bom.User.UserStatus;
import com.xtremand.user.dao.UserDAO;
import com.xtremand.util.CustomExceptionMessage;

@Service
@Transactional(readOnly = true)
public class UserDetailsServiceImpl implements UserDetailsService {

	Logger logger = LoggerFactory.getLogger(UserDetailsServiceImpl.class);

	@Autowired
	UserDAO userDAO;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		User user = userDAO.findByEmailId(username);
		if (user == null) {
			throw new UsernameNotFoundException(CustomExceptionMessage.ACCOUNT_DOESNOT_EXIST);
		} else {
			boolean isUnApprovedUser = UserStatus.UNAPPROVED.equals(user.getUserStatus());
			boolean isDeclinedUser = UserStatus.DECLINE.equals(user.getUserStatus());
			boolean isPasswordEmpty = !StringUtils.hasText(user.getPassword());
			if (isUnApprovedUser && isPasswordEmpty) {
				throw new UsernameNotFoundException(CustomExceptionMessage.ACCOUNT_NOT_CREATED);
			} else if (isUnApprovedUser && StringUtils.hasText(user.getPassword())) {
				throw new UsernameNotFoundException(
						CustomExceptionMessage.ACCOUNT_ACTIVATION_ERROR_AND_RESEND_EMAIL_INVITATION);
			} else if (isDeclinedUser) {
				throw new UsernameNotFoundException(CustomExceptionMessage.ACCOUNT_SUSPENDED);
			} else {
				Hibernate.initialize(user.getRoles());
				return new SecurityUserDetails(user);
			}
		}
	}
}