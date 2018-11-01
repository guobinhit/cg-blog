# 出现 org.apache.tomcat.xxx.ClassFormatException: Invalid byte tag in constant pool: 15 异常的原因及解决方法

## 1 异常描述

最近，当我从 SVN 检出项目并正常配置完 Tomcat 之后，启动项目，总是报出如下异常：

![CFE](http://img.blog.csdn.net/20171107152404359)

## 2 异常原因

通过观察上述标记出来的异常描述，我们可以知道：

> org.apache.tomcat.util.bcel.classfile.ClassFormatException: Invalid byte tag in constant pool: 15

此异常，为：**类格式异常：常量池中无效的字节标记：15**.

在网上搜索过后，发现遇到这个问题的同学并不少，解决的方法也不尽相同，而且解决问题的方法并不通用，例如 A 和 B 两位同学都遇到了上述的异常，但 A 解决此问题的方法并不一定适用于 B 同学。

想了想，这也正常，从异常的表现层面来看，异常都是相同的，但引起异常的实际原因却不一定相同，例如对于`java.lang.UnsupportedClassVersionError`这个异常，我们知道可能是由于 JDK 版本不兼容引起的，但实际上也可能是 Maven 版本和 JDK 版本不兼容（前提是 Maven 项目）引起的。


## 3 解决方法

- **方法 1**：升级 Tomcat 版本到 8 以上

网上很多同学说，此异常为 Tomcat 7 及以下版本的 bug，因此升级到 Tomcat 8 及以上版本之后，此异常就解决啦！对于这种方法，我并没有尝试，之所以在此列出来，仅想作为一个解决异常的可能方法，供大家选择，待验证。

- **方法 2**：修改 Tomcat 的`web.xml`，添加`metadata-complete="true"`到`web-app`头

对于上述异常，无论是在百度搜索还是通过 Stack Overflow 检索，很多同学都发言称验证了 **方法 2** 的可行性。我也尝试通过 **方法2** 来解决此异常，具体方法为：进入 Tomcat 的`conf`目录，修改`web.xml`的`web-app`头，内容为

```
<web-app version="3.0" 
xmlns="http://java.sun.com/xml/ns/javaee" 
xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
xsi:schemaLocation="http://java.sun.com/xml/ns/javaee http://java.sun.com/xml/ns/javaee/web-app_3_0.xsd" metadata-complete="true">
```

正常情况下，截止到此步骤，上述异常应该就可以解决啦！

But，实际上，当我的 JDK 版本为 1.8 的时候，此异常并没有解决。不过当我将 JDK 切换为 1.6 的时候，此异常解决了。但却报出如下错误，

![CME2](http://img.blog.csdn.net/20171107160047154)

对于上图所示的错误，我们可以参考「[出现 java.lang.UnsupportedClassVersionError 错误的原因及解决方法](https://github.com/guobinhit/solutioncase-throwable/blob/master/solution-cases/class-version-error.md)」这篇文章进行解决。不过让我纠结的是，正常情况下，报出这个错误，程序一般都会挂掉了，但我遇到的情况确是：此错误被忽略，项目启动成功了，囧。

就如我前文所言，对于同一个异常或错误，其产生的原因并不一定相同，因此解决方法也不一定相同。此文是为了给大家解决此类问题提供一个参考，仅做抛砖引玉之用，如果大家有其他的解决方法，欢迎在此分享。


----------

**温馨提示**：对于上述的异常，我曾经也遇到过，但解决的方法却和此文的两种方法都不相同，具体可以参考「[出现 org.apache.tomcat.util.bcel.classfile.ClassFormatException 异常的原因及解决方法](https://github.com/guobinhit/solutioncase-throwable/blob/master/solution-cases/class-format-exception.md)」.

