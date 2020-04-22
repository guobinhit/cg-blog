# 调度服务 ScheduledExecutorService 经常卡顿问题的排查及解决方法

## 问题描述
首先，给出调度服务的 Java 代码示例：

```java
@Slf4j
@Component
public class TaskProcessSchedule {

    // 核心线程数
    private static final int THREAD_COUNT = 10;

    // 查询数据步长
    private static final int ROWS_STEP = 30;

    @Resource
    private TaskDao taskDao;

    @Resource
    private TaskService taskService;

    private static ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(THREAD_COUNT);

    public TaskProcessSchedule() {
        for (int i = 0; i < THREAD_COUNT; i++) {
            scheduledExecutorService.scheduleAtFixedRate(
                    new TaskWorker(i * ROWS_STEP, ROWS_STEP),
                    10,
                    2,
                    TimeUnit.SECONDS
            );
        }
        log.info("TaskProcessSchedule scheduleAtFixedRate start success.");
    }
 
    class TaskWorker implements Runnable {
        private int offset;
        private int rows;

        TaskWorker(int offset, int rows) {
            this.offset = offset;
            this.rows = rows;
        }

        @Override
        public void run() {
            List<Task> taskList = taskDao.selectProcessingTaskByLimitRange(offset, rows);
            if (CollectionUtils.isEmpty(taskList)) {
                return;
            }
            log.info("TaskWorker: current schedule thread name is {}, taskList is {}", Thread.currentThread().getName(), JsonUtil.toJson(taskList));
            taskService.processTask(taskList);         
        }
    }
}
```

如上述代码所示，启动 10 个调度线程，延迟 10 秒，开始执行定时逻辑，然后每隔 2 秒执行一次定时任务。定时任务类为`TaskWorker`，其要做的事就是根据`offset`和`rows`参数，到数据库捞取指定范围的待处理记录，然后送到`TaskService`的`processTask`方法中进行处理。从逻辑上来看，该定时没有什么毛病，但是在执行定时任务的时候，却经常出现卡顿的问题，表现出来的现象就是：**定时任务不执行了**。

## 问题定位
既然已经知道问题的现象了，现在我们就来看看如果定位问题。

- 使用`jps`命令，查询当前服务器运行的 Java 进程`PID`

当然，也可以直接使用`jps | grep "ServerName"`查询指定服务的`PID`，其中`ServerName`为服务名称。

- 使用`jstack PID | grep "schedule"`命令，查询调度线程的状态

![jstack-schedule](https://github.com/guobinhit/cg-blog/blob/master/images/others/schedule-issue/jstack-schedule.png)

如上图所示，发现我们启动的 10 个调度线程均处于`WAITING`状态。

- 使用`jstack PID | grep "schedule-task-10" -A 50`命令，查询指定线程的详细信息

![schedule-task-10](https://github.com/guobinhit/cg-blog/blob/master/images/others/schedule-issue/schedule-task-10.png)

如上图所示，我们可以知道调度线程在执行`DelayedWorkQueue`的`take()`方法的时候被卡主了。


## 深入分析
通过上面的问题定位，我们已经知道了代码卡在了这里：

```java
at java.util.concurrent.ScheduledThreadPoolExecutor$DelayedWorkQueue.take(ScheduledThreadPoolExecutor.java:1088)
```

那么接下来，我们就详细分析一下出问题的代码。

```java
        public RunnableScheduledFuture<?> take() throws InterruptedException {
            final ReentrantLock lock = this.lock;
            lock.lockInterruptibly();
            try {
                for (;;) {
                    RunnableScheduledFuture<?> first = queue[0];
                    if (first == null)
                        available.await();
                    else {
                        long delay = first.getDelay(NANOSECONDS);
                        if (delay <= 0)
                            return finishPoll(first);
                        first = null; // don't retain ref while waiting
                        if (leader != null)
                            available.await(); // 1088 行代码
                        else {
                            Thread thisThread = Thread.currentThread();
                            leader = thisThread;
                            try {
                                available.awaitNanos(delay);
                            } finally {
                                if (leader == thisThread)
                                    leader = null;
                            }
                        }
                    }
                }
            } finally {
                if (leader == null && queue[0] != null)
                    available.signal();
                lock.unlock();
            }
        }
```

由于上述代码可知，当延迟队列的任务为空，或者当任务不为空且`leader`线程不为`null`的时候，都会调用`await`方法；而且，就算`leader`为`null`，后续也会调用`awaitNanos`方法进行延迟设置。下面， 我们再来看看提交任务的方法`scheduleAtFixedRate`：

```java
    public ScheduledFuture<?> scheduleAtFixedRate(Runnable command,
                                                  long initialDelay,
                                                  long period,
                                                  TimeUnit unit) {
        if (command == null || unit == null)
            throw new NullPointerException();
        if (period <= 0)
            throw new IllegalArgumentException();
        ScheduledFutureTask<Void> sft =
            new ScheduledFutureTask<Void>(command,
                                          null,
                                          triggerTime(initialDelay, unit),
                                          unit.toNanos(period));
        RunnableScheduledFuture<Void> t = decorateTask(command, sft);
        sft.outerTask = t;
        delayedExecute(t);
        return t;
    }
```

在`scheduleAtFixedRate`方法中会调用`decorateTask`方法装饰任务`t`，然后再将该任务扔到`delayedExecute`方法中进行处理。

```java
    private void delayedExecute(RunnableScheduledFuture<?> task) {
        if (isShutdown())
            reject(task);
        else {
            super.getQueue().add(task);
            if (isShutdown() &&
                !canRunInCurrentRunState(task.isPeriodic()) &&
                remove(task))
                task.cancel(false);
            else
                ensurePrestart();
        }
    }
```

在`delayedExecute`方法中，主要是检查线程池中是否可以创建线程，如果不可以，则拒绝任务；否则，向任务队列中添加任务并调用`ensurePrestart`方法。

```java
    void ensurePrestart() {
        int wc = workerCountOf(ctl.get());
        if (wc < corePoolSize)
            addWorker(null, true);
        else if (wc == 0)
            addWorker(null, false);
    }
```

在`ensurePrestart`方法中，主要就是判断工作线程数量是否大于核心线程数，然后根据判断的结果，使用不同的参数调用`addWorker`方法。

```java
    private boolean addWorker(Runnable firstTask, boolean core) {
        retry:
        for (;;) {
            int c = ctl.get();
            int rs = runStateOf(c);

            // Check if queue empty only if necessary.
            if (rs >= SHUTDOWN &&
                ! (rs == SHUTDOWN &&
                   firstTask == null &&
                   ! workQueue.isEmpty()))
                return false;

            for (;;) {
                int wc = workerCountOf(c);
                if (wc >= CAPACITY ||
                    wc >= (core ? corePoolSize : maximumPoolSize))
                    return false;
                if (compareAndIncrementWorkerCount(c))
                    break retry;
                c = ctl.get();  // Re-read ctl
                if (runStateOf(c) != rs)
                    continue retry;
                // else CAS failed due to workerCount change; retry inner loop
            }
        }

        boolean workerStarted = false;
        boolean workerAdded = false;
        Worker w = null;
        try {
            w = new Worker(firstTask);
            final Thread t = w.thread;
            if (t != null) {
                final ReentrantLock mainLock = this.mainLock;
                mainLock.lock();
                try {
                    // Recheck while holding lock.
                    // Back out on ThreadFactory failure or if
                    // shut down before lock acquired.
                    int rs = runStateOf(ctl.get());

                    if (rs < SHUTDOWN ||
                        (rs == SHUTDOWN && firstTask == null)) {
                        if (t.isAlive()) // precheck that t is startable
                            throw new IllegalThreadStateException();
                        workers.add(w);
                        int s = workers.size();
                        if (s > largestPoolSize)
                            largestPoolSize = s;
                        workerAdded = true;
                    }
                } finally {
                    mainLock.unlock();
                }
                if (workerAdded) {
                    t.start();
                    workerStarted = true;
                }
            }
        } finally {
            if (! workerStarted)
                addWorkerFailed(w);
        }
        return workerStarted;
    }
```

在`addWorker`方法中，主要目的就是将任务添加到`workers`工作线程池并启动工作线程。接下来，我们再来看看`Worker`的执行逻辑，也就是`run`方法：

```java
        public void run() {
            runWorker(this);
        }
```

在`run`方法中，主要就是将调用转发到外部的`runWorker`方法：

```java
    final void runWorker(Worker w) {
        Thread wt = Thread.currentThread();
        Runnable task = w.firstTask;
        w.firstTask = null;
        w.unlock(); // allow interrupts
        boolean completedAbruptly = true;
        try {
            while (task != null || (task = getTask()) != null) {
                w.lock();
                if ((runStateAtLeast(ctl.get(), STOP) ||
                     (Thread.interrupted() &&
                      runStateAtLeast(ctl.get(), STOP))) &&
                    !wt.isInterrupted())
                    wt.interrupt();
                try {
                    beforeExecute(wt, task);
                    Throwable thrown = null;
                    try {
                        task.run(); // 执行调度任务
                    } catch (RuntimeException x) {
                        thrown = x; throw x;
                    } catch (Error x) {
                        thrown = x; throw x;
                    } catch (Throwable x) {
                        thrown = x; throw new Error(x);
                    } finally {
                        afterExecute(task, thrown);
                    }
                } finally {
                    task = null;
                    w.completedTasks++;
                    w.unlock();
                }
            }
            completedAbruptly = false;
        } finally {
            processWorkerExit(w, completedAbruptly);
        }
    }
```

在`runWorker`方法中，核心操作就是调用`task.run()`，其中`task`为`Runnable`类型，其实现类为`ScheduledFutureTask`，而`ScheduledFutureTask`继承了`FutureTask`类。对于`FutureTask`类，如果在执行`run`方法的过程中抛出异常，则这个异常并不会显示抛出，而是需要我们调用`FutureTask`的`get`方法来获取，因此如果我们在执行调度任务的时候没有进行异常处理，则异常会被吞噬。

特别地，在`FutureTask`类中，大量操作了`sun.misc.Unsafe LockSupport`类，而这个类的`park`方法，正是上面我们排查问题时定位到调度任务卡住的地方。除此之外，如果我们详细阅读了`ScheduledExecutorService`的`scheduleAtFixedRate`的 doc 文档，如下所示：

```java
/**
     * Creates and executes a periodic action that becomes enabled first
     * after the given initial delay, and subsequently with the given
     * period; that is executions will commence after
     * {@code initialDelay} then {@code initialDelay+period}, then
     * {@code initialDelay + 2 * period}, and so on.
     * If any execution of the task
     * encounters an exception, subsequent executions are suppressed.
     * Otherwise, the task will only terminate via cancellation or
     * termination of the executor.  If any execution of this task
     * takes longer than its period, then subsequent executions
     * may start late, but will not concurrently execute.
     *
     * @param command the task to execute
     * @param initialDelay the time to delay first execution
     * @param period the period between successive executions
     * @param unit the time unit of the initialDelay and period parameters
     * @return a ScheduledFuture representing pending completion of
     *         the task, and whose {@code get()} method will throw an
     *         exception upon cancellation
     * @throws RejectedExecutionException if the task cannot be
     *         scheduled for execution
     * @throws NullPointerException if command is null
     * @throws IllegalArgumentException if period less than or equal to zero
     */
    public ScheduledFuture<?> scheduleAtFixedRate(Runnable command,
                                                  long initialDelay,
                                                  long period,
                                                  TimeUnit unit);
```


我们会发现这样一句话：

> **If any execution of the task encounters an exception, subsequent executions are suppressed**.

翻译过来，就是：

> **如果任务的任何执行遇到异常，则禁止后续的执行**。

说白了，就是在执行调度任务的时候，如果遇到了（**未捕获**）的异常，则后续的任务都不会执行了。

## 解决方法

到这里，我们已经知道了问题产生的原因。下面，我们就修改开篇的示例代码，进行优化：

```java
@Slf4j
@Component
public class TaskProcessSchedule {

    // 核心线程数
    private static final int THREAD_COUNT = 10;

    // 查询数据步长
    private static final int ROWS_STEP = 30;

    @Resource
    private TaskDao taskDao;

    @Resource
    private TaskService taskService;

    private static ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(THREAD_COUNT);

    public TaskProcessSchedule() {
        for (int i = 0; i < THREAD_COUNT; i++) {
            scheduledExecutorService.scheduleAtFixedRate(
                    new TaskWorker(i * ROWS_STEP, ROWS_STEP),
                    10,
                    2,
                    TimeUnit.SECONDS
            );
        }
        log.info("TaskProcessSchedule scheduleAtFixedRate start success.");
    }
 
    class TaskWorker implements Runnable {
        private int offset;
        private int rows;

        TaskWorker(int offset, int rows) {
            this.offset = offset;
            this.rows = rows;
        }

        @Override
        public void run() {
            List<Task> taskList = taskDao.selectProcessingTaskByLimitRange(offset, rows);
            if (CollectionUtils.isEmpty(taskList)) {
                return;
            }
            log.info("TaskWorker: current schedule thread name is {}, taskList is {}", Thread.currentThread().getName(), JsonUtil.toJson(taskList));
            try { // 新增异常处理
            	taskService.processTask(taskList);         
            } catch (Throwable e) {
                log.error("TaskWorker come across a error {}", e);
            }
        }
    }
}
```

如上述代码所示，我们对任务的核心逻辑进行了`try-catch`处理，这样当任务再抛出异常的时候，仅会忽略抛出异常的任务，而不会影响后续的任务。这也说明一件事，那就是：**我们在编码的时候，要特别注意对异常情况的处理**。

