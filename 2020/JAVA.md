# JAVA基础
> https://cyc2018.github.io/CS-Notes/#/  
https://github.com/doocs/advanced-java

## ThreadLocal原理：
- Thread类拥有 `ThreadLocal.ThreadLocalMap threadLocals = null;` 成员变量，信息都保存在这里
- `ThreadLocal<T> t = new ThreadLocal<>()` 不会产生任何与线程的关系，但在 `t.set` 时，会将自身，也就是t的引用传入此时线程的 `threadLocals` 里作为key
- `t.get()` 时也会将自身的hashCode作为key

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
我突然想到，static方法能不能重写。
- static方法使用类名调用，在**编译时**已经确定调用哪个方法了，所以子类可以覆写static方法，但是没有多态层面的意义
- 成员方法的重写是多态的体现
- 多态体现在在代码**运行时**才确定使用哪个类、哪个方法

# JVM
## JVM模型
- 内存模型：
  - 方法区
  - 堆
  - 栈帧
  - 本地方法栈
  - 程序计数器 - PC
> 一个**线程**一个**方法栈**、**程序计数器**  
一个**方法**一个**栈帧**  
大家共用一个**堆**

## JVM调试
- 堆内存参数：初始值`-Xms`， 最大值`-Xmx`