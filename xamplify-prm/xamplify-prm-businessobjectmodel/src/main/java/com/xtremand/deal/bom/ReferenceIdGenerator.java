package com.xtremand.deal.bom;

import org.hibernate.Session;
import org.hibernate.tuple.ValueGenerator;

public class ReferenceIdGenerator implements ValueGenerator<String>  {
	

	@Override
	public String generateValue(Session session, Object owner) {
		return "" + session.createSQLQuery("select nextval('xt_crm_ref_id_sequence')")
	               .uniqueResult();
	}

}
