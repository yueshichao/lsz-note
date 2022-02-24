
# Dubbo泛化调用
> 参考：  
> [Dubbo的泛化调用](https://dubbo.apache.org/zh/blog/2018/08/14/dubbo%E7%9A%84%E6%B3%9B%E5%8C%96%E8%B0%83%E7%94%A8/)  
> [使用泛化调用](https://dubbo.apache.org/zh/docs/advanced/generic-reference/)  


```java
// 引用远程服务
// 该实例很重量，里面封装了所有与注册中心及服务提供方连接，请缓存
ReferenceConfig<GenericService> reference = new ReferenceConfig<>();
reference.setRegistries(registryConfigs);
// 弱类型接口名
reference.setInterface("xxx");
reference.setVersion("1.0.0");
// 声明为泛化接口
reference.setGeneric(true);
// 用org.apache.dubbo.rpc.service.GenericService可以替代所有接口引用
GenericService genericService = reference.get();
// 基本类型以及Date,List,Map等不需要转换，直接调用
Map<Object, Object> param = MapUtil.builder()
        .put("param1", "1")
        .put("param2", "2")
        .build();
Object result = genericService.$invoke("funcName",
        new String[] {"full request class name"},
        new Object[] {param});
log.info("result = {}", result);
```


# Dubbo不提供服务，只消费

诉求：本地环境，只消费公司test环境服务，不对外提供服务（防止本地打断点影响test环境）  

> 参考：[dubbo不提供服务只消费服务](https://blog.csdn.net/a490789580/article/details/107908655)  


```yaml
dubbo.registry.register: false # 不参与注册
```

```yaml
dubbo.provider.protocol: injvm # 只消费不注册为provider
```


# Dubbo消费超时

## Consumer fullGC引起
> 参考：[Full GC引发的dubbo consumer端超时问题](https://blog.csdn.net/sss2951/article/details/117201396)  


# Dubbo原理

## 调用分析
GenericFilter
