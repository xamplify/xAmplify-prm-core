package com.xtremand.dam.bom;

import java.lang.reflect.InvocationTargetException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import org.hibernate.engine.spi.SessionImplementor;

import com.xtremand.common.bom.GenericEnumType;

public class DamAnalyticsActionEnumerator extends GenericEnumType<String, DamAnalyticsActionEnum> {

	public DamAnalyticsActionEnumerator() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
		super(DamAnalyticsActionEnum.class, DamAnalyticsActionEnum.values(), "getActionType", Types.OTHER);
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
