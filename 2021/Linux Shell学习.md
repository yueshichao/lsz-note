> 参考：
> [菜鸟教程](https://www.runoob.com/linux/linux-shell.html)  
> 


# 变量：

- 定义
fruit="apple"

- 使用
echo $fruit

- 字符串拼接
action="eat ${fruit} and banana"
action="eat "${fruit}" and banana"
action='eat '${fruit}' and banana'
action='eat ${fruit} and banana' # 此种方式不可行





实际应用：
```bash
#!/bin/bash

############## dump ##############
# 根据时间构造文件名
fileName=`date +%Y%m%d%H%M%S`'_dump.sql'
echo $fileName
# dump
docker exec -i mall-mysql /bin/bash -c 'mysqldump -uroot -p123456 lsz_mall > /tmp/sql/'$fileName

############## refresh ##############
docker exec -i mall-mysql /bin/bash -c 'mysql -uroot -p123456 < /tmp/sql/lsz_mall_init.sql'

```

> crontab定时任务执行失败？  
> 参考：  
> [How to workaround “the input device is not a TTY” when using grunt-shell to invoke a script that calls docker run?](https://stackoverflow.com/questions/40536778/how-to-workaround-the-input-device-is-not-a-tty-when-using-grunt-shell-to-invo)  
> 定时任务执行时，docker exec参数中应去除-t，-t表示tty，后台执行没有终端  
> TODO 2>&1