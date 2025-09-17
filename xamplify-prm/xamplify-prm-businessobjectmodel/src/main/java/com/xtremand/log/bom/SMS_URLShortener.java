package com.xtremand.log.bom;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name="xt_sms_shorten_url_log")
public class SMS_URLShortener{

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator="sms_shorten_url_log_id_seq")
	@SequenceGenerator(
			name="sms_shorten_url_log_id_seq",
			sequenceName="sms_shorten_url_log_id_seq",
			allocationSize=1
			)
	@Column(name="id")
	private Integer id;
	private String url;
	private String alias;
	
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getAlias() {
		return alias;
	}
	public void setAlias(String alias) {
		this.alias = alias;
	}
	
	public SMS_URLShortener() {
		super();
	}
	public SMS_URLShortener(String url, String alias) {
		super();
		this.url = url;
		this.alias = alias;
	}
	@Override
	public String toString() {
		return "SMS_URLShortener [id=" + id + ", url=" + url + ", alias=" + alias + "]";
	}	
	
}
