
如果不知道有哪些命令，就直接输入go mod，看提示

## 查看依赖路径

go mod why -m 'pkg.name'


## 清理本地依赖包缓存

go clean --modcache