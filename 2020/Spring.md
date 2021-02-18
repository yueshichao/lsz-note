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

# [@value注入static变量](https://www.cnblogs.com/xiang--liu/p/11445318.html)
像成员变量那样注入会为null
可以用set方法注入

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


# Swagger-ui
问题：
今天新建项目之后，随手写了个Controller，但是无论如何都访问不了Swagger-ui.html
页面直接报错
```log
Whitelabel Error Page
This application has no explicit mapping for /error, so you are seeing this as a fallback.
Thu Oct 15 16:39:47 CST 2020
There was an unexpected error (type=Not Acceptable, status=406).
Could not find acceptable representation
```
日志报错
```log
org.springframework.web.HttpMediaTypeNotAcceptableException: Could not find acceptable representation
```
百度搜了一些尝试不行，因为是新建项目，以为是pom出问题了，改了很多还是不行
最后终于查到，自己随手写的Controller没指定Mapping
```java
@RestController
@Slf4j
public class MyController {

    @GetMapping
    public ResponseMessage hi() {
        return ResponseMessage.ok(DateUtil.now());
    }

}
```
导致/Swagger-ui.html被mapping到了这个Controller，网页要的html，返回的json，就报错了


# @Transactional
