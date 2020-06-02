# 详述 ThreadLocal 的实现原理及其使用方法


`Threadlocal`是一个线程内部的存储类，可以在指定线程内存储数据，并且该数据只有指定线程能够获取到，其官方解释如下：

```java
/**
 * This class provides thread-local variables.  These variables differ from
 * their normal counterparts in that each thread that accesses one (via its
 * {@code get} or {@code set} method) has its own, independently initialized
 * copy of the variable.  {@code ThreadLocal} instances are typically private
 * static fields in classes that wish to associate state with a thread (e.g.,
 * a user ID or Transaction ID).
 */
```

其大致意思就是，`ThreadLocal`提供了线程内存储变量的能力，这些变量不同之处在于每一个线程读取的变量是对应的互相独立的，通过`set`和`get`方法就可以得到当前线程对应的值。

做个不恰当的比喻，从表面上看`ThreadLocal`相当于维护了一个`Map`，`key`就是当前的线程，`value`就是需要存储的对象。至于为什么说不恰当，因为实际上是`ThreadLocal`的静态内部类`ThreadLocalMap`为每个`Thread`都维护了一个数组`table`，`ThreadLocal`确定了一个数组下标，而这个下标就是`value`存储的对应位置。

## 实现原理
在`ThreadLocal`中，最重要的两个方法就是`set`和`get`，如果我们理解了这两个方法的实现原理，那么也就可以说我们理解了`ThreadLocal`的实现原理。

### ThreadLocal 的 get 方法
首先，我们来看一下`ThreadLocal`的`set`方法。

```java
 public void set(T value) {
      //获取当前线程
      Thread t = Thread.currentThread();
      //实际存储的数据结构类型
      ThreadLocalMap map = getMap(t);
      //如果存在map就直接set，没有则创建map并set
      if (map != null)
          map.set(this, value);
      else
          createMap(t, value);
  }
  
ThreadLocalMap getMap(Thread t) {
      //thread中维护了一个ThreadLocalMap
      return t.threadLocals;
 }
 
void createMap(Thread t, T firstValue) {
      //实例化一个新的ThreadLocalMap，并赋值给线程的成员变量threadLocals
      t.threadLocals = new ThreadLocalMap(this, firstValue);
}
```

如上述代码所示，我们可以看出来每个线程持有一个`ThreadLocalMap`对象。每创建一个新的线程`Thread`都会实例化一个`ThreadLocalMap`并赋值给成员变量`threadLocals`，使用时若已经存在`threadLocals`则直接使用已经存在的对象；否则的话，新创建一个`ThreadLocalMap`并赋值给`threadLocals`变量。

```java
    /* ThreadLocal values pertaining to this thread. This map is maintained
     * by the ThreadLocal class. */
    ThreadLocal.ThreadLocalMap threadLocals = null;
```

如上述代码所示，其为`Thread`类中关于`threadLocals`变量的声明。

接下来，我们看一下`createMap`方法中的实例化过程，主要就是创建`ThreadLocalMap`对象。

```java
//Entry为ThreadLocalMap静态内部类，对ThreadLocal的若引用
//同时让ThreadLocal和储值形成key-value的关系
static class Entry extends WeakReference<ThreadLocal<?>> {
    /** The value associated with this ThreadLocal. */
    Object value;

    Entry(ThreadLocal<?> k, Object v) {
           super(k);
            value = v;
    }
}

//ThreadLocalMap构造方法
ThreadLocalMap(ThreadLocal<?> firstKey, Object firstValue) {
        //内部成员数组，INITIAL_CAPACITY值为16的常量
        table = new Entry[INITIAL_CAPACITY];
        //位运算，结果与取模相同，计算出需要存放的位置
        //threadLocalHashCode比较有趣
        int i = firstKey.threadLocalHashCode & (INITIAL_CAPACITY - 1);
        table[i] = new Entry(firstKey, firstValue);
        size = 1;
        setThreshold(INITIAL_CAPACITY);
}
```

通过上面的代码不难看出在实例化`ThreadLocalMap`时创建了一个长度为 16 的`Entry`数组。通过`hashCode`与`length`位运算确定出一个索引值`i`，这个`i`就是被存储在`table`数组中的位置。

前面讲过每个线程`Thread`持有一个`ThreadLocalMap`类型的变量`threadLocals`，结合此处的构造方法可以理解成每个线程`Thread`都持有一个`Entry`型的数组`table`，而一切的读取过程都是通过操作这个数组`table`完成的。

![thread-local](https://github.com/guobinhit/cg-blog/blob/master/images/others/threadlocal-priciple-usage/thread-local.png)


显然`table`是`set`和`get`的焦点，在看具体的`set`和`get`方法前，先看下面这段代码。

```java
//在某一线程声明了ABC三种类型的ThreadLocal
ThreadLocal<A> sThreadLocalA = new ThreadLocal<A>();
ThreadLocal<B> sThreadLocalB = new ThreadLocal<B>();
ThreadLocal<C> sThreadLocalC = new ThreadLocal<C>();
```

由前面我们知道对于一个`Thread`来说只有持有一个`ThreadLocalMap`，所以 A、B、C 对应同一个`ThreadLocalMap`对象。为了管理 A、B、C，于是将他们存储在一个数组的不同位置，而这个数组就是上面提到的`Entry`型的数组`table`。

那么问题来了， A、B、C 在`table`中的位置是如何确定的？为了能正常够正常的访问对应的值，肯定存在一种方法计算出确定的索引值`i`，代码如下：

```java
//ThreadLocalMap中set方法。
  private void set(ThreadLocal<?> key, Object value) {

            // We don't use a fast path as with get() because it is at
            // least as common to use set() to create new entries as
            // it is to replace existing ones, in which case, a fast
            // path would fail more often than not.

            Entry[] tab = table;
            int len = tab.length;
            //获取索引值，这个地方是比较特别的地方
            int i = key.threadLocalHashCode & (len-1);

            //遍历tab如果已经存在则更新值
            for (Entry e = tab[i];
                 e != null;
                 e = tab[i = nextIndex(i, len)]) {
                ThreadLocal<?> k = e.get();

                if (k == key) {
                    e.value = value;
                    return;
                }

                if (k == null) {
                    replaceStaleEntry(key, value, i);
                    return;
                }
            }
            
            //如果上面没有遍历成功则创建新值
            tab[i] = new Entry(key, value);
            int sz = ++size;
            //满足条件数组扩容x2
            if (!cleanSomeSlots(i, sz) && sz >= threshold)
                rehash();
        }
```

在`ThreadLocalMap`中的`set`方法与构造方法中，能看到以下代码片段：

- `int i = key.threadLocalHashCode & (len-1)`
- `int i = firstKey.threadLocalHashCode & (INITIAL_CAPACITY - 1)`

简而言之，就是将`threadLocalHashCode`进行一个位运算（取模）得到索引`i`，`threadLocalHashCode`代码如下：

```java
    //ThreadLocal中threadLocalHashCode相关代码.
    
    private final int threadLocalHashCode = nextHashCode();

    /**
     * The next hash code to be given out. Updated atomically. Starts at
     * zero.
     */
    private static AtomicInteger nextHashCode =
        new AtomicInteger();

    /**
     * The difference between successively generated hash codes - turns
     * implicit sequential thread-local IDs into near-optimally spread
     * multiplicative hash values for power-of-two-sized tables.
     */
    private static final int HASH_INCREMENT = 0x61c88647;

    /**
     * Returns the next hash code.
     */
    private static int nextHashCode() {
        //自增
        return nextHashCode.getAndAdd(HASH_INCREMENT);
    }
```

因为`static`的原因，在每次`new ThreadLocal`时因为`threadLocalHashCode`的初始化，会使`threadLocalHashCode`值自增一次，增量为`0x61c88647`。其中，`0x61c88647`是斐波那契散列乘数，它的优点是通过它散列（`hash`）出来的结果分布会比较均匀，可以很大程度上避免`hash`冲突，已初始容量 16 为例，`hash`并与 15 位运算计算数组下标结果如下：

| hashCode | 数组下标      |
|:--------:| :-------------:|
| `0x61c88647` | 7 |
| `0xc3910c8e` |  14|
| `0x255992d5` | 5 |
|`0x8722191c`  | 12 |
| `0xe8ea9f63` | 3 |
| `0x4ab325aa` | 10 |
|`0xac7babf1`  |1  |
| `0xe443238` |  8|
|`0x700cb87f`  | 15 |

总结如下：

- 对于某一个`ThreadLocal`来讲，其索引值`i`是确定的，在不同线程之间访问时访问的是不同的`table`数组的同一位置即都为`table[i]`，只不过这个不同线程之间的`table`是独立的。
- 对于同一线程的不同`ThreadLocal`来讲，这些`ThreadLocal`实例共享一个`table`数组，然后每个`ThreadLocal`实例在`table`中的索引`i`是不同的。


### ThreadLocal 的 set 方法
在了解完`set`方法的实现原理之后，我们在来看一下`ThreadLocal`中的`get`方法。

```java
//ThreadLocal中get方法
public T get() {
    Thread t = Thread.currentThread();
    ThreadLocalMap map = getMap(t);
    if (map != null) {
        ThreadLocalMap.Entry e = map.getEntry(this);
        if (e != null) {
            @SuppressWarnings("unchecked")
            T result = (T)e.value;
            return result;
        }
    }
    return setInitialValue();
}
    
//ThreadLocalMap中getEntry方法
private Entry getEntry(ThreadLocal<?> key) {
       int i = key.threadLocalHashCode & (table.length - 1);
       Entry e = table[i];
       if (e != null && e.get() == key)
            return e;
       else
            return getEntryAfterMiss(key, i, e);
   }
```

如上述代码所示，`get`方法就是通过计算出的索引从数组的对应位置取值，其中`getMap`获取的是`Thread`类中的`threadLocals`变量。

```java
    ThreadLocalMap getMap(Thread t) {
        return t.threadLocals;
    }
```

在取值的时候，又分为两种情况，如果获取的`map`为空，则调用`setInitialValue`设置初始值，默认值为`null`，我们也可以在创建`ThreadLocal`的时候覆写其`initialValue`方法，以实现自定义默认值的目的；如果获取的`map`非空，则调用`getEntry`方法返回对应的值`e`，并当`e`不为`null`时，强转为实际的类型，否则，同样调用`setInitialValue`设置初始值。

### ThreadLocal 的特性
`ThreadLocal`和`synchronized`都是为了解决多线程中相同变量的访问冲突问题，不同的点是：

- `synchronized`是通过线程等待，牺牲时间来解决访问冲突；
- `ThreadLocal`是通过每个线程单独一份存储空间，牺牲空间来解决冲突，并且相比于`synchronized`，`ThreadLocal`具有线程隔离的效果，只有在线程内才能获取到对应的值，线程外则不能访问到想要的值。

正因为`ThreadLocal`的线程隔离特性，所以它的应用场景相对来说更为特殊一些。当某些数据是以线程为作用域并且不同线程具有不同的数据副本的时候，就可以考虑采用`ThreadLocal`实现。但是在使用`ThreadLocal`的时候，需要我们考虑内存泄漏的风险。

至于为什么会有内存泄漏的风险，则是因为在我们使用`ThreadLocal`保存一个`value`时，会在`ThreadLocalMap`中的数组插入一个`Entry`对象，按理说`key`和`value`都应该以强引用保存在`Entry`对象中，但在`ThreadLocalMap`的实现中，`key`被保存到了`WeakReference`对象中。

这就导致了一个问题，`ThreadLocal`在没有外部强引用时，发生 GC 时会被回收，但`Entry`对象和`value`并没有被回收，因此如果创建`ThreadLocal`的线程一直持续运行，那么这个`Entry`对象中的`value`就有可能一直得不到回收，从而发生内存泄露。既然已经发现有内存泄露的隐患，自然有应对的策略。在调用`ThreadLocal`的`get`方法时会自动清除`ThreadLocalMap`中`key`为`null`的`Entry`对象，其触发逻辑就在`getEntry`方法中：

```java
        private Entry getEntry(ThreadLocal<?> key) {
            int i = key.threadLocalHashCode & (table.length - 1);
            Entry e = table[i];
            if (e != null && e.get() == key)
                return e;
            else
                return getEntryAfterMiss(key, i, e);
        }
```

当`e`为`null`或者`e.get()`不等于`key`时，进入`getEntryAfterMiss`的逻辑：

```java
        private Entry getEntryAfterMiss(ThreadLocal<?> key, int i, Entry e) {
            Entry[] tab = table;
            int len = tab.length;

            while (e != null) {
                ThreadLocal<?> k = e.get();
                if (k == key)
                    return e;
                if (k == null)
                    expungeStaleEntry(i);
                else
                    i = nextIndex(i, len);
                e = tab[i];
            }
            return null;
        }
```

当`e`不为`null`且`e.get()`等于`null`时，执行`expungeStaleEntry`的逻辑，也就是真正删除过期`Entry`的方法：

```java
       /**
         * Expunge a stale entry by rehashing any possibly colliding entries
         * lying between staleSlot and the next null slot.  This also expunges
         * any other stale entries encountered before the trailing null.  See
         * Knuth, Section 6.4
         *
         * @param staleSlot index of slot known to have null key
         * @return the index of the next null slot after staleSlot
         * (all between staleSlot and this slot will have been checked
         * for expunging).
         */
        private int expungeStaleEntry(int staleSlot) {
            Entry[] tab = table;
            int len = tab.length;

            // expunge entry at staleSlot
            tab[staleSlot].value = null;
            tab[staleSlot] = null;
            size--;

            // Rehash until we encounter null
            Entry e;
            int i;
            for (i = nextIndex(staleSlot, len);
                 (e = tab[i]) != null;
                 i = nextIndex(i, len)) {
                ThreadLocal<?> k = e.get();
                if (k == null) {
                    e.value = null;
                    tab[i] = null;
                    size--;
                } else {
                    int h = k.threadLocalHashCode & (len - 1);
                    if (h != i) {
                        tab[i] = null;

                        // Unlike Knuth 6.4 Algorithm R, we must scan until
                        // null because multiple entries could have been stale.
                        while (tab[h] != null)
                            h = nextIndex(h, len);
                        tab[h] = e;
                    }
                }
            }
            return i;
        }
```

这样对应的`value`就不会 GC Roots 可达，从而在下次 GC 的时候就可以被回收了。但我们要知道，这仅是在调用`ThreadLocal`的`get`方法之后，才有可能执行的逻辑；特别地，当我们误用“先`get`再`set`”的使用逻辑时，就更会加大内存泄漏的风险。因此，**`ThreadLocal`的最佳实践就是在使用完`ThreadLocal`之后，使用`finally`关键字显示调用`ThreadLocal`的`remove`方法，防止内存泄漏**。

## 使用方法
假设，有这样一个类：

```java
@Data
@AllArgsConstructor
public class Counter{
	private int count;
}
```

我们希望多线程访问`Counter`对象时，每个线程各自保留一份`count`计数，那可以这么写：

```java
ThreadLocal<Counter> threadLocal = new ThreadLocal<>();
threadLocal.set(new Counter(0));
Counter counter = threadLocal.get();
```

如果我们不想每次调用的时候都去初始化，则可以重写`ThreadLocal`的`initValue()`方法给`ThreadLocal`设置一个对象的初始值：

```java
ThreadLocal<Counter> threadLocal = new ThreadLocal<Counter>() {
    @Override
    protected Counter initialValue() {
        return new Counter(0);
    }
};
```

如上述代码所示，这样每次再调用`threadLocal.get()`的时候，会去判断当前线程是否存在`Counter`对象，如果不存在则调用`initValue()`方法进行初始化。

```java
@Slf4j
public class MyThreadLocal<T> extends ThreadLocal<T>{
    public T get() {
        try {
            return super.get();
        } catch (Exception e) {
           log.error("获取ThreadLocal值失败！");
           return null;
        } finally {
            super.remove();
        }
    }
}
```

如上述代码所示，遵循`ThreadLocal`最佳实现，我们可以创建一个`MyThreadLocal`类，继承`ThreadLocal`并覆写其`get`方法。


---------

**参考资料**：

- [Java ThreadLocal原理分析](https://juejin.im/entry/5a276bb36fb9a0451e3fab8c)
- [Java面试必问，ThreadLocal终极篇](https://www.jianshu.com/p/377bb840802f)
- [ThreadLocal](https://www.jianshu.com/p/3c5d7f09dfbd)
- [ThreadLocal 原理和使用场景分析](https://www.cnblogs.com/fengzheng/p/8690253.html)