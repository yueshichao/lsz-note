## JDK动态代理
> 参考：  
> [cglib与JDK动态代理适用场景](https://blog.csdn.net/neosmith/article/details/51072840)  
> [JDK动态代理](https://www.cnblogs.com/wlwl/p/9468348.html)  
> [Jdk动态代理原理解析](https://www.jianshu.com/p/84ffb8d0a338)  

```java

public class Main {

    public static void main(String[] args) {
        // 真正的业务类Hello
        IHello hello = new Hello();
        // 代理类
        ProxyHandler proxyHandler = new ProxyHandler(hello);
        // 通过这段代码，JDK帮你将InvocationHandler转化为IHello类型
        // TODO 这段代码可的原理以好好研究研究
        IHello proxyHello = (IHello) Proxy.newProxyInstance(hello.getClass().getClassLoader(), hello.getClass().getInterfaces(), proxyHandler);
        // 代理类执行
        proxyHello.sayHello();
    }
}

interface IHello {
    void sayHello();
}
// 被代理的类
class Hello implements IHello {

    @Override
    public void sayHello() {
        System.out.println("Hello");
    }
}
// 代理类必须要实现InvocationHandler接口
class ProxyHandler implements InvocationHandler {

    // 被代理对象
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

## cglib代理
> 参考：  
> [CGLIB详解(最详细)](https://blog.csdn.net/qq_33661044/article/details/79767596)
> [cglib动态生成class文件以及方法区溢出](https://blog.csdn.net/likaiwalkman/article/details/50635114)

```java
public class Main {

    public static void main(String[] args) {
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(TargetClass.class);
        enhancer.setCallback(new MethodInterceptor() {
            @Override
            public Object intercept(Object o, Method method, Object[] args, MethodProxy proxy) throws Throwable {
                System.out.println("before method run...");
                Object res = proxy.invokeSuper(o, args);
                System.out.println("after method run...");
                return res;
            }
        });
        TargetClass proxy = (TargetClass) enhancer.create();
        proxy.sayHello();
    }

    public static class TargetClass {
        public void sayHello() {
            System.out.println("Hello~");
        }
    }

}
```

Spring的面向切面实现，就是用动态代理，不过是用**cglib**框架实现的  
cglib原理是通过修改字节码，创造新的对象实现代理类的，据其他博客所言，此种方式，创建对象速度慢，运行速度快  
而JDK动态代理，创建速度快，运行速度慢  