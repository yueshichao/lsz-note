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

多级map
```go
func main() {
	m := make(map[string]map[string]int)
	m["1"] = map[string]int{"2": 3}
	println(m["1"]["2"])
}
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


### 控制、循环

循环语句
```go
for i := 0; i < 10; i++ {
	sum += i
}
// while
for sum < 1000 {
	sum += sum
}
// 无限循环
for {
}
```

控制语句
```go
if x < 0 {
	return sqrt(-x) + "i"
}
// 条件判断前可执行简单语句
if v := math.Pow(x, n); v < lim {
	return v
}
// switch case
switch os := runtime.GOOS; os {
case "darwin":
	fmt.Println("OS X.")
case "linux":
	fmt.Println("Linux.")
default:
	// freebsd, openbsd,
	// plan9, windows...
	fmt.Printf("%s.\n", os)
}
// switch case 代替 if then else
t := time.Now()
switch {
case t.Hour() < 12:
	fmt.Println("Good morning!")
case t.Hour() < 17:
	fmt.Println("Good afternoon.")
default:
	fmt.Println("Good evening.")
}

```

# defer

```go
func main() {
	defer fmt.Println("world")
	fmt.Println("hello")
}
```


defer函数会按顺序压入调用栈
```go
func main() {
	fmt.Println("counting")
	// 输出顺序是9876543210
	for i := 0; i < 10; i++ {
		defer fmt.Println(i)
	}

	fmt.Println("done")
}
```


# 指针

```go
func main() {
	i, j := 42, 2701

	p := &i         // 指向 i
	fmt.Println(*p) // 通过指针读取 i 的值
	*p = 21         // 通过指针设置 i 的值
	fmt.Println(i)  // 查看 i 的值

	p = &j         // 指向 j
	*p = *p / 37   // 通过指针对 j 进行除法运算
	fmt.Println(j) // 查看 j 的值
}
```

# 结构体

```go
type Vertex struct {
	X int
	Y int
}

func main() {
	fmt.Println(Vertex{1, 2})
}
```

结构体指针取值
```go
func main() {
	v := Vertex{1, 2}
	p := &v
	// 严格的写法：(*p).X，但给了语法糖，可以按如下写法
	p.X = 1e9
	fmt.Println(v)
}
```

各种结构体文法
```go
var (
	v1 = Vertex{1, 2}  // 创建一个 Vertex 类型的结构体
	v2 = Vertex{X: 1}  // Y:0 被隐式地赋予
	v3 = Vertex{}      // X:0 Y:0
	p  = &Vertex{1, 2} // 创建一个 *Vertex 类型的结构体（指针）
)

func main() {
	fmt.Println(v1, p, v2, v3)
}

```

# 函数式

```go
func compute(fn func(float64, float64) float64) float64 {
	return fn(3, 4)
}

func main() {
	hypot := func(x, y float64) float64 {
		return math.Sqrt(x*x + y*y)
	}
	fmt.Println(hypot(5, 12))

	fmt.Println(compute(hypot))
	fmt.Println(compute(math.Pow))
}

```

## 闭包

TODO 
https://tour.go-zh.org/moretypes/25

# 类型

## 类型断言

```go
func main() {
	var i interface{} = "hello"

	s := i.(string)
	fmt.Println(s)

	s, ok := i.(string)
	fmt.Println(s, ok)

	f, ok := i.(float64)
	fmt.Println(f, ok)

	f = i.(float64) // 报错(panic)
	fmt.Println(f)
}
```

## 类型选择

```go
switch v := i.(type) {
case T:
    // v 的类型为 T
case S:
    // v 的类型为 S
default:
    // 没有匹配，v 与 i 的类型相同
}
```

## 重写Stringer接口

```go
type IPAddr [4]byte

// TODO: 给 IPAddr 添加一个 "String() string" 方法
func (ip IPAddr) String() string {
	return fmt.Sprintf("%d.%d.%d.%d", ip[0], ip[1], ip[2], ip[3])
}

func main() {
	hosts := map[string]IPAddr{
		"loopback":  {127, 0, 0, 1},
		"googleDNS": {8, 8, 8, 8},
	}
	for name, ip := range hosts {
		fmt.Printf("%v: %v\n", name, ip)
	}
}
```


## 重写error
```go
package main

import (
	"fmt"
	"math"
)
// 定义error
type ErrNegativeSqrt float64
// 重写error输出
func (e ErrNegativeSqrt) Error() string {
	return fmt.Sprintf("cannot Sqrt negative number: %f", e)
}
// 返回error
func Sqrt(x float64) (float64, error) {
	if x < 0 {
		return x, ErrNegativeSqrt(x)
	}
	return math.Sqrt(x), nil
}

func main() {
	fmt.Println(Sqrt(2))
	fmt.Println(Sqrt(-2))
}

```


# Reader

TODO 


# go程


# 信道


// TODO 这句话什么意思？
默认情况下，发送和接收操作在另一端准备好之前都会阻塞。这使得 Go 程可以在没有显式的锁或竞态变量的情况下进行同步。

```go

func sum(s []int, c chan int) {
	sum := 0
	for _, v := range s {
		sum += v
	}
	c <- sum // 将和送入 c
}

func main() {
	s := []int{7, 2, 8, -9, 4, 0}

	c := make(chan int)
	go sum(s[:len(s)/2], c)
	go sum(s[len(s)/2:], c)
	x, y := <-c, <-c // 从 c 中接收

	fmt.Println(x, y, x+y)
}

```

缓冲区满，发生死锁：`all goroutines are asleep - deadlock!`
```go
func main() {
	ch := make(chan int, 1)
	ch <- 1
	ch <- 2
	fmt.Println(<-ch)
	fmt.Println(<-ch)
}

```

## select

TODO 阻塞到某个分支可以执行为止


## Ticker

```go
func testTicker() {
	ticker := time.NewTicker(1 * time.Second)
	i := uint64(1)
	for range ticker.C {
		fmt.Println(i)
		i = atomic.AddUint64(&i, 1)
	}
	fmt.Println("end...")
}

```

# 协程


## WaitGroup

```go
import (
	"fmt"
	"strconv"
	"sync"
)

var cnt = 0

func f(wait *sync.WaitGroup) {
	cnt++
	fmt.Println("f" + strconv.Itoa(cnt))
	wait.Done()
}

func main() {
	var wait sync.WaitGroup
	for i := 0; i < 10; i++ {
		wait.Add(1)
		go f(&wait)
	}
	wait.Wait()
	fmt.Println("all end...")
}
```

# WEB服务器

```go
func main() {
	http.HandleFunc("/", sayHello)
	err := http.ListenAndServe(":9090", nil) // 设置监听的端口
	if err != nil {
		log.Fatal(err)
	}
}

func sayHello(w http.ResponseWriter, r *http.Request) {
	fmt.Fprintf(w, "Hello!")
}
```

通过curl测试
```bash
curl "http://127.0.0.1:9090/"
```

# 序列化

## json

```go
func testJson() {
	cat := Cat{Name: "cat", Dog: Dog{
		Age: 1,
	}}
	bs, _ := json.Marshal(cat)
	jsonStr := string(bs)
	fmt.Printf("json = %v\n", jsonStr)

	var cat1 Cat
	bytes := []byte(jsonStr)
	_ = json.Unmarshal(bytes, &cat1)
	fmt.Printf("struct = %v\n", cat1)
}

```