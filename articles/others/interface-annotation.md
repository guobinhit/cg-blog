# @interface 注解详述

1 简介
----

　　在 Java 中，定义注解其实和定义接口差多不，只需要在 interface 前添加一个`@`符号就可以，例如`@interface Zhujie{}`，这就表明我们定义了一个名为`@Zhujie`的注解。注解中的每一个方法定义了这个注解类型的一个元素，特别注意：**注解中方法的声明中一定不能包含参数，也不能抛出异 常；方法的返回值被限制为简单类型、String、Class、emnus、注释，和这些类型的数组，但方法可以有一个缺省值**。

　　注解相当于一种标记，在程序中加上了注解就等于为程序加上了某种标记，JAVAC 编译器、开发工具和其他程序可以用反射机制来了解咱们的类以及各种元素上有无标记，如果找到标记，就做相应的事。例如，`@Deprecated`可以标记在一些不建议被使用的类、方法和字段上，如果有人使用了，就给出警告。

2 元注解
-----

　　注解`@Retention`可以用来修饰注解，是注解的注解，称为**元注解**。Retention 注解有一个属性`value`，是 RetentionPolicy 类型的，而 Enum RetentionPolicy 是一个枚举类型，这就决定了 Retention 注解应该如何去操作，也可以理解为 Rentention 搭配 RententionPolicy 来使用。RetentionPolicy 有 3 个值，分别为：`CLASS` 、`RUNTIME`和`SOURCE`。

 - 用`@Retention(RetentionPolicy.CLASS)`修饰的注解，表示注解的信息被保留在 class 文件（字节码文件）中，当程序编译时，不会被虚拟机读取在运行的时候；
 - 用`@Retention(RetentionPolicy.SOURCE)`修饰的注解,表示注解的信息会被编译器抛弃，不会留在 class 文件中，注解的信息只会留在源文件中；
 - 用`@Retention(RetentionPolicy.RUNTIME)`修饰的注解，表示注解的信息被保留在 class 文件（字节码文件）中，当程序编译时，会被虚拟机保留在运行时。

3 使用示例
------
首先，创建一个简单的注解：

```
public @interface Coder { 
　　 int personId(); 
　　 String school() default "[unassigned]";
} 
```
注解定义完之后，咱们就可以用来作注释声明。注解是一种特殊的修饰符，在其他修饰符（例如，public、static 或者 final 等）使用地方都可以使用注解。按照惯例，注解应该放在其他修饰符的前面。注解的声明用`@`符号后面跟上这个注解类型的名字，再后面加上括号，括号中列出这个注释中元素或者方法的`key－value`对，其中，值必须是常量。例如：

```
@coder(personId=20151120,school="HIT")
```
没有元素或者方法的注解被称为“**标记（marker）**”类型，例如：

```
public @interface Coder {}
```
标记注解在使用的时候，其后面的括号可以省略。如果注释中仅包含一个元素，这个元素的名字应该为`value`，例如：
```
public @interface Coder { 
　　 String value();
} 
```
如果元素的名字为`value`，那么在使用这个注解的时候，元素的名字和等号都可以省略，例如：
```
@Coder("HIT")
```





