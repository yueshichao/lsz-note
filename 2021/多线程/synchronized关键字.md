> 参考: [Java并发系列（14）——synchronized之HotSpot源码解读（上）](https://blog.csdn.net/weixin_38380858/article/details/111054668)  
> [Synchronized原理深度剖析，彻底理解Synchronized的层实现原理](https://zhuanlan.zhihu.com/p/165542160)  

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
synchronized修饰代码块时，我们编译出字节码查看  
```
monitorenter
// do sth
monitorexit
```
查阅资料得知，确实和一个叫**Monitor**的对象有关（有翻译为管程），该对象由JVM实现，维护锁队列    
从字节码来看，`monitorenter`是一个持锁操作，`monitorexit`是一个放锁的操作  
// TODO HostSpot虚拟机源码


# 无锁 -> 偏向锁 -> 轻量锁 -> 重量锁

synchronized锁的是对象，锁的状态自然也就维护在对象头中
> 可以通过**jol**包打印对象头信息，对象需要填充字节对齐内存，按8字节的整数倍填充  
> 操作系统小端存储时，二进制低位存储在低地址空间，高位在高地址，但jol打印是从低位到高位的   

以32位对象头为例，不同阶段的对象头如下：

当**仅有一个线程**（线程A）到来时，改变锁标记位**无锁 -> 偏向锁**，记录线程ID  


| 锁状态 | 25bit          | 4bit                      | 1bit(偏向锁标记位) | 2bit(锁标记位) |
| ------ | -------------- | ------------------------- | ------------------ | -------------- |
| 无锁   | 对象的HashCode | 分代年龄                  | 0                  | 01             |
| 偏向锁 | 线程ID + Epoch | 存储对象类型(Class)的指针 | 1                  | 01             |

当其他线程（线程B）来竞争锁时，通过CAS改变对象头的线程ID，**修改成功 -> 保持偏向锁状态**，**修改失败 -> 升级为轻量级锁**  

此时线程B进入**自旋**（不阻塞，不停重试获取锁），自旋超过一定次数后，将锁**升级为重量锁**，将等待的线程都入队  

| 锁状态   | 30bit                             | 2bit(锁标记位) |
| -------- | --------------------------------- | -------------- |
| 轻量级锁 | 指向栈中锁记录的指针              | 00             |
| 重量级锁 | 指向重量级锁(ObjectMonitor)的指针 | 10             |

> 轻量锁可以关闭或开启，参数是`-XX:-UseBiasedLocking`



> 可以看到上述一系列流程**无锁 -> 偏向锁 -> 轻量级锁 -> 重量级锁**，这是jdk1.5之后做的优化，这些都是为了减少开销，做的这么~~麻烦~~智能，是为了让初级开发者一个synchronized关键字就能锁住代码

但是**软件工程没有银弹**，我们为了性能，不得不了解实现原理，根据业务作出最合理的配置  

例如，在高并发的场景下，大量线程自旋很耗性能，我们可以选择关闭自旋锁，甚至有时偏向锁也可以关闭，因为解除偏向锁时，**需要全局安全点，发生STW——Stop The World**，对于高并发而言，也是很不利的。

# 非公平的
synchronized锁是非公平的。实现时，通过monitor对象维护的两个队列EntryList和WaitSet

// TODO 重入与moniter运行机制
