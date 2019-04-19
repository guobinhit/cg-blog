# Elasticsearch 快照到 HDFS 遇到的 PrivateCredentialPermission 问题及解决方法

## 问题背景

在 Elasticsearch 集群中配置了 HDFS 插件，用于存储集群的索引快照。

## 问题描述

在 HDFS 存储库创建成功之后，尝试创建索引快照的时候，遇到了如下问题：

![private-credential-permission](https://github.com/guobinhit/cg-blog/blob/master/images/solutioncase/part-3-others/es-hdfs-permission/private-credential-permission.png)

如上图所示，在执行创建快照的命令时，抛出了`repository_exception`异常，但也给出了一串原因：

```java
"reason 4" : "[test541to660] could not read repository data from index blob"

"reason 3" : "com.google.protobuf.ServiceException: java.security.AccessControlException: access denied (\"javax.security.auth.PrivateCredentialPermission\" \"org.apache.hadoop.security.Credentials\" \"read\")"

"reason 2" : "java.security.AccessControlException: access denied (\"javax.security.auth.PrivateCredentialPermission\" \"org.apache.hadoop.security.Credentials\" \"read\")"

"reason 1" : "access denied (\"javax.security.auth.PrivateCredentialPermission\" \"org.apache.hadoop.security.Credentials\" \"read\")"
```

从下向上看，显然，最根本的原因在于`javax.security.auth.PrivateCredentialPermission`，没有`read`权限。

## 解决方法

实际上，每个 Java 应用在启动的时候，都会加载一个安全管理器，其指定了一些安全策略，在没有指定安全管理器的情况下，会默认加载`$JAVA_HOME/jre/lib/security`目录下的`java.policy`文件。

![java-jre-lib-security](https://github.com/guobinhit/cg-blog/blob/master/images/solutioncase/part-3-others/es-hdfs-permission/java-jre-lib-security.png)

为了解决上述问题，我们需要做的就是在`java.policy`文件中，新增一项配置：

- `permission java.security.AllPermission;`

具体如下所示，

![java-policy](https://github.com/guobinhit/cg-blog/blob/master/images/solutioncase/part-3-others/es-hdfs-permission/java-policy.png)

在添加完该项配置之后，重新启动 Elasticsearch 集群，再次创建快照，即可成功。

**特别地，以下为博主的实践验证，供大家参考**：

- 仅修改了 HDFS 集群中所有节点所在服务器的配置，重启 HDFS 集群，未生效；
- 进而，重启 Elasticsearch 集群，未生效；
- 进而，修改 Elasticsearch 集群中所有`master`节点所在服务器的配置，重启`master`节点，仍未生效；
- 最后，修改 Elasticsearch 集群中所有节点所在服务器的配置，重启 Elasticsearch 集群，问题解决。

因此，博主算是把 Elasticsearch 集群和 HDFS 集群中所有节点所在服务器的配置都修改了一遍，这才解决了上面的权限问题。




----------
———— ☆☆☆ —— [返回 -> 超实用的「Exception」和「Error」解决案例 <- 目录](https://github.com/guobinhit/cg-blog/blob/master/articles/solutioncase/README.md) —— ☆☆☆ ————