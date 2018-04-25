# 史上最简单的 Spring MVC 教程（三）「Controller」

1 前言
====

在「[史上最简单的 Spring MVC 教程（二）](https://github.com/guobinhit/springmvc-tutorial/blob/master/articles-of-springmvc/handlermapping.md)」中，咱们讲解了常见的处理器映射（handlerMapping），并给出了应用示例。在本篇文章中，咱们讲解常见的控制器（Controller），在这里有一点需要大家知晓，那就是：在咱们创建 Controller，并继承父类的时候，父类上会被画上一条横线，这表示该类已经过时啦！这是因为在 Spring 框架在升级到 Spring 3.0 后，推荐大家使用注解的方式进行开发，因此用继承的方式就过时啦！

2 Controller
============

2.1 CommandController
---------------------
在使用命令控制器 CommandController 的时候，咱们需要在类的构造方法中进行传参，指定 command 对应的实体。 由于要指定实体，咱们就需要创建一个实体类，在此以 Person 为例；其次，咱们需要在`springmvc-servlet.xml`中声明一个命令控制器；最后，在访问链接的地址有多个单词的时候，都需要小写，也可以去掉其中的 Controller 单词。修改后的项目结构图如下所示：

![1](http://img.blog.csdn.net/20170521131533842)

**第 1 步**：创建实体类（Person）
```
package com.hit.domain;

/**
 * Created by 维C果糖 on 2017/5/21.
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
**第 2 步**：创建命令控制器（DemoCommandController）

```
package com.hit.controller;

import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractCommandController;
import com.hit.domain.Person;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by 维C果糖 on 2017/5/21.
 */
public class DemoCommandController extends AbstractCommandController {

    // 在构造函数中初始化 command 对象
    public DemoCommandController() {
        // 页面封装数据到 command 对象，对应的实体为 Person
        this.setCommandClass(Person.class);
    }

    @Override
    protected ModelAndView handle(HttpServletRequest request, HttpServletResponse response,
                                  Object command, BindException e) throws Exception {
        Person p = (Person) command;
        System.out.println(p);
        return null;
    }
}

```
**第 3 步**：修改`springmvc-servlet.xml`配置文件

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
    <!-- 声明 命令控制器 CommandController -->
    <bean class="com.hit.controller.DemoCommandController" />

    <!-- 内部资源视图解析器，前缀 + 逻辑名 + 后缀 -->
    <bean id="internalResourceViewResolver" class="org.springframework.web.servlet.view.InternalResourceViewResolver">
        <property name="prefix" value="/WEB-INF/pages/"/>
        <property name="suffix" value=".jsp"/>
    </bean>
</beans>
```

2.2 SimpleFormController
---------------------
在使用简单表单控制器 SimpleFormController 的时候，咱们也需要在类的构造方法中进行传参，指定 command 对应的实体。 接下来，同 CommandController 的配置类似，需要在`springmvc-servlet.xml`中声明一个简单表单控制器。由于这是表单控制器，所以咱们需要新建一个 JSP 页面用于提交表单。在这里，有一点需要注意：当链接以 get 方式提交时，转向 formView 视图；当链接以 post 方式提交时，转向 successView 视图。修改后的项目结构图如下所示：

![2](http://img.blog.csdn.net/20170521132114411)

**第 1 步**：创建简单表单控制器（PersonFormController）

```
package com.hit.controller;

import com.hit.domain.Person;
import org.springframework.web.servlet.mvc.SimpleFormController;

/**
 * Created by 维C果糖 on 2017/5/21.
 */
public class PersonFormController extends SimpleFormController {

    public PersonFormController() {
        // 设置 command
        this.setCommandClass(Person.class);
    }

    // 提交后，交给业务处理
    protected void doSubmitAction(Object command) throws Exception {
        Person p = (Person) command;
        System.out.println(p);
    }
}

```
**第 2 步**：新建 JSP 页面（PersonForm）

```
<%--
  Created by IntelliJ IDEA.
  User: 维C果糖
  Date: 2017/5/21
  Time: 13:20
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <meta http-equiv="content-type" content="text/html; charset=UTF-8">
    <title>人员列表</title>
</head>
<body>

<form action="${pageContext.request.contextPath}/personform.action" method="post">

    <table>
        <tr>
            <td>编号:</td>
            <td><input tyep="text" name="id"></td>
        </tr>
        <tr>
            <td>姓名:</td>
            <td><input tyep="text" name="name"></td>
        </tr>
        <tr>
            <td>年龄:</td>
            <td><input tyep="text" name="age"></td>
        </tr>
        <tr>
            <td colspan="2"><input type="button" name="btnOK" value="submit"></td>
        </tr>
    </table>
</form>

</body>
</html>
```
**第 3 步**：修改`springmvc-servlet.xml`配置文件

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
    <!-- 声明 命令控制器 CommandController -->
    <bean class="com.hit.controller.DemoCommandController" />
    <!-- 声明 简单表单控制器 SimpleFormController -->
    <bean class="com.hit.controller.PersonFormController" >
        <property name="formView" value="PersonForm"/>  <!-- 转向 form 视图-->
        <property name="successView" value="index"/>    <!-- 转向 成功 视图-->
    </bean>

    <!-- 内部资源视图解析器，前缀 + 逻辑名 + 后缀 -->
    <bean id="internalResourceViewResolver" class="org.springframework.web.servlet.view.InternalResourceViewResolver">
        <property name="prefix" value="/WEB-INF/pages/"/>
        <property name="suffix" value=".jsp"/>
    </bean>
</beans>
```
2.3 WizardController
---------------------
在使用向导控制器 WizarController 的时候，咱们也需要在类的构造方法中设置关联的实体，并且可以在构造方法中实现页面参数的回显功能。 接下来，在类中覆写 processFinish 方法，实现点击“完成”操作后需要跳转的页面；覆写 processCancel 方法，实现点击“取消”操作后跳转的页面。由于在使用向导控制器时，涉及多个页面，因此咱们需要新建多个 JSP 页面，并在`springmvc-servlet.xml`中声明一个向导控制器，并配置页面的流转顺序。修改后的项目结构图如下所示：

![str](http://img.blog.csdn.net/20170521133404013)

**第 1 步**：创建向导控制器（DemoWizardController）

```
package com.hit.controller;

import com.hit.domain.Person;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractWizardFormController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by 维C果糖 on 2017/5/21.
 */
public class DemoWizardController extends AbstractWizardFormController {

    // 在构造函数中初始化 command 对象
    public DemoWizardController() {
        // 页面封装数据到 command 对象，对应的实体为 Person
        this.setCommandClass(Person.class);
        // 帮助页面实现回显功能
        this.setCommandName("p");
    }

    // 最终完成后提交
    @Override
    protected ModelAndView processFinish(HttpServletRequest request,
                                         HttpServletResponse response, Object command, BindException e) throws Exception {
        Person p = (Person) command;
        System.out.println(p);
        return new ModelAndView("index");
    }

    // 取消填写，返回第一个页面
    @Override
    protected ModelAndView processCancel(HttpServletRequest request,
                                         HttpServletResponse response, Object command, BindException e) throws Exception {
        return new ModelAndView("wizard/PersonInfo");
    }
}


```
**第 2 步**：新建 JSP 页面（PersonInfo、PersonEdu、PersonWork）

```
<%--
  Created by IntelliJ IDEA.
  User: 维C果糖
  Date: 2017/5/21
  Time: 13:24
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <meta http-equiv="content-type" content="text/html; charset=UTF-8">
    <title>人员信息 PersonInfo</title>
</head>
<body>

    <form action="${pageContext.request.contextPath}/demowizard.action" method="post">

        <table>
            <tr>
                <td>编号:</td>
                <td><input tyep="text" name="id" value="${p.id}"></td>
            </tr>
            <tr>
                <td colspan="2">
                    <input type="submit" name="_target1" value="下一步"/>
                    <input type="submit" name="_cancel" value="取消"/>
                </td>
            </tr>
        </table>
    </form>

</body>
</html>

```

```
<%--
  Created by IntelliJ IDEA.
  User: 维C果糖
  Date: 2017/5/21
  Time: 13:24
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <meta http-equiv="content-type" content="text/html; charset=UTF-8">
    <title>教育信息 PersonEdu</title>
</head>
<body>

<form action="${pageContext.request.contextPath}/demowizard.action" method="post">

    <table>
        <tr>
            <td>姓名:</td>
            <td><input tyep="text" name="id" value="${p.name}"></td>
        </tr>
        <tr>
            <td colspan="2">
                <input type="submit" name="_target0" value="上一步"/>
                <input type="submit" name="_cancel" value="取消"/>
                <input type="submit" name="_target2" value="下一步"/>
            </td>
        </tr>
    </table>
</form>

</body>
</html>
```

```
<%--
  Created by IntelliJ IDEA.
  User: 维C果糖
  Date: 2017/5/21
  Time: 13:24
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <meta http-equiv="content-type" content="text/html; charset=UTF-8">
    <title>工作信息 PersonWork</title>
</head>
<body>

<form action="${pageContext.request.contextPath}/demowizard.action" method="post">

    <table>
        <tr>
            <td>年龄:</td>
            <td><input tyep="text" name="id" value="${p.age}"></td>
        </tr>
        <tr>
            <td colspan="2">
                <input type="submit" name="_target1" value="上一步"/>
                <input type="submit" name="_cancel" value="取消"/>
                <input type="submit" name="_finish" value="完成"/>
            </td>
        </tr>
    </table>
</form>

</body>
</html>
```
**第 3 步**：修改`springmvc-servlet.xml`配置文件

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

    <!-- 声明 命令控制器 CommandController -->
    <bean class="com.hit.controller.DemoCommandController" />

    <!-- 声明 简单表单控制器 SimpleFormController -->
    <bean class="com.hit.controller.PersonFormController" >
        <property name="formView" value="PersonForm"/>  <!-- 转向 form 视图-->
        <property name="successView" value="index"/>    <!-- 转向 成功 视图-->
    </bean>

    <!-- 声明 向导控制器 WizardController -->
    <bean class="com.hit.controller.DemoWizardController" >
        <!-- 配置这个向导控制器有哪些页面，以及这些页面的流转顺序 -->
        <property name="pages">
            <list>
                <value>wizard/PersonInfo</value>
                <value>wizard/PersonEdu</value>
                <value>wizard/PersonWork</value>
            </list>
        </property>
    </bean>

    <!-- 内部资源视图解析器，前缀 + 逻辑名 + 后缀 -->
    <bean id="internalResourceViewResolver" class="org.springframework.web.servlet.view.InternalResourceViewResolver">
        <property name="prefix" value="/WEB-INF/pages/"/>
        <property name="suffix" value=".jsp"/>
    </bean>
</beans>
```
在完成以上操作及配置后，启动 tomcat 服务器，在 Chrome 浏览器中访问`http://localhost:8080/demowizard.action`，然后在显示的页面中，依次输入编号、姓名和年龄，点击“完成”，将会返回如下页面：

![4](http://img.blog.csdn.net/20170521133050699)


----------
———— ☆☆☆ —— [返回 -> 史上最简单的 Spring MVC 教程 <- 目录](https://github.com/guobinhit/springmvc-tutorial/blob/master/README.md) —— ☆☆☆ ————
