# 史上最简单的 Spring MVC 教程（二）「HandlerMapping」

1 前言
====

在「[史上最简单的 Spring MVC 教程（一）](https://github.com/guobinhit/springmvc-tutorial/blob/master/articles-of-springmvc/buildspringmvc.md)」中，咱们已经成功搭建起 Spring MVC 框架，并且运行成功。在本篇文章中，咱们首先尝试着对前面的程序进行修改，即修改`action-servlet.xml`配置文件的位置和名称，修改后的项目结构图如下：

![01](http://img.blog.csdn.net/20170520083942690)

如上图所示，咱们调整`action-servlet.xml`的位置到`src`目录下，并修改其名称为`springmvc-servlet.xml`，接下来，咱们直接重启tomcat服务器是可以重启成功的，这意味着：

 - Spring MVC 不是随着容器启动而启动，它是在第一次访问时进行加载的。

虽然 tomcat 服务器启动成功，但在我们访问`http://localhost:8080/`的时候就会报错啦！因为 Java web 项目默认的是到`WEB-INF`中寻找配置文件，而我们又调整了配置文件的位置，报错也就是显然的啦！要想解决这个问题，这就需要我们在`web.xml`文件中进行初始化参数的配置：

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
这样，当咱们重新启动 tomcat 服务器，并访问`http://localhost:8080/`时候，就会访问成功啦！

![springmvc](http://img.blog.csdn.net/20170520083621667)

2 HandlerMapping
====================

接下来，咱们就介绍常见的 handlerMapping，共 3 种。对于 handlerMapping 的配置，将在`springmvc-servlet.xml`中进行配置。

2.1 BeanNameUrlHandlerMapping
-----------------------------

```
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xsi:schemaLocation="http://www.springframework.org/schema/beans
						http://www.springframework.org/schema/beans/spring-beans-3.2.xsd">

    <!-- 声明 handlerMapping -->
    <!-- 声明 BeanNameURI 处理器映射，其为默认的处理器映射 -->
    <bean id="beanNameUrlHandlerMapping" class="org.springframework.web.servlet.handler.BeanNameUrlHandlerMapping"/>

    <!-- 声明 Controller -->
    <bean name="/home.action" class="com.hit.controller.TestController" />

    <!-- 内部资源视图解析器，前缀 + 逻辑名 + 后缀 -->
    <bean id="internalResourceViewResolver" class="org.springframework.web.servlet.view.InternalResourceViewResolver">
        <property name="prefix" value="/WEB-INF/pages/"/>
        <property name="suffix" value=".jsp"/>
    </bean>
</beans>
```
由于 BeanNameUrlHandlerMapping 为默认配置的 handlerMapping，所以就算我们重新启动 tomcat 服务器，访问的地址及页面也不会出现什么变化，因此也就不把运行结果贴出来啦！


2.2 SimpleUrlHandlerMapping
-----------------------------

```
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:util="http://www.springframework.org/schema/util"
       xsi:schemaLocation="http://www.springframework.org/schema/beans
						http://www.springframework.org/schema/beans/spring-beans-3.2.xsd http://www.springframework.org/schema/util http://www.springframework.org/schema/util/spring-util.xsd">

    <!-- 声明 handlerMapping -->
    <!-- 声明 BeanNameURI 处理器映射，其为默认的处理器映射 -->
    <bean id="beanNameUrlHandlerMapping" class="org.springframework.web.servlet.handler.BeanNameUrlHandlerMapping"/>

    <!-- 声明 SimpleUrlHandlerMapping 处理器映射 -->
    <bean id="simpleUrlHandlerMapping" class="org.springframework.web.servlet.handler.SimpleUrlHandlerMapping">
        <property name="mappings">
            <props>
                <prop key="/a.action">homeController</prop>
                <prop key="/b.action">homeController</prop>
                <prop key="/c.action">homeController</prop>
            </props>
        </property>
    </bean>


    <!-- 声明 Controller -->
    <bean id="homeController" name="/home.action" class="com.hit.controller.TestController" />

    <!-- 内部资源视图解析器，前缀 + 逻辑名 + 后缀 -->
    <bean id="internalResourceViewResolver" class="org.springframework.web.servlet.view.InternalResourceViewResolver">
        <property name="prefix" value="/WEB-INF/pages/"/>
        <property name="suffix" value=".jsp"/>
    </bean>
</beans>
```
通过 SimpleUrlHandlerMapping，咱们可以在`property`标签中的`prop`配置多个`key`值，并且通过该`key`值访问页面，即咱们可以通过 `http://localhost:8080/a.action`访问页面，其效果同访问 `http://localhost:8080/home.action`相同。当然，在这里之前，咱们需要在声明的 Controller 中`bean`标签中添加一个`id`的属性，并将其值配置到`prop`标签内，其访问结果如下图所示：

![2](http://img.blog.csdn.net/20170520085021038)
![3](http://img.blog.csdn.net/20170520085038679)


**特别注意**：当有 BeanNameUrlHandlerMapping 方式和 SimpleUrlHandlerMapping 方式声明冲突时，也就是有同名的URL，这时 Spring MVC 框架如何处理？

 - 首先，配置重名不会引起冲突；
 - 其次，出现同名时，按配置文件的顺序执行，当发现有一个URL满足，就跳出；
 - 最后，Spring MVC 框架支持用户自定义顺序，增加`order`属性即可。

在咱们自定义顺序的时候，需要在`proprety`标签中添加`order`属性，即：

```
<!-- n 为整数，从0开始，越小优先级越高 -->
<proprety name=”order” value=”n”/>		
```
以上语句在`springmvc-servlet.xml`中配置的示例如下：

```
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:util="http://www.springframework.org/schema/util"
       xsi:schemaLocation="http://www.springframework.org/schema/beans
						http://www.springframework.org/schema/beans/spring-beans-3.2.xsd http://www.springframework.org/schema/util http://www.springframework.org/schema/util/spring-util.xsd">

    <!-- 声明 handlerMapping -->
    <!-- 声明 BeanNameURI 处理器映射，其为默认的处理器映射 -->
    <bean id="beanNameUrlHandlerMapping" class="org.springframework.web.servlet.handler.BeanNameUrlHandlerMapping">
        <property name="order" value="1"/>
    </bean>

    <!-- 声明 SimpleUrlHandlerMapping 处理器映射 -->
    <bean id="simpleUrlHandlerMapping" class="org.springframework.web.servlet.handler.SimpleUrlHandlerMapping">
        <property name="mappings">
            <props>
                <prop key="/a.action">homeController</prop>
                <prop key="/b.action">homeController</prop>
                <prop key="/home.action">homeController</prop>
            </props>
        </property>
        <property name="order" value="0"/>
    </bean>

    <!-- 声明 Controller -->
    <bean id="homeController" name="/home.action" class="com.hit.controller.TestController" />

    <!-- 内部资源视图解析器，前缀 + 逻辑名 + 后缀 -->
    <bean id="internalResourceViewResolver" class="org.springframework.web.servlet.view.InternalResourceViewResolver">
        <property name="prefix" value="/WEB-INF/pages/"/>
        <property name="suffix" value=".jsp"/>
    </bean>
</beans>
```
2.3 ControllerClassNameHandlerMapping
-----------------------------

```
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:util="http://www.springframework.org/schema/util"
       xsi:schemaLocation="http://www.springframework.org/schema/beans
						http://www.springframework.org/schema/beans/spring-beans-3.2.xsd http://www.springframework.org/schema/util http://www.springframework.org/schema/util/spring-util.xsd">

    <!-- 声明 handlerMapping -->
    <!-- 声明 BeanNameUrlHandlerMapping 处理器映射，其为默认的处理器映射 -->
    <bean id="beanNameUrlHandlerMapping" class="org.springframework.web.servlet.handler.BeanNameUrlHandlerMapping">
        <property name="order" value="1"/>
    </bean>

    <!-- 声明 SimpleUrlHandlerMapping 处理器映射 -->
    <bean id="simpleUrlHandlerMapping" class="org.springframework.web.servlet.handler.SimpleUrlHandlerMapping">
        <property name="mappings">
            <props>
                <prop key="/a.action">homeController</prop>
                <prop key="/b.action">homeController</prop>
                <prop key="/hello.action">homeController</prop>
            </props>
        </property>
        <property name="order" value="0"/>
    </bean>

    <!-- 声明 ControllerClassNameHandlerMapping 处理器映射 -->
    <bean id="controllerClassNameHandlerMapping" class="org.springframework.web.servlet.mvc.support.ControllerClassNameHandlerMapping"/>

    <!-- 声明 Controller -->
    <bean id="homeController" name="/hello.action" class="com.hit.controller.TestController" />

    <!-- 内部资源视图解析器，前缀 + 逻辑名 + 后缀 -->
    <bean id="internalResourceViewResolver" class="org.springframework.web.servlet.view.InternalResourceViewResolver">
        <property name="prefix" value="/WEB-INF/pages/"/>
        <property name="suffix" value=".jsp"/>
    </bean>
</beans>
```
控制器类名处理器映射，只需要简单的声明即可使用，值得注意的是其访问的地址为：

`http://localhost:8080/homeController.action`

![4](http://img.blog.csdn.net/20170520085546276)


----------
———— ☆☆☆ —— [返回 -> 史上最简单的 Spring MVC 教程 <- 目录](https://github.com/guobinhit/springmvc-tutorial/blob/master/README.md) —— ☆☆☆ ————
