# JAVA基础
> https://cyc2018.github.io/CS-Notes/#/  
https://github.com/doocs/advanced-java


## [创建对象的几种方式](https://www.cnblogs.com/yunche/p/9530927.html)
假定这样一个对象
```java
public class Animal {
    public void say() {
        System.out.println("hello");
    }
}
```

1. new（不谈了）

2. 反射
    
    1. 直接通过Class对象newInstance
    
       ```java
       Class<?> aClass = Class.forName("com.lsz.Animal");
       
       Animal o = (Animal) aClass.newInstance();
       
       o.say();
       ```
    
       
    
    2. 通过Constructor对象
    
       ```java
       Class<?> aClass = Class.forName("com.lsz.Animal");
       
       Constructor<?> constructor = aClass.getConstructor();// 可以获取不同的构造方法
       
       Animal o = (Animal) constructor.newInstance();
       
       o.say();
       ```
    
3. clone
Animal实现Cloneable接口
```java
public class Animal implements Cloneable {

    public void say() {
        System.out.println("hello");
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}

```

```java
Animal animal = new Animal();
Animal clone = (Animal) animal.clone();
clone.say();
```

4. 序列化
Animal实现Serializable接口，然后通过反序列化拿到对象
```java
Animal animal = new Animal();
// 通过文件保存对象
File f = new File("animal.obj")
try(FileOutputStream fos = new FileOutputStream(f);
    ObjectOutputStream oos = new ObjectOutputStream(fos);
    FileInputStream fis = new FileInputStream(f);
    ObjectInputStream ois = new ObjectInputStream(fis);) {
    oos.writeObject(animal);
    Animal o = (Animal) ois.readObject();
    o.say();
}
```

## ThreadLocal
### 作用与使用方法

- 简单使用
```java
ThreadLocal<String> threadLocal = new ThreadLocal<>();
threadLocal.set("此线程名：" + Thread.currentThread().getName());
String s = threadLocal.get();
System.out.println(s);
threadLocal.remove();
```

- [带初始化值](https://www.cnblogs.com/anhaogoon/p/13280737.html)
> 一般用于线程池，如数据库连接池，一个线程绑定一个Connection对象
```java
ExecutorService executor = Executors.newFixedThreadPool(5);
ThreadLocal<String> threadLocal = new ThreadLocal<String>(){
    @Override
    protected String initialValue() {
        return "默认值";
    }
};
executor.execute(() -> {
    System.out.println(threadLocal.get());
});
```

### 原理
- Thread类拥有 `ThreadLocal.ThreadLocalMap threadLocals = null;` 成员变量，信息都保存在这里（而非保存在ThreadLocal里）
- `ThreadLocal<T> t = new ThreadLocal<>()` 不会产生任何与线程的关系，但在 `t.set` 时，会将自身，也就是t的引用传入此时线程的 `threadLocals` 里作为key
```java
public void set(T value) {
    Thread t = Thread.currentThread();
    ThreadLocalMap map = getMap(t);
    if (map != null)
        map.set(this, value);
     else
        createMap(t, value);
}
```
- `t.get()` 时也会将自身的hashCode作为key拿到之前**保存在ThreadLocalMap**里的对象

### 关于ThreadLocal的内存泄漏
参考：  
https://zhuanlan.zhihu.com/p/128102523  
https://blog.csdn.net/vicoqi/article/details/79743112
https://blog.csdn.net/zhushuai1221/article/details/105440503

> 此处认为内存泄漏是指对象不会再被使用，但一直存在于内存中不被回收
ThreadLocal的使用情况有以下几种：
- ThreadLocal是否是static变量
- 是否使用的线程池

#### 1. 线程池下的static的ThreadLocal
一般情况下，在WEB开发中，我们会定义的是**public final static ThreadLocal threadLocal = new ThreadLocal<>();**  
假如我们用这个来存储权限信息，WEB开发HTTP请求一般也是线程池的情况  
在此情况下如果只threadLocal.set(auth);  
但是没有remove();就会导致本次接口请求结束了，权限信息还在ThreadLocalMap中
毫无疑问，此时是内存泄漏
如果下次接口请求用到了这个线程，你不set(newAuth)，就会导致bug(下次get拿到的是旧值)  
当set新的权限时，由于threadLocal引用未变，那么就会覆盖掉之前的auth  

> 但仍然不建议用完了不remove，全靠set来覆盖，因为你不知道业务逻辑会怎么变，以后会出什么bug
> 内存泄漏下，由于业务逻辑的不同，甚至可能会OOM，例如，下面这段代码就会
```java
    public final static ThreadLocal<Map> threadLocal = new ThreadLocal<>();

    public static void main(String[] args) {
        Random r = new Random();
        while (true) {
            if (threadLocal.get() == null) {
                threadLocal.set(new HashMap());
            }
            Map map = threadLocal.get();
            double value = r.nextDouble();
            map.put(value, value);
//            threadLocal.remove();
        }
    }
```
#### 2. 局部变量的ThreadLocal（何时才会引发ThreadLocalMap的弱引用回收？）
另一种情况，就是局部变量的ThreadLocal，也是很多技术博客提到的JVM会回收弱引用  
ThreadLocalMap里的Entry的key是弱引用，如果ThreadLocal没有强引用，那么会在gc时被回收  
此时就会有Entry的key为null的对象，此时Entry不会再被用到了，但却无法被回收，也可称为内存泄漏  
但一般不会产生OOM，因为在执行set、get、remove方法时都会调用**expungeStaleEntry**去删除key为null的Entry（ThreadLocal的安全措施）  
例如如下代码，内存泄漏不久后，就会被回收掉，不会OOM   
```java
    public static void main(String[] args) {
        Random r = new Random();
        while (true) {
            ThreadLocal threadLocal = new ThreadLocal();
            double value = r.nextDouble();
            threadLocal.set(value);
            System.out.println(threadLocal.get());
        }
    }
```
#### 3. 非线程池
而非线程池的情况，写的太累了，简单说说，就是Thread用完了，ThreadLocalMap（Thread的成员属性）也会被回收，使用不当，会泄漏，但线程结束，也都啥都没了。

### 主线程传递给子线程
InheritableThreadLocal // TODO

## HashMap原理：
- `static class Node<K,V> implements Map.Entry<K, V>` 是HashMap的内部类，（在key冲突不多时）用来存储信息，包括hash、key、value、next四个属性
- `transient Node<K,V>[] table;` 是HashMap实现的本质——Hash表，数组下标就是key的hashcode，数组内容就是Node对象
> 对象在HashMap里的位置取决于key的hashCode()以及equals()，先比较hashCode，再比较equals  
equals不同，hashcode相同，会被认为是不同的对象。但如果equals相同，hashCode却不同，因比较流程（先hashCode再equals）则会被认为是不同的对象  
所以我们要求重写equals，一定要重写hashCode，即保证equals相同时，hashCode一定相同

> 极端情况下，如果重写对象hashCode恒等于1，HashMap也不会出问题，只是会退化成链表。当同一结点下链表长度大于等于8时，链表转化为红黑树

- 使用**EntrySet**遍历HashMap
```java
Map<String, Integer> map = new HashMap<>();
Set<Map.Entry<String, Integer>> entrySet = map.entrySet();
for (Map.Entry<String, Integer> entry : entrySet) {
    String key = entry.getKey();
    Integer value = entry.getValue();
}
```
> EntrySet继承自AbstractSet，却没有add方法，在foreach遍历时，也是在用iterator的方式遍历  
HashMap自身实现了一个**HashIterator**，什么KeySet，ValueSet，EntrySet遍历时，都是他们自己的iterator继承自HashIterator去迭代
他们仅自己实现了next方法（或者说HashIterator什么都给了，他们只取了自己想要的值）  
以EntrySet的迭代器**EntryIterator**为例
```java
final class EntryIterator extends HashIterator
    implements Iterator<Map.Entry<K,V>> {
    public final Map.Entry<K,V> next() { return nextNode(); }
}
```
> 而nextNode()方法，本身就是在对HashMap的底层数据结构**table数组进行遍历**，所以普遍认为EntrySet的方法遍历HashMap是最快的。
```java
final Node<K,V> nextNode() {
    Node<K,V>[] t;
    Node<K,V> e = next;
    if (modCount != expectedModCount)
        throw new ConcurrentModificationException();
    if (e == null)
        throw new NoSuchElementException();
    if ((next = (current = e).next) == null && (t = table) != null) {
        do {} while (index < t.length && (next = t[index++]) == null);
    }
    return e;
}
```

## [synchronized](https://juejin.im/post/6854573221258199048)
1. 作用于方法、代码块
2. JVM基于Monitor（对象头标记）实现

## java.util.concurrent
### [AbstractQueuedSynchronizer](https://segmentfault.com/a/1190000015562787)
参考：
https://github.com/TangBean/Java-Concurrency-in-Practice/blob/master/Ch3-Java%E5%B9%B6%E5%8F%91%E9%AB%98%E7%BA%A7%E4%B8%BB%E9%A2%98/03-AQS%E6%A1%86%E6%9E%B6.md
https://github.com/qiurunze123/threadandjuc/blob/master/docs/AQS.md
> 队列同步器，简写AQS  
既然是抽象类，自然是用来被继承的，J.U.C包下的很多类都继承或组合了这个类  
抽象类主要是封装麻烦的细节，子类重写部分方法即可完成定制的类
AQS就是模板方法设计模式，存在大量的final方法，我们称之为skeleton method  
> jdk1.8上关于此类的说明：This class is designed to be a useful basis for most kinds of synchronizers that rely on a single atomic {@code int} value to represent state
1. 从ReentrantLock(以下简称Lock)看AQS
Lock与AQS是**组合**的关系，Lock可以是公平锁，也可以是非公平锁，由构造器传参决定。
公平锁由**FairSync**实现，非公平锁由**NonfairSync**实现，他们都继承自**Sync**，Sync继承自AQS
下面仅看公平锁部分**关键**代码
```java
public class ReentrantLock implements Lock {
    private final Sync sync;

    abstract static class Sync extends AbstractQueuedSynchronizer {
        abstract void lock();

        protected final boolean tryRelease(int releases) {
            // 释放锁资源后的锁值
            int c = getState() - releases;
            boolean free = false;
            // 资源
            if (c == 0) {
                free = true;
                setExclusiveOwnerThread(null);
            }
            setState(c);
            return free;
        }
    }

    static final class FairSync extends Sync {
        private static final long serialVersionUID = -3000897897090466540L;

        final void lock() {
            acquire(1);
        }

        protected final boolean tryAcquire(int acquires) {
            final Thread current = Thread.currentThread();
            int c = getState();
            if (c == 0) {
                if (!hasQueuedPredecessors() &&
                    compareAndSetState(0, acquires)) {
                    setExclusiveOwnerThread(current);
                    return true;
                }
            }
            else if (current == getExclusiveOwnerThread()) {
                int nextc = c + acquires;
                if (nextc < 0)
                    throw new Error("Maximum lock count exceeded");
                setState(nextc);
                return true;
            }
            return false;
        }
    }
}
```

2. AQS排他锁的简单演示
```java
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;

public class Main {

    public static void main(String[] args) throws InterruptedException {
        SyncDemo sync = new SyncDemo();

        // 拿到互斥资源
        System.out.println("Main acquire(1)");
        sync.acquire(1);

        new Thread(() -> {
            // 拿到2份互斥资源
            sync.acquire(2);
            System.out.println("Sub acquire(2)");
            System.out.println("Sub release(1)");
            // 分两次release资源
            sync.release(1);
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("Sub release(1)");
            sync.release(1);
        }).start();

        // 等待两秒再释放资源
        TimeUnit.SECONDS.sleep(2);
        System.out.println("Main release(1)");
        sync.release(1);

        // 等待10ms，让子线程先acquire
        TimeUnit.MILLISECONDS.sleep(10);
        sync.acquire(1);
        System.out.println("Main end...");

    }

    static class SyncDemo extends AbstractQueuedSynchronizer {

        @Override
        protected boolean tryAcquire(int arg) {
            int expect = 0;
            boolean b = compareAndSetState(expect, arg);
//            System.out.printf("compareAndSetState(%d, %d) = %s\n", expect, arg, b);
            return b;
        }

        @Override
        protected boolean tryRelease(int arg) {
//            System.out.printf("state = %d, arg = %d\n", getState(), arg);
            int currentState = getState() - arg;
            setState(currentState);
            return currentState == 0;
        }
    }
    
}
```

## ConcurrentHashMap、Hashtable对比
首先HashMap不支持多线程环境，这俩都支持。在并发量较大时，ConcurrentHashMap表现比Hashtable更好，因为Hashtable是在put方法上加锁，而ConcurrentHashMap是在key所在的hash下标那加锁的

## 序列化，实现Serializable接口
 - 自定义序列化内容：`transient`修饰，重写`writeObject`和`readObject`，由ObjectOutputStream通过反射调用

## JDK动态代理
```java

public class Main {

    public static void main(String[] args) {
        IHello hello = new Hello();
        ProxyHandler proxyHandler = new ProxyHandler(hello);
        IHello proxyHello = (IHello) Proxy.newProxyInstance(hello.getClass().getClassLoader(), hello.getClass().getInterfaces(), proxyHandler);
        proxyHello.sayHello();
    }
}

interface IHello {
    void sayHello();
}

class Hello implements IHello {

    @Override
    public void sayHello() {
        System.out.println("Hello");
    }
}

class ProxyHandler implements InvocationHandler {

    private Object object;

    public ProxyHandler(Object object) {
        this.object = object;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        System.out.println("Before invoke...");
        method.invoke(object, args);
        System.out.println("After invoke...");
        return null;
    }
}
```

## List.toArray()的类型强转
```java
List<String> collect = Stream.of("1", "2", "3").collect(Collectors.toList());
String[] strings = (String[])collect.toArray();// java.lang.ClassCastException: [Ljava.lang.Object; cannot be cast to [Ljava.lang.String;
System.out.println(Arrays.toString(strings));
```
> 原因有两点：
> 1. 泛型只是在编译期有效，编译成字节码后就没泛型信息了。  
且ArrayList内部使用了`Object[]`来保存数据的，toArray()仅仅是将`Object[]`复制了一份给出
> 2. Java中数组实例是对象（引用类型），也就是说`int[]`是对象，`String[]`是对象，`Object[]`是对象，`String[]`与`Object[]`无继承关系，故不能强转

> ArrayList之所以用`Object[]`来存储数据，而不用`T[]`，是因为创建数组必须在编译时就指定类型

想要强转成功，需要一个一个元素的强转。或调用`toArray(T[] a)`即可;
```java
List<String> collect = Stream.of("1", "2", "3").collect(Collectors.toList());
String[] strings = collect.toArray(new String[collect.size()]);
System.out.println(Arrays.toString(strings));
```

## 重写static方法
> 今天看Spring AOP原理，发现一个抽象类**AopProxyUtils**，没有任何类继承它，里面全是静态方法，这种方式写工具类也不错。  
我突然有个疑问，static方法能不能重写？
- static方法一般使用类名调用，在**编译时**已经确定调用哪个方法了，所以子类可以覆写static方法（并不是继承重写，可以理解为两个同名方法在不同的类里），但是没有多态层面的意义
- 成员方法的重写是多态的体现
- 多态体现在在代码**运行时**才确定使用哪个类、哪个方法  
[什么叫做编译时已经确定调用哪个方法？](##静态调用、动态调用)

# JVM
> 未特别指明，均为HotSpot虚拟机

## JVM模型
- 内存模型：
  - 方法区
  - 堆
  - 栈帧
  - 本地方法栈
  - 程序计数器 - PC
> 一个**线程**对应一个**方法栈**、**程序计数器**  
一个**方法**对应一个**栈帧**  
大家共用一个**堆**

### 堆内存
堆内存用来存放对象和方法，分为三个部分  
1. 新生代
由Eden、Survivor0、Survivor1组成，默认大小比例为8：1：1
回收算法：复制算法

2. 老年代
新生代中年龄大于15的会进入老年代
回收算法：标记整理算法

3. 永久代\元空间
逻辑上属于堆内存，用来存放方法，

> 堆内存默认的最小内存为物理内存的1/64，最大内存为物理内存的1/4（待确认，我的测试结果和这个不一致）

> 对象不止可以分配在堆上，还可以分配到[栈上](https://www.cnblogs.com/BlueStarWei/p/9358757.html)  
当对象作用域仅在本方法内（逃逸分析），就可以把对象打散分配到栈上（标量替换）

## Java文件执行流程
Java不能完全叫编译型或是解释型语言
执行流程是： *.java* 文件编译成 *.class字节码文件* ，再通过*执行引擎*解释执行字节码，但热点代码也会被*JIT*编译成机器码。

## 静态调用、动态调用
- 静态调用是指在编译时确定调用哪个方法，如构造器、private方法、static方法都是**解析字节码阶段**确定的  
> 字节码：*invokespecial*、*invokestatic*
- 动态调用，有说法叫做虚方法，名字不重要，关键是**运行时**根据上下文才知道具体调用什么方法，继承重写的方法一般就是动态调用  
> 字节码*invokevirtual*
下面比较一下静态方法和继承来的方法调用的不同：
先给出两个类，p()是继承重写的方法，make()是静态方法
```java
class Box {

    public void p() {
        System.out.println("Box private");
    }
    public static void make() {
        System.out.println("Box make");
    }
}

class Car extends Box {

    public void p() {
        System.out.println("Car private");
    }

    public static void make() {
        System.out.println("Car make!!!");
    }

    public void f() {
        super.make();
        make();
        p();
    }
}
```

**方法f()**的字节码信息
```class
 0 invokestatic #6 <com/xxx/bean/Box.make>
 3 invokestatic #7 <com/xxx/bean/Car.make>
 6 aload_0
 7 invokevirtual #8 <com/xxx/bean/Car.p>
10 return
```


## JVM调试
- 堆内存参数：初始值`-Xms`， 最大值`-Xmx`
- -XX:+PrintGC