package com.xtremand.godaddy.dao;

public interface GoDaddyConfigurationDao {

	void updateGodaddyConfiguration(Integer companyId,boolean isConnected,String domainName);

	boolean isGodaddyConfigured(Integer companyId);

	String getDomainName(Integer companyId);

}
