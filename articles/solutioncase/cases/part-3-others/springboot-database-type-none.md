# Spring Boot 项目启动报 driver class for database type NONE 的原因及解决方法

## 问题描述

Spring Boot 项目启动时，报出如下问题：

![springboot-database-none](https://github.com/guobinhit/cg-blog/blob/master/images/solutioncase/part-3-others/springboot-database-type-none/springboot-database-none.png)

## 问题原因

在默认配置下，Spring Boot 项目会在启动时自动加载数据库相关的配置，如果我们没有在`application.yml`文件中指定数据库配置文件的路径，则会出现该问题。

## 解决方法

在 Spring Boot 项目中，找到标有`@EnableAutoConfiguration`注解的类，并在这个注解中排除数据源自动配置类，即：

- `@EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class})`

修改前，类示例：

![enable-auto-configuration](https://github.com/guobinhit/cg-blog/blob/master/images/solutioncase/part-3-others/springboot-database-type-none/enable-auto-configuration.png)

修改后，类示例：
![enable-auto-configuration-2](https://github.com/guobinhit/cg-blog/blob/master/images/solutioncase/part-3-others/springboot-database-type-none/enable-auto-configuration2.png)




----------
———— ☆☆☆ —— [返回 -> 超实用的「Exception」和「Error」解决案例 <- 目录](https://github.com/guobinhit/cg-blog/blob/master/articles/solutioncase/README.md) —— ☆☆☆ ————
