# Elasticsearch 6.6 官方文档 之「集群」


`master`的一个主要角色是决定分配哪些分片给哪些节点，以及何时在节点之间移动分片以重新平衡集群。

有许多设置可用于控制分片分配过程：

- 集群等级分片分配（`Cluster Level Shard Allocation`）列出了控制分配和重新平衡操作的设置。
- 基于磁盘的分片分配（`Disk-based Shard Allocation`）解释了 Elasticsearch 如何考虑可用磁盘空间以及相关设置。
- 分片分配感知（`Shard Allocation Awareness`）和强制感知（`Forced Awareness`）控制如何在不同的`racks`或可用性`zones`分配分片。
- 分片分配过滤（`Shard Allocation Filtering`）允许某些节点或节点组从分配中排除，以便它们可以被解除授权。

除此之外，还有一些其他的「[集群等级设置](https://www.elastic.co/guide/en/elasticsearch/reference/6.6/misc-cluster.html)」。

本部分中的所有设置都是动态设置，可以使用「[群集更新设置 API](https://www.elastic.co/guide/en/elasticsearch/reference/6.6/cluster-update-settings.html)」在正在运行的群集上进行更新。

## 集群等级分片分配

分片分配（`Shard allocation`）是将分片分配到节点的过程。这可能发生在初始还原、副本分配、重新平衡、添加或删除节点时。

### 分片分配设置

以下动态设置可用于控制分片分配和还原：

- `cluster.routing.allocation.enable`，启用或禁用特定类型分片的分配：
  - `all` -（默认）允许为所有类型的分片进行分配分片。
  - `primaries` - 只允许为主分片分配分片。
  - `new_primaries` - 只允许为新索引的主分片分配分片。
  - `none` - 禁止为任何索引分配任何类型的分片。

重新启动节点时，此设置不会影响本地主分片的还原。具有未分配的主分片副本的重新启动节点将立即还原该主分片，假定其分配 ID 与集群状态中的活动分配 ID 之一匹配。

- `cluster.routing.allocation.node_concurrent_incoming_recoveries`，一个节点上允许多少并发的传入分片还原。传入还原（`incoming recoveries`）是在节点上分配目标分片（很可能是副本，除非分片正在重新定位）的还原，默认值为`2`。
- `cluster.routing.allocation.node_concurrent_outgoing_recoveries`，一个节点上允许多少并发的传出分片还原。传出还原（`outgoing recoveries`）是在节点上分配源（`source`）分片（很可能是主分片，除非分片正在重新定位）的还原，默认值为`2`。
- `cluster.routing.allocation.node_concurrent_recoveries`，设置`cluster.routing.allocation.node_concurrent_incoming_recoveries`和`cluster.routing.allocation.node_concurrent_outgoing_recoveries`的快捷方式。
- `cluster.routing.allocation.node_initial_primaries_recoveries`，当通过网络还原副本时，节点重新启动后未分配的主分片的还原将使用来自本地磁盘的数据。这些还原应该很快，这样在同一个节点上可以并行进行更多的初始主还原，默认值为`4`。
- `cluster.routing.allocation.same_shard.host`，允许执行检查以防止基于主机名和主机地址在单个主机上分配同一分片的多个实例，默认值为`false`，表示默认情况下不执行任何检查。此设置仅适用于在同一台计算机上启动多个节点的情况。

### 分片重新平衡设置

可以使用以下动态设置来控制集群中分片的重新平衡：

- `cluster.routing.rebalance.enable`，启用或禁用特定类型分片的重新平衡：
  - `all` - （默认）允许对所有类型的分片进行分片平衡。
  - `primaries` - 只允许对主分片进行分片平衡。
  - `replicas` - 仅允许对副本分片进行分片平衡。
  - `none` - 任何索引都不允许任何类型的分片平衡。
- `cluster.routing.allocation.allow_rebalance`，指定何时允许分片重新平衡：
  - `always`  - 总是允许重新平衡。
  - `indices_primaries_active`，仅当集群中的所有主分片都被分配时。
  - `indices_all_active` - （默认）只有在群集中的所有分片（主分片和副本）都分配时。
- `cluster.routing.allocation.cluster_concurrent_rebalance`，允许控制集群范围内多少并发分片重新平衡，默认值为`2`。请注意，此设置仅控制由于集群中的不平衡而导致的并发分片重新定位的数量。此设置不限制由于「[分配过滤](https://www.elastic.co/guide/en/elasticsearch/reference/current/allocation-filtering.html)」或「[强制感知](https://www.elastic.co/guide/en/elasticsearch/reference/current/allocation-awareness.html#forced-awareness)」而导致的分片重新定位。

### 分片平衡探索

以下设置一起用于确定放置每个分片的位置。当任何允许的重新平衡操作都不能使任何节点的权重比`balance.threshold`更接近任何其他节点的权重时，集群就是平衡的。

- `cluster.routing.allocation.balance.shard`，定义节点上分配的分片总数的权重因子（浮点数），默认值为`0.45f`。提高该值会趋向于增加平衡群集中所有节点上的分片数量。
- `cluster.routing.allocation.balance.index`，定义在特定节点上分配的每个索引的分片数量的权重因子（浮点数），默认值为`0.55f`。提高该值会趋向于增加在集群中所有节点上的平衡每个索引的分片数量。
- `cluster.routing.allocation.balance.threshold`，应该执行操作的最小优化值（非负浮点数），默认值为`1.0f`。提升该值将导致群集在优化分片平衡方面的力度降低。

特别地，无论平衡算法的结果如何，由于“强制感知”或“分配过滤”，都可能出现不允许重新平衡的情况。

## 基于磁盘的分片分配

Elasticsearch 在决定是将新分片分配给该节点还是主动将分片重新定位到远离该节点之前，会考虑节点上的可用磁盘空间。

以下是可以在`elasticsearch.yml`配置文件中配置或使用群集更新设置 API 在活动群集上动态更新的设置：

- `cluster.routing.allocation.disk.threshold_enabled`，默认为`true`。设置为`false`可以禁用磁盘分配决定符。
- `cluster.routing.allocation.disk.watermark.low`，控制磁盘使用的低水位线（`watermark`），它默认为`85%`，这意味着 Elasticsearch 不会将分片分配给磁盘空间使用率超过`85%`磁盘的节点。它还可以设置为绝对字节值（如`500MB`），以防止 Elasticsearch 在可用空间少于指定数量时分配分片。此设置对新创建索引的主分片没有影响，特别是对以前从未分配过的任何分片。

- `cluster.routing.allocation.disk.watermark.high`，控制高水位线，它默认为`90%`，这意味着 Elasticsearch 将尝试将分片从磁盘使用率高于`90%`的节点重新定位。它还可以设置为绝对字节值（类似于低水位线），以便在分片的可用空间小于指定数量时将其重新定位到远离节点的位置。此设置影响所有分片的分配，无论以前是否分配。
- `cluster.routing.allocation.disk.watermark.flood_stage`，控制洪泛水位线。它默认为`95%`，这意味着 Elasticsearch 对每个索引强制执行只读索引块（`index.blocks.read_only_allow_delete`），该索引在节点上分配了一个或多个分片，而该节点上至少有一个磁盘超过了洪泛阶段。这是防止节点耗尽磁盘空间的最后手段。一旦有足够的磁盘空间允许索引操作继续，则必须手动释放索引块。

特别地，在这些设置中不能混合使用百分比值和字节值。要么全部设置为百分比值，要么全部设置为字节值。这样我们就可以验证设置是否在内部一致，即低磁盘阈值不超过高磁盘阈值，高磁盘阈值不超过洪泛阶段阈值。

下面为重置`twitter`索引上只读索引块的示例：

```
curl -X PUT "localhost:9200/twitter/_settings" -H 'Content-Type: application/json' -d'
{
  "index.blocks.read_only_allow_delete": null
}
'
```

- `cluster.info.update.interval`，设置 Elasticsearch 应该多久检查一次集群中每个节点的磁盘使用情况，默认为`30`秒。
- `cluster.routing.allocation.disk.include_relocations`，默认值为`true`，这意味着 Elasticsearch 将在计算节点的磁盘使用率时考虑当前正在重新定位到目标节点的分片。但是，考虑到重新定位分片的大小，可能意味着节点的磁盘使用率在高端（`high side`）估计不正确，因为重新定位可能完成`90%`，最近检索到的磁盘使用率将包括重新定位分片的总大小以及正在运行的重新定位所用的空间。

特别地，用百分比值表示已用磁盘空间，而用字节值表示可用磁盘空间，这可能会让人困惑，因为它会翻转“高”和“低”的含义。例如，将低水位线设置为`10GB`，将高水位线设置为`5GB`是有意义的，但不是相反。

将低水位线设置为`100GB`可用空间、高水位线设置为`50GB`可用空间、洪泛阶段水位线设置为`10GB`可用空间及每分钟更新群集信息的示例如下：

```
curl -X PUT "localhost:9200/_cluster/settings" -H 'Content-Type: application/json' -d'
{
  "transient": {
    "cluster.routing.allocation.disk.watermark.low": "100gb",
    "cluster.routing.allocation.disk.watermark.high": "50gb",
    "cluster.routing.allocation.disk.watermark.flood_stage": "10gb",
    "cluster.info.update.interval": "1m"
  }
}
'
```

## 分片分配意识

在同一物理服务器上、在多个`racks`上、在跨多个`zones`或`domains`运行多个虚拟机上的节点时，同一物理服务器上、同一`racks`上、同一`zones`或`domains`中的两个节点更有可能同时崩溃，而不是两个不相关的节点同时崩溃。

如果 Elasticsearch 知道（`aware`）硬件的物理配置，它可以确保主分片及其副本分片分布在不同的物理服务器、`racks`或`zones`中，以最小化同时丢失所有分片副本的风险。

分片分配感知（`shard allocation awareness`）设置允许你告诉 Elasticsearch 你的硬件配置。

例如，假设我们有几个`racks`。当我们启动一个节点时，我们可以通过给它分配一个称为`rack_id`的任意元数据属性来告诉它在哪个`rack`中，我们可以使用任何属性名。例如：

```java
./bin/elasticsearch -Enode.attr.rack_id=rack_one 
```

也可以在`elasticsearch.yml`配置文件中指定此设置。

现在，我们需要通过告诉 Elasticsearch 使用哪些属性来建立分片分配意识。这可以在所有主资格节点上的`elasticsearch.yml`文件中配置，也可以使用集群更新设置 API 设置（和更改）。

对于我们的示例，我们将在配置文件中设置值：

```java
cluster.routing.allocation.awareness.attributes: rack_id
```

有了这个配置，假设我们启动两个节点，`node.attr.rack_id`设置为`rack_one`，然后创建一个索引，其中包含 5 个主分片和每个主分片的 1 个副本。所有的主分片和副本分片都在两个节点之间分配。

现在，如果我们在`node.attr.rack_id`设置为`rack_two`的情况下再启动两个节点，那么 Elasticsearch 会将分片移动到新节点，确保（如果可能）同一分片的两个副本不会在同一个`rack`中。但是，如果`rack_two`失败，同时删除了它的两个节点，那么 Elasticsearch 仍然会将丢失的分片副本分配给`rack_one`中的节点。

- **更倾向于本地分片**：执行`search`或`GET`请求时，如果启用了分片感知，Elasticsearch 将更喜欢使用同一个感知组中的本地分片来执行请求。这通常比跨越`racks`或跨越`zone`边界更快。

可以指定多个感知属性（`awareness attributes`），在这种情况下，在决定分配分片的位置时，将分别考虑每个属性。

```java
cluster.routing.allocation.awareness.attributes: rack_id,zone
```

- **注释 1**：使用感知属性时，不会将分片分配给没有为这些属性设置值的节点。
- **注释 2**：在具有相同感知属性值的特定节点组上分配的分片的主/副本数量由属性值的数量决定。当组中的节点数量不平衡并且有许多副本时，副本分片可能会保留未分配状态。

## 强制感知

假设你有两个`zone`，并且两个`zone`之间有足够的硬件来承载所有的主分片和副本分片。但是，也许一个`zone`中的硬件，虽然足以承载一半的分片，却不能承载所有的分片。

在普通感知下，如果一个`zone`与另一个`zone`失去联系，那么 Elasticsearch 会将所有丢失的副本分片分配给一个`zone`。但在本例中，这种突然的额外负载将导致剩余`zone`中的硬件过载。

强制感知（`Forced awareness`）通过不允许将同一个分片的副本分配到同一个`zone`来解决这个问题。

例如，假设我们有一个名为`zone`的感知属性，我们知道我们将有两个`zones`，`zone1`和`zone2`。以下是我们如何在节点上配置强制感知：

```java
cluster.routing.allocation.awareness.force.zone.values: zone1,zone2 
cluster.routing.allocation.awareness.attributes: zone
```

- 我们必须列出`zone`属性可以拥有的所有可能值。

现在，如果我们在`node.attr.zone`设置为`zone1`的情况下启动 2 个节点，并创建一个包含 5 个分片和 1 个副本的索引。将创建索引，但只分配 5 个主分片（没有副本）。只有在`node.attr.zone`设置为`zone2`的情况下启动更多节点，才会分配副本。

`cluster.routing.allocation.awareness.*`设置可以使用集群升级设置 API 在活动群集上动态更新。

## 分片分配过滤

虽然“索引分片分配”提供了每个索引设置来控制对节点的分片分配，但是“集群等级分片分配过滤”将配置是否允许将分片从任何索引分配到特定节点。

可用的动态集群设置如下，其中`{attribute}`指任意节点属性：

- `cluster.routing.allocation.include.{attribute}`，将分片分配给至少有一个逗号分隔值`{attribute}`的节点。
- `cluster.routing.allocation.require.{attribute}`，仅将分片分配给具有所有逗号分隔值`{attribute}`的节点。
- `cluster.routing.allocation.exclude.{attribute}`，禁止将分片分配给属性具有任何逗号分隔值`{attribute}`的节点。

还支持这些特殊属性：

| 关键字 | 含义     |
|:--------:| :-------------:|
| `_name` |  通过节点名称匹配节点 |
| `_ip` |   通过 ID 地址匹配节点（IP 地址与主机名关联） |
| `_host` |   通过主机名匹配节点 |

集群范围的分片分配过滤的典型用例是当你想要解除一个节点的委托时，并且你想要在关闭之前将分片从该节点移动到集群中的其他节点。

例如，我们可以使用一个节点的 IP 地址将其解除授权，如下所示：

```java
curl -X PUT "localhost:9200/_cluster/settings" -H 'Content-Type: application/json' -d'
{
  "transient" : {
    "cluster.routing.allocation.exclude._ip" : "10.0.0.1"
  }
}
'
```

只有在不破坏另一个路由约束（例如从不将主分片和副本分片分配到同一节点）的情况下，才可以重新定位分片。

除了以逗号分隔的列表形式列出多个值之外，还可以使用通配符指定所有属性值，例如：

```java
curl -X PUT "localhost:9200/_cluster/settings" -H 'Content-Type: application/json' -d'
{
  "transient": {
    "cluster.routing.allocation.exclude._ip": "192.168.2.*"
  }
}
'
```

## 其他群集设置
### 元数据

可以使用以下动态设置将整个集群设置为只读：

- `cluster.blocks.read_only`，使整个集群只读（索引不接受写操作），不允许修改元数据（创建或删除索引）。
- `cluster.blocks.read_only_allow_delete`，在配置集群只读的同时，允许删除索引以释放资源。

在此，需要特别注意：**不要依赖此设置来阻止更改群集。任何可以访问集群更新设置 API 的用户都可以使集群再次读写**。

### 集群分片限制

在 Elasticsearch 7.0 及更高版本中，基于集群中节点的数量，集群中分片的数量将受到软限制。这是为了防止无意中破坏集群稳定性的操作。在 7.0 之前，会导致集群超过限制的操作将发出一个拒绝警告。

- **注释**：你可以将系统属性`es.enforce_max_shards_per_node`设置为`true`以选择严格执行分片限制。如果设置了此系统属性，将导致集群超过限制的操作将导致错误，而不是拒绝警告。此属性将在 Elasticsearch 7.0 中删除，因为严格执行限制是默认的，也是唯一的行为。
- **重要的**：此限制旨在作为安全网，而不是尺寸建议。集群可以安全支持的分片的确切数量取决于你的硬件配置和工作负载，但在几乎所有情况下都应该远远低于此限制，因为默认限制设置得相当高。

如果创建新索引、还原索引快照或打开已关闭的索引等操作会导致群集中的分片数量超过此限制，则该操作将发出拒绝警告。

如果集群已经超过了限制，由于节点成员身份或设置的更改，所有创建或打开索引的操作都将发出警告，直到限制按下面所述增加，或者「[关闭](https://www.elastic.co/guide/en/elasticsearch/reference/6.6/indices-open-close.html)」或「[删除](https://www.elastic.co/guide/en/elasticsearch/reference/6.6/indices-delete-index.html)」某些索引以使分片数量低于限制。

副本数达到此限制，但关闭的索引数不到。一个包含 5 个主分片和 2 个副本分片的索引将被计算为 15 个分片。任何关闭的索引都被计算为 0，不管它包含多少分片和副本。

该限制默认为每个数据节点 1000 个分片，并使用以下属性进行动态调整：

- `cluster.max_shards_per_node`，控制群集中每个数据节点允许的分片数。

例如，具有默认设置的 3 节点集群将允许在所有打开的索引中总共有 3000 个分片。如果将上述设置更改为 500，那么集群将允许总共 1500 个分片。

### 用户定义的群集元数据

可以使用集群设置 API 存储和检索用户定义的元数据。这可以用来存储关于集群的任意、不经常更改的数据，而无需创建索引来存储它。可以使用以`cluster.metadata.`为前缀的任何键存储此数据。例如，要将集群管理员的电子邮件地址存储在`cluster.metadata.administrator`项下，可以发出此请求：

```java
curl -X PUT "localhost:9200/_cluster/settings" -H 'Content-Type: application/json' -d'
{
  "persistent": {
    "cluster.metadata.administrator": "sysadmin@example.com"
  }
}
'
```

- **重要的**：用户定义的集群元数据不用于存储敏感或机密信息。任何访问「[Cluster Get Settings](https://www.elastic.co/guide/en/elasticsearch/reference/6.6/cluster-get-settings.html)」API 的用户都可以查看存储在用户定义的集群元数据中的任何信息，并记录在 Elasticsearch 日志中。

### 索引墓碑

集群状态维护索引墓碑（`index tombstones`）以显式地表示已删除的索引。群集状态下维护的墓碑数量由以下属性控制，这些属性无法动态更新：

- `cluster.indices.tombstones.size`，当发生删除时，索引逻辑删除会阻止不属于群集的节点加入群集并重新导入索引，就像从未发出删除一样。为了防止集群状态增长，我们只保留最后一个`cluster.indices.tombstones.size`删除，默认为`500`。如果你希望集群增大该值，当然也是可以的，但我们认为这是罕见的，因此设置了默认值，并且禁止动态修改此值。墓碑不占多少空间，但我们也认为像`50000`这样的数字可能太大了。

### 日志记录器

控制日志记录的设置可以使用`logger.`前缀动态更新。例如，要增加日志级别，将`indices.recovery `模块的日志级别设置为`DEBUG`，可以发出以下请求：

```java
curl -X PUT "localhost:9200/_cluster/settings" -H 'Content-Type: application/json' -d'
{
  "transient": {
    "logger.org.elasticsearch.indices.recovery": "DEBUG"
  }
}
'
```

### 持久性任务分配

插件可以创建一种称为持久性任务（`persistent task`）的任务。这些任务通常是长期存在的任务，并存储在集群状态中，允许在集群完全重新启动后恢复任务。

每次创建持久性任务时，主节点负责将任务分配给集群的其他节点，然后分配的节点将拾取任务并在本地执行。将持久性任务分配给节点的过程由以下属性控制，这些属性可以动态更新：

- `cluster.persistent_tasks.allocation.enable`，启用或禁用持久任务的分配：
  - `all` -（默认）允许将持久性任务分配给节点
  - `none` - 不允许为任何类型的持久性任务分配

此设置不会影响已执行的持久性任务。只有新创建的持久性任务或必须重新分配的任务（例如，在节点离开集群之后）才受此设置的影响。

- `cluster.persistent_tasks.allocation.recheck_interval`，当集群状态发生显著变化时，主节点将自动检查是否需要分配持久性任务。但是，可能还有其他因素（例如内存使用）影响持久性任务是否可以分配给节点，但不会导致集群状态更改。此设置控制执行分配检查以响应这些因素的频率，默认值为`30`秒，最小允许值为`10`秒。





----------

**英文原文链接**：[Elasticsearch Reference [6.6] » Modules » Cluster](https://www.elastic.co/guide/en/elasticsearch/reference/6.6/modules-cluster.html).

