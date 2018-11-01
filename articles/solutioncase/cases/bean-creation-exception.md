# 出现 org.springframework.beans.factory.BeanCreationException 异常的原因及解决方法

1 异常描述
------
在从 SVN 检出项目并配置完成后，启动 Tomcat 服务器，报出如下错误：

![1](http://img.blog.csdn.net/20170417204543814)


2 异常原因
------
通过观察上图中被标记出来的异常信息，咱们可以知道

> org.springframework.beans.factory.BeanCreationException: Error creating bean with name 'XXX'

此异常，为：**注入`bean`失败异常**。

说白了，出现这个异常，就是找不到对应的`bean`啦！能够导致`bean`注入失败的原因包括以下几种但不限于这几种：

 - 对应的`bean`没有添加注解；
 - 对应的`bean`添加注解错误，例如将 Spring 的`@Service`错选成 dubbo 的；
 - 选择错误的自动注入方法等。




3 解决方法
------

既然知道了出现此异常的原因，那咱们就回过头来，去看看对应的`Bean`声明，观察后发现注入`Facade`的代码为：

```
@Autowired
ErrorCodeFacade errorCodeFacade;
```

好吧，错误也就出在了这里，一般来说，在注入`service`层和`biz`层接口的时候，可以用`@Autowired`，例如：
```
@Autowired
ErrorCodeService errorCodeService;
```
但是，在注入`Facade`层接口的时候，应该用`RemoteServiceFactory.getService`，例如：

```
ErrorCodeFacade errorCodeFacade = RemoteServiceFactory.getService(ErrorCodeFacade.class);
```

也就是说，对于这个异常，采用上述代码声明`ErrorCodeFacade`后，即可解决。


----------

**温馨提示**：对于不同的公司，可能使用不同的业务组件，因此上述的调用方法也可能有些差别。


