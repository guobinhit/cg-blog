# 详述获取字节码文件及其内容的方法

1 简述
----

Java 的反射机制是指：

 - **在运行状态中，对任意一个类（class文件），都能知道这个类的所有属性和方法；对任意一个对象，都能调用这个对象的方法和属性**。

简单点说，这种动态的获取信息和动态的调用对象的方法的功能就是 Java 的反射机制。利用 Java 的反射机制，我们可以很容易的获取类的详细信息，如构造函数、成员变量和成员函数等。

2 获取字节码文件
---------

首先，构造一个实体类：

```
/**
 * @Author Charies Guo
 * @Date 2017/7/29,下午5:15
 * @Description Person Entity
 */
public class Person {
    private String name;
    private int age;

    public Person() {
        System.out.println("Person run");
    }

    public Person(String name, int age) {
        this.name = name;
        this.age = age;
        System.out.println("Person param run ... " + this.name + " is " + this.age + " years old!");
    }

    public void showInfo(String name, int age){
        this.name = name;
        this.age = age;
        System.out.println("Show Peron Info : " + this.name + " is " + this.age + " years old!");
    }

    public void playBadminton(){
        System.out.println("Let's play badminton, go go go...");
    }
}
```
接下来，以 Person 类为例，演示获取字节码文件的 3 种方式：

```
/**
 * @Author Charies Guo
 * @Date 2017/7/29,下午5:23
 * @Description Get class file
 */
public class GetClassFile {
    public static void main(String[] args) throws ClassNotFoundException {
        System.out.println("第 1 种获取方式：");
        getClassObject_1();
        System.out.println("第 2 种获取方式：");
        getClassObject_2();
        System.out.println("第 3 种获取方式：");
        getClassObject_3();
    }

    /**
     * 利用 Object 类中的 getClass 方法
     * 用这个方法时，必须明确具体的类，并创建对象
     * 比较麻烦
     */
    public static void getClassObject_1() {
        Person p = new Person();
        Class clazz = p.getClass();
        Person p1 = new Person("Charies",18);
        Class clazz1 = p1.getClass();
        System.out.println(clazz == clazz1);
    }

    /**
     * 任何数据类型都具备一个静态属性
     * 通过 .class 来获取对应的 Class 对象
     * 扩展性较差
     */
    public static void getClassObject_2() {
        Class clazz = Person.class;
        Class clazz1 = Person.class;
        System.out.println(clazz == clazz1);
    }

    /**
     * 通过给定的类的字符串名称就可以获取该类的字节码文件，更利于扩展
     * 可以用 Class 类中的 forName() 方法来完成
     */
    public static void getClassObject_3() throws ClassNotFoundException {
        // 包名一定要写全，否则会报 java.lang.ClassNotFoundException 异常
        String className = "Person";
        Class clazz = Class.forName(className);
        System.out.println(clazz);
    }
}
```
执行上述代码，结果如下图所示：

![class](http://img.blog.csdn.net/20170729175959252)

3 获取字节码文件的内容
---------

### 3.1 获取构造函数

```
import java.lang.reflect.Constructor;

/**
 * @Author Charies Guo
 * @Date 2017/7/29,下午6:07
 * @Description Get class constructor
 */
public class GetClassConstructor {
    public static void main(String[] args) throws Exception {
        createNewObject_1();
        createNewObject_2();
    }

    /**
     * 获取默认构造函数
     */
    public static void createNewObject_1() throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        String name = "Person";
        // 寻找该名称类文件，并加进内存，产生 Class 对象
        Class clazz = Class.forName(name);
        // 产生该类的实例对象（空参）
        Object obj = clazz.newInstance();
    }

    /**
     * 获取带参数的构造函数
     */
    public static void createNewObject_2() throws Exception {
        /**
         *  当获取指定名称对应类中的实体对象时，而且该对象的初始化不适用空参的构造函数
         *  可以先通过该类的字节码文件对象，获取空参的构造函数
         *  该方法为：getConstructor(parameterTypes)
         */

        // 包名一定要写全，否则会报 java.lang.ClassNotFoundException 异常
        String name = "Person";
        // 找寻该名称类文件，并加进内存，产生 Class 对象
        Class clazz = Class.forName(name);
        // 获取指定的构造函数对象
        Constructor constructor = clazz.getConstructor(String.class, int.class);
        // 通过该构造器对象的 newInstance 方法进行对象的初始化
        constructor.newInstance("Charies", 18);
    }
}
```



### 3.2  获取成员变量

```
import java.lang.reflect.Field;

/**
 * @Author Charies Guo
 * @Date 2017/7/29,下午6:15
 * @Description Get class field
 */
public class GetClassField {
    public static void main(String[] args) throws Exception {
        getField();
    }

    /**
     * 获取字节码文件中的成员变量
     */
    public static void getField() throws Exception {
        Class clazz = Class.forName("Person");
        Field field = null;
        // 获取本类字段，包含私有
        field = clazz.getDeclaredField("age");
        // 对私有字段的访问取消权限检查，可称之为暴力访问
        field.setAccessible(true);
        Object obj = clazz.newInstance();
        field.set(obj, Integer.valueOf(18));
        Object o = field.get(obj);
        System.out.println(o);
    }
}
```



### 3.3 获取成员函数

```
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/**
 * @Author Charies Guo
 * @Date 2017/7/29,下午6:22
 * @Description Get class method
 */
public class GetClassMethod {
    public static void main(String[] args) throws Exception {
        System.out.println("第 1 个方法：");
        getMethod_1();
        // System.out.println("第 2 个方法：");
        // getMethod_2();
        // System.out.println("第 3 个方法：");
        // getMethod_3();
    }

    /**
     * 获取指定 Class 中的公有函数
     */
    public static void getMethod_1() throws Exception {
        Class clazz = Class.forName("Person");

        // 获取的都是类中的公有方法
        Method[] methods = clazz.getMethods();

        // 获取本类中的所有方法
        methods = clazz.getDeclaredMethods();
        Method[] var5 = methods;
        int var4 = methods.length;

        for (int var3 = 0; var3 < var4; ++var3) {
            Method method = var5[var3];
            System.out.println(method);
        }
    }

    /**
     * 获取指定 Class 中的空参函数
     */
    public static void getMethod_2() throws Exception {
        Class clazz = Class.forName("Person");
        // 获取空参数的方法
        Method method = clazz.getMethod("playBadminton");
        Constructor constructor = clazz.getConstructor(new Class[]{String.class, Integer.TYPE});
        Object obj = constructor.newInstance(new Object[]{"Charies", Integer.valueOf(18)});
        method.invoke(obj, (Object[]) null);
    }

    /**
     * Integer.TYPE 等价于 int.class
     */
    public static void getMethod_3() throws Exception {
        Class clazz = Class.forName("Person");
        Method method = clazz.getMethod("showInfo", new Class[]{String.class, Integer.TYPE});
        Object obj = clazz.newInstance();
        method.invoke(obj, new Object[]{"Charies", Integer.valueOf(18)});
    }
}
```


