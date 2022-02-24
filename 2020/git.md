# 版本管理git
![git](./img/git.png)
## 分支
- git branch # 查看本地分支

- git branch -a # 查看所有分支（包括远程分支）

- git branch -vv # 查看本地分支与远程分支信息

- git checkout -b new-branch #创建并切换至新分支

- git push origin dev:dev # 将本地dev分支推到服务器上，并命名为dev

- git checkout -b develop origin/develop # 切换到远程分支，本地命名为develop

- git reset --hard origin/master # 将本地代码置为某一分支

- git reflog show --date=iso master # 查看分支创建时间

- git push origin --delete patch-3 # 删除分支

- git push origin dev # 将本地dev分支push到远程服务器上

- git fetch --all && git reset --hard origin/develop && git pull # 本地强制获取更新为远程分支

- 分支改名
  1. git branch -m old_branch new_branch # 将本地分支进行改名
  2. git push origin :old_branch # 删除老分支
  3. git push origin new_branch # 将改名后的分支push到远程

- git branch -r --contains commit-id # 查看某次提交属于某个分支

### origin操作

- git remote rm origin # 删除远程地址

- git remote add origin <url> # 设置远程地址


## 子项目
> 主项目目录下的.gitmodules文件中包含了submodule的信息  
子项目更新，主项目并不会随之更新，因为主项目指向了当时的子项目的引用

- git submodule add git@submodule.com # 为项目添加子项目

### clone（包含子项目的）项目
- 第一种方式
    1. git clone git@XXX.com # 拉取主项目
    2. git submodule init # 初始化子项目空间
    3. git submodule update # 拉取子项目代码
- 第二种方式
    1. git clone git@XXX.com --recursive # 递归拉取项目代码

## 拉取

- git pull

## 推送
- git push <远程主机名> <本地分支名>:<远程主机分支名> # 将本地修改推送至远程主机

- [本地已有的仓库推送到github](https://sword.studio/142.html)
 1. git remote add origin git@github.com:XXX/XXX.git # 设置本地的origin地址
 2. git push -u origin master

## 合并
- git merge --abort # 取消merge

## cherry-pick
- git cherry-pick commit-id # 将某分支的某次提交合并(而非分支合并)

## 撤销修改
- git checkout fileName # 撤销未add的本地修改

- git reset HEAD fileName # 撤销已add的本地修改

- git reset --hard HEAD^ # 回滚到上一次commit的版本

- git rm --cached fileName # 将暂存区文件删除(如果是文件夹，需要加上 `-rf` 参数)

## 标签tag
> 参考：[git 如何同步本地、远程的分支和tag信息](https://blog.csdn.net/wei371522/article/details/83186077)  

- git tag v1.0 # 轻量级tag

- git push origin v1.0 # 推送tag

- git tag -d v1.0 # 删除本地tag

- git push origin :refs/tags/v1.0 # 删除本地tag后，再执行此句删除远端tag

- [删除所有tag](https://www.cnblogs.com/kiancyc/p/13936890.html)
  - git push origin --delete $(git tag -l) # 删除所有服务器端的tag
  - git tag -d $(git tag -l) # 删除所有本地tag

- git ls-remote --tags origin # 查询所有远程tag

- git fetch origin tagName # 拉取远程指定tag

示例，拉取redis指定tag
```bash
git clone --branch 6.0 --depth=1 https://github.com/redis/redis.git
```

- 回退到某tag
    - git show tag-v1.0 # 找到该tag对应的commit id
    - git reset --hard commit-id # 回退到该版本

## 其他
- git status # 本地工作区状态

- git config --global user.name lsz # 设置提交用户名

- git mv file.txt File.txt # git修改文件名大小写
> 参考：https://www.cnblogs.com/samwang88/p/6611947.html  
> git默认大小写不敏感，要修改文件大小写可以使用这种方式  

- git config --global http.proxy socks5://127.0.0.1:1080 # 设置全局代理
> 参考：https://blog.csdn.net/default7/article/details/100068256  

- git clean -fd
> 参考：[git删除未跟踪文件](https://blog.csdn.net/uhippo/article/details/46365737)  


## 恢复已被删除的本地分支

- git reflog --date=iso | grep '关键字' # 找到提交的id

- git branch dev-back 提交id # 那次版本会恢复为dev-back分支

## [配置多个ssh-key](https://www.jianshu.com/p/d6c6f37fb4f1)
1. 假设已有一个ssh-key存在，文件位置：`~\.ssh\id_rsa`

2. ssh-keygen -t rsa -C "你的邮箱@xxx.com" # 生成第二个

> 选择文件保存位置： D:\KIT\ssh-key\id_rsa  

3. 进入git安装目录，修改目录下的etc/ssh/ssh_config文件，新增两个Host配置，指定访问哪个git服务器，用哪个文件夹的ssh-key  

```conf
Host gitlab.com
    HostName gitlab.com
    User leshizhao
    PreferredAuthentications publickey
    IdentityFile ~/.ssh/id_rsa
	
Host github.com
    HostName github.com
    User leshizhao
    PreferredAuthentications publickey
    IdentityFile D:/KIT/ssh-key/id_rsa
```

# 常见gitignore配置
```conf
# Compiled class file
*.class

# Log file
*.log

# Package Files #
*.jar
*.war
*.nar
*.ear
*.zip
*.tar.gz
*.rar

### Maven template
target/*

# IDEA
.idea/*
*.iml
```

# 设置IDEA使git不追踪指定文件
> 参考：[IDEA忽略文件，防止git提交不想提交的文件](https://my.oschina.net/u/4395893/blog/3318210)  

首先，将文件从暂存区移除，IDEA里文件颜色应该是红色（未被add到git暂存区）  
在Version Control选项卡中右键，选择ignore，我选择的仅ignore该文件  

> 谨以此方式以及记录表达公司（小组成员）不让我创建application-local.yml文件的愤怒😡！  
