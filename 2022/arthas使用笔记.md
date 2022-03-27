> 参考：  
> [Arthas 用户文档](https://arthas.aliyun.com/doc/index.html)  
>   

# 下载，运行

- curl -O https://arthas.aliyun.com/arthas-boot.jar

- java -jar arthas-boot.jar

# 指令

## thread

## jad

## watch

1. watch demo.MathGame primeFactors returnObj

## sc

1. 模糊搜索：sc demo.*
2. 详细信息：sc -d demo.MathGame

## trace

1. trace demo.MathGame run -n 1

## stack

1. stack demo.MathGame primeFactors

# 实战

## 排查dubbo

> https://github.com/alibaba/arthas/issues/327  


- trace com.alibaba.dubbo.rpc.Filter *
