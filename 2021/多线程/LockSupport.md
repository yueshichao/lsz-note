LockSupport基本使用
```java
Thread mainThread = Thread.currentThread();
new Thread(() -> {
    try {
        TimeUnit.SECONDS.sleep(3);
    } catch (InterruptedException e) {
        e.printStackTrace();
    }
    System.out.println("子线程解除主线程的阻塞");
    LockSupport.unpark(mainThread);
}, "子线程").start();

System.out.println("主线程阻塞...");
LockSupport.park();
System.out.println("主线程结束...");
```