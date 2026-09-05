package com.systemdesign.developer_tools.task_scheduler.observers;

import com.systemdesign.developer_tools.task_scheduler.ScheduledTask;

public class LoggingObserver implements TaskExecutionObserver {
    @Override
    public void onTaskStarted(ScheduledTask task) {
        // Include thread name so logs show which worker executed which task
        System.out.printf("[%s] Task '%s' started%n",
            Thread.currentThread().getName(), task.getTask().getName());
    }

    @Override
    public void onTaskCompleted(ScheduledTask task) {
        System.out.printf("[%s] Task '%s' completed%n",
            Thread.currentThread().getName(), task.getTask().getName());
    }

    @Override
    public void onTaskFailed(ScheduledTask task, Exception exception) {
        // Use stderr for failures so they stand out in logs
        System.err.printf("[%s] Task '%s' failed: %s%n",
            Thread.currentThread().getName(), task.getTask().getName(),
            exception.getMessage());
    }
}
