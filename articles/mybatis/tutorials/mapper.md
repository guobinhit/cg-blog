# 史上最简单的 MyBatis 教程（五）「关联映射」

1 前言
----

在 [史上最简单的 MyBatis 教程（一、二、三、四）](https://github.com/guobinhit/mybatis-tutorial/blob/master/README.md)中，咱们已经把 MyBatis 框架的基本内容了解的差不多啦，然而美中不足的是：在前四篇文章的示例中，咱们仅仅演示了一对一`（1:1）`的映射关系，并没有演示一对多`（1:N）`的映射关系。因此，在本篇文章中，咱们就一起来看看 MyBatis 框架如何实现一对多的映射关系！

2 关联映射`(1:N)`
-----------
为了更好的演示一对多的映射关系，在原有的项目中，又新建了两个实体类以及一个映射文件，项目结构图发生了变化，因此先给出项目结构图：

![项目结构图](http://img.blog.csdn.net/20170307094001548)

**第一步**：在数据库中新建两个表，咱们以客户（customer）和订单（orders）为例

**customer 表结构**：

![customer1](http://img.blog.csdn.net/20170307095601384)

**customer 表内容**：

![customer2](http://img.blog.csdn.net/20170307095637046)

**orders 表结构**：

![orders1](http://img.blog.csdn.net/20170307095703937)

**orders 表内容**：

![orders2](http://img.blog.csdn.net/20170307095731306)

**第二步**：新建两个对应 customer 和 orders 表的实体类

**Customer 实体类**：

```
package yeepay.payplus.domain;

import java.util.List;

/**
 * Created by 维C果糖 on 2017/4/6.
 */
public class Customer {
    private List<Orders> orders;    // 关联多个订单
    private Integer id;
    private String name;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Orders> getOrders() {
        return orders;
    }

    public void setOrders(List<Orders> orders) {
        this.orders = orders;
    }
}
```
**Orders 实体类**：

```
package yeepay.payplus.domain;

/**
 * Created by 维C果糖 on 2017/4/6.
 */
public class Orders {
    private Integer id;
    private String sn;       // 订单编号
    private String remark;   // 订单描述

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getSn() {
        return sn;
    }

    public void setSn(String sn) {
        this.sn = sn;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
```
**第三步**：创建 CustomerMapper.xml 映射文件

```
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE mapper
        PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
        "http://mybatis.org/dtd/mybatis-3-mapper.dtd">

<mapper namespace="yeepay.payplus.mapper.CustomerMapper">
    <resultMap type="yeepay.payplus.domain.Customer" id="customerRM">
        <id property="id" column="ID"/>
        <result property="name" column="NAME"/>
    </resultMap>

    <!-- 配置关联关系 1:N -->
    <resultMap type="yeepay.payplus.domain.Customer" id="customerOrdersRM" extends="customerRM">
        <!-- 配置多的（N），property 属性就是实体中的 List 对象属性名称，ofType 属性就是集合元素的类型 -->
        <collection property="orders" ofType="yeepay.payplus.domain.Orders">
            <id property="id" column="ID"/>
            <result property="sn" column="SN"/>
            <result property="remark" column="REMARK"/>
        </collection>
    </resultMap>

    <!-- 查询，关联关系 Map 作为查询条件 -->
    <select id="find" parameterType="map" resultMap="customerOrdersRM">
        SELECT
            c.name,o.sn,o.remark
        FROM
            (SELECT id,name FROM customer) c
        LEFT JOIN
            (SELECT id,sn,remark,customer_id FROM orders) o
        ON c.id = o.customer_id
        WHERE c.name = #{customerName}
    </select>
</mapper>
```
在上面的映射文件中，咱们写了一个具有查询功能的 SQL 语句，但是程序还没有运行，咱们也不知道这个 SQL 语句写的是否正确，因此咱们可以先在数据库中试运行该 SQL 语句，看其是否能够实现多表关联的查询功能。运行示例及结果如下图所示：

![SQL](http://img.blog.csdn.net/20170307100815873)

由此可见，咱们写的 SQL 语句是正确的。

**第四步**：修改 sqlMapConfig.xml 配置文件

```
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE configuration
        PUBLIC "-//mybatis.org//DTD Config 3.0//EN"
        "http://mybatis.org/dtd/mybatis-3-config.dtd">

<configuration>

    <!-- 赋别名 -->
    <typeAliases>
        <typeAlias type="yeepay.payplus.domain.Person" alias="Person"/>
    </typeAliases>

    <!-- 配置开发环境，可以配置多个，在具体用时再做切换 -->
    <environments default="">
        <environment id="test">
            <transactionManager type="JDBC"></transactionManager>    <!-- 事务管理类型：JDBC、MANAGED -->
            <dataSource type="POOLED">    <!-- 数据源类型：POOLED、UNPOOLED、JNDI -->
                <property name="driver" value="com.mysql.jdbc.Driver" />
                <property name="url" value="jdbc:mysql://localhost:3306/test?characterEncoding=utf-8" />
                <property name="username" value="root" />
                <property name="password" value="root" />
            </dataSource>
        </environment>
    </environments>

    <!-- 加载映射文件 mapper -->
    <mappers>
        <!-- 路径用 斜线（/） 分割，而不是用 点(.) -->
        <mapper resource="yeepay/payplus/mapper/UserMapper.xml"/>
        <mapper resource="yeepay/payplus/mapper/CustomerMapper.xml"/>
    </mappers>
</configuration>
```
在此处，细心的童鞋估计会发现咱们也没有修改什么（与前四篇文章中使用的配置文件相比），仅仅是在配置文件中添加了一个`mapper`标签，把 CustomerMapper.xml 映射文件配置进来而已。
 
 **第五步**：创建 CeshiCustomer 测试类
 

```
package yeepay.payplus.test;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.Test;
import yeepay.payplus.domain.Customer;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by 维C果糖 on 2017/4/6.
 */
public class CeshiCustomer {
    @Test
    public void testFind() throws IOException {
        /**
         *  1、获得 SqlSessionFactory
         *  2、获得 SqlSession
         *  3、调用在 mapper 文件中配置的 SQL 语句
         */
        String resource = "sqlMapConfig.xml";           // 定位核心配置文件
        InputStream inputStream = Resources.getResourceAsStream(resource);
        SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);    // 创建 SqlSessionFactory

        SqlSession sqlSession = sqlSessionFactory.openSession();    // 获取到 SqlSession

        Map map = new HashMap();
        map.put("customerName", "charies");

        // 调用 mapper 中的方法：命名空间 + id
        List<Customer> customerList = sqlSession.selectList("yeepay.payplus.mapper.CustomerMapper.find", map);

        for (Customer c : customerList) {
            System.out.println(c);
        }
    }
}
```

至此，MyBatis 框架中的关联映射（一对多）演示完成。不过有一点需要大家特别注意，那就是：**在映射时，如果 SQL 查询语句出现了同名字段，那么 MyBatis 就会以第一个出现的字段为准，从而省略第二个同名字段的值，造成数据损失，因此在出现同名字段的时候，咱们可以保留第一个字段，再为其它同名字段分别起一个不同的别名来避免这种情况的发生。**

----------
———— ☆☆☆ —— [返回 -> 史上最简单的 MyBatis 教程 <- 目录](https://github.com/guobinhit/mybatis-tutorial/blob/master/README.md) —— ☆☆☆ ————
