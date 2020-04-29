# 来来来，我们聊一聊，为什么不建议使用递归操作？

## 递归的问题

如题，我们可能或多或少的都听见过类似的话或者建议：

> **尽量少使用递归操作，甚至干脆就不要使用递归操作**。

但我们在听到这句话的时候，是否会产生过疑问，为什么不建议使用递归操作呢？

现在，我们就一起聊聊这个话题，看看递归到底会产生什么样的问题。

首先，我们思考一道算法题：**如何实现二叉树的中序遍历**？

对于树的遍历，无论是前序、中序还是后序遍历，大家可能下意识的就会想到递归，为什么呢？因为递归操作实现起来“简单”啊，而且树的结构完美契合了递归的应用场景！下面为实现二叉树中序遍历的**递归**实现：

```java
    public List<Integer> inorder(TreeNode root) {
        List<Integer> ans = new ArrayList<>();
        helper(root, ans);
        return ans;
    }

    private void helper(TreeNode root, List<Integer> ans) {
        if (root != null) {
            if (root.left != null) {
                helper(root.left, ans);
            }
            ans.add(root.val);
            if (root.right != null) {
                helper(root.right, ans);
            }
        }
    }
```

观察上述代码，在使用递归的时候，我们会在函数的某一部分，重复的调用某个函数自身，直到触发终止条件时，递归才会停止，进而函数才会执行完毕。说到这里，我们就发现了递归可能会产生问题的第一个地方：

- **如果终止条件有问题，那么递归将无法停止。**

那么，我们进一步分析，如果递归无法停止，又会出现什么问题呢？

- **如果递归无法停止，函数会不断的调用自身，从而无法执行后序的流程**。

其表现出来的现象，就是程序卡在了某处，无法继续执行。到这里，我们已经从逻辑上分析了递归可能会产生的问题。接下来，我们再从 JVM 的层面上，分析递归可能会产生的问题。

我们知道，Java 源代码需要编译成字节码文件，然后由 JVM 解释执行，为了能高效地管理程序方法的调用，有条不紊地进行嵌套的方法调用和方法返回，JVM 维护了一个栈结构，称为虚拟机方法栈（如果调用的是 Native 方法，则为本地方法栈）。

栈里面存放的一个个实体称为栈帧，每个栈帧都包括了局部变量表、操作数栈、动态连接、方法返回地址和一些额外的附加信息。在 JVM 中，方法调用的过程大致为：

1. 除非被调用的方法是类方法，否则在每一次方法调用指令之前，JVM 会先把方法被调用的对象引用压入操作数栈中，除了对象的引用之外，JVM 还会把方法的参数依次压入操作数栈；
2. 在执行方法调用指令时，JVM 会将函数参数和对象引用依次从操作数栈弹出，并新建一个栈帧，把对象引用和函数参数分别放入新栈帧的局部变量表；
3. JVM 把新栈帧压入虚拟机方法栈，并把 PC（程序计数器）指向函数的第一条待执行的指令。

因此，我们总是说，每个方法的执行过程，都是一个栈帧从入栈到出栈的过程。这意味着，在执行递归操作的时候，如果终止条件有问题，无法终止递归，则会出现：

- **虚拟机方法栈只入栈不出栈**

进而，当栈中所有栈帧的大小总和大于`-Xss`设置的值时，就会出现栈溢出或者称之为栈击穿，即：

- **抛出`StackOverflowError`异常**

此外，函数的执行是有一定开销的，例如每次都要保存局部变量、参数、调用函数地址、返回值等，而递归的开销还要在此基础上乘以迭代次数，这自然会影响到函数的执行效率。

但对于某些问题，如上面我们考虑的二叉树的中序遍历，在条件允许的情况下，我们还是倾向于使用递归实现的，因为相对来说，递归的实现更简单，也更容易理解。

## 优化的方法

说的这里，我们不妨再来聊聊如何优化递归，其方法主要有三个，分别为：

- 限制递归次数
- 借助堆栈将递归转化为非递归
- 使用尾递归形式

### 限制递归次数

对于“限制递归次数”来说，就是在调用函数的时候，同时传入一个数字 N 作为递归的次数，当递归次数大于 N 的时候，强制结束递归并返回结果。仍以实现二叉树的中序遍历为例，在上述的递归实现之上，我们新增了一个`int`类型的参数`level`，作为递归可执行的最大次数，代码示例为：

```java
    public List<Integer> inorder(TreeNode root, int level) {
        List<Integer> ans = new ArrayList<>();
        helper(root, ans, level);
        return ans;
    }

    private void helper(TreeNode root, List<Integer> ans, int level) {
        if (level >= 0) {
            if (root != null) {
                if (root.left != null) {
                    helper(root.left, ans, level - 1);
                }
                ans.add(root.val);
                if (root.right != null) {
                    helper(root.right, ans, level - 1);
                }
            }
        }
    }
```

如上述代码所示，限制迭代次数能够有效的防止栈溢出或者说是栈击穿的问题，但却有可能得不到我们想要的“正确”的结果。例如，一棵 10 层的二叉树，我们调用上述的`inorder`方法，将`level`设置为 5，即使用`inorder(root, 5)`来进行遍历，这意味着我们仅能遍历出这棵 10 层树的前 5 层，并没有把这棵树完全遍历出来，因此限制递归次数的方法是有瑕疵的，治标不治本。

### 借助堆栈将递归转化为非递归

对于“借助堆栈将递归转化为非递归”来说，就是利用堆栈模拟递归的执行过程，这种方法几乎是通用的方法，因为递归本身就是通过堆栈实现的，我们只要把递归函数调用的局部变量和相应的状态放入到一个栈结构中，在函数调用和返回时做好`push`和`pop`操作，就可以了。仍以实现二叉树的中序遍历为例，我们利用堆栈将其改造为非递归的形式：

```java
    public List<Integer> inorder(TreeNode root) {
        List<Integer> ans = new ArrayList<>();
        Stack<TreeNode> stack = new Stack<>();
        TreeNode curr = root;
        while (curr != null || !stack.isEmpty()) {
            while (curr != null) {
                stack.push(curr);
                curr = curr.left;
            }
            curr = stack.pop();
            ans.add(curr.val);
            curr = curr.right;
        }
        return ans;
    }
```

如上述代码所示，我们利用`Stack`来存储二叉树的节点，由于中序遍历的顺序为首先遍历左子树、然后访问根结点、最后遍历右子树，因此我们从根节点开始，依次将左节点压入栈，直至把左子树遍历完，然后再依次弹栈，并将弹出的节点值存入我们设置的结果列表`ans`，最后再将当前节点的右节点赋值给当前节点，以保证后续的遍历，如此循环即可。

### 使用尾递归形式

对于“使用尾递归形式”来说，则是将递归中对函数本身的调用下移到函数的最后一行。因此，像我们上面实现的二叉树的中序遍历，就很难用尾递归的形式来改写，因为递归形式的中序遍历需要在遍历左右子树之间，把结果存起来，从而给在函数最后一行调用函数自身的形式造成了很大的困难。在此，我们以实现斐波那契数列为例，演示普通的递归形式与尾递归形式的区别：

- 普通递归形式

```java
public int fibonacci(int n) {
    if (n < 2) {
        return n;
    }
    return fibonacci(n - 1) + fibonacci(n - 2);
}
```

- 尾递归形式

```java
public int fibonacciTail(int n, int fn1, int fn2) {
    if (n == 0) {
        return fn1;
    }
    return fibonacciTail(n - 1, fn2, fn1 + fn2);
}
```

我们之所以说尾递归是对普通递归形式的优化，其原因在于：普通递归，每次执行递归调用的时候，JVM 都需要为局部变量创建栈来存储；而尾递归，则是因为对函数自身的调用在尾部，因此根本不需要新创建栈来保持任何局部变量，直接传递参数即可，减少了 N - 1 个新栈的创建，其中 N 为需要递归的次数。

说了这么多，那么尾递归形式是否真的有优化效果呢？我们不妨写一个简单的程序，来验证一下：

```java
public class RecursiveMethodTest {
    public static int fibonacci(int n) {
        if (n < 2) {
            return n;
        }
        return fibonacci(n - 1) + fibonacci(n - 2);
    }

    public static int fibonacciTail(int n, int fn1, int fn2) {
        if (n == 0) {
            return fn1;
        }
        return fibonacciTail(n - 1, fn2, fn1 + fn2);
    }

    public static void main(String[] args) {
        int n = 30;
        long d1 = System.nanoTime();
        System.out.println("普通递归结果：" + fibonacci(n));
        long d2 = System.nanoTime();
        System.out.println("普通递归形式：递归 " + n + " 次，耗时 " + (d2 - d1) + " 纳秒。");
        long d3 = System.nanoTime();
        System.out.println("尾递归结果：" + fibonacciTail(n, 0, 1));
        long d4 = System.nanoTime();
        System.out.println("尾递归形式：递归 " + n + " 次，耗时 " + (d4 - d3) + " 纳秒。");
    }
}
```

其执行结果为：

```
普通递归结果：832040
普通递归形式：递归 30 次，耗时 4896196 纳秒。
尾递归结果：832040
尾递归形式：递归 30 次，耗时 38125 纳秒。
```

如上述结果所示，尾递归与普通递归相比，快了近 128 倍。虽然这样的测试还很粗糙，但也足以说明两者的性能差异啦！


聊到这里，本篇文章就结束了，希望对大家有所帮助。如果大家对递归有其他的理解，请积极留言，我们一起探讨！
