# @Deprecated 注解详述

## 1 简介


　　Deprecated 同 SuppressWarnings 一样，都是 J2SE 5.0 中定义在`java.lang`包中的标准 Annotation 之一，其可以标注在类、字段和方法上，其作用为：**不鼓励程序员使用被 @Deprecated 注释的程序元素，因为被 @Deprecated 注释的元素很危险（例如，现阶段 JDK 提供的带有 @Deprecated 注释的元素在以后的 JDK 版本中可能被删除）或存在更好的选择**。在使用不被赞成的程序元素或在不被赞成的代码中执行重写时，编译器会发出警告。

## 2 使用方法


在不建议其他程序员使用的类、方法和字段上，添加`@Deprecated`注解标示即可。例如

```
@Deprecated
class TestClass {
	// do something
}
```

## 3 扩展延伸


　　在 Java 中，还有一个和`@Deprecated`非常相似的注解即`@deprecated`，其用于在 Javadoc 工具生成文档的时候，表示此类注解的类、接口、方法和字段已经被废止。
