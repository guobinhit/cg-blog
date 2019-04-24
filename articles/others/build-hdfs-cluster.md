# 快速搭建 HDFS 系统（超详细版）

# 节点介绍

首先，准备 5 台虚拟机，其中 1 台虚拟机作为`NameNode`，4 台虚拟机作为`DataNode`，分别为：

| IP  | Hosts（主机名）      |
|:--------:| :-------------:|
| `192.168.56.101` | `master` |
| `192.168.56.102` | `slave1` |
| `192.168.56.103` | `slave2` |
| `192.168.56.104` | `slave3` |
| `192.168.56.105` | `slave4` |

在这里，`master`充当着`NameNode`的角色，其他的`salve`充当着`DataNode`的角色，并且需要修改这 5 台虚拟机上的`hosts`文件，配置它们的主机名，以便它们可以通过主机名进行互相的访问。

- **执行命令**：`vim /etc/hosts`

![config-hosts](https://github.com/guobinhit/cg-blog/blob/master/images/others/build-hdfs-cluster/config-hosts.png)

配置完成后，使用`vim`编辑器的`:wq`保存退出。

## 开始搭建 HDFS 系统
在前面，我们已经准备好了虚拟机；在此，我们还需要准备两个资源，分别为 Hadoop 和 JDK 安装包，可通过以下链接到官方获取：

- JDK：[ Java SE Development Kit 8u201](https://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)
- Hadoop：[Apache Hadoop Download](https://hadoop.apache.org/releases.html)

当上述两个安装包下载完成之后，可通过 Linux 命令，将两个安装包上传到虚拟机，例如

- `scp -r /Users/bin.guo/Downloads/hadoop-2.7.7.tar.gz root@192.168.56.101:/home/hdfs-cg`
- `scp -r /Users/bin.guo/Downloads/jdk-8u201-linux-x64.tar.gz root@192.168.56.101:/home/hdfs-cg`

### 基础环境变量配置

#### 第 1 步：解压 Hadoop 安装包

![tar-zxvf-hadoop](https://github.com/guobinhit/cg-blog/blob/master/images/others/build-hdfs-cluster/tar-zxvf-hadoop.png)

如上述，使用 Linux 命令`tar -zxvf 待解压文件`解压 Hadoop 安装包。

#### 第 2 步：配置 Hadoop 的 Java 运行环境

在当前目录解压完成后，进入`/hadoop-2.7.3/etc/hadoop`目录，这个目录里存放的都是 Hadoop 配置文件，当然，我们需要修改的配置文件也在这个目录中。接下来，编辑`hadoop-env.sh`文件，配置 Java 环境变量。

- **执行命令**：`vim hadoop-env.sh`

![config-java-env](https://github.com/guobinhit/cg-blog/blob/master/images/others/build-hdfs-cluster/config-java-env.png)

#### 第 3 步：在 Linux 中配置 Hadoop 环境变量

编辑`/etc/profile`文件，配置 Hadoop 环境变量。

- **执行命令**：`vim /etc/profile`

![config-hadoop-env](https://github.com/guobinhit/cg-blog/blob/master/images/others/build-hdfs-cluster/config-hadoop-env.png)

如上图所示，通过在`profile`文件中追加`export PATH=$PATH:/usr/local/hadoop-2.7.3/bin:/usr/local/hadoop-2.7.3/sbin`语句，即可配置 Hadoop 环境变量。在这里，如果我们之前没有在`profile`中配置过`PATH`环境变量，则需要先配置`PATH`的环境变量，例如：

```
JAVA_HOME=/home/hdfs-cg/jdk1.8.0_131
PATH=$JAVA_HOME/bin:$PATH
CLASSPATH=.:$JAVA_HOME/lib/dt.jar:$JAVA_HOME/lib/tools.jar

export PATH
export JAVA_HOME
export CLASSPTH

export PATH=$PATH:/home/hdfs-cg/hadoop-2.7.3/bin:/home/hdfs-cg/hadoop-2.7.3/sbin
```

配置完 Hadoop 的环境变量之后，保存文件，并输入以下命令让`profile`文件立即生效。

- **执行命令**：`source /etc/profile`

正常情况下，输入`source /etc/profile`命令不会有任何提示；我们可以输入命令`hadoop`进行验证，如果出现以下内容，则说明 Hadoop 环境配置成功了。

![test-hadoop-source](https://github.com/guobinhit/cg-blog/blob/master/images/others/build-hdfs-cluster/test-hadoop-source.png)

### 设置 SSH 免密码登录

由于`master`机器，也就是`192.168.56.101`这台机器，其将成为我们 Hadoop 集群的`NameNode`节点，因此我们配置其可以免密登录集群中其它的`slave`机器。

- **执行命令**：`ssh-keygen -t rsa`

![ssh-keygen-t-rsa](https://github.com/guobinhit/cg-blog/blob/master/images/others/build-hdfs-cluster/ssh-keygen-t-rsa.png)

执行命令后，出现提示可以不予理会，直接按几次回车键就可以了。当出现以下界面时，则说明生成私钥`id_rsa`和公钥`id_rsa.pub`成功：

![create-rsa-success](https://github.com/guobinhit/cg-blog/blob/master/images/others/build-hdfs-cluster/create-rsa-success.png)

接下来，我们把生成的公钥`id`发送到`slave1`、`slave2`、`slave3`和`slave4`这 4 台机器。

- **执行命令**：` ssh-copy-id slave1`

`slave1`会要求你输入`slave1`这台机器上的密码：

![slave1-nopassword](https://github.com/guobinhit/cg-blog/blob/master/images/others/build-hdfs-cluster/slave1-nopassword.png)

密码输入正确后，你将会看到以下界面，它说已经添加了密钥，叫你尝试登陆：

![slave1-nopassword2](https://github.com/guobinhit/cg-blog/blob/master/images/others/build-hdfs-cluster/slave1-nopassword2.png)

现在，我们输入 SSH 命令测试 `slave1` 的免密登陆。

- **执行命令**：`ssh slave1`

![login-slave1-nopassword-success](https://github.com/guobinhit/cg-blog/blob/master/images/others/build-hdfs-cluster/login-slave1-nopassword-success.png)

如上图所示，显然我们已经实现了`master`到`slava1`的免密登录，其它`slave`机器同理。

### 配置 HDFS
在所有机器上的`/hadoop-2.7.3/etc/hadoop`目录中，修改`core-site.xml`和`hdfs-site.xml`文件，以完成 HDFS 的配置。

- 修改`core-site.xml`，在`configuration`标签内加入以下配置：

```xml
<configuration>
<property>
  <name>fs.defaultFS</name>
  <value>hdfs://master:9000</value>
  <description>HDFS 的 URI，文件系统://namenode标识:端口</description>
</property>

<property>
  <name>hadoop.tmp.dir</name>
  <value>/home/hadoopData</value>
  <description>namenode 上传到 hadoop 的临时文件夹</description>
</property>

<property>
    <name>fs.trash.interval</name>
    <value>4320</value>
</property>
</configuration>
```

- 修改`hdfs-site.xml`，在`configuration`标签内加入以下配置：

```xml
<configuration>
<property>
   <name>dfs.namenode.name.dir</name>
   <value>/home/hadoopData/dfs/name</value>
   <description>datanode 上存储 hdfs 名字空间元数据</description>
 </property>
 
 <property>
   <name>dfs.datanode.data.dir</name>
   <value>/home/hadoopData/dfs/data</value>
   <description>datanode 上数据块的物理存储位置</description>
 </property>
 
 <property>
   <name>dfs.replication</name>
   <value>3</value>
   <description>副本个数，默认配置是 3，应小于 datanode 机器数量</description>
 </property>
 
 <property>
   <name>dfs.webhdfs.enabled</name>
   <value>true</value>
 </property>
 
 <property>
   <name>dfs.permissions.superusergroup</name>
   <value>staff</value>
 </property>
 
 <property>
   <name>dfs.permissions.enabled</name>
   <value>false</value>
 </property>
</configuration>
```

在这里，我们需要创建 Hadoop 存放数据的文件夹，为了与配置文件中的路径匹配，我们将在`home`目录下，创建名为`hadoopData`的文件夹。

- **执行命令**：`mkdir /home/hadoopData`

当然，我们可以调整此文件夹的位置，只要保证其与配置文件的路径匹配即可。

### 配置 NameNode 节点
因为`master`机器是集群中的`NameNode`节点，因此我们在`master`机器上进行操作，也就是`192.168.56.101`这台主机。在`master`机器的`/hadoop-2.7.3/etc/hadoop`目录下，修改`slaves`文件，加入`DataNode`节点。**特别注意，由于我们之前修改了`hosts`文件，各虚拟机的 IP 已经与主机名绑定，因此在这里，我们之前配置主机名即可**。

- **执行命令**：`vim slaves`

![hadoop-slave-datanode](https://github.com/guobinhit/cg-blog/blob/master/images/others/build-hdfs-cluster/hadoop-slave-datanode.png)

其中，`slave1`、`slave2`、`slave3`和`slave4`都是`DataNode`节点，我们把它们加入到`NameNode`节点中，这样我们就可以用一个命令启动整个集群。

### 格式化 NameNode 以及启动 HDFS 系统
在`master`这台机器上，输入命令 HDFS 格式化命令。

- **执行命令**：`hdfs namenode -format`

![hdfs-format](https://github.com/guobinhit/cg-blog/blob/master/images/others/build-hdfs-cluster/hdfs-format.png)

格式化完成之后，输入 HDFS 系统启动命令。

- **执行命令**：`start-dfs.sh`

![start-hdfs](https://github.com/guobinhit/cg-blog/blob/master/images/others/build-hdfs-cluster/start-hdfs.png)

接下来，检查 HDFS 是否启动成功。在游览器中输入`http://192.168.56.101:50070/`，默认为`NameNode`的`IP + 50070`端口，当你见到以下界面的时候，就说明你的集群已经起来了。

![hdfs-gui](https://github.com/guobinhit/cg-blog/blob/master/images/others/build-hdfs-cluster/hdfs-gui.png)

最后，再检查一下`DataNode`节点的启动情况：

![hdfs-datanode-gui](https://github.com/guobinhit/cg-blog/blob/master/images/others/build-hdfs-cluster/hdfs-datanode-gui.png)

如上图所示，我们配置的 4 个`DataNode`也起来了，这说明整个 HDFS 集群搭建完成啦！




------------

**转载声明**：本文转自简书「[陈_志鹏](https://www.jianshu.com/u/9644551dad34)」的「[HDFS系统的搭建(整理详细版)](https://www.jianshu.com/p/58b39974abb7)」这篇文章。

**温馨提示**：本文与原文相比有些许改动，但整体流程未变，图片也是直接引用，在此对原作者的分享表示感谢！
