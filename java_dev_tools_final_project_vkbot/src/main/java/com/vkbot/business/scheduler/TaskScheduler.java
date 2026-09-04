package com.vkbot.business.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Slf4j
@RequiredArgsConstructor
public class TaskScheduler {
    private static final long DEFAULT_INITIAL_DELAY = 0;
    private static final long DEFAULT_PERIOD = 24;
    private static final TimeUnit DEFAULT_TIME_UNIT = TimeUnit.HOURS;

    private final ScheduledExecutorService executorService;
    private final Map<Long, ScheduledFuture<?>> scheduledTasks = new HashMap<>();

    public ScheduledFuture<?> scheduleTask(Long taskId, Runnable task) {
        return scheduleTask(taskId, task, DEFAULT_INITIAL_DELAY, DEFAULT_PERIOD, DEFAULT_TIME_UNIT);
    }

    public ScheduledFuture<?> scheduleTask(Long taskId, Runnable task, long initialDelay,
                                          long period, TimeUnit timeUnit) {
        cancelTask(taskId);

        log.info("Scheduling search task: {}, period: {} {}", taskId, period, timeUnit);

        ScheduledFuture<?> future = executorService.scheduleAtFixedRate(
                task,
                initialDelay,
                period,
                timeUnit
        );

        scheduledTasks.put(taskId, future);
        return future;
    }

    public void cancelTask(Long taskId) {
        ScheduledFuture<?> future = scheduledTasks.remove(taskId);
        if (future != null && !future.isCancelled()) {
            future.cancel(false);
            log.info("Search task cancelled: {}", taskId);
        }
    }

    public boolean hasActiveTask(Long taskId) {
        ScheduledFuture<?> future = scheduledTasks.get(taskId);
        return future != null && !future.isCancelled() && !future.isDone();
    }

    public int getActiveTaskCount() {
        return (int) scheduledTasks.values().stream()
                .filter(f -> !f.isCancelled() && !f.isDone())
                .count();
    }

    public void shutdown() {
        log.info("Shutting down task scheduler");
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(10, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            log.error("Error shutting down executor service", e);
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
