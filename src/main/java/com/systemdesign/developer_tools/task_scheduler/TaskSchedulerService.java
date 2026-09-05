package com.systemdesign.developer_tools.task_scheduler;

import java.util.List;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.PriorityQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

import com.systemdesign.developer_tools.task_scheduler.enums.TaskState;
import com.systemdesign.developer_tools.task_scheduler.exceptions.TaskSchedulerException;
import com.systemdesign.developer_tools.task_scheduler.observers.TaskExecutionObserver;
import com.systemdesign.developer_tools.task_scheduler.strategies.SchedulingStrategy;
import com.systemdesign.developer_tools.task_scheduler.tasks.Task;

public class TaskSchedulerService {
    // volatile ensures all threads see the latest instance reference.
    // Without it, a thread might see a partially constructed object
    // due to instruction reordering.
    private static volatile TaskSchedulerService instance;
    private static final Object instanceLock = new Object();

    private final PriorityQueue<ScheduledTask> taskQueue;  // Min-heap by execution time
    private final Object queueLock;     // Monitor object for wait/notify coordination
    private final List<TaskExecutionObserver> observers;
    private Thread[] workers;           // Raw thread pool (no ExecutorService)
    private volatile boolean running;   // volatile so all workers see shutdown immediately
    private final AtomicLong sequenceCounter;  // Generates tiebreaker sequence numbers

    private TaskSchedulerService() {
        this.taskQueue = new PriorityQueue<>();
        this.queueLock = new Object();
        // CopyOnWriteArrayList: safe to iterate while another thread adds observers
        this.observers = new CopyOnWriteArrayList<>();
        this.running = false;
        this.sequenceCounter = new AtomicLong(0);
    }

    public static TaskSchedulerService getInstance() {
        // First check: avoid synchronization overhead on every call
        if (instance == null) {
            synchronized (instanceLock) {
                // Second check: another thread may have created the instance
                // between our first check and acquiring the lock
                if (instance == null) {
                    instance = new TaskSchedulerService();
                }
            }
        }
        return instance;
    }

    public void initialize(int workerCount) {
        if (workerCount <= 0) {
            throw new IllegalArgumentException("Worker count must be positive");
        }
        if (running) {
            throw new TaskSchedulerException("Scheduler is already running");
        }

        this.running = true;
        this.workers = new Thread[workerCount];
        for (int i = 0; i < workerCount; i++) {
            workers[i] = new Thread(this::runWorker, "Scheduler-Worker-" + i);
            // Daemon threads don't prevent JVM shutdown. If the main thread
            // exits without calling shutdown(), the JVM still terminates cleanly.
            workers[i].setDaemon(true);
            workers[i].start();
        }
        System.out.printf("Started %d worker threads%n", workerCount);
    }

    public String schedule(Task task, SchedulingStrategy strategy) {
        if (task == null || strategy == null) {
            throw new IllegalArgumentException("Task and strategy must not be null");
        }
        if (!running) {
            throw new TaskSchedulerException("Scheduler is not running");
        }

        ScheduledTask scheduledTask = new ScheduledTask(
            task, strategy, sequenceCounter.getAndIncrement());

        synchronized (queueLock) {
            taskQueue.add(scheduledTask);
            // Wake ALL waiting workers. One of them will pick up this task.
            // We use notifyAll() instead of notify() because a worker in a
            // timed wait (sleeping until a future task) also needs to wake up
            // and re-evaluate if this new task is earlier than what it's waiting for.
            queueLock.notifyAll();
        }
        return scheduledTask.getId();
    }

    public boolean cancel(String taskId) {
        synchronized (queueLock) {
            // Linear scan through the queue to find the task by ID.
            // PriorityQueue doesn't support O(1) lookup by ID, but cancellation
            // is rare enough that O(n) is acceptable for an interview solution.
            Iterator<ScheduledTask> iterator = taskQueue.iterator();
            while (iterator.hasNext()) {
                ScheduledTask task = iterator.next();
                if (task.getId().equals(taskId)) {
                    task.setStatus(TaskState.CANCELLED);
                    iterator.remove();
                    return true;
                }
            }
        }
        // Task not found: either already executed or invalid ID
        return false;
    }

    public void addObserver(TaskExecutionObserver observer) {
        // CopyOnWriteArrayList handles thread safety internally
        observers.add(observer);
    }

    public void shutdown() {
        running = false;  // volatile write: immediately visible to all workers

        // Wake any workers blocked in wait()
        synchronized (queueLock) {
            queueLock.notifyAll();
        }

        // Interrupt workers that might be sleeping in wait(delayMs)
        // or blocked inside a long-running task
        for (Thread worker : workers) {
            if (worker != null) {
                worker.interrupt();
            }
        }
        System.out.println("Scheduler shut down.");
    }

    private void runWorker() {
        while (running) {
            ScheduledTask task = null;

            // --- CRITICAL SECTION: access the shared queue under the lock ---
            synchronized (queueLock) {
                while (running) {
                    // Case 1: Queue is empty. Sleep until a task is scheduled.
                    if (taskQueue.isEmpty()) {
                        try {
                            // Releases queueLock and sleeps. Wakes when:
                            // - schedule() calls notifyAll() (new task added)
                            // - shutdown() calls notifyAll()
                            queueLock.wait();
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            return;
                        }
                        continue;  // Re-check conditions after waking
                    }

                    // Case 2: Queue has tasks. Check if the earliest one is due.
                    ScheduledTask next = taskQueue.peek();
                    long delayMs = Duration.between(LocalDateTime.now(),
                        next.getNextExecutionTime()).toMillis();

                    if (delayMs <= 0) {
                        // Task is due (or overdue). Remove it and break out
                        // of the synchronized block to execute it.
                        task = taskQueue.poll();
                        break;
                    } else {
                        // Case 3: Task is in the future. Sleep for exactly
                        // the right amount of time. Wakes up when:
                        // - delayMs expires (time to execute)
                        // - schedule() calls notifyAll() (new earlier task)
                        // - shutdown() calls notifyAll()
                        try {
                            queueLock.wait(delayMs);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            return;
                        }
                        // After waking, loop back and re-peek. The queue head
                        // might have changed if a new earlier task was added.
                    }
                }
            }
            // --- END CRITICAL SECTION ---

            // Execute OUTSIDE the synchronized block. This is crucial:
            // if we held the lock during execution, no other worker could
            // access the queue, and the entire scheduler would serialize.
            if (task != null && task.getStatus() != TaskState.CANCELLED) {
                executeTask(task);
            }
        }
    }

    private void executeTask(ScheduledTask scheduledTask) {
        scheduledTask.setStatus(TaskState.RUNNING);
        notifyObservers(scheduledTask, "started");

        try {
            scheduledTask.getTask().execute();
            scheduledTask.setStatus(TaskState.COMPLETED);
            notifyObservers(scheduledTask, "completed");
        } catch (Exception e) {
            // Catch ALL exceptions so a failing task never kills the worker
            scheduledTask.setStatus(TaskState.FAILED);
            notifyObserversFailed(scheduledTask, e);
        }

        // Whether the task succeeded or failed, check if it should run again.
        // For one-time tasks, updateForNextExecution() sets nextExecutionTime to null.
        // For recurring tasks, it calculates the next run time.
        scheduledTask.updateForNextExecution();
        if (scheduledTask.getNextExecutionTime() != null) {
            scheduledTask.setStatus(TaskState.SCHEDULED);
            synchronized (queueLock) {
                taskQueue.add(scheduledTask);
                // Wake workers so they re-evaluate the new queue head
                queueLock.notifyAll();
            }
        }
    }

    private void notifyObservers(ScheduledTask task, String event) {
        for (TaskExecutionObserver observer : observers) {
            try {
                if ("started".equals(event)) observer.onTaskStarted(task);
                else if ("completed".equals(event)) observer.onTaskCompleted(task);
            } catch (Exception e) {
                // Observer failures must NEVER affect task execution or scheduling.
                // A broken logger should not prevent tasks from running.
            }
        }
    }

    private void notifyObserversFailed(ScheduledTask task, Exception exception) {
        for (TaskExecutionObserver observer : observers) {
            try {
                observer.onTaskFailed(task, exception);
            } catch (Exception e) {
                // Same principle: observer errors are silently swallowed
            }
        }
    }

    // For testing: allows resetting the singleton between test cases
    public static void resetInstance() {
        synchronized (instanceLock) {
            instance = null;
        }
    }
}
