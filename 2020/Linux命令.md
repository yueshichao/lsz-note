# 处理器
- top # 查看计算机资源，包括内存、cpu

# 存储器
## 内存
- free -h # 查看计算机内存信息

## 磁盘
- df -h # 查看磁盘信息

### 文件
- find
 - find / -name "fileName"

- ls -lht # 当前文件夹总大小，以及各个文件的大小

- du
  - du --max-depth=1 -h # 查看每个文件夹情况，最大深度1
  - du -sh fileName # 当前文件大小

- ping 127.0.0.1 | tee ping.log # 执行任务并把输出保存到文件中

# 其他IO设备
## 网络
- tcpdump -iany -s0 udp #查看码流
- tcpdump -iany -s0 -w record.pcap # 抓所有包并保存至record.pcap
- tcpdump -iany -s0 udp src port 19020 # 抓取udp包，源端口19020
- tcpdump -iany -s0 udp | grep "[a-z|0-9|.|-]*\.19020\s>" # 抓取结果正则输出

***
***
> 以上命令分类是根据计算机硬件分的
# 权限管理
- useradd -d /home/sam -m sam # 添加用户
- passwd sam # 设置密码

# 进程
- nohup command &> /dev/null # 后台执行

- ps -aux

- kill -15 PID # 结束进程，15表示正常结束，9表示强制终止

# 文本处理more、tail、grep

- more test.txt | grep ">\s[a-z|0-9|.|-]*\.19[0-9]*: "

- tail -f test.txt | grep "test"

- less test.txt

- awk # TODO