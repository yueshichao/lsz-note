
# 快速实现go接口

> https://www.bilibili.com/video/BV19e4y1A7DD

```go

type ImplStrcut struct {}

var _ SomeInterface = (*ImplStrcut)(nil)

```