package com.xtremand.highlevel.analytics.bom;

import java.lang.reflect.InvocationTargetException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionImplementor;

import com.xtremand.common.bom.GenericEnumType;

public class DownloadModuleType extends GenericEnumType<String, DownloadModule> {
	
	public DownloadModuleType() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
		super(DownloadModule.class, DownloadModule.values(), "getModuleName", Types.OTHER);
	}

	@Override
	public Object nullSafeGet(ResultSet rs, String[] names, SessionImplementor session, Object owner)
			throws HibernateException, SQLException {
		// TODO Auto-generated method stub
		return nullSafeGet(rs, names, owner);
	}

	@Override
	public void nullSafeSet(PreparedStatement st, Object value, int index, SessionImplementor session)
			throws HibernateException, SQLException {
		// TODO Auto-generated method stub
		nullSafeSet(st, value, index);
	}

}
