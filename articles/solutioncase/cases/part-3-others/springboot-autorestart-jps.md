# Spring Boot 项目自动重启以及 jps 查不到 java 进程的原因及解决方法

## 问题背景

新开发了一个 Spring Boot 项目，直接打包部署到了服务器，使用`bash`脚本启动程序，日志输出到直接指定`nohup.out`文件。

## 问题描述

实际上，遇到了有两个问题，分别是：

1. 程序总是在运行了一段时间后自动重启，而且不断的重启；
2. 在程序正常运行期间，使用`jps`命令查看 java 进程，查不到该应用的进程。

## 解决方法

在程序出现问题之后，查看日志，发现日志文件的头部，也就是程序启动时出输出了如下内容：

```xml
The Class-Path manifest attribute in /usr/local/judge-alarm/judge-alarm-controller/judge-alarm-controller-1.0-SNAPSHOT.jar referenced one or more files that do not exist:
file:/usr/local/judge-alarm/judge-alarm-controller/lib/unbescape-1.1.0.RELEASE.jar, ....

......

2019-04-19 09:58:14.266  INFO 24645 --- [      Thread-23] o.s.c.support.DefaultLifecycleProcessor  : Stopping beans in phase 0
2019-04-19 09:58:14.267  INFO 24645 --- [      Thread-23] o.s.j.e.a.AnnotationMBeanExporter        : Unregistering JMX-exposed beans on shutdown
```

其中，`/usr/local/judge-alarm/judge-alarm-controller`为项目的部署目录，`judge-alarm-controller-1.0-SNAPSHOT.jar`为项目执行`mvn package`命令后打成的 jar 包，通过观察上述内容，我们可以知道项目中引用了`unbescape-1.1.0.RELEASE.jar`的内容，有可能是间接引用，即通过 Maven 传递依赖引间接入的，但是在打包后`MANIFEST.MF`文件并没有包括该 jar 包。在检查项目后，发现没有用到该 jar 包，因此在排除依赖后，重新打包、部署、启动项目，该问题解决。现在，我们来看项目重复启动的问题：

![stop-shutdown](https://github.com/guobinhit/cg-blog/blob/master/images/solutioncase/part-3-others/es-hdfs-permission/stop-shutdown.png)

如上图所示，观察日志，我们发现在项目关闭后，仅留下了如下两条信息：


```xml
2019-04-19 09:58:14.266  INFO 24645 --- [      Thread-23] o.s.c.support.DefaultLifecycleProcessor  : Stopping beans in phase 0
2019-04-19 09:58:14.267  INFO 24645 --- [      Thread-23] o.s.j.e.a.AnnotationMBeanExporter        : Unregistering JMX-exposed beans on shutdown
```

然后就是不断重启、关闭、重启、关闭.....，如果我们拿着上面两条信息去网上搜索解决方法，大多数文章给出的答案是添加 Spring Boot 的`web`或者`tomcat`依赖，即：

```xml
 <dependency>
 	<groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
<!-- 或者 -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-tomcat</artifactId>
</dependency>
```

对于这些解决方法，都是为了引入`spring-boot-starter-tomcat`依赖，而如果我们已经引入了`spring-boot-starter-web`依赖、甚至是`spring-boot-starter-thymeleaf`依赖的话，我们根本就不用显示的引入`spring-boot-starter-tomcat`依赖，因为它们三个之间是具有传递依赖关系的，即：

```
+- org.springframework.boot:spring-boot-starter-thymeleaf:jar
|  +- org.springframework.boot:spring-boot-starter-web:jar
|  |  +- org.springframework.boot:spring-boot-starter-tomcat:jar
```

但实际上，现在程序是能够启动的，这也就说明了并不是因为缺少`tomcat`依赖的原因。如果我们再次查看日志文件头部内容的话，会发现在程序启动时，输出了两条`devtools`日志：
![devtools-settings](https://github.com/guobinhit/cg-blog/blob/master/images/solutioncase/part-3-others/es-hdfs-permission/devtools-settings.png)

该日志消息来自于`spring-boot-devtools`依赖，而该依赖的作用就是方便我们进行热部署，即在程序有变化的时候，自动重启服务，这也就是我们的 Spring Boot 项目自动重启的原因了。因此，在`pom.xml`文件中删除该依赖，重新打包、部署、启动项目，该问题解决。接下来，我们来看最后一个问题，那就是：使用`jps`命令查询不到正在运行的 Spring Boot 项目的 java 进程。

这个问题得从`jps`的执行原理说起，在 Java 程序启动后，会在`tmp`目录下生成一个名为`hsperfdata_用户名`的文件夹，在这个文件夹中会有一些以 java 进程`pid`命名的文件。在我们使用`jps`命令查询进程信息的时候，实际上就是将这个文件夹下的文件列出来，因此当这个文件夹为空或者这个文件夹的所有者和文件所属组权限与运行 Java 程序的用户权限不一致时，`jps`命令就查询不到该进程了。解决的方法很简单，用么是修改文件夹的权限，要么是直接使用`sudo jps`命令以`root`权限执行`jps`命令即可。



----------
———— ☆☆☆ —— [返回 -> 超实用的「Exception」和「Error」解决案例 <- 目录](https://github.com/guobinhit/cg-blog/blob/master/articles/solutioncase/README.md) —— ☆☆☆ ————