# 详述 synchronized 和 volatile 的实现原理以及两者的区别


> **版权声明**：本文的内容大都来自于「[zejian_](https://blog.csdn.net/javazejian/article/details/72828483)」的博文，略作修改。


## 线程安全
在并发编程中，线程安全是我们最需要关心的问题，而导致并发问题的原因，主要是：

- 存在共享数据；
- 并且，存在多条线程共同操作共享数据。

因此，为了解决这个问题，我们需要保证当存在多个线程操作共享数据时，同一时刻有且只有一个线程能够操作共享数据，其他线程必须等到该线程处理完数据之后才能进行处理。在 Java 中，关键字`synchronized`就可以保证在同一个时刻，只有一个线程能够执行某个方法或者某个代码块，主要是对方法或者代码块中存在共享数据的操作。除此之外，`synchronized`另外一个重要的作用，是其可以保证一个线程的变化（主要是共享数据的变化）能够被其他线程所看到，即保证可见性。

但是并不是所有操作都需要这么严格的限制，所以在 Java 中，还提供了具有稍弱同步语义的`volatile`关键字，用于保证内存可见性。在本文中，我们就主要讲解`synchronized`和`volatile`的实现原理以及两者的区别，

## synchronized
### 使用方式
`synchronized`关键字主要有以下 3 种使用方式，分别为：

- 修饰实例方法，作用于当前实例加锁，进入同步代码前要获得当前实例的锁；
- 修饰静态方法，作用于当前类对象加锁，进入同步代码前要获得当前类对象的锁；
- 修饰代码块，指定加锁对象，对给定对象加锁，进入同步代码库前要获得给定对象的锁。

#### 作用于实例方法
所谓的实例对象锁就是用`synchronized`修饰实例对象中的实例方法，注意是实例方法不包括静态方法，如下：

```java
public class AccountingSync implements Runnable{
    // 共享资源(临界资源)
    static int i=0;
    
    // synchronized 修饰实例方法
    public synchronized void increase(){
        i++;
    }    
    
    @Override
    public void run() {
        for(int j=0;j<1000000;j++){
            increase();
        }
    }
    
    public static void main(String[] args) throws InterruptedException {
        AccountingSync instance=new AccountingSync();
        Thread t1=new Thread(instance);
        Thread t2=new Thread(instance);
        t1.start();
        t2.start();
        t1.join();
        t2.join();
        // 输出结果为 2000000
        System.out.println(i);
    }
}
```
在上述代码中，我们开启两个线程操作同一个共享资源即变量`i`，由于`i++`操作并不具备原子性，该操作是先读取值，然后写回一个新值，相当于原来的值加上 1，分两步完成，如果第二个线程在第一个线程读取旧值和写回新值期间读取`i`的域值，那么第二个线程就会与第一个线程一起看到同一个值，并执行相同值的加`1`操作，这也就造成了线程安全失败，因此对于`increase`方法必须使用`synchronized`修饰，以便保证线程安全。

此时我们应该注意到`synchronized`修饰的是实例方法`increase`，在这样的情况下，当前线程的锁便是实例对象`instance`，注意 Java 中的线程同步锁可以是任意对象。从代码执行结果来看确实是正确的，倘若我们没有使用`synchronized`关键字，其最终输出结果就很可能小于 2000000，这便是`synchronized`关键字的作用。

这里我们还需要意识到，当一个线程正在访问一个对象的`synchronized`实例方法，那么其他线程不能访问该对象的其他`synchronized`方法，毕竟一个对象只有一把锁，当一个线程获取了该对象的锁之后，其他线程无法获取该对象的锁，所以无法访问该对象的其他`synchronized`实例方法，但是其他线程还是可以访问该实例对象的其他非`synchronized`方法。

当然，如果一个线程 A 需要访问实例对象`obj1`的`synchronized`方法`f1`（当前对象锁是`obj1`），另一个线程 B 需要访问实例对象`obj2`的`synchronized`方法`f2`（当前对象锁是`obj2`），这样是允许的，因为两个实例对象锁并不同相同，此时如果两个线程操作数据并非共享的，线程安全是有保障的，遗憾的是如果两个线程操作的是共享数据，那么线程安全就有可能无法保证了，如下代码将演示出该现象：

```java
public class AccountingSyncBad implements Runnable{
    static int i=0;
    
    public synchronized void increase(){
        i++;
    }
    
    @Override
    public void run() {
        for(int j=0;j<1000000;j++){
            increase();
        }
    }
    
    public static void main(String[] args) throws InterruptedException {
        // new新实例
        Thread t1=new Thread(new AccountingSyncBad());
        // new新实例
        Thread t2=new Thread(new AccountingSyncBad());
        t1.start();
        t2.start();
        // join含义:当前线程A等待thread线程终止之后才能从thread.join()返回
        t1.join();
        t2.join();
        System.out.println(i);
    }
}
```
上述代码与前面不同的是我们同时创建了两个新实例`AccountingSyncBad`，然后启动两个不同的线程对共享变量i进行操作，但很遗憾操作结果是1452317而不是期望结果 2000000，因为上述代码犯了严重的错误，虽然我们使用`synchronized`修饰了`increase`方法，但却`new`了两个不同的实例对象，这也就意味着存在着两个不同的实例对象锁，因此`t1`和`t2`都会进入各自的对象锁，也就是说`t1`和`t2`线程使用的是不同的锁，因此线程安全是无法保证的。

解决这种困境的的方式是将`synchronized`作用于静态的`increase`方法，这样的话，对象锁就当前类对象，由于无论创建多少个实例对象，但对于的类对象拥有只有一个，所有在这样的情况下对象锁就是唯一的。下面我们看看如何使用将`synchronized`作用于静态的`increase`方法。

#### 作用于静态方法
当`synchronized`作用于静态方法时，其锁就是当前类的`class`对象锁。由于静态成员不专属于任何一个实例对象，是类成员，因此通过`class`对象锁可以控制静态成员的并发操作。需要注意的是，如果一个线程 A 调用一个实例对象的非`static synchronized`方法，而线程 B 需要调用这个实例对象所属类的静态`synchronized`方法，是允许的，不会发生互斥现象，因为访问静态`synchronized`方法占用的锁是当前类的`class`对象，而访问非静态`synchronized`方法占用的锁是当前实例对象锁，看如下代码：

```java
public class AccountingSyncClass implements Runnable{
    static int i=0;

    /**
     * 作用于静态方法,锁是当前class对象,也就是
     * AccountingSyncClass类对应的class对象
     */
    public static synchronized void increase(){
        i++;
    }

    /**
     * 非静态,访问时锁不一样不会发生互斥
     */
    public synchronized void increase4Obj(){
        i++;
    }

    @Override
    public void run() {
        for(int j=0;j<1000000;j++){
            increase();
        }
    }
    
    public static void main(String[] args) throws InterruptedException {
        // new新实例
        Thread t1=new Thread(new AccountingSyncClass());
        // new新实例
        Thread t2=new Thread(new AccountingSyncClass());
        // 启动线程
        t1.start();
        t2.start();
        t1.join();
        t2.join();
        System.out.println(i);
    }
}
```
由于`synchronized`关键字修饰的是静态`increase`方法，与修饰实例方法不同的是，其锁对象是当前类的`class`对象。注意代码中的`increase4Obj`方法是实例方法，其对象锁是当前实例对象，如果别的线程调用该方法，将不会产生互斥现象，毕竟锁对象不同，但我们应该意识到这种情况下可能会发现线程安全问题，因为操作了共享静态变量`i`。

#### 作用于同步代码块
除了使用关键字修饰实例方法和静态方法外，还可以使用同步代码块，在某些情况下，我们编写的方法体可能比较大，同时存在一些比较耗时的操作，而需要同步的代码又只有一小部分，如果直接对整个方法进行同步操作，可能会得不偿失，此时我们可以使用同步代码块的方式对需要同步的代码进行包裹，这样就无需对整个方法进行同步操作了，同步代码块的使用示例如下：

```java
public class AccountingSync implements Runnable{
    static AccountingSync instance=new AccountingSync();
    static int i=0;
    
    @Override
    public void run() {
        //省略其他耗时操作....
        //使用同步代码块对变量i进行同步操作,锁对象为instance
        synchronized(instance){
            for(int j=0;j<1000000;j++){
                    i++;
              }
        }
    }
    
    public static void main(String[] args) throws InterruptedException {
        Thread t1=new Thread(instance);
        Thread t2=new Thread(instance);
        t1.start();
        t2.start();
        t1.join();
        t2.join();
        System.out.println(i);
    }
}
```
从代码看出，将`synchronized`作用于一个给定的实例对象`instance`，即当前实例对象就是锁对象，每次当线程进入`synchronized`包裹的代码块时就会要求当前线程持有`instance`实例对象锁，如果当前有其他线程正持有该对象锁，那么新到的线程就必须等待，这样也就保证了每次只有一个线程执行`i++`操作。当然除了`instance`作为对象外，我们还可以使用`this`对象（代表当前实例）或者当前类的`class`对象作为锁，如下代码：

```java
// this 表示当前实例对象锁
synchronized(this){
    for(int j=0;j<1000000;j++){
        i++;
    }
}

// class 对象锁
synchronized(AccountingSync.class){
    for(int j=0;j<1000000;j++){
        i++;
    }
}
```
了解完`synchronized`的基本含义及其使用方式后，下面我们将进一步深入理解`synchronized`的底层实现原理。

### 实现原理
Java 虚拟机中的同步基于进入和退出管程（Monitor）对象实现， 无论是显式同步（有明确的`monitorenter`和`monitorexit`指令，即同步代码块）还是隐式同步都是如此。在 Java 语言中，同步用的最多的地方可能是被`synchronized`修饰的同步方法。同步方法并不是由`monitorenter`和`monitorexit`指令来实现同步的，而是由方法调用指令读取运行时常量池中方法的`ACC_SYNCHRONIZED`标志来隐式实现的，关于这点，稍后详细分析。下面先来了解一个概念 Java 对象头，这对深入理解`synchronized`实现原理非常关键。

在 JVM 中，对象在内存中的布局分为三块区域：对象头、实例数据和对齐填充。如下：

- **实例变量**：存放类的属性数据信息，包括父类的属性信息，如果是数组的实例部分还包括数组的长度，这部分内存按 4 字节对齐。
- **对齐填充**：由于虚拟机要求对象起始地址必须是 8 字节的整数倍，因此填充数据不是必须存在的，仅仅是为了字节对齐。

而对 Java 头对象，它实现`synchronized`的锁对象的基础，这点我们重点分析它。一般而言，`synchronized`使用的锁对象是存储在 Java 对象头里的，JVM 中采用 2 个字来存储对象头（如果对象是数组则会分配 3 个字，多出来的 1 个字记录的是数组长度），其主要结构是由 Mark Word 和 Class Metadata Address 组成，其结构说明如下表：

| 虚拟机位数  | 头对象结构     |说明    |
|:--------: | :-------------:| :-------------:|
| 32 / 64bit     | Mark Word     | 存储对象的 HashCode、锁信息或分代年龄或 GC 标志等信息 |
| 32 / 64bit     | Class Metadata Address | 类型指针指向对象的类元数据，JVM 通过这个指针确定该对象是哪个类的实例 |

其中，Mark Word 在默认情况下存储着对象的 HashCode、分代年龄、锁标记位等，以下是 32 位 JVM 的 Mark Word 默认存储结构：


| 锁状态    | 25bit     |4bit    |1bit 是否是偏向锁    |2bit 锁标志位    |
|:--------: | :-------------:| :-------------:|:-------------:| :-------------:|
| 无锁状态   | 对象 HashCode   |对象分代年龄 |0  | 01 |

由于对象头的信息是与对象自身定义的数据没有关系的额外存储成本，因此考虑到 JVM 的空间效率，Mark Word 被设计成为一个非固定的数据结构，以便存储更多有效的数据，它会根据对象本身的状态复用自己的存储空间，如在 32 位 JVM 下，除了上述列出的 Mark Word 默认存储结构外，其结构可能还会发生变化，如锁状态可能是轻量级锁、偏向锁或重量级锁。

其中，轻量级锁和偏向锁是 Java 6 对`synchronized`锁进行优化后新增加的，稍后我们会简要分析。这里我们主要分析一下重量级锁也就是通常说`synchronized`的对象锁，锁标识位为 10，其中指针指向的是`monitor`对象（也称为管程或监视器锁）的起始地址。每个对象都存在着一个`monitor`与之关联，对象与其`monitor`之间的关系有存在多种实现方式，如`monitor`可以与对象一起创建销毁或当线程试图获取对象锁时自动生成，但当一个`monitor`被某个线程持有后，它便处于锁定状态。在 Java 虚拟机（HotSpot）中，`monitor`是由`ObjectMonitor`实现的，其主要数据结构如下（位于 HotSpot 虚拟机源码`ObjectMonitor.hpp`文件）：

```java
ObjectMonitor() {
    _header       = NULL;
    _count        = 0; // 记录个数
    _waiters      = 0,
    _recursions   = 0;
    _object       = NULL;
    _owner        = NULL;
    _WaitSet      = NULL; // 处于wait状态的线程，会被加入到_WaitSet
    _WaitSetLock  = 0 ;
    _Responsible  = NULL ;
    _succ         = NULL ;
    _cxq          = NULL ;
    FreeNext      = NULL ;
    _EntryList    = NULL ; // 处于等待锁block状态的线程，会被加入到该列表
    _SpinFreq     = 0 ;
    _SpinClock    = 0 ;
    OwnerIsThread = 0 ;
  }
```
`ObjectMonitor`中有两个队列，`_WaitSet`和`_EntryList`，用来保存`ObjectWaiter`对象列表（每个等待锁的线程都会被封装成`ObjectWaiter`对象)），`_owner`指向持有`ObjectMonitor`对象的线程，当多个线程同时访问一段同步代码时，首先会进入`_EntryList`集合，当线程获取到对象的`monitor`后进入`_owner`区域并把`monitor`中的`owner`变量设置为当前线程同时`monitor`中的计数器`count`加 1，若线程调用`wait()`方法，将释放当前持有的`monitor`，`_owner`变量恢复为`null`，`count`自减 1，同时该线程进入`_WaitSet`集合中等待被唤醒。若当前线程执行完毕也将释放`monitor`并复位变量的值，以便其他线程进入获取`monitor`。

由此看来，`monitor`对象存在于每个 Java 对象的对象头中（存储的指针的指向），·synchronized·锁便是通过这种方式获取锁的，也是为什么 Java 中任意对象可以作为锁的原因，同时也是`notify/notifyAll/wait`等方法存在于顶级对象`Object`中的原因（关于这点稍后还会进行分析），有了上述知识基础后，下面我们将进一步分析`synchronized`在字节码层面的具体语义实现。

#### 同步代码块
现在我们重新定义一个`synchronized`修饰的同步代码块，在代码块中操作共享变量`i`，如下

```java
public class SyncCodeBlock {

   public int i;

   public void syncTask(){
       // 同步代码块
       synchronized (this){
           i++;
       }
   }
}
```

编译上述代码并使用`javap`反编译后得到字节码如下（这里我们省略一部分没有必要的信息）：

```java
Classfile /Users/zejian/Downloads/Java8_Action/src/main/java/com/zejian/concurrencys/SyncCodeBlock.class
  Last modified 2017-6-2; size 426 bytes
  MD5 checksum c80bc322c87b312de760942820b4fed5
  Compiled from "SyncCodeBlock.java"
public class com.zejian.concurrencys.SyncCodeBlock
  minor version: 0
  major version: 52
  flags: ACC_PUBLIC, ACC_SUPER
Constant pool:
  //........省略常量池中数据
  //构造函数
  public com.zejian.concurrencys.SyncCodeBlock();
    descriptor: ()V
    flags: ACC_PUBLIC
    Code:
      stack=1, locals=1, args_size=1
         0: aload_0
         1: invokespecial #1                  // Method java/lang/Object."<init>":()V
         4: return
      LineNumberTable:
        line 7: 0
  
  //===========主要看看syncTask方法实现================
  public void syncTask();
    descriptor: ()V
    flags: ACC_PUBLIC
    Code:
      stack=3, locals=3, args_size=1
         0: aload_0
         1: dup
         2: astore_1
         3: monitorenter  //注意此处，进入同步方法
         4: aload_0
         5: dup
         6: getfield      #2             // Field i:I
         9: iconst_1
        10: iadd
        11: putfield      #2            // Field i:I
        14: aload_1
        15: monitorexit   //注意此处，退出同步方法
        16: goto          24
        19: astore_2
        20: aload_1
        21: monitorexit //注意此处，退出同步方法
        22: aload_2
        23: athrow
        24: return
      Exception table:
      //省略其他字节码.......
}
SourceFile: "SyncCodeBlock.java"
```
我们主要关注字节码中的如下代码：

```java
3: monitorenter  //进入同步方法
//..........省略其他  
15: monitorexit   //退出同步方法
16: goto          24
//省略其他.......
21: monitorexit //退出同步方法
```

从字节码中可知同步代码块的实现使用的是`monitorenter`和`monitorexit`指令，其中`monitorenter`指令指向同步代码块的开始位置，`monitorexit`指令则指明同步代码块的结束位置，当执行`monitorenter`指令时，当前线程将试图获取`objectref`（即对象锁）所对应的`monitor`的持有权，当`objectref`的`monitor`的进入计数器为 0，那线程可以成功取得`monitor`，并将计数器值设置为 1，取锁成功。如果当前线程已经拥有`objectref`的`monitor`的持有权，那它可以重入这个`monitor`（关于重入性稍后会分析），重入时计数器的值也会加 1。

倘若其他线程已经拥有`objectref`的`monitor`的所有权，那当前线程将被阻塞，直到正在执行线程执行完毕，即`monitorexit`指令被执行，执行线程将释放`monitor`并设置计数器值为 0 ，其他线程将有机会持有`monitor`。值得注意的是，编译器将会确保无论方法通过何种方式完成，方法中调用过的每条`monitorenter`指令都有执行其对应`monitorexit`指令，而无论这个方法是正常结束还是异常结束。为了保证在方法异常完成时`monitorenter`和`monitorexit`指令依然可以正确配对执行，编译器会自动产生一个异常处理器，这个异常处理器声明可处理所有的异常，它的目的就是用来执行`monitorexit`指令。从字节码中也可以看出多了一个`monitorexit`指令，它就是异常结束时被执行的释放`monitor`的指令。

#### 同步方法
方法级的同步是隐式的，即无需通过字节码指令来控制的，它实现在方法调用和返回操作之中。JVM 可以从方法常量池中的方法表结构中的`ACC_SYNCHRONIZED`访问标志区分一个方法是否同步方法。当方法调用时，调用指令将会 检查方法的`ACC_SYNCHRONIZED`访问标志是否被设置，如果设置了，执行线程将先持有`monitor`（虚拟机规范中用的是管程一词）， 然后再执行方法，最后在方法完成（无论是正常完成还是非正常完成）时释放`monitor`。在方法执行期间，执行线程持有了`monitor`，其他任何线程都无法再获得同一个`monitor`。如果一个同步方法执行期间抛出了异常，并且在方法内部无法处理此异常，那这个同步方法所持有的`monitor`将在异常抛到同步方法之外时自动释放。下面我们看看字节码层面如何实现：

```java
public class SyncMethod {

   public int i;

   public synchronized void syncTask(){
           i++;
   }
}
```

使用`javap`反编译后的字节码如下：

```java
Classfile /Users/zejian/Downloads/Java8_Action/src/main/java/com/zejian/concurrencys/SyncMethod.class
  Last modified 2017-6-2; size 308 bytes
  MD5 checksum f34075a8c059ea65e4cc2fa610e0cd94
  Compiled from "SyncMethod.java"
public class com.zejian.concurrencys.SyncMethod
  minor version: 0
  major version: 52
  flags: ACC_PUBLIC, ACC_SUPER
Constant pool;

   //省略没必要的字节码
  //==================syncTask方法======================
  public synchronized void syncTask();
    descriptor: ()V
    //方法标识ACC_PUBLIC代表public修饰，ACC_SYNCHRONIZED指明该方法为同步方法
    flags: ACC_PUBLIC, ACC_SYNCHRONIZED
    Code:
      stack=3, locals=1, args_size=1
         0: aload_0
         1: dup
         2: getfield      #2                  // Field i:I
         5: iconst_1
         6: iadd
         7: putfield      #2                  // Field i:I
        10: return
      LineNumberTable:
        line 12: 0
        line 13: 10
}
SourceFile: "SyncMethod.java"
```

从字节码中可以看出，`synchronized`修饰的方法并没有`monitorenter`指令和`monitorexit`指令，取得代之的确实是`ACC_SYNCHRONIZED`标识，该标识指明了该方法是一个同步方法，JVM 通过该`ACC_SYNCHRONIZED`访问标志来辨别一个方法是否声明为同步方法，从而执行相应的同步调用。这便是`synchronized`锁在同步代码块和同步方法上实现的基本原理。

同时，我们还必须注意到的是在 Java 早期版本中，`synchronized`属于重量级锁，效率低下，因为监视器锁（`monitor`）是依赖于底层的操作系统的 Mutex Lock 实现的，而操作系统实现线程之间的切换时需要从用户态转换到核心态，这个状态之间的转换需要相对比较长的时间，时间成本相对较高，这也是为什么早期的`synchronized`效率低的原因。

庆幸的是在 Java 6 之后，Java 官方对从 JVM 层面对`synchronized`较大优化，所以现在的`synchronized`锁效率也优化得很不错了，Java 6 之后，为了减少获得锁和释放锁所带来的性能消耗，引入了轻量级锁和偏向锁，接下来我们将简单了解一下 Java 官方在 JVM 层面对`synchronized`锁的优化。

在 JVM 中，锁的状态总共有四种，分别为：无锁状态、偏向锁、轻量级锁和重量级锁。随着锁的竞争，锁可以从偏向锁升级到轻量级锁，再升级的重量级锁，但是锁的升级是单向的，也就是说只能从低到高升级，不会出现锁的降级，关于重量级锁，前面我们已详细分析过，下面我们将介绍偏向锁和轻量级锁以及 JVM 的其他优化手段，这里并不打算深入到每个锁的实现和转换过程更多地是阐述 Java 虚拟机所提供的每个锁的核心优化思想，毕竟涉及到具体过程比较繁琐，如需了解详细过程可以查阅《深入理解Java虚拟机原理》。

- **偏向锁**：偏向锁是 Java 6 之后加入的新锁，它是一种针对加锁操作的优化手段，经过研究发现，在大多数情况下，锁不仅不存在多线程竞争，而且总是由同一线程多次获得，因此为了减少同一线程获取锁（会涉及到一些 CAS 操作）的代价而引入偏向锁。偏向锁的核心思想是，如果一个线程获得了锁，那么锁就进入偏向模式，此时 Mark Word 的结构也变为偏向锁结构，当这个线程再次请求锁时，无需再做任何同步操作，即获取锁的过程，这样就省去了大量有关锁申请的操作，从而也就提供程序的性能。所以，对于没有锁竞争的场合，偏向锁有很好的优化效果，毕竟极有可能连续多次是同一个线程申请相同的锁。但是对于锁竞争比较激烈的场合，偏向锁就失效了，因为这样场合极有可能每次申请锁的线程都是不相同的，因此这种场合下不应该使用偏向锁，否则会得不偿失，需要注意的是，偏向锁失败后，并不会立即膨胀为重量级锁，而是先升级为轻量级锁。
- **轻量级锁**：倘若偏向锁失败，虚拟机并不会立即升级为重量级锁，它还会尝试使用一种称为轻量级锁的优化手段（JDK 1.6 之后加入的），此时 Mark Word 的结构也变为轻量级锁的结构。轻量级锁能够提升程序性能的依据是“对绝大部分的锁，在整个同步周期内都不存在竞争”，注意这是经验数据。需要了解的是，轻量级锁所适应的场景是线程交替执行同步块的场合，如果存在同一时间访问同一锁的场合，就会导致轻量级锁膨胀为重量级锁。
- **自旋锁**：轻量级锁失败后，虚拟机为了避免线程真实地在操作系统层面挂起，还会进行一项称为自旋锁的优化手段。这是基于在大多数情况下，线程持有锁的时间都不会太长，如果直接挂起操作系统层面的线程可能会得不偿失，毕竟操作系统实现线程之间的切换时需要从用户态转换到核心态，这个状态之间的转换需要相对比较长的时间，时间成本相对较高，因此自旋锁会假设在不久将来，当前的线程可以获得锁，因此虚拟机会让当前想要获取锁的线程做几个空循环（这也是称为自旋的原因），一般不会太久，可能是 50 个循环或 100 循环，在经过若干次循环后，如果得到锁，就顺利进入临界区。如果还不能获得锁，那就会将线程在操作系统层面挂起，这就是自旋锁的优化方式，这种方式确实也是可以提升效率的。最后没办法也就只能升级为重量级锁了。
- **锁消除**：消除锁是虚拟机另外一种锁的优化，这种优化更彻底，Java 虚拟机在 JIT 编译时（可以简单理解为当某段代码即将第一次被执行时进行编译，又称即时编译），通过对运行上下文的扫描，去除不可能存在共享资源竞争的锁，通过这种方式消除没有必要的锁，可以节省毫无意义的请求锁时间，如下`StringBuffer`的`append`是一个同步方法，但是在`add`方法中的`StringBuffer`属于一个局部变量，并且不会被其他线程所使用，因此`StringBuffer`不可能存在共享资源竞争的情景，JVM 会自动将其锁消除。

```java
/**
 * 消除StringBuffer同步锁
 */
public class StringBufferRemoveSync {

    public void add(String str1, String str2) {
        //StringBuffer是线程安全,由于sb只会在append方法中使用,不可能被其他线程引用
        //因此sb属于不可能共享的资源,JVM会自动消除内部的锁
        StringBuffer sb = new StringBuffer();
        sb.append(str1).append(str2);
    }

    public static void main(String[] args) {
        StringBufferRemoveSync rmsync = new StringBufferRemoveSync();
        for (int i = 0; i < 10000000; i++) {
            rmsync.add("abc", "123");
        }
    }
}
```

### 其他可能需要了解的关键点
#### 可重入性
从互斥锁的设计上来说，当一个线程试图操作一个由其他线程持有的对象锁的临界资源时，将会处于阻塞状态，但当一个线程再次请求自己持有对象锁的临界资源时，这种情况属于重入锁，请求将会成功，在 Java 中`synchronized`是基于原子性的内部锁机制，是可重入的，因此在一个线程调用`synchronized`方法的同时在其方法体内部调用该对象另一个`synchronized`方法，也就是说一个线程得到一个对象锁后再次请求该对象锁，是允许的，这就是`synchronized`的可重入性。如下：

```java
public class AccountingSync implements Runnable{
    static AccountingSync instance=new AccountingSync();
    static int i=0;
    static int j=0;
    
    @Override
    public void run() {
        for(int j=0;j<1000000;j++){

            //this,当前实例对象锁
            synchronized(this){
                i++;
                increase();//synchronized的可重入性
            }
        }
    }

    public synchronized void increase(){
        j++;
    }

    public static void main(String[] args) throws InterruptedException {
        Thread t1=new Thread(instance);
        Thread t2=new Thread(instance);
        t1.start();t2.start();
        t1.join();t2.join();
        System.out.println(i);
    }
}
```
正如代码所演示的，在获取当前实例对象锁后进入`synchronized`代码块执行同步代码，并在代码块中调用了当前实例对象的另外一个`synchronized`方法，再次请求当前实例锁时，将被允许，进而执行方法体代码，这就是重入锁最直接的体现，需要特别注意另外一种情况，当子类继承父类时，子类也是可以通过可重入锁调用父类的同步方法。注意由于`synchronized`是基于`monitor`实现的，因此每次重入，`monitor`中的计数器仍会加 1。

#### 线程中断
正如中断二字所表达的意义，在线程运行（`run`方法）中间打断它，在 Java 中，提供了以下 3 个有关线程中断的方法

```java
// 中断线程（实例方法）
public void Thread.interrupt();
// 判断线程是否被中断（实例方法）
public boolean Thread.isInterrupted();
// 判断是否被中断并清除当前中断状态（静态方法）
public static boolean Thread.interrupted();
```
当一个线程处于被阻塞状态或者试图执行一个阻塞操作时，使用`Thread.interrupt()`方式中断该线程，注意此时将会抛出一个`InterruptedException`的异常，同时中断状态将会被复位（由中断状态改为非中断状态），如下代码将演示该过程：

```java
public class InterruputSleepThread3 {
    public static void main(String[] args) throws InterruptedException {
        Thread t1 = new Thread() {
            @Override
            public void run() {
                //while在try中，通过异常中断就可以退出run循环
                try {
                    while (true) {
                        //当前线程处于阻塞状态，异常必须捕捉处理，无法往外抛出
                        TimeUnit.SECONDS.sleep(2);
                    }
                } catch (InterruptedException e) {
                    System.out.println("Interruted When Sleep");
                    boolean interrupt = this.isInterrupted();
                    //中断状态被复位
                    System.out.println("interrupt:"+interrupt);
                }
            }
        };
        t1.start();
        TimeUnit.SECONDS.sleep(2);
        //中断处于阻塞状态的线程
        t1.interrupt();
         /**
          * 输出结果:
          * Interruted When Sleep
          * interrupt:false
          */
    }
}
```

如上述代码所示，我们创建一个线程，并在线程中调用了`sleep`方法从而使用线程进入阻塞状态，启动线程后，调用线程实例对象的`interrupt`方法中断阻塞异常，并抛出`InterruptedException`异常，此时中断状态也将被复位。这里有些人可能会诧异，为什么不用`Thread.sleep(2000)`而是用`TimeUnit.SECONDS.sleep(2)`？

其实原因很简单，前者使用时并没有明确的单位说明，而后者非常明确表达秒的单位，事实上后者的内部实现最终还是调用了`Thread.sleep(2000)`，但为了编写的代码语义更清晰，建议使用`TimeUnit.SECONDS.sleep(2)`的方式，注意`TimeUnit`是个枚举类型。除了阻塞中断的情景，我们还可能会遇到处于运行期且非阻塞的状态的线程，这种情况下，直接调用`Thread.interrupt()`中断线程是不会得到任响应的，如下代码，将无法中断非阻塞状态下的线程：

```java
public class InterruputThread {
    public static void main(String[] args) throws InterruptedException {
        Thread t1=new Thread(){
            @Override
            public void run(){
                while(true){
                    System.out.println("未被中断");
                }
            }
        };
        t1.start();
        TimeUnit.SECONDS.sleep(2);
        t1.interrupt();

        /**
         * 输出结果(无限执行):
         *    未被中断
         *    未被中断
         *    未被中断
         *    ......
         */
    }
}
```
虽然我们调用了`interrupt`方法，但线程`t1`并未被中断，因为处于非阻塞状态的线程需要我们手动进行中断检测并结束程序，改进后代码如下：

```java
public class InterruputThread {
    public static void main(String[] args) throws InterruptedException {
        Thread t1=new Thread(){
            @Override
            public void run(){
                while(true){
                    // 判断当前线程是否被中断
                    if (this.isInterrupted()){
                        System.out.println("线程中断");
                        break;
                    }
                }

                System.out.println("已跳出循环,线程中断!");
            }
        };
        t1.start();
        TimeUnit.SECONDS.sleep(2);
        t1.interrupt();

        /**
         * 输出结果:
         *   线程中断
         *   已跳出循环,线程中断!
         */
    }
}
```

我们在代码中使用了实例方法`isInterrupted`判断线程是否已被中断，如果被中断将跳出循环以此结束线程，注意非阻塞状态调用`interrupt()`并不会导致中断状态重置。综合所述，可以简单总结一下中断两种情况，一种是当线程处于阻塞状态或者试图执行一个阻塞操作时，我们可以使用实例方法`interrupt()`进行线程中断，执行中断操作后将会抛出`InterruptException`异常（该异常必须捕捉无法向外抛出）并将中断状态复位；另外一种是当线程处于运行状态时，我们也可调用实例方法`interrupt()`进行线程中断，但同时必须手动判断中断状态，并编写中断线程的代码（其实就是结束`run`方法体的代码）。有时我们在编码时可能需要兼顾以上两种情况，那么就可以如下编写：

```java
public void run(){
    try {
    //判断当前线程是否已中断,注意interrupted方法是静态的,执行后会对中断状态进行复位
    while (!Thread.interrupted()) {
        TimeUnit.SECONDS.sleep(2);
    }
    } catch (InterruptedException e) {
        // ......
    }
}
```

事实上，线程的中断操作对于正在等待获取的锁对象的`synchronized`方法或者代码块并不起作用，也就是对于`synchronized`来说，如果一个线程在等待锁，那么结果只有两种，要么它获得这把锁继续执行，要么它就保存等待，即使调用中断线程的方法，也不会生效。演示代码如下

```java
public class SynchronizedBlocked implements Runnable{

    public synchronized void f() {
        System.out.println("Trying to call f()");
        while(true) // Never releases lock
            Thread.yield();
    }

    /**
     * 在构造器中创建新线程并启动获取对象锁
     */
    public SynchronizedBlocked() {
        //该线程已持有当前实例锁
        new Thread() {
            public void run() {
                f(); // Lock acquired by this thread
            }
        }.start();
    }
    public void run() {
        //中断判断
        while (true) {
            if (Thread.interrupted()) {
                System.out.println("中断线程!!");
                break;
            } else {
                f();
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {
        SynchronizedBlocked sync = new SynchronizedBlocked();
        Thread t = new Thread(sync);
        //启动后调用f()方法,无法获取当前实例锁处于等待状态
        t.start();
        TimeUnit.SECONDS.sleep(1);
        //中断线程,无法生效
        t.interrupt();
    }
}
```
我们在`SynchronizedBlocked`构造函数中创建一个新线程并启动获取调用`f()`获取到当前实例锁，由于`SynchronizedBlocked`自身也是线程，启动后在其`run`方法中也调用了`f()`，但由于对象锁被其他线程占用，导致t线程只能等到锁，此时我们调用了`t.interrupt()`但并不能中断线程。

#### 等待唤醒机制
所谓等待唤醒机制本篇主要指的是`notify/notifyAll`和`wait`方法，在使用这 3 个方法时，必须处于`synchronized`代码块或者`synchronized`方法中，否则就会抛出`IllegalMonitorStateException`异常，这是因为调用这几个方法前必须拿到当前对象的监视器`monitor`对象，也就是说`notify/notifyAll`和`wait`方法依赖于`monitor`对象，在前面的分析中，我们知道`monitor`存在于对象头的 Mark Word 中，而`synchronized`关键字可以获取`monitor`，这也就是为什么`notify/notifyAll`和`wait`方法必须在`synchronized`代码块或者`synchronized`方法调用的原因。

```java
synchronized (obj) {
       obj.wait();
       obj.notify();
       obj.notifyAll();         
 }
```

需要特别理解的一点是，与`sleep`方法不同的是`wait`方法调用完成后，线程将被暂停，但`wait`方法将会释放当前持有的监视器锁，直到有线程调用`notify/notifyAll`方法后方能继续执行，而`sleep`方法只让线程休眠并不释放锁。同时`notify/notifyAll`方法调用后，并不会马上释放监视器锁，而是在相应的`synchronized(){}/synchronized`方法执行结束后才自动释放锁。

## volatile
`volatile`是 Java 中提供的另外一个用于并发编程的关键字，其在并发编程中很常见，但也容易被滥用。现在，我们就进一步分析`volatile`关键字的语义。`volatile`是 Java 虚拟机提供的轻量级的同步机制。`volatile`关键字有如下两个作用：

- 内存可见性；
- 禁止指令重排优化。

### 内存可见性
关于`volatile`的可见性作用，我们必须意识到被`volatile`修饰的变量对所有线程总数立即可见的，对`volatile`变量的所有写操作总是能立刻反应到其他线程中，但是对于`volatile`变量运算操作在多线程环境并不保证安全性，例如：

```java
public class VolatileVisibility {
    public static volatile int i =0;

    public static void increase(){
        i++;
    }
}
```
正如上述代码所示，`i`变量的任何改变都会立马反应到其他线程中，但是如此存在多条线程同时调用`increase()`方法的话，就会出现线程安全问题，毕竟`i++`操作并不具备原子性，该操作是先读取值，然后写回一个新值，相当于原来的值加上 1，分两步完成，如果第二个线程在第一个线程读取旧值和写回新值期间读取i的域值，那么第二个线程就会与第一个线程一起看到同一个值，并执行相同值的加`1`操作，这也就造成了线程安全失败，因此对于`increase`方法必须使用`synchronized`修饰，以便保证线程安全，需要注意的是一旦使用`synchronized`修饰方法后，由于`synchronized`本身也具备与`volatile`相同的特性，即可见性，因此在这样种情况下就完全可以省去`volatile`修饰变量。

```java
public class VolatileVisibility {
    public static int i =0;

    public synchronized static void increase(){
        i++;
    }
}
```
现在来看另外一种场景，可以使用`volatile`修饰变量达到线程安全的目的，如下：

```java
public class VolatileSafe {

    volatile boolean close;

    public void close(){
        close=true;
    }

    public void doWork(){
        while (!close){
            System.out.println("safe....");
        }
    }
}
```

由于对于`boolean`变量`close`值的修改属于原子性操作，因此可以通过使用`volatile`修饰变量`close`，使用该变量对其他线程立即可见，从而达到线程安全的目的。那么 JMM 是如何实现让`volatile`变量对其他线程立即可见的呢？

实际上，当写一个`volatile`变量时，JMM 会把该线程对应的工作内存中的共享变量值刷新到主内存中，当读取一个`volatile`变量时，JMM 会把该线程对应的工作内存置为无效，那么该线程将只能从主内存中重新读取共享变量。`volatile`变量正是通过这种“写-读”方式实现对其他线程可见的。

### 禁止指令重排优化
`volatile`关键字另一个作用就是禁止指令重排优化，从而避免多线程环境下程序出现乱序执行的现象，关于指令重排优化前面已详细分析过，这里主要简单说明一下`volatile`是如何实现禁止指令重排优化的。先了解一个概念，内存屏障。

内存屏障（`Memory Barrier`），又称内存栅栏，是一个 CPU 指令，它的作用有两个，一是保证特定操作的执行顺序，二是保证某些变量的内存可见性（利用该特性实现`volatile`的内存可见性）。由于编译器和处理器都能执行指令重排优化。如果在指令间插入一条`Memory Barrier`则会告诉编译器和 CPU，不管什么指令都不能和这条`Memory Barrier`指令重排序，也就是说通过插入内存屏障禁止在内存屏障前后的指令执行重排序优化。`Memory Barrier`的另外一个作用是强制刷出各种 CPU 的缓存数据，因此任何 CPU 上的线程都能读取到这些数据的最新版本。总之，`volatile`变量正是通过内存屏障实现其在内存中的语义，即可见性和禁止重排优化。下面看一个非常典型的禁止重排优化的例子 DCL，如下：

```java
public class DoubleCheckLock {

    private static DoubleCheckLock instance;

    private DoubleCheckLock(){}

    public static DoubleCheckLock getInstance(){
        //第一次检测
        if (instance==null){
            //同步
            synchronized (DoubleCheckLock.class){
                if (instance == null){
                    //多线程环境下可能会出现问题的地方
                    instance = new DoubleCheckLock();
                }
            }
        }
        return instance;
    }
}
```
上述代码一个经典的单例的双重检测的代码，这段代码在单线程环境下并没有什么问题，但如果在多线程环境下就可以出现线程安全问题。原因在于某一个线程执行到第一次检测，读取到的`instance`不为`null`时，`instance`的引用对象可能没有完成初始化。因为`instance = new DoubleCheckLock()`可以分为以下 3 步完成（伪代码）：

```java
// 1.分配对象内存空间
memory = allocate();
// 2.初始化对象
instance(memory);   
// 3.设置instance指向刚分配的内存地址，此时instance！=null
instance = memory;   
```
由于步骤 1 和步骤 2 间可能会重排序，如下：

```java
// 1.分配对象内存空间
memory = allocate(); 
// 3.设置instance指向刚分配的内存地址，此时instance！=null，但是对象还没有初始化完成！
instance = memory;  
// 2.初始化对象
instance(memory);   
```
由于步骤 2 和步骤 3 不存在数据依赖关系，而且无论重排前还是重排后程序的执行结果在单线程中并没有改变，因此这种重排优化是允许的。但是指令重排只会保证串行语义的执行的一致性（单线程），但并不会关心多线程间的语义一致性。所以当一条线程访问`instance`不为`null`时，由于`instance`实例未必已初始化完成，也就造成了线程安全问题。那么该如何解决呢，很简单，我们使用`volatile`禁止`instance`变量被执行指令重排优化即可。

```java
// 禁止指令重排优化
private volatile static DoubleCheckLock instance;
  ```

## synchronized 和 volatile 的区别
最后，我们来总结一下`synchronized`和`volatile`的主要区别：

- 阻塞性：
  - `synchronized`：可能造成线程阻塞；
  - `volatile`：不会造成线程阻塞。
- 作用范围：
  - `synchronized`：可以作用于变量、方法和代码块级别；
  - `volatile`：仅能作用于变量级别。
- 编译器优化：
  - `synchronized`：可以被编译器优化；
  - `volatile`：禁止编译器优化。
- 可见性和原子性：
  - `synchronized`：保证可见性和原子性；
  - `volatile`：仅保证可见性。

到这里，本篇文章就要结束了，希望能够对大家有所帮助，欢迎大家积极留言讨论！


-----------------

**参考资料**：

-  [深入理解Java并发之synchronized实现原理](https://blog.csdn.net/javazejian/article/details/72828483)
- [全面理解Java内存模型(JMM)及volatile关键字](https://blog.csdn.net/javazejian/article/details/72772461)
-  [深度解析volatile—底层实现](https://www.jianshu.com/p/2643c9ea1b82)