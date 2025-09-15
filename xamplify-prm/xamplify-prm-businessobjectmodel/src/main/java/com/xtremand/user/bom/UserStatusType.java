package com.xtremand.user.bom;
import java.lang.reflect.InvocationTargetException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionImplementor;

import com.xtremand.common.bom.GenericEnumType;
import com.xtremand.user.bom.User.UserStatus;
public class UserStatusType extends GenericEnumType<String, UserStatus> {
	public UserStatusType() throws NoSuchMethodException,
	InvocationTargetException, IllegalAccessException {
		super(UserStatus.class, UserStatus.values(), "getStatus", Types.OTHER);
	}

	@Override
	public Object nullSafeGet(ResultSet rs, String[] names,
			SessionImplementor session, Object owner)
					throws HibernateException, SQLException {
		return nullSafeGet(rs, names, owner);
	}

	@Override
	public void nullSafeSet(PreparedStatement st, Object value, int index,
			SessionImplementor session) throws HibernateException, SQLException {
		nullSafeSet(st, value, index);
	}
}
