# Elasticsearch 5.x 版本中的冷热数据架构

当使用 Elasticsearch 进行更大的时间数据分析用例时，我们建议使用基于时间（`time-based`）的索引和具有 3 种不同类型节点（主节点、热节点和冷节点）的分层架构，我们称之为`Hot-Warm`架构。每个节点都有自己的特性，如下所述。

# 主节点

我们建议每个集群运行 3 个专用的主节点（`master nodes`），以提供最大的弹性。使用这些功能时，还应将`discovery.zen.minimum_master_nodes`设置为`2`，以防止出现“脑裂”的情况。利用专用的主节点，只负责处理集群管理和状态，增强了整体稳定性。因为它们不包含数据，也不参与搜索和索引操作，所以它们对 JVM 的要求与在大量索引或长时间、昂贵的搜索中可能出现的要求不同。因此，不太可能受到长时间垃圾收集暂停的影响。因此，可以为它们提供比数据节点所需配置低得多的 CPU、RAM 和磁盘配置。

# 热节点

这个专门的数据节点执行集群中的所有索引。它们还持有最新的索引，因为这些索引通常最常被查询的。由于索引是一种 CPU 和 IO 密集型操作，因此这些服务器需要强大的功能并由连接的 SSD 存储进行支持。我们建议至少运行 3 个热节点（`hot node`）以实现高可用性。不过，根据你希望收集和查询的最新数据量，你很可能需要增加这个数字以实现性能目标。

# 冷节点

这种类型的数据节点被设计用来处理大量的只读索引，这些索引不太可能被频繁查询。由于这些索引是只读的，所以冷节点（`warm node`，*译者注：冷热节点是相对的概念*）倾向于使用大型附加磁盘（通常是旋转磁盘）而不是 SSD。与热节点一样，我们建议至少 3 个冷节点以实现高可用性。和以前一样，需要注意的是，大量的数据可能需要额外的节点来满足性能要求。还要注意，CPU 和内存配置通常需要镜像那些热节点。这只能通过使用类似于在生产环境中体验的查询进行测试来确定。

Elasticsearch 集群需要知道哪些服务器包含热节点，哪些服务器包含冷节点。这可以通过为每个服务器分配任意「[属性](https://www.elastic.co/guide/en/elasticsearch/reference/5.1/allocation-awareness.html#forced-awareness)」来实现。

例如，可以在`elasticsearch.yml`中使用`node.attr.box_type: hot`标记热节点，或者使用`./bin/elasticsearch -Enode.attr.box_type=hot`启动热节点。

类似的，冷节点也需要在`elasticsearch.yml`中使用`node.attr.box_type: warm`进行标记，或者使用`./bin/elasticsearch -Enode.attr.box_type=warm`启动冷节点。

`box_type`属性是完全任意的，你可以随意命名它（*译者注，正如`hot`和`warm`仅是概念上的名称而已，我们完全可以用`black`和`white`来代替*）。这些任意值将用于告诉 Elasticsearch 在何处分配索引。

我们可以通过使用以下设置创建热节点，从而确保今天的索引位于使用 SSD 的热节点上：

```java
PUT /logs_2016-12-26
{
  "settings": {
    "index.routing.allocation.require.box_type": "hot"
  }
}
```

几天后，如果索引不再需要在性能最高的硬件上运行，我们可以通过更新其索引设置将其移动到标记为`warm`的节点上：

```java
PUT /logs_2016-12-26/_settings 
{ 
  "settings": { 
    "index.routing.allocation.require.box_type": "warm"
  } 
}
```

现在，我们如何使用`logstash`或`beats`来实现这一点：

如果在`logstash`或`beats`级别管理索引模板（`index template`），则应更新索引模板以包括分配筛选。`"index.routing.allocation.require.box_type" : "hot"`设置将导致在热节点上创建任何新索引。例如，

```java
{
  "template" : "indexname-*",
  "version" : 50001,
  "settings" : {
             "index.routing.allocation.require.box_type": "hot"
 ...
```

另一种策略是为集群中的任何索引添加一个通用模板，`"template": "*"`，它在热节点中创建新的索引。例如，

```java
{
  "template" : "*",
  "version" : 50001,
  "settings" : {
           "index.routing.allocation.require.box_type": "hot"
 ...
```

当你确定一个索引没有被写入，也没有被频繁搜索时，它可以从热节点迁移到冷节点。这可以通过更新其索引设置来完成：`"index.routing.allocation.require.box_type" : "warm"`。Elasticsearch 将自动将索引迁移到冷节点。

最后，通过在`elasticsearch.yml`中设置`index.codec: best_compression`，我们还可以在所有冷数据节点上实现更好的压缩。当数据移动到冷节点时，我们可以调用`_forcemerge` API 来合并段：它不仅通过具有较少的段来节省内存、磁盘空间和文件句柄，而且还具有使用这种新的最佳压缩（`best_compression`）编解码器重写索引的能力。

在索引仍被分配给大盒子（`strong boxes`）的时候，强制合并索引是一个坏主意，因为优化过程将淹没这些节点上的 I/O，并影响当前日志的索引速度。但是中等大小的盒子（`medium boxes`）做的不多，所以强制合并它们是安全的。

既然我们已经了解了如何手动更改索引的分片分配，那么让我们来看看如何使用我们的一个叫做「[Curator](https://www.elastic.co/guide/en/elasticsearch/client/curator/current/installation.html)」的工具来自动化这个过程。

在下面的示例中，我们使用 Curator 4.2 在 3 天后将索引从热节点移动到冷节点：

```java
actions:
  1:
    action: allocation
    description: "Apply shard allocation filtering rules to the specified indices"
    options:
      key: box_type
      value: warm
      allocation_type: require
      wait_for_completion: true
      timeout_override:
      continue_if_exception: false
      disable_action: false
    filters:
    - filtertype: pattern
      kind: prefix
      value: logstash-
    - filtertype: age
      source: name
      direction: older
      timestring: '%Y.%m.%d'
      unit: days
      unit_count: 3
```

最后，我们可以使用 Curator 强制合并索引。在运行优化之前，请确保等待足够长的时间以完成重新分配。你可以通过在操作`1`中设置`wait-for-completion`或更改`unit_count`来选择操作`2`中大于`4`天的索引，这样它们就有机会在索引强制合并之前完全迁移。

```java
 2:
    action: forcemerge
    description: "Perform a forceMerge on selected indices to 'max_num_segments' per shard"
    options:
      max_num_segments: 1
      delay:
      timeout_override: 21600 
      continue_if_exception: false
      disable_action: false
    filters:
    - filtertype: pattern
      kind: prefix
      value: logstash-
    - filtertype: age
      source: name
      direction: older
      timestring: '%Y.%m.%d'
      unit: days
      unit_count: 3
```

注意`timeout_override`默认值为`21600`秒，但根据你的设置，它可能会变快或变慢。

由于 Elasticsearch 5.0，我们还可以使用`Rollover`和`shrink` API来减少分片的数量，这是一种更简单、更有效的管理基于时间的索引的方法。你可以在这个「[博客](https://www.elastic.co/blog/managing-time-based-indices-efficiently)」中找到更多关于它的细节。

喜欢这个话题吗？来我们的用户会议`Elastic{ON}`吧，在这里我们将进行更深入的探讨，而且你可以与我们的工程团队面对面的讨论！我们希望在那里见到你。


----------

**英文原文链接**：[“Hot-Warm” Architecture in Elasticsearch 5.x](https://www.elastic.co/blog/hot-warm-architecture-in-elasticsearch-5-x).


