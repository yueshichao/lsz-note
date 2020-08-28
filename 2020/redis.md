# [redis基础](https://juejin.im/post/6857667542652190728)
## 常用命令

| 命令            | 说明                                     |
| --------------- | ---------------------------------------- |
| ping            | 返回pong表示连接正常                     |
| config get *    | 获取redis配置信息                        |
| select 15       | 切换至15号数据库                         |
| set key value   | key - value                              |
| get key         | 获取key对应的valye                       |
| keys *          | 模糊查询所有key，有3个通配符 *、?、[]    |
| del key         | 删除key                                  |
| exists key      | 是否存在某个key                          |
| expire key 20   | 过期时间20s                              |
| setnx key value | 当key不存在时设置value（常用语分布式锁） |
|                 |                                          |

## 散列 - Hash

| 命令                                  | 说明                  |
| ------------------------------------- | --------------------- |
| hset key field value                  | key - hash表的名      |
| hget key field                        | field - hash表内的key |
| hexists key value                     | 是否存在              |
| hkeys key                             | hash表内所有key       |
| hmset key field0 value0 field1 value1 | 批量设值              |

> 可以把Hash结构视为redis中的小redis，只要在基础redis前面加上h就可以调用，后面跟上key（代表数据库）	

## 列表 - List

| 命令              | 说明                                                 |
| ----------------- | ---------------------------------------------------- |
| lpush key value   | key - 列表名，value - 值，插入List头部               |
| rpush key value   | 插入到List尾部                                       |
| lpop key          | 移除List头部元素                                     |
| rpop key          | 移除List尾部元素                                     |
| blpop key timeout | timeout - 超时时间，移除List头部元素，如果为空则阻塞 |
| brpop key timeout | 移除List尾部元素，如果为空则阻塞                     |

## 集合 - Set

| 命令             | 说明                                      |
| ---------------- | ----------------------------------------- |
| sadd key element | key - Set名，value - 元素值，插入一个元素 |
| sismember key    | Set中是否包含此元素                       |
| spop key         | 弹出一个元素                              |
| srem key element | 从Set中删除一个元素                       |
| scard key        | 元素数量                                  |
| smembers key     | 所有元素值                                |

## 有序集合

| 命令                                     | 说明                                             |
| ---------------------------------------- | ------------------------------------------------ |
| zadd key score1 element1 score2 element2 | 往有序集合中添加元素，key - 集合名，score - 分数 |
| zincrby key 600 element                  | 将元素score加600                                 |
| zrange key 0 -1                          | 返回所有的元素（升序）                           |
| zrangebyscore key 0 100                  | 分数在0到100内的所有元素（升序）                 |
| zrem key value                           | 删除元素                                         |
| zrevrank key value                       | 返回value对应的排名（降序）                      |

> 还有部分命令没写出来，例如set的交集并集操作，有序集合的按分数删除，按排名删除等...





# redis应用

## redis实现消息队列
- list数据结构实现
  1. lpush mylist a b c d # 生产
  2. rpop mylist # 消费

- publish实现
  1. publish channel:1 hi # 向channel:1发布事件
  2， subscribe channel:1 # 订阅channel:1



