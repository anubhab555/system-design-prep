package com.systemdesign.developer_tools.task_scheduler.tasks;

public interface Task {
    String getName();  // Human-readable identifier for logging and monitoring
    void execute();    // Performs the actual work
}