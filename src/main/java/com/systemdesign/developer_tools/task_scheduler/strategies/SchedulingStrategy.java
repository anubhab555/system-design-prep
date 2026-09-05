package com.systemdesign.developer_tools.task_scheduler.strategies;

import java.time.LocalDateTime;
import java.util.Optional;

public interface SchedulingStrategy {
    Optional<LocalDateTime> getNextExecutionTime(LocalDateTime lastExecutionTime);
}