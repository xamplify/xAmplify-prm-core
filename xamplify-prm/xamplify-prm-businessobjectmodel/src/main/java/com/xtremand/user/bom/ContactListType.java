package com.xtremand.user.bom;

import java.lang.reflect.InvocationTargetException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionImplementor;

import com.xtremand.common.bom.GenericEnumType;
import com.xtremand.user.bom.UserList.ContactListTypeValue;

public class ContactListType extends GenericEnumType<String, ContactListTypeValue>{
	
		public ContactListType() throws NoSuchMethodException,
		InvocationTargetException, IllegalAccessException {
			super(ContactListTypeValue.class, ContactListTypeValue.values(), "getContactListTypeValue", Types.OTHER);
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

