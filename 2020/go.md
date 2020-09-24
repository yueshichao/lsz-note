# 安装
1. windows平台，下载，安装
2. [设置go path](https://blog.csdn.net/sinat_23588337/article/details/91383659)
   1. 新建目录go_path，包含文件夹：**bin,pkg,src**
   2. 设置环境变量**GOPATH**，值为 **文件路径/go_path**
   3. 命令行输入:**go env**发现GOPATH仍然没变
   4. 命令行输入：**setx GOPATH 文件路径/go_path**
3. 配置代理
   1. go env -w GO111MODULE=on
   2. go env -w GOPROXY=https://goproxy.cn,direct


# Hello
```go
package main

import "fmt"

func main() {
	fmt.Println("Hello, World!")
}
```
> 只有标注package main才会被运行，运行方式`go run Hello.go`

# 语法基础
## 变量
```go
   // 常量
   const c = 1
	// var 变量名 类型
	var i int
	// 批量定义
	var (
		i1 int8 = 127	// 双字节整数，最大127
		i2 uint64		// 无符号8字节整数，最大 2^64 - 1
		f0 float32
		f1 []float64
		s string
		b bool
	)
	var s1 string = "s1 直接赋值"
	// 编译器根据右值推断变量类型
	var s2 = "s2 直接赋值"

	// 不使用var声明赋值变量
	j,k := 0, '1'

	// 多重赋值来交换变量（以前在Python中见过）
	s1, s2 = s2, s1

	// 匿名变量（哑元变量），标志符："_"，不能被使用，可以把任何值赋给它，它不会保留这些值
	_ = s1
```

## 字符串
```go
	s1 := "hel" + "lo"
	s2 := s1 + ", world -> s2"
	s1 += ", world -> s1"

	// 多行字符串
	s3 :=
`hello
		,world`
```

## 指针
```go
	a := 1
	var ptr *int = &a	// 指针ptr，指向a的地址
	b := *ptr			// 取ptr指向的内容，赋给b
```

## 数组
```go
// 定义数组
	var a [3]int
	// 定义并且初始化数组
	var b [3]int = [3]int{100, 200, 300};
	// 不指定长度初始化值，编译器自己判断长度
	c := [...]float32{1.1, 2.2, 3.3}


	// 下标访问
	fmt.Println(a[0])

	// for循环遍历下标和值
	for i, v :=range a {
		fmt.Printf("index = %d, value = %d\n", i, v)
	}

	// 通过匿名变量，只打印value，要不然定义了变量不用编译报错
	for _, v :=range b {
		fmt.Printf("value = %d\n", v)
	}
	for _, v :=range c {
		fmt.Printf("value = %.1f\n", v)
	}

	tmp := [...]int{0, 0, 0}
	// 判断数组是否相等
	fmt.Println(a == tmp) // true

	// 二维数组
	var aa [4][2]int
	// 三行两列的二维数组（之所有叫3行2列，是因为，一般来说，数组在内存中的线性存储顺序：00 01 10 11 20 21
	bb := [3][2]int{{00, 01}, {10, 11}, {20, 21}}
	// 初始化第0行的元素，初始化第2行第3列的元素
	cc := [][]int{0: {00, 01}, 2: {3: 23}}
	fmt.Println(aa)
	fmt.Println(bb)
	fmt.Println(cc)
	// TODO 这行代码会报错，为什么？
	//cc[1][1] = 1 // panic: runtime error: index out of range [1] with length 0

	// 并非引用复制过去，而是像C语言结构体一样，是整个数据结构复制过去
	// TODO 那么是强拷贝还是弱拷贝？
	dd := bb
	fmt.Println(dd)
```


