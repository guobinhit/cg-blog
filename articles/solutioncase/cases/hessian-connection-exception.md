# 出现 com.caucho.hessian.client.HessianConnectionException 异常的原因及解决方法

1 异常描述
------

在测试接口实现类`ErrorCodeFacadeImpl`的过程中，报出如下错误：

![1](http://img.blog.csdn.net/20170407164118874)

2 异常原因
------

通过观察上面的错误描述，咱们可以知道错误原因：

> Caused by: java.io.FileNotFoundException：`http://localhost:8014/pp-config-hessian/hessian/ErrorCodeFacade`

此异常，为：**文件未找到异常**。

在此处，说白了，就是上面的请求路径有问题。

3 解决方法
------

既然咱们已经知道了请求路径有问题，那么修改请求路径就可以啦！

事实上，也确实如此，当博主把上面的请求路径修改为：

`http://localhost:8014/pp-config/hessian/ErrorCodeFacade`

之后，这个异常问题就解决啦！
