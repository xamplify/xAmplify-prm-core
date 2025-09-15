package com.xtremand.white.labeled.dao.hibernate;

import java.util.List;

import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.xtremand.util.PaginationUtil;
import com.xtremand.white.labeled.dao.WhiteLabeledDao;
import com.xtremand.white.labeled.dao.WhiteLabeledFormDao;
import com.xtremand.white.labeled.dto.WhiteLabeledContentDTO;
import com.xtremand.white.labeled.dto.WhiteLabeledFormDTO;

@Repository
public class HibernateWhiteLabeledFormDao implements WhiteLabeledFormDao {

	/********** Forms ************/
	private static final String XT_WHITE_LABELED_FORMS = "xt_white_labeled_forms";

	private static final String VENDOR_COMPANY_FORM_ID = "vendor_company_form_id";

	@Autowired
	private WhiteLabeledDao whiteLabeledDao;

	@Autowired
	private SessionFactory sessionFactory;

	@Autowired
	private PaginationUtil paginationUtil;

	@Override
	public boolean isWhiteLabeledFormSharedWithPartnerCompanyId(Integer vendorCompanyFormId, Integer vendorCompanyId,
			Integer partnerCompanyId) {
		return whiteLabeledDao.isWhiteLabeledContentSharedWithPartnerCompany(vendorCompanyFormId, vendorCompanyId,
				partnerCompanyId, XT_WHITE_LABELED_FORMS, VENDOR_COMPANY_FORM_ID);
	}

	@Override
	public WhiteLabeledContentDTO findSharedByVendorCompanyNameByForm(Integer receivedWhiteLabeledFormId) {
		return whiteLabeledDao.findSharedByVendorCompanyNameByContentId(receivedWhiteLabeledFormId,
				XT_WHITE_LABELED_FORMS, "received_white_labeled_form_id");
	}

	@Override
	public boolean isVendorCompanyFormWhiteLabeledAndSharedWithPartners(Integer id) {
		return whiteLabeledDao.isVendorCompanyContentWhiteLabeledAndSharedWithPartners(id, XT_WHITE_LABELED_FORMS,
				VENDOR_COMPANY_FORM_ID);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Integer> findSharedVendorCompanyFormIds(Integer vendorCompanyId, Integer partnerCompanyId) {
		Session session = sessionFactory.getCurrentSession();
		String sqlString = "select vendor_company_form_id from xt_white_labeled_forms where shared_by_vendor_company_id = :vendorCompanyId and shared_with_partner_company_id = :partnerCompanyId";
		SQLQuery query = session.createSQLQuery(sqlString);
		query.setParameter("vendorCompanyId", vendorCompanyId);
		query.setParameter("partnerCompanyId", partnerCompanyId);
		return query.list();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<WhiteLabeledFormDTO> findWhiteLabeledForms(Integer vendorCompanyId, Integer partnerCompanyId) {
		Session session = sessionFactory.getCurrentSession();
		String sqlString = "select vf.form_name as \"vendorCompanyFormName\",vf.alias as \"vendorCompanyFormAlias\", wlf.vendor_company_form_id as \"vendorCompanyFormId\",  wf.form_name as \"receivedWhiteLabeledFormName\",wf.alias as \"receivedWhiteLabeledFormAlias\", wlf.received_white_labeled_form_id as \"receivedWhiteLabeledFormId\" "
				+ " from xt_white_labeled_forms wlf,\r\n" + " xt_form vf,xt_form wf\r\n"
				+ " where wlf.shared_by_vendor_company_id = :vendorCompanyId and\r\n"
				+ " wlf.shared_with_partner_company_id = :partnerCompanyId and vf.id = wlf.vendor_company_form_id \r\n"
				+ " and wf.id = wlf.received_white_labeled_form_id";
		SQLQuery query = session.createSQLQuery(sqlString);
		query.setParameter("vendorCompanyId", vendorCompanyId);
		query.setParameter("partnerCompanyId", partnerCompanyId);
		return (List<WhiteLabeledFormDTO>) paginationUtil.getListDTO(WhiteLabeledFormDTO.class, query);
	}

}
