# 史上最简单的 MyBatis 教程（一）「框架初体验」

1 简介
----

MyBatis 是支持普通 SQL 查询，存储过程和高级映射的优秀持久层框架，其几乎消除了所有的 JDBC 代码和参数的手工设置以及结果集的检索。MyBatis 使用简单的 XML 或注解用于配置和原始映射，将接口和 Java 的 POJOs（Plain Old Java Objects，普通的 Java对象）映射成数据库中的记录。MyBatis 应用程序大都使用 SqlSessionFactory 实例，SqlSessionFactory 实例可以通过 SqlSessionFactoryBuilder 获得，而　SqlSessionFactoryBuilder 则可以从一个 XML 配置文件或者一个预定义的配置类的实例获得。


2 构建步骤
------

想要熟练的使用 MyBatis 框架，就必须明确其构建步骤，在此，咱们给出构建 MyBatis 框架的详细步骤，以供大家参考：

 - 创建一个 Java Web 项目；
 - 导入 MyBatis 框架的 jar 包；
 - 创建核心配置文件   sqlMapConfig.xml ；
 - 创建映射文件 UersMapper.xml；
 - 创建测试类。

其中，MyBatis 框架的 jar 包可以通过「[MyBatis 之 各种依赖包](http://download.csdn.net/detail/qq_35246620/9745924)」进行下载，而且里面包含了大多数常用的配置文件，值得大家 get.  此外，还有一点需要大家注意，那就是 MyBatis 框架用于操作数据，支持 SQL 语句，因此在体验 MyBatis 框架的时候，需要使用数据库配合进行测试。**在本项目中，咱们就需要在数据库中创建了一个名为`person`的表，并通过 MyBatis 框架对其进行一系列常见的操作（增、删、改、查等）。**

3 体验 MyBatis 框架
---------------
首先，给出项目结构图：

![项目结构图](http://img.blog.csdn.net/20170201212514729)


**第一步**：创建 Java Web 项目，导入 jar 包

```
mybatis-3.2.2.jar		                核心jar
mysql-connector-java-5.1.10-bin.jar		数据库访问
asm-3.3.1.jar		        		增强类
cglib-2.2.2.jar			          	动态代理
commons-logging-1.1.1.jar	                通用日志
javassist-3.17.1-GA.jar			        java助手
log4j-1.2.17.jar			        日志
slf4j-api-1.7.5.jar		  	        日志
slf4j-log4j12-1.7.5.jar		                日志
```
**第二步**：创建核心配置文件   sqlMapConfig.xml 

```
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE configuration
        PUBLIC "-//mybatis.org//DTD Config 3.0//EN"
        "http://mybatis.org/dtd/mybatis-3-config.dtd">

<configuration>
    <!-- 配置开发环境，可以配置多个，在具体用时再做切换 -->
    <environments default="test">
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
        <mapper resource="yeepay/payplus/mapper/UserMapper.xml"></mapper>
    </mappers>
</configuration>
```
**第三步**：创建映射文件 UersMapper.xml

```
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE mapper
        PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
        "http://mybatis.org/dtd/mybatis-3-mapper.dtd">

<mapper namespace="yeepay.payplus/mapper.UserMapper">   <!-- 命名空间，名字可以随意起，只要不冲突即可 -->
    <!-- 对象映射，可以不写 -->
    <!-- 查询功能，resultType 设置返回值类型 -->
    <select id="findAll" resultType="yeepay.payplus.Person">  <!-- 书写 SQL 语句 -->
        SELECT * FROM Person
    </select>
</mapper>
```
**第四步**：创建实体类 Person

```
package yeepay.payplus;

/**
 * Created by 维C果糖 on 2017/4/2.
 */
public class Person {
    private Integer id;
    private String name;
    private Integer age;

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

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    @Override
    public String toString() {
        return "Person{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", age=" + age +
                '}';
    }
}
```
**第五步**：创建测试类 CeshiMyBatis

```
package yeepay.payplus.test;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.Test;
import yeepay.payplus.Person;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Created by 维C果糖 on 2017/4/2.
 */
public class CeshiMyBatis {
    @Test
    public void ceshi() throws IOException {
        /**
         *  1、获得 SqlSessionFactory
         *  2、获得 SqlSession
         *  3、调用在 mapper 文件中配置的 SQL 语句
         */
        String resource = "sqlMapConfig.xml";           // 定位核心配置文件
        InputStream inputStream = Resources.getResourceAsStream(resource);
        SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);    // 创建 SqlSessionFactory

        SqlSession sqlSession = sqlSessionFactory.openSession();    // 获取到 SqlSession

        // 调用 mapper 中的方法：命名空间 + id
        List<Person> personList = sqlSession.selectList("yeepay.payplus/mapper.UserMapper.findAll");

        for (Person p : personList){
            System.out.println(p);
        }
    }
}
```
在完成以上的操作后，咱们可以通过 JUnit 来测试框架是否搭建成功，具体使用 JUnit 进行测试的方法，可以通过阅读「[基于 JUnit 单元测试的原理及示例](http://blog.csdn.net/qq_35246620/article/details/54620207)」来了解更为详细的内容。

----------
———— ☆☆☆ —— [返回 -> 史上最简单的 MyBatis 教程 <- 目录](https://github.com/guobinhit/mybatis-tutorial/blob/master/README.md) —— ☆☆☆ ————
