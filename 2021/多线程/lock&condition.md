
Condition在Lock内部的使用
```java
// Lock & Condition

Lock lock = new ReentrantLock();
// Condition与Lock结合使用，在Lock内部控制执行顺序
Condition condition = lock.newCondition();

new Thread(() -> {
    try {
        TimeUnit.SECONDS.sleep(1);
    } catch (InterruptedException e) {
        e.printStackTrace();
    }
    System.out.println("线程A休眠1s结束");
    lock.lock();
    try {
        System.out.println("线程A结束...");
        // 释放信号量
        condition.signal();
    } finally {
        lock.unlock();
    }
}, "A").start();

new Thread(() -> {
    lock.lock();
    System.out.println("进入线程B");
    try {
        // 等待信号量
        condition.await();
    } catch (InterruptedException e) {
        e.printStackTrace();
    }
    try {
        System.out.println("线程B结束...");
    } finally {
        lock.unlock();
    }
}, "B").start();

```