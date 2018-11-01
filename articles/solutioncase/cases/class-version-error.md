# 出现 java.lang.UnsupportedClassVersionError 错误的原因及解决方法

1 错误描述
------
正常运行程序，报出如下错误：

![000](http://img.blog.csdn.net/20170627090436912)

2 错误原因
------

通过观察上述标记出来的错误描述，咱们可以知道：

> java.lang.UnsupportedClassVersionError：Unsupported major.minor version 51.0

此错误，为：**不支持类版本错误。**

也就是说，不同的 JDK 版本编译出的 class 文件也可能有差异，有的高版本 JDK 能够兼容低版本的 JDK 自然没有问题，但反之就会出现问题啦！

此外，在 Maven 项目中，也有可能出现`java.lang.UnsupportedClassVersionError`的错误，同样是因为版本不同而产生无法识别的错误。不过，在 Maven 项目中造成这样错误的原因可能是不同的 Maven 版本与不同的 Java JDK 的版本之间产生的原因，例如，`maven 3.3.1＋`的版本只能运行在`java JDK 1.7＋`的版本上。


3 解决方法
------

检查项目 SDK 和运行时所用的 JRE，发现使用的都是 1.6 版本，但幸好此前本地还安装了 JDK 1.8 版本，将两者都更好为JDK 1.8 版本之后，此错误解决。当然，如果是 Maven 项目的话，也可能尝试更新 Maven 的版本！

