
# JVM
> 未特别指明，均为HotSpot虚拟机

## JMM - Java Memory Model - Java内存模型

> 参考：[面试官问我什么是JMM](https://zhuanlan.zhihu.com/p/258393139)

JMM的存在是为了屏蔽各个操作系统、计算机硬件间的差异  
JMM满足的性质：原子性，可见性，有序性  
synchronized满足原子性，volatile满足可见性  

### 8种内存交互操作
- lock(锁定)，作用于主内存中的变量，把变量标识为线程独占的状态。
- read(读取)，作用于主内存的变量，把变量的值从主内存传输到线程的工作内存中，以便下一步的load操作使用。
- load(加载)，作用于工作内存的变量，把read操作主存的变量放入到工作内存的变量副本中。
- use(使用)，作用于工作内存的变量，把工作内存中的变量传输到执行引擎，每当虚拟机遇到一个需要使用到变量的值的字节码指令时将会执行这个操作。
- assign(赋值)，作用于工作内存的变量，它把一个从执行引擎中接受到的值赋值给工作内存的变量副本中，每当虚拟机遇到一个给变量赋值的字节码指令时将会执行这个操作。
- store(存储)，作用于工作内存的变量，它把一个从工作内存中一个变量的值传送到主内存中，以便后续的write使用。
- write(写入)：作用于主内存中的变量，它把store操作从工作内存中得到的变量的值放入主内存的变量中。
- unlock(解锁)：作用于主内存的变量，它把一个处于锁定状态的变量释放出来，释放后的变量才可以被其他线程锁定。

### 交互规则：
- read后必须load，store后必须write  
- assgin后必须要同步到主存，没有assign不允许同步到主存  
- unlock前必须将变量同步回主存  
- lock、unlock必须成对出现

### volatile关键字
- 保证线程间变量可见性
- 禁止指令重排

> 指令重排可能发生在3个阶段：编译器优化重排，CPU指令并行时乱序（指令流水线），内存系统重排序  


## JVM内存分配
- 方法区
- 堆
- 栈帧
- 本地方法栈
- 程序计数器 - PC
> 一个**线程**对应一个**方法栈**、**程序计数器**  
> 一个**方法**对应一个**栈帧**  
> 大家共用一个**堆**

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


## 类加载机制
> 参考：[Java类加载器 — classloader 的原理及应用](https://blog.csdn.net/Taobaojishu/article/details/113874686)  
>
> [通俗易懂 启动类加载器、扩展类加载器、应用类加载器](https://zhuanlan.zhihu.com/p/73359363)
>
> [Java 类隔离加载的正确姿势](https://zhuanlan.zhihu.com/p/141527120)

字节码从数据流变成可执行的字节码需要经历
- 加载 

  > 将磁盘、网络或其他字节码中的字节码，通过类加载器加载到内存

- 验证 

  > 文件格式、访问限制的验证

- 准备 

  > 分配内存、赋予初值（并非开发者在代码中设置的初值，而是将int赋为1，对象引用赋为null）

- 解析 

  > 符号引用(方法名)解析为直接引用(内存地址)

- 初始化

  > 1. 对static修饰的变量、代码块(按代码文件中自上而下的顺序)进行初始化。
  > 2. 如果父类未初始化，初始化其父类

### 双亲委派机制
类加载器与其parent是组合关系（非继承），所以可以说加载器间是父子关系，但不能是父类子类关系，每次加载类时会先让父加载器试着加载

自定义类加载器可以破坏这种机制，因为双亲委派机制是使用ClassLoader的模板方法**loadClass()**实现的

```java
protected Class<?> loadClass(String name, boolean resolve)
        throws ClassNotFoundException
    {
        synchronized (getClassLoadingLock(name)) {
            // First, check if the class has already been loaded
            Class<?> c = findLoadedClass(name);
            // 省略部分代码...
            if (parent != null) {
                c = parent.loadClass(name, false);
            } else {
                c = findBootstrapClassOrNull(name);
            }
            // 省略无关代码...
            if (resolve) {
                resolveClass(c);
            }
            return c;
        }
    }
```

而你完全可以重写此方法，从而破坏双亲委派机制。

> 另外，如果你自定义了类加载器，而不指定它的**parent**，默认的parent是**AppClassLoader**

### 类加载器

除了**BootstrapClassLoader**，其他类加载器最终都继承自**ClassLoader**  

ExtClassLoader、AppClassLoader都直接继承自**URLClassLoader**  

- BootstrapClassLoader  
使用C/C++实现，负责加载jre/lib/rt.jar下的class

- ExtClassLoader  
从源码 **System.getProperty("java.ext.dirs");** 可以看出加载ext目录下的class

- AppClassLoader  
从 **System.getProperty("java.class.path");** 可以看出加载classpath下的class

### 自定义类加载器
1. 准备class文件，我是从一个.java文件编译过来的，java源码如下
```java
public class Hello {

    public static void staticHi() {
        System.out.println("static hi~");
    }
    
    public void sayHi() {
        System.out.println("say hi~");
    }
    
}
```

2. 自定义类加载器
```java
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class Main {

    public static void main(String[] args) throws IOException, ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InstantiationException, InvocationTargetException {
        // class文件位置，因为我打算从文件加载字节码
        String fileName = "Hello.class";
        // 自定义加载器
        MyClassLoader classLoader = new MyClassLoader();
        classLoader.setFileName(fileName);
        // 拿到class
        Class<?> helloClazz = classLoader.loadClass("Hello");
        // 创建实例
        Object o = helloClazz.newInstance();
        // 通过反射拿到方法调用
        Method sayHiMethod = helloClazz.getMethod("sayHi");
        System.out.println("\n执行sayHi方法：");
        sayHiMethod.invoke(o);
        System.out.println("\n执行staticHi方法：");
        Method staticHiMethod = helloClazz.getMethod("staticHi");
        staticHiMethod.invoke(null);
    }

    static class MyClassLoader extends ClassLoader {

        @Override
        protected Class<?> findClass(String name) throws ClassNotFoundException {
            byte[] bytes;
            try(FileInputStream fis = new FileInputStream(fileName);) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();

                byte[] buffer = new byte[1024];
                int len;
                while ((len = fis.read(buffer)) != -1) {
                    baos.write(buffer, 0, len);
                }
                // 为了类安全，byte[]是可以加密解密的，也是自定义加载器的主要用途之一
                bytes = baos.toByteArray();
                System.out.println("字节码：");
                System.out.println(new String(bytes));
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException(e.getMessage());
            }
            return defineClass(name, bytes, 0, bytes.length);
        }

        @Override
        protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
            return super.loadClass(name, resolve);
        }

        // 根据需求，自行定义字段
        private String fileName;

        public void setFileName(String fileName) {
            this.fileName = fileName;
        }

    }

}
```

自定义类加载器常用于类的加密、类的隔离（解决依赖冲突）。

## 重写static方法
> 今天看Spring AOP原理，发现一个抽象类**AopProxyUtils**，没有任何类继承它，里面全是静态方法，这种方式写工具类也不错。  
我突然有个疑问，static方法能不能重写？
- static方法一般使用类名调用，在**编译时**已经确定调用哪个方法了，所以子类可以覆写static方法（并不是继承重写，可以理解为两个同名方法在不同的类里），但是没有多态层面的意义
- 成员方法的重写是多态的体现
- 多态体现在在代码**运行时**才确定使用哪个类、哪个方法  
[什么叫做编译时已经确定调用哪个方法？](##静态调用、动态调用)

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

**方法f()** 的字节码信息
```class
 0 invokestatic #6 <com/xxx/bean/Box.make>
 3 invokestatic #7 <com/xxx/bean/Car.make>
 6 aload_0
 7 invokevirtual #8 <com/xxx/bean/Car.p>
10 return
```

## 垃圾回收

## 强引用、软引用、弱引用、虚引用

## 对象头信息

> 计算机字长并无定论，不同场景不同含义  

32bit计算机1字为32bit，64bit计算机1字就是64bit  
Java对象头2字（数组对象除外，3字）  

| 长度  | 内容               | 说明                      |
| ----- | ------------------ | ------------------------- |
| 第1字 | Mark Word          | 存储对象HashCode或锁信息  |
| 第2字 | Class Meta Address | 存储对象类型(Class)的指针 |

如果是数组，第3字存储数组长度

## JVM调试
- 堆内存参数：初始值`-Xms`， 最大值`-Xmx`
- -XX:+PrintGC
- -XX:-UseCompressedOops

### 调试工具

> 参考：[[【JVM】jstat命令详解---JVM的统计监测工具](https://www.cnblogs.com/sxdcgaq8080/p/11089841.html)](https://www.cnblogs.com/sxdcgaq8080/p/11089841.html)

- jps # 显示Java进程
- javap  Hello.class # 对class文件反编译 
- jstack pid # 打印pid进程的线程信息
- jmap # 查看JVM内存情况
- jstat # 查看堆内存、加载类情况
  - jstat [-命令选项] [vmid] [间隔时间/毫秒] [查询次数] 
  - jstat -class [vmid] # 类加载统计
  - jstat -gc [vmid] # 垃圾回收统计
  - jstat -gcnew [vmid] # 新生代垃圾回收统计

> jvisualvm可视化，很强大