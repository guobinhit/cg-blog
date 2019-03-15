# Elasticsearch 6.6 官方文档 之「索引分片分配」

在本模块中，提供每个索引的设置，以控制分片到节点的分配：

- [分片分配过滤](https://www.elastic.co/guide/en/elasticsearch/reference/current/shard-allocation-filtering.html)：`Shard allocation filtering`，控制将哪些分片（`shard`）分配（`allocation`）给哪些节点。
- [延迟分配](https://www.elastic.co/guide/en/elasticsearch/reference/current/delayed-allocation.html)：`Delayed allocation`，由于节点离开而延迟分配未分配的分片。
- [每个节点的分片总数](https://www.elastic.co/guide/en/elasticsearch/reference/current/allocation-total-shards.html)：`Total shards per node`，对每个节点相同索引中的分片数量的硬限制。

## 分片分配过滤
分片分配过滤允许你指定允许哪些节点承载特定索引的分片。

- **注释**：下面解释的每个索引分片分配过滤器与「[集群级分片分配](https://www.elastic.co/guide/en/elasticsearch/reference/current/shards-allocation.html)」中解释的集群范围的分配过滤器一起工作。

可以在启动时为每个节点分配任意元数据属性。例如，可以为节点分配`rack`和`size`属性，如下所示：

```java
bin/elasticsearch -Enode.attr.rack=rack1 -Enode.attr.size=big  
```

当然，这些属性设置也可以在`elasticsearch.yml`配置文件中指定。

这些元数据属性可与`index.routing.allocation.*`设置一起使用，用于将索引分配给特定的节点组。例如，我们可以将索引`test`移动到`big`节点或`medium`节点，如下所示：

```java
curl -X PUT "localhost:9200/test/_settings" -H 'Content-Type: application/json' -d'
{
  "index.routing.allocation.include.size": "big,medium"
}
'
```

或者，我们可以使用`exclude`规则将索引`test`移离`small`节点：

```java
curl -X PUT "localhost:9200/test/_settings" -H 'Content-Type: application/json' -d'
{
  "index.routing.allocation.exclude.size": "small"
}
'
```

可以指定多个规则，在这种情况下，必须满足所有条件。例如，我们可以使用以下方法将索引`test`移动到`rack1`中的`big`节点：

```java
curl -X PUT "localhost:9200/test/_settings" -H 'Content-Type: application/json' -d'
{
  "index.routing.allocation.include.size": "big",
  "index.routing.allocation.include.rack": "rack1"
}
'
```

- **注释**：如果不能满足某些条件，则不会移动分片。

以下设置是动态的，允许活动索引从一组节点移动到另一组节点：

- `index.routing.allocation.include.{attribute}`，将索引分配给其`{attribute}`至少有一个逗号分隔值的节点。
- `index.routing.allocation.require.{attribute}`，将索引分配给其`{attribute}`具有所有逗号分隔值的节点。
- `index.routing.allocation.exclude.{attribute}`，将索引分配给其`{attribute}`没有逗号分隔值的节点。

还支持这些特殊属性：

| 关键字 | 含义      |
|:--------| :-------------|
| `_name` | 按节点名匹配节点 |
| `_host_ip` |按主机 IP 地址匹配节点（与主机名关联的 IP） |
| `_publish_ip` |按发布 IP 地址匹配节点 |
| `_ip` | 按主机 IP 或发布 IP 匹配节点 |
| `_host` |按主机名匹配节点 |

所有属性值都可以用通配符指定，例如：

```java
curl -X PUT "localhost:9200/test/_settings" -H 'Content-Type: application/json' -d'
{
  "index.routing.allocation.include._ip": "192.168.2.*"
}
'
```

## 节点离开时延迟分配

当节点出于故意或其他原因离开集群时，主节点的反应是：

- 将副本分片升级为主分片以替换节点上的任何主分片。
- 分配副本分片以替换丢失的副本（假设有足够的节点）。
- 重新平衡剩余节点上的分片。

这些操作旨在通过确保每个分片尽快完全复制来保护集群免受数据丢失。

尽管我们在「[节点级别](https://www.elastic.co/guide/en/elasticsearch/reference/current/recovery.html)」和「[集群级别](https://www.elastic.co/guide/en/elasticsearch/reference/current/shards-allocation.html)」都限制了并发还原，但是这种“分片洗牌”仍然会给集群带来大量额外的负载，如果丢失的节点很快就会返回，那么这可能是不必要的。想象一下这个场景：

- 节点`5`失去网络连接。
- 对于节点`5`上的每个主节点，主节点将副本分片提升为主节点。
- 主节点将新副本分配给集群中的其他节点。
- 每个新的复制副本都会在整个网络上复制主分片的完整副本。
- 更多的分片被移动到不同的节点以重新平衡集群。
- 节点`5`几分钟后返回。
- 主节点通过将分片分配到节点`5`来重新平衡集群。

如果主节点只等了几分钟，那么丢失的分片就可以重新分配给节点`5`，网络流量最小。对于「[自动同步刷新](https://www.elastic.co/guide/en/elasticsearch/reference/current/indices-synced-flush.html)」的空闲分片（未接收索引请求的分片），此过程甚至更快。

由于节点已离开而变为未分配的副本分片的分配可以通过`index.unassigned.node_left.delayed_timeout`动态设置延迟，`1m`为其默认值。

可以在活动索引（或所有索引）上更新此设置：

```java
curl -X PUT "localhost:9200/_all/_settings" -H 'Content-Type: application/json' -d'
{
  "settings": {
    "index.unassigned.node_left.delayed_timeout": "5m"
  }
}
'
```

在启用延迟分配的情况下，上述场景将更改如下：

- 节点`5`失去网络连接。
- 对于节点`5`上的每个主节点，主节点将副本分片提升为主节点。
- 主节点记录一条消息，说明未分配分片的分配已延迟，以及延迟了多长时间。
- 群集保持黄色，因为存在未分配的副本分片。
- 节点`5`在几分钟后，在超时到期之前返回。
- 丢失的副本被重新分配到节点`5`（同步刷新的分片几乎立即恢复）。

特别地，此设置不会影响将副本提升为主要副本，也不会影响以前未分配的副本的分配。而且，延迟的分配在完全重新启动集群之后不会生效。此外，在主故障转移情况下，会忘记经过的延迟时间，即重置为完全初始延迟。

### 取消分片迁移

如果延迟分配超时，主节点将丢失的分片分配给另一个节点，该节点将开始恢复。如果丢失的节点重新加入集群，并且其分片仍与主节点具有相同的`sync-id`，则分片迁移将被取消，而同步分片将用于恢复。

因此，默认超时设置为一分钟：即使分片迁移开始，取消还原以支持同步分片也是低成本的。

### 监视延迟的未分配分片

可以使用「[群集运行状况 API](https://www.elastic.co/guide/en/elasticsearch/reference/current/cluster-health.html)」查看其分配被此超时设置延迟的分片数：

```java
curl -X GET "localhost:9200/_cluster/health"
```

此请求将返回`delayed_unassigned_shards`值。

### 永久删除节点

如果一个节点不返回，并且你希望 Elasticsearch 立即分配丢失的分片，只需将超时更新为零即可：

```java
curl -X PUT "localhost:9200/_all/_settings" -H 'Content-Type: application/json' -d'
{
  "settings": {
    "index.unassigned.node_left.delayed_timeout": "0"
  }
}
'
```

一旦丢失的分片开始恢复，就可以重置超时。

## 索引还原优先级

尽可能按优先顺序还原未分配的碎片。索引按以下优先顺序排序：

- 可选`index.priority`设置（先高后低）
- 索引创建日期（先高后低）
- 索引名（先高后低）

这意味着，默认情况下，较新的索引将在较旧的索引之前还原。

可以使用每个索引的可动态更新`index.priority`设置自定义索引优先顺序。例如：

```java
curl -X PUT "localhost:9200/index_1"
curl -X PUT "localhost:9200/index_2"
curl -X PUT "localhost:9200/index_3" -H 'Content-Type: application/json' -d'
{
  "settings": {
    "index.priority": 10
  }
}
'
curl -X PUT "localhost:9200/index_4" -H 'Content-Type: application/json' -d'
{
  "settings": {
    "index.priority": 5
  }
}
'
```

在上述示例中：

- `index_3`将首先恢复，因为它具有最高的`index.priority`。
- `index_4`将在下一个恢复，因为它具有下一个最高优先级。
- 下一步将恢复`index_2`，因为它是最近创建的。
- `index_1`将在最后恢复。

此设置接受整数，并且可以使用「[更新索引设置 API](https://www.elastic.co/guide/en/elasticsearch/reference/current/indices-update-settings.html)」在活动索引上更新：

```java
curl -X PUT "localhost:9200/index_4/_settings" -H 'Content-Type: application/json' -d'
{
  "index.priority": 1
}
'
```

## 每个节点的分片总数

集群级的分片分配器试图将单个索引的分片尽可能地分布在多个节点上。但是，根据你拥有的分片和索引的数量以及它们的大小，可能并不总是能够均匀地分布分片。

以下动态设置允许你指定每个节点允许的单个索引中分片总数的硬限制：

- `index.routing.allocation.total_shards_per_node`，将分配给单个节点的最大分片数（副本和主分片）。默认为无边界。

你还可以限制一个节点可以拥有的分片数量，而不考虑索引：

- `cluster.routing.allocation.total_shards_per_node`，将全局分配给单个节点的最大分片数（副本和主分片）。默认为无边界（`-1`）。

在此需要注意，这些设置是强制执行的硬限制，可能导致某些分片未分配。

小心使用。

----------

**英文原文链接**：[Elasticsearch Reference [6.6] » Index Modules » Index Shard Allocation](https://www.elastic.co/guide/en/elasticsearch/reference/current/index-modules-allocation.html).

