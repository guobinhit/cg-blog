# HDFS 集群无法启动 DataNode 节点以及管理界面缺少 DataNode 节点的解决方法

# 前言

搭建了一个 HDFS 集群，用了 3 台虚拟机，1 台虚拟机是`master`作为`NameNode`节点；2 台虚拟机分别是`slave1`和`slave2`作为`DataNode`节点，具体的集群搭建过程可参考「[快速搭建 HDFS 系统（超详细版）](https://blog.csdn.net/qq_35246620/article/details/88576800)」这篇博文。


## 1 问题描述

在搭建 HDFS 集群的过程中，难免会遇到一些稀奇古怪的问题，就如我遇到的这个问题一样：

- `ISSUE 1`，HDFS 集群搭建并启动成功，1 个`NameNode`节点和 2 个`DataNode`节点也运行正常， 可以在各自的虚拟机中用`jps`命令查看正在运行的 Java 进程，但是通过`http://master:50070/dfshealth.html#tab-datanode`查看数据节点，却发现可视化管理界面仅显示了一个`DataNode`节点，另一个数据节点缺失。

在尝试解决这个问题的时候，又遇到了另一个问题，即

- `ISSUE 2`，在 HDFS 集群关闭后，使用`hdfs namenode -format`命令刷新`NameNode`节点格式，重新启动集群，发现仅能成功启动`NameNode`节点，而两个`DataNode`节点启动失败。

接下来，我们就尝试解决上面的两个问题。

## 2 尝试解决

虽然我们是先遇到`ISSUE 1`，后遇到`ISSUE 2`的，但想要继续调试集群，我们显然要先解决`ISSUE 2`，让集群正常跑起来；否则的话，集群连一个数据节点都连接不上，何谈界面显示的问题啊？因此，我们首先来看看`ISSUE 2`该如何解决。

### 2.1 解决 ISSUE 2

在启动集群的时候，我们可以通过集群日志来查看错误信息，默认的日志位置在 Hadoop 的安装目录的`logs`的目录下，例如：

![hadoop-logs](https://github.com/guobinhit/cg-blog/blob/master/images/solutioncase/part-3-others/hdfs-datanode-loss/hadoop-logs.png)

如上图所示，在我们进入到`logs`目录之后，可以使用`tail`命令来还查看日志，例如：

- `tail -1000f hadoop-root-namenode-localhost.localdomain.log`

同理，我们也可以到各个`DataNode`对应的`logs`目录查看数据节点的日志信息，其会记录集群在启动和运行过程中的日志信息，如果出现异常或者错误，查看对应的日志文件是一个很好的定位问题的方法。

而之所以会出现`ISSUE 2`这样的问题，其根本原因在于我们使用`hdfs namenode -format`命令刷新`NameNode`节点的格式后，会重新生成集群的相关信息，特别是`clusterID`，每次刷新都会生成一个新的`clusterID`；但是当我们在`NameNode`节点所在的虚拟机刷新格式后，并不会影响`DataNode`节点，也就是说，那 2 台配置`DataNode`节点的虚拟机上关于集群的信息并不会刷新，仍保留上一次（未刷新`NameNode`格式前）的集群信息，这就导致了`NameNode`节点和`DataNode`节点的`clusterID`不一致的情况，因此`DataNode`节点不能连接到`NameNode`节点。我们可以到 Hadoop 安装目录下的`/etc/hadoop`目录下：

![etc-hadoop-hdfs-site](https://github.com/guobinhit/cg-blog/blob/master/images/solutioncase/part-3-others/hdfs-datanode-loss/etc-hadoop-hdfs-site.png)

查看`hdfs-site.xml`文件来获取 Hadoop 数据存储的位置，当然，这个位置也是我们之前在配置集群时设置的：

![hadoop-data-dir](https://github.com/guobinhit/cg-blog/blob/master/images/solutioncase/part-3-others/hdfs-datanode-loss/hadoop-data-dir.png)

其中，`hadoopData`目录为我事先创建的存储 Hadoop 数据的目录，而`/dfs/name`和`/dfs/data`目录则会通过配置在集群启动时自动生成。

![cat-version](https://github.com/guobinhit/cg-blog/blob/master/images/solutioncase/part-3-others/hdfs-datanode-loss/cat-version.png)

如上图所示，在`hadoopData/dfs/name/current`目录下，有一个名为`VERSION`的文件，该文件就包含了 HDFS 集群的信息，我们可以使用`cat VERSION`命令来查看`VERSION`文件的内容。同样，在`DataNode`节点也会自动生成该文件！因此，在出现`ISSUE 2`问题的时候，如果我们分别查看`NameNode`节点和`DataNode`节点的`VERSION`文件的话，我们将会发现两者的`clusterID`不一样。

- **解决`ISSUE 2`的方法**：停止 HDFS 集群后，同时删除`NameNode`节点和`DataNode`节点中配置的存储 Hadoop 数据的文件目录的所有子目录及文件，如我们配置的`hadoopData`目录下的所有子目录及文件。接下来，再使用`hdfs namenode -format`命令重新格式化`NameNode`节点，然后重新启动 HDFS 集群，即可！

在把`ISSUE 2`的问题解决之后，我们再看看`ISSUE 1`该如何解决？

### 2.1 解决 ISSUE 1
为了解决`ISSUE 1`，我也在网上搜了很多文章，这些文章给出的解决方法可以归纳为两个，分别为：

- **方法 1**：修改各个节点的`hdfs-site.xml`配置文件中配置的`dfs.datanode.data.dir`目录，保持在`NameNode`节点和`DataNode`节点中，该配置的数据存储路径各不相同。例如

```xml
<!--- NameNode master 节点--->
<property>
   <name>dfs.datanode.data.dir</name>
   <value>/home/hdfs-cg/hadoopData/dfs/data</value>
</property>

<!--- DataNode slave1 节点--->
<property>
   <name>dfs.datanode.data.dir</name>
   <value>/home/hdfs-cg/hadoopData/dfs/slave1data</value>
</property>

<!--- DataNode slave2 节点--->
<property>
   <name>dfs.datanode.data.dir</name>
   <value>/home/hdfs-cg/hadoopData/dfs/slave2data</value>
</property>
```


- **方法 2**：在各个节点的`hdfs-site.xml`配置文件中配置`dfs.namenode.datanode.registration.ip-hostname-check`属性，并将其值设置为`false`，例如

```xml
<property>
   <name>dfs.namenode.datanode.registration.ip-hostname-check</name>
   <value>false</value>
</property>
```

说实话，无论是 **方法 1** 还是 **方法 2**，我都尝试了，但都没有成功。不过看大家的反馈，既有说通过 **方法 1** 解决问题的，也有说通过 **方法 2** 解决问题的！具体效果如何，大家可以自行尝试。特别的，对于 **方法 2**，我特意查了查该属性的含义，该属性默认为`ture`，表示`NameNode`节点连接`DataNode`时会进行`host`解析查询，感觉这个方法还是比较靠谱的。实际上，对于我遇到的问题，**方法 2** 理论上应该是可以解决问题的，但实际上并没有解决，可能是某些配置冲突了，或者是有优先级的问题。

不过在观察`Datanode usage histogram`的时候，我发现了一个问题，那就是唯一显示出来的`DataNode`节点的主机名显示为`localhost`，而且随着我多次重启 HDFS 集群，虽然主机名不变，都为`localhost`，但是主机名后面跟着的`IP`会变化，在两个启动的数据节点中不断切换，因此我怀疑这可能和我配置的`hosts`文件有关。

![datanode-usage-histogram](https://github.com/guobinhit/cg-blog/blob/master/images/solutioncase/part-3-others/hdfs-datanode-loss/datanode-usage-histogram.png)

呃，不要纠结于上面的图为啥会显示两个数据节点，因为这是我把上述的问题解决之后的效果，懒得恢复原先的配置重新截图了，大家凑合看吧，捂脸！既然怀疑`hosts`文件有问题，那我们就看看我的`hosts`文件到底都配置啥了：

```
127.0.0.1   localhost localhost.localdomain localhost4 localhost4.localdomain4
::1         localhost localhost.localdomain localhost6 localhost6.localdomain6

10.123.456.38 master
10.123.456.39 slave1
10.123.456.40 slave2
```

如上述所示，我配置的 3 台虚拟机的`hosts`文件的内容均是如此，而在我把前两个映射注释掉之后，也就是修改为：

```
#127.0.0.1   localhost localhost.localdomain localhost4 localhost4.localdomain4
#::1         localhost localhost.localdomain localhost6 localhost6.localdomain6

10.123.456.38 master
10.123.456.39 slave1
10.123.456.40 slave2
```

这时，再次验证集群的启动效果，我们就会发现`ISSUE 1`也随之解决啦！因此，我们也知道了`ISSUE 1`的解决方法。

- **解决`ISSUE 1`的方法**：修改虚拟机的`hosts`文件，保证每个节点的主机名都各不相同。

## 3 总结

在集群环境中，节点加入集群的一个条件就是节点与集群拥有统一的标识，如 HDFS 集群的`clusterID`，也如 es 集群的`cluster.name`，因此对于`ISSUE 2`这样由于`clusterID`不一致而导致数据节点不能加入集群的问题，也就再正常不过了。因此，要么在格式化之后，保持各节点的标识信息一致，要么就不要格式化。

通过解决`ISSUE 1`，我们知道了在 HDFS 集群的图形化管理界面的`Datanode usage histogram`中，显示的数据节点是根据主机名进行区分的，如果数据节点的主机名都相同，就是导致虽然数据节点正常启动，但却不会在管理界面中显示的问题。至于我们如何判断数据节点是否正常启动，可以使用如下命令：

- `hdfs dfsadmin -report`

该命令执行的效果为：

![hadoop-dfsadmin-report](https://github.com/guobinhit/cg-blog/blob/master/images/solutioncase/part-3-others/hdfs-datanode-loss/hadoop-dfsadmin-report.png)

最后，在实际操作中，遇到问题是难免的，也是正常的，虽然看到问题还是会让我们感到闹心，但当问题解决的一刹那，我们得到幸福感和愉悦感是难以形容的，而且每解决一个问题，都会让我们的自信心得到提升，加油吧，少年！


----------
———— ☆☆☆ —— [返回 -> 超实用的「Exception」和「Error」解决案例 <- 目录](https://github.com/guobinhit/cg-blog/blob/master/articles/solutioncase/README.md) —— ☆☆☆ ————
