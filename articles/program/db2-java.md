# 详述 DB2 分页查询及 Java 实现

 > **博主说**：有时候，我们需要对数据库中现有的数据进行大量处理操作（例如表中的某个字段需要全部更新等），如果直接使用`select * from tableName`很容易出现问题，因此我们可以选择分页查询，批量处理数据。


DB2
---

 - startNum：起始数
 - endNum：结尾数

 **SQL 语句** 

```
SELECT * FROM 
(
SELECT B.*, ROWNUMBER() OVER() AS TN FROM   
(
SELECT * FROM 表名
) AS B
) AS A 
WHERE A.TN BETWEEN startNum AND endNum; 
```

如上所示，此即为 DB2 的分页查询语句。

Mapper
---------

```
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd" >
<mapper namespace="com.hit.store.dao.StoreEntityDao" >
  <resultMap id="BaseResultMap" type="StoreEntity" >
    <id column="ID" property="id" jdbcType="BIGINT" />
    <result column="CREATE_TIME" property="createTime" jdbcType="TIMESTAMP" />
    <result column="OWNER" property="owner" jdbcType="VARCHAR" />
    <result column="DESCRIPTION" property="description" jdbcType="VARCHAR" />
  </resultMap>

  <select id="query4encrypt" parameterType="Map" resultMap="BaseResultMap">
    <!--- 在映射文件中 SQL 语句末尾不应该加分号，防止解析错误 --->
	SELECT * FROM 
	(
	SELECT B.*, ROWNUMBER() OVER() AS TN FROM   
	(
	SELECT * FROM TBL_STORE
	) AS B
	) AS A 
	WHERE A.TN BETWEEN #{startNum} AND #{endNum}
  </select>
</mapper>

```

Java
-------

```
/**
 * Dao 层代码
 */
@Repository("storeEntityDao")
public interface StoreEntityDao {
    List<StoreEntity> query4encrypt(Map<String, Object> paramMap);
}

/**
 * Service 层接口代码
 */
public interface StoreEntityService {
    public void query4encrypt();
}

/**
 * Service 层实现代码
 */
@Service("storeEntityService")
public interface StoreEntityServiceImpl implements StoreEntityService {
     @Override
    public void query4encrypt() {
        boolean flag = true;
        Long startNum = 0L;
        Long endNum = 0L;
        Map<String, Object> paramMap = new HashMap<String, Object>();
        while (flag) {
            endNum = startNum + 100;
            paramMap.put("startNum", startNum);
            paramMap.put("endNum", endNum);
            List<StoreEntity> storeEntityList = StoreEntityDao.query4encrypt(paramMap);
            if (storeEntityList != null && storeEntityList.size() > 0) {
                // 遍历加密数据
                for (StoreEntity storeEntity : storeEntityList) {
                    // 加密及持久化处理
                }
            }
            if (storeEntityList != null && storeEntityList.size() >= 100) {
                startNum = endNum++;
            } else {
                flag = false;
            }
        }
    }
}
```
至此，我们模拟了数据库映射 Mapper.xml 文件、Dao 层和 Service 层，并在 Mapper.xml 中书写了分页查询 SQL 语句。特别地，在 Service 的实现层中，我们实现了具体的分页查询操作，并在其中批量处理数据。
