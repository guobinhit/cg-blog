# 分库分表？如何做到永不迁移数据和避免热点？

## 1 前言

中大型项目中，一旦遇到数据量比较大，小伙伴应该都知道就应该对数据进行拆分了。有垂直和水平两种。

垂直拆分比较简单，也就是本来一个数据库，数据量大之后，从业务角度进行拆分多个库。如下图，独立的拆分出订单库和用户库。

![在这里插入图片描述](https://img-blog.csdnimg.cn/2020042719353087.png)

水平拆分的概念，是同一个业务数据量大之后，进行水平拆分。

![在这里插入图片描述](https://img-blog.csdnimg.cn/20200427193533228.png)

上图中订单数据达到了 4000 万，我们也知道 MySQL 单表存储量推荐是 500 万，如果不进行处理，MySQL 单表数据太大，会导致性能变慢。使用方案可以参考数据进行水平拆分。把 4000 万数据拆分 4 张表或者更多。当然也可以分库，再分表；把压力从数据库层级分开。

## 2 分库分表方案

分库分表方案中有常用的方案，hash 取模和 range 范围方案；分库分表方案最主要就是路由算法，把路由的 key 按照指定的算法进行路由存放。下边来介绍一下两个方案的特点。

### 2.1 hash 取模方案

![在这里插入图片描述](https://img-blog.csdnimg.cn/20200427193533325.png)

在我们设计系统之前，可以先预估一下大概这几年的订单量，如：4000 万。每张表我们可以容纳 1000 万，也我们可以设计 4 张表进行存储。

那具体如何路由存储的呢？hash 的方案就是对指定的路由 key（例如：id）对分表总数进行取模，上图中，id = 12 的订单，对 4 进行取模，也就是会得到 0，那此订单会放到 0 表中。id = 13 的订单，取模得到为 1，就会放到 1 表中。为什么对 4 取模，是因为分表总数是 4。

- **优点**：订单数据可以均匀的放到那 4 张表中，这样此订单进行操作时，就不会有热点问题。
 
 热点的含义：热点的意思就是对订单进行操作集中到 1 个表中，其他表的操作很少。

订单有个特点就是时间属性，一般用户操作订单数据，都会集中到这段时间产生的订单。如果这段时间产生的订单 都在同一张订单表中，那就会形成热点，那张表的压力会比较大。

- **缺点**：将来的数据迁移和扩容，会很难。

如：业务发展很好，订单量很大，超出了 4000 万的量，那我们就需要增加分表数。如果我们增加 4 个表。

![在这里插入图片描述](https://img-blog.csdnimg.cn/20200427193533291.png)

一旦我们增加了分表的总数，取模的基数就会变成 8，以前 id = 12 的订单按照此方案就会到 4 表中查询，但之前的此订单时在 0 表的，这样就导致了数据查不到。就是因为取模的基数产生了变化。

遇到这个情况，我们小伙伴想到的方案就是做数据迁移，把之前的 4000 万数据，重新做一个 hash 方案，放到新的规划分表中。也就是我们要做数据迁移。这个是很痛苦的事情。有些小公司可以接受晚上停机迁移，但大公司是不允许停机做数据迁移的。

当然做数据迁移可以结合自己的公司的业务，做一个工具进行，不过也带来了很多工作量，每次扩容都要做数据迁移。

那有没有不需要做数据迁移的方案呢？我们看下面的方案：

### 2.2 range 范围方案

range 方案也就是以范围进行拆分数据。

![在这里插入图片描述](https://img-blog.csdnimg.cn/20200427193533262.png)

range 方案比较简单，就是把一定范围内的订单，存放到一个表中；如上图 id = 12 放到 0 表中，id = 1300 万的放到 1 表中。设计这个方案时就是前期把表的范围设计好。通过 id 进行路由存放。

- **优点**：我们小伙伴们想一下，此方案是不是有利于将来的扩容，不需要做数据迁移。即时再增加 4 张表，之前的 4 张表的范围不需要改变，id = 12 的还是在 0 表，id = 1300 万的还是在 1 表，新增的 4 张表他们的范围肯定是大于 4000 万之后的范围划分的。

- **缺点**：有热点问题，我们想一下，因为 id 的值会一直递增变大，那这段时间的订单是不是会一直在某一张表中，如 id = 1000 万 ～ id = 2000 万之间，这段时间产生的订单是不是都会集中到此张表中，这个就导致 1 表过热，压力过大，而其他的表没有什么压力。

### 2.3 总结

- **hash 取模方案**：没有热点问题，但扩容迁移数据痛苦。
- **range方案**：不需要迁移数据，但有热点问题。

那有什么方案可以做到两者的优点结合呢？即不需要迁移数据，又能解决数据热点的问题呢？

其实还有一个现实需求，能否根据服务器的性能以及存储高低，适当均匀调整存储呢?

![在这里插入图片描述](https://img-blog.csdnimg.cn/2020042719353337.png)

## 3 方案思路

hash 是可以解决数据均匀的问题，range 可以解决数据迁移问题，那我们可以不可以两者相结合呢？利用这两者的特性呢？

我们考虑一下数据的扩容代表着，路由 key（例如：id）的值变大了，这个是一定的，那我们先保证数据变大的时候，首先用 range 方案让数据落地到一个范围里面。这样以后 id 再变大，那以前的数据是不需要迁移的。

但又要考虑到数据均匀，那是不是可以在一定的范围内数据均匀的呢？因为我们每次的扩容肯定会事先设计好这次扩容的范围大小，我们只要保证这次的范围内的数据均匀是不是就 ok 了。

![在这里插入图片描述](https://img-blog.csdnimg.cn/20200427193530148.png)

## 4 方案设计

我们先定义一个 group 组概念，这组里面包含了一些分库以及分表，如下图：

![在这里插入图片描述](https://img-blog.csdnimg.cn/20200427193534425.png)

上图有几个关键点：

1. id = 0 ～ 4000 万肯定落到 group01 组中。
2. group01 组有 3 个 DB，那一个 id 如何路由到哪个 DB？
3. 根据 hash 取模定位 DB，那模数为多少？模数要为所有此 group 组 DB 中的表数，上图总表数为 10。为什么要去模表的总数？而不是 DB 总数 3 呢？
4. 如 id = 12，id % 10 = 2，那值为 2，落到哪个 DB 库呢？这是设计是前期设定好的，那怎么设定的呢？
5. 一旦设计定位哪个 DB 后，就需要确定落到 DB 中的哪张表呢?

![在这里插入图片描述](https://img-blog.csdnimg.cn/2020042719353385.png)

## 5 核心主流程

![在这里插入图片描述](https://img-blog.csdnimg.cn/20200427193533229.png)

按照上面的流程，我们就可以根据此规则，定位一个 id，我们看看有没有避免热点问题。

我们看一下，id 在【0，1000 万】范围内的，根据上面的流程设计，1000 万以内的 id 都均匀的分配到 DB_0，DB_1，DB_2 三个数据库中的 Table_0 表中，为什么可以均匀，因为我们用了 hash 的方案，对 10 进行取模。

上面我们也提了疑问，为什么对表的总数 10 取模，而不是 DB 的总数 3 进行取模？我们看一下为什么 DB_0 是 4 张表，其他两个 DB 是 3 张表?

在我们安排服务器时，有些服务器的性能高，存储高，就可以安排多存放些数据，有些性能低的就少放点数据。如果我们取模是按照 DB 总数 3，进行取模，那就代表着【0，4000 万】的数据是平均分配到 3 个 DB 中的，那就不能够实现按照服务器能力适当分配了。

按照 Table 总数 10 就能够达到，看如何达到：

![在这里插入图片描述](https://img-blog.csdnimg.cn/20200427193534489.png)

上图中我们对 10 进行取模，如果值为【0，1，2，3】就路由到 DB_0，【4，5，6】路由到DB_1，【7，8，9】路由到 DB_2。现在小伙伴们有没有理解，这样的设计就可以把多一点的数据放到 DB_0 中，其他 2 个 DB 数据量就可以少一点。DB_0 承担了 4/10 的数据量，DB_1 承担了 3/10 的数据量，DB_2 也承担了 3/10 的数据量。整个 Group01 承担了【0，4000万】的数据量。

注意：小伙伴千万不要被 DB_1 或 DB_2 中 table 的范围也是【0，4000 万】疑惑了，这个是范围区间，也就是 id 在哪些范围内，落地到哪个表而已。

上面一大段的介绍，就解决了热点的问题，以及可以按照服务器指标，设计数据量的分配。

![在这里插入图片描述](https://img-blog.csdnimg.cn/20200427193533140.png)

## 6 如何扩容

其实上面设计思路理解了，扩容就已经出来了;那就是扩容的时候再设计一个 group02 组，定义好此 group 的数据范围就 ok 了。

![在这里插入图片描述](https://img-blog.csdnimg.cn/20200427193534558.png)

因为是新增的一个 group01 组，所以就没有什么数据迁移概念，完全是新增的 group 组，而且这个 group 组照样就防止了热点，也就是【4000 万，5500 万】的数据，都均匀分配到三个 DB 的 table_0 表中，【5500 万～7000 万】数据均匀分配到 table_1 表中。

## 7 系统设计

![在这里插入图片描述](https://img-blog.csdnimg.cn/2020042719353372.png)

思路确定了，设计是比较简单的，就 3 张表，把 group、DB 和 table 之间建立好关联关系就行了。

- group 和 DB 的关系

![在这里插入图片描述](https://img-blog.csdnimg.cn/20200427193534458.png)

- table 和 DB 的关系

![在这里插入图片描述](https://img-blog.csdnimg.cn/20200427193534576.png)

上面的表关联其实是比较简单的，只要原理思路理顺了，就 ok 了。小伙伴们在开发的时候不要每次都去查询三张关联表，可以保存到缓存中（本地 JVM 缓存），这样不会影响性能。

![在这里插入图片描述](https://img-blog.csdnimg.cn/20200427193532977.png)

一旦需要扩容，小伙伴是不是要增加一下 group02 关联关系，那应用服务需要重新启动吗？

简单点的话，就凌晨配置，重启应用服务就行了。但如果是大型公司，是不允许的，因为凌晨也有订单的。那怎么办呢？本地 JVM 缓存怎么更新呢？

其实方案也很多，可以使用 Zookeeper，也可以使用分布式配置，这里是比较推荐使用分布式配置中心的，可以将这些数据配置到分布式配置中心去。

到此为止，整体的方案介绍结束，希望对小伙伴们有所帮助。谢谢！！！
