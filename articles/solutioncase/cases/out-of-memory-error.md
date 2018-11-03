# 出现 java.lang.OutOfMemoryError: PermGen space 错误的原因及解决方法

1 错误描述
------

在正常启动 Tomcat  的时候，报出如下错误：

![error-info](https://github.com/guobinhit/cg-blog/blob/master/images/solutioncase/export-exception/error-info.png)

2 错误原因
------

通过观察上面的错误描述，我们可以知道错误原因： 

> java.lang.OutOfMemoryError: PermGen space

此错误，为**内存溢出错误**。更具体的说，是指方法区（永久代）内存溢出！

3 解决方法
------

由于 JDK 自带的虚拟机为 HotSpot，且其支持内存区域的动态扩展，因此可以通过设置虚拟机参数来扩展方法区的内存大小。例如，进入`Run/Debug Configuration`页面，修改虚拟机参数为：

**`-Xms1024M -Xmx2048M -XX:PermSize=128M -XX:MaxPermSize=256M`** 

具体如何配置，如下图所示：

![vm-options](https://github.com/guobinhit/cg-blog/blob/master/images/solutioncase/export-exception/vm-options.png)

其中，各个参数的含义为：

- `-Xms`，表示程序启动时，JVM 堆的初始化最小尺寸参数；
- `-Xmx`，表示程序启动时，JVM 堆的初始化最大尺寸参数；
- `-XX:PermSize`，表示程序启动时，JVM 方法区的初始化最小尺寸参数；
- `-XX:MaxPermSize`，表示程序启动时，JVM 方法区的初始化最大尺寸参数。

对于本例中的错误，实际上，只需要扩展方法区的虚拟机参数即可。

----------


**温馨提示**：在磁盘满足条件的情况下，可自行修改虚拟机参数进行测试。

----------
———— ☆☆☆ —— [返回 -> 超实用的「Exception」和「Error」解决案例 <- 目录](https://github.com/guobinhit/cg-blog/blob/master/articles/solutioncase/README.md) —— ☆☆☆ ————
