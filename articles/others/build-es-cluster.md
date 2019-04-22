# 详述 Elasticsearch 集群的搭建步骤及方法

# 前言

说实话，关于搭建 Elasticsearch 集群的步骤及方法，网上有很多文章，内容也都大同小异，呃，本文也差不多，因此并没有指望本文能够给大家带来什么特别新鲜的东西，仅是为了记录自己搭建 Elasticsearch 集群的过程而已！如果在此基础之上，能够帮忙大家顺利搭建属于自己的 Elasticsearch 集群的话，那就再好不过了。

# 搭建 Elasticsearch 集群
无论是在同一个服务器上搭建多个节点，还是在多个不同的服务器上搭建多个节点，其操作流程都是一样，唯一需要改变的，也就是某些特定的配置了。因此，我们以一台服务器为例，搭建一个`master`节点、一个`client`节点和一个`data`节点！**在此，需要我们特别注意的是，像本文这样单服务器多节点（超过 3 个节点）的情况，仅供测试使用**。

在生产环境，我们需要根据服务器的内存情况来选择节点的配置，一般来说，128GB 的内存，除去一半的内存留给 Lucene 使用，剩下的 64GB 内存，我们可以配置两个节点，为每个节点分配 32GB 内存，这也是官方推荐的配置方式。除此之外，根据磁盘空间的大小，我们可以大致确定集群中分片（`shard`）的数量，官方及大家实践后根据经验推荐的是，每个分片的大小不应超过 30GB。

## 配置环境
### 1 修改 hostname
为了方便我们识别服务器，特别是在多服务器的集群环境中，修改服务器的`hostname`是一个比较好的实践。因此，我们首先修改服务器的`hostname`，先修改`network`文件，添加以下内容：

- `sudo vim /etc/sysconfig/network`

```java
NETWORKING=yes
HOSTNAME=es666.localdomain
```

再修改`hosts`文件，添加以下内容：

- `sudo vim /etc/hosts`

```java
127.0.0.1   localhost localhost.localdomain localhost4 localhost4.localdomain4
::1         localhost localhost.localdomain localhost6 localhost6.localdomain6

127.0.0.1  es666.localdomain es666
```

然后，直接在服务器中执行如下命令：

- `hostname es666.localdomain`

命令执行成功之后，退出服务器，重新登录，这时我们就会发现服务器的`hostname`已经变为`es666`了。

### 2 配置环境变量
在 Linux 系统里，默认的文件描述符最大值是`1024`，对于测试环境也足够了，但是如果是生产环境的话，我们需要修改文件描述符的限制，也就是修改`limits.conf`文件，修改示例如下：

- `sudo vim /etc/security/limits.conf`

```java
cghit  soft	nofile 131072
cghit  hard nofile 131072
cghit  soft memlock unlimited
cghit  hard memlock unlimited
root   soft memlock unlimited
root   hard memlock unlimited
cghit  soft nproc 102400
cghit  hard nproc 102400
root   soft nproc 102400
root   hard nproc 102400
```

其中，`cghit`和`root`是服务器的用户名，我们可以根据实际用户进行替换。同样，在生产环境中，我们还需要修改一些 Linux 的内核参数，修改示例如下：

- `sudo vim /etc/sysctl.conf`

```java
net.ipv6.conf.all.disable_ipv6 = 1
vm.max_map_count=262144
```

为了使上述配置生效，我们需要执行如下命令：

- `sudo sysctl -p /etc/sysctl.conf`

还有就是，配置 Java 的环境变量，假设我们将 JDK 解压到`/opt`目录，则进行如下操作：

- `sodu vim /etc/profile`

```java
export JAVA_HOME=/opt/jdk1.8.0_73
export JRE_HOME=/opt/jdk1.8.0_73/jre
export PATH=$JAVA_HOME/bin:$PATH
export CLASSPATH=.:$JAVA_HOME/jre/lib/rt.jar:$JAVA_HOME/lib/dt.jar:$JAVA_HOME/lib/tools.jar
```

对于如何将 JDK 解压到`/opt`目录，我们有两种方法，分别是：

- **第一种**：将下载 JDK 到本地电脑，然后通过`scp`命令上传到服务器的`/opt`目录，然后再通过`tar`命令解压；
- **第二种**：通过`yum`或者`wget`命令直接从云上下载 JDK 到服务器的`/opt`目录，然后再通过`tar`命令解压。

在解压完 JDK 之后，我们再将 Elasticsearch 解压到`/usr/local`目录，方法同上。

### 3 创建数据及日志目录

对于 Elasticsearch 存储数据及日志的目录，单独配置出来是一个比较好的实践。一般情况下，我们会将数据目录配置到`/data`目录，而将日志目录配置到`/var/log`目录。因此，这就需要我们提前创建好对应的目录，并为用户授权。特别地，我们习惯于将节点的日志目录命名为如下格式：

- `node + 服务器IP第四段 + . + 节点类型`，以`10.151.11.29`为例，各类节点的命名格式如下
  - `master`节点：`node29.master`
  - `client`节点：`node29.client`
  - `data`节点：`node29.data`

如果同一个服务器配置多个相同类型的节点，则进一步划分并以阿拉伯数字区分，以`data`节点为例
    
   - `data1`节点：`node29.data.1`
   - `data2`节点：`node29.data.2`

其他节点类型，同理。在目录创建完成之后，为了防止出现访问权限问题，我们需要为用户授权，以为用户`cghit`授权`/var/data/node29.master`目录为例：

- `sudo chown -R cghit:cghit /var/log/node29.master`

## 配置节点

在这里，我们直接给出 Elasticsearch 的`/config/elasticsearch.yml`文件的配置示例。

### master 节点

```yml
# ======================== Elasticsearch Configuration =========================
#
# NOTE: Elasticsearch comes with reasonable defaults for most settings.
#       Before you set out to tweak and tune the configuration, make sure you
#       understand what are you trying to accomplish and the consequences.
#
# The primary way of configuring a node is via this file. This template lists
# the most important settings you may want to configure for a production cluster.
#
# Please consult the documentation for further information on configuration options:
# https://www.elastic.co/guide/en/elasticsearch/reference/index.html
#
# ---------------------------------- Cluster -----------------------------------
#
# Use a descriptive name for your cluster:
#
 cluster.name: cghit-es
#
# ------------------------------------ Node ------------------------------------
#
# Use a descriptive name for the node:
#
 node.name: node29.master
#
# Add custom attributes to the node:
#
# node.attr.rack: hot
#
# ----------------------------------- Paths ------------------------------------
#
# Path to directory where to store the data (separate multiple locations by comma):
#
 path.data: /data/1/es
#
# Path to log files:
#
 path.logs: /var/log/node29.master
#
# ----------------------------------- Memory -----------------------------------
#
# Lock the memory on startup:
#
 bootstrap.memory_lock: true
 bootstrap.system_call_filter: false
#
# Make sure that the heap size is set to about half the memory available
# on the system and that the owner of the process is allowed to use this
# limit.
#
# Elasticsearch performs poorly when the system is swapping the memory.
#
# ---------------------------------- Network -----------------------------------
#
# Set the bind address to a specific IP (IPv4 or IPv6):
#
 network.host: 10.123.11.29
#
# Set a custom port for HTTP:
#
#http.enabled: false
 http.port: 9200
 http.cors.enabled: true
# transport.tcp.port: 9400
#
# For more information, consult the network module documentation.
#
# --------------------------------- Discovery ----------------------------------
#
# Pass an initial list of hosts to perform discovery when new node is started:
# The default list of hosts is ["127.0.0.1", "[::1]"]
#
 discovery.zen.ping_timeout: 80s
 discovery.zen.ping.unicast.hosts: ["10.123.11.29:9300", "10.123.11.30:9300", "10.123.11.30:9300"]
#
# Prevent the "split brain" by configuring the majority of nodes (total number of master-eligible nodes / 2 + 1):
#
 discovery.zen.minimum_master_nodes: 2
#
# For more information, consult the zen discovery module documentation.
#
# ---------------------------------- Gateway -----------------------------------
#
# Block initial recovery after a full cluster restart until N nodes are started:
#
 gateway.recover_after_nodes: 3
#
# For more information, consult the gateway module documentation.
#
# ---------------------------------- Various -----------------------------------
#
# Require explicit names when deleting indices:
#
#action.destructive_requires_name: true
# ---------------------------------- Customer setting --------------------------------
 node.master: true
 node.data: false
 node.ingest: true
 xpack.security.enabled: false
 xpack.graph.enabled: false
 xpack.watcher.enabled: false
 cluster.routing.allocation.disk.watermark.low: 80%
 cluster.routing.allocation.disk.watermark.high: 90%
 cluster.routing.allocation.same_shard.host: true
```

### data 节点

```yml
# ======================== Elasticsearch Configuration =========================
#
# NOTE: Elasticsearch comes with reasonable defaults for most settings.
#       Before you set out to tweak and tune the configuration, make sure you
#       understand what are you trying to accomplish and the consequences.
#
# The primary way of configuring a node is via this file. This template lists
# the most important settings you may want to configure for a production cluster.
#
# Please consult the documentation for further information on configuration options:
# https://www.elastic.co/guide/en/elasticsearch/reference/index.html
#
# ---------------------------------- Cluster -----------------------------------
#
# Use a descriptive name for your cluster:
# es                   #
 cluster.name: cghit-es
#
# ------------------------------------ Node ------------------------------------ #
# Use a descriptive name for the node:
# es            
#
 node.name: node29.data
#
# Add custom attributes to the node:
#           qa     
#
# node.attr.rack: warm
#
# ----------------------------------- Paths ------------------------------------
#
# Path to directory where to store the data (separate multiple locations by comma): #         
#
 path.data: /data/2/es,/data/3/es
#
# Path to log files: #         
 path.logs: /var/log/node29.data
#
# ----------------------------------- Memory ----------------------------------- #
# Lock the memory on startup:
#      lock    
 bootstrap.memory_lock: true
 bootstrap.system_call_filter: false
#
# Make sure that the heap size is set to about half the memory available
# on the system and that the owner of the process is allowed to use this
# limit.
#
# Elasticsearch performs poorly when the system is swapping the memory.
#
# ---------------------------------- Network ----------------------------------- #
# Set the bind address to a specific IP (IPv4 or IPv6):
#   ip
 network.host: 10.123.11.29
#
# Set a custom port for HTTP: #      http    
 http.enabled: false
# http.port: 9200
#
# For more information, consult the network module documentation.
#
# --------------------------------- Discovery ---------------------------------- #
# Pass an initial list of hosts to perform discovery when new node is started:
# The default list of hosts is ["127.0.0.1", "[::1]"]
#    master  
 discovery.zen.ping_timeout: 80s
 discovery.zen.ping.unicast.hosts: ["10.123.11.29:9300", "10.123.11.30:9300", "10.151.11.31:9300"]
#
# Prevent the "split brain" by configuring the majority of nodes (total number of master-eligible nodes / 2 + 1):
#   master          discovery.zen.minimum_master_nodes: 2
#
# For more information, consult the zen discovery module documentation.
#
# ---------------------------------- Gateway ----------------------------------- #
# Block initial recovery after a full cluster restart until N nodes are started: #              
 gateway.recover_after_nodes: 3
#
# For more information, consult the gateway module documentation.
#
# ---------------------------------- Various -----------------------------------
#
# Require explicit names when deleting indices:
#
#action.destructive_requires_name: true
# ---------------------------------- Customer setting --------------------------------
 node.master: false
 node.ingest: true
 node.data: true
 xpack.security.enabled: false
 xpack.graph.enabled: false
 xpack.watcher.enabled: false
 cluster.routing.allocation.disk.watermark.low: 80%
 cluster.routing.allocation.disk.watermark.high: 90%
 cluster.routing.allocation.same_shard.host: true
```

### client 节点

```yml
# ======================== Elasticsearch Configuration =========================
#
# NOTE: Elasticsearch comes with reasonable defaults for most settings.
#       Before you set out to tweak and tune the configuration, make sure you
#       understand what are you trying to accomplish and the consequences.
#
# The primary way of configuring a node is via this file. This template lists
# the most important settings you may want to configure for a production cluster.
#
# Please consult the documentation for further information on configuration options:
# https://www.elastic.co/guide/en/elasticsearch/reference/index.html
#
# ---------------------------------- Cluster -----------------------------------
#
# Use a descriptive name for your cluster:
#
 cluster.name: cghit-es
#
# ------------------------------------ Node ------------------------------------
#
# Use a descriptive name for the node:
#
 node.name: node29.client
#
# Add custom attributes to the node:
#
# node.attr.rack: hot
#
# ----------------------------------- Paths ------------------------------------
#
# Path to directory where to store the data (separate multiple locations by comma):
#
# path.data: /data/10
#
# Path to log files:
#
 path.logs: /var/log/node29.client
#
# ----------------------------------- Memory -----------------------------------
#
# Lock the memory on startup:
#
 bootstrap.memory_lock: true
 bootstrap.system_call_filter: false
#
# Make sure that the heap size is set to about half the memory available
# on the system and that the owner of the process is allowed to use this
# limit.
#
# Elasticsearch performs poorly when the system is swapping the memory.
#
# ---------------------------------- Network -----------------------------------
#
# Set the bind address to a specific IP (IPv4 or IPv6):
#
 network.host: 10.123.11.29
#
# Set a custom port for HTTP:
#
#http.enabled: false
 http.port: 9200
 http.cors.enabled: true
#
# For more information, consult the network module documentation.
#
# --------------------------------- Discovery ----------------------------------
#
# Pass an initial list of hosts to perform discovery when new node is started:
# The default list of hosts is ["127.0.0.1", "[::1]"]
#
 discovery.zen.ping_timeout: 80s
 discovery.zen.ping.unicast.hosts: ["10.151.11.29:9300", "10.151.11.30:9300", "10.151.11.31:9300"]
#
# Prevent the "split brain" by configuring the majority of nodes (total number of master-eligible nodes / 2 + 1):
#
 discovery.zen.minimum_master_nodes: 2
#
# For more information, consult the zen discovery module documentation.
#
# ---------------------------------- Gateway -----------------------------------
#
# Block initial recovery after a full cluster restart until N nodes are started:
#
 gateway.recover_after_nodes: 3
#
# For more information, consult the gateway module documentation.
#
# ---------------------------------- Various -----------------------------------
#
# Require explicit names when deleting indices:
#
#action.destructive_requires_name: true
# ---------------------------------- Customer setting --------------------------------
 node.master: false
 node.data: false
 node.ingest: true
 xpack.security.enabled: false
 xpack.graph.enabled: false
 xpack.watcher.enabled: false
 cluster.routing.allocation.disk.watermark.low: 80%
 cluster.routing.allocation.disk.watermark.high: 90%
 cluster.routing.allocation.same_shard.host: true
```

### tribe 节点

```yml
# ======================== Elasticsearch Configuration =========================
#
# NOTE: Elasticsearch comes with reasonable defaults for most settings.
#       Before you set out to tweak and tune the configuration, make sure you
#       understand what are you trying to accomplish and the consequences.
#
# The primary way of configuring a node is via this file. This template lists
# the most important settings you may want to configure for a production cluster.
#
# Please consult the documentation for further information on configuration options:
# https://www.elastic.co/guide/en/elasticsearch/reference/index.html
#
# ---------------------------------- Cluster -----------------------------------
#
# Use a descriptive name for your cluster:
#
 cluster.name: cghit-es
#
# ------------------------------------ Node ------------------------------------
#
# Use a descriptive name for the node:
#
 node.name: node29.tribe
#
# Add custom attributes to the node:
#
# node.attr.rack: hot
#
# ----------------------------------- Paths ------------------------------------
#
# Path to directory where to store the data (separate multiple locations by comma):
#
# path.data: /data/10
#
# Path to log files:
#
 path.logs: /var/log/node29.tribe
#
# ----------------------------------- Memory -----------------------------------
#
# Lock the memory on startup:
#
 bootstrap.memory_lock: true
 bootstrap.system_call_filter: false
#
# Make sure that the heap size is set to about half the memory available
# on the system and that the owner of the process is allowed to use this
# limit.
#
# Elasticsearch performs poorly when the system is swapping the memory.
#
# ---------------------------------- Network -----------------------------------
#
# Set the bind address to a specific IP (IPv4 or IPv6):
#
 network.host: 10.123.11.29
#
# Set a custom port for HTTP:
#
#http.enabled: false
 http.port: 9100
 http.cors.enabled: true
#
# For more information, consult the network module documentation.
#
# --------------------------------- Discovery ----------------------------------
#
# Pass an initial list of hosts to perform discovery when new node is started:
# The default list of hosts is ["127.0.0.1", "[::1]"]
#
# discovery.zen.ping_timeout: 80s
# discovery.zen.ping.unicast.hosts: ["10.148.180.35:9300", "10.148.180.36:9300", "10.148.180.37:9300"]
#
# Prevent the "split brain" by configuring the majority of nodes (total number of master-eligible nodes / 2 + 1):
#
 discovery.zen.minimum_master_nodes: 2
#
# For more information, consult the zen discovery module documentation.
#
#------------------------------ tribe setting ----------------
#
# Smoe tribe settings
#
# ---------------------------------- Gateway -----------------------------------
#
# Block initial recovery after a full cluster restart until N nodes are started:
#
 gateway.recover_after_nodes: 4
#
# For more information, consult the gateway module documentation.
#
# ---------------------------------- Various -----------------------------------
#
# Require explicit names when deleting indices:
#
#action.destructive_requires_name: true
# ---------------------------------- Customer setting -----------------------------------
 node.master: false
 node.data: false
 node.ingest: false
 xpack.security.enabled: false
 xpack.graph.enabled: false
 xpack.watcher.enabled: false
 cluster.routing.allocation.disk.watermark.low: 50%
 cluster.routing.allocation.disk.watermark.high: 60%
 cluster.routing.allocation.same_shard.host: true
```

在上述示例中，有一些内容需要我们特别注意，如：

- 在启动 Elasticsearch 的时候，不可以用`root`角色启动，需要切换到其他用户，如我们的`cghit`用户。
- 在`elasticsearch.yml`文件中，每个配置是否缩进都是有含义的，因此在配置的时候，我们要着重注意这一点，不用弄错缩进格式。
- 在`master`节点中，` discovery.zen.minimum_master_nodes: 2`配置表示至少得有两个主资格节点存活的时候，才可以进行`master`选举，官方推荐其值为`(N / 2) + 1`，其中`N`为主资格节点数，默认值为`1`。例如，我们仅有一个主资格节点，则不设置或将其显示设置为`1`均可。

## 启动集群

在上述配置完成之后，我们就可以启动集群了。我们首先启动主资格节点，即我们命名为`node29.master`的节点，为了让其可以在后台运行，而不是在服务器退出之后自动结束进程，可以进入 Elasticsearch 的安装目录，通过以下命令启动：

- `./bin/elasticsearch -d`

其中，参数`-d`就是表示该进程可以在后台运行。由于我们仅配置了一个主资格节点，因此在其启动后，其会自动选举为`master`节点；然后，我们依次启动数据节点、客户端节点即可。在节点都启动之后，我们可以通过以下命令查询集群的节点连接情况：

```java
curl -XGET localhost:9200/_cat/nodes?v
```

至此，集群搭建成功。其实上述步骤都应该附上对应的操作图片的，但允许我的偷懒吧，仅是详述了搭建集群的过程。

最后，推荐一个博主感觉不错的文章，里面附带有详细的图片讲解，感兴趣的同学可以移步阅读：

- [搭建 Elasticsearch 环境，搭建 kibana 环境](https://blog.csdn.net/weixin_39800144/article/details/81162002).



