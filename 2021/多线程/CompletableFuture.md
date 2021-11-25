> 参考:  
> [CompletableFuture使用场景和原理](https://segmentfault.com/a/1190000022882357)  
> [CompletableFuture：让你的代码免受阻塞之苦](https://juejin.cn/post/6844904024332828685#heading-8)  
> [CompletableFuture 的 20 个例子](https://juejin.cn/post/6844903590792790030)  
> [CompletableFuture return all finished result after timeout](https://stackoverflow.com/questions/56503017/completablefuture-return-all-finished-result-after-timeout)  


# 数据从多个RPC来，如何使用CompletableFuture并行获取

- runAsync方式，在lambada内部赋值  

```java
@Data
public static class Duck {
    private Long id;
    private String name;
    private Integer age;
}

@Test
public void queryDuckTest() {
    // 数据从多个RPC来，如何使用CompletableFuture并行获取
    ThreadPoolExecutor executor = new ThreadPoolExecutor(3, 3,
            0L, TimeUnit.MILLISECONDS,
            new ArrayBlockingQueue<>(5));
    // 查询条件
    Long requestId = 10L;
    log.info("requestId = {}", requestId);
    long start = System.currentTimeMillis();
    // 返回值放入此对象
    final Duck duck = new Duck();
    duck.setId(requestId);
    // 多个RPC查询
    List<CompletableFuture<?>> completableFutureList = Lists.newArrayList();
    CompletableFuture<Void> completableFuture = CompletableFuture.runAsync(() -> {
        ThreadUtil.sleep(2000L);
        // rpc result
        duck.setName("name");
    }, executor);
    completableFutureList.add(completableFuture);
    CompletableFuture<Void> completableFuture1 = CompletableFuture.runAsync(() -> {
        ThreadUtil.sleep(2000L);
        // rpc result
        duck.setAge(1);
    }, executor);
    completableFutureList.add(completableFuture1);
    // allOf 一起执行
    CompletableFuture[] cfs = completableFutureList.toArray(new CompletableFuture[0]);
    CompletableFuture.allOf(cfs).join();
    long end = System.currentTimeMillis();
    log.info("duration = {}ms, info = {}", end - start, duck);
}
```

- supplyAsync方式，异步拿到返回值

```java
@Data
public static class Duck {
    private Long id;
    private String name;
    private Integer age;
}

@Test
public void queryDuckTest() {
    // 数据从多个RPC来，如何使用CompletableFuture并行获取
    ThreadPoolExecutor executor = new ThreadPoolExecutor(3, 3,
            0L, TimeUnit.MILLISECONDS,
            new ArrayBlockingQueue<>(5));
    // 查询条件
    Long requestId = 10L;
    log.info("requestId = {}", requestId);
    long start = System.currentTimeMillis();
    // 返回值放入此对象
    final Duck duck = new Duck();
    duck.setId(requestId);
    // 多个RPC查询
    CompletableFuture<String> completableFuture = CompletableFuture.supplyAsync(() -> {
        ThreadUtil.sleep(2000L);
        // rpc result
        return "name";
    }, executor);
    CompletableFuture<Integer> completableFuture1 = CompletableFuture.supplyAsync(() -> {
        ThreadUtil.sleep(2000L);
        // rpc result
        return 1;
    }, executor);
    // 组装feature准备allOf
    List<CompletableFuture<?>> futures = Stream.of(completableFuture, completableFuture1)
            .collect(Collectors.toList());
    // 在这一步完成所有feature
    CompletableFuture<Void> allDoneFuture = CompletableFuture.allOf(
            futures.toArray(new CompletableFuture[]{})
    );
    // 以下仅是组装过程
    // 由于allOf中已经完成，所以此时feature.join()会直接拿到返回值
    CompletableFuture<List<Object>> listCompletableFuture = allDoneFuture.thenApply(v -> futures.stream().map(CompletableFuture::join).collect(Collectors.toList()));
    List<Object> join = listCompletableFuture.join();
    duck.setName(join.get(0).toString());
    duck.setAge((Integer) join.get(1));
    long end = System.currentTimeMillis();
    log.info("duration = {}ms, info = {}", end - start, duck);
}
```