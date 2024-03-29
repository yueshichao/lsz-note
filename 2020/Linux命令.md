# 处理器
> https://blog.csdn.net/yjclsx/article/details/81508455  

- top # 查看计算机资源，包括内存、cpu

- top -Hp pid # 查看pid进程下的线程情况

# 存储器
## 内存
- free -h # 查看计算机内存信息

## 磁盘
- df -h # 查看磁盘信息

## 文件
- find
 - find / -name "fileName"

- ls -lht # 当前文件夹总大小，以及各个文件的大小

- du
  - du --max-depth=1 -h # 查看每个文件夹情况，最大深度1
  - du -sh fileName # 当前文件大小

- ping 127.0.0.1 | tee ping.log # 执行任务并把输出保存到文件中

- tar
  - tar -tf test.tar.gz # 查看内容
  - tar -czvf test.tar.gz a.c # 压缩文件
  - tar -xzvf test.tar.gz # 解压文件

### nc
- 文件传输
  - nc -l -p 9995 > file.txt # 接收端
  - nc r receiveIP 9995 -q 0 < file.txt # 发送端

# 其他IO设备
## 网络
- tcpdump -iany -s0 udp #查看码流
- tcpdump -iany -s0 -w record.pcap # 抓所有包并保存至record.pcap
- tcpdump -iany -s0 udp src port 19020 # 抓取udp包，源端口19020
- tcpdump -iany -s0 udp | grep "[a-z|0-9|.|-]*\.19020\s>" # 抓取结果正则输出

- netstat -ntlp # 查看所有tcp端口

***
***
> 以上命令分类是根据计算机硬件分的
# 权限管理
- useradd -d /home/sam -m sam # 添加用户
- passwd sam # 设置密码
- sudo passwd root # 有些系统装好root用户没密码，需要自己设置

# 进程
- nohup command &> /dev/null # 后台执行

- ps -aux

- kill -15 PID # 结束进程，15表示正常结束，9表示强制终止

## 修改ssh默认端口
> 最近开了台云服务器，一直被暴力破解22端口和80端口
- vi /etc/ssh/sshd_config # 修改Port对应配置，假设修改为123
- firewall-cmd --zone=public --add-port=123/tcp --permanent # 开放123端口
- firewall-cmd --reload # 使配置生效
- systemctl restart sshd # 重启sshd

## 端口
- lsof -i tcp:80 # 查看80端口被哪个进程占用

## CentOS防火墙
- systemctl status firewalld # 查看防火墙状态
- systemctl start firewalld # 开启防火墙
- systemctl stop firewalld # 关闭防火墙
- firewall-cmd --zone=public --add-port=123/tcp --permanent # 开放123端口
- firewall-cmd --remove-port=123/tcp --permanent # CentOS中关闭123端口
- firewall-cmd --reload # 使配置生效
- firewall-cmd --query-port=123/tcp # 查看是否开启成功
- firewall-cmd --zone=public --list-ports # 查看所有开放的端口

# 文本处理more、tail、grep

- more test.txt | grep ">\s[a-z|0-9|.|-]*\.19[0-9]*: "

- tail -f test.txt | grep "test"

- less test.txt

- awk # TODO

# 操作系统信息
- cat /proc/version # 查看Linux版本信息

# Kali
## 打开ssh服务
1. vi /etc/ssh/ssh_config
将**PasswordAuthentication**字段注释去掉，并改值为**yes**
2. vi /etc/ssh/sshd_config
将**PermitRootLogin**字段注释去掉，并该值为**yes**
3. /etc/init.d/ssh start # 启动ssh
4. update-rc.d ssh enable  # 开机自启
> Kali版本2019.3，步骤1修改文件ssh_config（我修改的这个），我看有的博文是sshd_config（这个文件里也有PasswordAuthentication字段），懒得试了，反正都是些简单配置

## 开机不启动图形化页面
1. vi /etc/default/grub
将**GRUB_CMDLINE_LINUX**字段改为**text**
> **quiet**为图形化页面
2. update-grub
3. systemctl set-default multi-user.target # multi-user也就是以前版本中 */etc/inittab* 的 *init 3* （多用户模式）
4. init 6 # 重启

## Nmap
- nmap -PE 192.168.1.0/24 # 使用ICMP协议发现活跃主机