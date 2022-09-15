# 异常

## this authentication plugin is not supported

> https://blog.csdn.net/Charliewolf/article/details/82556583  

gorm连接数据库报错
```log
<nil>; connect to localhost:3306 err: this authentication plugin is not supported
```

是8.0版本加密方式变了

```sql
select host,user,plugin from mysql.user;
alter user root@127.0.0.1 identified with mysql_native_password by '123456';
```


