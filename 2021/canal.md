# 准备工作
1. clone源码，版本回退至1.1.4
2. 创建master数据库服务器，创建slave用户，赋予REPLICATION权限，再赋予SELECT权限

# 从MysqlBinlogDumpPerformanceTest开始

```java
public class MysqlBinlogDumpPerformanceTest {

    // 以下代码我运行时做了部分改动，并不是1.1.4原版的代码，部分注释是我自己加的
    public static void main(String args[]) {
        // 单独使用MysqlEventParser实例，初始化一些配置，如binlog、连接等
        final MysqlEventParser controller = new MysqlEventParser();
        final EntryPosition startPosition = new EntryPosition("mysql-bin.000008", null, 1612151873L);
        controller.setConnectionCharset(Charset.forName("UTF-8"));
        controller.setSlaveId(3344L);
        controller.setDetectingEnable(false);
        controller.setFilterQueryDml(true);
        controller.setMasterInfo(new AuthenticationInfo(new InetSocketAddress("192.168.8.183", 8001), "slave", "123456"));
        controller.setMasterPosition(startPosition);
        controller.setEnableTsdb(false);
        controller.setDestination("example");
        controller.setTsdbSpringXml("classpath:spring/tsdb/mysql-tsdb.xml");
        controller.setParallel(true);
        controller.setParallelBufferSize(256);
        controller.setParallelThreadSize(16);
        controller.setIsGTIDMode(false);

        // 我删除了用于计算TPS的代码
        // 消费数据库binlog的回调
        controller.setEventSink(new AbstractCanalEventSinkTest<List<CanalEntry.Entry>>() {

            public boolean sink(List<CanalEntry.Entry> entrys, InetSocketAddress remoteAddress, String destination) throws CanalSinkException,
                    InterruptedException {
                // 当主库binlog更新时，此方法会被调用，更新结果会被组装为Entry列表传入
                System.out.println(entrys);
                return true;
            }

        });
        controller.setLogPositionManager(new AbstractLogPositionManager() {

            @Override
            public LogPosition getLatestIndexBy(String destination) {
                return null;
            }

            @Override
            public void persistLogPosition(String destination, LogPosition logPosition) throws CanalParseException {
            }
        });

        // 启动
        controller.start();

        try {
            Thread.sleep(100 * 1000 * 1000L);
        } catch (InterruptedException e) {
        }
        controller.stop();
    }

    public static abstract class AbstractCanalEventSinkTest<T> extends AbstractCanalLifeCycle implements CanalEventSink<T> {

        public void interrupt() {
        }
    }
}
```

## 进入controller.start();
*MysqlEventParser*的start();

```java
public void start() throws CanalParseException {
    if (runningInfo == null) { // 第一次链接主库
        runningInfo = masterInfo;
    }
    super.start();
}
```
继续走父类*AbstractMysqlEventParser*的start();

```java
public void start() throws CanalParseException {
    if (enableTsdb) {
        if (tableMetaTSDB == null) {
            synchronized (CanalEventParser.class) {
                try {
                    // 设置当前正在加载的通道，加载spring查找文件时会用到该变量
                    System.setProperty("canal.instance.destination", destination);
                    // 初始化
                    tableMetaTSDB = tableMetaTSDBFactory.build(destination, tsdbSpringXml);
                } finally {
                    System.setProperty("canal.instance.destination", "");
                }
            }
        }
    }
    super.start();
}
```




