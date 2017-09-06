# 详述 Java 中的别名现象

　　在任何编程语言中，赋值操作都是最常见的操作之一，Java 自然也不例外。赋值时，使用赋值操作符`=`，它的意思是：“将等号右边的值（右值），复制给左边的值（左值）”。右值可以是任何常数、变量或者表达式（只要它能生成一个值就行）。但左值必须是一个明确的、已命名的变量。也就是说，必须有一个物理空间可以存储等号右边的值。例如：

```
a = 4;
```

就是将一个常数赋给一个变量。但是不能把任何东西赋给一个常数。

　　对于基本数据类型来说，赋值是很简单的，因为基本数据类型（在堆栈中）存储了实际的数值，而并非指向一个对象的引用，所有在为其赋值的时候，是直接将一个地方的内容复制到了另一个地方。但是在为对象“赋值”的时候，情况却发生了变化。对一个对象进行操作的时候，我们真正操作的是对象的引用，所以倘若“将一个对象赋值给另一个对象”，实际上是将“引用”从一个地方复制到另一个地方。这意味着如果对象在赋值的时候用`b = c`，那么对象`b`和`c`都指向原本只有`c`指向的那个对象。通过下面的代码示例，我们将看到这个现象：

```
package com.hit.operator;

/**
 * @Author Charies Gavin
 * @Date 2017/8/26,下午1:38
 * @GitHub https://github.com/guobinhit
 */
public class Book {
    int price;
}
```

如上面的代码所示，我们建立了一个`Book`类，用来表示书籍，其仅有一个字段`price`，表示书籍的价格。

```
package com.hit.operator;

/**
 * @Author Charies Gavin
 * @Date 2017/8/26,下午1:38
 * @GitHub https://github.com/guobinhit
 */
public class Assignment {
    public static void main(String[] args) {

        // 创建两个实体对象
        Book thinkingInJava = new Book();
        Book headFirstPattern = new Book();

        // 分别赋值
        thinkingInJava.price = 108;
        headFirstPattern.price = 68;
        System.out.println("Thinking In Java : price is " + thinkingInJava.price);
        System.out.println("Head First Pattern : price is " + headFirstPattern.price);

        // 将 headFirstPattern （的引用）赋值给 thinkingInJava
        thinkingInJava = headFirstPattern;

        System.out.println("Thinking In Java : price is " + thinkingInJava.price);
        System.out.println("Head First Pattern : price is " + headFirstPattern.price);

        // 修改 thinkingInJava 的价格（值），半价出售
        thinkingInJava.price = 54;

        System.out.println("Thinking In Java : price is " + thinkingInJava.price);
        System.out.println("Head First Pattern : price is " + headFirstPattern.price);
    }
}
```

![one](http://img.blog.csdn.net/20170826145913064)

如上面所示，我们建立了两个书籍对象`thinkingInJava`和`headFirstPattern`，并分别对其价格进行赋值；然后，将对象`headFirstPattern`的引用赋值给`thinkingInJava`；接下来，调用`thinkingInJava.price`，修改其价格为半价。最后，运行程序，输出结果。显然，由于对象的赋值是操作的对象引用，因此在我们改变`thinkingInJava`的价格时，`headFirstPattern`的价格也随之改变。这种现象，我们称之为“**别名现象**”。

　　当然，我们有时候并不希望发生别名现象，但如何避免呢？其实，想要避免别名现象也很简单，以上面的代码为例，我们只需要将对象赋值时的语句略作修改即可，如下所示：

```
package com.hit.operator;

/**
 * @Author Charies Gavin
 * @Date 2017/8/26,下午1:38
 * @GitHub https://github.com/guobinhit
 */
public class Assignment2 {
    public static void main(String[] args) {

        // 创建两个实体对象
        Book thinkingInJava = new Book();
        Book headFirstPattern = new Book();

        // 分别赋值
        thinkingInJava.price = 108;
        headFirstPattern.price = 68;
        System.out.println("Thinking In Java : price is " + thinkingInJava.price);
        System.out.println("Head First Pattern : price is " + headFirstPattern.price);

        // 将 headFirstPattern （的属性值）赋值给 thinkingInJava
        thinkingInJava.price = headFirstPattern.price;

        System.out.println("Thinking In Java : price is " + thinkingInJava.price);
        System.out.println("Head First Pattern : price is " + headFirstPattern.price);

        // 修改 thinkingInJava 的价格（值），半价出售
        thinkingInJava.price = 54;

        System.out.println("Thinking In Java : price is " + thinkingInJava.price);
        System.out.println("Head First Pattern : price is " + headFirstPattern.price);
    }
}

```

![two](http://img.blog.csdn.net/20170826151243081)

如上面所示，当我们将`thinkingInJava = headFirstPattern`修改为`thinkingInJava.price = headFirstPattern.price`之后，别名现象即可消除。


----------

**温馨提示**：此内容源于《Java编程思想》，可以通过阅读《Java编程思想》了解更多的内容。

