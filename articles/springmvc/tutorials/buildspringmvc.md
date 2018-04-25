# 史上最简单的 Spring MVC 教程（一）「框架初体验」

## 1 简介

　　Spring MVC 属于 SpringFrameWork 的后续产品，已经融合在 Spring Web Flow 里面。Spring 框架提供了构建 Web 应用程序的全功能 MVC 模块，而 Spring MVC 就是其中最优秀的 MVC 框架。自从 Spring 2.5 版本发布后，由于支持注解配置，易用性得到了大幅度的提高；Spring 3.0 更加完善，实现了对 Struts 2 的超越。从现阶段来看，Spring MVC 是当前应用最多的 MVC 框架，而且在很多公司，通常会把 Spring MVC 和 Mybatis 整合起来使用。

## 2 框架原理

　　在 Spring MVC 框架中，一个请求从开始到响应，需要经历的步骤为：从 Request（请求）开始，依次进入 DispatcherServlet（核心分发器） 、HandlerMapping（处理器映射）、Controller（控制器）、ModelAndView（模型和视图）、ViewResolver（视图解析器）、View（视图）和 Response（响应），其中DispatcherServlet、HandlerMapping 和 ViewResolver 只需要在 XML 文件中配置即可，从而大大提高了开发的效率，特别是对于 HandlerMapping 框架为其提供了默认的配置。Spring MVC 框架的结构图如下所示：

![SpringMVC](http://img.blog.csdn.net/20170207154527170)


## 3 搭建 Spring MVC 框架


　　首先，我们需要下载 Spring MVC 框架的各种依赖包，下载地址为「[Spring MVC 框架的各种依赖包](http://download.csdn.net/detail/qq_35246620/9743975)」；然后，创建 Java Web 项目，项目名可以随意取，在这里，我们不妨就取名为`springmvc`，构建项目结构图如下：

![projectStructure](http://img.blog.csdn.net/20170821162147434)

接下来，在`External Libraries`中导入 Spring MVC 框架的相关依赖包，具体 jar 包的导入方法可以参考「[详述 IntelliJ IDEA 之 添加 jar 包](http://blog.csdn.net/qq_35246620/article/details/54705071)」。至于需要导入的 jar 包，在我们之前下载的「[Spring MVC 框架的各种依赖包](http://download.csdn.net/detail/qq_35246620/9743975)」中都可以找到，下面附上需要导入的 jar 名称：

```
spring-aop-3.2.2.jar			          		AOP
spring-aspects-3.2.2.jar					AOP
spring-beans-3.2.2.jar						核心包
spring-context-3.2.2.jar					扩展包
spring-context-support-3.2.2.jar		          	对扩展包支持
spring-core-3.2.2.jar						核心包
spring-expression-3.2.2.jar	                                spring 表达式
spring-web-3.2.2.jar						web b/s
spring-webmvc-3.2.2.jar						springmvc

com.springsource.org.aopalliance-1.0.0.jar			AOP
com.springsource.org.apache.commons.logging-1.1.1.jar	        通用日志
```

**第 1 步：建立控制器 Controller（即 Java 类）**

```
package com.hit.controller;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @Author Charies Gavin
 * @Date 2017/8/21,下午3:40
 * @Description 测试控制器
 */
public class TestController  extends AbstractController{

    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request,
                                                 HttpServletResponse response) throws Exception {
        // 获取 Controller 的名称，即地址
        System.out.println(request.getRequestURI());

        // 逻辑名
        return new ModelAndView("index");
    }
}

```
**第 2 步：配置`web.xml`文件，主要是配置 DispatcherServlet，即核心分发器**

```
<?xml version="1.0" encoding="UTF-8"?>
<web-app version="2.5" xmlns="http://java.sun.com/xml/ns/javaee"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://java.sun.com/xml/ns/javaee
         http://java.sun.com/xml/ns/javaee/web-app_2_5.xsd">

    <!-- 配置 DispatcherServlet，对所有后缀为 action 的 url 进行过滤 -->
    <servlet>
        <servlet-name>action</servlet-name>
        <servlet-class>org.springframework.web.servlet.DispatcherServlet</servlet-class>
    </servlet>

    <servlet-mapping>
        <servlet-name>action</servlet-name>
        <url-pattern>*.action</url-pattern>
    </servlet-mapping>
</web-app>
```

**第 3 步：编辑 JSP 页面，用于显示，在这里可以将该 JSP 页面复制到 pages 目录一份备用**

```
<%--
  Created by IntelliJ IDEA.
  User: Charies Gavin
  Date: 2017/8/21
  Time: 下午3:32
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
  <head>
   <title>Spring MVC</title>
  </head>

  <body>
    This is my Spring MVC!
  </body>
</html>
```
**第 4 步：建立`action-servlet.xml`文件，主要是声明 Controller 和配置 ViewResolver，即控制器和视图解析器**

```
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xsi:schemaLocation="http://www.springframework.org/schema/beans
                        http://www.springframework.org/schema/beans/spring-beans-3.2.xsd">

    <!-- 声明 Controller -->
    <bean name="/home.action" class="com.hit.controller.TestController" />

    <!-- 内部资源视图解析器，前缀 + 逻辑名 + 后缀 -->
    <bean id="internalResourceViewResolver" class="org.springframework.web.servlet.view.InternalResourceViewResolver">
        <property name="prefix" value="/WEB-INF/pages/"/>
        <property name="suffix" value=".jsp"/>
    </bean>
</beans>
```

在完成以上操作之后，我们就已经初步搭建了 Spring MVC 框架。下面，在配置一下 web 服务器就可以进行体验啦！在这里，我们用的 Web 服务器是 tomcat，配置完的结果如下图所示：

![tomact](http://img.blog.csdn.net/20170821164107553)

 - 标注1：自定义 tomcat 服务器的名称；
 - 标注2：配置 Web 服务器默认启动的浏览器；
 - 标注3：配置虚拟机参数；
 - 标注4：配置 Java 运行环境；
 - 标注5：配置 HTTP 端口号；
 - 标注6：部署 tomcat 服务器。

在此处，点击 **标注6** 所示的`Deployment`，部署 tomcat 服务器：

![deployment](http://img.blog.csdn.net/20170821164134572)

- 标注1：建议选择`exploded`版本进行部署；
- 标注2：配置应用上下文。

至此，Spring MVC 框架搭建成功，运行程序后，将在 Chrome 浏览器显示如下内容：

![index](http://img.blog.csdn.net/20170821164555137)


----------

**温馨提示**：在此项目中，由于使用 IDE 工具为 IntelliJ IDEA ，因此不用咱们自己建立`lib`包，直接将`jar`包导入`External Libraries`中即可。 



----------
———— ☆☆☆ —— [返回 -> 史上最简单的 Spring MVC 教程 <- 目录](https://github.com/guobinhit/springmvc-tutorial/blob/master/README.md) —— ☆☆☆ ————
