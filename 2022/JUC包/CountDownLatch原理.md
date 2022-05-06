
# 主要原理

## new CountDownLatch(2)

构造方法传入参数为2时，表示等待两个门闩释放，才能运行await()之后的代码，
内部维护了state记录这个参数，
**state**是核心参数！

## countDown()

每次执行此方法，都会使state减1，
顺便检查，如果state为0了，就唤醒AQS的等待线程

## await()

使用该方法的线程一般是等待线程，如果state为0了，就继续执行，如果还不为0，就阻塞该线程，并进入AQS



