> [面试之 kafka 消息丢失与重复消费](https://www.bilibili.com/video/BV1zX4y1g7my)  
> 


# Kafka消息防丢失

## 首先要理解Kafka的消息流转流程

```
producer -> broker -> consumer
```


## producer -> broker

设置回调，监听消息发送成功与否  

kafkaTemplate方式：
```java
ListenableFuture<SendResult<String, String>> sendFuture = kafkaTemplate.send("demoTopic", msg);
sendFuture.addCallback(new ListenableFutureCallback<SendResult<String, String>>(){
    @Override
    public void onSuccess(SendResult<String, String> result) {
        log.info("producerRecord = {}", result.getProducerRecord());
        log.info("recordMetadata = {}", result.getRecordMetadata());
    }

    @Override
    public void onFailure(Throwable ex) {
        log.error("{}", ex);
    }
});
```

kafkaProducer方式：
```java
ProducerRecord<String, String> record = new ProducerRecord<>("demoTopic", msg);
Producer<String, String> producer = producerFactory.createProducer();
producer.send(record, (metadata, ex) -> {
    log.info("metadata = {}, ex = {}", metadata, ex);
    if (ex == null) {
        log.error("成功");
    } else {
        // 对消息重发或持久化
        log.error("失败");
    }
});
```

## broker内部消息丢失


配置producer.properties：  
0 - 不等待broker通知  
1 - leader消息接收成功  
all - 所有分区都拿到消息  
```request.required.acks = all```

配置```replication.factors >= 3```

配置```min.insync.replicas > 1```

配置```unclean.leader.election.enable = false```


## 消费者手动消费

配置手动提交：
```yml
spring.kafka.consumer.enable-auto-commit: false
```
手动提交：
```java
@KafkaListener(topics = {"demoTopic"})
public void onMessage(ConsumerRecord<?, ?> record, Consumer consumer) {
    log.info("topic = {}, partition = {}, value = {}", record.topic(), record.partition(), record.value());
    consumer.commitAsync();
}
```

手动提交可能影响性能，可以批量手动提交，TODO


> 手动提交可能会造成重复提交，如果业务需要可以配置手动提交
> TODO
> https://cloud.tencent.com/developer/article/1336564



