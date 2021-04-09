> 参考：
> [如果这篇文章说不清epoll的本质，那就过来掐死我吧！（1）](https://zhuanlan.zhihu.com/p/63179839)
> [linux多路IO--epoll(一)--水平触发和边沿触发](https://zhuanlan.zhihu.com/p/107995399)
> [java nio的select和linux的epoll有什么区别？](https://www.zhihu.com/question/343373314/answer/807838141)
> [Java NIO浅析](https://tech.meituan.com/2016/11/04/nio.html)

# NIO
Netty本身是对NIO的封装，所以先从NIO讲起  
1. 原本的网络IO模型是这样的：一个请求，一个线程，返回数据  
2. 可是请求量大的情况下，创建的线程太多，所以可以稍微优化下：一个请求，线程池里捞一个线程出来response  
3. 但是当请求更多时，一个线程的处理时间大部分耗在网络IO等待接收数据上，这样线程资源还是未充分利用  

NIO就是这样，当数据已经准备好了，才交给Handler线程去处理  
而准备数据的阶段交给同一个线程去监听，这也叫做复用  


# EPOLL
这种把多个连接交给一个线程去监听的方式叫做：IO多路复用  

> Linux 2.6之前是select、poll，2.6之后是epoll，Windows是IOCP  

一般情况，我们只讨论epoll
