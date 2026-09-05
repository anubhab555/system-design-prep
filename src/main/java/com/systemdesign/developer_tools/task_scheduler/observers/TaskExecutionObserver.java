package com.systemdesign.developer_tools.task_scheduler.observers;

import com.systemdesign.developer_tools.task_scheduler.ScheduledTask;

public interface TaskExecutionObserver {
    void onTaskStarted(ScheduledTask task);
    void onTaskCompleted(ScheduledTask task);
    void onTaskFailed(ScheduledTask task, Exception exception);
}
