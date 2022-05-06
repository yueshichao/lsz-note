# 一致性哈希
> 参考：https://www.bilibili.com/video/BV1Hs411j73w
> https://blog.csdn.net/monokai/article/details/106626945


如果要设计一个分布式缓存或负载均衡的算法，我们可以使用Hash来实现  
如存在3台缓存服务器实例，我们将请求对3取模（这就是我们的Hash函数）  
但是如果增加了2台实例，我们就需要修改Hash函数，改为对5取模  

这样做，也会导致大部分缓存失效，如3号请求，原本走0号服务器，现在去3号服务器了

而一致性Hash通过**环状的Hash空间**代替线性的Hash空间解决了这个问题

然而还会有一些问题，如数据倾斜和缓存雪崩  
比如，环上实例分布非常不均匀时，会导致大量数据请求同一台服务器，这就叫数据倾斜  
另外，当某台实例挂掉了，接下来所有请求都会访问它的下一台实例，导致缓存雪崩  

解决这两个问题，可以**引入虚拟结点**  

没有引入虚拟结点的实现：
```java
@Test
public void test() {
    // 搞几台实例
    List<String> nodes = Stream.of("结点1", "结点2", "结点3").collect(Collectors.toList());
    MyConsistentHash consistHash = new MyConsistentHash(nodes);
    // 100个请求
    for (int i = 0; i < 100; i++) {
        String nodeName = consistHash.get(i);
        System.out.printf("nodeName = %s\n", nodeName);
    }
    // 删除1个结点
    consistHash.remove("结点1");
    System.out.println("删除结点1");
    // 100个请求
    for (int i = 0; i < 100; i++) {
        String nodeName = consistHash.get(i);
        System.out.printf("nodeName = %s\n", nodeName);
    }
}

public static class MyConsistentHash {

    private List<String> nodes;
    // index -> node
    private NavigableMap<Integer, String> circle = new TreeMap<>();

    public MyConsistentHash(List<String> nodes) {
        this.nodes = nodes;
        // node放到环上
        for (String node : nodes) {
            circle.put(calcNodeIndex(node), node);
        }
    }

    public String get(Integer request) {
        // 根据请求获取环上index
        int index = calcIndex(request);
        // 根据index获取处理结点
        return getHandleNode(index);
    }

    public void remove(String node) {
        nodes.remove(node);
        circle.remove(calcNodeIndex(node));
    }

    private String getHandleNode(int index) {
        NavigableMap<Integer, String> tailMap = circle.tailMap(index, true);
        if (tailMap.isEmpty()) {
            return circle.firstEntry().getValue();
        } else {
            return tailMap.firstEntry().getValue();
        }
    }

    private int calcIndex(Integer request) {
        return Objects.hashCode(request) % nodes.size();
    }

    private int calcNodeIndex(String node) {
        return Objects.hashCode(node) % nodes.size();
    }

}
```

引入了**虚拟结点**，**circle**为虚拟结点映射到真实结点，当真实结点下线后，原先负载均匀的分给其他结点  
```java
@Test
public void test() {
    // 搞几台实例
    List<String> nodes = Stream.of("结点1", "结点2", "结点3").collect(Collectors.toList());
    MyConsistentHash consistHash = new MyConsistentHash(nodes);
    // 100个请求
    for (int i = 0; i < 100; i++) {
        String nodeName = consistHash.get(i);
        System.out.printf("nodeName = %s\n", nodeName);
    }
    // 删除1个结点
    consistHash.remove("结点1");
    System.out.println("删除结点1");
    // 100个请求
    for (int i = 0; i < 100; i++) {
        String nodeName = consistHash.get(i);
        System.out.printf("nodeName = %s\n", nodeName);
    }
}

public static class MyConsistentHash {

    private List<String> nodes;
    // 虚拟结点数量
    public static final Integer VIRTUAL_NODE_COUNT = 1000;
    // 引入虚拟结点层，vitrual node -> node
    private NavigableMap<Integer, String> circle = new TreeMap<>();


    public MyConsistentHash(List<String> nodes) {
        this.nodes = nodes;
        String[] nodeArr = nodes.toArray(new String[0]);
        // 建立虚拟结点到真实结点的映射
        for (int i = 0; i < VIRTUAL_NODE_COUNT; i++) {
            circle.put(i, nodeArr[i % nodeArr.length]);
        }
    }

    public String get(Integer request) {
        // 根据请求获取环上index
        int index = calcIndex(request);
        // 根据index获取处理结点
        return getHandleNode(index);
    }
    
    public void remove(String node) {
        nodes.remove(node);
        String[] nodeArr = nodes.toArray(new String[0]);
        int replaceIndex = 0;
        // 将删除的node的虚拟结点映射到现存的结点中
        for (Map.Entry<Integer, String> entry : circle.entrySet()) {
            if (Objects.equals(node, entry.getValue())) {
                entry.setValue(nodeArr[replaceIndex++ % nodeArr.length]);
            }
        }
        circle.remove(calcNodeIndex(node));
    }

    private String getHandleNode(int index) {
        NavigableMap<Integer, String> tailMap = circle.tailMap(index, true);
        if (tailMap.isEmpty()) {
            return circle.firstEntry().getValue();
        } else {
            return tailMap.firstEntry().getValue();
        }
    }

    private int calcIndex(Integer request) {
        return Objects.hashCode(request) % nodes.size();
    }

    private int calcNodeIndex(String node) {
        return Objects.hashCode(node) % nodes.size();
    }

}
```