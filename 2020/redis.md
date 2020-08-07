# [redis基础](https://juejin.im/post/6857667542652190728)
## 常用命令

| 命令            | 说明                                     |
| --------------- | ---------------------------------------- |
| ping            | 返回pong表示连接正常                     |
| set key value   | key - value                              |
| get key         | 获取key对应的valye                       |
| keys *          | 模糊查询所有key，有3个通配符 *、?、[]    |
| del key         | 删除key                                  |
| exists key      | 是否存在某个key                          |
| expire key 20   | 过期时间20s                              |
| setnx key value | 当key不存在时设置value（常用语分布式锁） |
|                 |                                          |

## redis内部数据结构 - Hash

| 命令                                  | 说明                  |
| ------------------------------------- | --------------------- |
| hset key field value                  | key - hash表的名      |
| hget key field                        | field - hash表内的key |
| hexists key value                     | 是否存在              |
| hkeys key                             | hash表内所有key       |
| hmset key field0 value0 field1 value1 | 批量设值              |
|                                       |                       |
|                                       |                       |
|                                       |                       |
|                                       |                       |



# redis应用

## redis实现消息队列
- list数据结构实现
  1. lpush mylist a b c d # 生产
  2. rpop mylist # 消费

- publish实现
  1. publish channel:1 hi # 向channel:1发布事件
  2， subscribe channel:1 # 订阅channel:1



