> https://cloud.tencent.com/developer/article/1754078
> 


主从Reactor模型：

BossGroup维护Selector，只关注连接建立的请求事件，ServerSocketChannel注册到这个Selector上
连接建立后，封装SocketChannel给WorkGroup的Selector维护
WorkGroup监听到自己需要的事件，交给handler处理
