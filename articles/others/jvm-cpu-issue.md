# 记一次 JVM CPU 使用率飙高问题的排查过程

## 问题现象

首先，我们一起看看通过 VisualVM 监控到的机器 CPU 使用率图：

![cpu-usage](https://github.com/guobinhit/cg-blog/blob/master/images/others/jvm-cpu-issue/cpu-usage.png)

如上图所示，在 **下午3:45** 分之前，CPU 的使用率明显飙高，最高飙到近 100%，为什么会出现这样的现象呢？

## 排查过程

**Step 1**：使用`top`命令，查询资源占用情况：

![top](https://github.com/guobinhit/cg-blog/blob/master/images/others/jvm-cpu-issue/top.png)

如上图所示，显示了服务器当前的资源占用情况，其中`PID`为`5456`的进程占用的资源最多。

在这里，我们也使用`top -p PID`命令，查询指定`PID`的资源占用情况：

![top-p](https://github.com/guobinhit/cg-blog/blob/master/images/others/jvm-cpu-issue/top-p.png)

**Step  2**：使用`ps -mp PID -o THREAD,tid,time`命令，查询该进程的线程情况：

![ps](https://github.com/guobinhit/cg-blog/blob/master/images/others/jvm-cpu-issue/ps.png)

在这里，我们也使用`ps -mp PID -o THREAD,tid,time | sort -rn`命令，将该进程下的线程按资源使用情况倒序展示：

![ps-sort](https://github.com/guobinhit/cg-blog/blob/master/images/others/jvm-cpu-issue/ps-sort.png)

**Step  3**：使用`printf "%x\n" PID`命令，将`PID`转为十六进制的`TID`：

![printf](https://github.com/guobinhit/cg-blog/blob/master/images/others/jvm-cpu-issue/printf.png)

在这里，我们之所以需要将`PID`转为十六进制是因为在堆栈信息中，`PID`是以十六进制形式存在的。

**Step  4**：使用`jstack PID | grep TID -A 100`命令，查询堆栈信息：

![jstack](https://github.com/guobinhit/cg-blog/blob/master/images/others/jvm-cpu-issue/jstack.png)

如上图所示，显示该进程下多个线程均处于`TIMED_WAITING`状态。

虽然线程处于`WAITING`或者`TIMED_WAITING`状态都不会消耗 CPU，但是线程频繁的挂起和唤醒却会消耗 CPU，而且代价高昂。

而上面之所以会出现 CPU 使用率飙高的情况，则是因为有人在做压测。

特别地，在 mock 底层接口的时候，使用了类似`TimeUnit.SECONDS.sleep(1)`这样的语句。

至于为何在 **下午3:45** 分之后，CPU 的使用率降下来了，则是因为停止了压测。
