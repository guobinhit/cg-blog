# 出现 java.net.UnknowHostException: XXX.XXX.XXX 异常的原因及解决方法

1 异常描述
------

在从 SVN 检出项目并配置完成后，启动 Tomcat 服务器，报出如下错误：

![netunknow](http://img.blog.csdn.net/20170712142705826)


2 异常原因
------

通过观察上图中被标记出来的异常信息，咱们可以知道

> java.net.UnknowHostException: zk.bass.3g

此异常，为：**未知主机异常**。

说白了，出现这个异常，就是`hosts`文件中没有对应的映射规则！

3 解决方法
------

既然知道了出现此异常的原因，那么咱们只需要在`hosts`文件中配置映射规则即可，例如：

 - `119.75.217.109 zk.bass.3g`

当然，要具体问题具体分析，这就得看咱们的项目需求啦！


----------

**温馨提示**：如果大家不了解如何修改`hosts`文件的话，可以参阅「[详述 hosts 文件的作用及修改 hosts 文件的方法](https://github.com/guobinhit/cg-blog/blob/master/articles-of-blog/tools-and-others/hosts.md)」。
