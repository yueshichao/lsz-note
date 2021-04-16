// TODO

```yml
version: '2'
services:
  redis1:
    image: redis
    container_name: redis1
    ports:
      - "6371:6371"
      - "16371:16371"
    volumes:
      - /root/cluster/redis1.conf:/etc/redis/redis.conf
    command: redis-server /etc/redis/redis.conf
  redis2:
    image: redis
    container_name: redis2
    ports:
      - "6372:6372"
      - "16372:16372"
    volumes:
      - /root/cluster/redis2.conf:/etc/redis/redis.conf
    command: redis-server /etc/redis/redis.conf
  redis3:
    image: redis
    container_name: redis3
    ports:
      - "6373:6373"
      - "16373:16373"
    volumes:
      - /root/cluster/redis3.conf:/etc/redis/redis.conf
    command: redis-server /etc/redis/redis.conf
  redis4:
    image: redis
    container_name: redis4
    ports:
      - "6374:6374"
      - "16374:16374"
    volumes:
      - /root/cluster/redis4.conf:/etc/redis/redis.conf
    command: redis-server /etc/redis/redis.conf
  redis5:
    image: redis
    container_name: redis5
    ports:
      - "6375:6375"
      - "16375:16375"
    volumes:
      - /root/cluster/redis5.conf:/etc/redis/redis.conf
    command: redis-server /etc/redis/redis.conf
  redis6:
    image: redis
    container_name: redis6
    ports:
      - "6376:6376"
      - "16376:16376"
    volumes:
      - /root/cluster/redis6.conf:/etc/redis/redis.conf
    command: redis-server /etc/redis/redis.conf
```


```conf
port 6371
#为每一个集群节点指定一个pid_file
pidfile /var/run/redis_6371.pid
#找到Cluster配置的代码段，使得Redis支持集群
cluster-enabled yes
#每一个集群节点都有一个配置文件，这个文件是不能手动编辑的。确保每一个集群节点的配置文件不通
cluster-config-file nodes-6371.conf
#集群节点的超时时间，单位：ms，超时后集群会认为该节点失败
cluster-node-timeout 5000
#最后将appendonly改成yes
appendonly yes
```