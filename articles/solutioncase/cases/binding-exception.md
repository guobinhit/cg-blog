# 出现 org.apache.ibatis.binding.BindingException 异常的原因及解决方法

1 异常描述
------
在对数据库表中现有敏感信息（例如姓名、手机号、身份证号、银行卡号等）进行加密处理的时候，报出如下错误：

![binding](http://img.blog.csdn.net/20170909183830923)


2 异常分析
------
通过观察上图中被标记出来的异常信息，我们可以知道

> org.apache.ibatis.binding.BindingException: Invalid bound statement（not found）: com.XXX.router.dao.RouterProviderEntityDao.query4encrypt

此异常，为：**ibatis 无效绑定异常。**

我们知道 MyBatis 源自于 ibatis，在流行的 SSM 框架中，Mybatis 常被用于持久化层，说白了，就是保存数据，负责将数据持久化（插入）到数据库。在常见的 Web 项目中，我们又将其划分为 Dao 层、Service 层、Biz 层和 Facade 层，其中 Dao 层就是负责和数据库进行交互的，而交互的方式就是用 Mapper 文件进行数据库表的映射，并到 Dao 层建立与 Mapper 文件中 SQL 语句对应的函数。

现在回过头看这个异常，它发生在`router.dao.RouterProviderEntityDao.query4encrypt`这个地方，其中`RouterProviderEntityDao`表示 Dao 层，`query4encrypt`是 Dao 层中与 Mapper 文件对应的方法名。而且，这个异常为`BindingException`，绑定异常，那么很有可能就是 Mapper 文件中 SQL 的 id 名与 Dao 层中的函数名不一致。


3 解决方法
------

在异常分析之后，查看 Mapper 文件中 SQL 的 id 名与 Dao 层中的函数名是否一致，经过检查，发现两者确实不一致，其中 SQL 的 id 名为`query4Encrypt`而 Dao 层中的函数名为`query4encrypt`，既然我们已经知道了发生异常的原因，那么统一两者之后，即可解决此异常。


----------

**温馨提示**：此案例为个性，异常是共性。
