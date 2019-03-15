# Elasticsearch 6.6 官方文档 之「节点」

每次启动 Elasticsearch 实例时，都会启动一个节点。连接节点的集合称之为「[集群](https://www.elastic.co/guide/en/elasticsearch/reference/6.6/modules-cluster.html)」。如果你运行的是单个 Elasticsearch 节点，那么你也就拥有一个由一个节点组成的集群。

集群中的每个节点默认都可以处理「[HTTP](https://www.elastic.co/guide/en/elasticsearch/reference/6.6/modules-http.html)」和 「[Transport](https://www.elastic.co/guide/en/elasticsearch/reference/6.6/modules-transport.html)」。Transport 层专门用于节点和「[Java TransportClient](https://www.elastic.co/guide/en/elasticsearch/client/java-api/6.6/transport-client.html)」之间的通信；HTTP 层仅由外部 REST 客户端使用。

所有节点都知道集群中的所有其他节点，并且可以将客户端请求转发到适当的节点。除此之外，每个节点都有一个或多个用途：

- **主合格节点**：`Master-eligible node`，将`node.master`设置为`true`（默认）的节点，使其有资格被选为控制集群的主节点。

- **数据节点**：`Data node`，将`node.data`设置为`true`（默认）的节点，数据节点保存数据并执行与数据相关的操作，如 CRUD、搜索和聚合。

- **摄取节点**：`Ingest node`，将`node.ingest`设置为`true`（默认）的节点，摄取节点能够将「[摄取管道（`ingest pipeline`）](https://www.elastic.co/guide/en/elasticsearch/reference/6.6/pipeline.html)」应用于文档，以便在索引前转换和丰富文档。对于大量的摄取负载，使用专用的摄取节点并将主节点和数据节点标记为`node.ingest:false`是有意义的。

- **部落节点**：`Tribe node`，部落节点通过`tribe.*`配置，是一种特殊类型的仅协调节点，可以连接到多个集群，并在所有连接的集群上执行搜索和其他操作。

默认情况下，节点既是主合格节点也是数据节点，并且它可以通过摄取管道预处理文档。这对于小型集群非常方便，但是随着集群的增长，考虑将专用的主合格节点与专用的数据节点分离变得非常重要。

- **协调节点**：
  - 像`search`请求或`bulk-indexing`请求这样的请求可能涉及不同数据节点上保存的数据。例如，`search`请求分两个阶段执行，由接收客户端请求的协调节点来进行节点协调。
  - 在分散阶段（`scatter phase`），协调节点将请求转发给保存数据的数据节点。每个数据节点在本地执行请求，并将其结果返回到协调节点。在收集阶段（`gather phase`），协调节点将每个数据节点的结果缩减为单个全局结果集。
  - 每个节点都隐式地是一个协调节点。这意味着将所有三个`node.master`、`node.data`和`node.ingent`设置为`false`的节点将仅充当协调节点，不能禁用。因此，这样的节点需要有足够的内存和 CPU 来处理收集阶段。

## 主合格节点
主节点负责轻量级的集群范围的操作，例如创建或删除索引、跟踪哪些节点是集群的一部分，以及决定将哪些分片（`shard`）分配给哪些节点。对于集群健康来说，拥有一个稳定的主节点是很重要的。

任何符合主节点条件的节点（默认为所有节点）都可以通过「[主选择流程](https://www.elastic.co/guide/en/elasticsearch/reference/6.6/modules-discovery-zen.html)」选择成为主节点。

- **重要的**：主节点必须能够访问`data/ directory`（就像数据节点一样），因为在节点重新启动之间，集群状态就是在这里持续的。

索引和搜索数据是 CPU、内存和 I/O 密集型工作，这会给节点的资源带来压力。为了确保主节点稳定且不受压力，在更大的集群中，最好在专用的符合主节点条件的节点和专用的数据节点之间划分角色。

尽管主节点也可以充当协调节点，并将搜索和索引请求从客户端路由到数据节点，但最好不要为此目的使用专用的主节点。主合格节点的工作越少，对集群的稳定性就越重要。

要创建专用的主合格节点，需要设置：

```java
node.master: true 
node.data: false 
node.ingest: false 
cluster.remote.connect: false
```

1. 默认情况下启用`node.master`角色。
2. 禁用`node.data`角色（默认情况下启用）。
3. 禁用`node.ingest`角色（默认情况下启用）。
4. 禁用跨群集搜索（默认情况下启用）。

特别地，这些设置仅在未安装 X-Pack 时适用。要在安装 X-pack 时创建专用的主合格节点，请参见「[X-Pack](https://www.elastic.co/guide/en/elasticsearch/reference/6.6/modules-node.html#modules-node-xpack)」节点设置。

### 用 minimum_master_nodes 避免脑裂

为了防止数据丢失，必须配置`discovery.zen.minimum_master_nodes`设置（默认为1），以便每个符合主资格的节点都知道为了形成集群必须可见的符合主资格的节点的最小数目。

为了解释，假设你有一个由两个主合格节点组成的集群。网络故障会中断这两个节点之间的通信。每个节点都会看到一个主合格节，也就是其本身。当`minimum_master_nodes`设置为默认值 1 时，这就足以形成集群。每个节点选择自己作为新的主节点（认为另一个符合主节点条件的节点已经死了），结果就是形成两个仅有一个节点的集群，或者说是脑裂（`split brain`）。只有重新启动一个节点后，这两个节点才会重新连接。已写入重新启动节点的任何数据都将丢失。

现在假设你有一个集群，其中有三个符合主节点条件的节点，并且`minimum_master_nodes`设置为 2。如果网络分裂将一个节点与其他两个节点分开，则一个节点的一侧无法看到足够多的符合主节点条件的节点，并且会意识到它无法将自己选为主节点。有两个节点的一侧将选择一个新的主节点（如果需要），并继续正常工作。一旦网络分裂得到解决，单个节点将重新加入集群并再次开始服务请求。

此设置应设置为主合格节点的`quorum`数量：

```java
(master_eligible_nodes / 2) + 1
```

换言之，如果有三个主合格节点，那么最小主节点数应设置为`(3 / 2) + 1`或者说是`2`：

```java
discovery.zen.minimum_master_nodes: 2
```
在此需要注意，配置`discovery.zen.minimum_master_nodes`的默认值为`1`。

为了在其中一个主合格节点出现故障时保持可用，集群应至少具有三个主合格节点，并相应地设置`minimum_master_nodes`数量。「[滚动升级](https://www.elastic.co/guide/en/elasticsearch/reference/6.6/rolling-upgrades.html)」在没有任何停机的情况下执行，也需要至少三个符合主节点条件的节点，以避免在升级过程中发生网络分裂时数据丢失的可能性。

还可以使用「[群集更新设置 API](https://www.elastic.co/guide/en/elasticsearch/reference/6.6/cluster-update-settings.html)」在运行期的群集上动态更改此设置：

```java
curl -X PUT "localhost:9200/_cluster/settings" -H 'Content-Type: application/json' -d'
{
  "transient": {
    "discovery.zen.minimum_master_nodes": 2
  }
}
'
```

- **提示**：在专用节点之间拆分主节点和数据节点角色的一个优点是，你可以只有三个符合主节点条件的节点，并将`minimum_master_nodes`设置为`2`。无论添加到集群中的专用数据节点有多少，都不必更改此设置。

## 数据节点

数据节点保存包含已索引文档的分片。数据节点处理与数据相关的操作，如 CRUD、搜索和聚合。这些操作是 I/O、内存和 CPU 密集型的。监视这些资源并在过载时添加更多的数据节点是很重要的。

拥有专用数据节点的主要好处是分离主节点和数据节点角色。

要创建专用数据节点，需要设置：

```java
node.master: false 
node.data: true 
node.ingest: false 
cluster.remote.connect: false
```

1. 禁用`node.master`角色（默认情况下启用）。
2. 默认情况下启用`node.data`角色。
3. 禁用`node.ingest`角色（默认情况下启用）。
4. 禁用跨群集搜索（默认情况下启用）。

特别地，这些设置仅在未安装 X-Pack 时适用。要在安装 X-pack 时创建专用的数据节点，请参见「[X-Pack](https://www.elastic.co/guide/en/elasticsearch/reference/6.6/modules-node.html#modules-node-xpack)」节点设置。

## 摄取节点

摄取节点可以执行由一个或多个摄取处理器（`ingest processor`）组成的预处理管道。根据摄取处理器执行的操作类型和所需资源的不同，有专门的摄取节点可能是有意义的，它只执行这个特定的任务。

要创建专用的摄取节点，需要设置：

```java
node.master: false 
node.data: false 
node.ingest: true 
cluster.remote.connect: false 
```

1. 禁用`node.master`角色（默认情况下启用）。
2. 禁用`node.data`角色（默认情况下启用）。
3. 默认情况下，`node.ingest`角色处于启用状态。
4. 禁用跨群集搜索（默认情况下启用）。

特别地，这些设置仅在未安装 X-Pack 时适用。要在安装 X-pack 时创建专用的摄取节点，请参见「[X-Pack](https://www.elastic.co/guide/en/elasticsearch/reference/6.6/modules-node.html#modules-node-xpack)」节点设置。

## 仅协调节点

如果你失去了处理主任务、保存数据和预处理文档的能力，那么你就只剩下一个协调节点，该节点只能路由请求、处理搜索减少阶段和分发批量索引。本质上，仅协调节点（`coordinating only node`）的行为就像智能负载均衡器。

通过从数据和主合格节点中卸载协调节点角色，仅协调节点可以使大型集群受益。它们与其他节点一样加入集群并接收完整的集群状态，并使用集群状态将请求直接路由到适当的位置。

- **警告**：向集群添加太多仅协调节点会增加整个集群的负担，因为所选的主节点必须等待来自每个节点的集群状态更新确认！不应夸大仅协调节点的好处，数据节点可以很好地实现相同的目的。

要创建专用的协调节点，需要设置：
	
```java
node.master: false 
node.data: false 
node.ingest: false 
cluster.remote.connect: false
```

1. 禁用`node.master`角色（默认情况下启用）。
2. 禁用`node.data`角色（默认情况下启用）。
3. 禁用`node.ingest`角色（默认情况下启用）。
4. 禁用跨群集搜索（默认情况下启用）。

特别地，这些设置仅在未安装 X-Pack 时适用。要在安装 X-pack 时创建专用的协调节点，请参见「[X-Pack](https://www.elastic.co/guide/en/elasticsearch/reference/6.6/modules-node.html#modules-node-xpack)」节点设置。

## 节点数据路径设置
### path.data

每个数据和主合格节点都需要访问一个数据目录（`data directory`），其中存储了分片、索引和集群元数据。`path.data`默认为`$ES_HOME/data`，但可以在`elasticsearch.yml`配置文件中配置绝对路径或相对于`$ES_HOME`的路径，如下所示：

```xml
path.data:  /var/elasticsearch/data
```

像所有节点设置一样，它也可以在命令行中指定：

```xml
./bin/elasticsearch -Epath.data=/var/elasticsearch/data
```

- **提示**：在使用`.zip`或`.tar.gz`发行版时，应将`path.data`设置配置为在 Elasticsearch 主目录之外定位数据目录，以便可以在不删除数据的情况下删除主目录！RPM 和 Debian 发行版已经为你做到了这一点。

### node.max_local_storage_nodes

数据路径可以由多个节点共享，甚至可以由来自不同集群的节点共享。这对于测试故障转移和开发计算机上的不同配置非常有用。但是，在生产环境中，建议每个服务器只运行一个 Elasticsearch 节点。

默认情况下，Elasticsearch 配置为阻止多个节点共享同一数据路径。要允许多个节点（例如，在你的开发计算机上），需要使用设置`node.max_local_storage_nodes`，并将其设置为大于`1`的正整数。

- **警告**：不要在同一个数据目录中运行不同的节点类型（即主节点、数据节点）。这可能导致意外的数据丢失。

## 其他节点设置

在「[Modules](https://www.elastic.co/guide/en/elasticsearch/reference/6.6/modules.html)」中可以找到更多的节点设置。特别要注意的是「[`cluster.name`](https://www.elastic.co/guide/en/elasticsearch/reference/6.6/cluster.name.html)」、「[`node.name`](https://www.elastic.co/guide/en/elasticsearch/reference/6.6/node.name.html)」和「[网络设置](https://www.elastic.co/guide/en/elasticsearch/reference/6.6/modules-network.html)」。

## X-Pack 节点设置
如果安装了 X-pack，则有一个附加的节点类型：

- [机器学习节点](https://www.elastic.co/guide/en/elasticsearch/reference/6.6/modules-node.html#ml-node)：`xpack.ml.enabled`和`node.ml`设置为`true`的节点，这是安装 X-pack 时的默认行为。如果要使用机器学习功能，集群中必须至少有一个机器学习节点。有关机器学习功能的更多信息，请参阅「[Elastic Stack](https://www.elastic.co/guide/en/elastic-stack-overview/6.6/xpack-ml.html)」中的机器学习。

特别地，**除非安装了 X-pack，否则不要设置使用`node.ml`设置。否则，节点无法启动**。

**如果安装了 X-pack，默认情况下，节点是符合主节点条件的节点、数据节点、摄取节点和机器学习节点**。随着集群的增长，特别是如果你有大型机器学习作业，请考虑将专用的主合格节点与专用的数据节点和专用的机器学习节点分开。

要在安装 X-Pack 时创建专用的**主合格节点**，需要设置：

```java
node.master: true 
node.data: false 
node.ingest: false 
node.ml: false 
xpack.ml.enabled: true 
```

1. 默认情况下启用`node.master`角色。
2. 禁用`node.data`角色（默认情况下启用）。
3. 禁用`node.ingest`角色（默认情况下启用）。
4. 禁用`node.ml`角色（默认情况下在 X-Pack 中启用）。
5. `xpack.ml.enabled`设置在 X-Pack 中默认启用。

要在安装 X-Pack 时创建专用的**数据节点**，需要设置：

```java
node.master: false 
node.data: true 
node.ingest: false 
node.ml: false
```

1. 禁用`node.master`角色（默认情况下启用）。
2. 默认情况下启用`node.data`角色。
3. 禁用`node.ingest`角色（默认情况下启用）。
4. 禁用`node.ml`角色（默认情况下在 X-Pack 中启用）。

要在安装 X-Pack 时创建专用的**摄取节点**，需要设置：

```java
node.master: false 
node.data: false 
node.ingest: true 
cluster.remote.connect: false 
node.ml: false
```

1. 禁用`node.master`角色（默认情况下启用）。
2. 禁用`node.data`角色（默认情况下启用）。
3. 默认情况下启用`node.ingest`角色。
4. 禁用跨群集搜索（默认情况下启用）。
5. 禁用`node.ml`角色（默认情况下在 X-Pack 中启用）。

要在安装 X-Pack 时创建专用的**协调节点**，需要设置：

```java
node.master: false 
node.data: false 
node.ingest: false 
cluster.remote.connect: false 
node.ml: false
```

1. 禁用`node.master`角色（默认情况下启用）。
2. 禁用`node.data`角色（默认情况下启用）。
3. 禁用`node.ingest`角色（默认情况下启用）。
4. 禁用跨群集搜索（默认情况下启用）。
5. 禁用`node.ml`角色（默认情况下在 X-Pack中启用）

## 机器学习节点

机器学习功能提供机器学习节点，这些节点运行作业并处理机器学习 API 请求。如果`xpack.ml.enabled`设置为`true`，`node.ml`设置为`false`，则节点可以服务 API 请求，但不能运行作业。

如果要在集群中使用机器学习功能，则必须在所有符合主资格的节点上启用机器学习（将`xpack.ml.enabled`设置为`true`）。如果没有安装 X-Pack，请不要使用这些设置。

有关这些设置的详细信息，请参阅「[机器学习设置](https://www.elastic.co/guide/en/elasticsearch/reference/6.6/ml-settings.html)」。

要创建专用的**机器学习节点**，需要设置：

```java
node.master: false 
node.data: false 
node.ingest: false 
cluster.remote.connect: false 
node.ml: true 
xpack.ml.enabled: true
```

1. 禁用`node.master`角色（默认情况下启用）。
2. 禁用`node.dat`角色（默认情况下启用）。
3. 禁用`node.ingest`角色（默认情况下启用）。
4. 禁用跨群集搜索（默认情况下启用）。
5. `node.ml`角色在 X-Pack 中默认启用。
6. `xpack.ml.enabled`设置在 X-Pack 中默认启用。


----------

**英文原文链接**：[Elasticsearch Reference [6.6] » Modules » Node](https://www.elastic.co/guide/en/elasticsearch/reference/6.6/modules-node.html).

