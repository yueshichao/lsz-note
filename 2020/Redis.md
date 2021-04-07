# Redis环境快速搭建

> 参考：[Redis Sentinel-深入浅出原理和实战](https://zhuanlan.zhihu.com/p/334983562)

## Docker单机方式启动
- docker run -d --name redis -p 6379:6379 redis

// TODO 主从 slaveof

## Sentinel集群方式启动

准备两个目录`./redis`和`./sentinel`，分别存放`docker-compose.yml`  和配置文件

1. 文件`./redis/docker-compose.yml`

```yml
version: '2'
services:
  master:
    image: redis
    container_name: redis-master
    ports:
      - 6380:6379
  slave1:
    image: redis
    container_name: redis-slave-1
    ports:
      - 6381:6379
    command:  redis-server --slaveof redis-master 6379
  slave2:
    image: redis
    container_name: redis-slave-2
    ports:
      - 6382:6379
    command: redis-server --slaveof redis-master 6379
```

在./redis目录下使用`docker-compose up`启动redis集群

2. 文件`./sentinel/docker-compose.yml`

```yml
version: '2'
services:
  sentinel1:
    image: redis
    container_name: redis-sentinel-1
    ports:
      - 26379:26379
    command: redis-sentinel /usr/local/etc/redis/sentinel.conf
    volumes:
      - ./sentinel.conf:/usr/local/etc/redis/sentinel.conf
  sentinel2:
    image: redis
    container_name: redis-sentinel-2
    ports:
    - 26380:26379
    command: redis-sentinel /usr/local/etc/redis/sentinel.conf
    volumes:
      - ./sentinel.conf:/usr/local/etc/redis/sentinel.conf
  sentinel3:
    image: redis
    container_name: redis-sentinel-3
    ports:
      - 26381:26379
    command: redis-sentinel /usr/local/etc/redis/sentinel.conf
    volumes:
      - ./sentinel.conf:/usr/local/etc/redis/sentinel.conf
networks:
  default:
    external:
      name: redis_default
```

3. 还需准备sentinel.conf

`./sentinel/sentinel.conf`

```conf
port 26379
dir "/tmp"
sentinel deny-scripts-reconfig yes
sentinel monitor mymaster 172.19.0.2 6379 2
sentinel config-epoch mymaster 1
sentinel leader-epoch mymaster 1
```

>  其中第四行配置项`sentinel monitor mymaster 172.19.0.2 6379 2`，是我自己master的容器ip(172.19.0.2)，6379是容器内开放的端口号，需要替换成自己本地的，使用`docker inspect redis-master | grep 'IPAddress'`查看

在./sentinel目录下`docker-compose up`启动sentinel集群

> 可以手动模拟master-redis挂掉，在`./redis`目录下`docker-compose pause master`暂停master-redis，也可以取消暂停`docker-compose unpause master`

// TODO cluster


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
| setnx key value | 当key不存在时设置value（常用于分布式锁） |
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
  2. subscribe channel:1 # 订阅channel:1



