> 参考：[跟面试官侃半小时MySQL事务，说完原子性、一致性、持久性的实现](https://zhuanlan.zhihu.com/p/129860691)  
> [我以为我对Mysql事务很熟，直到我遇到了阿里面试官](https://zhuanlan.zhihu.com/p/148035779)  
> [关于脏读,不可重复读,幻读.](https://zhuanlan.zhihu.com/p/66016870)
> [Innodb中的事务隔离级别和锁的关系](https://tech.meituan.com/2014/08/20/innodb-lock.html)

# 事务四大特性ACID：

- Atomicity 原子性
- Consistent 一致性
- Isalotion 隔离性
- Durable 持久性

# 实现原理：
InnoDB基于**Redo log**和**Undo log**实现事务  
Redo记录修改后的值，Undo记录修改前的值  
当发生crash后，如果事务A已经提交，通过Redo恢复事务的执行，如果事务B未提交，通过Undo回滚数据  
Redo log

# 事务隔离级别：
读未提交（READ UNCOMMITTED）、读提交 （READ COMMITTED）、可重复读 （REPEATABLE READ）、串行化 （SERIALIZABLE）。

1. 读未提交(READ UNCOMMITTED)

会有**脏读**问题，**读到另一个事务未提交的数据**，如果该事务回滚，那么数据读取也就是错误的

2. 读提交(READ COMMITTED)

会有**不可重复读**的问题，AB两个事务，A读取一条记录后，B更新这条记录，B提交事务后，A提交事务前再查询这条记录，发现这条记录值变化了

> 不可重复读一般出现在**update**操作上

3. 可重复读(REPEATABLE READ)

会有**幻读**问题，事务A第一次读取记录数量10条，事务B插入了2条记录并提交，同样的查询，第二次查到的数量是12条

> 幻读一般出现在**insert**操作上

3. 串行化(SERIALIZABLE)

# MySQL的锁机制

- 读锁/共享锁
- 写锁/排他锁
- 间隙锁
- 表锁

读未提交、读提交的问题，都可以通过行锁解决。  
可重复读的问题可以加表锁解决。  
加锁可能产生死锁，此时可以通过串行化结局。  
> 读取时加读锁（共享锁），更新时加写锁（独占锁）。  

# MVCC