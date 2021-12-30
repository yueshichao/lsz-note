> 参考：  
> 《MySQL技术内幕  InnoDB存储引擎》  
> [跟面试官侃半小时MySQL事务，说完原子性、一致性、持久性的实现](https://zhuanlan.zhihu.com/p/129860691)  
> [我以为我对Mysql事务很熟，直到我遇到了阿里面试官](https://zhuanlan.zhihu.com/p/148035779)  
> [关于脏读,不可重复读,幻读.](https://zhuanlan.zhihu.com/p/66016870)  
> [Innodb中的事务隔离级别和锁的关系](https://tech.meituan.com/2014/08/20/innodb-lock.html)  
> [快照读、当前读和MVCC](https://www.cnblogs.com/AlmostWasteTime/p/11466520.html)   


# 事务四大特性ACID：

- Atomicity 原子性
- Consistent 一致性
- Isalotion 隔离性
- Durable 持久性

# 实现原理：
InnoDB基于**Redo log**和**Undo log**实现事务  
Redo记录修改后的值，Undo记录修改前的值  
**当发生crash后，如果在binlog中事务A已经提交，通过Redo恢复事务的执行，如果事务B未提交，通过Undo回滚数据**    

redo恢复提交事务修改后的页操作，undo回滚行记录到某个特定版本  
redo记录数据页（物理存储结构）的修改，undo记录数据行（逻辑结构SQL语句）的修改，undo也用于实现MVCC  



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

## Innodb实现RR
读提交简称RC，可重复读简称RR  

MySQL在**RR级别**的事务中解决**幻读**问题使用了两种方式：
1. 多版本并发控制 MVCC
2. 邻键锁（next-key lock） = 行锁 + 间隙锁（gap lock）

MVCC是为了实现**快照读**

> 读取的是历史版本的数据

间隙锁是为了实现**当前读**

> 修改数据时就需要当前读，因为在历史版本上修改数据也没有意义，此时就会加上间隙锁，所谓间隙锁，可以理解为粒度更小的表锁，锁住的是在本次事务中可能涉及到当前读的行范围。

# MySQL的锁机制

- 读锁/共享锁、写锁/排他锁
- 间隙锁
- 表锁、行锁
- 意向锁


# MySQL事务语句
> 参考：  
> [MySQL查看和修改事务隔离级别](http://c.biancheng.net/view/7266.html)  
>   


```sql
-- 查看事务隔离级别
show variables like '%tx_isolation%';

-- 查看自动提交
SHOW VARIABLES LIKE '%AUTOCOMMIT%';

-- 取消自动提交
set @@autocommit = 0;

-- 开启事务
begin;

-- mysql8变量名换成了transaction_isolation
-- 查看全局事务隔离级别
SELECT @@global.tx_isolation;
-- 设置当前事务隔离级别
SET SESSION TRANSACTION ISOLATION LEVEL READ COMMITTED;
-- 查看当前事务隔离级别
SELECT @@session.tx_isolation;


-- sql
insert into test_table(`id`) values (1);

rollback;

-- set @@autocommit = 1;
```