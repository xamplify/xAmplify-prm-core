package com.xtremand.landing.page.analytics.bom;

import java.lang.reflect.InvocationTargetException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionImplementor;

import com.xtremand.common.bom.GenericEnumType;

public class GeoLocationAnalyticsType  extends GenericEnumType<String, GeoLocationAnalyticsEnum> {

	public GeoLocationAnalyticsType() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException{
		super(GeoLocationAnalyticsEnum.class, GeoLocationAnalyticsEnum.values(), "getType", Types.OTHER);
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
