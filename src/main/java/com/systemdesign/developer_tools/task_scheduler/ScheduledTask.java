package com.systemdesign.developer_tools.task_scheduler;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import com.systemdesign.developer_tools.task_scheduler.enums.TaskState;
import com.systemdesign.developer_tools.task_scheduler.strategies.SchedulingStrategy;
import com.systemdesign.developer_tools.task_scheduler.tasks.Task;

public class ScheduledTask implements Comparable<ScheduledTask>{
    private final String id;
    private final Task task;
    private final SchedulingStrategy strategy;
    private LocalDateTime nextExecutionTime;
    private LocalDateTime lastExecutionTime;
    private TaskState status;
    private final long sequenceNumber;

    public ScheduledTask(Task task, SchedulingStrategy strategy, long sequenceNumber) {
        this.id = UUID.randomUUID().toString();
        this.task = task;
        this.strategy = strategy;
        this.sequenceNumber = sequenceNumber;
        this.lastExecutionTime = null;
        this.status = TaskState.SCHEDULED;

        // Ask the strategy for the initial execution time.
        // Passing null signals "this task has never run before."
        Optional<LocalDateTime> firstTime = strategy.getNextExecutionTime(null);
        this.nextExecutionTime = firstTime.orElse(null);
    }

    public String getId() { return id; }
    public Task getTask() { return task; }
    public LocalDateTime getNextExecutionTime() { return nextExecutionTime; }
    public TaskState getStatus() { return status; }
    public void setStatus(TaskState status) { this.status = status; }

    public boolean hasMoreExecutions() {
        return strategy.getNextExecutionTime(lastExecutionTime).isPresent();
    }

    public void updateForNextExecution() {
        this.lastExecutionTime = LocalDateTime.now();
        Optional<LocalDateTime> nextTime = strategy.getNextExecutionTime(lastExecutionTime);
        this.nextExecutionTime = nextTime.orElse(null);
    }

    @Override
    public int compareTo(ScheduledTask other){
        if(nextExecutionTime  == null && other.nextExecutionTime == null) return 0;
        if(this.nextExecutionTime == null) return 1;
        if(other.nextExecutionTime == null) return -1;

        int timeCompare = this.nextExecutionTime.compareTo(other.nextExecutionTime);
        if(timeCompare != 0) return timeCompare;

        return Long.compare(this.sequenceNumber, other.sequenceNumber);
    }

    @Override
    public String toString(){
        return String.format("ScheduledTask[%s, next=%s, status=%s]",
            task.getName(), nextExecutionTime, status);
    }
}
