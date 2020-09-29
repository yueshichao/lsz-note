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


## 切片
```go
var a = [10]int{3: 3, 5: 5, 9: 9}
	fmt.Println(a)
	// 切片，在数组a上，截取[3,7)区间的元素，构成切片返回给slice1。
	// 数据不是拷贝，slice1指针指向a[3]内存区域，因为改变a[3]的值，slice1[0]的值也变了
	slice1 := a[3:7]
	fmt.Println(slice1)
	// 缺省开始位置，表示从0开始切
	sliceEnd := a[:10]
	fmt.Println(sliceEnd)
	// 缺省结束位置，表示切到结束
	sliceStart := a[3:]
	fmt.Println(sliceStart)

	// slice类型声明	var sliceName []sliceType
	var slice2 []int = a[:]
	fmt.Println(slice2)
	// make函数生成切片，函数原型：make( []Type, size, cap )
	// 如果size > cap，初始化会报错：len larger than cap in make([]int)
	slice3 := make([]int, 2, 10)
	fmt.Println(slice3)

	// append不会对原slice操作，append之后返回新的slice
	slice4 := append(slice3, 100)
	fmt.Println(slice4)
	// 对slice3[0]改变后，slice4[0]的值也变为了-1
	// TODO 猜测slice实际保存了开始指针和空间大小。每次切片，实际是新建指针指向内存区域
	slice3[0] = -1
	fmt.Println(slice4)
	// append返回slice，所以可以继续当做参数append，这个链式调用不太优雅啊
	slice5 := append(append(slice4, 1), 2)
	fmt.Println(slice5)

	// 解包：将切片变成参数列表，切片像python，解包像es，不过es的三点运算符在变量之前
	slice6 := append(slice4, slice3...)
	fmt.Println(slice6)

	// 如果说切数组是移动指针实现的，那么append是怎么实现的
	// 分别在base切片上append，总不能直接扩展内存区域吧
	// TODO 猜测：链表实现的append
	sliceBase := []int{1, 2, 3}[:]
	fmt.Println(sliceBase)
	sliceAppend0 := append(sliceBase, -1)
	fmt.Println(sliceAppend0)
	sliceAppend1 := append(sliceBase, 1)
	fmt.Println(sliceAppend1)

	// copy(dst, src) 把src的[0, min(len(dst), len(src)))区间的元素复制给dst
	slice7 := []int{1, 2, 3}
	slice8 := []int{10, 20, 30, 40, 50}
	copy(slice7, slice8)
	fmt.Println(slice7, slice8)

	// go本身没有切片删除操作
	slice9 := []int{1, 2, 3, 4, 5, 6, 7, 8, 9}
	// 删除前3个元素
	slice9 = slice9[3:]
	fmt.Println(slice9)
	// 删除最后3个元素
	slice9 = slice9[:len(slice9)-3]
	fmt.Println(slice9)
	// 删除第1个元素
	slice9 = append(slice9[:1], slice9[2:]...)
	fmt.Println(slice9)

	// 二维切片
	slice10 := [][]int{{10}, {100, 200}}
	// 二维切片赋值
	slice10[0] = append(slice10[0], 20, 30)
	fmt.Println(slice10)
```


## map
```go
// 初始化声明一个map
	var map0 map[string]int = map[string]int{"one": 1}
	map0["two"] = 2
	fmt.Println(map0)

	map1 := make(map[string]float32)
	map1["one"] = 0.1
	fmt.Println(map1)

	// 数字 - 数组 的map
	int2Arr := map[int][]int{}
	int2Arr[0] = []int{0, 1, 2}
	int2Arr[1] = []int{1, 2, 3}
	fmt.Println(int2Arr)

	// range遍历map
	for k, v := range int2Arr {
		// %v占位符对应值相应格式
		fmt.Printf("k = %d, v = %v\n", k, v)
	}

	// 删除key
	delete(map0, "one")
	fmt.Println(map0)
```

## list
```go
	// 初始化list
	list0 := list.New()
	list0.PushBack(1)
	list0.PushFront(0)
	// for循环遍历list
	for i := list0.Front(); i != nil; i = i.Next() {
		fmt.Printf("value = %v\t", i.Value)
	}
	fmt.Println()

	// 另一种初始化list的方式
	var list1 list.List
	// 拿到元素指针
	baseRef := list1.PushBack("base")
	// 在baseRef指向的元素后插入 "abc"
	list1.InsertAfter("abc", baseRef)
	// 在baseRef指向的元素前插入 "123"
	list1.InsertBefore("123", baseRef)
	for i := list1.Front(); i != nil; i = i.Next() {
		fmt.Printf("value = %v\t", i.Value)
	}
	fmt.Println()
```


