# 史上最简单的 Spring MVC 教程（四）「注解示例」

1 前言
----

在前面的三篇博客中，咱们已经初步搭建起了 Spring MVC 框架，并依次介绍了 Spring MVC 框架的处理器映射（HandlerMapping）和控制器（Controller），但咱们也说了，在 Spring 框架体系升级到 Spring 3.0 之后，推荐提供大家使用注解功能，而不用再去继承不同的控制器父类，以及在 XML 文件中配置那么多东西啦！注解已经帮我们解决上述的麻烦啦，那么，就让我们一起体验 Spring MVC 框架的注解功能的方便快捷之处吧！

2 注解示例
------

在这里，咱们首先介绍一下注解方式的开发步骤：

 - 新建项目；
 - 导入 jar 包；
 - 创建 Controller，用注解方式进行声明；
 - 在`web.xml`文件中配置核心分发器 DispatcherServlet；
 - 创建一个`springmvc-servlet.xml`文件，配置注解开发方式及视图解析器；
 - 创建 JSP 页面，用于显示数据。

在完成以上步骤后，项目结构图如下所示：

![strue](http://img.blog.csdn.net/20170625173254002)

**第 1 步**：新建项目、导入 jar 包，创建 Controller

```
package com.hit.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by 维C果糖 on 2017/6/25.
 */

@Controller
public class TestController {

    @RequestMapping("/ceshi")        // 请求映射 http://localhost:8080/springmvc-annotation/ceshi.action
    public String goCeshi(HttpServletRequest request) {
        System.out.println(request.getRequestURL());  // 输出请求 URL 路径
        return "index";             // 返回逻辑名
    }
}
```
**第 2 步**：在`web.xml`文件中配置核心分发器 DispatcherServlet

```
<?xml version="1.0" encoding="UTF-8"?>
<web-app version="2.5" xmlns="http://java.sun.com/xml/ns/javaee"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://java.sun.com/xml/ns/javaee
	http://java.sun.com/xml/ns/javaee/web-app_2_5.xsd">

    <!-- 配置 DispatcherServlet，对所有后缀为action的url进行过滤 -->
    <servlet>
        <servlet-name>action</servlet-name>
        <servlet-class>org.springframework.web.servlet.DispatcherServlet</servlet-class>
        <!-- 修改 Spring MVC 配置文件的位置和名称 -->
        <init-param>
            <param-name>contextConfigLocation</param-name>
            <param-value>classpath:springmvc-servlet.xml</param-value>
        </init-param>
    </servlet>
    
    <servlet-mapping>
        <servlet-name>action</servlet-name>
        <url-pattern>*.action</url-pattern>
    </servlet-mapping>
    
    <welcome-file-list> 
        <welcome-file>index.jsp</welcome-file>
    </welcome-file-list>

</web-app>
```
**第 3 步**：创建一个`springmvc-servlet.xml`文件，配置注解开发方式及视图解析器

```
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:mvc="http://www.springframework.org/schema/mvc"
       xmlns:context="http://www.springframework.org/schema/context"
       xsi:schemaLocation="http://www.springframework.org/schema/beans
						http://www.springframework.org/schema/beans/spring-beans-3.2.xsd
						http://www.springframework.org/schema/mvc
						http://www.springframework.org/schema/mvc/spring-mvc-3.2.xsd
						http://www.springframework.org/schema/context
						http://www.springframework.org/schema/context/spring-context-3.2.xsd ">

    <!-- 声明注解开发方式 -->
    <mvc:annotation-driven/>

    <!-- 包自动扫描 -->
    <context:component-scan base-package="com.hit.controller"/>

    <!-- 内部资源视图解析器，前缀 + 逻辑名 + 后缀 -->
    <bean id="internalResourceViewResolver" class="org.springframework.web.servlet.view.InternalResourceViewResolver">
        <property name="prefix" value="/WEB-INF/pages/"/>
        <property name="suffix" value=".jsp"/>
    </bean>
</beans>
```
**第 4 步**：创建 JSP 页面，用于显示数据

```
<%--
  Created by IntelliJ IDEA.
  User: 维C果糖
  Date: 2017/6/25
  Time: 17:13
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
  <head>
    <meta http-equiv="content-type" content="text/html; charset=UTF-8">
    <title>Spring MVC</title>
  </head>
  <body>
  This is my Spring MVC!
  </body>
</html>

```
接下来，启动 tomcat 服务器，在 Chrome 浏览器中访问`http://localhost:8080/springmvc-annotation/ceshi.action`，将会显然如下页面：

![测试结果](http://img.blog.csdn.net/20170126134547933)

至此，运用注解方式进行开发体验完成。最后，有两点值得注意：

**第一点**：如果我们在 TestController 类中的 goCeshi 方法中定义参数类型为 HttpServletRequest 时，提示未找到该类，我们可以通过如下地址「[javax.servlet.jar包  ](http://download.csdn.net/download/qq_35246620/9740045)」下载相关的Servlet jar 包，再把其加载到项目中，或者直接添加“Java EE 6”运行环境依赖即可解决该问题。

**第二点**：如果在启动 tomcat 服务器并访问`http://localhost:8080/springmvc-annotation/ceshi.action`后，页面报出  HTTP Status 500 - Servlet.init() for servlet springmvc threw exception 这个异常，我们可以通过阅读「[出现 HTTP Status 500 - Servlet.init() for servlet springmvc threw exception 异常的原因及解决方法](http://blog.csdn.net/qq_35246620/article/details/54745098)」来查看解决方法。

----------
———— ☆☆☆ —— [返回 -> 史上最简单的 Spring MVC 教程 <- 目录](https://github.com/guobinhit/springmvc-tutorial/blob/master/README.md) —— ☆☆☆ ————
