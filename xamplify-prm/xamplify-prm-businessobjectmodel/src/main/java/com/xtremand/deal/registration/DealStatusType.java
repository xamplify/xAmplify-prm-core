package com.xtremand.deal.registration;

import java.lang.reflect.InvocationTargetException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import org.hibernate.engine.spi.SessionImplementor;

import com.xtremand.common.bom.GenericEnumType;

public class DealStatusType extends   GenericEnumType<String, CampaignDealStatusEnum> {

	public DealStatusType() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
		super(CampaignDealStatusEnum.class, CampaignDealStatusEnum.values(), "getDealStatus", Types.OTHER);
	}

	@Override
	public Object nullSafeGet(ResultSet rs, String[] names,
			SessionImplementor session, Object owner)
					throws SQLException {
		return nullSafeGet(rs, names, owner);
	}

	@Override
	public void nullSafeSet(PreparedStatement st, Object value, int index,
			SessionImplementor session) throws SQLException {
		nullSafeSet(st, value, index);
	}


}
