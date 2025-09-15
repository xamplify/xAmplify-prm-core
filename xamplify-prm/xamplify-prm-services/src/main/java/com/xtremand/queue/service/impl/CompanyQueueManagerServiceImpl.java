package com.xtremand.queue.service.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.xtremand.queue.service.CompanyQueueManagerService;

@Service("CompanyQueueManagerService")
@Transactional
public class CompanyQueueManagerServiceImpl implements CompanyQueueManagerService{

	private static final Logger logger = LoggerFactory.getLogger(CompanyQueueManagerServiceImpl.class);

	private static final Map<Integer, BlockingQueue<Runnable>> companyQueues = new ConcurrentHashMap<>();
    private static final Map<Integer, ExecutorService> companyExecutors = new ConcurrentHashMap<>();
    private static final Map<Integer, AtomicBoolean> companyFlags = new ConcurrentHashMap<>();

    @Override
    public void addTaskToQueue(Integer companyId, Runnable task) {
    	
        companyQueues.putIfAbsent(companyId, new LinkedBlockingQueue<>());
        companyExecutors.putIfAbsent(companyId, Executors.newSingleThreadExecutor());
        companyFlags.putIfAbsent(companyId, new AtomicBoolean(false));

        companyQueues.get(companyId).add(task);
        String debugMessage = "QUEUE MANAGER:- Task added to queue for CompanyId: " + companyId + ", TimeStamp: " + new Date();
        logger.debug(debugMessage);
    }

    @Override
    public void processNextTask(Integer companyId) {
        ExecutorService executor = companyExecutors.get(companyId);
        BlockingQueue<Runnable> queue = companyQueues.get(companyId);
        AtomicBoolean isProcessing = companyFlags.get(companyId);
        
        if (executor != null && queue != null && isProcessing != null && !executor.isShutdown()
                && isProcessing.compareAndSet(false, true)) {

            Runnable task = queue.poll();
            if (task != null) {
                executor.execute(() -> {
                    try {
                        task.run();
                        logger.debug("QUEUE MANAGER:- Task executed for CompanyId: {}, TimeStamp: {}", companyId, new Date());
                    } catch (Exception e) {
                        logger.debug("QUEUE MANAGER:- Error executing task for CompanyId: {}, Message: {}", companyId, e.getMessage());
                        processNextTask(companyId);
                    } finally {
                        isProcessing.set(false);
                        logger.debug("QUEUE MANAGER:- Checking next task for CompanyId: {}, TimeStamp: {}", companyId, new Date());
                        processNextTask(companyId);
                    }
                });
            } else {
                isProcessing.set(false);
                logger.debug("QUEUE MANAGER:- No more tasks. Cleaning up resources for CompanyId: {}, TimeStamp: {}", companyId, new Date());
                shutdownAndCleanupResources(companyId);
            }
        }
    }

    @Override
    public void shutdownAndCleanupResources(Integer companyId) {
        ExecutorService executor = companyExecutors.remove(companyId);
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
        }
        companyQueues.remove(companyId);
        companyFlags.remove(companyId);
        logger.debug("QUEUE MANAGER:- Cleaned up resources for CompanyId: {}, TimeStamp: {}", companyId, new Date());
    }
    
    @Override
    public Map<Integer, Integer> getCompanyQueueSizes() {
        Map<Integer, Integer> sizes = new HashMap<>();
        for (Map.Entry<Integer, BlockingQueue<Runnable>> entry : companyQueues.entrySet()) {
            sizes.put(entry.getKey(), entry.getValue().size());
        }
        return sizes;
    }
    
    @Override
    public Integer getTotalQueueSize() {
        return companyQueues.values().stream().mapToInt(BlockingQueue::size).sum();
    }
	
}