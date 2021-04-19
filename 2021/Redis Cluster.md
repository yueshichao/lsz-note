> 参考：
> [Redis cluster tutorial](https://redis.io/topics/cluster-tutorial)

# 配置方式1

注意事项：  
- Redis Cluster至少需要**六个**实例  
- Redis Cluster需要开放**消息总线端口**  
- 在Docker中配置Redis Cluster需要配置为**Host网络模式**  
- 配置中`pidfile`和`cluster-config-file`字段应在集群中**唯一**  

1. `redis.conf`文件，拷贝6个，分别改其中的port、pidfile和cluster-config-file，保证集群中唯一  
```conf
port 6371
# 为每一个集群节点指定一个pid_file
pidfile /var/run/redis_6371.pid
# 找到Cluster配置的代码段，使得Redis支持集群
cluster-enabled yes
# 每一个集群节点都有一个配置文件，这个文件是不能手动编辑的。确保每一个集群节点的配置文件不通
cluster-config-file nodes-6371.conf
# 集群节点的超时时间，单位：ms，超时后集群会认为该节点失败
cluster-node-timeout 5000
# 最后将appendonly改成yes
appendonly yes
```

2. `docker-compose.yml`文件编写  
> /root/cluster/是我宿主机配置文件的目录  

```yml
version: '2'
services:
  redis1:
    image: redis
    container_name: redis1
    network_mode: "host"
    ports:
      - "6371:6371"
      - "16371:16371" # 此端口为Redis消息总线，集群需要用到
    volumes:
      - /root/cluster/redis1.conf:/etc/redis/redis.conf
    command: redis-server /etc/redis/redis.conf
  redis2:
    image: redis
    container_name: redis2
    network_mode: "host"
    ports:
      - "6372:6372"
      - "16372:16372"
    volumes:
      - /root/cluster/redis2.conf:/etc/redis/redis.conf
    command: redis-server /etc/redis/redis.conf
  redis3:
    image: redis
    container_name: redis3
    network_mode: "host"
    ports:
      - "6373:6373"
      - "16373:16373"
    volumes:
      - /root/cluster/redis3.conf:/etc/redis/redis.conf
    command: redis-server /etc/redis/redis.conf
  redis4:
    image: redis
    container_name: redis4
    network_mode: "host"
    ports:
      - "6374:6374"
      - "16374:16374"
    volumes:
      - /root/cluster/redis4.conf:/etc/redis/redis.conf
    command: redis-server /etc/redis/redis.conf
  redis5:
    image: redis
    container_name: redis5
    network_mode: "host"
    ports:
      - "6375:6375"
      - "16375:16375"
    volumes:
      - /root/cluster/redis5.conf:/etc/redis/redis.conf
    command: redis-server /etc/redis/redis.conf
  redis6:
    image: redis
    container_name: redis6
    network_mode: "host"
    ports:
      - "6376:6376"
      - "16376:16376"
    volumes:
      - /root/cluster/redis6.conf:/etc/redis/redis.conf
    command: redis-server /etc/redis/redis.conf
```

3. `docker-compose up`


4. 我的Docker的IP是：192.168.159.129，所以进入任意Redis实例，执行如下命令，启动集群：
```
redis-cli --cluster create --cluster-replicas 1 192.168.159.129:6371 192.168.159.129:6372 192.168.159.129:6373 192.168.159.129:6374 192.168.159.129:6375 192.168.159.129:6376
```


> VS Code的Remote SSH真的爽！  
> 看啥都不如看官网文档！
> 推荐一个国产的Redis客户端：Another Redis DeskTop Manager，比微软的哪一款界面好看，交互也舒服，关键不用每次开启都提示我更新。


> 回家重新部署Redis Cluster出问题了，容器内不能访问配置文件（Can't Open File什么的）  
> 排查发现容器内没有权限，我把宿主机全都777的权限也不行，进容器一看权限全是问号  
> 今天太晚了，下次再看看，以下几篇博客待参考：  
> https://blog.csdn.net/weixin_41826563/article/details/80549323  
> https://blog.csdn.net/kjh2007abc/article/details/90554099  

# 配置方式2

不用加配置文件，编排服务时，修改启动参数  
`docker-composed.yml`文件
```yml
version: '2'
services:
  redis1:
    image: redis
    container_name: redis1
    network_mode: "host"
    ports:
      - "6371:6371"
      - "16371:16371"
    command: redis-server --port 6371 --pidfile /redis_6371.pid --cluster-enabled yes --cluster-config-file nodes-6371.conf
  redis2:
    image: redis
    container_name: redis2
    network_mode: "host"
    ports:
      - "6372:6372"
      - "16372:16372"
    command: redis-server --port 6372 --pidfile /redis_6372.pid --cluster-enabled yes --cluster-config-file nodes-6372.conf
  redis3:
    image: redis
    container_name: redis3
    network_mode: "host"
    ports:
      - "6373:6373"
      - "16373:16373"
    command: redis-server --port 6373 --pidfile /redis_6373.pid --cluster-enabled yes --cluster-config-file nodes-6373.conf
  redis4:
    image: redis
    container_name: redis4
    network_mode: "host"
    ports:
      - "6374:6374"
      - "16374:16374"
    command: redis-server --port 6374 --pidfile /redis_6374.pid --cluster-enabled yes --cluster-config-file nodes-6374.conf
  redis5:
    image: redis
    container_name: redis5
    network_mode: "host"
    ports:
      - "6375:6375"
      - "16375:16375"
    command: redis-server --port 6375 --pidfile /redis_6375.pid --cluster-enabled yes --cluster-config-file nodes-6375.conf
  redis6:
    image: redis
    container_name: redis6
    network_mode: "host"
    ports:
      - "6376:6376"
      - "16376:16376"
    command: redis-server --port 6376 --pidfile /redis_6376.pid --cluster-enabled yes --cluster-config-file nodes-6376.conf
```

除了不用写6个配置文件，其他步骤与方式1一致  