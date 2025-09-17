package com.xtremand.queue.service;

import java.util.Map;

public interface CompanyQueueManagerService {

	public void addTaskToQueue(Integer companyId, Runnable task);

	public void processNextTask(Integer companyId);

	public void shutdownAndCleanupResources(Integer companyId);

	public Map<Integer, Integer> getCompanyQueueSizes();

	public Integer getTotalQueueSize();

}
