package com.xtremand.log.bom;

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

@Entity
@Table(name="xt_shorten_url_log")
public class URLShortener{

	
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator="shorten_url_log_id_seq")
	@SequenceGenerator(
			name="shorten_url_log_id_seq",
			sequenceName="shorten_url_log_id_seq",
			allocationSize=1
			)
	@Column(name="id")
	private Integer id;
	
	@Column(name="url", unique = true,  nullable = false)
	private String url;
	
	@Column(name="alias", unique = true,  nullable = false)
	private String alias;
	
	@Column(name = "created_time", columnDefinition = "DATETIME")
	@Temporal(TemporalType.TIMESTAMP)
	private Date createdTime;
	
	
	
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
	
	public URLShortener() {
		super();
	}
	public URLShortener(String url, String alias) {
		super();
		this.url = url;
		this.alias = alias;
	}
	public Date getCreatedTime() {
		return createdTime;
	}
	public void setCreatedTime(Date createdTime) {
		this.createdTime = createdTime;
	}
	@Override
	public String toString() {
		return "URLShortener (id=" + id + ", url=" + url + ", alias=" + alias + ", createdTime=" + createdTime + ")";
	}
	

	
}
