# 基本使用
synchronized关键字修饰的代码块会变为临界区，线程间互斥访问。
```java
// 1. 修饰实例方法
public synchronized void print() {
    // TODO
}

// 2. 修饰静态方法
public synchronized static void f() {
    // 3. 锁对象，修饰代码块
    synchronized (Object.class) {
        // TODO
    }
}
```

# 实现原理
> HotSpot JVM中的实现

JVM实现


