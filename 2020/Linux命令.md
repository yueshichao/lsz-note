# 用户管理
- useradd -d /home/sam -m sam # 添加用户
- passwd sam # 设置密码

# 网络
- tcpdump -iany -s0 udp #查看码流
- tcpdump -iany -s0 -w record.pcap # 抓所有包并保存至record.pcap
- tcpdump -iany -s0 udp src port 19020 # 抓取udp包，源端口19020
- tcpdump -iany -s0 udp | grep "[a-z|0-9|.|-]*\.19020\s>" # 抓取结果正则输出

# 文件相关
- find
 - find / -name "fileName"

## 文本处理more、tail、grep

- more test.txt | grep ">\s[a-z|0-9|.|-]*\.19[0-9]*: "

- tail -f test.txt | grep "test"

- less test.txt

# 系统

- top # 查看计算机资源，包括内存、cpu

- free -h # 查看计算机内存信息

- ps -aux

- kill -15 PID # 结束进程，15表示正常结束，9表示强制终止
