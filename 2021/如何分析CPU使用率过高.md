> 参考：[记录服务器CPU占用率飙升原因排查过程](https://zhuanlan.zhihu.com/p/315170484)
> [线上服务器出现CPU飙升问题该怎么办？](https://zhuanlan.zhihu.com/p/76271596)


> 排查Java程序

# 直接排查

1. `top`命令找到CPU使用率最高的进程

> top之后按`x`，表示按CPU使用率排序  
> 找到CPU使用率异常的进程ID，我的是27

2. `top -p 27`

> 只查看指定进程信息  
> `shift + h` 查看线程情况  
> 找到CPU异常的线程ID，我的是219  

3. `jstack 27 > dump.info`

> 把此时的的进程信息dump下来  
> 在根据219找到线程信息，219十六进制为db  
> 找到nid为0xdb的线程

4. 根据调用栈分析代码逻辑