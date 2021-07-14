> 参考：  
> [不要用JWT替代session管理（上）：全面了解Token,JWT,OAuth,SAML,SSO](https://zhuanlan.zhihu.com/p/38942172)  
> [cookie 和 session 到底是什么](https://zhuanlan.zhihu.com/p/105088923)


- HTTP是无状态的，发出一个request，接到一个response，但WEB交互有时需要状态


# Cookie & Session

cookie数据存放在浏览器，用来记录一些值，比如name=lsz  

session数据放在服务器，是服务端为了区分不同客户端而定义的概念  

两者联合使用可以实现会话管理（在HTTP无状态的基础上实现有状态的会话通信）  

基本流程：

1. 客户端请求
2. 服务端接到请求，生成Session（ID为123456），并设置浏览器Cookie为JSESSIONID=123456
3. 客户端之后每次带着JSESSIONID来，服务端就知道谁来了
4. 服务端也可以根据JSESSIONID存放一些会话数据，比如用户选择了什么



# JWT - JSON WEB TOKEN

本质上来讲是个**token**，使用流程一般是：

1. 客户端帐号密码登录
2. 服务端校验，生成jwt返回
3. 客户端使用

jwt由三部分组成：

1. header

用于描述基本信息

```json
{
    "typ": "JWT",
    "alg": "HS256"
}
```

2. payload

载荷，用于存放用户信息

```json
{
    "userId": "b08f86af-35da-48f2-8fab-cef3904660bd"
}
```

3. signature

签名，摘要算法算出的值，用于校验信息

```js
// signature algorithm
data = base64urlEncode( header ) + "." + base64urlEncode( payload )
signature = Hash( data, secret );
```



最终

```js
jwt = base64urlEncode( header ) + "." + base64urlEncode( payload ) + signature
```

可以看出header、payload都没加密（base64叫编码可能更妥当），唯一加密（或许叫签名更好些，也更符合英文）的是signature，**如果你没有密钥，你是无法验证signature的正确性的**。jwt是服务器生成的，也是服务器验证的，所以**jwt的意义仅在于服务器知道这是自己发出去**