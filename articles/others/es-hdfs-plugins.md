# 详述 Elasticsearch 安装 HDFS 插件存储及快照还原的方法

Elasticsearch 支持多种存储库的配置，如 S3、Azure、Google Cloud Storage 和 HDFS 等，具体可参阅「[Snapshot And Restore](https://www.elastic.co/guide/en/elasticsearch/reference/current/modules-snapshots.html)」。在此，我们仅详述如何配置 HDFS 存储库以及利用 HDFS 进行快照和还原的方法。

# 前提条件

既然我们想利用 HDFS 存储 Elasticsearch 的快照，那么前提肯定得有一个 HDFS 集群供我们使用。至于如何搭建 HDFS 集群，具体可参阅：

- [快速搭建 HDFS 系统（超详细版）](https://guobinhit.blog.csdn.net/article/details/88576800)

如果在搭建 HDFS 集群的过程中遇到了数据节点启动异常的情况，可以参阅：

- [HDFS 集群无法启动 DataNode 节点以及管理界面缺少 DataNode 节点的解决方法](https://guobinhit.blog.csdn.net/article/details/88657826)

如果在进行 Elasticsearch 快照的时候遇到了`PrivateCredentialPermission`权限问题，可以参阅：

- [Elasticsearch 快照到 HDFS 遇到的 PrivateCredentialPermission 问题及解决方法](https://guobinhit.blog.csdn.net/article/details/89212486)


# 安装 HDFS 插件

想要使用 HDFS 存储 Elasticsearch 的索引快照，我们需要把 Elasticsearch 集群中的“**所有节点**”都安装上 HDFS 插件。安装 HDFS 插件的方式有两种，一种是直接安装，另一种是下载 HDFS 插件后，离线安装。

- **第一种安装方式**：适用于网络情况良好并且不限制网络访问，一般在非生产环境使用，我们只需要在 Elasticsearch 节点的根目录下执行如下命令即可：
  - `sudo bin/elasticsearch-plugin install repository-hdfs`
- **第二种安装方式**：适用于网络情况不会或者限制网络访问，一般在生产环境使用，我们需要先把 HDFS 插件下载到本地环境，再上传到服务器，然后执行如下命令即可：
  - Unix 环境：`sudo bin/elasticsearch-plugin install file:///path/to/plugin.zip`
  - Windows 环境：`bin\elasticsearch-plugin install file:///C:/path/to/plugin.zip`

如果想要卸载插件，只需要将上述命令中的`install`替换为`remove`即可。

![install-hdfs-plugin](https://github.com/guobinhit/cg-blog/blob/master/images/others/es-hdfs-plugins/install-hdfs-plugin.png)

如上图所示，当出现`Continue with installation? [y/N]`的时候，按`y`键即可。当 HDFS 插件安装成功后，在 Elasticsearch 安装目录下的`plugins`目录下，新增一个名为`repository-hdfs`的目录，该目录包含了一些 HDFS 插件运行所需的 jar 包以及配置文件。当所有节点都安装完 HDFS 插件之后，重启所有节点，以使插件生效。当所有节点重启完毕之后，执行如下命令：

- `curl -i -X GET localhost:9200/_cat/nodes?v`


![cat-nodes](https://github.com/guobinhit/cg-blog/blob/master/images/others/es-hdfs-plugins/cat-nodes.png)

在观察到所有节点都加入集群之后，我们就可以执行索引的快照及还原操作了。


## 快照

在进行快照之前，我们需要先创建 HDFS 的存储库。

- 创建仓库

```
curl -X PUT localhost:9200: _snapshot/my_hdfs_repository?pretty -H 'Content-Type: application/json' -d'
{
  "type": "hdfs",
  "settings": {
    "uri": "hdfs://namenode:9000/",
    "path": "elasticsearch/repositories/my_hdfs_repository",
    "conf.dfs.client.read.shortcircuit": "true"
  }
}
'
```

其中，`my_hdfs_repository`为我们自定义的 HDFS 存储库的名称，`hdfs://namenode:9000/`为访问 HDFS 集群的地址。如果上述命令执行失败，可以尝试将`conf.dfs.client.read.shortcircuit`的值设置为`false`；如果上述命令执行成功，则会返回

```
{
	acknowledge: ture
}
```

除此之外，在创建 HDFS 存储库的时候，还可以指定其他参数，如快照的速度等，具体可参考「[Configuration Properties](https://www.elastic.co/guide/en/elasticsearch/plugins/7.0/repository-hdfs-config.html)」文档。

- 查询仓库

```
curl -X GET localhost:9200/_snapshot?pretty
```

- 注销仓库

```
curl -X DELETE localhost:9200/_snapshot/仓库名称?pretty
```

上面给出了关于仓库的相关命令，下面给出关于快照的相关命令。

- 创建快照

```
curl -X PUT localhost:9200/_snapshot/仓库名称/快照名称?wait_for_completion=true&pretty" -H 'Content-Type: application/json' -d'
{
  "indices": "索引名称",
  "ignore_unavailable": true,
  "include_global_state": false
}
'
```

其中，`wait_for_completion=true`参数表示阻塞该操作，直到该操作执行完成之后在返回。

* 查询快照

```
curl -X GET localhost:9200/_snapshot/仓库名称/快照名称?pretty
```

* 查询快照状态

```
curl -X GET localhost:9200/_snapshot/快照名称/_status?pretty
```

* 删除快照

```
curl -X DELETE localhost:9200/_snapshot/仓库名称/快照名称?pretty
```

## 还原
索引快照的还原，可以分为两种情况，分别是：

- 在同一个集群还原快照；
- 在不同的集群还原快照。

无论是否跨集群还原快照，其操作命令都是一样，即

```
curl -X POST localhost:9200/_snapshot/仓库名称/快照名称/_restore?wait_for_completion=true
```

由于快照包含构成索引的磁盘上数据结构的副本，因此快照只能还原到可以读取索引的 Elasticsearch 版本：

- 在`6.x`中创建的索引快照可以还原到`7.0.0`
- 在`5.x`中创建的索引快照可以还原到`6.x`
- 在`2.x`中创建的索引快照可以还原到`5.x`
- 在`1.x`中创建的索引快照可以还原到`2.x`

而`5.x`及之前版本的索引快照不能还原到`7.0.0`版本，具体可以参阅「[Snapshot And Restore](https://www.elastic.co/guide/en/elasticsearch/reference/current/modules-snapshots.html)」，其给出了最新版本的兼容性说明。要注意，每个快照可以包含在不同版本的 Elasticsearch 中创建的索引，并且在还原快照时，必须确定能够将所有索引还原到目标集群中；否则的话，如果快照中的任何索引是在不兼容的版本中创建的，则无法还原快照。

除此之外，**在跨集群还原索引快照的时候，我们需要在目标集群中创建与原始集群具体相同名称的存储库**。例如，

```
curl -X PUT localhost1:9200: _snapshot/my_hdfs_repository?pretty -H 'Content-Type: application/json' -d'
{
  "type": "hdfs",
  "settings": {
    "uri": "hdfs://namenode:9000/",
    "path": "elasticsearch/repositories/my_hdfs_repository",
    "conf.dfs.client.read.shortcircuit": "true"
  }
}
'
```

如上述命令所示，假设我们在原始集群中创建了名为`my_hdfs_repository`的存储库，并且想将其存储的索引快照还原到目标集群，则需要在目标集群中创建如下存储库：

```
curl -X PUT localhost2:9200: _snapshot/my_hdfs_repository?pretty -H 'Content-Type: application/json' -d'
{
  "type": "hdfs",
  "settings": {
    "uri": "hdfs://namenode:9000/",
    "path": "elasticsearch/repositories/my_hdfs_repository",
    "conf.dfs.client.read.shortcircuit": "true"
  }
}
'
```

如果我们仔细观察上面的两条命令，我们会发现，两者的区别仅在于`localhost1`和`localhost2`的不同。但是有一点需要我们特别注意，那就是：**两个集群的节点配置一定要相同，如果原始集群在节点中使用`node.attr.rack`划分了`hot`和`warm`属性，那么在目标集群也需要划分对应的属性，否则会还原失败**。

