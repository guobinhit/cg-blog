# @SuppressWarnings 注解详述

## 1 简介


　　SuppressWarnings 是 J2SE 5.0 中标准的 Annotation 之一，其可以标注在类、字段、方法、参数、构造函数，以及局部变量上，其作用为：**告诉编译器忽略制定的警告，不用在编译完成后出现警告信息**。

## 2 使用方法

 - 第一种：`@SuppressWarnings("")`
 - 第二种：`@SuppressWarnings({})`
 - 第三种：`@SuppressWarinings(value = {})`

## 3 注解详述

 - `all`：to suppress all warnings
 - `boxing`：to suppress warnings relative to boxing/unboxing operations
 - `cast`：to suppress warnings relative to cast operations
 - `dep-ann`：to suppress warnings relative to deprecated annotation
 - `deprecation`：to suppress warnings relative to deprecation
 - `fallthrough`：to suppress warnings relative to missing breaks in switch statements
 - `finally`：to suppress warnings relative to finally block that don't return
 - `hiding`：to suppress warnings relative to locals that hide variable
 - `incomplete-switch`：to suppress warnings relative to missing entries in a switch statement (enum case)
 - `nls`：to suppress warnings relative to non-nls string literals
 - `null`：to suppress warnings relative to null analysis
 - `rawtypes`：to suppress warnings relative to un-specific types when using generics on class params
 - `restriction`：to suppress warnings relative to usage of discouraged or forbidden references
 - `serial`：to suppress warnings relative to missing serialVersionUID field for a serializable class
 - `static-access`：to suppress warnings relative to incorrect static access
 - `synthetic-access`：to suppress warnings relative to unoptimized access from inner classes
 - `unchecked`：to suppress warnings relative to unchecked operations
 - `unqualified-field-access`：to suppress warnings relative to field access unqualified
 - `unused`：to suppress warnings relative to unused code

