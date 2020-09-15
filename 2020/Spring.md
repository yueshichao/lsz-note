# 带token的FeignClient（带拦截器的FeignClient）
> 这的代码顺序是倒着来的，但是对于人的认知是合理的，从已知（FeignClient）到未知（Intercepter）  
实际写代码时，先后顺序不重要，本质就是给FeignClient加一个configuration

1. 写好FeignClient
```java
@FeignClient(url="XXX", configuration = FeignConfig.class)
public interface RbacFeignClient {
    // 接口配置...
}
```

2. 编写FeignConfig
```java
// 这里我曾经加过@Component，但是出过BUG，其实没必要加
public class FeignConfig {
    @Bean
    public FeignRequestInterceptor feignRequestInterceptor() {
        return new FeignRequestInterceptor();
    }
}
```
> 当时BUG的现象是**栈溢出**，直接原因是方法的递归调用，细查之后发现  
调用某FeignX时需要过**拦截器A**（全局的），过拦截器A后要过**拦截器B**（也是全局的）  
然后拦截器B又去请求这个FeignX，形成一个递归调用，最后爆栈

3. 编写RequestInterceptor
```java
public class FeignRequestInterceptor implements RequestInterceptor {

    private String accessToken;

    // 失效期限/秒
    private Integer expireIn = 86400;

    // 获取时间/秒级时间戳
    private Long preTimestamp = 0L;

    @Override
    public void apply(RequestTemplate template) {
        long currentTimestamp = System.currentTimeMillis() / 1000;
        if (currentTimestamp - preTimestamp > expireIn || accessToken == null) {
            preTimestamp = currentTimestamp;
            accessToken = applyAccessToken();
            expireIn = accessToken.getExpiresIn();
        }
        template.header("token", accessToken);
    }

    private String applyAccessToken(){
        // TODO 拿到access_token
        return null;
    }
}
```


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

# Spring实现AOP的两种方式
## 1. 使用jdk动态代理
## 2. 使用cglib修改字节码
