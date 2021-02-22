# 导入外部项目，gradle wrapper包下载非常慢
> https://blog.hissummer.com/2016/10/gradle-wrapper-%e4%b8%8b%e8%bd%bdgradle%e9%80%9f%e5%ba%a6%e8%b6%85%e7%ba%a7%e6%85%a2%e5%a6%82%e4%bd%95%e8%a7%a3%e5%86%b3%ef%bc%9f/

由于墙的原因，你懂的。
gradle wrapper原理就是新项目构建时，下载源码发布者指定版本的gradle  
但是国内下载不了，比龟速还慢，你可以下载好指定版本，然后修改文件`gradle-wrapper.properties`  
```properties
distributionUrl=file\:下载好文件的绝对路径
```

也可以使用本地gradle去构建项目，不过不推荐，毕竟版本不一致，谁也不知道会发生什么。

另一种不用改任何东西，只要你运行**gradlew**时，手动 Ctrl + C 终止  
> 我这里需要的版本是**gradle-6.2.2-all**  

然后去你用户目录/.gradle/wrapper/dists，手动下载压缩包放入**gradle-6.2.2-all**目录下的一个看似乱码的文件夹  
我这里是`a85vgba9ir879tdzkyeii4ffj`  
压缩包地址就是`gradle-wrapper.properties`文件中`distributionUrl`属性值  