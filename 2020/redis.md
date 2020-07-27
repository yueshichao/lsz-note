# redis应用
## redis实现消息队列
- list数据结构实现
  1. lpush mylist a b c d # 生产
  2. rpop mylist # 消费

- publish实现
  1. publish channel:1 hi # 向channel:1发布事件
  2， subscribe channel:1 # 订阅channel:1



