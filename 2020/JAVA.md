# JAVA基础
> https://cyc2018.github.io/CS-Notes/#/  
https://github.com/doocs/advanced-java

## 容器
### 栈
如果没有线程安全的要求，尽量选择**ArrayDeque**实现栈，而不是**Stack**

> 因为Stack继承自Vector，维护线程安全有一定的开销，<del>并且Stack直接继承自Vector，可以通过实例化的Stack直接调用Vector的方法，在设计上不够安全</del>

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

5. Unsafe类

   ```java
   try {
       // 通过反射拿到Unsafe对象
       Field field = Unsafe.class.getDeclaredField("theUnsafe");
       field.setAccessible(true);
       Unsafe unsafe = (Unsafe) field.get(null);
       // 实例化对象
       Animal o = (Animal) unsafe.allocateInstance(Animal.class);
       System.out.println(o);
   } catch (NoSuchFieldException | IllegalAccessException | InstantiationException e) {
       e.printStackTrace();
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
1. 重写initialValue  
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
2. withInitial方式  
```java
// 原理也是重写了initialValue
ThreadLocal<String> threadLocal = ThreadLocal.withInitial(() -> "默认值");
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
> 参考：  
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

### InheritableThreadLocal 主线程传递给子线程

Thread#init()中实现ThreadLocal的拷贝，核心代码如下：  

```java
if (inheritThreadLocals && parent.inheritableThreadLocals != null)
            this.inheritableThreadLocals =
                ThreadLocal.createInheritedMap(parent.inheritableThreadLocals);
```
> 因为是在init时拷贝，而线程池原理是复用，所以线程池无法继承InheritableThreadLocal  


## HashMap原理：

- `static class Node<K,V> implements Map.Entry<K, V>` 是HashMap的内部类，（在key冲突不多时）用来存储信息，包括hash、key、value、next四个属性  
- `transient Node<K,V>[] table;` 是HashMap实现的本质——Hash表，数组下标就是(key的hashcode & n-1)，n是数组长度，数组内容就是Node对象  

### 重写equals()一定要重写hashCode()

在Java中，我们约定**euqals()才是真正评判两对象是否相同的标准**，而hashCode()是equals()的**快速比较**，如果hashCode()相同，那么equals()可能相同，hashCode()不同，则equals()肯定不同，有点布隆过滤器的感觉了：'False is always false'  

在容器的对象比较中，比如HashSet和HashMap，equals不同，hashcode相同，会被认为是不同的对象。但如果equals相同，hashCode却不同，因**比较流程（先hashCode再equals）**则会被认为是不同的对象  
所以我们要求重写equals，一定要重写hashCode，即保证**equals相同时，hashCode一定相同，hashCode不同，euqals也一定不同**，反之则没必要，hashCode相同，不代表equals为true  

> 极端情况下，如果重写对象hashCode恒等于1，HashMap也不会出问题，只是会退化成链表。当同一结点下链表长度大于等于8时，链表转化为红黑树  

- 下面演示hashCode()不一致，equals返回true导致的Set去重失效的例子：
```java
@Test
public void test() {
    log.info("this is a test case...");
    // hashCode
    Dog dog1 = new Dog(1, "dog");
    System.out.printf("dog1, hashCode = %s \n", dog1.hashCode());
    Dog dog2 = new Dog(1, "dog");
    System.out.printf("dog2, hashCode = %s \n", dog2.hashCode());

    System.out.printf("dog1.hashCode() == dog2.hashCode() = %s  \n", dog1.hashCode() == dog2.hashCode());
    System.out.printf("dog1.equals(dog2) = %s  \n", dog1.equals(dog2));
    // 容器中的表现，在set和map中都无法按equals去重，因为hashCode提前判定不一致了
    Set<Dog> set = new HashSet<>();
    set.add(dog1);
    set.add(dog2);
    System.out.println("set = " + set);

    Map<Dog, Integer> map = new HashMap<>();
    map.put(dog1, 1);
    map.put(dog2, 2);
    System.out.println("map = " + map);

}

static class Dog {
    int age;
    String name;

    public Dog(int age, String name) {
        this.age = age;
        this.name = name;
    }

    @Override
    public String toString() {
        return "Dog{age=" + age + ", name='" + name + "\'}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Dog dog = (Dog) o;
        return age == dog.age &&
                Objects.equals(name, dog.name);
    }

    // @Override
    // public int hashCode() {
    //     return Objects.hash(age, name);
    // }

}
```

### 使用**EntrySet**遍历HashMap
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
他们仅自己实现了next方法（用了模板方法，HashIterator把迭代细节都实现了，子类只要迭代拿值就行了）  
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

### resize()扩容

原理：oldTable 重新计算hash槽位 -> newTable  
为什么2倍扩容，因为hash槽位计算是是 ```(n - 1) & hash``` ，其中n是数组长度，扩容后可以使同一hash槽的元素部分移动，从而形成分布均匀的hashMap

> 例如，当前数组长度8，*(n - 1) & hash* 一式中，n = 8，n-1 = 7（二进制位111），假设两个元素的hash值的二进制表示分别为10111,11111，代入式子中，计算hash槽位：都是111，所以会串成链表，挂在111槽位下。  
> 如果元素过多，需要扩容，table长度扩大两倍，n = 16，n-1 = 15（二进制位1111），之前两个元素hash值重新计算，重新分布，分别是0111槽位（不变）和1111槽位（该元素移动）。  

### jdk1.7的头插法在1.8改为尾插法
> [HashMap的链表成环演示](https://blog.csdn.net/insomsia/article/details/93882739)  
> [探究HashMap线性不安全（二）——链表成环的详细过程](https://www.cnblogs.com/lonelyjay/p/9726187.html)  

头插法代码：  
```java
void transfer(Entry[] newTable, boolean rehash) {
    int newCapacity = newTable.length;
    //遍历table数组中键值对链
    for (Entry<K,V> e : table) {
        //遍历键值对e链上的所有键值对，当e指向null时结束
        while(null != e) {
            Entry<K,V> next = e.next;//断点一
            //通常rehash为false，不会重新计算键值对key的hash值
            if (rehash) {
                e.hash = null == e.key ? 0 : hash(e.key);
            }
            //根据扩容后的table数组长度计算键值对的index
            int i = indexFor(e.hash, newCapacity);
            //头插法，将后遍历的键值对存到链条的头部
            e.next = newTable[i];
            newTable[i] = e;
            //链条中的下一个键值对继续执行while循环。
            e = next;
        }
    }
}
```

```
a->null
线程1：table[i] = a -> null
线程2：a -> table[i] = a
```

## [synchronized](https://juejin.im/post/6854573221258199048)
1. 作用于方法、代码块
2. JVM基于Monitor（对象头标记）实现

## java.util.concurrent
### [AbstractQueuedSynchronizer](https://segmentfault.com/a/1190000015562787)
> 参考：
> [AbstractQueuedSynchronizer 框架 (AQS)](https://github.com/TangBean/Java-Concurrency-in-Practice/blob/master/Ch3-Java%E5%B9%B6%E5%8F%91%E9%AB%98%E7%BA%A7%E4%B8%BB%E9%A2%98/03-AQS%E6%A1%86%E6%9E%B6.md)  
> [AbstractQueuedSynchronizer 详细解析 一切的基础](https://github.com/qiurunze123/threadandjuc/blob/master/docs/AQS.md)  

> 队列同步器，简写AQS  
作者Doug Lea，是CLH队列锁的一种变体  
可以看到AQS是抽象类，抽象类自然是用来被继承的，J.U.C包下的很多类都使用继承或组合的方式使用了这个类  
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

### AQS是CLH的变体
> [Java AQS 核心数据结构-CLH 锁](https://juejin.cn/post/6942753984436404255)  
```java
public class CLH {

    private final AtomicReference<Node> tail = new AtomicReference<>(new Node());
    private final ThreadLocal<Node> node = ThreadLocal.withInitial(Node::new);

    private static class Node {
        private volatile boolean locked;
    }

    public void lock() {
        Node node = this.node.get();
        node.locked = true;
        Node pre = this.tail.getAndSet(node);
        while (pre.locked);
    }

    public void unlock() {
        Node node = this.node.get();
        node.locked = false;
        this.node.set(new Node());
    }

}

```



## ConcurrentHashMap、Hashtable对比
首先HashMap不支持多线程环境，这俩都支持。在并发量较大时，ConcurrentHashMap表现比Hashtable更好，因为Hashtable是在put方法上加锁，而ConcurrentHashMap是在key所在的hash下标那加锁的

## BlockingQueue 阻塞队列
```java
BlockingQueue<String> queue = new ArrayBlockingQueue<>(10);
final long producerVelocity = 10L;// 每秒生产3个
final long consumerVelocity = 1L;// 每秒消费5个
// 生产者
new Thread(() -> {
    int i = 0;
    while (true) {
        // 如果队内元素个数超出容量，返回false
        queue.offer(++i + "");
        try {
            TimeUnit.MILLISECONDS.sleep(1000 / producerVelocity);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}).start();

// 消费者
new Thread(() -> {
    while (true) {
        try {
            // 当队列中无元素时，take会阻塞线程
            String take = queue.take();
            System.out.println(take);
            TimeUnit.MILLISECONDS.sleep(1000 / consumerVelocity);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}).start();
```


## 多线程
> 参考：
https://www.cnblogs.com/jinggod/p/8484674.html  
https://www.cnblogs.com/myseries/p/10895078.html  

定义：线程是轻量级的进程，进程是资源分配的最小单位，线程是资源调度的最小单位，线程共享进程的资源  

1. 线程的常用方法
```java
AtomicInteger i = new AtomicInteger(0);
// 创建线程
Thread thread = new Thread(() -> {
    while (true) System.out.println(i.getAndIncrement());
});
// 设置为守护线程（守护线程在所有用户线程结束后中断）
thread.setDaemon(true);
thread.setName("abc");
// 启动线程，
thread.start();

// 调用此方法的线程（主线程）等待thread线程
thread.join();
// 将中断标记位置为true，至于线程是否、何时终止，交由线程自己决定（一般线程在每次迭代时判断终止标记位）
thread.interrupt();

TimeUnit.MILLISECONDS.sleep(100);
```

2. 线程池

## ExecutorService
### 基本用法
```java
// 固定线程数量的线程池
ExecutorService executor0 = Executors.newFixedThreadPool(1);
// 单个线程数量的线程池
ExecutorService executor1 = Executors.newSingleThreadExecutor();
// 缓存线程的线程池
ExecutorService executor2 = Executors.newCachedThreadPool();
// 定期任务线程池
ScheduledExecutorService executor3 = Executors.newScheduledThreadPool(1);

// 0. execute执行任务
executor0.execute(() -> System.out.println("task0"));

// 1. submit提交任务，返回future对象，任务结果和异常都放在future对象里
Future<Object> future1 = executor1.submit(() -> {
    throw new RuntimeException("task1");
});
try {
    // 如果不get，task1的异常也不会打印出来
    future1.get();
} catch (InterruptedException | ExecutionException e) {
    e.printStackTrace();
}

// 2. submit提交任务，传入result，返回future对象
Future<String> future2 = executor2.submit(() -> System.out.println("task2"), "task2 result");
try {
    System.out.println(future2.get());
} catch (InterruptedException | ExecutionException e) {
    e.printStackTrace();
}

// 3. task3，定时任务，延迟0s，周期1s
executor3.scheduleAtFixedRate(() -> System.out.println("task3"), 0L, 1L, TimeUnit.SECONDS);

// 4. 不再接受新的任务
executor0.shutdown();
executor1.shutdown();
executor2.shutdown();
executor3.shutdown();
```

以上通过Executors静态方法创建的线程池，实际都是直接或间接的调用ThreadPoolExecutor构造方法。  
阿里巴巴编程规范中**不建议使用以上方法生成线程池**，原因如下，以newFixedThreadPool方法举例  
```java
public static ExecutorService newFixedThreadPool(int nThreads) {
    return new ThreadPoolExecutor(nThreads, nThreads,
                                    0L, TimeUnit.MILLISECONDS,
                                    new LinkedBlockingQueue<Runnable>());
}
```
LinkedBlockingQueue作为阻塞队列，没有指定容量时，则**int最大值为默认容量**，内存资源不够时，是可能发生OOM的  


### 通过ThreadPoolExecutor自定义线程池
```java
ThreadPoolExecutor executor = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(1), Executors.defaultThreadFactory(), new ThreadPoolExecutor.AbortPolicy());

// task1
executor.execute(() -> {
    System.out.println("run task 1");
    try {
        TimeUnit.SECONDS.sleep(1);
    } catch (InterruptedException e) {
        e.printStackTrace();
    }
});

// task2
executor.execute(() -> System.out.println("run task 2"));

// task3会被拒绝，因为task1被消费掉，task2还在阻塞队列中（容量为1），阻塞队列offer(task3)时返回false
try {
    executor.execute(() -> System.out.println("run task 3"));
} catch (RejectedExecutionException e) {
    e.printStackTrace();
    System.out.println("task 3被拒绝!");
}

System.out.println("executor shutdown...");
executor.shutdown();
```


```java
while (true) {
    try {
        TimeUnit.MILLISECONDS.sleep(2);
    } catch (InterruptedException e) {
        e.printStackTrace();
    }
    System.out.println("---");
    ExecutorService executor = Executors.newFixedThreadPool(4);
    executor.execute(() -> {
        System.out.println("1");
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    });

    executor.execute(() -> {
        System.out.println("2");
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    });
    // executor.shutdown(); // shutdown后会回收已经结束的线程对象
}
```
以上代码会OOM：*java.lang.OutOfMemoryError: unable to create new native thread*
加入shutdown后，会对线程池对象进行GC，所以不需要的线程池，记得及时**shutdown**


## 序列化，实现Serializable接口
> 参考：  
https://blog.csdn.net/u014653197/article/details/78114041  

### 自定义序列化内容：
`transient`修饰不需要序列化的属性
加上成员方法`private void writeObject(ObjectOutputStream oos) throws IOException`  
和`private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {}`  
控制序列化过程(这两个方法由ObjectOutputStream通过反射调用）



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

## String (jdk1.8)
### "+"运算符拼接字符串
```java
String a = "a";
String b = "b";
// a、b至少有一个非常量引用
String s0 = a + b;
```
字节码如下
```bytecode
 0 ldc #2 <a>
 2 astore_1
 3 ldc #3 <b>
 5 astore_2
 6 new #4 <java/lang/StringBuilder>
 9 dup
10 invokespecial #5 <java/lang/StringBuilder.<init>>
13 aload_1
14 invokevirtual #6 <java/lang/StringBuilder.append>
17 aload_2
18 invokevirtual #6 <java/lang/StringBuilder.append>
21 invokevirtual #7 <java/lang/StringBuilder.toString>
24 astore_3
25 return
```
可以看出+运算符，在JVM编译后，本质是new了**StringBuilder**去append，最后调用StringBuilder的toString()赋值  
StringBuilder#toString()源码如下：
```java
    @Override
    public String toString() {
        // Create a copy, don't share the array
        return new String(value, 0, count);
    }
```
这种new String(char value[], int offset, int count)的方式，会在**堆中**创建对象，但**常量池中没有**


> 常量、常量引用的拼接，都会在编译期优化，例如："a" + "b"在编译时会直接变成"ab"

### intern()
> 参考：  
[【译】Java中的字符串字面量](https://www.cnblogs.com/justcooooode/p/7670256.html])  
[Java 中new String("字面量") 中 "字面量" 是何时进入字符串常量池的?](https://www.zhihu.com/question/55994121)  

代码片段1：
```java
String s0 = new String("a") + new String("b");
// s0.intern();
String s1 = "ab";
System.out.println(s0 == s1);// false
```
代码片段2：
```java
String s0 = new String("a") + new String("b");
s0.intern();
String s1 = "ab";
System.out.println(s0 == s1);// true
```

上面代码中  
```String s0 = new String("a") + new String("b");```  
等价于  
```String s0 = new StringBuilder().append("a").append("b").toString();```  
等价于  
```String s0 = new String(new char[]{'a', 'b'}, 0, 2);```


在代码片段1中，s0作为new出来的String对象，位置在堆中，而s1是直接申明的字符串，在常量池中，所以s0 != s1  
在代码片段2中，intern()的作用就是寻找常量池中是否有"ab"，如果没有，在常量池中增加一个引用指向s0，当```String s1 = "ab";```时直接拿了常量池中的引用

思考：
```java
String s1 = new String("he") + new String("llo");
String s2 = new String("h") + new String("ello");
String s3 = s1.intern();// 第3行
String s4 = s2.intern();// 第4行
System.out.println(s1 == s3);// true
System.out.println(s1 == s4);// true
System.out.println(s2 == s3);// false
System.out.println(s2 == s4);// false
```
提示：  
第3行，第4行代码互换顺序，结果也会不一样


## 内部类
```java
public class Main {

    public static void main(String[] args) {
        class A {

        }
    }

    static class B {

    }

    class C {

    }

}
```

对上述代码编译后，出现4个class文件：`Main.class`, `Main$1A.class`, `Main$B.class`, `Main$C.class`  
对ABC三个class文件反编译得到：
```java
class Main$1A {
    Main$1A() {
    }
}
```

```java
class Main$B {
    Main$B() {
    }
}
```

```java
class Main$C {
    // 这里的var1就是Main的实例
    Main$C(Main var1) {
        this.this$0 = var1;
    }
}
```
要实例化C，需如下操作
```java
Main main = new Main();
C c = main.new C();
```


## javadoc

### 根据第三方source-jar包生成javadoc

> 参考：  
> https://tianmingxing.com/2019/07/16/%E4%BB%8E%E6%BA%90%E7%A0%81JAR%E5%8C%85%E4%B8%AD%E7%94%9F%E6%88%90javadocHTML%E9%A1%B5%E9%9D%A2/  


```sh

# 解压源码jar包  
jar vxf ./mockito-core-2.15.0-sources.jar  
# -d参数：解析的doc目录，-sourcepath：解压后的源码文件  
javadoc -d ~/code/tmp/mockito_doc -sourcepath ~/code/tmp/mockito -subpackages org.mockito  

```

源码文件可以从IDEA上download，但存储位置不一定是maven仓库  
可以去打开IDEA，从 **project structure** 中，找到 **libraries** 选项卡，找到需要的jar的source所在目录  
