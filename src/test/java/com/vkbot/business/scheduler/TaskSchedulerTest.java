package com.vkbot.business.scheduler;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TaskSchedulerTest {

    private ScheduledExecutorService executorService;
    private TaskScheduler taskScheduler;

    @BeforeEach
    void setUp() {
        executorService = Executors.newScheduledThreadPool(2);
        taskScheduler = new TaskScheduler(executorService);
    }

    @AfterEach
    void tearDown() {
        taskScheduler.shutdown();
    }

    @Test
    void scheduleTask_registersActiveTask() {
        taskScheduler.scheduleTask(1L, () -> { }, 1, 1, TimeUnit.HOURS);

        assertTrue(taskScheduler.hasActiveTask(1L));
        assertTrue(taskScheduler.getActiveTaskCount() >= 1);
    }

    @Test
    void scheduleTask_defaultOverloadUsesTwentyFourHourPeriod() {
        ScheduledFuture<?> future = taskScheduler.scheduleTask(1L, () -> { });

        assertFalse(future.isCancelled());
        assertTrue(taskScheduler.hasActiveTask(1L));
    }

    @Test
    void scheduleTask_reschedulingSameTaskIdCancelsPreviousJob() {
        ScheduledFuture<?> first = taskScheduler.scheduleTask(1L, () -> { }, 1, 1, TimeUnit.HOURS);
        ScheduledFuture<?> second = taskScheduler.scheduleTask(1L, () -> { }, 1, 1, TimeUnit.HOURS);

        assertTrue(first.isCancelled());
        assertFalse(second.isCancelled());
        assertTrue(taskScheduler.hasActiveTask(1L));
    }

    @Test
    void cancelTask_cancelsAndRemovesTask() {
        taskScheduler.scheduleTask(1L, () -> { }, 1, 1, TimeUnit.HOURS);

        taskScheduler.cancelTask(1L);

        assertFalse(taskScheduler.hasActiveTask(1L));
    }

    @Test
    void cancelTask_onUnknownTaskIdIsNoOp() {
        assertFalse(taskScheduler.hasActiveTask(999L));

        taskScheduler.cancelTask(999L);

        assertFalse(taskScheduler.hasActiveTask(999L));
    }

    @Test
    void hasActiveTask_falseForUnknownTask() {
        assertFalse(taskScheduler.hasActiveTask(42L));
    }

    @Test
    void getActiveTaskCount_reflectsMultipleScheduledTasks() {
        taskScheduler.scheduleTask(1L, () -> { }, 1, 1, TimeUnit.HOURS);
        taskScheduler.scheduleTask(2L, () -> { }, 1, 1, TimeUnit.HOURS);

        assertTrue(taskScheduler.getActiveTaskCount() >= 2);

        taskScheduler.cancelTask(1L);

        assertTrue(taskScheduler.getActiveTaskCount() >= 1);
    }
}
