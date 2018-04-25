# 史上最简单的 MyBatis 教程（四）「动态 SQL 语句」

## 1 前言

在 [史上最简单的 MyBatis 教程（一、二、三）](https://github.com/guobinhit/mybatis-tutorial/blob/master/README.md)中，咱们已经初步体验了 MyBatis 框架的特性，尤其是其支持普通的 SQL 语句，但如果仔细阅读前三篇文章的示例，大家会发现一个问题，那就是到目前为止，咱们在映射文件 UserMapper.xml 中给出的 SQL 语句都非常的简单，不足以满足咱们个性化的需求。因此，在本篇文章中，咱们更进一步，研究一下 MyBatis 框架是如何支持动态 SQL 语句的，从而满足咱们个性化的需求。

## 2 动态SQL语句


### 2.1 查询

**第一步**：修改映射文件 UserMapper.xml 中的 select 语句

```
<!-- 查询功能，parameterType 设置参数类型，resultType 设置返回值类型 -->
<select id="findAll" parameterType="yeepay.payplus.Person" resultType="Person">  <!-- 书写 SQL 语句 -->
    SELECT id,name,age FROM person
    <where>
      <if test="name =! null">
        name = #{name}
      </if>
      <if test="age =! null">
        and age = #{age}
      </if>
    </where>
</select>
```


**第二步**：修改测试类 CeshiMyBatis 中的查询方法

```
@Test
public void testQuery() throws IOException {   // 查询记录
    /**
      *  1、获得 SqlSessionFactory
      *  2、获得 SqlSession
      *  3、调用在 mapper 文件中配置的 SQL 语句
      */
    String resource = "sqlMapConfig.xml";           // 定位核心配置文件
    InputStream inputStream = Resources.getResourceAsStream(resource);
    SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);    // 创建 SqlSessionFactory

    SqlSession sqlSession = sqlSessionFactory.openSession();    // 获取到 SqlSession

    Person findPerson = new Person();
    findPerson.setAge(18);

    // 调用 mapper 中的方法：命名空间 + id
    List<Person> personList = sqlSession.selectList("yeepay.payplus/mapper.UserMapper.findAll", findPerson);

    for (Person p : personList) {
      System.out.println(p);
    }
}
```

### 2.2 修改

**第一步**：修改映射文件 UserMapper.xml 中的 update 语句

```
<!-- 修改功能 -->
<update id="update" parameterType="yeepay.payplus.Person">
    UPDATE person
      <set>
        <if test="name =! null">
          name = #{name},
        </if>
        <if test="age =! null">
          age=#{age},
        </if>
      </set>
    WHERE id = #{id}
</update>
```
**第二步**：修改测试类 CeshiMyBatis 中的修改方法

```
@Test
public void testUpdate() {   // 修改方法
    String resource = "sqlMapConfig.xml";            //定位核心配置文件
    InputStream inputStream = Resources.getResourceAsStream(resource);
    SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);        // 创建 SqlSessionFactory

    SqlSession sqlSession = sqlSessionFactory.openSession();            // 获取到 SqlSession

    Person p = new Person();
    p.setId(1);
    p.setAge(12);

    sqlSession.insert("yeepay.payplus.mapper.UserMapper.update", p);
    sqlSession.commit();            //默认是不自动提交，必须手工提交
}
```

### 2.3 删除

在这里，给出三种完成批量删除功能的方法，参数类型分别为：Array、List 和 Map.

**第一种**：修改映射文件 UserMapper.xml 中的 delete 语句，参数类型为 Array

```
<!-- 批量删除，Array 类型 -->
<delete id="deleteArray" parameterType="integer">
    DELETE FROM person WHEN id IN
    <foreach collection="array" item="id" open="(" close=")" separator=",">
      #{id}
    </foreach>
</delete>
```
在测试类 CeshiMyBatis 中，新建批量删除方法 testDeleteArray

```
@Test
public void testDeleteArray() {   // 批量删除
    String resource = "sqlMapConfig.xml";            //定位核心配置文件
    InputStream inputStream = Resources.getResourceAsStream(resource);
    SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);        // 创建 SqlSessionFactory

    SqlSession sqlSession = sqlSessionFactory.openSession();            // 获取到 SqlSession

    sqlSession.delete("yeepay.payplus.mapper.UserMapper.deleteArray", new Integer[]{2, 3, 4});
    sqlSession.commit();            //默认是不自动提交，必须手工提交
}
```
**第二种**：修改映射文件 UserMapper.xml 中的 delete 语句，参数类型为 List

```
<!-- 批量删除，List 类型 -->
 <delete id="deleteList" parameterType="integer">
    DELETE FROM person WHEN id IN
    <foreach collection="list" item="id" open="(" close=")" separator=",">
      #{id}
    </foreach>
</delete>
```
在测试类 CeshiMyBatis 中，新建批量删除方法 testDeleteList

```
@Test
public void testDeleteList() {   // 批量删除
    String resource = "sqlMapConfig.xml";            //定位核心配置文件
    InputStream inputStream = Resources.getResourceAsStream(resource);
    SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);        // 创建 SqlSessionFactory

    SqlSession sqlSession = sqlSessionFactory.openSession();            // 获取到 SqlSession

    List<Integer> personList = new ArrayList<Integer>();

    personList.add(2);
    personList.add(3);
    personList.add(4);

    sqlSession.delete("yeepay.payplus.mapper.UserMapper.deleteList", personList);
    sqlSession.commit();            //默认是不自动提交，必须手工提交
}
```
**第三种**：修改映射文件 UserMapper.xml 中的 delete 语句，参数类型为 Map

```
<!-- 批量删除，Map 类型 -->
<delete id="deleteMap" parameterType="Map">
    DELETE FROM person WHERE id IN
    <foreach collection="ids" item="id" open="(" close=")" separator=",">
      #{id}
    </foreach>
      AND  age = #{age}
</delete>
```
在测试类 CeshiMyBatis 中，新建批量删除方法 testDeleteMap

```
@Test
public void testDeleteMap() {   // 批量删除
    String resource = "sqlMapConfig.xml";            //定位核心配置文件
    InputStream inputStream = Resources.getResourceAsStream(resource);
    SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);        // 创建 SqlSessionFactory

    SqlSession sqlSession = sqlSessionFactory.openSession();            // 获取到 SqlSession

    Map<String, Object> map = new HashMap<String, Object>();

    /**
      * 在通过以下两条语句测试本方法时，实际上仅删除了 id = 3、4、5 中 age = 18 的记录
      */
    map.put("ids", new Integer[]{2, 3, 4});
    map.put("age",18);

    sqlSession.delete("yeepay.payplus.mapper.UserMapper.deleteMap", map);
    sqlSession.commit();            //默认是不自动提交，必须手工提交
}
```

## 3 总结

在使用动态 SQL 语句的时候，咱们需要多注意以下几点：

 1. 通过 if 标签来判断字段是否为空，如果为空，则默认不参与到 SQL 语句中，并且可以自动省略逗号；
 2. 通过 where 标签来输出条件完成判断，其可以自动省略多余的 and 和 逗号；
 3. 通过 set 标签来完成修改操作，当字段值为 null 时，其不参与到 SQL 语句中；
 4. 在 foreach 标签中，collection 属性表示传入的参数集合， item 表示每个元素变量的名字，open 表示开始字符，close 表示结束字符，separator 表示分隔符；
 5. 任何参数都可以封装到 Map 中，其以 key 来取值。

此外，在本篇文章中，咱们测试示例的时候，使用的是 MySQL 数据库，但如果咱们毫无修改（当然，如果想使用 Oracle 数据库，怎么都得先加载 Oracle 数据库的驱动啊）的直接切换到 Oracle 数据库，同样测试以上功能的时候就会报出一个异常，即“**java.sql.SQLException：无效的列类型**”，究其原因：

**Oracle 数据库，在进行新增、修改操作时，如果字段值为 null，必须指定字段默认的类型。**

接下来，以 UserMapper.xml 文件中的修改语句为例，给出示例：

```
<!-- 在 Oracle 数据中，完成修改功能 -->
<update id="update" parameterType="yeepay.payplus.Person">
     UPDATE person
      <set>
        name = #{name,jdbcType=VARCHAR},
        age=#{age,jdbcType=INTEGER}
      </set>
        WHERE id = #{id}
</update>
```
其中，jdbcType 的类型为数据库中字段的类型，需要严格的对应。

----------
———— ☆☆☆ —— [返回 -> 史上最简单的 MyBatis 教程 <- 目录](https://github.com/guobinhit/mybatis-tutorial/blob/master/README.md) —— ☆☆☆ ————
