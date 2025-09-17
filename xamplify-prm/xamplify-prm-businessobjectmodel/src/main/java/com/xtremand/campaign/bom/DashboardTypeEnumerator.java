package com.xtremand.campaign.bom;

import java.lang.reflect.InvocationTargetException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import org.hibernate.engine.spi.SessionImplementor;

import com.xtremand.common.bom.GenericEnumType;

public class DashboardTypeEnumerator extends GenericEnumType<String, DashboardTypeEnum> {

	public DashboardTypeEnumerator() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
		super(DashboardTypeEnum.class, DashboardTypeEnum.values(), "getDashboardType", Types.OTHER);
	}


	@Override
	public Object nullSafeGet(ResultSet rs, String[] names, SessionImplementor session, Object owner)
			throws SQLException {
		return nullSafeGet(rs, names, owner);
	}

	@Override
	public void nullSafeSet(PreparedStatement st, Object value, int index, SessionImplementor session)
			throws SQLException {
		nullSafeSet(st, value, index);
	}

}
