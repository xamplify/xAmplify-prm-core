package com.xtremand.campaign.bom;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import lombok.Data;

@Entity
@Table(name="xt_unsubscribed_user_history")
@Data
public class UnsubscribedUserHistory {
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator="unsubscribed_user_history_id_seq")
	@SequenceGenerator(
			name="unsubscribed_user_history_id_seq",
			sequenceName="unsubscribed_user_history_id_seq",
			allocationSize=1
			)
	private Integer id;

	@Column(name = "user_id")
	private Integer userId;

	@Column(name = "customer_company_id")
	private Integer customerCompanyId;


	@Column(name = "time", columnDefinition="DATETIME")
	@Temporal(TemporalType.TIMESTAMP)
	private Date time;
	
	@org.hibernate.annotations.Type(type="com.xtremand.campaign.bom.UnsubscribedType")
	@Column(name="type")
	private UNSUBSCRIBEDTYPE type;
	
	@Column(name = "reason")
	private String reason;
	
	public enum UNSUBSCRIBEDTYPE{
		UNSUBSCRIBED("unsubscribed"), RESUBSCRIBED("resubscribed");
		protected String unsubscribedType;
		private UNSUBSCRIBEDTYPE(String unsubscribedType) {
			this.unsubscribedType = unsubscribedType;
		}
		public String geUnsubscribedType() {
			return unsubscribedType;
		}
	}
	
	
}
