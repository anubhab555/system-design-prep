package com.systemdesign.developer_tools.task_scheduler.tasks;

import java.time.LocalTime;

public class PrintMessageTask implements Task{
    private final String message;

    public PrintMessageTask(String message) {
        this.message = message;
    }

    @Override
    public String getName() { return message; }

    @Override
    public void execute() {
        // withNano(0) truncates nanoseconds for cleaner log output
        System.out.printf("[%s] %s%n", LocalTime.now().withNano(0), message);
    }
}
