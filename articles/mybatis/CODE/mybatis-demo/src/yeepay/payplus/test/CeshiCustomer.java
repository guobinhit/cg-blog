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