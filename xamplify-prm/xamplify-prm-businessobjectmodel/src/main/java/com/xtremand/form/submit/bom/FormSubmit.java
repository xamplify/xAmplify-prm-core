package com.xtremand.form.submit.bom;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.xtremand.form.bom.Form;
import com.xtremand.landing.page.analytics.bom.GeoLocationAnalytics;
import com.xtremand.lms.bom.LearningTrack;
import com.xtremand.mdf.bom.MdfRequest;
import com.xtremand.user.bom.User;

@Entity
@Table(name = "xt_form_submit")
public class FormSubmit implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8697353085055706769L;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "xt_form_submit_sequence")
	@SequenceGenerator(name = "xt_form_submit_sequence", sequenceName = "xt_form_submit_sequence", allocationSize = 1)
	@Column(name = "id")
	private Integer id;

	@ManyToOne
	@JsonIgnore
	@JoinColumn(name = "form_id")
	private Form form;

	@Column(name = "submitted_on", columnDefinition = "DATETIME")
	@Temporal(TemporalType.TIMESTAMP)
	private Date submittedOn;

	@Fetch(FetchMode.SUBSELECT)
	@OneToMany(mappedBy = "formSubmit", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
	private Set<FormSubmitSingleChoice> singleChoices = new HashSet<>();

	@Fetch(FetchMode.SUBSELECT)
	@OneToMany(mappedBy = "formSubmit", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
	private Set<FormSubmitMultiChoice> multiChoices = new HashSet<>();


	@Column(name = "form_submit_type")
	@org.hibernate.annotations.Type(type = "com.xtremand.form.submit.bom.FormSubmitEnumType")
	private FormSubmitEnum formSubmitEnum;

	@Column(name = "landing_page_id")
	private Integer landingPageId;

	@Column(name = "partner_company_id")
	private Integer partnerCompanyId;

	@OneToOne(cascade = { CascadeType.ALL })
	@JoinColumn(name = "request_id", nullable = true)
	private MdfRequest mdfRequest;

	@ManyToOne
	@JsonIgnore
	@JoinColumn(name = "user_id")
	private User user;

	@ManyToOne
	@JsonIgnore
	@JoinColumn(name = "learning_track_id")
	private LearningTrack learningTrack;

	private Integer score;

	@OneToOne(mappedBy = "formSubmit")
	private GeoLocationAnalytics geoLocationAnalytics;

	@Column(name = "partner_master_landing_page_id")
	private Integer partnerMasterLandingPageId;

	public GeoLocationAnalytics getGeoLocationAnalytics() {
		return geoLocationAnalytics;
	}

	public void setGeoLocationAnalytics(GeoLocationAnalytics geoLocationAnalytics) {
		this.geoLocationAnalytics = geoLocationAnalytics;
	}

	public Integer getScore() {
		return score;
	}

	public void setScore(Integer score) {
		this.score = score;
	}

	public LearningTrack getLearningTrack() {
		return learningTrack;
	}

	public void setLearningTrack(LearningTrack learningTrack) {
		this.learningTrack = learningTrack;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public MdfRequest getMdfRequest() {
		return mdfRequest;
	}

	public void setMdfRequest(MdfRequest mdfRequest) {
		this.mdfRequest = mdfRequest;
	}

	public Integer getPartnerCompanyId() {
		return partnerCompanyId;
	}

	public void setPartnerCompanyId(Integer partnerCompanyId) {
		this.partnerCompanyId = partnerCompanyId;
	}

	/**
	 * @return the id
	 */
	public Integer getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(Integer id) {
		this.id = id;
	}

	/**
	 * @return the form
	 */
	public Form getForm() {
		return form;
	}

	/**
	 * @param form the form to set
	 */
	public void setForm(Form form) {
		this.form = form;
	}

	/**
	 * @return the submittedOn
	 */
	public Date getSubmittedOn() {
		return submittedOn;
	}

	/**
	 * @param submittedOn the submittedOn to set
	 */
	public void setSubmittedOn(Date submittedOn) {
		this.submittedOn = submittedOn;
	}

	/**
	 * @return the serialversionuid
	 */
	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	/**
	 * @return the singleChoices
	 */
	public Set<FormSubmitSingleChoice> getSingleChoices() {
		return singleChoices;
	}

	/**
	 * @param singleChoices the singleChoices to set
	 */
	public void setSingleChoices(Set<FormSubmitSingleChoice> singleChoices) {
		this.singleChoices = singleChoices;
	}

	/**
	 * @return the multiChoices
	 */
	public Set<FormSubmitMultiChoice> getMultiChoices() {
		return multiChoices;
	}

	/**
	 * @param multiChoices the multiChoices to set
	 */
	public void setMultiChoices(Set<FormSubmitMultiChoice> multiChoices) {
		this.multiChoices = multiChoices;
	}


	/**
	 * @return the formSubmitEnum
	 */
	public FormSubmitEnum getFormSubmitEnum() {
		return formSubmitEnum;
	}

	/**
	 * @param formSubmitEnum the formSubmitEnum to set
	 */
	public void setFormSubmitEnum(FormSubmitEnum formSubmitEnum) {
		this.formSubmitEnum = formSubmitEnum;
	}

	/**
	 * @return the landingPageId
	 */
	public Integer getLandingPageId() {
		return landingPageId;
	}

	/**
	 * @param landingPageId the landingPageId to set
	 */
	public void setLandingPageId(Integer landingPageId) {
		this.landingPageId = landingPageId;
	}


	public Integer getPartnerMasterLandingPageId() {
		return partnerMasterLandingPageId;
	}

	public void setPartnerMasterLandingPageId(Integer partnerMasterLandingPageId) {
		this.partnerMasterLandingPageId = partnerMasterLandingPageId;
	}

}
