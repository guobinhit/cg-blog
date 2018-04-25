# 史上最简单的 MyBatis 教程（三）「映射文件及调用方式」
## 1 前言


在 [史上最简单的 MyBatis 教程（一、二）](https://github.com/guobinhit/mybatis-tutorial/blob/master/README.md)中，咱们已经初步体验了 MyBatis 框架的一些优秀的特性，例如在映射文件中书写自定义的 SQL 语句以及便捷的调用方式等等。为了能够更好的掌握 MyBatis 框架的知识点，在本文中，咱们一起总结一下前两篇文章的内容。

## 2 总结


### 2.1 映射文件

在此，咱们仅以前两篇文章中的代码为例，给出映射文件 Mapper.xml 的总结：

**① 设置 namespace 命名空间，目的是为了区分映射文件中的方法；**

**② 结果集 resultMap 是 MyBatis 最大的特色，对象的 ORM 就由其来转换：**

 - 在结果集中，包括主键 id 和普通属性 result；
 - 在结果集中，常用的两个属性分别为：property，表示实体的属性；column，表示 SQL 查询的结果集的列。

**③ 在映射文件中，常用的标签有四个，分别为： select、insert、update 和 delete：**

 - 每个标签中都有 id 属性，在同一个 mapper 文件中 id 不允许重复；
 - 参数 parameterMap 已经被废弃，现在其存在的目的就是为了兼容前期的项目；
 - 参数 parameterType 支持很多的类型，例如 int、Integer、String、Double、List、Map 或者实体对象等；
 - 返回值 resultType 用于简单的类型；
 - 返回值 resultMap 用于复杂的类型；
 - 当参数和返回值是集合的时候，其声明的是集合中的元素类型；
 - SQL 语句不区分大小写，它默认使用 PrepareStatement 预编译，可以防止 SQL 注入。

**④ 获取参数的方法为` #{ 字段名 }`** 


### 2.2 调用方式


**① 获取 SqlSessionFactory**  

```
/*
* 其中，inputStream 为输入流
*/
SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream );
```

**② 获取 SqlSession**

```
SqlSession sqlSession = sqlSessionFactory.openSession(); 
```

**③ 查询所有记录**

```
/*
* 其中，“yeepay.payplus/mapper.UserMapper.findAll”，表示命名空间 + id
*/
List<Person> personList = sqlSession.selectList("yeepay.payplus/mapper.UserMapper.findAll");
```
**④ 查询单条记录**

```
Person p = sqlSession.selectOne("yeepay.payplus.mapper.UserMapper.get", 2);
```

**⑤ 新增记录**

```
sqlSession.insert("yeepay.payplus.mapper.UserMapper.insert", p);
```

**⑥ 修改记录**

```
sqlSession.insert("yeepay.payplus.mapper.UserMapper.update", p);
```

**⑦ 删除记录**

```
sqlSession.delete("yeepay.payplus.mapper.UserMapper.deleteById", 2);
```


**⑧ 简化扩展**

 - 类调用 Mapper 文件中的方法比较随意，就算将 delete 修改为 insert，照样执行删除功能，因为其是由命名空间和 id 共同决定的；
 - 命名空间可以简化，随意命名，只要保证项目中没有同命名空间和 id 即可。

**⑨ Sql标签** 

当多处调用相同的字段时，可以使用 Sql 标签，完成底层的字符串拼接，例如：

```
<!-- 定义 Sql 标签 -->
<sql id="cols">
	id,name,age
</sql>

<!-- 使用示例 -->
SELECT <include refid="cols"/> FROM person
```
**⑩ 赋别名**

在 Mapper.xml 文件中可以简写调用别名，例如：

```
<insert id="insert" parameterType="Person">
```
当然，提前需要我们在 sqlMapConfig.xml 中先定义别名，在这里，特别需要注意标签的顺序，如果标签的顺序出错，程序就会报错：

```
<!-- 赋别名 -->
<typeAliases>
    <typeAlias type="yeepay.payplus.Person" alias="Person"/>
</typeAliases>
```
----------
———— ☆☆☆ —— [返回 -> 史上最简单的 MyBatis 教程 <- 目录](https://github.com/guobinhit/mybatis-tutorial/blob/master/README.md) —— ☆☆☆ ————


















