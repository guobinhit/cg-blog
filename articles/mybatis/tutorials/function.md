# 史上最简单的 MyBatis 教程（二）「演示 · 增删改查」

## 1 前言


在 [史上最简单的 MyBatis 教程（一）](https://github.com/guobinhit/mybatis-tutorial/blob/master/experience-mybatis.md)中，咱们已经初步搭建了 MyBatis 框架，实现了查询所有记录的功能，并用 JUnit 进行了单元测试。接下来，咱们继续体验 MyBatis 框架，并实现添加、修改和删除等三个功能。

## 2 示例

老规矩，首先给出项目结构图：

![项目结构图](http://img.blog.csdn.net/20170202110143909)

在实现以上三个功能的时候，咱们需要修改的地方其实并不多，只需要修改两个地方就可以啦，分别是映射文件 UserMapper.xml 和测试类 CeshiMyBatis.

### 2.1 添加

**第一步**：在映射文件 UserMapper.xml 中添加 insert 的 SQL 语句

```
<!-- 新增功能，在SQL语句中有参数，并以实体来封装参数 -->
<insert id="insert" parameterType="yeepay.payplus.Person">
    INSERT INTO person (id,name,age) VALUES (#{id},#{name},#{age})
</insert>
```
**第二步**：在测试类 CeshiMyBatis 中添加 testInsert 方法

```
@Test
public void testInsert(){
    //定位核心配置文件
    String resource = "sqlMapConfig.xml";			
    InputStream inputStream = Resources.getResourceAsStream(resource);
    // 创建 SqlSessionFactory
    SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);		

    //获取到 SqlSession
    SqlSession sqlSession = sqlSessionFactory.openSession();			

    Person p = new Person();
    p.setId(5);
    p.setName("gavin");
    p.setAge(12);
    sqlSession.insert("yeepay.payplus.mapper.UserMapper.insert", p);
    //默认是不自动提交，必须手工提交
    sqlSession.commit();			
}
```

### 2.2 修改

**第一步**：在映射文件 UserMapper.xml 中添加 update 的 SQL 语句

```
<!-- 修改功能 -->
<update id="update" parameterType="yeepay.payplus.Person">
    UPDATE person set name=#{name},age=#{age}
    WHERE id = #{id}
</update>
```
**第二步**：在测试类 CeshiMyBatis 中添加 testUpdate 方法

```
@Test
public void testUpdate(){
    //定位核心配置文件
    String resource = "sqlMapConfig.xml";			
    InputStream inputStream = Resources.getResourceAsStream(resource);
    // 创建 SqlSessionFactory
    SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);	
    
    // 获取到 SqlSession
    SqlSession sqlSession = sqlSessionFactory.openSession();	
    
    // 获得 id=2 的记录
    Person p = sqlSession.selectOne("yeepay.payplus.mapper.UserMapper.get", 2);   
    p.setName("jane");
    p.setAge(16);

    // sqlSession.insert("yeepay.payplus.mapper.UserMapper.update", p);
    sqlSession.update("yeepay.payplus.mapper.UserMapper.update", p);
    //默认是不自动提交，必须手工提交
    sqlSession.commit();			
}
```

### 2.3 删除

**第一步**：在映射文件 UserMapper.xml 中添加 delete 的 SQL 语句

```
<!-- 删除功能 -->
<delete id="deleteById" parameterType="integer">
    DELETE FROM person
    WHERE id = #{id}
</delete>
```
**第二步**：在测试类 CeshiMyBatis 中添加 testDeleteById 方法

```
@Test
public void testDeleteById(){
    //定位核心配置文件
    String resource = "sqlMapConfig.xml";
    InputStream inputStream = Resources.getResourceAsStream(resource);
    // 创建 SqlSessionFactory
    SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);		
    // 获取到 SqlSession 
    SqlSession sqlSession = sqlSessionFactory.openSession();			

    sqlSession.delete("yeepay.payplus.mapper.UserMapper.deleteById", 2);
    //默认是不自动提交，必须手工提交
    sqlSession.commit();			
}
```

## 3 完整代码


**第一部分**：核心配置文件 sqlMapConfig.xml 

```
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE configuration
        PUBLIC "-//mybatis.org//DTD Config 3.0//EN"
        "http://mybatis.org/dtd/mybatis-3-config.dtd">

<configuration>
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
        <mapper resource="yeepay/payplus/mapper/UserMapper.xml"></mapper>
    </mappers>
</configuration>
```
**第二部分**：实体类 Person

```
package yeepay.payplus;

/**
 * Created by 维C果糖 on 2017/4/3.
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
**第三部分**：映射文件 UserMapper.xml 

```
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE mapper
        PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
        "http://mybatis.org/dtd/mybatis-3-mapper.dtd">

<mapper namespace="yeepay.payplus/mapper.UserMapper">   <!-- 命名空间，名字可以随意起，只要不冲突即可 -->
    <!-- 对象映射，可以不写 -->
    <resultMap type="yeepay.payplus.Person" id="personRM">
        <!-- property="id"，表示实体对象的属性；column="ID"，表示结果集字段 -->
        <id property="id" column="ID"/>
        <result property="Name" column="NAME"/>
        <result property="age" column="AGE"/>
    </resultMap>

    <!-- 查询功能，resultType 设置返回值类型 -->
    <select id="findAll" resultType="yeepay.payplus.Person">  <!-- 书写 SQL 语句 -->
        SELECT * FROM person
    </select>

    <!-- 通过 ID 查询 -->
    <select id="get" parameterType="Integer" resultMap="personRM">  <!-- 书写 SQL 语句 -->
        SELECT * FROM person
        WHERE id = #{id}
    </select>

    <!-- 新增功能，在SQL语句中有参数，并以实体来封装参数 -->
    <insert id="insert" parameterType="yeepay.payplus.Person">
        INSERT INTO person (id,name,age) VALUES (#{id},#{name},#{age})
    </insert>

    <!-- 修改功能 -->
    <update id="update" parameterType="yeepay.payplus.Person">
        UPDATE person set name=#{name},age=#{age}
        WHERE id = #{id}
    </update>

    <!-- 删除功能 -->
    <delete id="deleteById" parameterType="integer">
        DELETE FROM person
        WHERE id = #{id}
    </delete>
</mapper>
```
**第四部分**：测试类 CeshiMyBatis

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
 * Created by 维C果糖 on 2017/4/3.
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

    @Test
    public void testInsert(){
        String resource = "sqlMapConfig.xml";			//定位核心配置文件
        InputStream inputStream = Resources.getResourceAsStream(resource);
        SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);		// 创建 SqlSessionFactory

        SqlSession sqlSession = sqlSessionFactory.openSession();			//获取到 SqlSession

        Person p = new Person();
        p.setId(5);
        p.setName("gavin");
        p.setAge(12);

        sqlSession.insert("yeepay.payplus.mapper.UserMapper.insert", p);
        sqlSession.commit();			//默认是不自动提交，必须手工提交
    }

    @Test
    public void testUpdate(){
        String resource = "sqlMapConfig.xml";			//定位核心配置文件
        InputStream inputStream = Resources.getResourceAsStream(resource);
        SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);		// 创建 SqlSessionFactory

        SqlSession sqlSession = sqlSessionFactory.openSession();			// 获取到 SqlSession

        Person p = sqlSession.selectOne("yeepay.payplus.mapper.UserMapper.get", 2);   // 获得 id=2 的记录
        p.setName("jane");
        p.setAge(16);

        sqlSession.insert("yeepay.payplus.mapper.UserMapper.update", p);
        sqlSession.commit();			//默认是不自动提交，必须手工提交
    }

    @Test
    public void testDeleteById(){
        String resource = "sqlMapConfig.xml";			//定位核心配置文件
        InputStream inputStream = Resources.getResourceAsStream(resource);
        SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);		// 创建 SqlSessionFactory

        SqlSession sqlSession = sqlSessionFactory.openSession();			// 获取到 SqlSession

        sqlSession.delete("yeepay.payplus.mapper.UserMapper.deleteById", 2);
        sqlSession.commit();			//默认是不自动提交，必须手工提交
    }
}
```

----------
———— ☆☆☆ —— [返回 -> 史上最简单的 MyBatis 教程 <- 目录](https://github.com/guobinhit/mybatis-tutorial/blob/master/README.md) —— ☆☆☆ ————

