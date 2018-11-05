# java.net.MalformedURLException: no protocol

1 异常描述
------
在通过 IP 地址及端口号调用远程方法进行单元测试的时候，报出如下异常：

![1](http://img.blog.csdn.net/20170413205845813)


2 异常原因
------

通过观察上图标记出来的异常描述，咱们可以知道：

> java.net.MalformedURLException: no protocol

此异常，为：**no protocol，没有指定通信协议异常。**

3 解决方法
------

既然咱们已经知道了是因为没有指定通信协议，从而导致异常的发生。

那么，咱们再回过头来，看看上面的 URL 是不是少了什么东西啊？少了吗？

好吧，答案是：**没有指定`http`协议，在 URL 前面加上`http://`即可解决此异常。**


----------
———— ☆☆☆ —— [返回 -> 超实用的「Exception」和「Error」解决案例 <- 目录](https://github.com/guobinhit/cg-blog/blob/master/articles/solutioncase/README.md) —— ☆☆☆ ————
