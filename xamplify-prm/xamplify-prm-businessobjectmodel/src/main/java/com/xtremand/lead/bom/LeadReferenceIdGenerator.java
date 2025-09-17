package com.xtremand.lead.bom;

import org.hibernate.Session;
import org.hibernate.tuple.ValueGenerator;

public class LeadReferenceIdGenerator implements ValueGenerator<String>  {
	

	@Override
	public String generateValue(Session session, Object owner) {
		return "" + session.createSQLQuery("select nextval('xt_crm_lead_ref_id_sequence')")
	               .uniqueResult();
	}
}
