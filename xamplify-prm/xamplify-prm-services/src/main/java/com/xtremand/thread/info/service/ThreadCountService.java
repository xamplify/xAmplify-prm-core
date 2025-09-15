package com.xtremand.thread.info.service;

import java.lang.management.ManagementFactory;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ThreadCountService {

	private static final Logger logger = LoggerFactory.getLogger(ThreadCountService.class);

	public void getThreadCountDetails() {
		Set<Thread> threadSet = Thread.getAllStackTraces().keySet();
		logger.debug("*******Thread Info Started************");
		for (Thread t : threadSet) {
			logger.debug("\nThread Name:-" + t.getName() + "\nTotal Number Of Threads:-"
					+ ManagementFactory.getThreadMXBean().getThreadCount() + "\nActive Threads:-" + Thread.activeCount()
					+ "\n------------------------------------------------------------------------");
		}
		logger.debug("*******Thread Info Ended************");
		logger.debug("******************************************************************************");
	}

	public void findMemoryDetails() {
		Runtime runtime = Runtime.getRuntime();
		long maxMemory = runtime.maxMemory();
		logger.debug("Max Memory" + maxMemory / 1024);
		long mTotalMemory = runtime.totalMemory();
		logger.debug("Total Memory" + mTotalMemory / 1024);
		long freeMemory = runtime.freeMemory();
		logger.debug("Free Memory" + freeMemory / 1024);
		int mAvailableProcessors = runtime.availableProcessors();
		logger.debug("Available Processeor" + mAvailableProcessors);
		long mTotalFreeMemory = freeMemory + (maxMemory - mTotalMemory);
		logger.debug("Total Free Memory" + mTotalFreeMemory / 1024);
	}

}
