# Redisson分布式锁

1. 配置Bean

```java
@Bean
public Redisson getRedisson() {
    Config config = new Config();
    config.useSingleServer().setAddress("192.168.8.220:6379");
    Redisson redisson = (Redisson) Redisson.create(config);
    return redisson;
}
```

2. 使用方式

```java
lock.lock(3000L, TimeUnit.MILLISECONDS);
RLock lock = redisson.getLock(lockKey);
try {
    // TODO sth.
} catch (InterruptedException e) {
   e.printStackTrace();
} finally {
    lock.unlock();
}
```