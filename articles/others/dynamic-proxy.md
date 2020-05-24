# 详述 JDK 和 CGLIB 动态代理的实现原理以及两者的区别

> **版权声明**：本文的内容大都来自于「[街灯下的小草](https://blog.csdn.net/yhl_jxy)」的博文，略作修改。

## 代理

代理，我们一般又称之为“代理模式”，是一种常见设计模式，其含义是：为其他对象提供一种代理以控制对这个对象的访问。在某些情况下，一个对象不适合或者不能直接引用另一个对象，而代理对象可以在客户端和目标对象之间起到中介的作用。

代理分为静态代理和动态代理两类，两者的主要区别就是代理类生成的时机，其中：

- **静态代理**：在程序运行前，创建代理类，实现代理逻辑；
- **动态代理**：在程序运行时，运用反射机制动态创建代理类。

特别地，动态代理又有两种主要的实现方式，分别为：JDK 动态代理和 CGLIB 动态代理。

在本文中，我们就来讲解动态代理的两种实现方式、原理以及区别。


## JDK 动态代理

顾名思义，JDK 动态代理就是基于 JDK 实现的代理模式，主要运用了其拦截器和反射机制，其代理对象是由 JDK 动态生成的，而不像静态代理方式写死代理对象和被代理类。JDK 代理是不需要第三方库支持的，只需要 JDK 环境就可以进行代理，使用条件：

- 必须实现`InvocationHandler`接口；
- 使用`Proxy.newProxyInstance`产生代理对象；
- 被代理的对象必须要实现接口。

### 代码示例

使用 JDK 动态代理的五大步骤：

1. 通过实现`InvocationHandler`接口来自定义自己的`InvocationHandler`；
2. 通过`Proxy.getProxyClass`获得动态代理类；
3. 通过反射机制获得代理类的构造方法，方法签名为`getConstructor(InvocationHandler.class)`；
4. 通过构造函数获得代理对象并将自定义的`InvocationHandler`实例对象传为参数传入；
5. 通过代理对象调用目标方法。

接下来，我们就按上面的 5 个步骤，写一个 JDK 动态代理的示例。

- `IHello`，自定义接口

```java
public interface IHello {
    void sayHello();
}
```

- `HelloImpl`，接口实现类

```java
public class HelloImpl implements IHello {
    @Override
    public void sayHello() {
        System.out.println("Hello world!");
    }
}
```

- `MyInvocationHandler`，实现`InvocationHandler`接口

```java
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
 
public class MyInvocationHandler implements InvocationHandler {
 
    /** 目标对象 */
    private Object target;
 
    public MyInvocationHandler(Object target){
        this.target = target;
    }
 
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        System.out.println("------插入前置通知代码-------------");
        // 执行相应的目标方法
        Object rs = method.invoke(target,args);
        System.out.println("------插入后置处理代码-------------");
        return rs;
    }
}
```

- `MyProxyTest`，测试类

```java
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
 
/**
 * 使用JDK动态代理的五大步骤:
 * 1.通过实现InvocationHandler接口来自定义自己的InvocationHandler;
 * 2.通过Proxy.getProxyClass获得动态代理类
 * 3.通过反射机制获得代理类的构造方法，方法签名为getConstructor(InvocationHandler.class)
 * 4.通过构造函数获得代理对象并将自定义的InvocationHandler实例对象传为参数传入
 * 5.通过代理对象调用目标方法
 */
public class MyProxyTest {
    public static void main(String[] args)
            throws NoSuchMethodException, IllegalAccessException, InstantiationException, InvocationTargetException {
        // =========================第一种==========================
        // 1、生成$Proxy0的class文件
        System.getProperties().put("sun.misc.ProxyGenerator.saveGeneratedFiles", "true");
        // 2、获取动态代理类
        Class proxyClazz = Proxy.getProxyClass(IHello.class.getClassLoader(),IHello.class);
        // 3、获得代理类的构造函数，并传入参数类型InvocationHandler.class
        Constructor constructor = proxyClazz.getConstructor(InvocationHandler.class);
        // 4、通过构造函数来创建动态代理对象，将自定义的InvocationHandler实例传入
        IHello iHello1 = (IHello) constructor.newInstance(new MyInvocationHandler(new HelloImpl()));
        // 5、通过代理对象调用目标方法
        iHello1.sayHello();
 
        // ==========================第二种=============================
        /**
         * Proxy类中还有个将2~4步骤封装好的简便方法来创建动态代理对象，
         *其方法签名为：newProxyInstance(ClassLoader loader,Class<?>[] instance, InvocationHandler h)
         */
        IHello  iHello2 = (IHello) Proxy.newProxyInstance(IHello.class.getClassLoader(), // 加载接口的类加载器
                new Class[]{IHello.class}, // 一组接口
                new MyInvocationHandler(new HelloImpl())); // 自定义的InvocationHandler
        iHello2.sayHello();
    }
}
```

运行上述测试类，其结果如下图所示：

![jdk-proxy-test](https://github.com/guobinhit/cg-blog/blob/master/images/others/dynamic-proxy/jdk-proxy-test.png)

### 源码分析

以`Proxy.newProxyInstance()`方法为切入点来剖析代理类的生成及代理方法的调用。

```java
@CallerSensitive
public static Object newProxyInstance(ClassLoader loader,
                                          Class<?>[] interfaces,
                                          InvocationHandler h) throws IllegalArgumentException {
	    // 如果h为空直接抛出空指针异常，之后所有的单纯的判断null并抛异常，都是此方法
        Objects.requireNonNull(h);
	    // 拷贝类实现的所有接口
        final Class<?>[] intfs = interfaces.clone();
	    // 获取当前系统安全接口
        final SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
	        // Reflection.getCallerClass返回调用该方法的方法的调用类;loader：接口的类加载器
	        // 进行包访问权限、类加载器权限等检查
            checkProxyAccess(Reflection.getCallerClass(), loader, intfs);
        }
 
        // 查找或生成指定的代理类
        Class<?> cl = getProxyClass0(loader, intfs);
 
        // 用指定的调用处理程序调用它的构造函数
        try {
            if (sm != null) {
                checkNewProxyPermission(Reflection.getCallerClass(), cl);
            }
            
		   /*
		    * 获取代理类的构造函数对象。
		    * constructorParams是类常量，作为代理类构造函数的参数类型，常量定义如下:
		    * private static final Class<?>[] constructorParams = { InvocationHandler.class };
		    */
            final Constructor<?> cons = cl.getConstructor(constructorParams);
            final InvocationHandler ih = h;
            if (!Modifier.isPublic(cl.getModifiers())) {
                AccessController.doPrivileged(new PrivilegedAction<Void>() {
                    public Void run() {
                        cons.setAccessible(true);
                        return null;
                    }
                });
            }
	        // 根据代理类的构造函数对象来创建需要返回的代理类对象
            return cons.newInstance(new Object[]{h});
        } catch (IllegalAccessException|InstantiationException e) {
            throw new InternalError(e.toString(), e);
        } catch (InvocationTargetException e) {
            Throwable t = e.getCause();
            if (t instanceof RuntimeException) {
                throw (RuntimeException) t;
            } else {
                throw new InternalError(t.toString(), t);
            }
        } catch (NoSuchMethodException e) {
            throw new InternalError(e.toString(), e);
        }
}
```

如上述代码所示，`newProxyInstance()`方法帮我们执行了生成代理类、获取构造器和生成代理对象这三步：

- 生成代理类：`Class<?> cl = getProxyClass0(loader, intfs);`
- 获取构造器：`final Constructor<?> cons = cl.getConstructor(constructorParams);`
- 生成代理对象：`cons.newInstance(new Object[]{h});`

那么，`Proxy.getProxyClass0()`又是如何生成代理类的呢？

```java
private static Class<?> getProxyClass0(ClassLoader loader,
                                           Class<?>... interfaces) {
		// 接口数不得超过65535个，这么大，足够使用的了
        if (interfaces.length > 65535) {
            throw new IllegalArgumentException("interface limit exceeded");
        }
 
        // If the proxy class defined by the given loader implementing
        // the given interfaces exists, this will simply return the cached copy;
        // otherwise, it will create the proxy class via the ProxyClassFactory
		// 译: 如果缓存中有代理类了直接返回，否则将由代理类工厂ProxyClassFactory创建代理类
        return proxyClassCache.get(loader, interfaces);
}
```
如果缓存中没有代理类，`Proxy`中的`ProxyClassFactory`如何创建代理类？从`get()`方法追踪进去看看。

```java
public V get(K key, P parameter) {// key：类加载器；parameter：接口数组
        // 检查指定类型的对象引用不为空null。当参数为null时，抛出空指针异常。
        Objects.requireNonNull(parameter);
		// 清除已经被GC回收的弱引用
        expungeStaleEntries();
		// 将ClassLoader包装成CacheKey, 作为一级缓存的key
        Object cacheKey = CacheKey.valueOf(key, refQueue);
 
        // lazily install the 2nd level valuesMap for the particular cacheKey
		// 获取得到二级缓存
        ConcurrentMap<Object, Supplier<V>> valuesMap = map.get(cacheKey);
		// 没有获取到对应的值
        if (valuesMap == null) {
            ConcurrentMap<Object, Supplier<V>> oldValuesMap
                = map.putIfAbsent(cacheKey,
                                  valuesMap = new ConcurrentHashMap<>());
            if (oldValuesMap != null) {
                valuesMap = oldValuesMap;
            }
        }
 
        // create subKey and retrieve the possible Supplier<V> stored by that
        // subKey from valuesMap
		// 根据代理类实现的接口数组来生成二级缓存key
        Object subKey = Objects.requireNonNull(subKeyFactory.apply(key, parameter));
		// 通过subKey获取二级缓存值
        Supplier<V> supplier = valuesMap.get(subKey);
        Factory factory = null;
		// 这个循环提供了轮询机制, 如果条件为假就继续重试直到条件为真为止
        while (true) {
            if (supplier != null) {
                // supplier might be a Factory or a CacheValue<V> instance
				// 在这里supplier可能是一个Factory也可能会是一个CacheValue
				// 在这里不作判断, 而是在Supplier实现类的get方法里面进行验证
                V value = supplier.get();
                if (value != null) {
                    return value;
                }
            }
            // else no supplier in cache
            // or a supplier that returned null (could be a cleared CacheValue
            // or a Factory that wasn't successful in installing the CacheValue)
 
            // lazily construct a Factory
            if (factory == null) {
			    // 新建一个Factory实例作为subKey对应的值
                factory = new Factory(key, parameter, subKey, valuesMap);
            }
 
            if (supplier == null) {
			    // 到这里表明subKey没有对应的值, 就将factory作为subKey的值放入
                supplier = valuesMap.putIfAbsent(subKey, factory);
                if (supplier == null) {
                    // successfully installed Factory
					// 到这里表明成功将factory放入缓存
                    supplier = factory;
                }
				// 否则, 可能期间有其他线程修改了值, 那么就不再继续给subKey赋值, 而是取出来直接用
                // else retry with winning supplier
            } else {
			    // 期间可能其他线程修改了值, 那么就将原先的值替换
                if (valuesMap.replace(subKey, supplier, factory)) {
                    // successfully replaced
                    // cleared CacheEntry / unsuccessful Factory
                    // with our Factory
					// 成功将factory替换成新的值
                    supplier = factory;
                } else {
                    // retry with current supplier
					// 替换失败, 继续使用原先的值
                    supplier = valuesMap.get(subKey);
                }
            }
        }
}
```

在`get`方法中，先调用`Objects.requireNonNull(subKeyFactory.apply(key, parameter))`获取`subKey`，其中`subKeyFactory`又调用了`apply`方法，具体实现在`ProxyClassFactory`中完成。`ProxyClassFactory.apply()`实现代理类的创建。

```java
private static final class ProxyClassFactory implements BiFunction<ClassLoader, Class<?>[], Class<?>> {
		// 统一代理类的前缀名都以$Proxy
        private static final String proxyClassNamePrefix = "$Proxy";
 
        // 使用唯一的编号给作为代理类名的一部分，如$Proxy0,$Proxy1等
        private static final AtomicLong nextUniqueNumber = new AtomicLong();
 
        @Override
        public Class<?> apply(ClassLoader loader, Class<?>[] interfaces) {
 
            Map<Class<?>, Boolean> interfaceSet = new IdentityHashMap<>(interfaces.length);
            for (Class<?> intf : interfaces) {
                // 验证指定的类加载器(loader)加载接口所得到的Class对象(interfaceClass)是否与intf对象相同
                Class<?> interfaceClass = null;
                try {
                    interfaceClass = Class.forName(intf.getName(), false, loader);
                } catch (ClassNotFoundException e) {
                }
                if (interfaceClass != intf) {
                    throw new IllegalArgumentException(
                        intf + " is not visible from class loader");
                }
                // 验证该Class对象是不是接口
                if (!interfaceClass.isInterface()) {
                    throw new IllegalArgumentException(
                        interfaceClass.getName() + " is not an interface");
                }
                // 验证该接口是否重复
                if (interfaceSet.put(interfaceClass, Boolean.TRUE) != null) {
                    throw new IllegalArgumentException(
                        "repeated interface: " + interfaceClass.getName());
                }
            }
	   		 // 声明代理类所在包
            String proxyPkg = null; 
            int accessFlags = Modifier.PUBLIC | Modifier.FINAL;
 
            // 验证所有非公共的接口在同一个包内；公共的就无需处理
            for (Class<?> intf : interfaces) {
                int flags = intf.getModifiers();
                if (!Modifier.isPublic(flags)) {
                    accessFlags = Modifier.FINAL;
                    String name = intf.getName();
                    int n = name.lastIndexOf('.');
					// 截取完整包名
                    String pkg = ((n == -1) ? "" : name.substring(0, n + 1));
                    if (proxyPkg == null) {
                        proxyPkg = pkg;
                    } else if (!pkg.equals(proxyPkg)) {
                        throw new IllegalArgumentException(
                            "non-public interfaces from different packages");
                    }
                }
            }
 
            if (proxyPkg == null) {
                // if no non-public proxy interfaces, use com.sun.proxy package
		/*如果都是public接口，那么生成的代理类就在com.sun.proxy包下如果报java.io.FileNotFoundException: com\sun\proxy\$Proxy0.class 
		(系统找不到指定的路径。)的错误，就先在你项目中创建com.sun.proxy路径*/
                proxyPkg = ReflectUtil.PROXY_PACKAGE + ".";
            }
 
            /*
             * Choose a name for the proxy class to generate.
	    	 * nextUniqueNumber 是一个原子类，确保多线程安全，防止类名重复，类似于：$Proxy0，$Proxy1......
             */
            long num = nextUniqueNumber.getAndIncrement();
	    	// 代理类的完全限定名，如com.sun.proxy.$Proxy0.calss
            String proxyName = proxyPkg + proxyClassNamePrefix + num;
 
            /*
             * Generate the specified proxy class.
	   	     * 生成类字节码的方法（重点）
             */
            byte[] proxyClassFile = ProxyGenerator.generateProxyClass(
                proxyName, interfaces, accessFlags);
            try {
                return defineClass0(loader, proxyName,
                                    proxyClassFile, 0, proxyClassFile.length);
            } catch (ClassFormatError e) {
                /*
                 * A ClassFormatError here means that (barring bugs in the
                 * proxy class generation code) there was some other
                 * invalid aspect of the arguments supplied to the proxy
                 * class creation (such as virtual machine limitations
                 * exceeded).
                 */
                throw new IllegalArgumentException(e.toString());
            }
        }
}
```
代理类创建真正在`ProxyGenerator.generateProxyClass()`方法中，方法签名如下:

- `byte[] proxyClassFile = ProxyGenerator.generateProxyClass(proxyName, interfaces, accessFlags);`

```java
public static byte[] generateProxyClass(final String name, Class<?>[] interfaces, int accessFlags) {
        ProxyGenerator gen = new ProxyGenerator(name, interfaces, accessFlags);
        // 真正生成字节码的方法
        final byte[] classFile = gen.generateClassFile();
        // 如果saveGeneratedFiles为true 则生成字节码文件，所以在开始我们要设置这个参数
        // 当然，也可以通过返回的bytes自己输出
        if (saveGeneratedFiles) {
            java.security.AccessController.doPrivileged( new java.security.PrivilegedAction<Void>() {
                        public Void run() {
                            try {
                                int i = name.lastIndexOf('.');
                                Path path;
                                if (i > 0) {
                                    Path dir = Paths.get(name.substring(0, i).replace('.', File.separatorChar));
                                    Files.createDirectories(dir);
                                    path = dir.resolve(name.substring(i+1, name.length()) + ".class");
                                } else {
                                    path = Paths.get(name + ".class");
                                }
                                Files.write(path, classFile);
                                return null;
                            } catch (IOException e) {
                                throw new InternalError( "I/O exception saving generated file: " + e);
                            }
                        }
                    });
        }
        return classFile;
}
```

代理类生成的最终方法是`ProxyGenerator.generateClassFile()`。

```java
private byte[] generateClassFile() {
        /* ============================================================
         * Step 1: Assemble ProxyMethod objects for all methods to generate proxy dispatching code for.
         * 步骤1：为所有方法生成代理调度代码，将代理方法对象集合起来。
         */
        //增加 hashcode、equals、toString方法
        addProxyMethod(hashCodeMethod, Object.class);
        addProxyMethod(equalsMethod, Object.class);
        addProxyMethod(toStringMethod, Object.class);
        // 获得所有接口中的所有方法，并将方法添加到代理方法中
        for (Class<?> intf : interfaces) {
            for (Method m : intf.getMethods()) {
                addProxyMethod(m, intf);
            }
        }
 
        /*
         * 验证方法签名相同的一组方法，返回值类型是否相同；意思就是重写方法要方法签名和返回值一样
         */
        for (List<ProxyMethod> sigmethods : proxyMethods.values()) {
            checkReturnTypes(sigmethods);
        }
 
        /* ============================================================
         * Step 2: Assemble FieldInfo and MethodInfo structs for all of fields and methods in the class we are generating.
         * 为类中的方法生成字段信息和方法信息
         */
        try {
            // 生成代理类的构造函数
            methods.add(generateConstructor());
            for (List<ProxyMethod> sigmethods : proxyMethods.values()) {
                for (ProxyMethod pm : sigmethods) {
                    // add static field for method's Method object
                    fields.add(new FieldInfo(pm.methodFieldName,
                            "Ljava/lang/reflect/Method;",
                            ACC_PRIVATE | ACC_STATIC));
                    // generate code for proxy method and add it
					// 生成代理类的代理方法
                    methods.add(pm.generateMethod());
                }
            }
            // 为代理类生成静态代码块，对一些字段进行初始化
            methods.add(generateStaticInitializer());
        } catch (IOException e) {
            throw new InternalError("unexpected I/O Exception", e);
        }
 
        if (methods.size() > 65535) {
            throw new IllegalArgumentException("method limit exceeded");
        }
        if (fields.size() > 65535) {
            throw new IllegalArgumentException("field limit exceeded");
        }
 
        /* ============================================================
         * Step 3: Write the final class file.
         * 步骤3：编写最终类文件
         */
        /*
         * Make sure that constant pool indexes are reserved for the following items before starting to write the final class file.
         * 在开始编写最终类文件之前，确保为下面的项目保留常量池索引。
         */
        cp.getClass(dotToSlash(className));
        cp.getClass(superclassName);
        for (Class<?> intf: interfaces) {
            cp.getClass(dotToSlash(intf.getName()));
        }
 
        /*
         * Disallow new constant pool additions beyond this point, since we are about to write the final constant pool table.
         * 设置只读，在这之前不允许在常量池中增加信息，因为要写常量池表
         */
        cp.setReadOnly();
 
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        DataOutputStream dout = new DataOutputStream(bout);
 
        try {
            // u4 magic;
            dout.writeInt(0xCAFEBABE);
            // u2 次要版本;
            dout.writeShort(CLASSFILE_MINOR_VERSION);
            // u2 主版本
            dout.writeShort(CLASSFILE_MAJOR_VERSION);
 
            cp.write(dout);             // (write constant pool)
 
            // u2 访问标识;
            dout.writeShort(accessFlags);
            // u2 本类名;
            dout.writeShort(cp.getClass(dotToSlash(className)));
            // u2 父类名;
            dout.writeShort(cp.getClass(superclassName));
            // u2 接口;
            dout.writeShort(interfaces.length);
            // u2 interfaces[interfaces_count];
            for (Class<?> intf : interfaces) {
                dout.writeShort(cp.getClass(
                        dotToSlash(intf.getName())));
            }
            // u2 字段;
            dout.writeShort(fields.size());
            // field_info fields[fields_count];
            for (FieldInfo f : fields) {
                f.write(dout);
            }
            // u2 方法;
            dout.writeShort(methods.size());
            // method_info methods[methods_count];
            for (MethodInfo m : methods) {
                m.write(dout);
            }
            // u2 类文件属性：对于代理类来说没有类文件属性;
            dout.writeShort(0); // (no ClassFile attributes for proxy classes)
 
        } catch (IOException e) {
            throw new InternalError("unexpected I/O Exception", e);
        }
 
        return bout.toByteArray();
}
```
通过`addProxyMethod()`添加`hashcode`、`equals`和`toString`方法。

```java
private void addProxyMethod(Method var1, Class var2) {
        String var3 = var1.getName();  //方法名
        Class[] var4 = var1.getParameterTypes();   //方法参数类型数组
        Class var5 = var1.getReturnType();    //返回值类型
        Class[] var6 = var1.getExceptionTypes();   //异常类型
        String var7 = var3 + getParameterDescriptors(var4);   //方法签名
        Object var8 = (List)this.proxyMethods.get(var7);   //根据方法签名却获得proxyMethods的Value
        if(var8 != null) {    //处理多个代理接口中重复的方法的情况
            Iterator var9 = ((List)var8).iterator();
            while(var9.hasNext()) {
                ProxyGenerator.ProxyMethod var10 = (ProxyGenerator.ProxyMethod)var9.next();
                if(var5 == var10.returnType) {
                    /*归约异常类型以至于让重写的方法抛出合适的异常类型，我认为这里可能是多个接口中有相同的方法，而这些相同的方法抛出的异常类                      型又不同，所以对这些相同方法抛出的异常进行了归约*/
                    ArrayList var11 = new ArrayList();
                    collectCompatibleTypes(var6, var10.exceptionTypes, var11);
                    collectCompatibleTypes(var10.exceptionTypes, var6, var11);
                    var10.exceptionTypes = new Class[var11.size()];
                    //将ArrayList转换为Class对象数组
                    var10.exceptionTypes = (Class[])var11.toArray(var10.exceptionTypes);
                    return;
                }
            }
        } else {
            var8 = new ArrayList(3);
            this.proxyMethods.put(var7, var8);
        }    
        ((List)var8).add(new ProxyGenerator.ProxyMethod(var3, var4, var5, var6, var2, null));
       /*如果var8为空，就创建一个数组，并以方法签名为key,proxymethod对象数组为value添加到proxyMethods*/
}
```
生成的代理对象`$Proxy0.class`字节码反编译：

```java
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.UndeclaredThrowableException;
 
public final class $Proxy0 extends Proxy
  implements IHello // 继承了Proxy类和实现IHello接口
{
  // 变量，都是private static Method  XXX
  private static Method m1;
  private static Method m3;
  private static Method m2;
  private static Method m0;
 
  // 代理类的构造函数，其参数正是是InvocationHandler实例，Proxy.newInstance方法就是通过通过这个构造函数来创建代理实例的
  public $Proxy0(InvocationHandler paramInvocationHandler)
    throws 
  {
    super(paramInvocationHandler);
  }
 
  // 以下Object中的三个方法
  public final boolean equals(Object paramObject)
    throws 
  {
    try
    {
      return ((Boolean)this.h.invoke(this, m1, new Object[] { paramObject })).booleanValue();
    }
    catch (RuntimeException localRuntimeException)
    {
      throw localRuntimeException;
    }
    catch (Throwable localThrowable)
    {
      throw new UndeclaredThrowableException(localThrowable);
    }
  }
  
  // 接口代理方法
  public final void sayHello()
    throws 
  {
    try
    {
      this.h.invoke(this, m3, null);
      return;
    }
    catch (RuntimeException localRuntimeException)
    {
      throw localRuntimeException;
    }
    catch (Throwable localThrowable)
    {
      throw new UndeclaredThrowableException(localThrowable);
    }
  }
 
  public final String toString()
    throws 
  {
    try
    {
      return ((String)this.h.invoke(this, m2, null));
    }
    catch (RuntimeException localRuntimeException)
    {
      throw localRuntimeException;
    }
    catch (Throwable localThrowable)
    {
      throw new UndeclaredThrowableException(localThrowable);
    }
  }
 
  public final int hashCode()
    throws 
  {
    try
    {
      return ((Integer)this.h.invoke(this, m0, null)).intValue();
    }
    catch (RuntimeException localRuntimeException)
    {
      throw localRuntimeException;
    }
    catch (Throwable localThrowable)
    {
      throw new UndeclaredThrowableException(localThrowable);
    }
  }
 
  // 静态代码块对变量进行一些初始化工作
  static
  {
    try
    {
	  // 这里每个方法对象 和类的实际方法绑定
      m1 = Class.forName("java.lang.Object").getMethod("equals", new Class[] { Class.forName("java.lang.Object") });
      m3 = Class.forName("com.jpeony.spring.proxy.jdk.IHello").getMethod("sayHello", new Class[0]);
      m2 = Class.forName("java.lang.Object").getMethod("toString", new Class[0]);
      m0 = Class.forName("java.lang.Object").getMethod("hashCode", new Class[0]);
      return;
    }
    catch (NoSuchMethodException localNoSuchMethodException)
    {
      throw new NoSuchMethodError(localNoSuchMethodException.getMessage());
    }
    catch (ClassNotFoundException localClassNotFoundException)
    {
      throw new NoClassDefFoundError(localClassNotFoundException.getMessage());
    }
  }
}
```
当代理对象生成后，最后由`InvocationHandler的invoke()`方法调用目标方法：在动态代理中`InvocationHandler`是核心，每个代理实例都具有一个关联的调用处理程。对代理实例调用方法时，将对方法调用进行编码并将其指派到它的调用处理程序的`invoke()`方法。所以对代理方法的调用都是通`InvocationHadler`的`invoke`来实现中，而`invoke`方法根据传入的代理对象、方法和参数来决定调用代理的哪个方法，其方法签名如下:

- `invoke(Object Proxy，Method method，Object[] args)`

通过反编译源码分析调用`invoke()`过程：从反编译后的源码看`$Proxy0`类继承了`Proxy`类，同时实现了`IHello`接口，即代理类接口，所以才能强制将代理对象转换为`IHello`接口，然后调用`$Proxy0`中的`sayHello()`方法。`$Proxy0`中`sayHello()`源码：

```java
public final void sayHello() throws {
    try{
      this.h.invoke(this, m3, null);
      return;
    } catch (RuntimeException localRuntimeException) {
      throw localRuntimeException;
    } catch (Throwable localThrowable) {
      throw new UndeclaredThrowableException(localThrowable);
   }
}
```

其中，`this.h.invoke(this, m3, null)`中的`this`就是`$Proxy0`对象；`m3`就是`m3 = Class.forName("com.jpeony.spring.proxy.jdk.IHello").getMethod("sayHello", new Class[0])`，即是通过全路径名，反射获取的目标对象中的真实方法加参数；`h`就是`Proxy`类中的变量`protected InvocationHandler h;`，所以成功的调到了`InvocationHandler`中的`invoke()`方法，但是`invoke()`方法在我们自定义的`MyInvocationHandler`中实现，`MyInvocationHandler`中的`invoke()`方法：

```java
@Override
public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        System.out.println("------插入前置通知代码-------------");
        // 执行相应的目标方法
        Object rs = method.invoke(target,args);
        System.out.println("------插入后置处理代码-------------");
        return rs;
}
```

所以，绕了半天，终于调用到了`MyInvocationHandler`中的`invoke()`方法，从上面的`this.h.invoke(this, m3, null)`可以看出，`MyInvocationHandler`中`invoke`第一个参数为`$Proxy0`（代理对象），第二个参数为目标类的真实方法，第三个参数为目标方法参数，因为`sayHello()`没有参数，所以是`null`。

到这里，我们真正的实现了通过代理调用目标对象的完全分析，至于`InvocationHandler`中的`invoke()`方法就是最后执行了目标方法。到此完成了代理对象生成，目标方法调用。

所以，我们可以看到在打印目标方法调用输出结果前后所插入的前置和后置代码处理。

## CGLIB 动态代理

CGLIB（Code Generation Library）是一个开源项目，其是一个强大的，高性能，高质量的 Code 生成类库，它可以在运行期扩展 Java 类与实现 Java 接口。Hibernate 用它来实现 PO（Persistent Object，持久化对象）字节码的动态生成。

CGLIB 是一个强大的高性能的代码生成包。它广泛的被许多 AOP 的框架使用，例如 Spring AOP 为他们提供方法的`interception`（拦截）。CGLIB 包的底层是通过使用一个小而快的字节码处理框架 ASM，来转换字节码并生成新的类。

除了 CGLIB 包，脚本语言例如 Groovy 和 BeanShell，也是使用 ASM 来生成 Java 的字节码。当然不鼓励直接使用 ASM，因为它要求你必须对 JVM 内部结构包括`class`文件的格式和指令集都很熟悉。

### 代码示例
实现一个业务类，注意，这个业务类并没有实现任何接口：

```java
public class HelloService {
 
    public HelloService() {
        System.out.println("HelloService构造");
    }
 
    /**
     * 该方法不能被子类覆盖,Cglib是无法代理final修饰的方法的
     */
    final public String sayOthers(String name) {
        System.out.println("HelloService:sayOthers>>"+name);
        return null;
    }
 
    public void sayHello() {
        System.out.println("HelloService:sayHello");
    }
}
```

自定义`MethodInterceptor`：


```java
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
 
import java.lang.reflect.Method;
 
/**
 * 自定义MethodInterceptor
 */
public class MyMethodInterceptor implements MethodInterceptor{
 
    /**
     * sub：cglib生成的代理对象
     * method：被代理对象方法
     * objects：方法入参
     * methodProxy: 代理方法
     */
    @Override
    public Object intercept(Object sub, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
        System.out.println("======插入前置通知======");
        Object object = methodProxy.invokeSuper(sub, objects);
        System.out.println("======插入后者通知======");
        return object;
    }
}
```
生成 CGLIB 代理对象调用目标方法：

```java
import net.sf.cglib.core.DebuggingClassWriter;
import net.sf.cglib.proxy.Enhancer;
 
public class Client {
    public static void main(String[] args) {
        // 代理类class文件存入本地磁盘方便我们反编译查看源码
        System.setProperty(DebuggingClassWriter.DEBUG_LOCATION_PROPERTY, "D:\\code");
        // 通过CGLIB动态代理获取代理对象的过程
        Enhancer enhancer = new Enhancer();
        // 设置enhancer对象的父类
        enhancer.setSuperclass(HelloService.class);
        // 设置enhancer的回调对象
        enhancer.setCallback(new MyMethodInterceptor());
        // 创建代理对象
        HelloService proxy= (HelloService)enhancer.create();
        // 通过代理对象调用目标方法
        proxy.sayHello();
    }
}
```

运行上述测试类，其结果如下图所示：

![cglib-proxy-test](https://github.com/guobinhit/cg-blog/blob/master/images/others/dynamic-proxy/cglib-proxy-test.png)

### 源码分析
实现 CGLIB 动态代理必须实现`MethodInterceptor`（方法拦截器）接口，源码如下:

```java
/*
 * Copyright 2002,2003 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.sf.cglib.proxy;
 
/**
 * General-purpose {@link Enhancer} callback which provides for "around advice".
 * @author Juozas Baliuka <a href="mailto:baliuka@mwm.lt">baliuka@mwm.lt</a>
 * @version $Id: MethodInterceptor.java,v 1.8 2004/06/24 21:15:20 herbyderby Exp $
 */
public interface MethodInterceptor
extends Callback
{
    /**
     * All generated proxied methods call this method instead of the original method.
     * The original method may either be invoked by normal reflection using the Method object,
     * or by using the MethodProxy (faster).
     * @param obj "this", the enhanced object
     * @param method intercepted Method
     * @param args argument array; primitive types are wrapped
     * @param proxy used to invoke super (non-intercepted method); may be called
     * as many times as needed
     * @throws Throwable any exception may be thrown; if so, super method will not be invoked
     * @return any value compatible with the signature of the proxied method. Method returning void will ignore this value.
     * @see MethodProxy
     */    
    public Object intercept(Object obj, java.lang.reflect.Method method, 
    							Object[] args,
                                MethodProxy proxy) throws Throwable;
}
```

这个接口只有一个`intercept()`方法，这个方法有 4 个参数，分别为：

- `obj`表示增强的对象，即实现这个接口类的一个对象；
- `method`表示要被拦截的方法；
- `args`表示要被拦截方法的参数；
- `proxy`表示要触发父类的方法对象。

在上面的`Client`代码中，通过`Enhancer.create()`方法创建代理对象，`create()`方法的源码：

```java
	/**
     * Generate a new class if necessary and uses the specified
     * callbacks (if any) to create a new object instance.
     * Uses the no-arg constructor of the superclass.
     * @return a new instance
     */
    public Object create() {
        classOnly = false;
        argumentTypes = null;
        return createHelper();
	}
```

该方法含义就是如果有必要就创建一个新类，并且用指定的回调对象创建一个新的对象实例，使用的父类的参数的构造方法来实例化父类的部分。核心内容在`createHelper()`中，源码如下:

```java
private Object createHelper() {
        preValidate();
        Object key = KEY_FACTORY.newInstance((superclass != null) ? superclass.getName() : null,
                ReflectUtils.getNames(interfaces),
                filter == ALL_ZERO ? null : new WeakCacheKey<CallbackFilter>(filter),
                callbackTypes,
                useFactory,
                interceptDuringConstruction,
                serialVersionUID);
        this.currentKey = key;
        Object result = super.create(key);
        return result;
}
```
其中，`preValidate()`方法校验`callbackTypes`、`filter`是否为空，以及为空时的处理。通过`newInstance()`方法创建`EnhancerKey`对象，作为`Enhancer`父类`AbstractClassGenerator.create()`方法创建代理对象的参数。

```java
protected Object create(Object key) {
        try {
            ClassLoader loader = getClassLoader();
            Map<ClassLoader, ClassLoaderData> cache = CACHE;
            ClassLoaderData data = cache.get(loader);
            if (data == null) {
                synchronized (AbstractClassGenerator.class) {
                    cache = CACHE;
                    data = cache.get(loader);
                    if (data == null) {
                        Map<ClassLoader, ClassLoaderData> newCache = new WeakHashMap<ClassLoader, ClassLoaderData>(cache);
                        data = new ClassLoaderData(loader);
                        newCache.put(loader, data);
                        CACHE = newCache;
                    }
                }
            }
            this.key = key;
            Object obj = data.get(this, getUseCache());
            if (obj instanceof Class) {
                return firstInstance((Class) obj);
            }
            return nextInstance(obj);
        } catch (RuntimeException e) {
            throw e;
        } catch (Error e) {
            throw e;
        } catch (Exception e) {
            throw new CodeGenerationException(e);
        }
}
```
真正创建代理对象方法在`nextInstance()`方法中，该方法为抽象类`AbstractClassGenerator`的一个方法，签名如下：

- `abstract protected Object nextInstance(Object instance) throws Exception;`

在子类`Enhancer`中实现，实现源码如下：

```java
protected Object nextInstance(Object instance) {
        EnhancerFactoryData data = (EnhancerFactoryData) instance;
 
        if (classOnly) {
            return data.generatedClass;
        }
 
        Class[] argumentTypes = this.argumentTypes;
        Object[] arguments = this.arguments;
        if (argumentTypes == null) {
            argumentTypes = Constants.EMPTY_CLASS_ARRAY;
            arguments = null;
        }
        return data.newInstance(argumentTypes, arguments, callbacks);
}
```
看看`data.newInstance(argumentTypes, arguments, callbacks)`方法，第一个参数为代理对象的构成器类型，第二个为代理对象构造方法参数，第三个为对应回调对象。最后根据这些参数，通过反射生成代理对象，源码如下：

```java
/**
         * Creates proxy instance for given argument types, and assigns the callbacks.
         * Ideally, for each proxy class, just one set of argument types should be used,
         * otherwise it would have to spend time on constructor lookup.
         * Technically, it is a re-implementation of {@link Enhancer#createUsingReflection(Class)},
         * with "cache {@link #setThreadCallbacks} and {@link #primaryConstructor}"
         *
         * @see #createUsingReflection(Class)
         * @param argumentTypes constructor argument types
         * @param arguments constructor arguments
         * @param callbacks callbacks to set for the new instance
         * @return newly created proxy
         */
        public Object newInstance(Class[] argumentTypes, Object[] arguments, Callback[] callbacks) {
            setThreadCallbacks(callbacks);
            try {
                // Explicit reference equality is added here just in case Arrays.equals does not have one
                if (primaryConstructorArgTypes == argumentTypes ||
                        Arrays.equals(primaryConstructorArgTypes, argumentTypes)) {
                    // If we have relevant Constructor instance at hand, just call it
                    // This skips "get constructors" machinery
                    return ReflectUtils.newInstance(primaryConstructor, arguments);
                }
                // Take a slow path if observing unexpected argument types
                return ReflectUtils.newInstance(generatedClass, argumentTypes, arguments);
            } finally {
                // clear thread callbacks to allow them to be gc'd
                setThreadCallbacks(null);
            }
 }
```

最后生成代理对象：

![hello-service-proxy-class](https://github.com/guobinhit/cg-blog/blob/master/images/others/dynamic-proxy/hello-service-proxy-class.png)

将其反编译后代码如下：

```java
//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//
 
import java.lang.reflect.Method;
import net.sf.cglib.core.ReflectUtils;
import net.sf.cglib.core.Signature;
import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.Factory;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
 
public class HelloService$$EnhancerByCGLIB$$be45efdd extends HelloService implements Factory {
    private boolean CGLIB$BOUND;
    public static Object CGLIB$FACTORY_DATA;
    private static final ThreadLocal CGLIB$THREAD_CALLBACKS;
    private static final Callback[] CGLIB$STATIC_CALLBACKS;
    private MethodInterceptor CGLIB$CALLBACK_0;
    private static Object CGLIB$CALLBACK_FILTER;
    private static final Method CGLIB$sayHello$0$Method;
    private static final MethodProxy CGLIB$sayHello$0$Proxy;
    private static final Object[] CGLIB$emptyArgs;
    private static final Method CGLIB$equals$1$Method;
    private static final MethodProxy CGLIB$equals$1$Proxy;
    private static final Method CGLIB$toString$2$Method;
    private static final MethodProxy CGLIB$toString$2$Proxy;
    private static final Method CGLIB$hashCode$3$Method;
    private static final MethodProxy CGLIB$hashCode$3$Proxy;
    private static final Method CGLIB$clone$4$Method;
    private static final MethodProxy CGLIB$clone$4$Proxy;
 
    static void CGLIB$STATICHOOK1() {
        CGLIB$THREAD_CALLBACKS = new ThreadLocal();
        CGLIB$emptyArgs = new Object[0];
        Class var0 = Class.forName("com.jpeony.spring.proxy.cglib.HelloService$$EnhancerByCGLIB$$be45efdd");
        Class var1;
        Method[] var10000 = ReflectUtils.findMethods(new String[]{"equals", "(Ljava/lang/Object;)Z", "toString", "()Ljava/lang/String;", "hashCode", "()I", "clone", "()Ljava/lang/Object;"}, (var1 = Class.forName("java.lang.Object")).getDeclaredMethods());
        CGLIB$equals$1$Method = var10000[0];
        CGLIB$equals$1$Proxy = MethodProxy.create(var1, var0, "(Ljava/lang/Object;)Z", "equals", "CGLIB$equals$1");
        CGLIB$toString$2$Method = var10000[1];
        CGLIB$toString$2$Proxy = MethodProxy.create(var1, var0, "()Ljava/lang/String;", "toString", "CGLIB$toString$2");
        CGLIB$hashCode$3$Method = var10000[2];
        CGLIB$hashCode$3$Proxy = MethodProxy.create(var1, var0, "()I", "hashCode", "CGLIB$hashCode$3");
        CGLIB$clone$4$Method = var10000[3];
        CGLIB$clone$4$Proxy = MethodProxy.create(var1, var0, "()Ljava/lang/Object;", "clone", "CGLIB$clone$4");
        CGLIB$sayHello$0$Method = ReflectUtils.findMethods(new String[]{"sayHello", "()V"}, (var1 = Class.forName("com.jpeony.spring.proxy.cglib.HelloService")).getDeclaredMethods())[0];
        CGLIB$sayHello$0$Proxy = MethodProxy.create(var1, var0, "()V", "sayHello", "CGLIB$sayHello$0");
    }
 
    final void CGLIB$sayHello$0() {
        super.sayHello();
    }
 
    public final void sayHello() {
        MethodInterceptor var10000 = this.CGLIB$CALLBACK_0;
        if (var10000 == null) {
            CGLIB$BIND_CALLBACKS(this);
            var10000 = this.CGLIB$CALLBACK_0;
        }
 
        if (var10000 != null) {
            var10000.intercept(this, CGLIB$sayHello$0$Method, CGLIB$emptyArgs, CGLIB$sayHello$0$Proxy);
        } else {
            super.sayHello();
        }
    }
 
    final boolean CGLIB$equals$1(Object var1) {
        return super.equals(var1);
    }
 
    public final boolean equals(Object var1) {
        MethodInterceptor var10000 = this.CGLIB$CALLBACK_0;
        if (var10000 == null) {
            CGLIB$BIND_CALLBACKS(this);
            var10000 = this.CGLIB$CALLBACK_0;
        }
 
        if (var10000 != null) {
            Object var2 = var10000.intercept(this, CGLIB$equals$1$Method, new Object[]{var1}, CGLIB$equals$1$Proxy);
            return var2 == null ? false : (Boolean)var2;
        } else {
            return super.equals(var1);
        }
    }
 
    final String CGLIB$toString$2() {
        return super.toString();
    }
 
    public final String toString() {
        MethodInterceptor var10000 = this.CGLIB$CALLBACK_0;
        if (var10000 == null) {
            CGLIB$BIND_CALLBACKS(this);
            var10000 = this.CGLIB$CALLBACK_0;
        }
 
        return var10000 != null ? (String)var10000.intercept(this, CGLIB$toString$2$Method, CGLIB$emptyArgs, CGLIB$toString$2$Proxy) : super.toString();
    }
 
    final int CGLIB$hashCode$3() {
        return super.hashCode();
    }
 
    public final int hashCode() {
        MethodInterceptor var10000 = this.CGLIB$CALLBACK_0;
        if (var10000 == null) {
            CGLIB$BIND_CALLBACKS(this);
            var10000 = this.CGLIB$CALLBACK_0;
        }
 
        if (var10000 != null) {
            Object var1 = var10000.intercept(this, CGLIB$hashCode$3$Method, CGLIB$emptyArgs, CGLIB$hashCode$3$Proxy);
            return var1 == null ? 0 : ((Number)var1).intValue();
        } else {
            return super.hashCode();
        }
    }
 
    final Object CGLIB$clone$4() throws CloneNotSupportedException {
        return super.clone();
    }
 
    protected final Object clone() throws CloneNotSupportedException {
        MethodInterceptor var10000 = this.CGLIB$CALLBACK_0;
        if (var10000 == null) {
            CGLIB$BIND_CALLBACKS(this);
            var10000 = this.CGLIB$CALLBACK_0;
        }
 
        return var10000 != null ? var10000.intercept(this, CGLIB$clone$4$Method, CGLIB$emptyArgs, CGLIB$clone$4$Proxy) : super.clone();
    }
 
    public static MethodProxy CGLIB$findMethodProxy(Signature var0) {
        String var10000 = var0.toString();
        switch(var10000.hashCode()) {
        case -508378822:
            if (var10000.equals("clone()Ljava/lang/Object;")) {
                return CGLIB$clone$4$Proxy;
            }
            break;
        case 1535311470:
            if (var10000.equals("sayHello()V")) {
                return CGLIB$sayHello$0$Proxy;
            }
            break;
        case 1826985398:
            if (var10000.equals("equals(Ljava/lang/Object;)Z")) {
                return CGLIB$equals$1$Proxy;
            }
            break;
        case 1913648695:
            if (var10000.equals("toString()Ljava/lang/String;")) {
                return CGLIB$toString$2$Proxy;
            }
            break;
        case 1984935277:
            if (var10000.equals("hashCode()I")) {
                return CGLIB$hashCode$3$Proxy;
            }
        }
 
        return null;
    }
 
    public HelloService$$EnhancerByCGLIB$$be45efdd() {
        CGLIB$BIND_CALLBACKS(this);
    }
 
    public static void CGLIB$SET_THREAD_CALLBACKS(Callback[] var0) {
        CGLIB$THREAD_CALLBACKS.set(var0);
    }
 
    public static void CGLIB$SET_STATIC_CALLBACKS(Callback[] var0) {
        CGLIB$STATIC_CALLBACKS = var0;
    }
 
    private static final void CGLIB$BIND_CALLBACKS(Object var0) {
        HelloService$$EnhancerByCGLIB$$be45efdd var1 = (HelloService$$EnhancerByCGLIB$$be45efdd)var0;
        if (!var1.CGLIB$BOUND) {
            var1.CGLIB$BOUND = true;
            Object var10000 = CGLIB$THREAD_CALLBACKS.get();
            if (var10000 == null) {
                var10000 = CGLIB$STATIC_CALLBACKS;
                if (var10000 == null) {
                    return;
                }
            }
 
            var1.CGLIB$CALLBACK_0 = (MethodInterceptor)((Callback[])var10000)[0];
        }
 
    }
 
    public Object newInstance(Callback[] var1) {
        CGLIB$SET_THREAD_CALLBACKS(var1);
        HelloService$$EnhancerByCGLIB$$be45efdd var10000 = new HelloService$$EnhancerByCGLIB$$be45efdd();
        CGLIB$SET_THREAD_CALLBACKS((Callback[])null);
        return var10000;
    }
 
    public Object newInstance(Callback var1) {
        CGLIB$SET_THREAD_CALLBACKS(new Callback[]{var1});
        HelloService$$EnhancerByCGLIB$$be45efdd var10000 = new HelloService$$EnhancerByCGLIB$$be45efdd();
        CGLIB$SET_THREAD_CALLBACKS((Callback[])null);
        return var10000;
    }
 
    public Object newInstance(Class[] var1, Object[] var2, Callback[] var3) {
        CGLIB$SET_THREAD_CALLBACKS(var3);
        HelloService$$EnhancerByCGLIB$$be45efdd var10000 = new HelloService$$EnhancerByCGLIB$$be45efdd;
        switch(var1.length) {
        case 0:
            var10000.<init>();
            CGLIB$SET_THREAD_CALLBACKS((Callback[])null);
            return var10000;
        default:
            throw new IllegalArgumentException("Constructor not found");
        }
    }
 
    public Callback getCallback(int var1) {
        CGLIB$BIND_CALLBACKS(this);
        MethodInterceptor var10000;
        switch(var1) {
        case 0:
            var10000 = this.CGLIB$CALLBACK_0;
            break;
        default:
            var10000 = null;
        }
 
        return var10000;
    }
 
    public void setCallback(int var1, Callback var2) {
        switch(var1) {
        case 0:
            this.CGLIB$CALLBACK_0 = (MethodInterceptor)var2;
        default:
        }
    }
 
    public Callback[] getCallbacks() {
        CGLIB$BIND_CALLBACKS(this);
        return new Callback[]{this.CGLIB$CALLBACK_0};
    }
 
    public void setCallbacks(Callback[] var1) {
        this.CGLIB$CALLBACK_0 = (MethodInterceptor)var1[0];
    }
 
    static {
        CGLIB$STATICHOOK1();
    }
}
```

重点关注代理对象的`sayHello`方法：

```java
    public final void sayHello() {
        MethodInterceptor var10000 = this.CGLIB$CALLBACK_0;
        if (var10000 == null) {
            CGLIB$BIND_CALLBACKS(this);
            var10000 = this.CGLIB$CALLBACK_0;
        }
 
        if (var10000 != null) {
            var10000.intercept(this, CGLIB$sayHello$0$Method, CGLIB$emptyArgs, CGLIB$sayHello$0$Proxy);
        } else {
            super.sayHello();
        }
    }
```

从代理对象反编译源码可以知道，代理对象继承于`HelloService`，拦截器调用`intercept()`方法，`intercept()`方法由自定义`MyMethodInterceptor`实现，所以最后调用`MyMethodInterceptor`中的`intercept()`方法，从而完成了由代理对象访问到目标对象的动态代理实现。

## JDK 和 CGLIB 动态代理的区别
### 主要区别

- JDK 动态代理：利用拦截器（拦截器必须实现`InvocationHanlder`）加上反射机制生成一个实现代理接口的匿名类，在调用具体方法前调用`InvokeHandler`来处理。
- CGLIB 动态代理：利用 ASM 开源包，对代理对象类的`class`文件加载进来，通过修改其字节码生成子类来处理。

那么，何时使用 JDK 还是 CGLIB 动态代理呢？

- 如果目标对象实现了接口，默认情况下会采用 JDK 的动态代理实现 AOP。
- 如果目标对象实现了接口，可以强制使用 CGLIB 实现 AOP。
- 如果目标对象没有实现了接口，必须采用 CGLIB 库，Spring 会自动在 JDK 动态代理和 CGLIB 之间转换。

更近一步，如何强制使用 CGLIB 实现 AOP 呢？

1. 添加 CGLIB 库，如`aspectjrt-xxx.jar`、`aspectjweaver-xxx.jar`和`cglib-nodep-xxx.jar`等
2. 在 Spring 配置文件中加入`<aop:aspectj-autoproxy proxy-target-class="true"/>`

除此之外，JDK 和 CGLIB 动态代理字节码生成的区别是？

- JDK 动态代理只能对实现了接口的类生成代理，而不能针对类。
- CGLIB 是针对类实现代理，主要是对指定的类生成一个子类，覆盖其中的方法，并覆盖其中方法实现增强，但是因为采用的是继承，所以该类或方法最好不要声明成`final`，对于`final`类或方法，是无法继承的。

还有一个大家比较关心的问题，那就是 JDK 和 CGLIB 哪个速度更快？

- 使用 CGLIB 实现动态代理，CGLIB 底层采用 ASM 字节码生成框架，使用字节码技术生成代理类，在 JDK 6 之前比使用 Java 反射效率要高。唯一需要注意的是，CGLIB 不能对声明为`final`的方法进行代理，因为 CGLIB 原理是动态生成被代理类的子类。
- 在 JDK 6 之后逐步对 JDK 动态代理优化之后，在调用次数较少的情况下，JDK 代理效率高于 CGLIB 代理效率，只有当进行大量调用的时候，JDK 6 和 JDK 7 比 CGLIB 代理效率低一点，但是到 JDK 8 的时候，JDK 代理效率高于 CGLIB 代理。
- 总之，每一次 JDK 版本升级，JDK 代理效率都得到提升，而 CGLIB 代理消息确有点跟不上步伐。

作为我们开发中常用的框架，Spring 是如何选择用 JDK 还是 CGLIB 的呢？

- 当 Bean 实现接口时，Spring 就会用 JDK 的动态代理
- 当 Bean 没有实现接口时，Spring 使用 CGLIB 是实现
- 可以强制使用 CGLIB，在 Spring 配置中加入`<aop:aspectj-autoproxy proxy-target-class="true"/>`

### 代码示例

- 接口

```java
/**
 * 用户管理接口(真实主题和代理主题的共同接口，这样在任何可以使用真实主题的地方都可以使用代理主题代理。)
 * --被代理接口定义
 */
public interface IUserManager {
    void addUser(String id, String password);
}
```

- 接口实现类

```java
/**
 * 用户管理接口实现(被代理的实现类)
 */
public class UserManagerImpl implements IUserManager {
 
    @Override
    public void addUser(String id, String password) {
        System.out.println("======调用了UserManagerImpl.addUser()方法======");
    }
}
```

- JDK 动态代理实现

```java
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
 
/**
 * JDK动态代理类
 */
public class JDKProxy implements InvocationHandler {
    /** 需要代理的目标对象 */
    private Object targetObject;
 
    /**
     * 将目标对象传入进行代理
     */
    public Object newProxy(Object targetObject) {
        this.targetObject = targetObject;
        //返回代理对象
        return Proxy.newProxyInstance(targetObject.getClass().getClassLoader(),
                targetObject.getClass().getInterfaces(), this);
    }
 
    /**
     * invoke方法
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // 一般我们进行逻辑处理的函数比如这个地方是模拟检查权限
        checkPopedom();
        // 设置方法的返回值
        Object ret = null;
        // 调用invoke方法，ret存储该方法的返回值
        ret  = method.invoke(targetObject, args);
        return ret;
    }
 
    /**
     * 模拟检查权限的例子
     */
    private void checkPopedom() {
        System.out.println("======检查权限checkPopedom()======");
    }
}
```

- CGLIB 动态代理实现：

```java
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
 
import java.lang.reflect.Method;
 
/**
 * CGLibProxy动态代理类
 */
public class CGLibProxy implements MethodInterceptor {
    /** CGLib需要代理的目标对象 */
    private Object targetObject;
 
    public Object createProxyObject(Object obj) {
        this.targetObject = obj;
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(obj.getClass());
        enhancer.setCallback(this);
        Object proxyObj = enhancer.create();
        // 返回代理对象
        return proxyObj;
    }
 
    @Override
    public Object intercept(Object proxy, Method method, Object[] args,
                            MethodProxy methodProxy) throws Throwable {
        Object obj = null;
        // 过滤方法
        if ("addUser".equals(method.getName())) {
            // 检查权限
            checkPopedom();
        }
        obj = method.invoke(targetObject, args);
        return obj;
    }
 
    private void checkPopedom() {
        System.out.println("======检查权限checkPopedom()======");
    }
}
```
- 客户端测试类

```java
/**
 * 代理模式[[ 客户端--》代理对象--》目标对象 ]]
 */
public class Client {
    public static void main(String[] args) {
        System.out.println("**********************CGLibProxy**********************");
        CGLibProxy cgLibProxy = new CGLibProxy();
        IUserManager userManager = (IUserManager) cgLibProxy.createProxyObject(new UserManagerImpl());
        userManager.addUser("jpeony", "123456");
 
        System.out.println("**********************JDKProxy**********************");
        JDKProxy jdkPrpxy = new JDKProxy();
        IUserManager userManagerJDK = (IUserManager) jdkPrpxy.newProxy(new UserManagerImpl());
        userManagerJDK.addUser("jpeony", "123456");
    }
}
```

运行上述测试类，其结果如下图所示：

![proxy-test](https://github.com/guobinhit/cg-blog/blob/master/images/others/dynamic-proxy/proxy-test.png)

### 总结
JDK 动态代理不需要第三方库支持，只需要 JDK 环境就可以进行代理，使用条件：

- 实现`InvocationHandler`
- 使用`Proxy.newProxyInstance`产生代理对象
- 被代理的对象必须要实现接口

CGLIB 必须依赖于 CGLIB 的类库，其为需要被代理的类生成一个子类，覆盖其中的方法，实际上是一种继承。



-----------

**参考资料**：

-  [JDK动态代理实现原理(jdk8)](https://blog.csdn.net/yhl_jxy/article/details/80586785)
-  [CGLIB动态代理实现原理](https://blog.csdn.net/yhl_jxy/article/details/80633194)
-  [JDK和CGLIB动态代理区别](https://blog.csdn.net/yhl_jxy/article/details/80635012)
-  [Spring AOP 中 JDK 和 CGLib 动态代理哪个更快？](https://cloud.tencent.com/developer/article/1462784)