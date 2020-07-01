## 将Spring Cloud项目打包成jar包
- 修改启动类项目下的pom.xml，设置project-packaging标签为jar
```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xmlns="http://maven.apache.org/POM/4.0.0"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <packaging>jar</packaging>

</project>
```

- 设置project-build标签
```xml
<build>
    <finalName>server</finalName>
    <plugins>
        <plugin>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-maven-plugin</artifactId>
            <version>2.0.3.RELEASE</version>
            <configuration>
                <mainClass>com.lsz.Application</mainClass>
            </configuration>
            <executions>
                <execution>
                    <goals>
                        <goal>repackage</goal>
                    </goals>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```

## 打包自己项目到Maven私服
1. 设置账号密码

更改maven_home/conf下setting.xml
```xml
</servers>
    <server>
        <id>server-id</id>
        <username>lsz</username>
        <password>lsz</password>
    </server>
</servers>
```

2. 设置jar包目的地址
更改项目文件pom.xml，distributionManagement.snapshotRepository标签中的server-id需与上面(settings.xml)相同
```xml
<developers>
    <developer>
        <name>lsz</name>
        <email>lsz@XXX.com</email>
        <roles>
            <role>developer</role>
        </roles>
        <timezone>+8</timezone>
    </developer>
</developers>

<distributionManagement>
    <snapshotRepository>
        <id>server-id</id>
        <url>http://server/url/</url>
    </snapshotRepository>
</distributionManagement>
```

3. 执行maven命令
> mvn -DskipTests=true deploy

4. 问题
> 401是权限问题
> 400是已有相同版本号

## 编译跳过javadoc、编译时带着源码
```xml
<build>

<plugins>
    <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-javadoc-plugin</artifactId>
        <!--<version>2.10.4</version>-->
        <configuration>
            <skip>true</skip>
        </configuration>
    </plugin>
    <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-source-plugin</artifactId>
        <executions>
            <execution>
                <id>attach-sources</id>
                <goals>
                    <goal>jar</goal>
                </goals>
            </execution>
        </executions>
    </plugin>
</plugins>

</build>
```