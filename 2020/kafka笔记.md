# 安装

- wget http://mirror.bit.edu.cn/apache/kafka/1.0.0/kafka_2.11-1.0.0.tgz # 下载

- tar -zvxf kafka_2.11-1.0.0.tgz # 解压

- vi config/server.properties # 修改配置文件
```text
broker.id=1
log.dirs=/log/kafka-logs
```
> 若使用客户端连接出错，需手动配置advertised.host.name和advertised.port

# 启动
- bin/zookeeper-server-start.sh -daemon config/zookeeper.properties # 启动zookeeper

- bin/kafka-server-start.sh config/server.properties # 启动kafka

- bin/kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic test # 创建topic，名称test

# 创建topic

- bin/kafka-topics.sh --list --zookeeper localhost:2181 # 查看topic

- bin/kafka-console-producer.sh --broker-list localhost:9092 --topic test # 产生消息

- bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic test --from-beginning # 消费消息



