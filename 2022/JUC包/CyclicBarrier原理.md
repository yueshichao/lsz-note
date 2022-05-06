
# 主要原理

# CyclicBarrier barrier = new CyclicBarrier(2)

构造方法传入参数表示需要线程2个，
记录为**count**，该参数贯穿主要流程

# barrier.await()

每次执行该方法，会先使--count，
并且判断如果count为0了，就**唤醒全部**线程，
如果count不为0，就**阻塞自己**，等待被唤醒


## 通过```Condition trip = lock.newCondition()```唤醒

阻塞自己：```trip.await();```
唤醒全部：```trip.signalAll()```






