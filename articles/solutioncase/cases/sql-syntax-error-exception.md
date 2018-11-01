# 出现 com.ibm.db2.jcc.am.SqlSyntaxErrorException: DB2 SQL Error 异常的原因及解决方法


1 异常描述
------

在测试向数据库中添加记录的方法的时候，报出如下错误：

![1](http://img.blog.csdn.net/20170410190406694)



2 异常原因
------
观察上图划线的部分：

```
Error updating database.Cause: 
com.ibm.db2.jcc.am.SqlSyntaxErrorException: 
DB2 SQL Error: SQLCODE=-104, SQLSTATE=42601,SQLERRMC=CURRENT_TIMESTAMP;?,
```

观察上图中被标记出的来异常描述，咱们可以知道：

> com.ibm.db2.jcc.am.SqlSyntaxErrorException: 
DB2 SQL Error: SQLCODE=-104, SQLSTATE=42601

此异常，为：**数据库异常**。

而且在异常描述的时候，给出了`SQLCODE=-104`和`SQLSTATE=42601`.


3 解决方法
------

通过查询 [史上最全的 DB2 错误代码大全](http://blog.csdn.net/qq_35246620/article/details/56877433) ：

![2](http://img.blog.csdn.net/20170410191137792)

显然可以看出来，对应上述`SQLCODE`和`SQLSTATE`的错误原因为“ **SQL 语句中遇到了非法符号**”。对于这个异常，博主查看了对应的 SQL 语句，好吧，原因仅仅是少写了一个逗号！或许，这篇博文的题目修改为“论·细心的重要性”更切题一些，囧。
