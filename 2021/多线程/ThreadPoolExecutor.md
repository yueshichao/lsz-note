# 由一个现象引发的思考
参考：https://blog.csdn.net/aitangyong/article/details/38822505  

以下代码运行结果，1、3总是先打印的（一秒后打印），顺序随机，但2总是最后打印的（3秒后打印）  

```java
    public static void main(String[] args) {
        ExecutorService rankTableExecutor = new ThreadPoolExecutor(1, 3,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(1));
        rankTableExecutor.execute(() -> {
            sleep(1000);
            System.out.println("========1");
        });

        rankTableExecutor.execute(() -> {
            sleep(2000);
            System.out.println("========2");
        });

        rankTableExecutor.execute(() -> {
            sleep(1000);
            System.out.println("========3");
        });

        rankTableExecutor.shutdown();
    }

    public static void sleep(long t) {
        try {
            TimeUnit.MILLISECONDS.sleep(t);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

```
原因在于**ThreadPoolExecutor**线程池的执行流程：  
1. 如果 poolSize < corePoolSize, 新建一个线程执行任务
2. 如果 poolSize >= corePoolSize, 进入工作队列等待
3. 如果 poolSize >= corePoolSize && 队列满 && poolSize < maximumPoolSize, 创建线程执行任务
4. 如果 队列满，线程池满，拒绝任务