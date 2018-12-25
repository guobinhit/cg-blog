# 出现 DB2 SQL Error: SQLCODE = -1773, SQLSTATE = null 错误的原因及解决方法

### 1 错误描述

在项目从虚拟机迁移到容器云之后，生产环境在执行某个数据库下所有表的新增及更新操作的时候，都会遇到 BD2 报出来的`SQLCODE = -1773, SQLSTATE = null`异常，从而导致该库下所有涉及到新增和更新的操作全部失败，具体的错误日志如下：

![1773](https://github.com/guobinhit/cg-blog/blob/master/images/solutioncase/sqlcode-1773/1773.png)

### 2 错误原因

通过观察上述标记出来的错误描述：

> SQLCODE = -1773, SQLSTATE = null

然后，查询「[史上最全的 DB2 错误代码大全](http://blog.csdn.net/qq_35246620/article/details/56877433)」可知，此错误的原因为：

> -1773 在 HADR 数据库下不支持的操作（如备库可能不支持写操作）

如果直接查询 DB2 的`SQLCODE`，其解释为：

- `The statement or command requires functionality that is not supported on a read-enabled HADR standby database.`

其中，HADR 为 DB2 高可用性灾难恢复（`High Availability Disaster Recovery`）功能的简称，其特性就是：支持在备机上的只读操作。换言之，通过配置，可以实现在备库上“只能读、不能写”的限制！而在检查数据库的配置之后，发现确实是将数据库配置误配为备库的地址了。

###  3 解决方法

既然找了问题的原因，那么解决该问题的方式自然是切换数据库的配置，在数据库的配置切换为主库之后，该错误解决。不过，在后续查看日志的时候，发现 DB2 还报了另外一个错误，即：

![4499](https://github.com/guobinhit/cg-blog/blob/master/images/solutioncase/sqlcode-1773/4499.png)

> ERRORCODE = -4499，SALSTATE = 08001

经过查询及定位问题之后，发现当时仅使用了备库，而主库会在某个时间点进行`runstatus`操作，从而导致在该时间点后的小段时间内，备库频繁被杀，从而导致连接失败。实际上，问题的根源，还在于操作的大意，如果在操作的时候更细心一些，相信这个问题是可以避免的，希望大家引以为戒！案例仅供参考。
