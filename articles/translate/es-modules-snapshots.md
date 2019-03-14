# 快照和还原

快照（`snapshot`）是从正在运行的 Elasticsearch 集群中获取的备份。你可以获取单个索引（`indices`）或整个集群的快照，并将其存储在共享文件系统上的存储库中，并且有支持 S3、HDFS、Azure、Google 云存储等远程存储库的插件。

快照是递增的。这意味着，创建索引快照时，Elasticsearch 将避免复制存储库中已存储的任何数据，作为同一索引的早期快照的一部分。因此，频繁地对集群进行快照是很有效的。

快照可以通过`restore` API 还原到正在运行的集群中。还原（`restore`）索引时，可以更改已还原索引的名称及其某些设置，从而在如何使用快照和还原功能方面具有很大的灵活性。

- **警告**：不可能只通过复制其所有节点的数据目录来备份 Elasticsearch 集群。Elasticsearch 在运行时可能会对其数据目录的内容进行更改，这意味着复制其数据目录无法捕获其内容的一致图片。尝试从这样的备份中恢复群集可能会失败，报告损坏和丢失文件，或者看似成功地恢复集群但实际上却丢失了一些数据。备份集群的唯一可靠方法是使用快照和还原功能。

## 版本兼容性

快照包含构成索引的磁盘上数据结构的副本。这意味着快照只能还原为可以读取索引的 Elasticsearch 版本：

- 在`5.x`中创建的索引快照可以还原为`6.x`。
- 在`2.x`中创建的索引快照可以还原为`5.x`。
- 在`1.x`中创建的索引快照可以还原为`2.x`。

相反，在`1.x`中创建的索引的快照不能还原为`5.x`或`6.x`，在`2.x`中创建的索引的快照不能还原为`6.x`。

每个快照可以包含在不同版本的 Elasticsearch 中创建的索引，并且在还原快照时，必须能够将所有索引还原到目标集群中。如果快照中的任何索引是在不兼容的版本中创建的，则无法还原快照。

- **重要的**：在升级前备份数据时，请记住，如果快照包含在与升级版本不兼容的版本中创建的索引，升级后将无法还原快照。

如果在需要还原与当前运行的群集版本不兼容的索引快照的情况下结束，可以在最新的兼容版本上还原该快照，并使用「[`reindex-from-remote`](https://www.elastic.co/guide/en/elasticsearch/reference/6.6/docs-reindex.html#reindex-from-remote)」在当前版本上重建索引。只有在原始索引启用了`source`时，才能从远程重新索引。检索和重新索引数据可能比简单地还原快照要花费更长的时间。如果你有大量的数据，我们建议你在继续之前使用数据子集测试远程进程的`reindex`，以了解时间要求。

## 仓库

必须先注册快照存储库，然后才能执行快照和还原操作。我们建议为每个主要版本创建一个新的快照存储库。有效的存储库设置取决于存储库类型。

如果在多个集群中注册相同的快照存储库，则只有一个集群对该存储库具有写访问权。连接到该存储库的所有其他集群都应将存储库设置为只读模式。

- **重要的**：快照格式可以跨主要版本进行更改，因此，如果不同版本上的集群试图写入同一存储库，则由一个版本写入的快照可能对另一个版本不可见，并且存储库可能已损坏。当将存储库设置为除一个集群之外的所有集群的只读时，应使用多个不同于一个主要版本的集群，但这不是受支持的配置。

```java
curl -X PUT "localhost:9200/_snapshot/my_backup" -H 'Content-Type: application/json' -d'
{
  "type": "fs",
  "settings": {
    "location": "my_backup_location"
  }
}
'
```

要检索有关已注册存储库的信息，可以使用 GET 请求：

```java
curl -X GET "localhost:9200/_snapshot/my_backup"
```

其返回结果为：

```java
{
  "my_backup": {
    "type": "fs",
    "settings": {
      "location": "my_backup_location"
    }
  }
}
```

要检索有关多个存储库的信息，请指定以逗号分隔的存储库列表。指定存储库名称时，还可以使用`*`通配符。例如，以下请求检索有关以`repo`开始或包含`backup`的所有快照存储库的信息：

```java
curl -X GET "localhost:9200/_snapshot/repo*,*backup*"
```

要检索有关所有已注册快照存储库的信息，请省略存储库名称或指定`_all`：

```java
curl -X GET "localhost:9200/_snapshot"
```

或者

```java
curl -X GET "localhost:9200/_snapshot/_all"
```

### 共享文件系统资源库

共享文件系统存储库（`"type": "fs"`）使用共享文件系统存储快照。为了注册共享文件系统存储库，需要将同一共享文件系统装载到所有主节点和数据节点上的同一位置。此位置（或其父目录之一）必须在所有主节点和数据节点的`path.repo`设置中注册。

假设共享文件系统安装到`/mount/backups/my_fs_backup_location`，那么应将以下设置添加到`elasticsearch.yml`文件中：

```java
path.repo: ["/mount/backups", "/mount/longterm_backups"]
```

`path.repo`设置支持 Microsoft Windows UNC 路径，只要至少将服务器名称和共享指定为前缀，并且反斜杠正确转义：

```java
path.repo: ["\\\\MY_SERVER\\Snapshots"]
```

重新启动所有节点后，可以使用以下命令以`my fs_backup`的名称注册共享文件系统存储库：

```java
curl -X PUT "localhost:9200/_snapshot/my_fs_backup" -H 'Content-Type: application/json' -d'
{
    "type": "fs",
    "settings": {
        "location": "/mount/backups/my_fs_backup_location",
        "compress": true
    }
}
'
```

如果将存储库位置指定为相对路径，则将根据`path.repo`中指定的第一个路径解析此路径：

```java
curl -X PUT "localhost:9200/_snapshot/my_fs_backup" -H 'Content-Type: application/json' -d'
{
    "type": "fs",
    "settings": {
        "location": "my_fs_backup_location",
        "compress": true
    }
}
'
```

存储库的配置支持以及配置：

| 关键字 | 含义     |
|:--------| :-------------|
|  `location`    |     快照的位置，强制性的。     |
|  `compress`    |    打开快照文件的压缩功能。压缩仅应用于元数据文件（索引映射和设置），数据文件不压缩，默认为`true`。      |
|   `chunk_size`   |     如果需要，可以在快照期间将大文件分解成块。块大小可以用字节或使用大小值表示法指定，即`1g`、`10m`、`5k`。默认为`null`，不限制块大小。     |
|  `max_restore_bytes_per_sec`    |    每节点还原速率的限制，默认为每秒`40MB`。      |
|   `max_snapshot_bytes_per_sec`   |   每节点快照速率的限制。默认为每秒`40MB`。       |
|   `readonly`   |     使存储库为只读，默认为`false`。     |

### 只读 URL 资源库

URL 存储库（`"type": "url"`）可用作访问共享文件系统存储库创建的数据的可选只读方式。在`url`参数中指定的 URL 应该指向共享文件系统存储库的根目录。支持以下设置：

| 关键字 | 含义     |
|:--------| :-------------|
|  `url`    |     快照的位置，强制性的。     |

URL 存储库支持以下协议：`http`、`https`、`ftp`、`file`和`jar`。必须通过在`repositories.url.allowed_urls`的 URLs 设置中指定允许的白名单，包含`http:`、`https:`和`ftp:`的 URL 存储库。此设置支持在主机、路径、查询和片段位置使用通配符。例如：

```java
repositories.url.allowed_urls: ["http://www.example.org/root/*", "https://*.mydomain.com/*?*#*"]
```

带文件的 URL 存储库：URLs 只能指向在`path.repo`设置中注册的位置，类似于共享文件系统存储库。

### 仅限源存储库

源存储库（`source repository`）使你能够创建占用磁盘空间最多减少 50% 的仅限源的最小快照。仅限源快照包含存储字段和索引元数据。它们不包括索引或 doc 值结构，并且在还原时不可搜索。还原仅源（`source-only`）快照后，必须将数据重新索引到新索引中。

源存储库委托给另一个快照存储库进行存储。

- **重要的**：只有在启用了`_source`字段且未应用源筛选（`source-filtering`）时，才支持仅源快照。还原仅源快照时：
  - 还原的索引是只读的，只能满足`match_all`搜索或滚动请求以启用重新索引。
  - 不支持除`match_all`和`_get`请求以外的查询。
  - 还原索引的映射为空，但原始映射可从类型顶级元（`meta`）元素获得。
  
 创建源存储库时，必须指定存储快照的代理存储库的类型和名称：

```java
curl -X PUT "localhost:9200/_snapshot/my_src_only_repository" -H 'Content-Type: application/json' -d'
{
  "type": "source",
  "settings": {
    "delegate_type": "fs",
    "location": "my_backup_location"
  }
}
'
```

### 存储库插件编

这些官方插件中还提供了其他存储库后端：

- 用于 S3 存储库支持的「[repository-s3](https://www.elastic.co/guide/en/elasticsearch/plugins/6.6/repository-s3.html)」
- 用于 Hadoop 环境中 HDFS 存储库支持的「[repository-hdfs](https://www.elastic.co/guide/en/elasticsearch/plugins/6.6/repository-hdfs.html)」
- 用于 Azure 存储库的「[repository-azure](https://www.elastic.co/guide/en/elasticsearch/plugins/6.6/repository-azure.html)」
- Google 云存储库的「[repository-gcs](https://www.elastic.co/guide/en/elasticsearch/plugins/6.6/repository-gcs.html)」

### 存储库验证

注册存储库后，会立即在所有主节点和数据节点上进行验证，以确保它在集群中当前存在的所有节点上都能正常工作。`verify`参数可用于在注册或更新存储库时显式禁用存储库验证：

```java
curl -X PUT "localhost:9200/_snapshot/my_unverified_backup?verify=false" -H 'Content-Type: application/json' -d'
{
  "type": "fs",
  "settings": {
    "location": "my_unverified_backup_location"
  }
}
'
```

也可以通过运行以下命令手动执行验证过程：

```java
curl -X POST "localhost:9200/_snapshot/my_unverified_backup/_verify"
```

它返回成功验证存储库的节点列表，或者在验证过程失败时返回错误消息。

## 快照

存储库可以包含同一集群的多个快照。快照由集群中的唯一名称标识。通过执行以下命令，可以创建存储库`my_backup`的名为`snapshot_1`的快照：

```java
curl -X PUT "localhost:9200/_snapshot/my_backup/snapshot_1?wait_for_completion=true"
```

`wait_for_completion`参数指定请求是否应在快照初始化（默认）后立即返回，或等待快照完成。在快照初始化过程中，所有以前的快照的信息都会加载到内存中，这意味着在大型存储库中，即使`wait_for_completion`参数设置为`false`，此命令也可能需要几秒钟（甚至几分钟）才能返回。

默认情况下，将创建集群中所有打开和启动索引的快照。通过在快照请求主体中指定索引列表，可以更改此行为。

```java
curl -X PUT "localhost:9200/_snapshot/my_backup/snapshot_2?wait_for_completion=true" -H 'Content-Type: application/json' -d'
{
  "indices": "index_1,index_2",
  "ignore_unavailable": true,
  "include_global_state": false
}
'
```

可以使用支持「[多索引语法](https://www.elastic.co/guide/en/elasticsearch/reference/6.6/multi-index.html)」的`indices`参数指定应包含在快照中的索引列表。快照请求还支持`ignore_unavailable`选项。将其设置为`true`将导致在创建快照期间忽略不存在的索引。默认情况下，如果未设置`ignore_unavailable`选项并且缺少索引，则快照请求将失败。通过将` include_global_state`设置为`false`，可以防止集群全局状态存储为快照的一部分。默认情况下，如果参与快照的一个或多个索引没有所有主碎片可用，则整个快照将失败。可以通过将`partial`设置为`true`来更改此行为。

快照名称可以使用「[日期数学表达式](https://www.elastic.co/guide/en/elasticsearch/reference/6.6/date-math-index-names.html)」自动派生，与创建新索引时类似。请注意，特殊字符需要进行 URI 编码。

例如，可以使用以下命令创建名称为当前日期的快照，如`snapshot-2018.05.11`：

```java
# PUT /_snapshot/my_backup/<snapshot-{now/d}>
curl -X PUT "localhost:9200/_snapshot/my_backup/%3Csnapshot-%7Bnow%2Fd%7D%3E"
```

索引快照过程是增量的。在创建索引快照 Elasticsearch 的过程中，分析存储库中已存储的索引文件列表，并仅复制自上次快照以来创建或更改的文件。它允许在存储库中以紧凑的形式保留多个快照。快照过程以非阻塞方式执行。所有索引和搜索操作都可以继续对正在快照的索引执行。但是，快照表示创建快照时索引的时间点视图，因此快照中不存在在快照进程启动后添加到索引中的记录。对于已启动但目前未重新定位的主分片，快照过程将立即启动。在`1.2.0`版本之前，如果集群有任何重新定位或初始化参与快照的主要索引，则快照操作将失败。从`1.2.0`版开始，Elasticsearch 等待分片的重新定位或初始化完成，然后再对其进行快照。

除了创建每个索引的副本，快照过程还可以存储全局集群元数据，其中包括持久集群设置和模板。临时设置和已注册的快照存储库不作为快照的一部分存储。

在集群中，任何时候只能执行一个快照进程。在创建特定分片的快照时，此分片不能移动到另一个节点，这可能会干扰重新平衡过程和分配筛选。完成快照后，Elasticsearch 只能将分片移动到另一个节点（根据当前分配过滤设置和重新平衡算法）。

创建快照后，可以使用以下命令获取有关此快照的信息：

```java
curl -X GET "localhost:9200/_snapshot/my_backup/snapshot_1"
```

此命令返回有关快照的基本信息，包括开始和结束时间、创建快照的 Elasticsearch 版本、包含索引的列表、快照的当前状态以及快照期间发生的故障列表。快照状态可以是：

| 关键字 | 含义     |
|:--------| :-------------|
|  `IN_PROGRESS`    |    快照当前正在运行。     |
|  `SUCCESS`    |   快照完成，所有分片存储成功。     |
|  `FAILED`    |   快照已完成，但出现错误，无法存储任何数据。     |
|  `PARTIAL`    |   全局群集状态已存储，但至少一个碎片的数据未成功存储。在这种情况下，故障部分应该包含有关未正确处理的碎片的更详细信息。     |
|  `INCOMPATIBLE`    |    快照是用旧版本的 Elasticsearch 创建的，因此与集群的当前版本不兼容。     |

与存储库类似，可以一次查询有关多个快照的信息，还支持通配符：

```java
curl -X GET "localhost:9200/_snapshot/my_backup/snapshot_*,some_other_snapshot"
```

可以使用以下命令列出存储库中当前存储的所有快照：

```java
curl -X GET "localhost:9200/_snapshot/my_backup/_all"
```

如果某些快照不可用，则该命令将失败。布尔参数`ignore_unavailable`可用于返回当前可用的所有快照。

从成本和性能的角度来看，在基于云的存储库中获取存储库中的所有快照都是昂贵的。如果所需的唯一信息是存储库中的快照`names/uuids`和每个快照中的索引，则可以将可选的布尔参数`verbose`设置为`false`，以对存储库中的快照执行更高性能和更经济高效的检索。请注意，将`verbose`设置为`false`将忽略有关快照的所有其他信息，例如状态信息、快照碎片数等。`verbose`参数的默认值为`true`。

可以使用以下命令检索当前正在运行的快照：

```java
curl -X GET "localhost:9200/_snapshot/my_backup/_current"
```

可以使用以下命令从存储库中删除快照：

```java
curl -X DELETE "localhost:9200/_snapshot/my_backup/snapshot_2"
```

从存储库中删除快照时，Elasticsearch 将删除与已删除快照关联且未被任何其他快照使用的所有文件。如果在创建快照时执行已删除的快照操作，则快照过程将中止，并且将清除作为快照过程一部分创建的所有文件。因此，删除快照操作可用于取消错误启动的长时间运行的快照操作。

可以使用以下命令注销存储库：

```java
curl -X DELETE "localhost:9200/_snapshot/my_backup"
```

当存储库未注册时，Elasticsearch 只删除对存储库存储快照的位置的引用。快照本身保持原样。

## 还原

可以使用以下命令还原快照：

```java
curl -X POST "localhost:9200/_snapshot/my_backup/snapshot_1/_restore"
```

默认情况下，将还原快照中的所有索引，并且不会还原群集状态。可以选择应该还原的索引，也可以通过使用索引来还原全局群集状态，并在还原请求正文中设置`include_global_state`选项。索引列表支持多索引语法。`rename_pattern`和`rename_replacement`选项还可用于使用支持引用原始文本的正则表达式重命名还原时的索引，如「[此处](https://docs.oracle.com/javase/6/docs/api/java/util/regex/Matcher.html#appendReplacement(java.lang.StringBuffer,%20java.lang.String))」所述。将`include_aliases`设置为`false`，以防止别名与关联索引一起还原。

```java
curl -X POST "localhost:9200/_snapshot/my_backup/snapshot_1/_restore" -H 'Content-Type: application/json' -d'
{
  "indices": "index_1,index_2",
  "ignore_unavailable": true,
  "include_global_state": true,
  "rename_pattern": "index_(.+)",
  "rename_replacement": "restored_index_$1"
}
'
```

可以在正常工作的群集上执行还原操作。但是，只有当现有索引「[关闭](https://www.elastic.co/guide/en/elasticsearch/reference/6.6/indices-open-close.html)」并且具有与快照中的索引相同数量的分片时，才能还原该索引。如果还原索引已关闭，还原操作将自动打开它们；如果它们不在群集中，则创建新索引。如果群集状态恢复为`include_global_state`（默认值为`false`），则会添加群集中当前不存在的已还原模板，并将具有相同名称的现有模板替换为已还原模板。还原的永久性设置将添加到现有的永久性设置中。

### 部分还原

默认情况下，如果参与操作的一个或多个索引没有所有分片的快照，则整个恢复操作将失败。例如，如果某些分片未能快照，则可能发生这种情况。仍然可以通过将`partial`设置为`true`来恢复这些索引。请注意，在这种情况下，只会还原成功的快照分片，并且所有丢失的分片都将重新创建为空。

### 在还原期间更改索引设置

在还原过程中，可以覆盖大多数索引设置。例如，在切换回默认刷新间隔时，以下命令将在不创建任何副本的情况下恢复`index_1`：

```java
curl -X POST "localhost:9200/_snapshot/my_backup/snapshot_1/_restore" -H 'Content-Type: application/json' -d'
{
  "indices": "index_1",
  "index_settings": {
    "index.number_of_replicas": 0
  },
  "ignore_index_settings": [
    "index.refresh_interval"
  ]
}
'
```

请注意，在还原操作期间不能更改某些设置，如`index.number_of_shards`。

### 还原到其他群集

存储在快照中的信息没有绑定到特定的集群或集群名称。因此，可以将从一个集群生成的快照还原到另一个集群。只需要在新集群中注册包含快照的存储库并启动还原过程。新集群不必具有相同的大小或拓扑结构。但是，新集群的版本应该与用于创建快照的集群的版本相同或更新（只有 1 个主要版本更新）。例如，可以将`1.x`快照还原到`2.x`群集，但不能将`1.x`快照还原到`5.x`群集。

如果新集群的大小较小，则应额外考虑。首先，需要确保新集群有足够的容量来存储快照中的所有索引。可以在恢复期间更改索引设置以减少副本的数量，这有助于将快照还原到较小的集群中。也可以使用`indexs`参数仅选择索引的子集。

如果使用「[分片分配过滤](https://www.elastic.co/guide/en/elasticsearch/reference/6.6/shard-allocation-filtering.html)」将原始集群中的索引分配给特定的节点，那么新集群中将强制执行相同的规则。因此，如果新集群不包含具有可在其上分配已还原索引的适当属性的节点，则除非在还原操作期间更改这些索引分配设置，否则将无法成功还原此类索引。

还原操作还检查还原的永久设置是否与当前群集兼容，以避免意外还原不兼容的设置，如`discovery.zen.minimum_master_nodes`，从而禁用较小的群集，直到添加所需数量的符合主节点。如果需要使用不兼容的持久设置还原快照，请尝试在不使用全局群集状态的情况下还原快照。

## 快照状态

可以使用以下命令获取当前正在运行的快照及其详细状态信息的列表：

```java
curl -X GET "localhost:9200/_snapshot/_status"
```

在这种格式下，命令将返回有关当前运行的所有快照的信息。通过指定存储库名称，可以将结果限制到特定的存储库：

```java
curl -X GET "localhost:9200/_snapshot/my_backup/_status"
```

如果同时指定了存储库名称和快照 ID，则此命令将返回给定快照的详细状态信息，即使该快照当前未运行：

```java
curl -X GET "localhost:9200/_snapshot/my_backup/snapshot_1/_status"
```

输出类似于以下内容：

```java
{
  "snapshots": [
    {
      "snapshot": "snapshot_1",
      "repository": "my_backup",
      "uuid": "XuBo4l4ISYiVg0nYUen9zg",
      "state": "SUCCESS",
      "include_global_state": true,
      "shards_stats": {
        "initializing": 0,
        "started": 0,
        "finalizing": 0,
        "done": 5,
        "failed": 0,
        "total": 5
      },
      "stats": {
        "incremental": {
          "file_count": 8,
          "size_in_bytes": 4704
        },
        "processed": {
          "file_count": 7,
          "size_in_bytes": 4254
        },
        "total": {
          "file_count": 8,
          "size_in_bytes": 4704
        },
        "start_time_in_millis": 1526280280355,
        "time_in_millis": 358,

        "number_of_files": 8,
        "processed_files": 8,
        "total_size_in_bytes": 4704,
        "processed_size_in_bytes": 4704
      }
    }
  ]
}
```

输出由不同的部分组成。`stats`子对象提供了有关快照文件的数量和大小的详细信息。由于快照是增量的，只复制存储库中不存在的 Lucene 段，`stats`对象包含快照引用的所有文件的总节，以及作为增量快照一部分实际需要复制的文件的增量节。在快照仍在进行的情况下，还有一个已处理的部分，其中包含有关正在复制的文件的信息。

- **注意**：由于与旧版`5.x`和`6.x`版本的向后兼容性原因，属性`number_of_files`、`processed_files`、`total_size_in_bytes`和`processed_size_in_bytes`，这些字段将在 Elasticsearch `v7.0.0` 中删除。

还支持以多个快照 ID 来查询快照的信息，如：

```java
curl -X GET "localhost:9200/_snapshot/my_backup/snapshot_1,snapshot_2/_status"
```

## 监视快照/还原进度

有几种方法可以监视快照的进度，并在进程运行时还原进程。这两个操作都支持`wait_for_completion`参数，该参数将阻塞客户端，直到操作完成。这是一种最简单的方法，可以用来获得有关操作完成的通知。

还可以通过定期调用快照信息来监视快照操作：

```java
curl -X GET "localhost:9200/_snapshot/my_backup/snapshot_1"
```

请注意，快照信息操作使用的资源和线程池与快照操作相同。因此，在快照大分片时执行快照信息操作可能会导致快照信息操作在返回结果之前等待可用资源。对于非常大的分片，等待时间可能很长。

要获得关于快照的更直接和完整的信息，可以使用快照状态命令：

```java
curl -X GET "localhost:9200/_snapshot/my_backup/snapshot_1/_status"
```

虽然快照信息方法只返回有关正在进行的快照的基本信息，但快照状态返回参与快照的每个分片的当前状态的完整细分。

还原过程依附于 Elasticsearch 的标准还原机制。因此，可以使用标准还原监视服务来监视还原状态。执行还原操作时，群集通常进入红色状态。发生这种情况是因为还原操作从还原索引的“还原”主分片开始的。在此操作过程中，主分片将变得不可用，并显示为红色群集状态。一旦完成主分片的还原，Elasticsearch 将切换到标准复制过程，此时将创建所需数量的副本，集群将切换到黄色状态。一旦创建了所有必需的副本，集群就切换到绿色状态。

群集运行状况操作仅提供还原进程的高级状态。通过使用「[索引还原](https://www.elastic.co/guide/en/elasticsearch/reference/6.6/indices-recovery.html)」和「[cat 还原](https://www.elastic.co/guide/en/elasticsearch/reference/6.6/cat-recovery.html)」的 API，可以更详细地了解还原过程的当前状态。

## 停止当前运行的快照和还原操作

快照和还原框架一次只允许运行一个快照或一个还原操作。如果当前运行的快照是错误执行的，或者花费了异常长的时间，则可以使用快照删除操作终止快照。快照删除操作检查删除的快照当前是否正在运行，如果正在运行，则删除操作将在从存储库中删除快照数据之前停止该快照。

```java
curl -X DELETE "localhost:9200/_snapshot/my_backup/snapshot_1"
```

还原操作使用标准的分片还原机制。因此，可以通过删除正在还原的索引来取消当前正在运行的还原操作。请注意，此操作将从群集中删除所有已删除索引的数据。

## 集群块对快照和恢复操作的影响

许多快照和还原操作都受集群和索引块的影响。例如，注册和注销存储库需要对全局元数据进行写访问。快照操作要求所有索引及其元数据以及全局元数据都是可读的。还原操作要求全局元数据可写，但是在还原过程中忽略索引级块，因为在还原过程中基本上重新创建了索引。请注意，存储库内容不是集群的一部分，因此集群块不会影响内部存储库操作，如从已注册的存储库中列出或删除快照。



----------

**英文原文链接**：[Elasticsearch Reference [6.6] » Modules » Snapshot And Restore](https://www.elastic.co/guide/en/elasticsearch/reference/6.6/modules-snapshots.html).

