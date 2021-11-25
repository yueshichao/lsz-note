> 参考：[阿里技术专家详解 DDD 系列 第一讲- Domain Primitive](https://zhuanlan.zhihu.com/p/340911587)
> [团队开发框架实战—CQRS架构](https://www.jianshu.com/p/d4ca2133875c)
> [阿里技术专家详解DDD系列 第二讲 - 应用架构](https://zhuanlan.zhihu.com/p/343388831)
> [阿里技术专家详解DDD系列 第三讲 - Repository模式](https://zhuanlan.zhihu.com/p/348706530)

传统开发，业务简单时，**对象仅承担数据功能**，如POJO，俗称贫血模型  
DDD模型中，强调Value Object，而 **Domain Primitive** 是Value Object的进阶版  

# Domain Primitive
- 让隐性的概念显性化
- 让隐性的上下文显性化
- 封装多对象行为

DP具有更丰富的行为，而不是数据集合，包含了如数据验证的行为，代表着业务的抽象

