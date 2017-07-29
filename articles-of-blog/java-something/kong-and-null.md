# 详述 String 类中的 空串 和 Null 串

在 String 类中，有两个特殊的字符串，分别是：空串 和`Null`串。

空串`""`是长度为`0`的字符串，因此可以调用以下代码检查一个字符串是否为空：

```
if(str.length() == 0)
```
或者

```
if(str.equals(""))
```
空串是一个 Java 对象，有自己的串长度和内容，即：“长度为`0`，内容为空”。

此外，String 类型的变量还可以存储一个特殊的值，即`null`，它表示目前没有任何对象与之关联。想要检查一个字符串是否为`null`，可以调用以下代码：

```
if(str == null)
```
不过，有时候咱们还需要检查一个字符串既不是`null`也不是空，这时候就需要调用下面的代码：

```
if(str != null && str.length() != 0)
```

在这里，咱们需要先检查字符串`str`不为`null`，否则的话，当`str == null`的时候，再调用`length()`函数，就意味着咱们在一个`null`值上调用方法，显然会出现错误。 


----------


**扩展阅读**：[Java 语言中 equals 和 == 的区别](https://github.com/guobinhit/cg-blog/blob/master/articles-of-blog/java-something/equals-hd.md)



