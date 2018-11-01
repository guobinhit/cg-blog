# 出现 org.apache.tomcat.util.bcel.classfile.ClassFormatException 异常的原因及解决方法


1 异常描述
------
在从 SVN 正常检出项目后，部署完 Maven 及配置完 Tomcat 之后，按常规方法启动服务器，报出如下错误：

![1](http://img.blog.csdn.net/20170414174230957)



2 异常原因
------

通过观察上图标记出来的异常描述，咱们可以知道：

> org.apache.tomcat.util.bcel.classfile.ClassFormatException: Invalid byte tag in constant pool: 15

此异常，为：**ClassFormatException，类格式异常**。

也就是说，Java JDK 的版本选择有问题，编译时用的 JDK 与现在运行时用的 JDK 版本不兼容，从而导致此异常的发生。

倒是和「[java.lang.UnsupportedClassVersionError：Unsupported major.minor version 51.0](https://github.com/guobinhit/SolutionCase-Exception-and-Error/blob/master/solution-cases/class-version-error.md)」这个异常有些类似。

3 解决方法
------

既然咱们已经知道了此异常发生的原因，那么更换 JDK 之后，即可解决此问题。

悄悄的说一句，虽然知道异常发生的原因，但博主在解决这个异常的时候，却花费了很长时间。至于原因嘛，项目编译时用的 JDK 是`1.6.0_21`，但是运行项目时，博主使用的 JDK 是`1.6.0_65`版本，报出此异常；之后，更换 JDK 为`1.8.0_112`版本后仍然报出此异常。

最后测试完，发现 JDK 高于`1.6.0_21`版本的`1.6.0_39`和`1.7.0_67`可以运行，我也是醉啦！这谁能想到呢？囧。



