# 详述 Java 语言中 equals 和 == 的区别

1 前言
----

　　在 Java 语言中，`equals`和`==`都是用来检测两个字符串是否相等，返回值也都是布尔型（boolean），但是两者在内部比较的处理中却不尽相同，因此在需要检测两个字符串是否相等的时候，我们一定要特别的注意，选择适当的检测方式，防止造成不必要的 bug。从表面上来看，这种 bug 很像随机产生的间歇性错误。

2 区别
----

　　在需要检测两个字符串是否相等的时候，我们可以使用`equals`方法。对于表达式：

```
s.equals(t)
```
　　如果字符串`s`与字符串`t`相等，则返回`true`；否则，返回`false`。需要注意的是，`s`与`t`可以是字符串常量也可以是字符串变量。例如，下面的表达式就是合法的：
```
"Hello".equals(greating)
```
　　更进一步，如果想要检测两个字符串是否相等，而不区分大小写，可以使用`equalsIgnoreCase`方法。例如，下面的表达式的值就是`true`：
```
"Hello".equals("hello")
```
　　**在此，一定不能用`==`运算符来检测两个字符串是否相等！因为恒等运算符只能够确定两个字符串是否放置在同一个位置上。当然，如果两个字符串放置在同一个位置上，它们必然相等。但是，完全有可能将内容相同的多个字符串的拷贝位置放置在不同的位置上。**

　　**如果虚拟机始终将相同的字符串共享，就可以使用`==`运算符来检测两个字符串是否相等。但实际上，只有字符串常量是共享的，而`+`和`substring`等操作产生的结果并不是共享的。**

3 示例
------

```
/**
 * @author Charies Guo
 * @create 2017-07-26
 */

public class equalsAndHD {
    public static void main(String[] args) {
        String greating = "Hello";

        if (greating.equals("Hello")){
            System.out.println("1，通过 equals 输出的结果为：greating 与 Hello 的值相等！");
        }else {
            System.out.println("1，通过 equals 输出的结果为：greating 与 Hello 的值不相等！");
        }

        if ((greating.substring(0,2) + "llo") == "Hello"){
            System.out.println("2，通过 == 输出的结果为：greating 与 Hello 的值相等！");
        }else {
            System.out.println("2，通过 == 输出的结果为：greating 与 Hello 的值不相等！");
        }
    }
}
```
运行以上程序后，结果如下图所示：

![equals](http://img.blog.csdn.net/20170214215650996)

**通过观察以上的运行结果，显然可以发现，该程序完成验证了我们之前的观点。**
