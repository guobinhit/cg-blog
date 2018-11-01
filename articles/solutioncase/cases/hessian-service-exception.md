# 出现 com.caucho.hessian.io.HessianServiceException 异常的原因及解决方法


1 异常描述
------

在启动 Tomcat 服务器，进行本地单元测试的时候，报出如下错误：

![1](http://img.blog.csdn.net/20170418173018590)



2 异常原因
------

观察上图中被标记出的来异常描述，咱们可以知道：

> com.caucho.hessian.io.HessianServiceException: The service has no method named: XXX

此异常，为：**Hessian 服务异常**。

在进行代码测试的时候，咱们通过 URL 来调通服务，URL 示例如下：

```
private static String url = "http://localhost:8080/user/hessian/UnitTestFacade";
```

以上图为例，由异常描述可知，找不到名为`queryCustomerTest`方法啦！在保证其他地方没有错误的前提下，很有可能就是 URL 写错啦！



3 解决方法
------

仅以博主为例，将上述的 URL 换为：

```
private static String url = "http://localhost:8080/user/hessian/CustomerEntityFacade";
```
即可解决此异常。因为在`UnitTestFacade`接口中根本就没有`queryCustomerTest`方法，能找到才怪了呢！


----------
**温馨提示**：对于此异常，请具体情况具体分析。

